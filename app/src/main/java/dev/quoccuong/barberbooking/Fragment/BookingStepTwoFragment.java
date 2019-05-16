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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dev.quoccuong.barberbooking.Adapter.MyBarberAdapter;
import dev.quoccuong.barberbooking.Common.Common;
import dev.quoccuong.barberbooking.Common.SpacesItemDecoration;
import dev.quoccuong.barberbooking.Model.Barber;

public class BookingStepTwoFragment extends Fragment {

    static BookingStepTwoFragment instance;

    Unbinder unbinder;
    LocalBroadcastManager localBroadcastManager;

    @BindView(R.id.recycler_barber)
    RecyclerView recyclerViewBarber;

    private BroadcastReceiver barberDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Barber> barbers = intent.getParcelableArrayListExtra(Common.KEY_BARBER_LOAD_DONE);
            MyBarberAdapter adapter = new MyBarberAdapter(getContext(), barbers);
            recyclerViewBarber.setAdapter(adapter);
        }
    };

    public static BookingStepTwoFragment getInstance() {
        if (instance == null)
            instance = new BookingStepTwoFragment();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(barberDoneReceiver, new IntentFilter(Common.KEY_BARBER_LOAD_DONE));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(barberDoneReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View itemView = inflater.inflate(R.layout.fragment_booking_step_two, container, false);
        unbinder = ButterKnife.bind(this, itemView);

        initView();
        return itemView;
    }

    private void initView() {
        recyclerViewBarber.setHasFixedSize(true);
        recyclerViewBarber.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerViewBarber.addItemDecoration(new SpacesItemDecoration(10));
    }
}
