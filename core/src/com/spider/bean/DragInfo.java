package com.spider.bean;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.spider.card.Card;

public class DragInfo {
    private boolean bOnDrag;
    int orig = -1;
    int cardIndex = -1;
    int num = -1;
    Array<ArrayMap<Card, Vector2>> vecCard = new Array<ArrayMap<Card, Vector2>>();
    public DragInfo(){
    }

    public boolean isbOnDrag() {
        return bOnDrag;
    }

    public void setbOnDrag(boolean bOnDrag) {
        this.bOnDrag = bOnDrag;
    }

    public int getOrig() {
        return orig;
    }

    public void setOrig(int orig) {
        this.orig = orig;
    }

    public int getCardIndex() {
        return cardIndex;
    }

    public void setCardIndex(int cardIndex) {
        this.cardIndex = cardIndex;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public Array<ArrayMap<Card, Vector2>> getVecCard() {
        return vecCard;
    }

    public void setVecCard(Array<ArrayMap<Card, Vector2>> vecCard) {
        this.vecCard = vecCard;
    }
}
