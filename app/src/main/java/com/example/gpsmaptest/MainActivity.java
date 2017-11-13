package com.example.gpsmaptest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private GoogleMap mMap;
    int flagGpsStatus;  // 0: 엑티비티 구동되는 경우
    // 1: GPS 설정을 타고 돌아오는 경우
    EditText addr;
    boolean isFirstGpsLoad; // gps가 최초로 로딩되어 좌표값을 획득했는가?
    RecyclerView recyclerView;
    ArrayList<CoffeeStoreModel> coffees;
    CoffeeAdapter coffeeAdapter;
    Spinner cateSpinner;
    EditText dist;
    String selectShop;

    LatLng myLoc;
    // 1. 데이터를 부분으로 계속 가져오면 통신의 결과를 담아야한다.
    // 2. 한번에 통으로 가져오면 참조값만 가지면 된다.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addr = (EditText) findViewById(R.id.addr);


        /** 버스 등록 **/
        U.getInstance().getGpsBus().register(this);
        initUI();
        /** GPS Detected ===========================================================================*/

        /**지도를 소유하고 있는 Fragment 획득*/
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        /**지도 비동기화 진행*/
        mapFragment.getMapAsync(this); // ㅡ onMapReady

        // 1. Network checking
        // 2. GPS On checking
        checkGps();
        // 3. OS version 6.0 checking => 동의 여부 확인
        // 4. GPS detecting start ( 구버전, 신버전 )
        // 5. geocoder ( gps -> address 변환 )
        //     + OTTO Bus 이용하여 비동기적 상황의 이벤트를 전달하는 루틴으로 사용


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (flagGpsStatus == 1) {
            flagGpsStatus = 2; // 아무 값도 정해지지 않은 값으로 초기화
            checkGpsUseOn();
        }

        /** GPS 설정을 처리하고 돌아 왔을 때 다음 단계로 갈 수 있도록 설정해야 한다.**/
    }

    public void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        coffeeAdapter = new CoffeeAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(coffeeAdapter);

        cateSpinner = (Spinner) findViewById(R.id.cateSpinner);
        dist = (EditText) findViewById(R.id.dist);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this,
                R.array.cate,
                android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, getResources().getTextArray(R.array.cate));

        cateSpinner.setAdapter(arrayAdapter);
        cateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 커피전문점을 선택하면 호출 => 어떤 샵을 선택했는지 획득 저장 필요
                selectShop = cateSpinner.getItemAtPosition(i).toString();
                U.getInstance().log("[0]" + selectShop + "을 선택하였다.");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
