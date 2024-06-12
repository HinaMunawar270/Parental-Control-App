package com.mansourappdevelopment.androidapp.kidsafe.interfaces;

public interface OnAppClickListener {
    void onItemClick(String packageName, String appName, boolean blocked);

    void onLockAppSet(int hours, int minutes);

    void onLockCanceled();

    /*void onLockCanceled();

    void onLockAppSet(int hours, int minutes);*/
}
