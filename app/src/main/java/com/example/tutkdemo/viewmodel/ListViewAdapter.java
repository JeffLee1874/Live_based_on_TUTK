package com.example.tutkdemo.viewmodel;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.example.tutkdemo.Client;
import com.example.tutkdemo.MainActivity;
import com.example.tutkdemo.R;
import com.example.tutkdemo.model.FrameBuffer;
import com.example.tutkdemo.model.MediaHandler;
import com.example.tutkdemo.view.page;

import java.io.IOException;
import java.util.List;

public class ListViewAdapter extends ArrayAdapter<page> {
    private int resourceId;
    public static String UID = "GV4GRAS1S2XJY3F1111A";
    boolean isInit = false;
    private Client client;
    private MediaHandler mediaHandler;


    public ListViewAdapter(Context context, int textViewResourceId,
                            List<page> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        page page = getItem(position); // 获取当前项的Fruit实例
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        SurfaceView mSurfaceView = (SurfaceView) view.findViewById(R.id.surfaceView);
        Button mReadButton = (Button) view.findViewById(R.id.btn_readfile);
        mediaHandler = new MediaHandler(mSurfaceView);
        mReadButton.setText(page.getButton());
        mReadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isInit) {
                    try {
                        mediaHandler.initDecoder();
                        (new Thread() {
                            public void run() {
                                client = new Client();
                                client.start(UID);
                            }
                        }).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isInit = true;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                (new Thread() {
                    public void run() {
                        while(true)
                        {
                            if(!FrameBuffer.queue.isEmpty() && !FrameBuffer.Flags.isEmpty())
                            {
                                Log.i("队列数量", FrameBuffer.queue.size() + " " + FrameBuffer.Flags.size());
                                mediaHandler.onFrame(FrameBuffer.queue.poll(),0, FrameBuffer.Flags.poll());
                            }
                        }
                    }
                }).start();

            }
        });

        return view;
    }
}
