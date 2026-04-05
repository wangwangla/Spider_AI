package com.tony.balatro.resource;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.kw.gdx.asset.Asset;
import com.tony.balatro.utils.TextureSplitUtil;

public class TextureResource {
    //cardBG Enhancers
    public static TextureRegion [][] enhancers = TextureSplitUtil.split(Asset.getAsset().getTexture("texture/Enhancers.png"), 7,5);
    //cardBG 8BitDeck
    public static TextureRegion [][]  decks = TextureSplitUtil.split(Asset.getAsset().getTexture("texture/8BitDeck.png"), 13,4);
    //    gamepad_ui
    public static TextureRegion [][]  gamePadUI = TextureSplitUtil.split(Asset.getAsset().getTexture("texture/gamepad_ui.png"), 19,4);

}

