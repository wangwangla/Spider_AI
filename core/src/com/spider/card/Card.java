package com.spider.card;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.spider.SpiderGame;

public class Card extends Group {
    private Image img, imgBack;
    private int suit;//花色 1 2 3 4
    private int point;//点数 1-13
    private boolean show;//是否已翻开

    public Card(int suit, int point) {
        this(suit, point, false);
    }

    public Card(int suit, int point, boolean show) {
        this.suit = suit;
        this.point = point;
        this.show = show;
    }

    public int getPoint() {
        return point;
    }

    public void setShow(boolean show) {
        this.show = show;
        if (show) {
            if (img != null) {
                img.setVisible(true);
                imgBack.setVisible(false);
            }
        }else {
            if (img != null) {
                img.setVisible(false);
                imgBack.setVisible(true);
            }
        }
    }

    //返回花色 C D H S
    public int getSuit() {
        return suit;
    }

    char getSuit1() {
        switch (suit)
        {
            case 1:return 'C';//梅花
            case 2:return 'D';//方块
            case 3:return 'H';//红桃
            case 4:return 'S';//黑桃
            default:
                throw new RuntimeException("Error:'getSuit():' Undefined suit");
        }
    }

    public boolean isShow() {
        return show;
    }

    private Vector2 position = new Vector2();
    public Vector2 getPosition() {
        return position.set(getX(),getY());
    }

    @Override
    public String toString() {
        String x = (show ? "" : "[") + getSuit1() + point + (show ? "" : "]");
        return x;
    }

    public void initCard() {
        int imageIndex = (suit - 1) * 13 + getPoint();
        img = new Image(SpiderGame.getAssetUtil()
                .loadTexture("Resource/card/CARD"+imageIndex+".png"));
        imgBack = new Image(SpiderGame.getAssetUtil().loadTexture(
                "Resource/cardback.png"));
        addActor(img);
        addActor(imgBack);
        setSize(img.getWidth(),img.getHeight());
        if (isShow()){
            img.setVisible(true);
            imgBack.setVisible(false);
        }
        img.setTouchable(Touchable.disabled);
        imgBack.setTouchable(Touchable.disabled);
        img.setX(0);
        img.setY(0);
    }

    @Override
    protected void setParent(Group parent) {
        super.setParent(parent);
    }

    public void setShowDelay(final boolean b, int index) {
        addAction(Actions.delay(0.1F * index + 0.2f,Actions.run(new Runnable() {
            @Override
            public void run() {
                setShow(b);
            }
        })));
    }

    public Card cloneInstance(){
        Card card = new Card(suit,point,isShow());
//        card.initCard();
        return card;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Card){
            Card obj1 = (Card) (obj);
            return suit == obj1.suit && point == obj1.point;
        }
        return false;
    }
}