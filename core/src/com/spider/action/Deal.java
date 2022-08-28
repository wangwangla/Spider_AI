package com.spider.action;

import com.badlogic.gdx.utils.Array;
import com.spider.card.Card;
import com.spider.pocker.Pocker;

public class Deal extends Action{
    private int suitNum;
    private int seed;
    private boolean enableSound;
    private int soundDeal;
    public Deal(int suitNum,int seed,boolean enableSound,int soundDeal){
        this.enableSound = enableSound;
        this.soundDeal = soundDeal;
        this.suitNum = suitNum;
        this.seed = seed;
    }


    public String GetCommand() {
//        using namespace std;
        return "dr "+(suitNum)+" "+(seed);
    }

    public void StartAnimation(boolean bOnAnimation, boolean bStopAnimation) {

    }

    public void RedoAnimation(boolean bOnAnimation, boolean bStopAnimation){

    }

    //返回1维数组，各花色依次1-13点，共8*13=104张
    public Array<Card> genInitCard() {
        Array<Card> result = new Array<Card>();
        switch (suitNum)
        {
            case 1:
                for (int i = 0; i < 8; ++i)
                    for (int j = 1; j <= 13; ++j)
                        result.add(new Card(4, j));//1个花色：黑桃
                break;
            case 2:
                for (int i = 0; i < 8; ++i)
                    for (int j = 1; j <= 13; ++j)
                        result.add(new Card((i>3) ? 3 : 4, j));//2个花色：红桃，黑桃
                break;
            case 4:
                for (int i = 0; i < 8; ++i)
                    for (int j = 1; j <= 13; ++j)
                        result.add(new Card( i % 4 + 1, j));//4个花色
                break;
            default:
//                throw string("Error:'genInitCard(" + to_string(suitNum) + ")");
                return result;
        }
        return result;
    }

    public boolean Do(Pocker inpoker) {
        poker = inpoker;

        poker.setSuitNum(suitNum);
        poker.setSeed(seed);
        //先清理
        poker.getDesk().clear();
        poker.getCorner().clear();
        poker->finished.clear();

        std::default_random_engine e;

        //生成整齐牌
        auto cards = genInitCard();

        //打乱
        e.seed(seed);
        random_shuffle(cards.begin(), cards.end(), [&](int i){return e() % i; });

        //发牌
        int pos = 0;

        //4摞6张的=24
        for (int i = 0; i < 4; ++i)
        {
            vector<Card> deskOne;
            for (int j = 0; j < 6; ++j)
                deskOne.push_back(cards[pos++]);
            poker->desk.push_back(deskOne);
        }

        //6摞5张的=30
        for (int i = 0; i < 6; ++i)
        {
            vector<Card> deskOne;
            for (int j = 0; j < 5; ++j)
                deskOne.push_back(cards[pos++]);
            poker->desk.push_back(deskOne);
        }

        //5摞 待发区=50
        for (int i = 0; i < 5; ++i)
        {
            vector<Card> cornerOne;
            for (int j = 0; j < 10; ++j)
                cornerOne.push_back(cards[pos++]);
            poker->corner.push_back(cornerOne);
        }

        //每摞最外的牌亮牌
        for (auto &deskOne : poker->desk)
        deskOne.back().show = true;

        poker->score = 500;
        poker->operation = 0;

        return true;
    }

#ifndef _CONSOLE
    void Deal::StartAnimation(HWND hWnd,bool &bOnAnimation,bool &bStopAnimation)
    {
        //刷新牌的最后位置
        SendMessage(hWnd, WM_SIZE, 0, 0);

        shared_ptr<SequentialAnimation> seq(make_shared<SequentialAnimation>());
        POINT ptStart = poker->corner.back().back().GetPos();

        vector<AbstractAnimation*> vecFinal;
        for (int i = 0; i < 54; ++i)
        {
            int deskIndex = i % 10;
            int cardIndex = i / 10;

            auto& card = poker->desk[deskIndex][cardIndex];

            //所有牌设置为不可见
            card.SetVisible(false);

            //动画：设置z-index
            seq->Add(new SettingAnimation<Card, int>(&card,0,&Card::SetZIndex,999-i));

            //动画：设置为可见
            seq->Add(new SettingAnimation<Card, bool>(&card,0,&Card::SetVisible,true));

            //动画：从角落到指定位置
            seq->Add(new ValueAnimation<Card,POINT>(&card,25,&Card::SetPos,ptStart,card.GetPos()));

            card.SetPos(ptStart);

            //动画：恢复z-index
            seq->Add(new SettingAnimation<Card, int>(&card,0,&Card::SetZIndex,0));

            //所有牌设置为背面
            card.show = false;

            //最后10张牌
            if (cardIndex == poker->desk[deskIndex].size() - 1)
            {
                //背面翻到不显示
                vecFinal.push_back(new ValueAnimation<TImage, double>(&card.GetBackImage(),25,&TImage::SetIWidth,1.0,0.0));

                //动画：显示牌正面
                vecFinal.push_back(new SettingAnimation<Card, bool>(&card,0,&Card::SetShow,true));

                //正面翻出来
                vecFinal.push_back(new ValueAnimation<TImage, double>(&card.GetImage(),25,&TImage::SetIWidth,0.0,1.0));
            }
        }

        seq->Add(vecFinal);

        if (enableSound)
        {
            //
            int msAll = 25 * 54 + 50 * 10;
            int times = msAll / 125 + 1;
            auto play = [&]()
            {
                PlaySound((LPCSTR)soundDeal, GetModuleHandle(NULL), SND_RESOURCE | SND_SYNC);
            };
            while (times--)
            {
                thread t(play);
                t.detach();
            }
        }

        bOnAnimation = true;
        seq->Start(hWnd, bStopAnimation);
        bOnAnimation = false;
    }
#endif

    bool Deal::Redo(Poker* inpoker)
    {
        poker = inpoker;

        poker->desk.clear();
        poker->corner.clear();
        poker->finished.clear();

        return true;
    }
}
