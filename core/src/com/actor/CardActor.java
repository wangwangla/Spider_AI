package com.actor;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.constant.CardConstant;
import com.kw.gdx.asset.Asset;
import com.utils.CardModel;
import com.utils.SpiderStack;

public class CardActor extends Image {
    final CardModel card;

    public CardActor(CardModel card) {
        super(card.isFaceUp() ?
                Asset.getAsset().getTexture("card/CARD" + ((card.getSuit() - 1) * 13 + card.getRank()) + ".png")
                : Asset.getAsset().getTexture("cardback.png"));
        this.card = card;
        setSize(CardConstant.CARD_W, CardConstant.CARD_H);
        setTouchable(Touchable.disabled);
         // we handle input centrally
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
}
