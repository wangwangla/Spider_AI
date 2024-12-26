package com.spider.action;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spider.action.Action;
import com.spider.card.Card;
import com.spider.manager.GameManager;
import com.spider.pocker.Pocker;
import com.spider.restore.Restore;

public class ReleaseCorner extends Action {
    private Restore restored;
    private boolean success;
    private Pocker poker;
    private Group sendCardGroup;
    private Group cardGroup;
    private Group finishGroup;

    public ReleaseCorner(Group sendCardGroup, Group cardGroup,Group finishGroup) {
        this.cardGroup = cardGroup;
        this.sendCardGroup = sendCardGroup;
        this.finishGroup = finishGroup;
    }

    //释放一摞右下角，检查收牌情况
    public boolean doAction(Pocker inpoker) {
        poker = inpoker;
        //角落区没牌
        if (poker.getCorner().size <= 0) {
            return false;
        }
        //有空位不能发牌，但总牌数小于10张不受限制
        int sum = 0;
        boolean hasEmpty = false;
        for (Array<Card> cards : poker.getDesk()) {
            sum += cards.size;
            if (cards.size <= 0) {
                hasEmpty = true;
            }
        }
        if (hasEmpty && sum >= 10)
            return false;
        //取得角落区坐标
        Array<Array<Card>> corner = poker.getCorner();
        //待发区亮牌
        Array<Card> cards = corner.get(corner.size - 1);
        //遍历一摞待发区牌
        for (int i = 0; i < 10; ++i) {
            //逐个堆叠加上
            poker.getDesk().get(i).add(cards.get(i));
            Card card = cards.get(i);
            //检测是否不可以点击
            if (poker.getDesk().get(i).size>=2) {
                cards.get(i).setShow(true);
            }
            Vector2 vector2 = new Vector2(card.getX(Align.center),card.getY(Align.center));
            card.localToStageCoordinates(vector2);
            cardGroup.addActor(cards.get(i));
            card.stageToLocalCoordinates(vector2);
            cards.get(i).setPosition(vector2.x, vector2.y,Align.center);
        }
        //去掉一摞待发区
        corner.removeIndex(corner.size - 1);
        success = true;

        poker.setScore(poker.getScore() - 1);
        poker.setOperation(poker.getOperation() + 1);

        //进行回收
        restored = new Restore(finishGroup,cardGroup);
        restored.setUpdateGroup(updateGroup);
        if (restored.doAction(poker) == false)
            restored = null;
        return true;
    }

    public boolean redo(Pocker inpoker) {
        assert (success);
        poker = inpoker;
        //撤销回收
        if (restored != null) {
            restored.redo(poker);
        }
        poker.setScore(poker.getScore() + 1);
        poker.setOperation(poker.getOperation() - 1);
        Vector2 vector2 = new Vector2();
        //回收10张牌
        Array<Card> temp = new Array<Card>();
        for (int i = 0; i < 10; ++i) {
            //改为背面
            Array<Card> cards = poker.getDesk().get(i);
            Card card1 = cards.get(cards.size - 1);
            temp.add(cards.get(cards.size - 1));
            if (sendCardGroup!=null) {
                vector2.set(card1.getX(), card1.getY());
                card1.getParent().localToStageCoordinates(vector2);
                sendCardGroup.stageToLocalCoordinates(vector2);
                card1.setPosition(vector2.x,vector2.y);
                //从桌上取掉
                sendCardGroup.addActor(cards.get(cards.size - 1));
            }
            cards.removeIndex(cards.size - 1);
        }
        poker.getCorner().add(temp);
        return true;
    }

    public void startAnimation() {
        //如果发生了回收事件，先恢复到回收前
//        if (restored != null)
//            restored.redo(poker);
        Array<Image> vecImageEmpty = GameManager.vecImageEmpty;
        for (int i = 0; i < poker.getDesk().size; i++) {
            Array<Card> cards =poker.getDesk().get(i);
            if (cards.size>1) {
                Card card = cards.get(cards.size - 2);
                final Card card1 = cards.get(cards.size - 1);
                card1.addAction(Actions.delay(i*0.1F,Actions.moveTo(card.getX(),card.getY()-20,0.2F)));
            }else if (cards.size>0){
                Image image = vecImageEmpty.get(i);
                Card card = cards.get(cards.size - 1);
                card.addAction(Actions.delay(i*0.1F,Actions.moveTo(image.getX(),image.getY()-20,0.2F)));
                card.setPosition(1,1);
            }
        }
    }

    public void redoAnimation() {
        if (poker.getCorner().size<0) {
            throw new GdxRuntimeException("error ");
        }

        Array<Card> cards = poker.getCorner().get(poker.getCorner().size - 1);
        int index = 0;
        for (Card card : cards) {
            card.addAction(Actions.delay(index * 0.1F,Actions.moveTo(0, 0,0.1F)));
            card.setShowDelay(false,index);
            index ++;
        }
    }
}
