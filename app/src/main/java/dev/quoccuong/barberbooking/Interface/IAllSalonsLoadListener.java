package dev.quoccuong.barberbooking.Interface;

import java.util.List;

public interface IAllSalonsLoadListener {
    void onAllSalonsLoadSuccess(List<String> areaNames);
    void onAllSalonsFailed(String message);
}
