package com.spider.action.pMove;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.spider.action.CardAction;
import com.spider.action.restore.Restore;
import com.spider.card.Card;
import com.spider.card.CardViewProvider;
import com.spider.manager.GameManager;
import com.spider.model.CardModel;
import com.spider.pocker.Pocker;

public class PMove extends CardAction {
    private final int orig;
    private final int dest;
    private final int num;
    private Pocker poker;
    private boolean shownLastCard;
    private Restore restored;
    private boolean success;
    private final Group finishGroup;
    private final Group cardGroup;
    private Array<CardModel> temp;
    private final CardViewProvider viewProvider;

    public PMove(int origIndex, int destIndex, int num, Group finishGroup, Group cardGroup, CardViewProvider viewProvider){
        this.finishGroup = finishGroup;
        this.cardGroup = cardGroup;
        this.orig = origIndex;
        this.dest = destIndex;
        this.num = num;
        this.viewProvider = viewProvider;
    }

    //能否从 origIndex 拿 num 张
    public boolean canPick(Pocker poker, int origIndex, int num) {
        if (origIndex < 0 || origIndex >= poker.getDesk().size) {
            return false;
        }
        Array<CardModel> cards = poker.getDesk().get(origIndex);
        if (num <= 0 || num > cards.size) {
            return false;
        }
        int suit = cards.get(cards.size - 1).getSuit();
        int point = cards.get(cards.size - 1).getPoint();

        for (int i = 0; i < num - 1; ++i) {
            int index = cards.size - i - 2;
            CardModel card = cards.get(index);
            if (card.getSuit() != suit) {
                return false;
            }
            if (!card.isFaceUp()) {
                return false;
            }
            if (point + 1 == card.getPoint()) {
                point++;
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean canMove(Pocker poker, int origIndex, int destIndex, int num) {
        if (!canPick(poker, origIndex, num)) {
            return false;
        }
        Array<CardModel> cards = poker.getDesk().get(origIndex);
        CardModel origTopCard = cards.get(cards.size - num);
        Array<CardModel> destCards = poker.getDesk().get(destIndex);
        if (destCards.size <= 0) {
            return true;
        } else if (origTopCard.getPoint() + 1 == destCards.get(destCards.size - 1).getPoint())
            return true;
        return false;
    }

    public boolean doAction(Pocker inpoker) {
        poker = inpoker;
        if (!canPick(poker, orig, num)) {
            return false;
        }
        Array<CardModel> cards = poker.getDesk().get(orig);
        CardModel itOrigBegin = cards.get(cards.size - num);
        Array<CardModel> itDest = poker.getDesk().get(dest);
        if (itDest.size <= 0 || (itOrigBegin.getPoint() + 1 == itDest.get(itDest.size - 1).getPoint())) {
            temp = new Array<CardModel>();
            for (int i = cards.size - num; i < cards.size; i++) {
                itDest.add(cards.get(i));
                temp.add(cards.get(i));
            }
            Array<CardModel> array = poker.getDesk().get(orig);
            for (CardModel cardModel : temp) {
                Card card = viewProvider.viewOf(cardModel);
                if (card != null) {
                    card.toFront();
                }
                array.removeValue(cardModel, true);
            }
            Array<CardModel> array1 = poker.getDesk().get(orig);
            if (array1.size > 0 && !array1.get(array1.size - 1).isFaceUp()) {
                array1.get(array1.size - 1).setFaceUp(true);
                Card lastCardView = viewProvider.viewOf(array1.get(array1.size - 1));
                if (lastCardView != null) {
                    lastCardView.setShow(true);
                }
                shownLastCard = true;
            } else {
                shownLastCard = false;
            }
            poker.minusOne();
            poker.addOperation();
            success = true;
            return true;
        } else {
            return false;
        }
    }

    public Restore restore(){
        restored = new Restore(finishGroup,cardGroup, viewProvider);
        if (restored.doAction(poker)) {
            cardGroup.addAction(Actions.delay(0.3F,Actions.run(()-> restored.startAnimation())));
        }else {
            restored = null;
        }
        return restored;
    }

    public void startAnimation() {
        startAnimation_inner();
    }

    public void startAnimation_inner() {
        Array<CardModel> cards = poker.getDesk().get(dest);
        int firstNewIndex = cards.size - num; // index of the lowest card just added
        Array<Image> vecImageEmpty = GameManager.vecImageEmpty;
        Image image = vecImageEmpty.get(dest);
        for (int i = 0; i < num; ++i) {
            CardModel cardModel = cards.get(firstNewIndex + i);
            Card card = viewProvider.viewOf(cardModel);
            if (card != null) {
                float targetY = GameManager.stackY(firstNewIndex + i);
                card.addAction(Actions.moveTo(image.getX(), targetY,0.1F));
            }
        }
    }

    public void redoAnimation() {
        Array<Image> vecImageEmpty = GameManager.vecImageEmpty;
        Image image = vecImageEmpty.get(orig);
        Array<CardModel> cards = poker.getDesk().get(orig);
        int firstIndex = cards.size - num; // after redo, temp cards are at the top end
        for (int i = 0; i < temp.size; i++) {
            CardModel cardModel = temp.get(i);
            Card card = viewProvider.viewOf(cardModel);
            if (card != null) {
                float targetY = GameManager.stackY(firstIndex + i);
                card.addAction(Actions.moveTo(image.getX(),targetY,0.1f));
                cardGroup.addActor(card);
            }
        }
    }

    public boolean redo(Pocker inpoker) {
        assert (success);
        poker = inpoker;
        if (restored != null) {
            restored.redo(poker);
        }
        success = false;
        poker.setOperation(poker.getOperation() - 1);
        poker.setScore(poker.getScore() + 1);
        if (shownLastCard) {
            Array<CardModel> array = poker.getDesk().get(orig);
            array.get(array.size - 1).setFaceUp(false);
            Card view = viewProvider.viewOf(array.get(array.size - 1));
            if (view != null) {
                view.setShow(false);
            }
        }
        Array<Array<CardModel>> desk = poker.getDesk();
        Array<CardModel> array1 = desk.get(dest);
        int start = array1.size - num;
        int end = array1.size - 1;
        temp = new Array<CardModel>();
        Array<CardModel> array = poker.getDesk().get(orig);
        if ((start<0)) {
            start = 0;
        }
        if (end>=array1.size) {
            end = array1.size-1;
        }
        for (int i = start; i <= end; i++) {
            CardModel cardModel = array1.get(i);
            array.add(cardModel);
            temp.add(cardModel);
            Card cardView = viewProvider.viewOf(cardModel);
            if (cardView != null) {
                cardView.toFront();
            }
        }
        for (CardModel cardModel : temp) {
            array1.removeValue(cardModel,true);
        }
        return true;
    }

    @Override
    public String toString() {
        return orig +"  "+dest +"   ";
    }
}
