package com.spider.manager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.spider.action.Action;
import com.spider.card.Card;
import com.spider.pocker.Pocker;
import com.spider.restore.Restore;

public class ReleaseCorner extends Action {
    private Restore restored;
    private Vector2 ptStart = new Vector2();
    private boolean success;
    private Pocker poker;
    private Group cardGroup;
    private Group sendCardGroup;

    public ReleaseCorner(Group cardGroup, Group sendCardGroup) {
        this(false,cardGroup,sendCardGroup);
    }

    public ReleaseCorner(boolean b, Group cardGroup, Group sendCardGroup){
        this.success = b;
        this.cardGroup = cardGroup;
        this.sendCardGroup = sendCardGroup;
    }

    //释放一摞右下角，检查收牌情况
    public boolean Do(Pocker inpoker, Group cardGroup) {
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
        if (poker.isHasGUI()) {
            Array<Array<Card>> corner = poker.getCorner();
            Array<Card> cards = corner.get(corner.size - 1);
            Card card = cards.get(cards.size - 1);
            ptStart = card.getPosition();
        }
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
        if (restored.Do(poker) == false)
            restored = null;
        return true;
    }

    public boolean Redo(Pocker inpoker) {
        assert (success);
        poker = inpoker;
        //撤销回收
        if (restored != null) {
            restored.Redo(poker);
        }
        poker.setScore(poker.getScore() + 1);
        poker.setOperation(poker.getOperation() - 1);

        //回收10张牌
        Array<Card> temp = new Array<Card>();
        for (int i = 0; i < 10; ++i) {
            //改为背面
            Array<Card> cards = poker.getDesk().get(i);
            cards.get(cards.size - 1).setShow(false);
            //回收
            temp.add(cards.get(cards.size - 1));
            //从桌上取掉
            sendCardGroup.addActor(cards.get(cards.size-1));

            cards.removeIndex(cards.size - 1);
            if (cards.size>0) {
                Card card = cards.get(cards.size - 1);
                card.setShow(true);
            }
        }
        poker.getCorner().add(temp);
        return true;
    }

    void StartAnimation(boolean bOnAnimation, boolean bStopAnimation) {
        assert (poker.isHasGUI());
        assert (success);
        //如果发生了回收事件，先恢复到回收前
        if (restored != null)
            restored.Redo(poker);
        //刷新牌的最后位置
//        SendMessage(hWnd, WM_SIZE, 0, 0);

//        shared_ptr<SequentialAnimation> seq(make_shared<SequentialAnimation>());
//
//        vector<AbstractAnimation*> vecFinalAni;
//        for (int i = 0; i < 10; ++i)
//        {
//            auto& card = poker->desk[i].back();
//
//            所有牌设置为可见
//            card.SetVisible(true);
//
//            第一张在最上面
//            card.SetZIndex(999 - i);
//
        //动画：设置为可见
//            seq->Add(new SettingAnimation<Card, bool>(&card, 0, &Card::SetVisible, true));
//
//            动画：从角落到指定位置
//            seq->Add(new ValueAnimation<Card, POINT>(&card, 25, &Card::SetPos, ptStart, card.GetPos()));

//            vecStartPos.push_back(ptStart);
//            vecEndPos.push_back(card.GetPos());
//
//            card.SetPos(ptStart);

        //从背面翻出来
//            auto temp = CardTurnOverAnimation::AddBackToFrontAnimation(card);
//            vecFinalAni.insert(vecFinalAni.end(), temp.begin(), temp.end());
//
//            //动画：恢复z-index
//            seq->Add(new SettingAnimation<Card, int>(&card, 0, &Card::SetZIndex, 0));
//    }

//        seq->Add(vecFinalAni);

//        if (enableSound)
//        {
//            //
//            int msAll = 75 * 10;
//            int times = msAll / 125 + 1;
//            auto play = [&]()
//            {
//                PlaySound((LPCSTR)soundDeal, GetModuleHandle(NULL), SND_RESOURCE | SND_SYNC);
//            };
//            while (times--)
//            {
//                thread t(play);
//                t.detach();
//            }
//        }

//        bStopAnimation = false;
//        bOnAnimation = true;
//        seq->Start(hWnd, bStopAnimation);
        //如果在Do中发生了回收，此时再进行回收
        if (restored!=null){
            restored.Do(poker);
//            restored->StartAnimation(bOnAnimation, bStopAnimation);
        }
        bOnAnimation = false;
    }

    void RedoAnimation(boolean bOnAnimation, boolean bStopAnimation) {
        assert (poker.isHasGUI());
//        SequentialAnimation* seq = new SequentialAnimation;
//
//        for (int i = 0; i < 10; ++i)
//        {
//            //最后一摞每张牌
//            auto& card = poker->corner.back()[i];
//
//            //所有牌设置为可见
//            card.SetVisible(true);
//
//            //所有牌设置为发完牌的位置
//            card.SetPos(vecEndPos[i]);
//
//            //动画时设置为顶层
//            card.SetZIndex(999);
//
//            //从正面翻回背面
//            seq->Add(CardTurnOverAnimation::AddFrontToBackAnimation(card));
//
//            //动画：从角落到指定位置
//            seq->Add(new ValueAnimation<Card, POINT>(&card, 25, &Card::SetPos, vecEndPos[i], vecStartPos[i]));
//
//            //恢复z-index
//            seq->Add(new SettingAnimation<Card, int>(&card, 0, &Card::SetZIndex, 0));
//        }
//
//        vecStartPos.clear();
//        vecEndPos.clear();
//
//        //
//        int msAll = 75 * 10;
//        int times = msAll / 125 + 1;
//        auto play = []()
//        {
//            PlaySound((LPCSTR)IDR_WAVE_DEAL, GetModuleHandle(NULL), SND_RESOURCE | SND_SYNC);
//        };
//        while (times--)
//        {
//            thread t(play);
//            t.detach();
//        }

        bStopAnimation = false;
        bOnAnimation = true;
//        seq->Start(hWnd, bStopAnimation);
//        delete seq;
        bOnAnimation = false;
//        SendMessage(hWnd, WM_SIZE, 0, 0);
//        RECT rc;
//        GetClientRect(hWnd, &rc);
//        InvalidateRect(hWnd, &rc, false);
//        UpdateWindow(hWnd);
    }
}
