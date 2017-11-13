package com.example.gpsmaptest;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;

/**
 * GPS 디텍팅하는 코드 (최신 방법론 이전 방법)
 **/
public class GpsDetectService extends Service implements LocationListener {
    public GpsDetectService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 코드는 여기서부터 진행

        initLocation();
        return super.onStartCommand(intent, flags, startId);
    }

    LocationManager locationManager;
    final long MIN_UPDATE_TIME = 1000 * 60 * 1; // 1분 단위 갱신
    final float MIN_UPDATE_DISTANCE = 10.0f;// 10m 단위 갱신

    public void initLocation() {

        // 1. 위치 매니저 획득
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 2. 위치 정보를 제공하는 공급자 (3개) 가용 여부 획득
        boolean isGpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);        // 센서
        boolean isNetOn = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);    // WIFI
        boolean isPassOn = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);    // 기지국

        // 3. 각 공급자 별로 처리

        if (!isGpsOn && !isNetOn && !isPassOn) {
            sendGpsBus(null);
            return;
        }
        if (isGpsOn) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER
                    , MIN_UPDATE_TIME
                    , MIN_UPDATE_DISTANCE
                    , this
            );
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                sendGpsBus(location);

            }
        }
        if (isNetOn) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER
                    , MIN_UPDATE_TIME
                    , MIN_UPDATE_DISTANCE
                    , this
            );
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null) {
                sendGpsBus(location);

            }

        }
        if (isPassOn) {
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER
                    , MIN_UPDATE_TIME
                    , MIN_UPDATE_DISTANCE
                    , this
            );
            Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (location != null) {
                sendGpsBus(location);

            }
        }
        // 4. 어느 쪽이던 데이터가 오면 BUS로 전송
    }

    public void freeLocation() {
        // 각종 설정된 리스너등 이벤트 헤제 및 디텍팅 중단
        locationManager.removeUpdates(this);
    }

    //==================================================================================================
    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
//==================================================================================================

    //sendGps를 호출할때는 location 객체를 보내라
    public void sendGpsBus(Location location) {
        // GPS 값을 Bus로 전송한다.
        U.getInstance().getGpsBus().post(location);
    }



    @Override
    public void onDestroy() {
        freeLocation(); // 서비스 중단되는 경우 이벤트 해제
        super.onDestroy();
    }
}
