package dev.quoccuong.barberbooking;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.quoccuong.barberbooking.R;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shuhart.stepview.StepView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dev.quoccuong.barberbooking.Adapter.MyViewPagerAdapter;
import dev.quoccuong.barberbooking.Common.Common;
import dev.quoccuong.barberbooking.Common.NonSwipeViewPager;
import dev.quoccuong.barberbooking.Model.Barber;
import dmax.dialog.SpotsDialog;

public class BookingActivity extends AppCompatActivity {

    @BindView(R.id.step_view)
    StepView stepView;
    @BindView(R.id.view_pager)
    NonSwipeViewPager viewPager;
    @BindView(R.id.btn_previous_step)
    Button btnPreviousStep;
    @BindView(R.id.btn_next_step)
    Button btnNextStep;

    AlertDialog dialog;
    CollectionReference barberRef;


    @OnClick(R.id.btn_previous_step)
    void previousStep() {
        if (Common.step == 3 || Common.step > 0) {
            Common.step--;
            viewPager.setCurrentItem(Common.step);
        }
    }

    @OnClick(R.id.btn_next_step)
    void nextClick() {
        if (Common.step < 3 || Common.step == 0) {

            Common.step++;
            if (Common.step == 1) { // after choose salon
                if (Common.currentSalon != null)
                    loadBarberBySalon(Common.currentSalon.getSalonId());
            } else if (Common.step == 2) { // pick time slot
                if (Common.currentBarber != null)
                    loadTimeSlotOfBarber(Common.currentBarber.getBarberID());
            }
            viewPager.setCurrentItem(Common.step);
        }
    }

    private void loadTimeSlotOfBarber(String barberID) {
        // send Local broadcast to Fragment step 3
        Intent intent = new Intent(Common.KEY_DISPLAY_TIME_SLOT);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void loadBarberBySalon(String salonId) {
        dialog.show();

        // Now, select all barbers of salon
        // /AllSalon/NewYork/Branch/4sUEoGnpzMwY8ts5AbbQ/Barbers
        barberRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(salonId)
                .collection("Barbers");

        barberRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<Barber> barbers = new ArrayList<>();
                        for (QueryDocumentSnapshot barberSnapshot : task.getResult()) {
                            Barber barber = barberSnapshot.toObject(Barber.class);
                            barber.setPassword(""); // remove password because in client app
                            barber.setBarberID(barberSnapshot.getId());

                            barbers.add(barber);
                        }

                        // send broadcast to BookingStepTwoFragment to load Recycler
                        Intent intent = new Intent(Common.KEY_BARBER_LOAD_DONE);
                        intent.putParcelableArrayListExtra(Common.KEY_BARBER_LOAD_DONE, barbers);
                        localBroadcastManager.sendBroadcast(intent);

                        dialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
            }
        });


    }

    LocalBroadcastManager localBroadcastManager;

    // Broadcast Receiver
    private BroadcastReceiver buttonNextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int step = intent.getIntExtra(Common.KEY_STEP, 0);

            if (step == 1)
                Common.currentSalon = intent.getParcelableExtra(Common.KEY_SALON_STORE);
            else if (step == 2)
                Common.currentBarber = intent.getParcelableExtra(Common.KEY_BARBER_SELECTED);

            // set disable Next button
            btnNextStep.setEnabled(true);
            setColorNextButton();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(buttonNextReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        ButterKnife.bind(BookingActivity.this);

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(buttonNextReceiver, new IntentFilter(Common.KEY_ENABLE_NEXT_BUTTON));

        setupSetView();
        setColorNextButton();
        setColorPreviousButton();

        // view pager
        viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(4); // we have 4 fragment so we need keep state of this 4 screen page when press previous button
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int page) {
                // show step
                stepView.go(page, true);
                if (page == 0)
                    btnPreviousStep.setEnabled(false);
                else
                    btnPreviousStep.setEnabled(true);

                btnNextStep.setEnabled(false);

                setColorPreviousButton();
                setColorNextButton();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private void setColorPreviousButton() {
        if (btnPreviousStep.isEnabled())
            btnPreviousStep.setBackgroundResource(R.color.colorButton);
        else
            btnPreviousStep.setBackgroundResource(android.R.color.darker_gray);
    }

    private void setColorNextButton() {
        if (btnNextStep.isEnabled())
            btnNextStep.setBackgroundResource(R.color.colorButton);
        else
            btnNextStep.setBackgroundResource(android.R.color.darker_gray);
    }

    private void setupSetView() {
        List<String> stepList = new ArrayList<>();
        stepList.add("Salon");
        stepList.add("Barber");
        stepList.add("Time");
        stepList.add("Confirm");
        stepView.setSteps(stepList);
    }
}
