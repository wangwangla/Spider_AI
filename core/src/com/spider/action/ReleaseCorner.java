package com.spider.action;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spider.card.Card;
import com.spider.card.CardViewProvider;
import com.spider.manager.GameManager;
import com.spider.model.CardModel;
import com.spider.pocker.Pocker;
import com.spider.action.restore.Restore;

public class ReleaseCorner extends CardAction {
    private Restore restored;
    private boolean success;
    private Pocker poker;
    private final Group sendCardGroup;
    private final Group cardGroup;
    private final Group finishGroup;
    private final CardViewProvider viewProvider;

    public ReleaseCorner(Group sendCardGroup, Group cardGroup, Group finishGroup, CardViewProvider viewProvider) {
        this.cardGroup = cardGroup;
        this.sendCardGroup = sendCardGroup;
        this.finishGroup = finishGroup;
        this.viewProvider = viewProvider;
    }

    //释放一叠右下角牌，检查回收
    public boolean doAction(Pocker inpoker) {
        poker = inpoker;
        //待发牌为空
        if (poker.getCorner().size <= 0) {
            return false;
        }
        //有空位且桌面牌 >=10 才能发，避免出现空列导致崩
        int sum = 0;
        boolean hasEmpty = false;
        for (Array<CardModel> cards : poker.getDesk()) {
            sum += cards.size;
            if (cards.size <= 0) {
                hasEmpty = true;
            }
        }
        if (hasEmpty && sum >= 10)
            return false;
        //取待发牌堆
        Array<Array<CardModel>> corner = poker.getCorner();
        //第一叠
        Array<CardModel> cards = corner.first();
        //分发到桌面
        for (int i = 0; i < 10; ++i) {
            poker.getDesk().get(i).add(cards.get(i));
            CardModel cardModel = cards.get(i);
            Card card = viewProvider.viewOf(cardModel);
            if (card == null) continue;
            if (poker.getDesk().get(i).size>=2) {
                card.setShow(true);
            }
            Vector2 vector2 = new Vector2(card.getX(Align.center),card.getY(Align.center));
            card.localToStageCoordinates(vector2);
            cardGroup.addActor(card);
            card.stageToLocalCoordinates(vector2);
            card.setPosition(vector2.x, vector2.y,Align.center);
        }
        //移除该叠
        corner.removeIndex(0);
        success = true;

        poker.setScore(poker.getScore() - 1);
        poker.setOperation(poker.getOperation() + 1);

        //回收检测
        restored = new Restore(finishGroup, cardGroup, viewProvider);
        if (!restored.doAction(poker))
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
        //收回10张
        Array<CardModel> temp = new Array<CardModel>();
        for (int i = 0; i < 10; ++i) {
            Array<CardModel> cards = poker.getDesk().get(i);
            CardModel cardModel = cards.get(cards.size - 1);
            temp.add(cardModel);
            Card card = viewProvider.viewOf(cardModel);
            if (card != null && sendCardGroup!=null) {
                vector2.set(card.getX(), card.getY());
                card.getParent().localToStageCoordinates(vector2);
                sendCardGroup.stageToLocalCoordinates(vector2);
                card.setPosition(vector2.x,vector2.y);
                sendCardGroup.addActor(card);
            }
            cards.removeIndex(cards.size - 1);
        }
        poker.getCorner().insert(0, temp);
        return true;
    }

    public void startAnimation() {
        Array<Image> vecImageEmpty = GameManager.vecImageEmpty;
        for (int i = 0; i < poker.getDesk().size; i++) {
            Array<CardModel> cards =poker.getDesk().get(i);
            if (cards.size>0){
                Image image = vecImageEmpty.get(i);
                CardModel topModel = cards.get(cards.size - 1);
                Card card = viewProvider.viewOf(topModel);
                if (card != null) {
                    float targetY = GameManager.stackY(cards.size - 1);
                    card.addAction(Actions.delay(i*0.1F,Actions.moveTo(image.getX(),targetY,0.2F)));
                }
            }
        }
    }

    public void redoAnimation() {
        if (poker.getCorner().size<0) {
            throw new GdxRuntimeException("error ");
        }
        Array<CardModel> cards = poker.getCorner().first();
        int index = 0;
        for (CardModel cardModel : cards) {
            Card card = viewProvider.viewOf(cardModel);
            if (card == null) continue;
            card.addAction(Actions.delay(index * 0.1F,Actions.moveTo(0, 0,0.1F)));
            card.setShowDelay(false,index);
            index ++;
        }
    }
}
