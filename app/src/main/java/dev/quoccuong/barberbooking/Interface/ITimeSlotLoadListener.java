package dev.quoccuong.barberbooking.Interface;

import java.util.List;

import dev.quoccuong.barberbooking.Model.TimeSlot;

public interface ITimeSlotLoadListener {
    void onTimeSlotLoadSuccess(List<TimeSlot> timeSlots);
    void onTimeSlotLoadFailed(String message);
    void onTimeSlotEmpty();
}
