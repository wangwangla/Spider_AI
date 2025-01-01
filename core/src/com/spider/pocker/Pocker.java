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
    //桌上套牌
    private Array<Array<Card>> desk = new Array<Array<Card>>();//0为最里面
    //发牌区
    Array<Array<Card>> corner = new Array<Array<Card>>();//0为最里面
    //已完成套牌
    Array<Array<Card>> finished = new Array<Array<Card>>();
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

    public boolean isFinish(){
        return finished.size >= 8;
    }

    public Pocker copyInstance(){
        Pocker pocker = new Pocker();


        pocker.seed = this.seed;//种子
        pocker.suitNum = this.suitNum;//花色
        pocker.score = this.score;//分数
        pocker.operation = this.operation;//操作次数
        //桌上套牌
        pocker.desk = new Array<Array<Card>>(this.desk.size);//0为最里面
        for (int i = 0; i < desk.size; i++) {
            pocker.desk.add(new Array<>());
            for (int i1 = 0; i1 < desk.get(i).size; i1++) {
                pocker.desk.get(i).add(desk.get(i).get(i1).cloneInstance());
            }
        }
        //发牌区
        pocker.corner = new Array<Array<Card>>(this.corner.size);//0为最里面
        for (int i = 0; i < corner.size; i++) {
            pocker.corner.add(new Array<>());
            for (int i1 = 0; i1 < corner.get(i).size; i1++) {
                pocker.corner.get(i).add(corner.get(i).get(i1).cloneInstance());
            }
        }
        //已完成套牌
        pocker.finished = new Array<Array<Card>>(this.finished);
        for (int i = 0; i < finished.size; i++) {
            pocker.finished.add(new Array<>());
            for (int i1 = 0; i1 < finished.get(i).size; i1++) {
                pocker.finished.get(i).add(finished.get(i).get(i1).cloneInstance());
            }
        }

        return pocker;
    }

    //返回当前局面的评估值
    public int GetValue() {
        //每组完成牌200
        int value = finished.size * 200;
        //遍历桌牌
        for (Array<Card> cards : desk) {
            if (cards.size == 0) {
                value += 0;
            } else {
                //一摞牌且非空
                //eg. num=4, topPoint=10, value+=40
                int num = 0;
                Card pTop = cards.get(cards.size-1);
                Card pDown = cards.get(cards.size-1);
                //从下数第2张到顶部
                for (int i = cards.size - 2; i >= 0; --i) {
                    //已经不显示了则跳出
                    if (cards.get(i).isShow() == false)
                        break;

                    pTop = cards.get(i);
                    pDown = cards.get(i + 1);

                    //点数相差1
                    if (pTop.getPoint() == pDown.getPoint() + 1) {
                        //花色相同
                        if (pTop.getSuit() == pDown.getSuit()) {
                            num++;
                        } else//花色不同
                        {
//                            //储存之前的序列
//                            AddValue(num, pDown->point);
                            value += num * pDown.getPoint();

                            //花色不同，点数差1的分值
//                            AddValue(1,1);
                            value += 1;
                            num = 0;
                        }
                    } else {
                        value += num * pDown.getPoint();
                        num = 0;
                        //一个乱序组
                        //eg. 7 1 -> -7
                        //eg. 1 7 -> -14
                        //eg. 5 5 -> -10
                        int dv = -Math.max(pTop.getPoint(), pDown.getPoint());
                        if (pTop.getPoint() < pDown.getPoint()) {
                            dv *= 2;
                        }
                        value += 1 * dv;
                    }
                }
                value += num * pTop.getPoint();
            }
            //hide card
            int num = 10;
            //没有翻开的牌分值：-10, -9, -8 ...
            for (Card card : cards) {
                if (card.isShow() == false) {
                    value -= num;
                    num--;
                } else
                    break;
            }
        }
        return value;
    }

}