//        cateSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                // 커피전문점을 선택하면 호출 => 어떤 샵을 선택했는지 획득 저장 필요
//
//                String selectShop = cateSpinner.getItemAtPosition(i).toString();
//                U.getInstance().log("[1]" + selectShop + "을 선택하였다.");
//
//            }
//        });

    }

    public void checkGps() {
        // 단말기에서 gps 사용을 on 했는지 체크한다
        // 정확도를 높이고, gps 값을 획득하기 위한 조치
        String gps = android.provider.Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        U.getInstance().log("[gps]=" + gps);
        if (!(gps.matches(".*gps*.") || gps.matches(".*network*."))) {
            /** GPS 사용 막았다 사용 설정 on 시켜라 **/
            U.getInstance().showPopup3(this, "알림", "GPS를 사용할 수 없습니다. 설정 화면으로 이동하시겠습니까?",
                    "예", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();

                            if (flagGpsStatus == 0) {
                                flagGpsStatus = 1;
                            }

                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    },
                    "아니오", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            checkGpsUseOn();

                        }
                    });

        } else {
            /** GPS 켜져있다 = > 3단계로 이동 **/
            checkGpsUseOn();


        }
    }
    // 3. OS version 6.0 checking => 동의 여부 확인

    public void checkGpsUseOn() {
        if (Build.VERSION.SDK_INT >= M) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    gpsDetect(1);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
                }
            } else {

                gpsDetect(2);
            }

        } else {

            gpsDetect(3);
            // 6.0 이하
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            // 동의했는지, 거절했는지 판별
            if (grantResults.length > 0) {
                if (grantResults[0] < 0) {
                    // 거부
                    gpsDetect(4);

                } else {
                    // 동의
                    gpsDetect(5);

                }
            }

            for (int i = 0; i < permissions.length; i++) {
                U.getInstance().log(permissions[i]);

            }

            for (String s : permissions) {
                U.getInstance().log(s);

            }

        }

    }


    // 코드별 처리
    public void gpsDetect(int code) {
        if (code == 4) {
            // 거부했음 -> 종료
            U.getInstance().showPopup3(this
                    , "알림"
                    , "안타깝습니다."
                    , "확인",
                    new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            finish();
                        }
                    }
                    , null
                    , null);
        } else if (code == 5 || code == 2) { //동의한 것 => 6.0이상 사용자 의미
            startService();
        } else if (code == 3) {
            // 6.0 이하 단말기이다. 그냥 PASS
        }
    }

    // GPS를 획득하는 서비스 가동
    public void startService() {
        Intent intent = new Intent(this, GpsDetectService.class);
        startService(intent);

    }

    /**
     * 서비스로부터 gps값을 버스를 통해 받는다
     **/
    @Subscribe
    public void receive(Location location) {

        // TODO: React to the event somehow!
        if (location != null) {
            addr.setText(location.getLatitude() + ", " + location.getLongitude());
            getAddress(location);

            if (!isFirstGpsLoad) //false를 부정하면 true
            {
                myLocationShow(location); //최초만 세팅, 다음부터는 들고만 있는다.
                isFirstGpsLoad = true;
                startAllCoffeeStroe();

            }

        }

    }

    // Lat, Lng => address 획득
    public void getAddress(Location location) {
        if (location == null) return;
        // 기본 재료 준비

        Geocoder geocoder = new Geocoder(this, Locale.KOREA);
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        // 변환
        try {
            List<Address> addressList = geocoder.getFromLocation(lat, lng, 2);
            if (addressList != null && addressList.size() > 0) {
                for (Address address : addressList) {
                    U.getInstance().log(address.toString());
                    U.getInstance().log(address.getThoroughfare());

                }
                // 컬렉션이니까 배열로 빼는것이아닌 get(position)으로 뺀다.
                addr.setText(U.getInstance().getTransferAddr(addressList.get(0)));
            } else {
                U.getInstance().log("주소 변환한 결과가 없다.");
                addr.setText("주소 변환한 결과가 없다");

            }
        } catch (IOException e) {
            e.printStackTrace();
            U.getInstance().log("주소 변환한 결과 실패");
            addr.setText("주소 변환한 결과 실패");


        }


    }

    /**
     * 지도가 준비되면 호출된다.
     **/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        /**지도 자체를 가르키는 객체*/
        mMap = googleMap;

        //지도에 마커 이벤트
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final CoffeeStoreModel csm = (CoffeeStoreModel) marker.getTag();
                U.getInstance().log(csm.toString());
                // 리스트의 센터를 선택한 내용으로 choice
//                recyclerView.scrollToPosition(csm.getIndex());
                recyclerView.smoothScrollToPosition(csm.getIndex());
                // 추후 포커싱 부분( 선택 부분이 도도라지게 처리 필요 )

                // 하단 부분에서 상위로 올라온다.
                Snackbar.make(recyclerView, csm.getNM() + ":" + csm.getADDRESS(), Snackbar.LENGTH_LONG)
                        .setAction("상세보기", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(MainActivity.this, SubDetailActivity.class);
                                intent.putExtra("csm", csm);
                                startActivity(intent);
                            }
                        }).show();
                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                myLoc = latLng;
                aniMoveCamera(latLng);

            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //U.getInstance().showPopup(MainActivity.this, "추가", "일정을 입력하세요. ", SweetAlertDialog.NORMAL_TYPE);

                myLoc = latLng;
                aniMoveCamera(latLng);
                onSearch2(null);
            }
        });

