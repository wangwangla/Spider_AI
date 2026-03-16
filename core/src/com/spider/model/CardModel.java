package com.spider.model;

/**
 * Data-only representation of a card. Keeps game logic independent from UI actors.
 */
public class CardModel {
    private final int suit;   // 1=club, 2=diamond, 3=heart, 4=spade
    private final int point;  // 1-13
    private boolean faceUp;

    public CardModel(int suit, int point) {
        this(suit, point, false);
    }

    public CardModel(int suit, int point, boolean faceUp) {
        this.suit = suit;
        this.point = point;
        this.faceUp = faceUp;
    }

    public int getSuit() {
        return suit;
    }

    public int getPoint() {
        return point;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
    }

    @Override
    public String toString() {
        return (faceUp ? "" : "[") + suit + ":" + point + (faceUp ? "" : "]");
    }
}
