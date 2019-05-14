package dev.quoccuong.barberbooking.Common;

import dev.quoccuong.barberbooking.Model.Salon;
import dev.quoccuong.barberbooking.Model.User;

public class Common {
    public static String KEY_ENABLE_NEXT_BUTTON = "KEY_ENABLE_NEXT_BUTTON";
    public static String KEY_SALON_STORE = "SALON_SAVE";
    public static String KEY_BARBER_LOAD_DONE = "BARBER_LOAD_DONE";
    public static String IS_LOGIN = "IsLogin";
    public static User currentUser;
    public static int totalBookingSteps = 4;
    public static Salon currentSalon;
    public static int step = 0; // init first step is 0
    public static String city = "";
}
