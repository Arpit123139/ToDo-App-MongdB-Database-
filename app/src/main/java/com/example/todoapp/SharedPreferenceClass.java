package com.example.todoapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceClass {
    private static final String USER_PREF="user_todo";
    private SharedPreferences appShared;
    //to get the data and delte and update the data
    private SharedPreferences.Editor prefsEditor;


    public SharedPreferenceClass(Context context){
        appShared=context.getSharedPreferences(USER_PREF, Activity.MODE_PRIVATE);
        this.prefsEditor= appShared.edit();
    }

    /************************************************FOR THE INT DATA*********************************************************/
    //To get the data and update it we take getter Setter

    public int getValue_int(String key){
        return appShared.getInt(key,0);
    }

    public void setValue_int(String key,int value){
        prefsEditor.putInt(key,value).commit();
    }

    /************************************************ FOR THE STRING DATA*****************************************************/
    //To get the data and update it we take getter Setter
    public String getValue_string(String key){
        return appShared.getString(key,"");
    }

    public void setValue_string(String key,String value){
        prefsEditor.putString(key,value).commit();
    }

    /************************************************ FOR THE Boolean DATA*****************************************************/
    //To get the data and update it we take getter Setter
    public Boolean getValue_boolean(String key){
        return appShared.getBoolean(key,false);
    }

    public void setValue_boolean(String key,boolean value){
        prefsEditor.putBoolean(key,value).commit();
    }

    /***************************************************TO CLEAR DATA*****************************************************/
    public void clear(){
        prefsEditor.clear().commit();
    }



}
