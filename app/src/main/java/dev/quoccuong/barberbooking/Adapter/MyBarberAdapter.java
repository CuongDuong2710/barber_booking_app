package dev.quoccuong.barberbooking.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.quoccuong.barberbooking.R;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

import dev.quoccuong.barberbooking.Model.Barber;

public class MyBarberAdapter extends RecyclerView.Adapter<MyBarberAdapter.MyViewHolder> {

    Context context;
    List<Barber> barbers;

    public MyBarberAdapter(Context context, List<Barber> barbers) {
        this.context = context;
        this.barbers = barbers;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_barbers, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.txtBarberName.setText(barbers.get(i).getName());
        myViewHolder.ratingBar.setRating((float) barbers.get(i).getRating());
    }

    @Override
    public int getItemCount() {
        return barbers.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView txtBarberName;
        RatingBar ratingBar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBarberName = itemView.findViewById(R.id.txt_barber_name);
            ratingBar = itemView.findViewById(R.id.rating_bar_barber);
        }
    }
}
