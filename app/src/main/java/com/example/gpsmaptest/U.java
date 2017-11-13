package com.example.gpsmaptest;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.util.Log;

import com.squareup.otto.Bus;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 유틸리티
 */

class U {
    // =============================================================================================
    private static final U ourInstance = new U();
    static U getInstance() {
        return ourInstance;
    }
    private U() {
    }
    // =============================================================================================
    // 팝업
    // =============================================================================================
    public void showPopup3(Context context, String title, String msg,
                           String cName, SweetAlertDialog.OnSweetClickListener cEvent,
                           String oName, SweetAlertDialog.OnSweetClickListener oEvent)
    {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(msg)
                .setConfirmText(cName)
                .setConfirmClickListener(cEvent)
                .setCancelText(oName)
                .setCancelClickListener(oEvent)
                .show();
    }
    public SweetAlertDialog showLoading(Context context)
    {
        return showLoading(context, "LOADING");
    }
    public SweetAlertDialog showLoading(Context context, String msg)
    {
        return showLoading(context, msg, "#A5DC86");
    }
    public SweetAlertDialog showLoading(Context context, String msg, String color)
    {
        SweetAlertDialog pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor(color));
        pDialog.setTitleText(msg);
        pDialog.setCancelable(false); // 백키를 눌러도 닫히지 않는다.
        pDialog.show();
        return pDialog;
    }
    public void showSimplePopup(Context context, String title, String msg, int type)
    {
        new SweetAlertDialog(context, type)
                .setTitleText(title)
                .setContentText(msg)
                .show();
    }

    public SweetAlertDialog showPopup(Context context, String title, String msg, int type)
    {
        SweetAlertDialog alert = new SweetAlertDialog(context, type);
        alert.setTitleText(title)
                .setContentText(msg)
                .setCancelable(false);
        alert.show();
        return alert;
//                .setTitleText(title)
//                .setContentText(msg)
//                .show();

    }

    final String TAG = "SEONOHTAG";
    boolean isTestMode = true;

    public void log(String m)
    {
        if(isTestMode) {
            Log.e(TAG, "=============================");
            Log.e(TAG, "" + m);
            Log.e(TAG, "=============================");

        }
    }

    //=============================================================================================
    //버스
    Bus gpsBus = new Bus();

    public Bus getGpsBus() {
        return gpsBus;
    }
    //=============================================================================================
    // Address => 시군구동 표시
    public String getTransferAddr(Address address)
    {
        if( address ==null) return  "";

        return String.format("%s %s %s",address.getAdminArea(),address.getThoroughfare(),address.getFeatureName());
    }

    //=============================================================================================
    // 좌표 변환 -> KATEC -> GEO

    public GeoPoint transGeoToKatec(GeoPoint point)
    {
        return GeoTrans.convert(GeoTrans.KATEC, GeoTrans.GEO, point);
    }

    //=============================================================================================
    // String -> double
    public double getDouble(String src)
    {

        try {
            return Double.parseDouble(src);
        } catch (NumberFormatException e) {
            return 0;
        }

    }

    public String changeBrand(String brand)
    {
        if(brand.equals("스타벅스"))
        {
            return "starbucks";
        }else if(brand.equals("커피빈")){
            return "coffeebean";
        }
        return "";


    }



}











