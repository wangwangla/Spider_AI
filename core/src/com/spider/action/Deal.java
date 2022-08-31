package com.spider.action;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.Array;
import com.spider.card.Card;
import com.spider.log.NLog;
import com.spider.pocker.Pocker;

import java.util.Random;

public class Deal extends Action{
    private int suitNum;
    private int seed;
    private boolean enableSound;
    private int soundDeal;
    public Deal(int suitNum,int seed,boolean enableSound,int soundDeal) {
        this.enableSound = enableSound;
        this.soundDeal = soundDeal;
        this.suitNum = suitNum;
        this.seed = seed;
    }

    public String GetCommand() {
        return "dr "+(suitNum)+" "+(seed);
    }

    public void StartAnimation(boolean bOnAnimation, boolean bStopAnimation) {

    }

    public void RedoAnimation(boolean bOnAnimation, boolean bStopAnimation){

    }

    //返回1维数组，各花色依次1-13点，共8*13=104张
    public Array<Card> genInitCard() {
        Array<Card> result = new Array<Card>();
        switch (suitNum) {
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
        poker.setHasGUI(true);
        poker.setSuitNum(suitNum);
        poker.setSeed(seed);
        //先清理
        poker.getDesk().clear();
        poker.getCorner().clear();
        poker.getFinished().clear();
        //生成整齐牌
        Array<Card> cards = genInitCard();
        cardPrint("shuffle pre",cards);
        //打乱
        Random random = new Random();
        random.setSeed(seed);
        shuffle(cards);
        cardPrint("shuffle after",cards);
        //发牌
        int pos = 0;
        //4摞6张的=24
        for (int i = 0; i < 4; ++i) {
            Array<Card> deskOne = new Array<Card>();
            for (int j = 0; j < 6; ++j)
                deskOne.add(cards.get(pos++));
            poker.getDesk().add(deskOne);
        }
        //6摞5张的=30
        for (int i = 0; i < 6; ++i){
            Array<Card> deskOne = new Array<Card>();
            for (int j = 0; j < 5; ++j)
                deskOne.add(cards.get(pos++));
            poker.getDesk().add(deskOne);
        }

        //5摞 待发区=50
        for (int i = 0; i < 5; ++i) {
            Array<Card> cornerOne = new Array<Card>();
            for (int j = 0; j < 10; ++j)
                cornerOne.add(cards.get(pos++));
            poker.getCorner().add(cornerOne);
        }

        //每摞最外的牌亮牌
        for (Array<Card> cardArray : poker.getDesk()) {
            cardArray.get(cardArray.size-1).setShow(true);
        }
        poker.setScore(500);
        poker.setOperation(0);
        return true;
    }

    public void shuffle (Array<Card> array) {
        Object [] items = array.toArray();
        int size = items.length;
        Random random = new Random();
        random.setSeed(1);
        for (int i = size - 1; i >= 0; i--) {
            int ii = random.nextInt(i+1);
            Object temp = items[i];
            items[i] = items[ii];
            items[ii] = temp;
        }
        array.clear();
        for (Object item : items) {
            array.add((Card) item);
        }
    }

    private void cardPrint(String preShuffle,Array<Card> cards) {
        for (Card card : cards) {
            NLog.e("%s cards \n: %s",preShuffle,card);
        }
    }

    public void startAnimation(boolean bOnAnimation,boolean bStopAnimation) {
        //刷新牌的最后位置
        SequenceAction seq = new SequenceAction();
        Array<Array<Card>> corner = poker.getCorner();
        Array<Card> cards = corner.get(corner.size - 1);
        Card card = cards.get(cards.size - 1);
        Vector2 ptStart = card.getPosition();
        for (int i = 0; i < 54; ++i)
        {
            int deskIndex = i % 10;
            int cardIndex = i / 10;

            Array<Array<Card>> desk = poker.getDesk();
            Array<Card> cards1 = desk.get(deskIndex);
            card = cards1.get(cardIndex);
            //所有牌设置为不可见
            card.setVisible(false);
            //动画：设置z-index
//            seq.addAction(new SettingAnimation<Card, int>(&card,0,&Card::SetZIndex,999-i));
            card.setZIndex(999-i);
            //动画：设置为可见
            seq.addAction(Actions.visible(true));
            //动画：从角落到指定位置
            seq.addAction(Actions.moveTo(card.getX(),card.getY(),0.25F));
            //动画：恢复z-index
            card.setZIndex(0);

            //所有牌设置为背面
//            card.setShow(false);


            //最后10张牌
            if (cardIndex == poker.getDesk().get(deskIndex).size - 1) {
                //背面翻到不显示
//                vecFinal.push_back(new ValueAnimation<TImage, double>(&card.GetBackImage(),25,&TImage::SetIWidth,1.0,0.0));
//
//                //动画：显示牌正面
//                vecFinal.push_back(new SettingAnimation<Card, bool>(&card,0,&Card::SetShow,true));
//
//                //正面翻出来
//                vecFinal.push_back(new ValueAnimation<TImage, double>(&card.GetImage(),25,&TImage::SetIWidth,0.0,1.0));
            }
        }

//        seq->Add(vecFinal);

//        if (enableSound) {

//            int msAll = 25 * 54 + 50 * 10;
//            int times = msAll / 125 + 1;
//            auto play = [&]()
            {
//                PlaySound((LPCSTR)soundDeal, GetModuleHandle(NULL), SND_RESOURCE | SND_SYNC);
//            };
//            while (times--)
//            {
//                thread t(play);
//                t.detach();
//            }
        }

        bOnAnimation = true;
//        seq.Start(hWnd, bStopAnimation);
        bOnAnimation = false;
    }

    boolean redo(Pocker inpoker) {
        poker = inpoker;
        poker.getDesk().clear();
        poker.getCorner().clear();
        poker.getFinished().clear();
        return true;
    }
}
