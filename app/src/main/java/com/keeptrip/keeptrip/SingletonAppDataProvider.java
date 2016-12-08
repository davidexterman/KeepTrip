package com.keeptrip.keeptrip;

public class SingletonAppDataProvider {
    private static AppDataProvider mInstance = null;

    public static void init() {
        mInstance = new SqlLiteAppDataProvider();
        mInstance.initialize();
    }

    public static AppDataProvider getInstance(){
        // todo: more research maybe syncornaycze

        return mInstance;
    }
}
