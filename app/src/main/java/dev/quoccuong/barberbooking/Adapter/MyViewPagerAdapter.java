package dev.quoccuong.barberbooking.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import dev.quoccuong.barberbooking.Common.Common;
import dev.quoccuong.barberbooking.Fragment.BookingStepFourFragment;
import dev.quoccuong.barberbooking.Fragment.BookingStepOneFragment;
import dev.quoccuong.barberbooking.Fragment.BookingStepThreeFragment;
import dev.quoccuong.barberbooking.Fragment.BookingStepTwoFragment;

public class MyViewPagerAdapter extends FragmentPagerAdapter {

    final int STEP_ONE = 0;
    final int STEP_TWO = 1;
    final int STEP_THREE = 2;
    final int STEP_FOUR = 3;

    public MyViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int page) {
        switch (page) {
            case STEP_ONE:
                return BookingStepOneFragment.getInstance();
            case STEP_TWO:
                return BookingStepTwoFragment.getInstance();
            case STEP_THREE:
                return BookingStepThreeFragment.getInstance();
            case STEP_FOUR:
                return BookingStepFourFragment.getInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return Common.totalBookingSteps;
    }
}
