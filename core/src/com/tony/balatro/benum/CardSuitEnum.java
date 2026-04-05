package com.tony.balatro.benum;

/**
 *     Diamonds(0), // 方块
 *     Clubs(1),    // 梅花
 *     Hearts(2),   // 红桃
 *     Spades(3);   // 黑桃
 * */
public enum CardSuitEnum {
    Hearts(0),
    Clubs(1),
    Diamonds(2),
    Spades(3);
    private int resRowIndex;
    CardSuitEnum(int resRowIndex){
        this.resRowIndex = resRowIndex;
    }

    public int getResRowIndex() {
        return resRowIndex;
    }
}
