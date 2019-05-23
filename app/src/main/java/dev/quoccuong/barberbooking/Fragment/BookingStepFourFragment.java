package dev.quoccuong.barberbooking.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.quoccuong.barberbooking.R;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dev.quoccuong.barberbooking.Common.Common;
import dev.quoccuong.barberbooking.Model.BookingInformation;

public class BookingStepFourFragment extends Fragment {

    static BookingStepFourFragment instance;

    SimpleDateFormat simpleDateFormat;
    LocalBroadcastManager localBroadcastManager;

    Unbinder unbinder;

    @BindView(R.id.txt_booking_barber_text)
    TextView txtBarberName;
    @BindView(R.id.txt_booking_time_text)
    TextView txtBookingTime;
    @BindView(R.id.txt_salon_address)
    TextView txtSalonAddress;
    @BindView(R.id.txt_salon_name)
    TextView txtSalonName;
    @BindView(R.id.txt_salon_open_hours)
    TextView txtSalonOpenHours;
    @BindView(R.id.txt_salon_phone)
    TextView txtSalonPhone;
    @BindView(R.id.txt_salon_website)
    TextView txtSalonWebsite;
    @BindView(R.id.btn_confirm)
    Button btnConfirm;

    @OnClick(R.id.btn_confirm)
    void confirmBooking() {
        BookingInformation bookingInformation = new BookingInformation();



        bookingInformation.setDone(false); // always false, use to filter display on user
        bookingInformation.setBarberId(Common.currentBarber.getBarberID());
        bookingInformation.setBarberName(Common.currentBarber.getName());
        bookingInformation.setCustomerName(Common.currentUser.getName());
        bookingInformation.setCustomerPhone(Common.currentUser.getPhoneNumber());
        bookingInformation.setSalonId(Common.currentSalon.getSalonId());
        bookingInformation.setSalonName(Common.currentSalon.getName());
        bookingInformation.setSalonAddress(Common.currentSalon.getAddress());
        bookingInformation.setTime(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                .append(" at ")
                .append(simpleDateFormat.format(Common.bookingDate.getTime())).toString());
        bookingInformation.setSlot(Long.valueOf(Common.currentTimeSlot));

        // submit to Barber document
        DocumentReference bookingDate = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberID())
                .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                .document(String.valueOf(Common.currentTimeSlot));

        // write data
        bookingDate.set(bookingInformation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        resetStaticData();
                        getActivity().finish();
                        Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetStaticData() {
        Common.step = 0;
        Common.currentTimeSlot = -1;
        Common.currentSalon = null;
        Common.currentBarber = null;
        Common.bookingDate.add(Calendar.DATE, 0);
    }

    public static BookingStepFourFragment getInstance() {
        if (instance == null)
            instance = new BookingStepFourFragment();
        return instance;
    }

    BroadcastReceiver confirmBookingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setData();
        }
    };

    private void setData() {
        txtBarberName.setText(Common.currentBarber.getName());
        txtBookingTime.setText(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                .append(" at ")
                .append(simpleDateFormat.format(Common.bookingDate.getTime())));
        txtSalonAddress.setText(Common.currentSalon.getAddress());
        txtSalonWebsite.setText(Common.currentSalon.getWebsite());
        txtSalonName.setText(Common.currentSalon.getName());
        txtSalonOpenHours.setText(Common.currentSalon.getOpenHours());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // apply format for date display on Confirm
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(confirmBookingReceiver, new IntentFilter(Common.KEY_CONFIRM_BOOKING));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(confirmBookingReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View itemView = inflater.inflate(R.layout.fragment_booking_step_four, container, false);
        unbinder = ButterKnife.bind(this, itemView);
        return itemView;
    }
}
