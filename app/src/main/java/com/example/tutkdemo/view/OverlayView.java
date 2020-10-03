/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.example.tutkdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.tutkdemo.tflite.Classifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/** A simple View providing a render callback to other classes. */
public class OverlayView extends View {
  private List<Classifier.Recognition> ResultRecognitions = new LinkedList<Classifier.Recognition>();
  private Paint mPaint=new Paint();
  private Paint textpaint=new Paint();
  private ArrayList<RectF> rect=new ArrayList<>();
  private Canvas mcanvas;
  private float weightcount=974/300;
  private float heightecount=973/300;

  public OverlayView(Context context) {
    super(context);
    initPaint();
    Handler handler=this.getHandler();
  }

  private void initPaint(){
    mPaint.setColor(Color.RED);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeWidth(5.0f);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeMiter(100);

    textpaint.setColor(Color.BLUE);
    textpaint.setAntiAlias(true);//设置抗锯齿
    textpaint.setStrokeWidth(2);//线条粗细
    textpaint.setStyle(Paint.Style.STROKE);//设置空心
    textpaint.setTextSize(26);
  }


  public void setResultRecognitions(List<Classifier.Recognition> resultRecognitions) {
    if(resultRecognitions!=null){
       ResultRecognitions = resultRecognitions;
    }
  }

  public OverlayView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    initPaint();
  }


  public Canvas getcanvas(){
    return mcanvas;
  }
  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    mcanvas=canvas;
    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    Paint p = new Paint();
    //清屏
    p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    canvas.drawPaint(p);
    p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    Log.i("huahua","qingkongle");
    if(ResultRecognitions!=null){
      Log.i("huahua","kaihua");
      Iterator<Classifier.Recognition> iterator = ResultRecognitions.iterator();
      while (iterator.hasNext()){
        Classifier.Recognition i=iterator.next();
        if(i.getConfidence()*100>40){
           RectF rectf=i.getLocation();
           if((rectf.bottom*heightecount) > 473)
             rectf.set(rectf.left*weightcount, (float) (rectf.top*(heightecount+0.1)),rectf.right*weightcount, 470);
           else
             rectf.set(rectf.left*weightcount, (float) (rectf.top*(heightecount+0.1)),rectf.right*weightcount, (float) (rectf.bottom*heightecount*0.8));
           rect.add(rectf);
           canvas.drawText(i.getTitle()+": "+ Math.round(i.getConfidence()*100) +"%",rectf.left+5,rectf.top+25,textpaint);
          Log.i("huahua","tianjiale"+i.toString());
          Log.i("huahua","kuodahou"+rectf.toString());
        }
      }
      for(RectF j:rect){
        canvas.drawRect(j,mPaint);
        Log.i("huahua","huale"+j.toString());
      }
      Log.i("huahua","bianliwanle");
    }
    ResultRecognitions.clear();
    rect.clear();
  }


}
