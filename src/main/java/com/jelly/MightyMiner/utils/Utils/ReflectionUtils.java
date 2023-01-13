package com.jelly.MightyMiner.utils.Utils;

import java.lang.reflect.Method;

public class ReflectionUtils {
    public static boolean invoke(Object object, String methodName) {
        try {
            final Method method = object.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(object);
            return true;
        } catch(Exception ignored) {

        }
        return false;
    }
}
