package com.spider.pMove;

import com.badlogic.gdx.utils.Array;
import com.spider.card.Card;
import com.spider.pocker.Pocker;

public class PMove {
    //返回是否可以移动
    //deskNum 牌堆编号
    //pos 牌编号
    boolean canPick(Pocker poker, int origIndex, int num) {
        assert(origIndex >= 0 && origIndex < poker.getDesk().size);
        assert(num > 0 && num <= poker.getDesk().get(origIndex).size);

        //暂存最外张牌
        //eg. size=10, card[9].suit
        Array<Card> cards = poker.getDesk().get(origIndex);
        int suit = cards.get(cards.size-1).getSuit();
        int point = cards.get(cards.size-1).getPoint();

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

    boolean canMove(Pocker poker, int origIndex, int destIndex, int num) {
        //不能拾取返回false
        if (!canPick(poker, origIndex, num))
            return false;
        Array<Card> cards = poker.getDesk().get(origIndex);
        Card origTopCard = cards.get(cards.size-num);
        Array<Card> destCards = poker.getDesk().get(destIndex);
        if (destCards.size<=0)
            return true;
        else if (origTopCard.getPoint() + 1 == destCards.get(destCards.size-1).getPoint())//目标堆叠的最外牌==移动牌顶层+1
            return true;
        return false;
    }

    private Pocker poker;
    private int orig;
    private int dest;
    private int num;

    boolean Do(Pocker inpoker) {
        poker = inpoker;
        //不能拾取返回false
        if (!canPick(poker, orig, num)) {
            return false;
        }
        Array<Card> cards = poker.getDesk().get(orig);
        Card itOrigBegin = cards.get(cards.size-1-num);
        Card itOrigEnd = cards.get(cards.size-1);
        Array<Card> itDest = poker.getDesk().get(dest);
        Array<Card> cards1 = poker.getDesk().get(dest);
        //目标位置为空 或者
        //目标堆叠的最外牌==移动牌顶层+1
        if (poker.getDesk().get(dest).size<= 0 ||
                (itOrigBegin.getPoint() + 1 == cards.get(cards.size-1).getPoint())) {
            //加上移来的牌
            poker.getDesk().get(dest).insert(itDest, itOrigBegin, itOrigEnd);

            if (poker.isHasGUI()) {
                //加入点集
                vecStartPt.clear();
                for_each(itOrigBegin, itOrigEnd, [&](const Card& card) {vecStartPt.push_back(card.GetPos()); });
            }

        //擦除移走的牌
            poker->desk[orig].erase(itOrigBegin, itOrigEnd);

        //翻开暗牌
        if (!poker->desk[orig].empty() && poker->desk[orig].back().show == false)
        {
        poker->desk[orig].back().show = true;
        shownLastCard = true;
        }
        else
        shownLastCard = false;

        poker->score--;
        poker->operation++;

        //进行回收
        restored = make_shared<Restore>(dest);
        if (restored->Do(poker) == false)
        restored = nullptr;

        success = true;
        return true;
        }
        else
        return false;
    }


    void PMove::StartAnimation(HWND hWnd, bool& bOnAnimation, bool& bStopAnimation)
    {
    StartAnimation_inner(hWnd, bOnAnimation, bStopAnimation,1.0);
    }

    void PMove::StartAnimationQuick(HWND hWnd, bool& bOnAnimation, bool& bStopAnimation)
    {
    StartAnimation_inner(hWnd, bOnAnimation, bStopAnimation,0.1);
    }

    void PMove::StartAnimation_inner(HWND hWnd, bool& bOnAnimation, bool& bStopAnimation,double iDuration)
    {
    assert(poker->hasGUI);
    assert(success);

    //如果发生了回收事件，先恢复到回收前
    if (restored)
    restored->Redo(poker);

    SendMessage(hWnd, WM_SIZE, 0, 0);

    vector<POINT> vecEndPt;

    shared_ptr<SequentialAnimation> seq(make_shared<SequentialAnimation>());

    ParallelAnimation* para = new ParallelAnimation;

    vector<AbstractAnimation*> vecFinalAni;
    for (int i = 0; i < num; ++i)
    {
    int sz = poker->desk[dest].size();
    auto& card = poker->desk[dest][sz - num + i];

    vecEndPt.push_back(card.GetPos());

    card.SetPos(vecStartPt[i]);
    card.SetZIndex(999);

    para->Add(new ValueAnimation<Card, POINT>(&card, 500*iDuration, &Card::SetPos, vecStartPt[i], vecEndPt[i]));

    //恢复z-index
    vecFinalAni.push_back(new SettingAnimation<Card, int>(&card, 0, &Card::SetZIndex, 0));
    }

    //移动
    seq->Add(para);

    //翻出正面
    if (shownLastCard)
    {
    auto& card = poker->desk[orig].back();
    seq->Add(CardTurnOverAnimation::AddBackToFrontAnimation(card));
    }

    //恢复z-index
    for (auto& ani : vecFinalAni)
    seq->Add(ani);

    bStopAnimation = false;
    bOnAnimation = true;
    seq->Start(hWnd, bStopAnimation);

    //如果在Do中发生了回收，此时再进行回收
    if (restored)
    {
    restored->Do(poker);
    restored->StartAnimation(hWnd, bOnAnimation, bStopAnimation);
    }
    bOnAnimation = false;
    }

    void PMove::StartHintAnimation(HWND hWnd, bool& bOnAnimation, bool& bStopAnimation)
    {
    assert(poker->hasGUI);
    assert(success);

    //如果发生了回收事件，先恢复到回收前
    if (restored)
    restored->Redo(poker);

    SendMessage(hWnd, WM_SIZE, 0, 0);

    vector<POINT> vecEndPt;

    shared_ptr<SequentialAnimation> seq(make_shared<SequentialAnimation>());

    ParallelAnimation* para = new ParallelAnimation;
    ParallelAnimation* paraGoBack = new ParallelAnimation;

    vector<AbstractAnimation*> vecFinalAni;

    //
    if (shownLastCard)
    {
    auto& card = poker->desk[orig].back();
    card.SetShow(false);
    }
    for (int i = 0; i < num; ++i)
    {
    int sz = poker->desk[dest].size();
    auto& card = poker->desk[dest][sz - num + i];

    vecEndPt.push_back(card.GetPos());

    card.SetPos(vecStartPt[i]);
    card.SetZIndex(999);

    para->Add(new ValueAnimation<Card, POINT>(&card, 500, &Card::SetPos, vecStartPt[i], vecEndPt[i]));
    paraGoBack->Add(new ValueAnimation<Card, POINT>(&card, 500, &Card::SetPos, vecEndPt[i], vecStartPt[i]));

    //恢复z-index
    vecFinalAni.push_back(new SettingAnimation<Card, int>(&card, 0, &Card::SetZIndex, 0));
    }

    //移动
    seq->Add(para);
    seq->Add(paraGoBack);

    //恢复z-index
    for (auto& ani : vecFinalAni)
    seq->Add(ani);

    bStopAnimation = false;
    bOnAnimation = true;
    seq->Start(hWnd, bStopAnimation);

    bOnAnimation = false;
    }


    void PMove::RedoAnimation(HWND hWnd, bool& bOnAnimation, bool& bStopAnimation)
    {
    assert(poker->hasGUI);
    }
    #endif

    bool PMove::Redo(Poker* inpoker)
    {
    assert(success);

    poker = inpoker;

    if (restored)
    {
    restored->Redo(poker);
    }

    success = false;

    poker->operation--;
    poker->score++;

    if (shownLastCard)
    {
    poker->desk[orig].back().show = false;
    }

    auto itOrigBegin = poker->desk[dest].end() - num;
    auto itOrigEnd = poker->desk[dest].end();

    auto itDest = poker->desk[orig].end();

    //加上移走的牌
    poker->desk[orig].insert(itDest, itOrigBegin, itOrigEnd);

    //擦除移来的牌
    poker->desk[dest].erase(itOrigBegin, itOrigEnd);

    return true;
    }`