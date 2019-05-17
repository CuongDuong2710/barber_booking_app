package dev.quoccuong.barberbooking.Adapter;

import android.content.Context;
import android.content.Intent;
import android.quoccuong.barberbooking.R;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dev.quoccuong.barberbooking.Common.Common;
import dev.quoccuong.barberbooking.Interface.IRecyclerItemSelectedListener;
import dev.quoccuong.barberbooking.Model.Salon;

public class MySalonAdapter extends RecyclerView.Adapter<MySalonAdapter.MyViewHolder> {

    Context context;
    List<Salon> salons;
    List<CardView> cardViews;
    LocalBroadcastManager localBroadcastManager;

    public MySalonAdapter(Context context, List<Salon> salons) {
        this.context = context;
        this.salons = salons;
        cardViews = new ArrayList<>();
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_salons, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {
        myViewHolder.txtSalonName.setText(salons.get(i).getName());
        myViewHolder.txtSalonAddress.setText(salons.get(i).getAddress());

        if (!cardViews.contains(myViewHolder.cardSalon))
            cardViews.add(myViewHolder.cardSalon);

        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int pos) {
                // set white background for all cards not be selected
                for (CardView cardView : cardViews) {
                    cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
                }

                // set selected BG for only selected item
                myViewHolder.cardSalon.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));

                // send broadcast to tell BookingActivity enable 'Next' button
                Intent intent = new Intent(Common.KEY_ENABLE_NEXT_BUTTON);
                intent.putExtra(Common.KEY_SALON_STORE, salons.get(pos));
                intent.putExtra(Common.KEY_STEP, 1);
                localBroadcastManager.sendBroadcast(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return salons.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtSalonName, txtSalonAddress;
        CardView cardSalon;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtSalonName = itemView.findViewById(R.id.txt_salon_name);
            txtSalonAddress = itemView.findViewById(R.id.txt_salon_address);
            cardSalon = itemView.findViewById(R.id.card_salon);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view, getAdapterPosition());
        }
    }
}
