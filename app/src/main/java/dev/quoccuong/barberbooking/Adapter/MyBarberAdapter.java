package dev.quoccuong.barberbooking.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.quoccuong.barberbooking.R;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dev.quoccuong.barberbooking.Common.Common;
import dev.quoccuong.barberbooking.Interface.IRecyclerItemSelectedListener;
import dev.quoccuong.barberbooking.Model.Barber;

public class MyBarberAdapter extends RecyclerView.Adapter<MyBarberAdapter.MyViewHolder> {

    Context context;
    List<Barber> barbers;
    List<CardView> cardViews;
    LocalBroadcastManager localBroadcastManager;

    public MyBarberAdapter(Context context, List<Barber> barbers) {
        this.context = context;
        this.barbers = barbers;
        cardViews = new ArrayList<>();
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_barbers, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {
        myViewHolder.txtBarberName.setText(barbers.get(i).getName());
        myViewHolder.ratingBar.setRating((float) barbers.get(i).getRating());
        if (!cardViews.contains(myViewHolder.cardBarber))
            cardViews.add(myViewHolder.cardBarber);

        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int pos) {
                // set background for all items not choice
                for (CardView cardView : cardViews) {
                    cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
                }
                myViewHolder.cardBarber.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));

                // send local broadcast to enable Next button
                Intent intent = new Intent(Common.KEY_ENABLE_NEXT_BUTTON);
                intent.putExtra(Common.KEY_BARBER_SELECTED, barbers.get(pos));
                intent.putExtra(Common.KEY_STEP, 2);
                localBroadcastManager.sendBroadcast(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return barbers.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtBarberName;
        RatingBar ratingBar;
        CardView cardBarber;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBarberName = itemView.findViewById(R.id.txt_barber_name);
            ratingBar = itemView.findViewById(R.id.rating_bar_barber);
            cardBarber = itemView.findViewById(R.id.card_barber);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelectedListener(v, getAdapterPosition());
        }
    }
}
