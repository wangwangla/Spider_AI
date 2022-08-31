package com.spider.card;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.spider.SpiderGame;
import com.spider.asset.AssetUtil;

import java.lang.reflect.Array;

public class Card extends Group {
    private int z_index;
    private boolean visible;
    private Image img, imgBack;
    private int suit;//花色 1 2 3 4
    private int point;//点数 1-13
    private boolean show;//是否已翻开

    public Card(int suit, int point) {
        this(suit, point, false);
    }

    public Card(Card card) {
        this.suit = card.suit;
        this.point = card.point;
        this.show = card.show;
        this.z_index = card.z_index;
        this.visible = card.visible;
        this.img = card.img;
        this.imgBack = card.imgBack;
    }
    public Card(int suit, int point, boolean show) {
        this.suit = suit;
        this.point = point;
        this.show = show;
        this.visible = true;
    }

    public int getPoint() {
        return point;
    }

    public void setSuit(int suit) {
        this.suit = suit;
    }

    public void setPoint(int point) {
        this.point = point;
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

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    //返回花色 C D H S
    public int getSuit() {
        return suit;
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
        return "Card{" +
                "z_index=" + z_index +
                ", visible=" + visible +
                ", img=" + img +
                ", imgBack=" + imgBack +
                ", suit=" + suit +
                ", point=" + point +
                ", show=" + show +
                ", position=" + position +
                '}';
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
}