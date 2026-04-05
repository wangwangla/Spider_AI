package com.tony.balatro.view;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.kw.gdx.asset.Asset;
import com.tony.balatro.benum.CardSuitEnum;
import com.tony.balatro.benum.CardValueEnum;

public class IconCardGroup extends Group {
    public IconCardGroup(){
        Image logoImg = new Image(Asset.getAsset().getTexture("texture/balatro.png"));
        addActor(logoImg);
        setSize(logoImg.getWidth(),logoImg.getHeight());

        CardView cardView = new CardView(CardSuitEnum.Spades,CardValueEnum.ACE);
        addActor(cardView);
        cardView.setPosition(getWidth()/2f,getHeight()/2f,Align.center);
    }
}
