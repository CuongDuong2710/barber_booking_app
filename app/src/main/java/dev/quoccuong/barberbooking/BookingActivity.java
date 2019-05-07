package dev.quoccuong.barberbooking;

import android.quoccuong.barberbooking.R;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.shuhart.stepview.StepView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookingActivity extends AppCompatActivity {

    @BindView(R.id.step_view)
    StepView stepView;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.btn_previous_step)
    Button btnPreviousStep;
    @BindView(R.id.btn_next_step)
    Button btnNextStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        ButterKnife.bind(BookingActivity.this);

        setupSetView();
        setColorNextButton();
        setColorPreviousButton();

        // view pager
//        viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager()));
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
