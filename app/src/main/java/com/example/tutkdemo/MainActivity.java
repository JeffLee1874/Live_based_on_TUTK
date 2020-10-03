package com.example.tutkdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tutkdemo.model.FrameBuffer;
import com.example.tutkdemo.model.MediaHandler;
import com.example.tutkdemo.model.Utils;
import com.example.tutkdemo.tflite.Classifier;
import com.example.tutkdemo.tflite.TFLiteObjectDetectionAPIModel;
import com.example.tutkdemo.view.OverlayView;
import com.example.tutkdemo.view.PopUpWindow;
import com.example.tutkdemo.view.SimplePage;
import com.example.tutkdemo.viewmodel.ListViewAdapter;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.scwang.smartrefresh.header.PhoenixHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import at.markushi.ui.CircleButton;

public class MainActivity extends AppCompatActivity {

    private SmartRefreshLayout mPullToRefreshView;
    private TextView textView;
    private FloatingActionButton button_add_by_id;
    private CircleButton confirm_button;
    private CircleButton cancel_button;
    private List<SimplePage> pageList = new ArrayList<SimplePage>();
    private ListViewAdapter listViewAdapter;
    private MaterialEditText UID_text;
    private MaterialEditText username_text;
    private MaterialEditText password_text;
    private MediaHandler mediaHandler;
    private PopUpWindow popUpWindow;
    private OverlayView overlayview;
    private Thread ReThread;

