package dev.quoccuong.barberbooking.Adapter;

import android.content.Context;
import android.quoccuong.barberbooking.R;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import dev.quoccuong.barberbooking.Model.Salon;

public class MySalonAdapter extends RecyclerView.Adapter<MySalonAdapter.MyViewHolder> {

    Context context;
    List<Salon> salons;

    public MySalonAdapter(Context context, List<Salon> salons) {
        this.context = context;
        this.salons = salons;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_salons, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.txtSalonName.setText(salons.get(i).getName());
        myViewHolder.txtSalonAddress.setText(salons.get(i).getAddress());
    }

    @Override
    public int getItemCount() {
        return salons.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView txtSalonName, txtSalonAddress;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtSalonName = itemView.findViewById(R.id.txt_salon_name);
            txtSalonAddress = itemView.findViewById(R.id.txt_salon_address);
        }
    }
}
