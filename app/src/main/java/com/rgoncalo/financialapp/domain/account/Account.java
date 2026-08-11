package com.rgoncalo.financialapp.domain.account;

public class Account {

    private String name;

    public Account(String new_name){
        name = new_name;
    }

    public String getName(){
        return name;
    }

}