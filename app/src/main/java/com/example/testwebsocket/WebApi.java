package com.example.testwebsocket;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WebApi {
    @GET("/api/v1/aggTrades")
    Call<ArrayList<SocketData>> getData(@Query("symbol") String symbol, @Query("limit")int limit);
}
