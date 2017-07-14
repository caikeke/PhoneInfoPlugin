package com.phone.yhck;

import android.graphics.drawable.Drawable;

public class MyAppInfo {
    private Drawable image;
    private String appName;
    private String packageName;
    public MyAppInfo(Drawable image, String appName,String packageName) {
        this.image = image;
        this.appName = appName;
        this.packageName =packageName;
    }
    public MyAppInfo() {

    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
