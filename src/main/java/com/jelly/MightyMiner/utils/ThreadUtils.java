package com.jelly.MightyMiner.utils;

public class ThreadUtils {
    public static void sleep(int time){
        try{
            Thread.sleep(time);
        } catch (InterruptedException ignored){
        }
    }
}
