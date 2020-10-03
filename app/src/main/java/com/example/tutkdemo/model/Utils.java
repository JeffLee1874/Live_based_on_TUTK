package com.example.tutkdemo.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Utils {

    public static boolean saveUID(Context context, HashSet<String> hashSet) {

        SharedPreferences sp = context.getSharedPreferences("UID", Context.MODE_PRIVATE);//数据自己可用
        SharedPreferences.Editor edit = sp.edit();

        edit.putStringSet("UID", hashSet);
        edit.commit();
        return true;//存储成功
    }

    public static Set<String> getUID(Context context) {
        SharedPreferences sp = context.getSharedPreferences("UID", Context.MODE_PRIVATE);
        Set<String> hashSet = sp.getStringSet("UID", null);

        return hashSet;
    }

    public static boolean saveUserInfo(Context context, String UID, HashSet<String> hashSet) {

        SharedPreferences sp = context.getSharedPreferences("inform", Context.MODE_PRIVATE);//数据自己可用
        SharedPreferences.Editor edit = sp.edit();

        edit.putStringSet(UID, hashSet);
        edit.commit();
        return true;//存储成功
    }

    // 从inform.xml中获取账号和密码
    public static Map<String, String> getUserInfo(Context context, String UID) {
        SharedPreferences sp = context.getSharedPreferences("inform", Context.MODE_PRIVATE);

        Set<String> hashSet = sp.getStringSet(UID, null);
        Map<String,String> userMap = new HashMap<String, String>();

        int count = 0;
        for(String i : hashSet)
        {
            if(count == 0)
                userMap.put("UID", i);
            else if(count == 1)
                userMap.put("password", i);
            else
                userMap.put("username", i);

            count++;
        }


        return userMap;
    }


    public static boolean saveDetail(Context context,String UID, HashSet<String> hashSet) {

        SharedPreferences sp = context.getSharedPreferences("detail", Context.MODE_PRIVATE);//数据自己可用
        SharedPreferences.Editor edit = sp.edit();

        if(hashSet!=null)
        {
            for(String i : hashSet)
                Log.i("储存", "+" + i);
        }
        edit.putStringSet(UID, hashSet);
        edit.commit();
        return true;//存储成功
    }

    public static Map<String, String> getDetail(Context context, String UID) {
        SharedPreferences sp = context.getSharedPreferences("detail", Context.MODE_PRIVATE);

        Set<String> hashSet = sp.getStringSet(UID, null);
        Map<String,String> detailMap = new HashMap<String, String>();

        int count = 0;
        if(hashSet!=null)
        {
            for(String i : hashSet)
            {
                if(count == 0)
                    detailMap.put("location", i);
                else
                    detailMap.put("usage", i);
                count++;
            }
        }

        return detailMap;
    }
}
