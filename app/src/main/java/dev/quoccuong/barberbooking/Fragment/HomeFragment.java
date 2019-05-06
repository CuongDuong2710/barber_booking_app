package dev.quoccuong.barberbooking.Fragment;


import android.os.Bundle;
import android.quoccuong.barberbooking.R;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.accountkit.AccountKit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dev.quoccuong.barberbooking.Common.Common;
import dev.quoccuong.barberbooking.Service.PicassoImageLoadingService;
import ss.com.bannerslider.Slider;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private Unbinder unbinder;

    @BindView(R.id.layout_user_information)
    LinearLayout layoutUserInformation;
    @BindView(R.id.txt_user_name)
    TextView txtUserName;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);

        // Init
        Slider.init(new PicassoImageLoadingService());

        // check user is logged in
        if (AccountKit.getCurrentAccessToken() != null) {
            setUserInformation();
        }

        return view;
    }

    private void setUserInformation() {
        layoutUserInformation.setVisibility(View.VISIBLE);
        txtUserName.setText(Common.currentUser.getName());
    }

}
