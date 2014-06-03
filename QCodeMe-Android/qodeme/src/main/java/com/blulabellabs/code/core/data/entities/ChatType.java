package com.blulabellabs.code.core.data.entities;

/**
 * Created by Alex on 8/20/13.
 */
public enum ChatType {
    ONE2ONE(0),
    PRIVATE_GROUP(1),
    PUBLIC_GROUP(2);

    private final int value;

    private ChatType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public ChatType parse(int value){
        switch (value){
            case 0: return ONE2ONE;
            case 1: return PRIVATE_GROUP;
            case 2: return PUBLIC_GROUP;
            default: return null;
        }
    }


}
