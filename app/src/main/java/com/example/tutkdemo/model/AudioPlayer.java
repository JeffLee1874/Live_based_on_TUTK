package com.example.tutkdemo.model;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioPlayer {
    public AudioTrack mAudioTrack;
    //音频流类型
    private static final int mStreamType = AudioManager.STREAM_MUSIC;
    //指定采样率 （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。 设置采样率为44100，目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
    private static final int mSampleRateInHz=8000 ;
    //指定捕获音频的声道数目。在AudioFormat类中指定用于此的常量
    private static final int mChannelConfig= AudioFormat.CHANNEL_CONFIGURATION_MONO; //单声道
    //指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。
    //因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实。
    private static final int mAudioFormat= AudioFormat.ENCODING_PCM_16BIT;
    //指定缓冲区大小。调用AudioRecord类的getMinBufferSize方法可以获得。
    private int mMinBufferSize;
    //STREAM的意思是由用户在应用程序通过write方式把数据一次一次得写到audiotrack中。
    // 应用层从某个地方获取数据，例如通过编解码得到PCM数据，然后write到audiotrack。
    private static int mMode = AudioTrack.MODE_STREAM;
//    private static int mMode = AudioTrack.MODE_STATIC;

    public AudioPlayer() {
        initData();
    }

    private void initData(){
        //根据采样率，采样精度，单双声道来得到frame的大小。
        mMinBufferSize = AudioTrack.getMinBufferSize(mSampleRateInHz,mChannelConfig, mAudioFormat)*8;//计算最小缓冲区
        //注意，按照数字音频的知识，这个算出来的是一秒钟buffer的大小。
        //创建AudioTrack
        mAudioTrack = new AudioTrack(mStreamType, mSampleRateInHz,mChannelConfig,
                mAudioFormat,mMinBufferSize,mMode);
    }


    public byte[] convertG711aToPcm(byte[] g711Buf, int length, byte[] pcmBuf)
    {
        pcmBuf = new byte[length*2];

        for (int i=0; i<length; i++)
        {
            byte alaw = g711Buf[i];
            alaw ^= 0xD5;

            int sign     =  alaw & 0x80;
            int exponent = (alaw & 0x70) >> 4;
            int value    = (alaw & 0x0F) >> 4 + 8;
            if (exponent != 0)
            {
                value += 0x0100;
            }
            if (exponent > 1)
            {
                value <<= (exponent - 1);
            }
            value = (char)((sign == 0 ? value : -value) & 0xFFFF);
            pcmBuf[i*2+0] = (byte) (value      & 0xFF);
            pcmBuf[i*2+1] = (byte) (value >> 8 & 0xFF);
        }
        return pcmBuf;
    }

}