    private Classifier detector;
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private boolean stop = false;
    private Handler handler;
    private static final int COMPLETED = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        confirm_button = (CircleButton) findViewById(R.id.confirm);
        cancel_button = (CircleButton) findViewById(R.id.cancel);
        textView = (TextView)findViewById(R.id.title);
        button_add_by_id = (FloatingActionButton) findViewById(R.id.add_by_id);
        final ListView listView = (ListView) findViewById(R.id.list_view);
        mPullToRefreshView = (SmartRefreshLayout) findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setRefreshHeader(new PhoenixHeader(this));
        Typeface mtypeface=Typeface.createFromAsset(this.getAssets(),"HanyiSentyChalk2018.ttf");
        textView.setTypeface(mtypeface);
        mPullToRefreshView.setEnableRefresh(true);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == COMPLETED) {
                    listViewAdapter = new ListViewAdapter(MainActivity.this,
                            R.layout.item_simple, pageList);
                    listView.setAdapter(listViewAdapter);
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                HashSet<String> UID = (HashSet<String>) Utils.getUID(MainActivity.this);
                if(UID!=null)
                {
                    for(String i : UID)
                    {
                        HashMap<String, String> hashMap = (HashMap<String, String>) Utils.getUserInfo(MainActivity.this,i);
                        SimplePage page1 = new SimplePage(i, hashMap.get("username"), hashMap.get("password"));
                        pageList.add(page1);
                    }
                }

                listViewAdapter = new ListViewAdapter(MainActivity.this,
                        R.layout.item_simple, pageList);
                listView.setAdapter(listViewAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        SimplePage simplePage = (SimplePage) parent.getItemAtPosition(position);
                        mediaHandler = listViewAdapter.getMediaHandler();
                        if(mediaHandler == null)
                            Log.i("UI测试", "YYYYYYYYYYY");
                        listViewAdapter.stop();
                        while(!listViewAdapter.isStop() || !listViewAdapter.getClient().isStop())
                        {

                        }
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("解码器", mediaHandler);
                        bundle.putString("UID", simplePage.getUID()) ;
                        overlayview.invalidate();
                        Intent intent = new Intent(MainActivity.this, VideoDetailActivity.class);
                        intent.putExtras(bundle);
                        //启动
                        if(mediaHandler == null)
                            Log.i("UI测试", "空");
                        Log.i("UI测试", "跳转测试");
                        startActivityForResult(intent, 1);
                    }
                });
            }
        }).start();

        mPullToRefreshView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(!pageList.isEmpty())
                        {
                            stop = true;
                            listViewAdapter.stop();
                            while (!listViewAdapter.getClient().isStop())
                            {
                                Log.i("刷新测试", String.valueOf(listViewAdapter.getClient().isStop()));
                            }
                            Log.i("刷新测试", "外部" + String.valueOf(listViewAdapter.getClient().isStop()));
//                            listViewAdapter = new ListViewAdapter(MainActivity.this,
//                                    R.layout.item_simple, pageList);
//                            listView.setAdapter(listViewAdapter);

                            Message msg = new Message();
                            msg.what = COMPLETED;
                            handler.sendMessage(msg);
                            stop = false;
                            ReThread = new Thread(new mRunnable());
                            ReThread.start();
                        }
                        refreshLayout.finishRefresh(true);
                    }
                }).start();
            }
        });

        button_add_by_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUpWindow = new PopUpWindow(MainActivity.this);
                confirm_button = popUpWindow.getContentView().findViewById(R.id.confirm);
                cancel_button = popUpWindow.getContentView().findViewById(R.id.cancel);
                UID_text = popUpWindow.getContentView().findViewById(R.id.UID);
                username_text = popUpWindow.getContentView().findViewById(R.id.username);
                password_text = popUpWindow.getContentView().findViewById(R.id.password);
                confirm_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirm_button.setPressed(true);
                        if (!(UID_text.getText()).toString().equals("") && !(username_text.getText()).toString().equals("") && !(password_text.getText()).toString().equals(""))
                        {
                            pageList.add(new SimplePage(Objects.requireNonNull(UID_text.getText()).toString(), Objects.requireNonNull(username_text.getText()).toString(), Objects.requireNonNull(password_text.getText()).toString()));
                            listView.setAdapter(new ListViewAdapter(MainActivity.this, R.layout.item_simple, pageList));
                            LinkedHashSet<String> UID = (LinkedHashSet<String>) Utils.getUID(MainActivity.this);
                            if(UID != null)
                            {
                                UID.add((UID_text.getText()).toString());
                                Utils.saveUID(MainActivity.this, UID);
                            }
                            else
                            {
                                UID = new LinkedHashSet<>();
                                UID.add((UID_text.getText()).toString());
                                Utils.saveUID(MainActivity.this, null);
                                Utils.saveUID(MainActivity.this, UID);
                            }
                            LinkedHashSet<String> linkedHashSet= new LinkedHashSet<String>();
                            linkedHashSet.add((UID_text.getText()).toString());
                            linkedHashSet.add((username_text.getText()).toString());
                            linkedHashSet.add((password_text.getText()).toString());
                            Utils.saveUserInfo(MainActivity.this, (UID_text.getText()).toString(), linkedHashSet);
                            popUpWindow.dismiss();
                        }
                        else
                        {
                            Toast toast= Toast.makeText(getApplicationContext(), "Please Enter Correct Information", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        confirm_button.setPressed(false);
                    }
                });

                cancel_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirm_button.setPressed(true);
                        popUpWindow.dismiss();
                        confirm_button.setPressed(false);
                    }
                });
                popUpWindow.setBlurBackgroundEnable(true);
                popUpWindow.setAdjustInputMethod(true);
                popUpWindow.showPopupWindow();
            }
        });
        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
        } catch (IOException e) {
            e.printStackTrace();
        }


        ReThread = new Thread(new mRunnable());
        ReThread.start();
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
    }


        public Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        if(bm == null)
        {
            Log.i("识别", "吃屎了");
            return null;
        }
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    class mRunnable implements Runnable{
        @Override
        public void run() {
            while(listViewAdapter.getOverlayView()==null)
            {}
            Log.i("huahua","chulaile");
            overlayview=listViewAdapter.getOverlayView();
            while(!stop)
            {
                if(FrameBuffer.bitmaps.isEmpty())
                    continue;

                Bitmap b = FrameBuffer.bitmaps.poll();
                if(b !=null)
                {
                    Bitmap bitmap = Bitmap.createBitmap(zoomImg(b, 300, 300));
                    List<Classifier.Recognition> recognitions = detector.recognizeImage(bitmap);
//            overlayView.draw(canvas);
//                    processImage(bitmap);
                    for(Classifier.Recognition i : recognitions) {
                        Log.i("识别", i.toString());
                    }
                    if(overlayview==null)
                        Log.i("huahua","chishi");
                    Log.i("overlay", "Weight: "+overlayview.getWidth()+"  Height: "+overlayview.getHeight());
                    //width:974 Height:473
                    overlayview.setResultRecognitions(recognitions);
//                    Log.i("huahua","chishihou");
                    overlayview.invalidate();
                }
            }
        }
    }
}