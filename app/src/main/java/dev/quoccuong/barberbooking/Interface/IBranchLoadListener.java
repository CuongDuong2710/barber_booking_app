package dev.quoccuong.barberbooking.Interface;

import java.util.List;

import dev.quoccuong.barberbooking.Model.Salon;

public interface IBranchLoadListener {
    void onBranchLoadSuccess(List<Salon> salons);
    void onBranchLoadFailed(String message);
}
