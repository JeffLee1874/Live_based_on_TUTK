package com.example.tutkdemo.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import ua.polohalo.zoomabletextureview.ZoomableTextureView;

public class MediaHandler implements Parcelable {
    private MediaCodec mCodec;
    private MediaFormat mediaFormat;
    //    private SurfaceView surfaceView;
    private Surface mDecoderSurface;
    private final static String MIME_TYPE = "video/avc"; // H.264 Advanced Video
    private final static int VIDEO_WIDTH = 1280;
    private final static int VIDEO_HEIGHT = 720;
    private final static int TIME_INTERNAL = 50;
    private final static int HEAD_OFFSET = 512;
    private boolean StartCache = false;
    public boolean fuck = true;
    private static final int COLOR_FormatI420 = 1;
    private static final int COLOR_FormatNV21 = 2;
    private String FilePath;
    private InputStream is = null;
    private FileInputStream fs = null;
    // Video Constants
    private Thread readFileThread;
    private boolean stop = false;
    private boolean isLocalStop = false;


    public MediaHandler() {
    }

    protected MediaHandler(Parcel in) {
        mDecoderSurface = in.readParcelable(Surface.class.getClassLoader());
        StartCache = in.readByte() != 0;
        mCount = in.readInt();
    }

    public static final Creator<MediaHandler> CREATOR = new Creator<MediaHandler>() {
        @Override
        public MediaHandler createFromParcel(Parcel in) {
            return new MediaHandler(in);
        }

        @Override
        public MediaHandler[] newArray(int size) {
            return new MediaHandler[size];
        }
    };

    public boolean isStartCache() {
        return StartCache;
    }

    public void setStartCache(boolean startCache) {
        StartCache = startCache;
    }

    public void setFilePath(String path) {
        this.FilePath = path;
    }

    public void initDecoder() throws IOException {
        mCodec = MediaCodec.createDecoderByType(MIME_TYPE);
        mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
                VIDEO_WIDTH, VIDEO_HEIGHT);
        mediaFormat.setInteger(MediaFormat.KEY_LEVEL, 128);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ);
        mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
//        mCodec.configure(mediaFormat, mSurfaceView.getHolder().getSurface(),
//                null, 0);
        Log.i("读视频", "开始设置播放器");
        mCodec.configure(mediaFormat, null,
                null, 0);
        Log.i("读视频", "设置了播放器");
        mCodec.start();
    }

    public void initDecoder(Surface surface) throws IOException {
        mCodec = MediaCodec.createDecoderByType(MIME_TYPE);
        mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
                VIDEO_WIDTH, VIDEO_HEIGHT);
        mediaFormat.setInteger(MediaFormat.KEY_LEVEL, 128);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ);
        mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
//        mCodec.configure(mediaFormat, mSurfaceView.getHolder().getSurface(),
//                null, 0);
        Log.i("读视频", "开始设置播放器");
        mCodec.configure(mediaFormat, surface,
                null, 0);
        Log.i("读视频", "设置了播放器");
        mCodec.start();
    }

    int mCount = 0;

    public boolean onFrame(byte[] buf, int offset, int length) {
        if(buf != null)
        {
            Log.i("Media", "onFrame start");
            Log.i("Media", "onFrame Thread:" + Thread.currentThread().getId());
            // Get input buffer index
            ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
            int inputBufferIndex = mCodec.dequeueInputBuffer(500);

            Log.i("Media", "onFrame index:" + inputBufferIndex);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(buf, offset, length);
//            mCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount
//                    * TIME_INTERNAL, 0);
                mCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount * TIME_INTERNAL, 0);
                mCount++;
            } else {
                return false;
            }

            // Get output buffer index
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 1000);
            while (outputBufferIndex >= 0) {
                mCodec.releaseOutputBuffer(outputBufferIndex, true);
                outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 1000);
            }
            Log.e("Media", "onFrame end");
            return true;
        }
        return true;
    }

    public void createBitmap(byte[] buf, int offset, int length) {
        ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
        ByteBuffer outputBuffer;
        int inputBufferIndex = mCodec.dequeueInputBuffer(50);

        Log.i("Media", "onFrame index:" + inputBufferIndex);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buf, offset, length);
