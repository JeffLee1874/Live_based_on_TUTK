package com.example.tutkdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.tutkdemo.view.page;
import com.example.tutkdemo.viewmodel.ListViewAdapter;
import com.scwang.smartrefresh.header.PhoenixHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;


import java.util.ArrayList;
import java.util.List;
//import com.example.tutkdemo.model.pageholder;
//import com.example.tutkdemo.view.page;

public class MainActivity extends AppCompatActivity {

    static final int REFRESH_DELAY = 1000;
//    private pageholder page1holder;
    // 是否使用特殊的标题栏背景颜色，android5.0以上可以设置状态栏背景色，如果不使用则使用透明色值
    protected boolean useThemestatusBarColor = false;
    //是否使用状态栏文字和图标为暗色，如果状态栏采用了白色系，则需要使状态栏和图标为暗色，android6.0以上可以设置
    protected boolean useStatusBarColor = true;
    private SmartRefreshLayout mPullToRefreshView;
    private TextView textView;
    private List<page> pageList = new ArrayList<page>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ActivityMainBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
////        page page = new page("Start");
//        binding.setPage1(page);
        textView = (TextView)findViewById(R.id.title);
        ListView listView = (ListView) findViewById(R.id.list_view);
        mPullToRefreshView = (SmartRefreshLayout) findViewById(R.id.pull_to_refresh);

        mPullToRefreshView.setRefreshHeader(new PhoenixHeader(this));
        Typeface mtypeface=Typeface.createFromAsset(this.getAssets(),"HanyiSentyChalk2018.ttf");
        textView.setTypeface(mtypeface);
        mPullToRefreshView.setEnableRefresh(true);
        mPullToRefreshView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
            }
        });

        page page1 = new page("开始");
        pageList.add(page1);

        ListViewAdapter listViewAdapter = new ListViewAdapter(MainActivity.this,
                R.layout.item, pageList);
        listView.setAdapter(listViewAdapter);


//        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
//        mediaHandler = new MediaHandler(mSurfaceView);
//        page1holder = new pageholder(mSurfaceView, getApplicationContext());
//        mReadButton = (Button) findViewById(R.id.btn_readfile);

//        mPullToRefreshView.setOnRefreshListener(new OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                mPullToRefreshView.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mPullToRefreshView.setRefreshing(false);
//                    }
//                }, REFRESH_DELAY);
//            }
//        });
//        mPullToRefreshView.setOnRefreshListener(new OnRefreshListener() {
//            @Override
//            public void onRefresh(RefreshLayout refreshlayout) {
//                refreshlayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
//            }
//        });
    }

    protected void setStatusBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0及以上
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            //根据上面设置是否对状态栏单独设置颜色
//            if (useThemestatusBarColor) {
//                getWindow().setStatusBarColor(getResources().getColor(R.color.colorTheme));//设置状态栏背景色
//            } else {
                getWindow().setStatusBarColor(Color.TRANSPARENT);//透明
//            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4到5.0
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        } else {
            Toast.makeText(this, "低于4.4的android系统版本不存在沉浸式状态栏", Toast.LENGTH_SHORT).show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && useStatusBarColor) {//android6.0以后可以对状态栏文字颜色和图标进行修改
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        readFileThread.interrupt();
//        page1holder.readFileThread.interrupt();
    }

}