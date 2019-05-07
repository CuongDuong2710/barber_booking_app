package dev.quoccuong.barberbooking.Fragment;

import android.os.Bundle;
import android.quoccuong.barberbooking.R;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BookingStepOneFragment extends Fragment {

    static BookingStepOneFragment instance;

    public static BookingStepOneFragment getInstance() {
        if (instance == null)
            instance = new BookingStepOneFragment();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_booking_step_one, container, false);
    }
}
