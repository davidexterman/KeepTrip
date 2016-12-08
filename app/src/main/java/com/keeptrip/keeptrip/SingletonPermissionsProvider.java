package com.keeptrip.keeptrip;

/**
 * Created by david on 12/8/2016.
 */

public class SingletonPermissionsProvider {
    private static PermissionsProvider mInstance = null;

    public static void init() {
        mInstance = new PermissionsProvider();
    }

    public static PermissionsProvider getInstance(){
        // todo: more research maybe synchronize

        return mInstance;
    }
}
