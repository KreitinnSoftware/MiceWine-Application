/*
author = McFly 
*/
package com.micewine.emu.nativeLoader;

//use essa clase para chamar as funcoes nativas ok!


public class NativeLoader {

    static {
        System.loadLibrary("micewine");
    }


    //exec commands in written in c++ in native code this function wait string comand to execute in side c++ language 
    public native String shellExecCmd(String comand);


}
