package com.spider.card;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class Card {
    private int z_index;
    private boolean visible;
    private Image img, imgBack;
    private int suit;//花色 1 2 3 4
    private int point;//点数 1-13
    private boolean show;//是否已翻开

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

    public void setShow(boolean show) {
        this.show = show;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setZIndex(int z) {
        this.z_index = z;
    }

    int GetZIndex() {
        return z_index;
    }

    void SetPos(Vector2 vector2) {
        img.setPosition(vector2.x, vector2.y);
        imgBack.setPosition(vector2.x, vector2.y);
    }

//
//    Vector2 GetPos(){
//        return img.getP;
//    }

    void SetImage(Image img, Image imgBack) {
        this.img = img;
        this.imgBack = imgBack;
    }

    Image GetBackImage() {
        return imgBack;
    }

    Image GetImage() {
        return img;
    }

//    void Draw(HDC hdc)
//    {
//        if (visible)
//            if (show)
//                img->Draw(hdc);
//            else
//                imgBack->Draw(hdc);
//    }
//#endif

    //返回花色 C D H S
    public char getSuit() {
        switch (suit) {
            case 1:
                return 'C';//梅花
            case 2:
                return 'D';//方块
            case 3:
                return 'H';//红桃
            case 4:
                return 'S';//黑桃
            default:
                return 'S';
                //                throw new Exception("Error:'getSuit():' Undefined suit");
        }
    }

    public boolean isShow() {
        return show;
    }
}