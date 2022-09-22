package com.spider.pMove;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.spider.action.Action;
import com.spider.card.Card;
import com.spider.constant.Constant;
import com.spider.manager.GameManager;
import com.spider.pocker.Pocker;
import com.spider.restore.Restore;

public class PMove extends Action {
    private int orig;
    private int dest;
    private int num;
    private Pocker poker;
    private Array<Vector2> vecStartPt = new Array<Vector2>();
    private boolean shownLastCard;
    private Restore restored;
    private boolean success;
    private Group finishGroup;
    private Group cardGroup;

    public PMove(){}

    public PMove(int origIndex, int destIndex,int num,Group finishGroup,Group cardGroup){
        this.finishGroup = finishGroup;
        this.cardGroup = cardGroup;
        this.orig = origIndex;
        this.dest = destIndex;
        this.num = num;
    }

    //返回是否可以移动
    //deskNum 牌堆编号
    //pos 牌编号
    public boolean canPick(Pocker poker, int origIndex, int num) {
        assert (origIndex >= 0 && origIndex < poker.getDesk().size);
        assert (num > 0 && num > poker.getDesk().get(origIndex).size);
        //暂存最外张牌
        //eg. size=10, card[9].suit
        Array<Card> cards = poker.getDesk().get(origIndex);
        int suit = cards.get(cards.size - 1).getSuit();
        int point = cards.get(cards.size - 1).getPoint();

        //从下数第2张牌开始遍历
        //eg. num==4, i=[0,1,2]
        for (int i = 0; i < num - 1; ++i) {
            int index = poker.getDesk().get(origIndex).size - i - 2;
            Card card = poker.getDesk().get(origIndex).get(index);
            if (card.getSuit() != suit)
                return false;
            if (!card.isShow())
                return false;
            if (point + 1 == card.getPoint())
                point++;
            else
                return false;
        }
        return true;
    }

    public boolean canMove(Pocker poker, int origIndex, int destIndex, int num) {
        //不能拾取返回false
        if (!canPick(poker, origIndex, num))
            return false;
        Array<Card> cards = poker.getDesk().get(origIndex);
        Card origTopCard = cards.get(cards.size - num);
        Array<Card> destCards = poker.getDesk().get(destIndex);
        if (destCards.size <= 0)
            return true;
        else if (origTopCard.getPoint() + 1 == destCards.get(destCards.size - 1).getPoint())//目标堆叠的最外牌==移动牌顶层+1
            return true;
        return false;
    }

    public boolean doAction(Pocker inpoker) {
        poker = inpoker;
        //不能拾取返回false
        if (!canPick(poker, orig, num)) {
            return false;
        }
        Array<Card> cards = poker.getDesk().get(orig);
        Card itOrigBegin = cards.get(cards.size - num);
        Array<Card> itDest = poker.getDesk().get(dest);
        //目标位置为空 或者
        //目标堆叠的最外牌==移动牌顶层+1
        if (poker.getDesk().get(dest).size <= 0 ||
                (itOrigBegin.getPoint() + 1 == itDest.get(itDest.size - 1).getPoint())) {
            //加上移来的牌
            temp = new Array<Card>();
            for (int i = cards.size - num; i < cards.size; i++) {
                itDest.add(cards.get(i));
                temp.add(cards.get(i));
            }
            if (poker.isHasGUI()) {
                //加入点集
                vecStartPt.clear();
                for (int i = cards.size - num; i < cards.size; i++) {
                    Card card = cards.get(i);
                    vecStartPt.add(new Vector2(card.getX(), card.getY()));
                }
            }
            //擦除移走的牌
            Array<Card> array = poker.getDesk().get(orig);
            for (Card card : temp) {
                card.toFront();
                array.removeValue(card, true);
            }
            //翻开暗牌
            Array<Card> array1 = poker.getDesk().get(orig);
            if (!(poker.getDesk().get(orig).size <= 0) && array1.get(array1.size - 1).isShow() == false) {
                array1.get(array1.size - 1).setShow(true);
                shownLastCard = true;
            } else {
                shownLastCard = false;
            }
            poker.minusOne();
            poker.addOperation();
            success = true;
            restored = new Restore(finishGroup,cardGroup);
            restored.setUpdateGroup(updateGroup);

            if (restored.doAction(poker) == false) {
                restored = null;
            }

            return true;
        } else {
            return false;
        }
    }

    public void startAnimation() {
        startAnimation_inner();
    }

    public void startAnimation_inner() {
        assert (poker.isHasGUI());
        assert (success);
        //如果发生了回收事件，先恢复到回收前
        if (restored!=null){
            restored.redo(poker);
        }
        Array<Card> cards = poker.getDesk().get(dest);
        int i1 = cards.size - num;
        float baseY = 0;
        if (i1 >0) {
            Card card1 = cards.get(cards.size - num-1);
            baseY = card1.getY();
        }
        Array<Image> vecImageEmpty = GameManager.vecImageEmpty;
        for (int i = 0; i < num; ++i) {
            Image image = vecImageEmpty.get(dest);
            if(cards.size-num<0){
                System.out.println();
            }
            Card card = cards.get(cards.size - num+i);
            card.addAction(Actions.moveTo(image.getX(),baseY-20*(i+1),0.3F));
        }

        if (restored!=null){
            restored.doAction(poker);
            restored.startAnimation();
        }
    }

    public void redoAnimation() {
        assert (poker.isHasGUI());
        Array<Image> vecImageEmpty = GameManager.vecImageEmpty;
        Image image = vecImageEmpty.get(orig);
        Array<Card> cards = poker.getDesk().get(orig);
        float offSetY = -(cards.size-num) * 20;
        for (Card card : temp) {
            card.addAction(Actions.moveTo(image.getX(),offSetY,1));
            offSetY-=20;
        }
    }
    private Array<Card> temp;
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
            Array<Card> array = poker.getDesk().get(orig);
            array.get(array.size - 1).setShow(false);
        }
        Array<Array<Card>> desk = poker.getDesk();
        Array<Card> array1 = desk.get(dest);
        int start = array1.size - num;
        int end = array1.size - 1;
        temp = new Array<Card>();
        Array<Card> array = poker.getDesk().get(orig);
        if ((start<0)) {
            start = 0;
        }
        if (end>=array1.size) {
            end = array1.size-1;
        }
        for (int i = start; i <= end; i++) {
            Card card = array1.get(i);
            array.add(card);
            temp.add(card);
        }
        for (Card card : temp) {
            array1.removeValue(card,true);
        }
        return true;
    }

    public void setPocker(Pocker poker) {
        this.poker = poker;
    }

    @Override
    public String toString() {
        return orig +"  "+dest +"   ";
    }
}