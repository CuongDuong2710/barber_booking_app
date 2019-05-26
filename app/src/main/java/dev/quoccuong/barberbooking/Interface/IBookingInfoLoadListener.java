package dev.quoccuong.barberbooking.Interface;

import dev.quoccuong.barberbooking.Model.BookingInformation;

public interface IBookingInfoLoadListener {
    void onBookingInfoLoadEmpty();
    void onBookingInfoLoadSuccess(BookingInformation bookingInformation);
    void onBookingInfoLoadFailed(String message);
}
