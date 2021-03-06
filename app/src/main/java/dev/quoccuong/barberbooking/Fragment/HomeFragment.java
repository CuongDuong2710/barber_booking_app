package dev.quoccuong.barberbooking.Fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.quoccuong.barberbooking.R;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.AccountKit;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dev.quoccuong.barberbooking.Adapter.HomeSliderAdapter;
import dev.quoccuong.barberbooking.Adapter.LookBookAdapter;
import dev.quoccuong.barberbooking.BookingActivity;
import dev.quoccuong.barberbooking.Common.Common;
import dev.quoccuong.barberbooking.Interface.IBannerLoadListener;
import dev.quoccuong.barberbooking.Interface.IBookingInfoLoadListener;
import dev.quoccuong.barberbooking.Interface.IBookingInformationChangeListener;
import dev.quoccuong.barberbooking.Interface.ILookBookLoadListener;
import dev.quoccuong.barberbooking.Model.Banner;
import dev.quoccuong.barberbooking.Model.BookingInformation;
import dev.quoccuong.barberbooking.Model.LookBook;
import dev.quoccuong.barberbooking.Service.PicassoImageLoadingService;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import ss.com.bannerslider.Slider;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements IBannerLoadListener, ILookBookLoadListener, IBookingInfoLoadListener, IBookingInformationChangeListener {

    private Unbinder unbinder;
    AlertDialog dialog;

    // Firestore collection
    CollectionReference bannerRef, lookBookRef;

    // interface
    IBannerLoadListener iBannerLoadListener;
    ILookBookLoadListener iLookBookLoadListener;
    IBookingInfoLoadListener iBookingInfoLoadListener;
    IBookingInformationChangeListener iBookingInformationChangeListener;

    @BindView(R.id.layout_user_information)
    LinearLayout layoutUserInformation;
    @BindView(R.id.txt_user_name)
    TextView txtUserName;
    @BindView(R.id.banner_slider)
    Slider bannerSlider;
    @BindView(R.id.recycler_look_book)
    RecyclerView recyclerViewLookBook;

    @BindView(R.id.card_booking_info)
    CardView cardViewBookingInfo;
    @BindView(R.id.txt_salon_address)
    TextView txtSalonAddress;
    @BindView(R.id.txt_salon_barber)
    TextView txtSalonBarber;
    @BindView(R.id.txt_time)
    TextView txtTime;
    @BindView(R.id.txt_time_remain)
    TextView txtTimeRemain;
    @BindView(R.id.btn_delete_booking)
    Button btnDeleteBooking;

    @OnClick(R.id.btn_delete_booking)
    void deleteBooking() {
        deleteBookingFromBarber(false);
    }

    @OnClick(R.id.btn_change_booking)
    void changeBooking() {
        changeBookingFromBarber();
    }

    private void changeBookingFromBarber() {
        final android.support.v7.app.AlertDialog.Builder confirmDialog = new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setTitle("Hey!")
                .setMessage("Do you really want to change booking information?\nBecause we will delete your old booking. Just confirm!")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteBookingFromBarber(true);
                    }
                });
        confirmDialog.show();
    }

    private void deleteBookingFromBarber(final boolean isChange) {
        /* First, we delete from Barber collection -> User collection -> event calendar */

//        /AllSalon/NewYork/Branch/4sUEoGnpzMwY8ts5AbbQ/Barbers/7CJWD7YIvFtOyNbgOzcS/12_06_2019/0
        // load Common.currentBooking because we need some data from BookingInformation
        if (Common.currentBooking != null) {
            dialog.show();
            // get Booking information in barber object
            DocumentReference barberBookingInfo = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(Common.currentBooking.getCity())
                    .collection("Branch")
                    .document(Common.currentBooking.getSalonId())
                    .collection("Barbers")
                    .document(Common.currentBooking.getBarberId())
                    .collection(Common.convertTimeStampToStringKey(Common.currentBooking.getTimestamp()))
                    .document(Common.currentBooking.getSlot().toString());

            // when we have document, delete it
            barberBookingInfo.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // after delete on Barber done
                    // delete from User
                    Log.e("cuong", "deleteBookingFromBarber -> onSuccess");
                    deleteBookingFromUser(isChange);
                }
            });
        } else {
            if (dialog.isShowing())
                dialog.dismiss();
            Toast.makeText(getContext(), "Current booking must not be null", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteBookingFromUser(final boolean isChange) {
        // get information from User object
        if (!TextUtils.isEmpty(Common.currentBookingId)) {
            DocumentReference userBookingInfo = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(Common.currentUser.getPhoneNumber())
                    .collection("Booking")
                    .document(Common.currentBookingId);

            userBookingInfo.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // after delete on "User", delete events from Calendar
                    // First, get save Uri of event from cache
                    Log.e("cuong", "deleteBookingFromUser -> onSuccess");
                    Paper.init(getActivity());
                    Uri eventUri = Uri.parse(Paper.book().read(Common.EVENT_URI_CACHE).toString());
                    getActivity().getContentResolver().delete(eventUri, null, null);

                    Toast.makeText(getContext(), "Success delete booking!", Toast.LENGTH_SHORT).show();


                    // refresh
                    loadUserBooking();

                    // check if isChange -> call from change button, fire interface listener
                    if (isChange)
                        iBookingInformationChangeListener.onBookingInformationChange();

                    if (dialog.isShowing())
                        dialog.dismiss();
                }
            });
        }
    }

    public HomeFragment() {
        bannerRef = FirebaseFirestore.getInstance().collection("Banner");
        lookBookRef = FirebaseFirestore.getInstance().collection("Lookbook");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserBooking();
    }

    private void loadUserBooking() {
        final CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.currentUser.getPhoneNumber())
                .collection("Booking");

        // get current date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.MINUTE, 0);

        Timestamp toDayTimestamp = new Timestamp(calendar.getTime());

        // select booking information from Firebase with filter done = false and timestamp greater today
        // -> need add index to Firebase
        userBooking
                .whereGreaterThanOrEqualTo("timestamp", toDayTimestamp)
                .whereEqualTo("done", false)
                .limit(1) // only take 1
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                    BookingInformation bookingInformation = snapshot.toObject(BookingInformation.class);
                                    iBookingInfoLoadListener.onBookingInfoLoadSuccess(bookingInformation, snapshot.getId());
                                    break; // exit loop as soon as
                                }
                            } else {
                                iBookingInfoLoadListener.onBookingInfoLoadEmpty();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iBookingInfoLoadListener.onBookingInfoLoadFailed(e.getMessage());
            }
        });
    }

    @OnClick(R.id.card_view_booking)
    void booking() {
        startActivity(new Intent(getActivity(), BookingActivity.class));
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
        iBookingInfoLoadListener = this;
        iBookingInformationChangeListener = this;

        // check user is logged in
        if (AccountKit.getCurrentAccessToken() != null) {
            setUserInformation();
            loadBanner();
            loadLookBook();
            loadUserBooking();
        }

        return view;
    }

    private void loadLookBook() {
        lookBookRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<LookBook> lookBooks = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot lookBookSnapshot : task.getResult()) {
                                LookBook lookBook = lookBookSnapshot.toObject(LookBook.class);
                                lookBooks.add(lookBook);
                            }
                            iLookBookLoadListener.onLookBookLoadSuccess(lookBooks);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iLookBookLoadListener.onLookBookLoadFailed(e.getMessage());
            }
        });
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
    public void onLookBookLoadSuccess(List<LookBook> lookBooks) {
        recyclerViewLookBook.setHasFixedSize(true);
        recyclerViewLookBook.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewLookBook.setAdapter(new LookBookAdapter(getActivity(), lookBooks));
    }

    @Override
    public void onLookBookLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookingInfoLoadEmpty() {
        cardViewBookingInfo.setVisibility(View.GONE);
    }

    @Override
    public void onBookingInfoLoadSuccess(BookingInformation bookingInformation, String bookingId) {

        Common.currentBooking = bookingInformation;
        Common.currentBookingId = bookingId;

        cardViewBookingInfo.setVisibility(View.VISIBLE);
        txtSalonAddress.setText(bookingInformation.getSalonAddress());
        txtSalonBarber.setText(bookingInformation.getBarberName());
        txtTime.setText(bookingInformation.getTime());
        String dateRemain = DateUtils.getRelativeTimeSpanString(
                Long.valueOf(bookingInformation.getTimestamp().toDate().getTime()),
                Calendar.getInstance().getTimeInMillis(), 0).toString();
        txtTimeRemain.setText(dateRemain);
    }

    @Override
    public void onBookingInfoLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookingInformationChange() {
        startActivity(new Intent(getActivity(), BookingActivity.class));
    }
}
