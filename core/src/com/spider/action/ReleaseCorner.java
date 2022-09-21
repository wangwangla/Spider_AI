package com.spider.action;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.spider.action.Action;
import com.spider.card.Card;
import com.spider.pocker.Pocker;
import com.spider.restore.Restore;

public class ReleaseCorner extends Action {
    private Restore restored;
    private boolean success;
    private Pocker poker;
    private Group sendCardGroup;
    private Group cardGroup;

    public ReleaseCorner(Group sendCardGroup,Group cardGroup) {
        this(false,sendCardGroup,cardGroup);
    }

    public ReleaseCorner(boolean b,Group sendCardGroup,Group cardGroup){
        this.success = b;
        this.cardGroup = cardGroup;
        this.sendCardGroup = sendCardGroup;
    }

    //释放一摞右下角，检查收牌情况
    public boolean doAction(Pocker inpoker, Group cardGroup) {
        System.out.println("-------------------------------------");
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
        //遍历一摞待发区牌
        for (int i = 0; i < 10; ++i) {
            //待发区亮牌
            Array<Card> cards = corner.get(corner.size - 1);
            cards.get(i).setShow(true);
            //逐个堆叠加上
            poker.getDesk().get(i).add(cards.get(i));
            Card card = cards.get(i);
            Group parent = card.getParent();
            Vector2 vector2 = new Vector2(0,0);
            parent.localToStageCoordinates(vector2);
            cardGroup.stageToLocalCoordinates(vector2);
            cards.get(i).setPosition(vector2.x,vector2.y);
            cardGroup.addActor(cards.get(i));
        }
        //去掉一摞待发区
        corner.removeIndex(corner.size - 1);
        success = true;

        poker.setScore(poker.getScore() - 1);
        poker.setOperation(poker.getOperation() + 1);

        //进行回收
        restored = new Restore();
        if (restored.doAction(poker) == false)
            restored = null;
        return true;
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
        //遍历一摞待发区牌
        for (int i = 0; i < 10; ++i) {
            //待发区亮牌
            Array<Card> cards = corner.get(corner.size - 1);
            cards.get(i).setShow(true);
            //逐个堆叠加上
            poker.getDesk().get(i).add(cards.get(i));
            Card card = cards.get(i);
            if (cardGroup!=null) {
                Group parent = card.getParent();
                Vector2 vector2 = new Vector2(0, 0);
                parent.localToStageCoordinates(vector2);
                cardGroup.stageToLocalCoordinates(vector2);
                cards.get(i).setPosition(vector2.x, vector2.y);
                cardGroup.addActor(cards.get(i));
            }
        }
        //去掉一摞待发区
        corner.removeIndex(corner.size - 1);
        success = true;

        poker.setScore(poker.getScore() - 1);
        poker.setOperation(poker.getOperation() + 1);

        //进行回收
        restored = new Restore();
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
            cards.get(cards.size - 1).setShow(false);
            Card card1 = cards.get(cards.size - 1);
            if (sendCardGroup!=null) {
                vector2.set(card1.getX(), card1.getY());
                card1.getParent().localToStageCoordinates(vector2);
                sendCardGroup.stageToLocalCoordinates(vector2);
                card1.setPosition(vector2.x, vector2.y);
                //回收
                temp.add(cards.get(cards.size - 1));
                //从桌上取掉
                sendCardGroup.addActor(cards.get(cards.size - 1));
            }
            cards.removeIndex(cards.size - 1);
            if (cards.size>0) {
                Card card = cards.get(cards.size - 1);
                card.setShow(true);
            }
        }
        poker.getCorner().add(temp);
        redoAnimation();
        return true;
    }

    public void startAnimation() {
        assert (poker.isHasGUI());
        assert (success);
        //如果发生了回收事件，先恢复到回收前
        if (restored != null)
            restored.redo(poker);

        for (int i = 0; i < poker.getDesk().size; i++) {
            Array<Card> cards =poker.getDesk().get(i);
            if (cards.size>1) {
                Card card = cards.get(cards.size - 2);
                Card card1 = cards.get(cards.size - 1);
//                card1.addAction(Actions.moveTo(card.getX(),card.getY()-20,1));
                card1.setPosition(card.getX(),card.getY()-20,1);
            }else {
                Card card = cards.get(cards.size - 1);
//                card.addAction(Actions.moveTo(1,1,1));
                card.setPosition(1,1);
            }
        }
    }

    public void redoAnimation() {
//        assert (poker.isHasGUI());
//        Array<Array<Card>> corner = poker.getCorner();
//        Array<Card> array = corner.get(corner.size - 1);
//        for (Card card : array) {
//            card.addAction(Actions.moveTo(0,0,1));
//        }

    }
}
