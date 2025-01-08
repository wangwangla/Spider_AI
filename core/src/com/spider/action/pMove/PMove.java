package com.spider.action.pMove;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.spider.action.Action;
import com.spider.card.Card;
import com.spider.constant.Constant;
import com.spider.manager.GameManager;
import com.spider.pocker.Pocker;
import com.spider.action.restore.Restore;

/**
 * 此类仅仅作为一个   工具类存在
 */
public class PMove extends Action {
    private int orig;
    private int dest;
    private int num;
    private Pocker poker;
    private boolean shownLastCard;
    private Restore restored;
    private boolean success;
    private Array<Card> temp;
    private Group finishGroup;
    private Group cardGroup;
    public PMove(){}
    /**
     * 开始 -> 目标  移动的次数
     * @param origIndex
     * @param destIndex
     * @param num
     * @param finishGroup
     * @param cardGroup
     */
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
     * @param poker 牌局
     * @param origIndex 开始的列
     * @param num 牌数
     * @return 返回是否可以移动
     */
    public boolean canPick(Pocker poker, int origIndex, int num) {
        //开始列
        Array<Card> cards = poker.getDesk().get(origIndex);
        //最后一张的颜色和点数
        int suit = cards.get(cards.size - 1).getSuit();
        int point = cards.get(cards.size - 1).getPoint();
        /**
         * 第一张牌拿过了
         * 从倒数第二张开始和前一张比较
         * 这里不需要判断   因为不会出错  出错这不是这里的问题
         */
        for (int i = 0; i < num - 1; ++i) {
            int index = cards.size - i - 2;
            Card card = cards.get(index);
            //牌面是可点击状态的   这个需要修改，是为翻拍还是被压住
            if (!card.isShow()) {
                return false;
            }
            if (card.getSuit() != suit) {
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

    /**
     *  可以移动
     * @param poker
     * @param origIndex 开始
     * @param destIndex 目标
     * @param num 牌数
     * @return
     */
    public boolean canMove(Pocker poker, int origIndex, int destIndex, int num) {
        //不能拾取返回
        if (!canPick(poker, origIndex, num)) {
            return false;
        }
        Array<Card> cards = poker.getDesk().get(origIndex);

        //移动的最大牌是否和目标的最小数是想匹配的

        //可以拾起然后开始取牌
        Card origTopCard = cards.get(cards.size - num);
        //目标
        Array<Card> destCards = poker.getDesk().get(destIndex);
        //目标小于0 是可以放牌的  【没有K的限制】
        if (destCards.size <= 0) {
            return true;
        } else if (origTopCard.getPoint() + 1 == destCards.get(destCards.size - 1).getPoint()) {//目标堆叠的最外牌==移动牌顶层+1
            return true;
        }
        return false;
    }

    /**
     * 执行移动
     * @param inpoker
     * @return
     */
    public boolean doAction(Pocker inpoker) {
        poker = inpoker;
        if (!canPick(poker, orig, num)) {
            return false;
        }
        Array<Card> cards = poker.getDesk().get(orig);
        Card itOrigBegin = cards.get(cards.size - num);
        Array<Card> itDest = poker.getDesk().get(dest);
        //目标位置为空 或者
        //目标堆叠的最外牌==移动牌顶层+1
        if (itDest.size <= 0 ||
                (itOrigBegin.getPoint() + 1 == itDest.get(itDest.size - 1).getPoint())) {
            //加上移来的牌
            temp = new Array<Card>();
            //将牌一张一张的给目标堆
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
            if (cardGroup != null) {
                cardGroup.addAction(Actions.delay(0.5f, Actions.run(() -> {
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
        //数据上面已经移动过来了   还差动画
        Array<Card> cards = poker.getDesk().get(dest);
        int i1 = cards.size - num;
        float baseY = 20;
        if (i1 > 0) {
            Card card1 = cards.get(cards.size - num-1);
            baseY = card1.getY();
        }
        Array<Image> vecImageEmpty = GameManager.vecImageEmpty;
        for (int i = 0; i < num; ++i) {
            Image image = vecImageEmpty.get(dest);
            Card card = cards.get(cards.size - num+i);
            if (Constant.animation){
                card.addAction(Actions.moveTo(image.getX(), baseY -20*(i + 1),0.1F));
            }else {
                card.setPosition(image.getX(), baseY -20*(i + 1));
            }
        }
    }

    public void redoAnimation() {
        Array<Image> vecImageEmpty = GameManager.vecImageEmpty;
        Image image = vecImageEmpty.get(orig);
        Array<Card> cards = poker.getDesk().get(orig);
        float offSetY = -(cards.size-num) * 20;
        //撤销的时候加入到temp
        for (Card card : temp) {
            card.addAction(Actions.moveTo(image.getX(),offSetY,0.1f));
            offSetY-=20;
            cardGroup.addActor(card);
            //少一个牌面返回去
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
    public void print() {
        System.out.println(orig +"  "+dest +"   ");
    }
}