package com.spider.pMove;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
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
    public PMove(Group finishGroup){
        this.finishGroup = finishGroup;
    }

    public PMove(int origIndex, int destIndex,int num,Group finishGroup){
        this.finishGroup = finishGroup;
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
        if (num>0 && num <= poker.getDesk().get(origIndex).size){

        }else {
            System.out.println();
        }
        //暂存最外张牌
        //eg. size=10, card[9].suit
        Array<Card> cards = poker.getDesk().get(origIndex);
        int suit = cards.get(cards.size - 1).getSuit();
        int point = cards.get(cards.size - 1).getPoint();

        //从下数第2张牌开始遍历
        //eg. num==4, i=[0,1,2]
        for (int i = 0; i < num - 1; ++i) {
            //eg. size=10, up=10-[0,1,2]-2=[8,7,6]
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
        Card itOrigEnd = cards.get(cards.size - 1);
        Array<Card> itDest = poker.getDesk().get(dest);
        Array<Card> cards1 = poker.getDesk().get(dest);
        //目标位置为空 或者
        //目标堆叠的最外牌==移动牌顶层+1
        if (poker.getDesk().get(dest).size <= 0 ||
                (itOrigBegin.getPoint() + 1 == itDest.get(itDest.size - 1).getPoint())) {
            //加上移来的牌
//            poker.getDesk().get(dest).insert(itDest, itOrigBegin, itOrigEnd);
            Array<Card> temp = new Array<Card>();
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
                array.removeValue(card, false);
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
            Restore restored = new Restore(dest,finishGroup);
            if (restored.canRestore(poker,dest)) {
                System.out.println("=======================================");

                if (restored.doAction(poker) == false) {

                }
            }

            return true;
        } else {
            return false;
        }
    }


    public void startAnimation() {
        startAnimation_inner();
    }

    public void startAnimationQuick(boolean bOnAnimation, boolean bStopAnimation) {
//        startAnimation_inner(bOnAnimation, bStopAnimation, 0.1);
    }

    public void startAnimation_inner() {
        assert (poker.isHasGUI());
        assert (success);
        //如果发生了回收事件，先恢复到回收前
        Array<Card> cards = poker.getDesk().get(dest);
        int i1 = cards.size - num;
        float baseY = 0;
        if (i1 >0) {
            Card card1 = cards.get(cards.size - num-1);
            baseY = card1.getY();
        }
        for (int i = 0; i < num; ++i) {
            float v = Constant.worldWidth / 10.0F;
            Card card = cards.get(cards.size - num+i);
//            card.addAction(Actions.moveTo((dest)* v,baseY-20*(i+1),0.2F));
            card.setPosition(dest* v,baseY-20*(i+1));
        }


    }

    void startHintAnimation(boolean bOnAnimation, boolean bStopAnimation) {
        assert (poker.isHasGUI());
        assert (success);
        //如果发生了回收事件，先恢复到回收前
        if (restored != null) {
            restored.redo(poker);
        }
//        SendMessage(hWnd, WM_SIZE, 0, 0);

//        vector<POINT> vecEndPt;
//
//        shared_ptr<SequentialAnimation> seq(make_shared<SequentialAnimation>());
//
//        ParallelAnimation* para = new ParallelAnimation;
//        ParallelAnimation* paraGoBack = new ParallelAnimation;
//
//        vector<AbstractAnimation*> vecFinalAni;
//
//        //
//        if (shownLastCard)
//        {
//        auto& card = poker->desk[orig].back();
//        card.SetShow(false);
//        }
//        for (int i = 0; i < num; ++i)
//        {
//        int sz = poker->desk[dest].size();
//        auto& card = poker->desk[dest][sz - num + i];
//
//        vecEndPt.push_back(card.GetPos());
//
//        card.SetPos(vecStartPt[i]);
//        card.SetZIndex(999);
//
//        para->Add(new ValueAnimation<Card, POINT>(&card, 500, &Card::SetPos, vecStartPt[i], vecEndPt[i]));
//        paraGoBack->Add(new ValueAnimation<Card, POINT>(&card, 500, &Card::SetPos, vecEndPt[i], vecStartPt[i]));
//
//        //恢复z-index
//        vecFinalAni.push_back(new SettingAnimation<Card, int>(&card, 0, &Card::SetZIndex, 0));
//        }
//
//        //移动
//        seq->Add(para);
//        seq->Add(paraGoBack);
//
//        //恢复z-index
//        for (auto& ani : vecFinalAni)
//        seq->Add(ani);
//
//        bStopAnimation = false;
//        bOnAnimation = true;
//        seq->Start(hWnd, bStopAnimation);
//
        bOnAnimation = false;
    }


    public void redoAnimation() {
        assert (poker.isHasGUI());
        int index = 0;
        float worldWidth = Constant.worldWidth;
        float v = worldWidth / 10.0F;
        for (Array<Card> cards : poker.getDesk()) {
            index ++;
            float offSetY = 0;
            for (Card card : cards) {
                card.addAction(Actions.moveTo((index-1)* v,offSetY,1));
                offSetY -= 20;
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
            Array<Card> array = poker.getDesk().get(orig);
            array.get(array.size - 1).setShow(false);
        }

        Array<Array<Card>> desk = poker.getDesk();
        Array<Card> array1 = desk.get(dest);
        int start = array1.size - num;
        int end = array1.size - 1;
        Array<Card> temp = new Array<Card>();
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
            array1.removeValue(card,false);
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