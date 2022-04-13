package com.example.testwebsocket;

import android.os.Bundle;



import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testwebsocket.databinding.ActivityMainBinding;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import org.reactivestreams.Subscription;

import java.net.URI;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MyAdapter adapter;
    private JWebSocketClient client;
    private WebApi webApi;
    private Disposable timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerList.setLayoutManager(layoutManager);
        binding.recyclerList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new MyAdapter();
//        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onItemRangeInserted(int positionStart, int itemCount) {
//                super.onItemRangeInserted(positionStart, itemCount);
//                layoutManager.scrollToPosition(0);
//            }
//        });

        binding.recyclerList.setAdapter(adapter);


        retrieveData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(client != null)
        {
            client.close();
        }

        if(timer != null)
        {
            timer.dispose();
        }
    }

    private void retrieveData() {
        final String BASE_URL = "https://api.yshyqxx.com";
        final String SYMBOL = "BTCUSDT";
        final int LIMIT = 40;

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        webApi = retrofit.create(WebApi.class);
        final Call<ArrayList<SocketData>> call = webApi.getData(SYMBOL, LIMIT);
        call.enqueue(new Callback<ArrayList<SocketData>>() {
            @Override
            public void onResponse(Call<ArrayList<SocketData>> call, Response<ArrayList<SocketData>> response) {
                adapter.setDataList(response.body());
                adapter.notifyDataSetChanged();

                createWebSocket();
                createTimer();
            }

            @Override
            public void onFailure(Call<ArrayList<SocketData>> call, Throwable t) {
                // 連線失敗
            }
        });
    }

    private void createWebSocket() {
        final String WS_URL = "wss://stream.yshyqxx.com/ws/btcusdt@aggTrade";
        final URI uri = URI.create(WS_URL);

        client = new JWebSocketClient(uri) {
            @Override
            public void onMessage(String message) {
                //Log.e("MSG", message);

                JsonParser parser = new JsonParser();
                JsonElement mJson =  parser.parse(message);
                Gson gson = new Gson();
                SocketData socketData = gson.fromJson(mJson, SocketData.class);
                adapter.addData(socketData);
            }

            @Override
            public void onError(Exception ex) {
                super.onError(ex);
                //Log.e("MSG", "Error");
            }
        };

        try {
            client.connectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createTimer()
    {
        timer = Observable.interval(3000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private final int LIMIT_SIZE = 40;

        ArrayList<SocketData> dataList;
        private SimpleDateFormat dateFormat;
        private DecimalFormat formater = new DecimalFormat("#.######");

        public MyAdapter() {
            dateFormat = new SimpleDateFormat("HH:mm:ss");
        }

        public MyAdapter(ArrayList<SocketData> dataList) {
            setDataList(dataList);
        }

        public void setDataList(ArrayList<SocketData> dataList) {
            this.dataList = dataList;
            sort();
        }

        public synchronized void addData(SocketData data)
        {
            dataList.add(0, data);
            while(dataList.size() > LIMIT_SIZE)
                dataList.remove(dataList.size()-1);
            sort();
        }

        private void sort()
        {
            Collections.sort(dataList, new Comparator<SocketData>(){
                public int compare(SocketData obj1, SocketData obj2) {
                    // ## Descending order
                    return Long.valueOf(obj2.getT()).compareTo(Long.valueOf(obj1.getT()));
                }
            });
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;

            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            holder.txtItemTime.setText(dateFormat.format(dataList.get(position).getT()));
            holder.txtItemPrice.setText(dataList.get(position).getP() + "");
            holder.txtItemAmount.setText(formater.format(dataList.get(position).getQ()));


        }

        //RecyclerView長度
        @Override
        public int getItemCount() {
            if (dataList == null)
                return 0;
            return dataList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView txtItemTime;
            public TextView txtItemPrice;
            public TextView txtItemAmount;

            public ViewHolder(View holder) {
                super(holder);
                txtItemTime = (TextView) holder.findViewById(R.id.txt_item_time);
                txtItemPrice = (TextView) holder.findViewById(R.id.txt_item_price);
                txtItemAmount = (TextView) holder.findViewById(R.id.txt_item_amount);
            }
        }

        class ViewHolderHeader extends RecyclerView.ViewHolder {
            public TextView txtHeaderTime;
            public TextView txtHeaderPrice;
            public TextView txtHeaderAmount;

            public ViewHolderHeader(View holder) {
                super(holder);
                txtHeaderTime = (TextView) holder.findViewById(R.id.txt_header_time);
                txtHeaderPrice = (TextView) holder.findViewById(R.id.txt_header_price);
                txtHeaderAmount = (TextView) holder.findViewById(R.id.txt_header_amount);
            }
        }
    }
}