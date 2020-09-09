package com.example.tutkdemo.model;

import java.util.LinkedList;
import java.util.Queue;

public class FrameBuffer {
    public static Queue<byte[]> queue = new LinkedList<>();
    public static Queue<Integer> Flags = new LinkedList<>();
}
