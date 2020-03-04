package com.qmai.android.newinstance_api;

import java.util.HashMap;
import java.util.Map;


public class ImplLoader {
    private static Map<String, Class<?>> implMap = new HashMap<>();

    public static void registerImpl(String path, Class<?> clz) {
        implMap.put(path, clz);
    }


    public static void init() {
        try {
            Class.forName("com.qmai.getinstance.loaderhelper.NewInstanceHelper")
                    .getMethod("init")
                    .invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T getIntance(String path) {
        Class rtnClazz = implMap.get(path);

        try {
            return (T) rtnClazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
