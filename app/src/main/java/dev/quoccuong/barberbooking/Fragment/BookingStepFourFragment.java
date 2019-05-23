package dev.quoccuong.barberbooking.Fragment;

import android.app.AlertDialog;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dev.quoccuong.barberbooking.Common.Common;
import dev.quoccuong.barberbooking.Model.BookingInformation;
import dmax.dialog.SpotsDialog;

public class BookingStepFourFragment extends Fragment {

    static BookingStepFourFragment instance;

    SimpleDateFormat simpleDateFormat;
    LocalBroadcastManager localBroadcastManager;

    Unbinder unbinder;
    AlertDialog dialog;

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
        dialog.show();

        // process Timestamp
        // we will use Timestamp to filter all booking with date is greater today
        // for only display all future booking
        String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("-"); // split ex: 9:00-9:30
        // get start time: get 9:00
        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim()); // get '9'
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim()); // get '00'

        Calendar bookingDateWithOurHouse = Calendar.getInstance();
        bookingDateWithOurHouse.setTimeInMillis(Common.bookingDate.getTimeInMillis());
        bookingDateWithOurHouse.set(Calendar.HOUR_OF_DAY, startHourInt);
        bookingDateWithOurHouse.set(Calendar.MINUTE, startMinInt);

        // create Timestamp and apply to BookingInformation
        Timestamp timestamp = new Timestamp(bookingDateWithOurHouse.getTime());

        final BookingInformation bookingInformation = new BookingInformation();
        bookingInformation.setTimestamp(timestamp);
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
                .append(simpleDateFormat.format(bookingDateWithOurHouse.getTime())).toString());
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
                        // here we can write an function to check
                        // if already exist an booking, we will prevent new booking
                        addToUserBooking(bookingInformation);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToUserBooking(final BookingInformation bookingInformation) {


        // first, create new booking for user
        final CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.currentUser.getPhoneNumber())
                .collection("Booking");

        // check if exist document in this collection
        userBooking.whereEqualTo("done", false) // if have any document with field done = false
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().isEmpty()) {
                            // set data
                            userBooking.document()
                                    .set(bookingInformation)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            dismissDialog();
                                            resetDataAndFinish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dismissDialog();
                                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            dismissDialog();
                            resetDataAndFinish();
                        }
                    }
                });
    }

    private void resetDataAndFinish() {
        resetStaticData();
        getActivity().finish();
        Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
    }

    private void dismissDialog() {
        if (dialog.isShowing())
            dialog.dismiss();
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

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
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