//        /** 동적 퍼미션을 요구하는 경우 (6.0 이하로 컴파일 하거나, 동적 퍼미션을 구현하거나) 택일**/
//        mMap.setMyLocationEnabled(true);
//
//        /**특정 위치를 마킹하여서 화면의 센터로 설정한다.**/
//        // Add a marker in Sydney and move the camera
//        //LatLng sydney = new LatLng(-34, 151);
//        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void aniMoveCamera(LatLng myLoc) {
        CameraPosition ani = new CameraPosition
                .Builder()
                .target(myLoc)
                .zoom(16)
                .bearing(60)
                .tilt(30)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(ani));
    }

    /**
     * 현재 위치를 지도상에 표시한다.
     **/
    public void myLocationShow(Location location) {
        mMap.clear(); // 일단 마킹은 하나만
        myLoc = new LatLng(location.getLatitude(), location.getLongitude());

//        mMap.addMarker(new MarkerOptions().position(myLoc).title("Marker in 선오위치"));


        // 지도 줌 처리
        CameraPosition ani = new CameraPosition
                .Builder()
                .target(myLoc)
                .zoom(16).bearing(60).tilt(30).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(ani));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 커피 전문점 정보를 다 가져온다.
     **/
    public void startAllCoffeeStroe() {
        Call<ResCoffeeStoresModel> res = Net.getInstance().getApiIm().coffeeAll();
        res.enqueue(new Callback<ResCoffeeStoresModel>() {
            @Override
            public void onResponse(Call<ResCoffeeStoresModel> call, Response<ResCoffeeStoresModel> response) {
                if (response != null && response.isSuccessful()) {
                    U.getInstance().log(response.body().getCode() + ":" + response.body().getBody().size());
                    coffees = response.body().getBody();
//                    coffeeAdapter.notifyDataSetChanged();  //가급적 전체 갱신은 배제, 부분 처리 요망

                    // 마커 추가
                    makeMarker();

                } else {

                }
            }

            @Override
            public void onFailure(Call<ResCoffeeStoresModel> call, Throwable t) {
                U.getInstance().log("ㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁ실패ㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁ" + t.getLocalizedMessage());

            }
        });


    }

    // Viewholder
    public class CoffeeViewHolder extends RecyclerView.ViewHolder {

        ImageView brandImage;
        TextView NM;
        TextView ADDRESS;

        public CoffeeViewHolder(View itemView) {
            super(itemView);
            brandImage = (ImageView) itemView.findViewById(R.id.brandImage);
            NM = (TextView) itemView.findViewById(R.id.NM);
            ADDRESS = (TextView) itemView.findViewById(R.id.ADDRESS);
        }

        //데이터 모델 하나를 넘겨주는 toBind
        public void toBind(CoffeeStoreModel coffeeStoreModel) {

            if (coffeeStoreModel.getType().equals("COFFEEBEAN")) {
                Picasso.with(brandImage.getContext())
                        .load(R.drawable.coffeebean)
                        .error(R.mipmap.ic_launcher_round)
                        .into(brandImage);

            } else {
                Picasso.with(brandImage.getContext())
                        .load(R.drawable.starbuck)
                        .error(R.mipmap.ic_launcher_round)
                        .into(brandImage);
            }
            NM.setText(coffeeStoreModel.getNM());
            ADDRESS.setText(coffeeStoreModel.getADDRESS());

        }
    }


    class CoffeeAdapter extends RecyclerView.Adapter<CoffeeViewHolder> {
        @Override
        public CoffeeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view = getLayoutInflater().inflate(R.layout.cell_coffee_layout,null);
            //뷰 그룹이니 parent
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.cell_coffee_layout, parent, false);
//            CoffeeViewHolder coffeeViewHolder = new CoffeeViewHolder(view);

            return new CoffeeViewHolder(view);
        }

        //실제 데이터와 뷰를 세팅
        @Override
        public void onBindViewHolder(CoffeeViewHolder holder, int position) {

            holder.toBind(coffees.get(position));

        }

        @Override
        public int getItemCount() {
            //null이면 0 아니면 size
            return coffees == null ? 0 : coffees.size();
        }
    }

    /**
     * 위치값 변환후 addMarker
     **/
    public void makeMarker() {
        LatLng myLoc;
        GeoPoint point;
        int index = 0;
        for (CoffeeStoreModel csm : coffees) {
            //좌표 변환
            point = U.getInstance().transGeoToKatec(new GeoPoint(csm.getX_AXIS(), csm.getY_AXIS()));
            //GEO로 좌표 객체 생성
            myLoc = new LatLng(point.getY(), point.getX());
            // 마커의 아이콘을 업체별로 세팅하는 작업( 미완성 ), 이미지가 커서 추가 작업 필요

//            int imgId = 0;
//            if(csm.getType().equals("COFFEEBEAN")){
//                imgId = R.drawable.coffeebean;
//            }else{
//                imgId = R.drawable.starbuck;
//
//            }

            Marker marker = mMap.addMarker(new MarkerOptions().position(myLoc).title(csm.getNM()));

            csm.setIndex(index++); // 데이터 인덱스 세팅
            // 마커에다 데이터를 세팅팅
            marker.setTag(csm);

            //.icon(BitmapDescriptorFactory.fromResource(imgId))
        }
    }

    public void onDistanceSelect(View view) {
        // 팝업 => 검색 거리를 선택함
        AlertDialog.Builder ab = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setSingleChoiceItems(R.array.distance, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String[] array = getResources().getStringArray(R.array.distance);
                        Snackbar.make(recyclerView, array[i] + "를 선택하였습니다.", Snackbar.LENGTH_LONG)
                                .show();

                        // 써클 표시
                        int dis = Integer.parseInt(array[i].replace("km", ""));
                        mMap.addCircle(new CircleOptions().center(myLoc).radius(dis * 1000));
                        dist.setText(array[i]);
                        dialogInterface.dismiss();

                    }
                })
                .setTitle("거리 선택");
        AlertDialog alert = ab.create();
        alert.show();
    }

    public void onSearch2(View view) {


        // 커피숍 브랜드 + 거리 => 검색
        DistModel distModel = new DistModel(U.getInstance().changeBrand(selectShop)
                ,myLoc.latitude
                ,myLoc.longitude
                ,U.getInstance().getDouble(dist.getText().toString().replace("km","")));
        startBrandCoffeeStore(distModel);
    }

    public void startBrandCoffeeStore(final DistModel distModel) {
        Call<ResCoffeeStoresModel> res = Net.getInstance().getApiIm().coffeeDist(distModel);
        res.enqueue(new Callback<ResCoffeeStoresModel>() {
            @Override
            public void onResponse(Call<ResCoffeeStoresModel> call, Response<ResCoffeeStoresModel> response) {
                if (response != null && response.isSuccessful()) {
                    U.getInstance().log(response.body().getCode() + ":" + response.body().getBody().size());

                    coffees = response.body().getBody();

                    coffeeAdapter.notifyDataSetChanged();  //가급적 전체 갱신은 배제, 부분 처리 요망

                    mMap.clear();
                    // 마커 추가
                    makeMarker();

                    int dis = (int)distModel.getDist();
                    mMap.addCircle(new CircleOptions().center(myLoc)
                    .radius(dis*1000).fillColor(0x55ff0000).strokeColor(0x55ff0000));

                } else {

                }
            }

            @Override
            public void onFailure(Call<ResCoffeeStoresModel> call, Throwable t) {
                U.getInstance().log("ㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁ실패ㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁ" + t.getLocalizedMessage());

            }
        });


    }


}