//            mCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount
//                    * TIME_INTERNAL, 0);
            mCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount * TIME_INTERNAL, 0);
            mCount++;
        }

        // Get output buffer index
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 1000);
        while (outputBufferIndex >= 0) {
            Image image = mCodec.getOutputImage(outputBufferIndex);
            if (isImageFormatSupported(image) && fuck) {
                try {
                    FrameBuffer.bitmaps.offer(getBitmap(image));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            image.close();
            mCodec.releaseOutputBuffer(outputBufferIndex, true);
            outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 1000);
        }
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    private static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
        }
        return false;
    }

    private static byte[] getDataFromImage(Image image, int colorFormat) {
        if (colorFormat != COLOR_FormatI420 && colorFormat != COLOR_FormatNV21) {
            throw new IllegalArgumentException("only support COLOR_FormatI420 " + "and COLOR_FormatNV21");
        }
        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = width * height;
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }

    private Bitmap getBitmap(Image image) throws IOException {
        Rect rect = image.getCropRect();
        YuvImage yuvImage = new YuvImage(getDataFromImage(image, COLOR_FormatNV21), ImageFormat.NV21, rect.width(), rect.height(), null);
        if (yuvImage != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(rect, 100, stream);

            Bitmap bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            //TODO：此处可以对位图进行处理，如显示，保存等

            stream.close();
            return bitmap;
        }
        return null;
    }

    public void PlayFromFile()
    {
        readFileThread = new Thread(readFile);
        if (!readFileThread.isAlive())
            readFileThread.start();
    }

    Runnable readFile = new Runnable() {

        @Override
        public void run() {
            int h264Read = 0;
            int frameOffset = 0;
            byte[] buffer = new byte[100000];
            stop = false;
            byte[] framebuffer = new byte[200000];
            try {
                fs = new FileInputStream(FilePath);
                is = new BufferedInputStream(fs);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                int length = is.available();
                if (length > 0) {
                    // Read file and fill buffer
                    int count = is.read(buffer);
                    Log.i("count", "" + count);
                    h264Read += count;
                    Log.d("Read", "count:" + count + " h264Read:"
                            + h264Read);
                    // Fill frameBuffer
                    if (frameOffset + count < 200000) {
                        System.arraycopy(buffer, 0, framebuffer,
                                frameOffset, count);
                        frameOffset += count;
                    } else {
                        frameOffset = 0;
                        System.arraycopy(buffer, 0, framebuffer,
                                frameOffset, count);
                        frameOffset += count;
                    }

                    // Find H264 head
                    int offset = findHead(framebuffer, frameOffset);
                    Log.i("find head", " Head:" + offset);
                    while (offset > 0 && !stop) {
                        if (checkHead(framebuffer, 0)) {
                            // Fill decoder
                            boolean flag = onFrame(framebuffer, 0, offset);
                            if (flag) {
                                byte[] temp = framebuffer;
                                framebuffer = new byte[200000];
                                System.arraycopy(temp, offset, framebuffer,
                                        0, frameOffset - offset);
                                frameOffset -= offset;
                                Log.e("Check", "is Head:" + offset);
                                // Continue finding head
                                offset = findHead(framebuffer, frameOffset);
                            }
                        } else {

                            offset = 0;
                        }

                    }
                    Log.d("loop", "end loop");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                is.close();
                fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isLocalStop = true;
        }
    };

    public void stopPlayFromFile()
    {
        if(readFileThread !=null)
        {
            if(readFileThread.isAlive())
                stop = true;
            else
                isLocalStop = true;
        }
        else
            isLocalStop = true;
    }

    public boolean isLocalStop() {
        return isLocalStop;
    }
}
