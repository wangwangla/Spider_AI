package com.tony.balatro.view;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.tony.balatro.benum.CardSuitEnum;
import com.tony.balatro.benum.CardValueEnum;
import com.tony.balatro.resource.TextureResource;

public class CardView extends Group {
    private Image cardBg;
    private Image deckValue;
    public CardView(CardSuitEnum suitEnum, CardValueEnum value){
        TextureRegion[][] cardBgRegions = TextureResource.enhancers;
        TextureRegion[][] decks = TextureResource.decks;
        cardBg = new Image(cardBgRegions[0][1]);
        addActor(cardBg);
        setSize(cardBg.getWidth(),cardBg.getHeight());
        deckValue = new Image(decks[suitEnum.getResRowIndex()][value.getResIndex()]);
        addActor(deckValue);
    }
}
