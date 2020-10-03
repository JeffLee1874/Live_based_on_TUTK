package com.example.tutkdemo;

import android.util.Log;

import com.example.tutkdemo.model.AudioPlayer;
import com.example.tutkdemo.model.FrameBuffer;
import com.example.tutkdemo.model.MediaHandler;
import com.tutk.IOTC.AVAPIs;
import com.tutk.IOTC.IOTCAPIs;

import java.io.IOException;

public class Client {

    private Thread videoThread;
    private Thread audioThread;
    private boolean Find_First_I_Frame = false;
    private boolean stop = false;
    private boolean isStop = false;
    private String UID;
    private String userName;
    private String passWord;
    private MediaHandler mediaHandler;
    public boolean fuck = true;


    public Client(String UID, String userName, String passWord) throws IOException {
        this.UID = UID;
        this.userName = userName;
        this.passWord = passWord;
        mediaHandler = new MediaHandler();
        mediaHandler.initDecoder();
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public boolean isStop() {
        return isStop;
    }

    public void start() {

        Log.i("刷新测试","StreamClient start...");

        int ret = IOTCAPIs.IOTC_Initialize2(0);
        Log.i("连接测试","IOTC_Initialize() ret = "+ ret);
        if (ret != IOTCAPIs.IOTC_ER_NoERROR) {
            Log.i("连接测试","IOTCAPIs_Device exit...!!\n");
            return;
        }

        // alloc 3 sessions for video and two-way audio
        AVAPIs.avInitialize(3);

        int sid = IOTCAPIs.IOTC_Get_SessionID();
        if (sid < 0)
        {
            Log.i("连接测试","IOTC_Get_SessionID error code "+ sid);
            return;
        }
        ret = IOTCAPIs.IOTC_Connect_ByUID_Parallel(UID, sid);
        Log.i("连接测试","Step 2: call IOTC_Connect_ByUID_Parallel(%s)......."+ UID);

        int[] srvType = new int[1];
        int avIndex = AVAPIs.avClientStart(sid, userName, passWord, 20000, srvType, 0);
        Log.i("连接测试","Step 2: call avClientStart(%d)......." + avIndex);

        if (avIndex < 0) {
            Log.i("连接测试","avClientStart failed " + avIndex);
            return;
        }

        if (startIpcamStream(avIndex)) {
            videoThread = new Thread(new VideoThread(avIndex),
                    "Video Thread");
            audioThread = new Thread(new AudioThread(avIndex),
                    "Audio Thread");
            audioThread.start();
            videoThread.start();

            try {
                audioThread.join();
            }
            catch (InterruptedException e) {
                System.out.println(e.getMessage());
                return;
            }

            try {
                videoThread.join();
            }
            catch (InterruptedException e) {
                System.out.println(e.getMessage());
                return;
            }

        }

        AVAPIs.avClientStop(avIndex);
        Log.i("连接测试","avClientStop OK\n");
        IOTCAPIs.IOTC_Session_Close(sid);
        Log.i("连接测试","IOTC_Session_Close OK\n");
        AVAPIs.avDeInitialize();
        IOTCAPIs.IOTC_DeInitialize();
        Log.i("连接测试","StreamClient exit...\n");
        isStop = true;
    }

    public static boolean startIpcamStream(int avIndex) {
        AVAPIs av = new AVAPIs();
        int ret = av.avSendIOCtrl(avIndex, AVAPIs.IOTYPE_INNER_SND_DATA_DELAY,
                new byte[2], 2);
        if (ret < 0) {
            Log.i("连接测试","start_ipcam_stream failed " + ret);
            return false;
        }

        // This IOTYPE constant and its corrsponsing data structure is defined in
        // Sample/Linux/Sample_AVAPIs/AVIOCTRLDEFs.h
        //
        int IOTYPE_USER_IPCAM_START = 0x1FF;
        ret = av.avSendIOCtrl(avIndex, IOTYPE_USER_IPCAM_START,
                new byte[8], 8);
        if (ret < 0) {
            Log.i("连接测试","start_ipcam_stream failed " + ret);
            return false;
        }

        int IOTYPE_USER_IPCAM_AUDIOSTART = 0x300;
        ret = av.avSendIOCtrl(avIndex, IOTYPE_USER_IPCAM_AUDIOSTART,
                new byte[8], 8);
        if (ret < 0) {
            Log.i("连接测试","start_ipcam_stream failed " + ret);
            return false;
        }

        return true;
    }

    public class VideoThread implements Runnable {
        static final int VIDEO_BUF_SIZE = 100000;
        static final int FRAME_INFO_SIZE = 16;

        private int avIndex;

        public VideoThread(int avIndex) {
            this.avIndex = avIndex;
        }

        @Override
        public void run() {
            Log.i("连接测试","[%s] Start "+
                    Thread.currentThread().getName());
            AVAPIs av = new AVAPIs();
            byte[] frameInfo = new byte[FRAME_INFO_SIZE];
            byte[] videoBuffer = new byte[VIDEO_BUF_SIZE];
            int[] outBufSize = new int[1];
            int[] outFrameSize = new int[1];
            int[] outFrmInfoBufSize = new int [1];
            while (!stop) {
                int[] frameNumber = new int[1];
                int ret = av.avRecvFrameData2(avIndex, videoBuffer,
                        VIDEO_BUF_SIZE, outBufSize, outFrameSize,
                        frameInfo, FRAME_INFO_SIZE,
                        outFrmInfoBufSize, frameNumber);
                Log.i("连接测试", "ret:" + ret);

                //让第一帧是I帧
                if(!Find_First_I_Frame)
                {
                    Log.i("信息测试", "进来寻找I帧");
                    Check_First_I_Frame(videoBuffer, ret);
                    continue;
                }

                if (ret == AVAPIs.AV_ER_DATA_NOREADY) {
                    try {
                        Thread.sleep(30);
                        continue;
                    }
                    catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                        break;
                    }
                }
                else if (ret == AVAPIs.AV_ER_LOSED_THIS_FRAME) {
                    Log.i("连接测试","[%s] Lost video frame number "+
                            Thread.currentThread().getName()+ frameNumber[0]);
                    continue;
                }
                else if (ret == AVAPIs.AV_ER_INCOMPLETE_FRAME) {
                    Log.i("连接测试","[%s] Incomplete video frame number " +
                            Thread.currentThread().getName()+ frameNumber[0]);
                    continue;
                }
                else if (ret == AVAPIs.AV_ER_SESSION_CLOSE_BY_REMOTE) {
                    Log.i("连接测试","[%s] AV_ER_SESSION_CLOSE_BY_REMOTE " +
                            Thread.currentThread().getName());
                    break;
                }
                else if (ret == AVAPIs.AV_ER_REMOTE_TIMEOUT_DISCONNECT) {
                    Log.i("连接测试","[%s] AV_ER_REMOTE_TIMEOUT_DISCONNECT "+
                            Thread.currentThread().getName());
                    break;
                }
                else if (ret == AVAPIs.AV_ER_INVALID_SID) {
                    Log.i("连接测试","[%s] Session cant be used anymore "+
                            Thread.currentThread().getName());
                    break;
                }
                else {
                    Log.i("信息测试","信息长度: " +outFrmInfoBufSize[0] + "  " + byteArrayToHexStr(frameInfo));
                    Log.i("信息测试","数据: " + byteArrayToHexStr(videoBuffer));
                    byte[] bytes = new byte[videoBuffer.length];
                    int length = ret;
                    System.arraycopy(videoBuffer,0,bytes,0,videoBuffer.length);
                    Push_To_Buffer(videoBuffer, ret);
                    if(Check_I_Frame(bytes,length))
                    {
                        Log.i("识别", "长度" + length);
                        mediaHandler.createBitmap(bytes,0,length);
//                        Bitmap bitmap = Bytes2Bimap(bytes, length);
//                        if(fuck)
//                            saveBitmap(bitmap, "/sdcard/chishi.jpeg");
                    }

                }

                // Now the data is ready in videoBuffer[0 ... ret - 1]
                // Do something here
            }

            Log.i("连接测试","[%s] Exit " +
                    Thread.currentThread().getName());
        }
    }

