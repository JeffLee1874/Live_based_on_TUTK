package com.example.tutkdemo.model;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.SurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaHandler {
    private MediaCodec mCodec;
    private SurfaceView mSurfaceView;
    private final static String MIME_TYPE = "video/avc"; // H.264 Advanced Video
    private final static int VIDEO_WIDTH = 1280;
    private final static int VIDEO_HEIGHT = 720;
    private final static int TIME_INTERNAL = 50;
    private final static int HEAD_OFFSET = 512;

    public MediaHandler(SurfaceView surfaceView)
    {
        this.mSurfaceView = surfaceView;
    }

    public void initDecoder() throws IOException {
        mCodec = MediaCodec.createDecoderByType(MIME_TYPE);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
                VIDEO_WIDTH, VIDEO_HEIGHT);
        mediaFormat.setInteger(MediaFormat.KEY_LEVEL, 128);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ);
        mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
        mCodec.configure(mediaFormat, mSurfaceView.getHolder().getSurface(),
                null, 0);
        mCodec.start();
    }

    int mCount = 0;

    public boolean onFrame(byte[] buf, int offset, int length) {
        Log.i("Media", "onFrame start");
        Log.i("Media", "onFrame Thread:" + Thread.currentThread().getId());
        // Get input buffer index
        ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
        int inputBufferIndex = mCodec.dequeueInputBuffer(1000);

        Log.i("Media", "onFrame index:" + inputBufferIndex);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buf, offset, length);
//            mCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount
//                    * TIME_INTERNAL, 0);
            mCodec.queueInputBuffer(inputBufferIndex, 0, length, 1000, 0);
            mCount++;
        } else {
            return false;
        }

        // Get output buffer index
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 100);
        while (outputBufferIndex >= 0) {
            mCodec.releaseOutputBuffer(outputBufferIndex, true);
            outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 100);
        }
        Log.e("Media", "onFrame end");
        return true;
    }

    /**
     * Find H264 frame head
     *
     * @param buffer
     * @param len
     * @return the offset of frame head, return 0 if can not find one
     */
    static int findHead(byte[] buffer, int len) {
        int i;
        for (i = HEAD_OFFSET; i < len; i++) {
            if (checkHead(buffer, i))
                break;
        }
        if (i == len)
            return 0;
        if (i == HEAD_OFFSET)
            return 0;
        return i;
    }

    /**
     * Check if is H264 frame head
     *
     * @param buffer
     * @param offset
     * @return whether the src buffer is frame head
     */
    static boolean checkHead(byte[] buffer, int offset) {
        // 00 00 00 01
        if (buffer[offset] == 0 && buffer[offset + 1] == 0
                && buffer[offset + 2] == 0 && buffer[3] == 1)
            return true;
        // 00 00 01
        return buffer[offset] == 0 && buffer[offset + 1] == 0
                && buffer[offset + 2] == 1;
    }
}
