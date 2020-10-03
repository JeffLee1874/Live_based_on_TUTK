package com.example.tutkdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.tutkdemo.model.FrameBuffer;
import com.example.tutkdemo.model.MediaHandler;
import com.example.tutkdemo.model.Utils;
import com.example.tutkdemo.tflite.Classifier;
import com.example.tutkdemo.view.OverlayView;
import com.example.tutkdemo.view.SimplePage;
import com.example.tutkdemo.viewmodel.ViewPagerAdapter;
import com.rd.PageIndicatorView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.suke.widget.SwitchButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import mazouri.statebutton.StateButton;
import ua.polohalo.zoomabletextureview.ZoomableTextureView;

public class VideoDetailActivity extends AppCompatActivity{

    private ListView listView;
    private SimpleAdapter simpleAdapter;
    private ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
    private ZoomableTextureView zoomableTextureView;
    private String FileParentPath = "/sdcard";
    private Surface mDecoderSurface;
    private ViewPager viewPager;
    private SwitchButton switchButton;
    private PageIndicatorView pageIndicatorView;
    private List<View> pages;
    private View page1;
    private View page2;
    private MediaHandler mediaHandler;
    private MediaHandler preHandler;
    public Thread VideoHandler;
    private StateButton live;
    private StateButton TF;
    boolean isInit = false;
    private Client client;
    private boolean isStop = false;
    private boolean stop = false;
    private SimplePage simplePage;
    private ShineButton shineButton;
    private String UID;
    private MaterialEditText type_location;
    private MaterialEditText type_usage;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_detail);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        preHandler = (MediaHandler) bundle.get("解码器");
        UID = bundle.getString("UID");
        if( preHandler == null)
            Log.i("UI测试", "空");

        mediaHandler = new MediaHandler();

        HashMap<String, String> hashMap = (HashMap<String, String>) Utils.getUserInfo(VideoDetailActivity.this,UID);
        simplePage = new SimplePage(UID, hashMap.get("username"), hashMap.get("password"));


        pages = new ArrayList<>();
        page1 = View.inflate(this,R.layout.viewpage_description, null);
        page2 = View.inflate(this,R.layout.viewpage_videocache, null);

        type_location = page1.findViewById(R.id.type_location);
        type_usage = page1.findViewById(R.id.type_usage);

        HashMap<String, String> info = (HashMap<String, String>) Utils.getDetail(VideoDetailActivity.this,UID);
        Log.i("UI测试", "+" + info.get("location"));
        type_location.setText(info.get("location"));
        type_usage.setText(info.get("usage"));

        type_usage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                shineButton.setChecked(false);
            }
        });

        type_location.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                shineButton.setChecked(false);
            }
        });

        live = page1.findViewById(R.id.live_button);
        TF = page1.findViewById(R.id.TF_button);
        live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(live.getState() == StateButton.BUTTON_STATES.ENABLED)
                {
                    if(client != null)
                        client.setStop(true);
                    setStop(true);
                    live.setState(StateButton.BUTTON_STATES.SELECTED);
                }
                else if(live.getState() == StateButton.BUTTON_STATES.SELECTED)
                {
                    live.setState(StateButton.BUTTON_STATES.ENABLED);

                    //清空上一帧残留
                    FrameBuffer.queue.clear();
                    FrameBuffer.Flags.clear();
//                    zoomableTextureView.destroyDrawingCache();
                    setStop(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mediaHandler.stopPlayFromFile();
                            while (!mediaHandler.isLocalStop())
                            {

                            }
                            (new Thread() {
                                public void run() {
                                    Log.i("刷新测试", "1");
                                    try {
                                        client = new Client(simplePage.getUID(), simplePage.getUserName(), simplePage.getPassWord());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    Log.i("刷新测试", "2");
                                    client.start();
                                    Log.i("刷新测试", "3");
                                }
                            }).start();
                            VideoHandler = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    isStop = false;
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
                    }).start();
                }
            }
        });
        TF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TF.getState() == StateButton.BUTTON_STATES.ENABLED)
                    TF.setState(StateButton.BUTTON_STATES.SELECTED);
                else if(TF.getState() == StateButton.BUTTON_STATES.SELECTED)
                    TF.setState(StateButton.BUTTON_STATES.ENABLED);
            }
        });


        shineButton = page1.findViewById(R.id.save_button);
        shineButton.init(this);
        shineButton.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                if(checked)
                {
                    Log.i("UI测试","亮了");
                    Utils.saveDetail(VideoDetailActivity.this, UID, null);
                    HashSet<String> linkedHashSet = new HashSet<>();
                    linkedHashSet.add(type_location.getText().toString());
                    linkedHashSet.add(type_usage.getText().toString());
                    Log.i("储存", type_location.getText().toString());
                    Log.i("储存", type_usage.getText().toString());
                    Utils.saveDetail(VideoDetailActivity.this, UID, linkedHashSet);
                }

            }
        });

        listView = (ListView)page2.findViewById(R.id.file_list);
        switchButton = (SwitchButton)page2.findViewById(R.id.switch_button);
        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if(isChecked)
                    preHandler.setStartCache(true);
                Log.i("UI测试", String.valueOf( preHandler.isStartCache()));
            }
        });
        Typeface mtypeface=Typeface.createFromAsset(this.getAssets(),"laborunion-regular.otf");
        TextView textView1 = page2.findViewById(R.id.title_location);
        TextView textView2 = page2.findViewById(R.id.file_list_title);
        textView1.setTypeface(mtypeface);
        textView2.setTypeface(mtypeface);
        Refresh_Cache_File(FileParentPath);
        getSurface();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(live.getState() == StateButton.BUTTON_STATES.ENABLED)
                {
                    Toast toast = Toast.makeText(VideoDetailActivity.this, "Now is Live Stream MODE!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else
                {
                    while (!isStop)
                    {

                    }
                    TextView textView= (TextView) view.findViewById(R.id.file_path);
                    Toast toast= Toast.makeText(getApplicationContext(), textView.getText(), Toast.LENGTH_SHORT);
                    toast.show();
                    Log.i("读视频", "文件路径 " + (String)textView.getText());
                    mediaHandler.setFilePath((String) textView.getText());
//                            playFromFile.setFilePath((String) textView.getText());
                    //                                playFromFile.Start_Play(mDecoderSurface);
                    mediaHandler.PlayFromFile();
                }
            }
        });


        pages.add(page1);
        pages.add(page2);

        pageIndicatorView = findViewById(R.id.pageIndicatorView);
        pageIndicatorView.setCount(pages.size()); // specify total count of indicators
        pageIndicatorView.setSelection(1);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new ViewPagerAdapter(pages));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {/*empty*/}

            @Override
            public void onPageSelected(int position) {
                pageIndicatorView.setSelection(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {/*empty*/}
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        client.setStop(true);
        stop = true;
        while(!client.isStop())
        {

        }
    }




//    SD卡中读取h264文件路径并添加到Listview
    private void Refresh_Cache_File(String FileParentPath)
    {
        Log.i("找文件", "进来找文件了1");
        File file_folder = new File(FileParentPath);
        if(file_folder.exists() && file_folder.isDirectory())
        {
            File[] files = file_folder.listFiles();
            Log.i("找文件", "目录下文件数目" + files.length);
            for(int i = 0; i < files.length; i++)
            {
                String[] strings = files[i].getName().split("\\.");
                if(strings.length != 2)
                    continue;
                if(strings[strings.length-1].equals("h264") || strings[strings.length-1].equals("264"))
                {
                    Log.i("找文件", files[i].getName());
                    Log.i("找文件", "找到第" + i + "个h264文件： " + files[i].getName());
                    HashMap<String, String> item = new HashMap<>();
                    item.put("File_Name", strings[0]);
                    item.put("File_Path", files[i].getPath());
                    listItem.add(item);
                }
            }
            simpleAdapter = new SimpleAdapter(this, listItem, R.layout.fileitem, new String[]{"File_Name", "File_Path"}, new int[]{R.id.file_name, R.id.file_path});
            listView.setAdapter(simpleAdapter);
        }
    }

    private void getSurface()
    {
        zoomableTextureView = (ZoomableTextureView) findViewById(R.id.detail_video);
        zoomableTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mDecoderSurface = new Surface(surface);
                Log.i("UI测试", "准备好了");

                if (!isInit) {
                    try {
                        mediaHandler.initDecoder(mDecoderSurface);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    (new Thread() {
                        public void run() {
                            Log.i("刷新测试", "1");
                            try {
                                client = new Client(simplePage.getUID(), simplePage.getUserName(), simplePage.getPassWord());
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
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        while (!stop) {
                            if (!FrameBuffer.queue.isEmpty() && !FrameBuffer.Flags.isEmpty()) {
                                Log.i("队列数量", FrameBuffer.queue.size() + " " + FrameBuffer.Flags.size());
                                if(FrameBuffer.queue.peek() == null)
                                    FrameBuffer.queue.poll();
                                else
                                    mediaHandler.onFrame(FrameBuffer.queue.poll(), 0, FrameBuffer.Flags.poll());
                            }
                        }
                        Log.i("刷新测试", "解析线程结束");
                        isStop = true;
                    }
                });
                VideoHandler.start();

                Log.i("UI测试", "准备好了1");
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                Log.i("UI测试", "准备好了3");
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.i("UI测试", "准备好了4");
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                Log.i("UI测试", "准备好了5");
            }
        });
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
