package dev.quoccuong.barberbooking.Interface;

import java.util.List;

import dev.quoccuong.barberbooking.Model.Banner;

/* load banner from FireBase Firestore */
public interface IBannerLoadListener {
    void onBannerLoadSuccess(List<Banner> banners);
    void onBannerLoadFailed(String message);
}
