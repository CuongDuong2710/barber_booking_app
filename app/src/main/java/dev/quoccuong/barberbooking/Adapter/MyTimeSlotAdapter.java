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
import dev.quoccuong.barberbooking.Model.TimeSlot;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    Context context;
    List<TimeSlot> timeSlots;
    List<CardView> cardViews;
    LocalBroadcastManager localBroadcastManager;

    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        timeSlots = new ArrayList<>();
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViews = new ArrayList<>();
    }

    public MyTimeSlotAdapter(Context context, List<TimeSlot> timeSlots) {
        this.context = context;
        this.timeSlots = timeSlots;
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViews = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_time_slot, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int i) {
        myViewHolder.txtTimeSlot.setText(new StringBuilder(Common.convertTimeSlotToString(i)).toString());
        if (timeSlots.size() == 0) { // if all position is available, show list
            myViewHolder.cardTimeSlot.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
            myViewHolder.txtDescription.setText("Available");
            myViewHolder.txtDescription.setTextColor(context.getResources().getColor(android.R.color.black));
            myViewHolder.txtTimeSlot.setTextColor(context.getResources().getColor(android.R.color.black));
        } else { // if have position is full (booked)
            for (TimeSlot timeSlot : timeSlots) {
                // loop all time slot from server and set different color
                int slot = Integer.parseInt(timeSlot.getSlot().toString());
                if (slot == i) { // if slot == position
                    // we will set tag for all time slot is full
                    // Base on tag, we can set all remain card background without change full time slot
                    myViewHolder.cardTimeSlot.setTag(Common.DISABLE_SELECTED);
                    myViewHolder.cardTimeSlot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                    myViewHolder.txtDescription.setText("Full");
                    myViewHolder.txtDescription.setTextColor(context.getResources().getColor(android.R.color.white));
                    myViewHolder.txtTimeSlot.setTextColor(context.getResources().getColor(android.R.color.white));
                }
            }
        }

        // add all card to list (20 cards)
        if (!cardViews.contains(myViewHolder.cardTimeSlot)) {
            cardViews.add(myViewHolder.cardTimeSlot);
        }

        // check if card time slot is available, user can selected it.
        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int pos) {
                for (CardView cardView : cardViews) {
                    if (cardView.getTag() == null) { // only available time slot card is white background
                        cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
                    }
                }
                // selected card will be change background color
                myViewHolder.cardTimeSlot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));

                // send broadcast to enable NEXT button
                Intent intent = new Intent(Common.KEY_ENABLE_NEXT_BUTTON);
                intent.putExtra(Common.KEY_TIME_SLOT, i); // put index of time slot user selected
                intent.putExtra(Common.KEY_STEP, 3); // go to step 3
                localBroadcastManager.sendBroadcast(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtTimeSlot, txtDescription;
        CardView cardTimeSlot;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTimeSlot = itemView.findViewById(R.id.txt_time_slot);
            txtDescription = itemView.findViewById(R.id.txt_time_slot_description);
            cardTimeSlot = itemView.findViewById(R.id.card_time_slot);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view, getAdapterPosition());
        }
    }
}
