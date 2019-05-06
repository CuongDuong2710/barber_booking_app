package dev.quoccuong.barberbooking.Interface;

import java.util.List;

import dev.quoccuong.barberbooking.Model.LookBook;

public interface ILookBookLoadListener {
    void onLookBookLoadSuccess(List<LookBook> lookBooks);
    void onLookBookLoadFailed(String message);
}
