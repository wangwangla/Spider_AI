package com.bean;

import com.badlogic.gdx.scenes.scene2d.Actor;

public class TouchInfo {
    private int stackIndex;
    private int cardIndex;

    public int getStackIndex() {
        return stackIndex;
    }

    public void setStackIndex(int stackIndex) {
        this.stackIndex = stackIndex;
    }

    public int getCardIndex() {
        return cardIndex;
    }

    public void setCardIndex(int cardIndex) {
        this.cardIndex = cardIndex;
    }

    private Actor target;
    public void setTouchTarget(Actor target) {
        this.target = target;
    }

    public Actor getTarget() {
        return target;
    }
}
