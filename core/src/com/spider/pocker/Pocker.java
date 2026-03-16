package com.spider.pocker;

import com.badlogic.gdx.utils.Array;
import com.spider.model.CardModel;

/**
 * 纯数据的牌局状态，用于驱动游戏逻辑与求解器。
 */
public class Pocker {
    private int seed;//种子
    private int suitNum;//花色数
    private int score;//得分
    private int operation;//操作次数
    private String dealString; // solver deal text for integration

    //桌面10列牌堆，0 为最左
    private final Array<Array<CardModel>> desk = new Array<Array<CardModel>>();
    //待发牌堆（5 叠，每叠 10 张）
    final Array<Array<CardModel>> corner = new Array<Array<CardModel>>();
    //已完成的 13 张顺子堆
    final Array<Array<CardModel>> finished = new Array<Array<CardModel>>();

    public Pocker() {
        seed = -1;
        suitNum = -1;
        score = -1;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public int getSuitNum() {
        return suitNum;
    }

    public void setSuitNum(int suitNum) {
        this.suitNum = suitNum;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public String getDealString() {
        return dealString;
    }

    public void setDealString(String dealString) {
        this.dealString = dealString;
    }

    public Array<Array<CardModel>> getDesk() {
        return desk;
    }

    public Array<Array<CardModel>> getCorner() {
        return corner;
    }

    public Array<Array<CardModel>> getFinished() {
        return finished;
    }

    public void minusOne() {
        score--;
    }

    public void addOperation() {
        operation++;
    }
}
