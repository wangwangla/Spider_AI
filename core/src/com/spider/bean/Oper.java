package com.spider.bean;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Oper {
    private int origDeskIndex;//回收源堆叠序号
    private boolean shownLastCard;//回收后底牌是否翻出
    private Vector2 ptEnd;

    public Oper(){}
    public int getOrigDeskIndex() {
        return origDeskIndex;
    }

    public void setOrigDeskIndex(int origDeskIndex) {
        this.origDeskIndex = origDeskIndex;
    }

    public boolean isShownLastCard() {
        return shownLastCard;
    }

    public void setShownLastCard(boolean shownLastCard) {
        this.shownLastCard = shownLastCard;
    }


    public Vector2 getPtEnd() {
        return ptEnd;
    }

    public void setPtEnd(Vector2 ptEnd) {
        this.ptEnd = ptEnd;
    }
}
