package com.spider.action.pMove;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.spider.action.Action;
import com.spider.card.Card;
import com.spider.manager.GameManager;
import com.spider.pocker.Pocker;
import com.spider.action.restore.Restore;

public class PMove extends Action {
    private int orig;
    private int dest;
    private int num;
    private Pocker poker;
    private boolean shownLastCard;
    private Restore restored;
    private boolean success;
    private Array<Card> temp;
    public PMove(){}

    private Group finishGroup;
    private Group cardGroup;

    public PMove(int origIndex, int destIndex,int num,Group finishGroup,Group cardGroup){
        this.finishGroup = finishGroup;
        this.cardGroup = cardGroup;
        this.orig = origIndex;
        this.dest = destIndex;
        this.num = num;
    }

    /**
     * 能否拾起
     *
     * 起始列  的最下方向上找
     * @param poker
     * @param origIndex
     * @param num
     * @return
     */
    //返回是否可以移动
    //deskNum 牌堆编号
    //pos 牌编号
    public boolean canPick(Pocker poker, int origIndex, int num) {
        assert (origIndex >= 0 && origIndex < poker.getDesk().size);
        assert (num > 0 && num > poker.getDesk().get(origIndex).size);
        //暂存最外张牌
        //eg. size=10, card[9].suit
        //获取最外层的序号  以及  花色
        Array<Card> cards = poker.getDesk().get(origIndex);
        int suit = cards.get(cards.size - 1).getSuit();
        int point = cards.get(cards.size - 1).getPoint();

        //从下数第2张牌开始遍历
        //eg. num==4, i=[0,1,2]
        /**
         * 第一张牌拿过了
         * 从倒数第二张开始和前一张比较
         */
        for (int i = 0; i < num - 1; ++i) {
            int index = poker.getDesk().get(origIndex).size - i - 2;
            Card card = poker.getDesk().get(origIndex).get(index);
            if (card.getSuit() != suit) {
                return false;
            }
            if (!card.isShow()) {
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
        //不能拾取返回false
        if (!canPick(poker, origIndex, num)) {
            return false;
        }
        Array<Card> cards = poker.getDesk().get(origIndex);
        Card origTopCard = cards.get(cards.size - num);
        Array<Card> destCards = poker.getDesk().get(destIndex);
        if (destCards.size <= 0) {
            return true;
        } else if (origTopCard.getPoint() + 1 == destCards.get(destCards.size - 1).getPoint())//目标堆叠的最外牌==移动牌顶层+1
            return true;
        return false;
    }

    public boolean doAction(Pocker inpoker) {
        poker = inpoker;
        //不能拾取返回false
        /**
         * 移动之前再次检查是否可以拾起
         */
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
            return true;
        } else {
            return false;
        }
    }

    public Restore restore(){
        restored = new Restore(finishGroup,cardGroup);
        if (restored.doAction(poker)) {
            if (cardGroup == null) {
                cardGroup.addAction(Actions.delay(0.3F, Actions.run(() -> {
                    restored.startAnimation();
                })));
            }else {
                restored.startAnimation();
            }
        }else {
            restored = null;
        }
        return restored;
    }

    private Restore restore;
    public void startAnimation() {
        startAnimation_inner();
        restore = restore();
    }

    public Restore getRestore() {
        return restore;
    }

    /**
     * 执行完移动之后    会检测一下  是否存在回收任务   然后在播放动画，  但是有的时候 并不需要这个操作
     * 仅仅为了快速泡跑关卡
     */
    public void startAnimation_inner() {
        Array<Card> cards = poker.getDesk().get(dest);
        int i1 = cards.size - num;
        float baseY = 20;
        if (i1 >0) {
            Card card1 = cards.get(cards.size - num-1);
            baseY = card1.getY();
        }
        Array<Image> vecImageEmpty = GameManager.vecImageEmpty;
        for (int i = 0; i < num; ++i) {
            Image image = vecImageEmpty.get(dest);
            Card card = cards.get(cards.size - num+i);
            card.addAction(Actions.moveTo(image.getX(), baseY -20*(i + 1),0.1F));
        }
    }

    public void redoAnimation() {
        Array<Image> vecImageEmpty = GameManager.vecImageEmpty;
        Image image = vecImageEmpty.get(orig);
        Array<Card> cards = poker.getDesk().get(orig);
        float offSetY = -(cards.size-num) * 20;
        for (Card card : temp) {
            card.addAction(Actions.moveTo(image.getX(),offSetY,0.1f));
            offSetY-=20;
            cardGroup.addActor(card);
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
            card.toFront();
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


    @Override
    public void print() {
        super.print();
        System.out.println(toString());
    }
}