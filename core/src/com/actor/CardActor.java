package com.actor;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.constant.CardConstant;
import com.kw.gdx.asset.Asset;
import com.kw.gdx.utils.ImageUtils;
import com.utils.CardModel;
import com.utils.SpiderStack;

public class CardActor extends Image {
    final CardModel card;
    private boolean isFaceUp;

    public CardActor(CardModel card) {
        super(card.isFaceUp() ?
                Asset.getAsset().getTexture("card/CARD" + ((card.getSuit() - 1) * 13 + card.getRank()) + ".png")
                : Asset.getAsset().getTexture("cardback.png"));
        this.card = card;
        this.isFaceUp = card.isFaceUp();
        setSize(CardConstant.CARD_W, CardConstant.CARD_H);
    }

    public void checkFaceUp(){
        if (isFaceUp!=card.isFaceUp()) {
            float x = getX(Align.center);
            float y = getY(Align.center);
            isFaceUp = card.isFaceUp();
            ImageUtils.changeImageTexture(this,card.isFaceUp() ?
                    Asset.getAsset().getTexture("card/CARD" + ((card.getSuit() - 1) * 13 + card.getRank()) + ".png")
                    : Asset.getAsset().getTexture("cardback.png"));
            setSize(CardConstant.CARD_W, CardConstant.CARD_H);
            setPosition(x,y,Align.center);
        }
    }

    public CardModel getCard() {
        return card;
    }

    private SpiderStack ownStack;
    public void setOwnStack(SpiderStack stack) {
        this.ownStack = stack;
    }

    public SpiderStack getOwnStack() {
        return ownStack;
    }

    public void zhuan() {
        setOrigin(Align.center);
        addAction(Actions.sequence(
                Actions.scaleTo(0,1,0.2f, Interpolation.circle),
                Actions.run(()->{
                    card.setFaceUp(true);
                    checkFaceUp();
                }),
                Actions.scaleTo(1,1,0.2f,Interpolation.circle)
        ));
    }

    /** 临时显示牌背（不改model），用于发牌动画起始 */
    public void showBack() {
        isFaceUp = false;
        ImageUtils.changeImageTexture(this, Asset.getAsset().getTexture("cardback.png"));
        setSize(CardConstant.CARD_W, CardConstant.CARD_H);
    }

    /** 翻转动画：从牌背翻到正面（model已经是faceUp=true） */
    public void flipToFace() {
        setOrigin(Align.center);
        addAction(Actions.sequence(
                Actions.scaleTo(0, 1, 0.15f, Interpolation.circle),
                Actions.run(() -> {
                    isFaceUp = true;
                    ImageUtils.changeImageTexture(this,
                            Asset.getAsset().getTexture("card/CARD" + ((card.getSuit() - 1) * 13 + card.getRank()) + ".png"));
                    setSize(CardConstant.CARD_W, CardConstant.CARD_H);
                }),
                Actions.scaleTo(1, 1, 0.15f, Interpolation.circle)
        ));
    }
}
