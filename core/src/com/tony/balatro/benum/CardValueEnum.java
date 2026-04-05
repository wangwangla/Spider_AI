package com.tony.balatro.benum;

public enum CardValueEnum {
    TWO(2,0),
    THREE(3, 1),
    FOUR(4, 2),
    FIVE(5, 3),
    SIX(6, 4),
    SEVEN(7, 5),
    EIGHT(8, 6),
    NINE(9, 7),
    TEN(10, 8),
    JACK(11, 9),
    QUEEN(12, 10),
    KING(13, 11),
    ACE(14, 12);

    private int value;
    private int resIndex;
    CardValueEnum(int value,int resIndex){
        this.value = value;
        this.resIndex = resIndex;
    }

    public int getResIndex() {
        return resIndex;
    }

    public int getValue() {
        return value;
    }
}
