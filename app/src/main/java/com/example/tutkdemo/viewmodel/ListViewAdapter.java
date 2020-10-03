package com.example.tutkdemo.viewmodel;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.cardview.widget.CardView;

import com.example.tutkdemo.Client;
import com.example.tutkdemo.R;
import com.example.tutkdemo.model.FrameBuffer;
import com.example.tutkdemo.model.MediaHandler;
import com.example.tutkdemo.view.OverlayView;
import com.example.tutkdemo.view.SimplePage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ListViewAdapter extends ArrayAdapter<SimplePage> {
    private int resourceId;
    public static String UID = "GV4GRAS1S2XJY3F1111A";
    boolean isInit = false;
    private Client client;
    private MediaHandler mediaHandler;
    public Thread VideoHandler;
    private SurfaceView mSurfaceView;
    private CardView cardView;
    private boolean isStop = false;
    private boolean stop = false;
    public boolean fuck = true;
    private Button button;
    private Context context;
    private OverlayView overlayView;

    public ListViewAdapter(Context context, int textViewResourceId,
                           List<SimplePage> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        this.context = context;
    }

    public void stop() {
        if (client != null)
            client.setStop(true);
        if (VideoHandler != null)
            stop = true;
    }

    public MediaHandler getMediaHandler() {
        return mediaHandler;
    }

    public void setMediaHandler(MediaHandler mediaHandler) {
        this.mediaHandler = mediaHandler;
    }

    public Client getClient() {
        return client;
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public OverlayView getOverlayView(){return overlayView;}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SimplePage page = getItem(position); // 获取当前项的page实例
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);

        Log.i("刷新测试", "新的一页");

        cardView = view.findViewById(R.id.card_view);
        overlayView=view.findViewById(R.id.tracking_overlay);
        mediaHandler = new MediaHandler();
        Log.i("UI测试", "解码器准备好了");
        if (mediaHandler == null)
            Log.i("UI测试", "XXXXXXXXXXXXXXX");

        mSurfaceView = (SurfaceView) view.findViewById(R.id.surfaceView);
        final SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.i("UI测试", "准备好了");
                if (!isInit) {
                    try {
                        mediaHandler.initDecoder(holder.getSurface());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    (new Thread() {
                        public void run() {
                            Log.i("刷新测试", "1");
                            try {
                                client = new Client(page.getUID(), page.getUserName(), page.getPassWord());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Log.i("刷新测试", "2");
                            client.start();
                            Log.i("刷新测试", "3");
                        }
                    }).start();
                    isInit = true;
                }

                VideoHandler = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!stop) {
                            if (!FrameBuffer.queue.isEmpty() && !FrameBuffer.Flags.isEmpty()) {
                                Log.i("队列数量", FrameBuffer.queue.size() + " " + FrameBuffer.Flags.size());
                                mediaHandler.onFrame(FrameBuffer.queue.poll(), 0, FrameBuffer.Flags.poll());
                            }
                        }
                        Log.i("刷新测试", "解析线程结束");
                        isStop = true;
                    }
                });
                VideoHandler.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                stop();
            }
        });


//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Bitmap bitmap = mSurfaceView.getBitmap();
//                saveBitmap(bitmap,"/sdcard/chishi.jpeg");
//            }
//        });
        return view;
    }

//    private void testViewSnapshot(View view) {
//        //使控件可以进行缓存
//        view.setDrawingCacheEnabled(true);
//        //获取缓存的 Bitmap
//        Bitmap drawingCache = view.getDrawingCache();
//        //复制获取的 Bitmap
//        drawingCache = Bitmap.createBitmap(drawingCache);
//        if(fuck)
//            saveBitmap(drawingCache, "/sdcard/chishi.jpeg");
//        //关闭视图的缓存
//        view.setDrawingCacheEnabled(false);
//
//        if (drawingCache != null) {
//            Log.i("识别", "有了");
//        } else {
//            Log.i("识别", "mei了");
//        }
//    }

//    public void saveBitmap(Bitmap bitmap, String path) {
//        String savePath = path;
//        File filePic;
//        try {
//            filePic = new File(savePath);
//            if (!filePic.exists()) {
//                filePic.getParentFile().mkdirs();
//                filePic.createNewFile();
//            }
//            FileOutputStream fos = new FileOutputStream(filePic);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//            fos.flush();
//            fos.close();
//            fuck = false;
//        } catch (IOException e) {
//            Log.e("tag", "saveBitmap: " + e.getMessage());
//            return;
//        }
//        Log.i("tag", "saveBitmap success: " + filePic.getAbsolutePath());
//    }
}
