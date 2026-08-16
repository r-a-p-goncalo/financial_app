package com.rgoncalo.financialapp.utils;

public class StringUtils {

    public static  void printIfNotNull(String prefix, Object toPrint){

        if(toPrint != null)
            System.out.println(prefix + toPrint.toString());

    }

}
