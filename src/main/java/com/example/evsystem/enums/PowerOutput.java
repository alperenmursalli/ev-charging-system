package com.example.evsystem.enums;

public enum PowerOutput {

    KW_22(22),
    KW_50(50),
    KW_150(150);

    private final int value;

    PowerOutput(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}