    public void Check_First_I_Frame(byte[] byteArray, int length)
    {
        byte[] bytes = new byte[1];
        bytes[0] = byteArray[4];
        Log.i("信息测试", "NAL类型：" + byteArrayToHexStr(bytes));
        int NAL_type = byteArray[4] & 0x1F;
        if(NAL_type == 5 || NAL_type == 7 || NAL_type == 8 || NAL_type == 2)
        {
            Find_First_I_Frame = true;
            Push_To_Buffer(byteArray, length);
            Log.i("信息测试", "找到第一个I帧");
        }
    }

    public boolean Check_I_Frame(byte[] byteArray, int length)
    {
        byte[] bytes = new byte[1];
        bytes[0] = byteArray[4];
        Log.i("信息测试", "NAL类型：" + byteArrayToHexStr(bytes));
        int NAL_type = byteArray[4] & 0x1F;
        return NAL_type == 5 || NAL_type == 7 || NAL_type == 8 || NAL_type == 2;
    }

    private void Push_To_Buffer(byte[] byteArray, int length)
    {
        FrameBuffer.queue.offer(byteArray);
        FrameBuffer.Flags.offer(length);
    }

    public String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null){
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public class AudioThread implements Runnable {
        static final int AUDIO_BUF_SIZE = 1024;
        static final int FRAME_INFO_SIZE = 16;

        private int avIndex;
        private AudioPlayer AudioPlayer=new AudioPlayer();

        public AudioThread(int avIndex) {
            this.avIndex = avIndex;
        }

        @Override
        public void run() {
            Log.i("连接测试","[%s] Start "+
                    Thread.currentThread().getName());

            AVAPIs av = new AVAPIs();
            byte[] frameInfo = new byte[FRAME_INFO_SIZE];
            byte[] audioBuffer = new byte[AUDIO_BUF_SIZE];
            AudioPlayer.mAudioTrack.play();
            while (!stop) {
                int ret = av.avCheckAudioBuf(avIndex);

                if (ret < 0) {
                    // Same error codes as below
                    Log.i("连接测试","[%s] avCheckAudioBuf() failed: "+
                            Thread.currentThread().getName()+ret);
                    break;
                }
                else if (ret < 3) {
                    try {
                        Thread.sleep(120);
                        continue;
                    }
                    catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                        AudioPlayer.mAudioTrack.stop();
                        break;
                    }
                }

                int[] frameNumber = new int[1];
                ret = av.avRecvAudioData(avIndex, audioBuffer,
                        AUDIO_BUF_SIZE, frameInfo, FRAME_INFO_SIZE,
                        frameNumber);

                if (ret == AVAPIs.AV_ER_SESSION_CLOSE_BY_REMOTE) {
                    Log.i("连接测试","[%s] AV_ER_SESSION_CLOSE_BY_REMOTE "+
                            Thread.currentThread().getName());
                    break;
                }
                else if(ret == AVAPIs.AV_ER_DATA_NOREADY)
                {
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                else if (ret == AVAPIs.AV_ER_REMOTE_TIMEOUT_DISCONNECT) {
                    Log.i("连接测试","[%s] AV_ER_REMOTE_TIMEOUT_DISCONNECT "+
                            Thread.currentThread().getName());
                    break;
                }
                else if (ret == AVAPIs.AV_ER_INVALID_SID) {
                    Log.i("连接测试","[%s] Session cant be used anymore "+
                            Thread.currentThread().getName());
                    break;
                }
                else if (ret == AVAPIs.AV_ER_LOSED_THIS_FRAME) {
                    //Log.i("连接测试","[%s] Audio frame losed\n",
                    //        Thread.currentThread().getName());
                    continue;
                }

                // Now the data is ready in audioBuffer[0 ... ret - 1]
                // Do something here
                /*G711a convert to Pcm*/
                byte[] PcmaudioBuf=new byte[AUDIO_BUF_SIZE];
                Log.i("长度", String.valueOf(ret));
                PcmaudioBuf=AudioPlayer.convertG711aToPcm(audioBuffer,ret,PcmaudioBuf);
                Log.i("ret", "G711A\n");
                Log.i("ret", byteArrayToHexStr(audioBuffer));
                Log.i("ret", "PCM\n");
                Log.i("ret", byteArrayToHexStr(PcmaudioBuf));
                Log.i("Audiodate", String.valueOf(PcmaudioBuf.length));
                AudioPlayer.mAudioTrack.write(PcmaudioBuf,0,PcmaudioBuf.length);
//                AudioPlayer.mAudioTrack.play();
//                AudioPlayer.mAudioTrack.stop();
//                AudioPlayer.mAudioTrack.flush();
            }

            Log.i("连接测试","[%s] Exit "+
                    Thread.currentThread().getName());
        }
    }

}

