package com.bean;

import com.utils.CardModel;

public class TouchUpBean {
    private CardModel cardModel;
    private float distance;
    private int targetStackIndex;
    public CardModel getCardActor() {
        return cardModel;
    }

    public void setCardActor(CardModel cardActor) {
        this.cardModel = cardActor;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public CardModel getCardModel() {
        return cardModel;
    }

    public void setCardModel(CardModel cardModel) {
        this.cardModel = cardModel;
    }

    public int getTargetStackIndex() {
        return targetStackIndex;
    }

    public void setTargetStackIndex(int targetStackIndex) {
        this.targetStackIndex = targetStackIndex;
    }
}
