package dev.quoccuong.barberbooking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.quoccuong.barberbooking.R;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.shuhart.stepview.StepView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dev.quoccuong.barberbooking.Adapter.MyViewPagerAdapter;
import dev.quoccuong.barberbooking.Common.Common;

public class BookingActivity extends AppCompatActivity {

    @BindView(R.id.step_view)
    StepView stepView;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.btn_previous_step)
    Button btnPreviousStep;
    @BindView(R.id.btn_next_step)
    Button btnNextStep;

    @OnClick(R.id.btn_next_step)
    void nextClick() {
        Toast.makeText(this, "" + Common.currentSalon.getSalonId(), Toast.LENGTH_SHORT).show();
    }

    LocalBroadcastManager localBroadcastManager;

    // Broadcast Receiver
    private BroadcastReceiver buttonNextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Common.currentSalon = intent.getParcelableExtra(Common.KEY_SALON_STORE);
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

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(buttonNextReceiver, new IntentFilter(Common.KEY_ENABLE_NEXT_BUTTON));

        setupSetView();
        setColorNextButton();
        setColorPreviousButton();

        // view pager
        viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int page) {
                if (page == 0)
                    btnPreviousStep.setEnabled(false);
                else
                    btnPreviousStep.setEnabled(true);

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
