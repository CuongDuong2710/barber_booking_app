package dev.quoccuong.barberbooking.Fragment;


import android.os.Bundle;
import android.quoccuong.barberbooking.R;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.AccountKit;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dev.quoccuong.barberbooking.Adapter.HomeSliderAdapter;
import dev.quoccuong.barberbooking.Common.Common;
import dev.quoccuong.barberbooking.Interface.IBannerLoadListener;
import dev.quoccuong.barberbooking.Interface.ILookBookLoadListener;
import dev.quoccuong.barberbooking.Model.Banner;
import dev.quoccuong.barberbooking.Service.PicassoImageLoadingService;
import ss.com.bannerslider.Slider;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements IBannerLoadListener, ILookBookLoadListener {

    private Unbinder unbinder;

    @BindView(R.id.layout_user_information)
    LinearLayout layoutUserInformation;
    @BindView(R.id.txt_user_name)
    TextView txtUserName;
    @BindView(R.id.banner_slider)
    Slider bannerSlider;
    @BindView(R.id.recycler_look_book)
    RecyclerView recyclerViewLookBook;

    // Firestore collection
    CollectionReference bannerRef, lookBookRef;

    // interface
    IBannerLoadListener iBannerLoadListener;
    ILookBookLoadListener iLookBookLoadListener;

    public HomeFragment() {
        bannerRef = FirebaseFirestore.getInstance().collection("Banner");
        lookBookRef = FirebaseFirestore.getInstance().collection("Lookbook");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);

        // Init
        Slider.init(new PicassoImageLoadingService());
        iBannerLoadListener = this;
        iLookBookLoadListener = this;

        // check user is logged in
        if (AccountKit.getCurrentAccessToken() != null) {
            setUserInformation();
            loadBanner();
        }

        return view;
    }

    private void loadBanner() {
        bannerRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Banner> banners = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot bannerSnapshot : task.getResult()) {
                                Banner banner = bannerSnapshot.toObject(Banner.class);
                                banners.add(banner);
                            }
                            iBannerLoadListener.onBannerLoadSuccess(banners);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iBannerLoadListener.onBannerLoadFailed(e.getMessage());
            }
        });
    }

    private void setUserInformation() {
        layoutUserInformation.setVisibility(View.VISIBLE);
        txtUserName.setText(Common.currentUser.getName());
    }

    @Override
    public void onBannerLoadSuccess(List<Banner> banners) {
        bannerSlider.setAdapter(new HomeSliderAdapter(banners));
    }

    @Override
    public void onBannerLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLookBookLoadSuccess(List<Banner> banners) {

    }

    @Override
    public void onLookBookLoadFailed(String message) {

    }
}
