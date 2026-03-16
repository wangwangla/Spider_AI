package com.spider.action.restore;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.spider.action.Action;
import com.spider.bean.Oper;
import com.spider.card.Card;
import com.spider.card.CardViewProvider;
import com.spider.log.NLog;
import com.spider.manager.GameManager;
import com.spider.model.CardModel;
import com.spider.pocker.Pocker;

public class Restore extends Action {
    private final Group finished;
    private final Group cardGroup;
    private Array<CardModel> array1;
    private Pocker poker;
    private final CardViewProvider viewProvider;

    public Restore(Group finished, Group cardGroup, CardViewProvider viewProvider){
        this.finished = finished;
        this.cardGroup = cardGroup;
        this.viewProvider = viewProvider;
    }

    //已回收成功的操作
    private final Array<Oper> vecOper = new Array<Oper>();

    public boolean canRestore(Pocker poker, int deskNum) {
        if (poker.getDesk().get(deskNum).size<=0)
            return false;
        int pos = poker.getDesk().get(deskNum).size - 1;
        int suit = poker.getDesk().get(deskNum).get(pos).getSuit();
        for (int i = 1; i <= 13; ++i) {
            if (pos >= 0 && poker.getDesk().get(deskNum).get(pos).getPoint() == i
                    && poker.getDesk().get(deskNum).get(pos).getSuit() == suit) {
                pos--;
            } else {
                return false;
            }
        }
        return true;
    }

    boolean doRestore(Pocker poker,int deskNum) {
        if (canRestore(poker,deskNum)) {
            final Oper oper = new Oper();
            oper.setOrigDeskIndex(deskNum);
            //回收
            Array<CardModel> array = poker.getDesk().get(deskNum);
            array1 = new Array<CardModel>();
            for (int i = 0; i < 13; i++) {
                array1.add(array.get(array.size-1-i));
            }
            poker.getFinished().add(array1);
            for (CardModel cardModel : array) {
                Card card = viewProvider.viewOf(cardModel);
                if (card != null) {
                    oper.getVecStartPt().add(card.getPosition());
                }
            }
            for (CardModel cardModel : array1) {
                array.removeValue(cardModel,true);
                NLog.e("huishou : %s",cardModel.getPoint());
            }
            if (array.size>0 && !array.get(array.size-1).isFaceUp()) {
                array.get(array.size-1).setFaceUp(true);
                Card view = viewProvider.viewOf(array.get(array.size-1));
                if (view != null) {
                    view.setShow(true);
                }
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
            for (int i = 0; i < poker.getDesk().size; ++i) {
                doRestore(poker, i);
            }
        } else {
            int deskIndex = vecOper.get(0).getOrigDeskIndex();
            vecOper.clear();
            doRestore(poker,deskIndex);
        }
        return !(vecOper.size<=0);
    }

    public void  startAnimation() {
        Vector2 vector2 = new Vector2(0,0);
        Array<Array<CardModel>> finished1 = poker.getFinished();
        int size = finished1.size;
        vector2.x = size*10;
        array1.reverse();
        finished.toFront();
        for (CardModel cardModel : array1) {
            Card card = viewProvider.viewOf(cardModel);
            if (card == null) continue;
            Vector2 temp = new Vector2(card.getX(Align.center),card.getY(Align.center));
            card.getParent().localToStageCoordinates(temp);
            finished.stageToLocalCoordinates(temp);
            finished.addActor(card);
            card.clearActions();
            card.setPosition(temp.x,temp.y,Align.center);
            card.addAction(Actions.moveTo(vector2.x,0,0.1f));
        }
    }

    public boolean redo(Pocker inpoker) {
        super.redo(inpoker);
        poker = inpoker;
        for (Oper it : vecOper) {
            poker.setScore(poker.getScore()-100);
            if (it.isShownLastCard()) {
                Array<CardModel> array = poker.getDesk().get(it.getOrigDeskIndex());
                array.get(array.size - 1).setFaceUp(false);
                Card view = viewProvider.viewOf(array.get(array.size - 1));
                if (view != null) {
                    view.setShow(false);
                }
            }

            Array<Array<CardModel>> finished1 = poker.getFinished();
            Array<CardModel> it1 = finished1.get(finished1.size-1);
            Array<CardModel> cards = poker.getDesk().get(it.getOrigDeskIndex());
            Array<Image> vecImageEmpty = GameManager.vecImageEmpty;
            Image image = vecImageEmpty.get(it.getOrigDeskIndex());
            for (int i = 0; i < it1.size; i++) {
                CardModel cardModel = it1.get(i);
                Card card = viewProvider.viewOf(cardModel);
                if (card == null) continue;
                Vector2 temp = new Vector2();
                temp.set(card.getX(Align.center),card.getY(Align.center));
                card.getParent().localToStageCoordinates(temp);
                cardGroup.stageToLocalCoordinates(temp);
                card.setPosition(temp.x, temp.y,Align.center);

                cards.add(cardModel);
                cardGroup.addActor(card);
                float targetY = GameManager.stackY(cards.size -1);
                card.addAction(Actions.moveTo(image.getX(), targetY,0.3F));
            }
            Array<Array<CardModel>> finished = poker.getFinished();
            finished.removeIndex(finished.size-1);
        }
        vecOper.clear();
        return true;
    }

}
