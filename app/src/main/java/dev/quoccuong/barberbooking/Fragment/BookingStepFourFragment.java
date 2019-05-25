package dev.quoccuong.barberbooking.Fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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

    // "9:00 - 9:30"
    int startHour; // 9
    int startMin; // 00
    int endHour; // 9
    int endMin; // 30

    Uri calendarsUri = Uri.parse("content://com.android.calendar/calendars");

    @OnClick(R.id.btn_confirm)
    void confirmBooking() {
        dialog.show();

        getStartEndOfHourAndMinute();

        Calendar bookingDateWithOurHouse = Calendar.getInstance();
        bookingDateWithOurHouse.setTimeInMillis(Common.bookingDate.getTimeInMillis());
        bookingDateWithOurHouse.set(Calendar.HOUR_OF_DAY, startHour);
        bookingDateWithOurHouse.set(Calendar.MINUTE, startMin);

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

    private void getStartEndOfHourAndMinute() {
        // process Timestamp
        // we will use Timestamp to filter all booking with date is greater today
        // for only display all future booking
        String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("-"); // split ex: 9:00-9:30

        String[] startTimeConvert = convertTime[0].split(":"); // 9:00
        startHour = Integer.parseInt(startTimeConvert[0].trim()); // get '9'
        startMin = Integer.parseInt(startTimeConvert[1].trim()); // get '00'

        String[] endTimeConvert = convertTime[1].split(":"); // 9:30
        endHour = Integer.parseInt(endTimeConvert[0].trim()); // 9
        endMin = Integer.parseInt(endTimeConvert[1].trim()); // 30
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
                                            addToCalenderOfDevice(Common.bookingDate, Common.convertTimeSlotToString(Common.currentTimeSlot));
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

    private void addToCalenderOfDevice(Calendar bookingDate, String startTime) {
        Calendar startEvent = Calendar.getInstance();
        startEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        startEvent.set(Calendar.HOUR_OF_DAY, startHour);
        startEvent.set(Calendar.MINUTE, startMin);

        Calendar endEvent = Calendar.getInstance();
        endEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        endEvent.set(Calendar.HOUR_OF_DAY, endHour);
        endEvent.set(Calendar.MINUTE, endMin);

        // convert Calendar to String
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String startEventTime = sdf.format(startEvent.getTime());
        String endEventTime = sdf.format(endEvent.getTime());

        addToDevice(startEventTime, endEventTime, "Haircut Booking",
                new StringBuilder("Hair cut from")
                        .append(startTime)
                        .append(" with ")
                        .append(Common.currentBarber.getName())
                        .append(" at ")
                        .append(Common.currentSalon.getName()).toString(),
                new StringBuilder("Address: ").append(Common.currentSalon.getAddress()).toString());
    }

    private void addToDevice(String startEventTime, String endEventTime, String title, String description, String address) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        try {
            Date start = sdf.parse(startEventTime);
            Date end = sdf.parse(endEventTime);

            ContentValues event = new ContentValues();

            // put
            event.put(CalendarContract.Events.CALENDAR_ID, getCalendar(getContext()));
            event.put(CalendarContract.Events.TITLE, title);
            event.put(CalendarContract.Events.DESCRIPTION, description);
            event.put(CalendarContract.Events.EVENT_LOCATION, address);

            // time
            event.put(CalendarContract.Events.DTSTART, start.getTime());
            event.put(CalendarContract.Events.DTEND, end.getTime());
            event.put(CalendarContract.Events.ALL_DAY, 0);
            event.put(CalendarContract.Events.HAS_ALARM, 1);

            String timeZone = TimeZone.getDefault().getID();
            event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);

            getActivity().getContentResolver().insert(calendarsUri, event);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String getCalendar(Context context) {
        // get default calendar ID of Calendar Gmail
        String gmailIdCalendar = "";
        String protection[] = {"_id", "calendar_displayName"};

        ContentResolver contentResolver = context.getContentResolver();
        // select all calendar
        Cursor cursor = contentResolver.query(calendarsUri, protection, null, null, null);
        if (cursor.moveToFirst()) {
            String calName;
            int nameCol = cursor.getColumnIndex(protection[1]);
            int idCol = cursor.getColumnIndex(protection[0]);
            do {
                calName = cursor.getString(nameCol);
                if (calName.contains("@gmail.com")) {
                    gmailIdCalendar = cursor.getString(idCol);
                    break;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return  gmailIdCalendar;
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
