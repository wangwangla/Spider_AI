package com.spider.action;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.spider.card.Card;
import com.spider.constant.Constant;
import com.spider.log.NLog;
import com.spider.pocker.Pocker;

import java.util.Random;

public class Deal extends Action{
    private int suitNum;
    private int seed;

    public Deal(int suitNum,int seed) {
        this.suitNum = suitNum;
        this.seed = seed;
    }

    /**
     * 发牌
     * @param inpoker
     * @return
     */
    public boolean doAction(Pocker inpoker) {
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
        shuffle(cards,random);
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

    public void shuffle(Array<Card> array, Random random) {
        Object [] items = array.toArray();
        int size = items.length;
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

    public void startAnimation() {
        float worldWidth = Constant.worldWidth;
        float v = worldWidth / 10.0F;
        Array<Array<Card>> deskPocker = poker.getDesk();
        int indexX = 0;
        for (Array<Card> array : deskPocker) {
            int y = 0;
            for (Card card : array) {
                card.addAction(Actions.moveTo(indexX*v,-y*20,indexX*0.1F+0.2F*y));
                y++;
            }
            indexX++;
        }
    }

    public boolean redo(Pocker inpoker,Group deskGroup,Group finishGroup,Group coener) {
        poker = inpoker;
        poker.getDesk().clear();
        poker.getCorner().clear();
        poker.getFinished().clear();
        deskGroup.clearChildren();
        finishGroup.clearChildren();
        coener.clearChildren();
        return true;
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
                return result;
        }
        return result;
    }


    public void initPos(Group sendCardGroup, Group corner) {
        Array<Array<Card>> deskPocker = poker.getDesk();
        Vector2 pos = new Vector2(0,0);
        corner.localToStageCoordinates(pos);
        sendCardGroup.stageToLocalCoordinates(pos);
        for (Array<Card> array : deskPocker) {
            for (Card card : array) {
                card.setPosition(pos.y,pos.x);
                NLog.e("posx posy %s   %s",pos.x,pos.y);
            }
        }
    }
}
