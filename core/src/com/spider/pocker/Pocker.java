package com.spider.pocker;

import com.badlogic.gdx.utils.Array;
import com.spider.card.Card;

/**
桌面上共10摞牌，前4摞6张，后6摞5张，共6*4+5*6=54张
角落共5叠，每叠10张，共10*5=50张
总计104张，104=13*8，为两套牌去掉大小王得到
heart 红桃
spade 黑桃
club 梅花
diamond 方块
*/
public class Pocker {
    private int seed;//种子
    private int suitNum;//花色
    private int score;//分数
    private int operation;//操作次数
    public Pocker() {
        seed = -1;
        suitNum = -1;
        score = -1;
    }

    //桌上套牌
    private Array<Array<Card>> desk = new Array<Array<Card>>();//0为最里面
    //发牌区
    Array<Array<Card>> corner = new Array<Array<Card>>();//0为最里面
    //已完成套牌
    Array<Array<Card>> finished = new Array<Array<Card>>();

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

    public Array<Array<Card>> getDesk() {
        return desk;
    }

    public Array<Array<Card>> getCorner() {
        return corner;
    }

    public Array<Array<Card>> getFinished() {
        return finished;
    }

    public void minusOne() {
        score--;
    }

    public void addOperation() {
        operation++;
    }
}