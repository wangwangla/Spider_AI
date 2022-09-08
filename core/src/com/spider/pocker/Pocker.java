package com.spider.pocker;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.spider.card.Card;

import java.util.HashMap;
import java.util.Objects;

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
    private boolean hasGUI;//已加载图片
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

    public boolean isHasGUI() {
        return hasGUI;
    }

    public void setHasGUI(boolean hasGUI) {
        this.hasGUI = hasGUI;
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

    public Pocker() {
        seed = -1;
        suitNum = -1;
        score = -1;
        hasGUI = false;
    }

    public Pocker(Pocker pocker){
        this.seed = pocker.getSeed();//种子
        this.suitNum = pocker.getSuitNum();//花色
        this.score = pocker.getScore();//分数
        this.operation = pocker.getOperation();//操作次数
        this.hasGUI = pocker.hasGUI;//已加载图片
        //桌上套牌
        this.desk = new Array<Array<Card>>();//0为最里面
        for (Array<Card> cards : pocker.desk) {
            Array<Card> array = new Array<Card>();
            for (Card card : cards) {
                array.add(new Card(card));
            }
            desk.add(array);
        }
//        //发牌区
        this.corner = new Array<Array<Card>>();//0为最里面
        for (Array<Card> cards : pocker.corner) {
            Array<Card> array = new Array<Card>();
            for (Card card : cards) {
                array.add(new Card(card));
            }
            corner.add(cards);
        }
//        //已完成套牌
        this.finished = new Array<Array<Card>>();
        for (Array<Card> cards : pocker.finished) {
            Array<Card> cards1 = new Array<Card>();
            finished.add(cards1);
            for (Card card : cards) {
                cards1.add(new Card(card));
            }
        }
    }

    //通过检测 已完成==8 返回是否已完成
    public boolean isFinished() {
        return finished.size == 8;
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

    public void minusOne() {
        score--;
    }

    public void addOperation() {
        operation++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pocker pocker = (Pocker) o;
        return seed == pocker.seed && suitNum == pocker.suitNum &&
                score == pocker.score && operation == pocker.operation &&
                hasGUI == pocker.hasGUI &&
                checkArray(desk, pocker.desk) &&
                checkArray(corner, pocker.corner) &&
                checkArray(finished, pocker.finished);
    }

    public boolean checkArray(Array<Array<Card>> array, Array<Array<Card>> array1){
        if (array == null && array1 == null)return true;
        if (array.size!=array1.size) {
            return false;
        }
        for (int i = 0; i < array.size; i++) {
            Array<Card> array2 = array.get(i);
            Array<Card> array3 = array1.get(i);
            if (array2.size != array3.size)return false;
            for (int i1 = 0; i1 < array2.size; i1++) {
                Card card = array2.get(i1);
                Card card1 = array3.get(i1);
                if (!card.equals(card1)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seed, suitNum, score, operation, hasGUI, desk, corner, finished);
    }

    public int sss(){
        int ret = 0;
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (Array<Card> cards : desk) {
            for (Card card : cards) {
                builder.append(card.toString());
                index ++;
            }
            ret^=builder.toString().hashCode();
            builder.setLength(0);
        }
        for (Array<Card> cards : corner) {
            for (Card card : cards) {
                builder.append(card.toString());
            }
            ret^=builder.toString().hashCode();
            builder.setLength(0);
        }
        for (Array<Card> cards : finished) {
            for (Card card : cards) {
                builder.append(card.toString());
            }
            ret^=builder.toString().hashCode();
            builder.setLength(0);
        }
        ret^=index;
        return ret;
    }
}