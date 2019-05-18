package dev.quoccuong.barberbooking.Adapter;

import android.content.Context;
import android.quoccuong.barberbooking.R;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dev.quoccuong.barberbooking.Common.Common;
import dev.quoccuong.barberbooking.Model.TimeSlot;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    Context context;
    List<TimeSlot> timeSlots;

    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        timeSlots = new ArrayList<>();
    }

    public MyTimeSlotAdapter(Context context, List<TimeSlot> timeSlots) {
        this.context = context;
        this.timeSlots = timeSlots;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_time_slot, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
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
                    myViewHolder.cardTimeSlot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                    myViewHolder.txtDescription.setText("Full");
                    myViewHolder.txtDescription.setTextColor(context.getResources().getColor(android.R.color.white));
                    myViewHolder.txtTimeSlot.setTextColor(context.getResources().getColor(android.R.color.white));
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtTimeSlot, txtDescription;
        CardView cardTimeSlot;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTimeSlot = itemView.findViewById(R.id.txt_time_slot);
            txtDescription = itemView.findViewById(R.id.txt_time_slot_description);
            cardTimeSlot = itemView.findViewById(R.id.card_time_slot);
        }
    }
}
