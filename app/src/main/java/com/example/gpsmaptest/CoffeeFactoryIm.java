package com.example.gpsmaptest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 커피 전문점 정보를 요청하는 API 통신 메소드 정의
 */

public interface CoffeeFactoryIm
{
    //커피전문점 모든 정보 요청
    //http://ec2-52-78-185-193.ap-northeast-2.compute.amazonaws.com:3000/all
    @GET("all")
    Call<ResCoffeeStoresModel> coffeeAll();

    //커피전문점 특정 업체 정보 요청
    //http://ec2-52-78-185-193.ap-northeast-2.compute.amazonaws.com:3000/coffee?t=COFFEEBEAN
    @GET("coffee")
    Call<ResCoffeeStoresModel> coffeeBrand(@Query("t") String t);

    @POST("coffee")
    Call<ResCoffeeStoresModel> coffeeDist(@Body DistModel distModel);



}
