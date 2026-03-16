package com.spider.card;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.spider.SpiderGame;
import com.spider.model.CardModel;

/**
 * UI actor for a card. Wraps a CardModel so rendering is separated from game logic.
 */
public class Card extends Group {
    private Image img, imgBack;
    private final CardModel model;
    private final Vector2 position = new Vector2();

    public Card(int suit, int point) {
        this(new CardModel(suit, point, false));
    }

    public Card(int suit, int point, boolean show) {
        this(new CardModel(suit, point, show));
    }

    public Card(CardModel model) {
        this.model = model;
    }

    public CardModel getModel() {
        return model;
    }

    public int getPoint() {
        return model.getPoint();
    }

    public void setShow(boolean show) {
        model.setFaceUp(show);
        if (img != null) {
            img.setVisible(show);
            if (imgBack != null) {
                imgBack.setVisible(!show);
            }
        }
    }

    //返回花色 C D H S
    public int getSuit() {
        return model.getSuit();
    }

    char getSuit1() {
        switch (model.getSuit())
        {
            case 1:return 'C';//club
            case 2:return 'D';//diamond
            case 3:return 'H';//heart
            case 4:return 'S';//spade
            default:
                throw new RuntimeException("Error:'getSuit():' Undefined suit");
        }
    }

    public boolean isShow() {
        return model.isFaceUp();
    }

    public Vector2 getPosition() {
        return position.set(getX(),getY());
    }

    @Override
    public String toString() {
        String x = (isShow() ? "" : "[") + getSuit1() + getPoint() + (isShow() ? "" : "]");
        return x;
    }

    public void initCard() {
        int imageIndex = (model.getSuit() - 1) * 13 + getPoint();
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
        }else {
            img.setVisible(false);
            imgBack.setVisible(true);
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
}
