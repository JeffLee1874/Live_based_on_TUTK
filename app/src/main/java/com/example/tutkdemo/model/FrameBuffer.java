package com.example.tutkdemo.model;

import android.graphics.Bitmap;

import java.util.LinkedList;
import java.util.Queue;

public class FrameBuffer {
    public static Queue<byte[]> queue = new LinkedList<>();
    public static Queue<Integer> Flags = new LinkedList<>();
    public static Queue<Bitmap> bitmaps = new LinkedList<>();
}
