package com.utils;

public class CardModel {
    final int code;
    final int suit; // 1 spade, 2 heart, 3 diamond, 4 club
    final int rank; // 1..13
    boolean faceUp;

    public CardModel(int code, int suit, int rank, boolean faceUp) {
        this.code = code;
        this.suit = suit;
        this.rank = rank;
        this.faceUp = faceUp;
    }

    public int getCode() {
        return code;
    }

    public int getSuit() {
        return suit;
    }

    public int getRank() {
        return rank;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
    }
}

