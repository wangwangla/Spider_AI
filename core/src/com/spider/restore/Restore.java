package com.spider.restore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.spider.action.Action;
import com.spider.bean.Oper;
import com.spider.card.Card;
import com.spider.constant.Constant;
import com.spider.log.NLog;
import com.spider.pocker.Pocker;

public class Restore extends Action {
    private Group finished;
    private Group cardGroup;
    private Array<Card> array1;
    private Pocker poker;
    public Restore(Group finished,Group cardGroup){
        this.finished = finished;
        this.cardGroup = cardGroup;
    }

    //已回收成功的操作
    private Array<Oper> vecOper = new Array<Oper>();
    //若所有堆叠有可回收的情况则回收
    //若对应堆叠能回收则回收

        //返回对应堆叠能否回收
    public boolean canRestore(Pocker poker, int deskNum) {
        if (poker.getDesk().get(deskNum).size<=0)
            return false;
        int pos = poker.getDesk().get(deskNum).size - 1;
        int suit = poker.getDesk().get(deskNum).get(pos).getSuit();
        //i是点数
        for (int i = 1; i <= 13; ++i) {
            //从最后一张牌开始，点数升序，花色一致 则可以回收
            if (pos >= 0 && poker.getDesk().get(deskNum).get(pos).getPoint() == i
                    && poker.getDesk().get(deskNum).get(pos).getSuit() == suit) {
                pos--;
                continue;
            }
            else
                return false;
        }
        return true;
    }

    boolean doRestore(Pocker poker,int deskNum) {
        if (canRestore(poker,deskNum)) {
            final Oper oper = new Oper();
            oper.setOrigDeskIndex(deskNum);
            //进行回收
            //加入套牌，从最低下一张倒数13张，所以顺序为1-13
            final Array<Card> array = poker.getDesk().get(deskNum);
            array1 = new Array<Card>();
            for (int i = 0; i < 13; i++) {
                array1.add(array.get(array.size-1-i));
            }

            poker.getFinished().add(array1);
            //预存起点位置

            for (Card card : array) {
                oper.getVecStartPt().add(card.getPosition());
            }

            //去掉牌堆叠的13张
            for (Card card : array1) {
                array.removeValue(card,true);
                NLog.e("huishou : %s",card.getPoint());
            }

            if (!(array.size<=0) && array.get(array.size-1).isShow() == false) {
                array.get(array.size-1).setShow(true);
                oper.setShownLastCard(true);
            } else {
                oper.setShownLastCard(false);
            }
            poker.setScore(poker.getScore()+100);
            vecOper.add(oper);
            return true;
        }
        return false;
    }



    public boolean doAction(Pocker inpoker) {
        poker = inpoker;
        if (vecOper.size<=0) {
            //未指定回收组号
            //扫描每个堆叠寻找能回收的组
            for (int i = 0; i < poker.getDesk().size; ++i) {
                doRestore(poker, i);
            }
        } else {
            //已指定回收组号
            int deskIndex = vecOper.get(0).getOrigDeskIndex();
            vecOper.clear();
            doRestore(poker,deskIndex);
        }
        return !(vecOper.size<=0);
    }

    public void  startAnimation() {
        /**
         * 改变位置   播放动画
         *
         */
        Vector2 vector2 = new Vector2(0,0);
        Array<Array<Card>> finished1 = poker.getFinished();
        int size = finished1.size;
        vector2.x = size*10;
        //最终位置
        array1.reverse();
        finished.toFront();
        for (Card card : array1) {
            Vector2 temp = new Vector2(card.getX(Align.center),card.getY(Align.center));
            card.getParent().localToStageCoordinates(temp);
            finished.stageToLocalCoordinates(temp);
            finished.addActor(card);
//            card.setPosition(0,0);
            card.clearActions();
            card.setPosition(temp.x,temp.y,Align.center);
            card.addAction(Actions.moveTo(vector2.x,0,1));
        }
    }


    public boolean redo(Pocker inpoker) {
        super.redo(inpoker);
        assert(vecOper.size<=0);
        poker = inpoker;
        for (Oper it : vecOper) {
            poker.setScore(poker.getScore()-100);
            //如果翻过牌则翻回去
            if (it.isShownLastCard()) {
                Array<Card> array = poker.getDesk().get(it.getOrigDeskIndex());
                array.get(array.size - 1).setShow(false);
            }

            //把完成的牌放回堆叠
            Array<Array<Card>> finished1 = poker.getFinished();
            Array<Card> it1 = finished1.get(finished1.size-1);
            float v = Constant.worldWidth / 10.0F;
            Array<Card> array = poker.getDesk().get(it.getOrigDeskIndex());
            float baseY = array.size>0 ? array.get(array.size-1).getY() : 0;
            for (int i = 0; i < it1.size; i++) {
                Card card = it1.get(it1.size - 1-i);
                array.add(card);
//                if (cardGroup!=null) {
//                    Vector2 vector2 = new Vector2();
//                    vector2.set(card.getX(), card.getY());
//                    card.getParent().localToStageCoordinates(vector2);
//                    cardGroup.stageToLocalCoordinates(vector2);
//                    card.setPosition(vector2.x, vector2.y);
////                card.addAction(Actions.moveTo(array));
//                    cardGroup.addActor(card);
////                    card.addAction(Actions.moveTo(it.getOrigDeskIndex()*v,baseY,1F));
//                    card.setPosition(it.getOrigDeskIndex()*v,baseY);
//
//                }
                baseY -= 40;
            }
            //完成的牌消掉
            Array<Array<Card>> finished = poker.getFinished();
            finished.removeIndex(finished.size-1);
        }
        vecOper.clear();
        return true;
    }

}
