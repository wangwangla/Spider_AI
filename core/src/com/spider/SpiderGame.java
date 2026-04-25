package com.spider;

import com.kw.gdx.BaseBaseGame;
import com.kw.gdx.resource.annotation.GameInfo;
import com.kw.gdx.resource.annotation.ScreenResource;

@GameInfo(width = 1920, height = 1080)
public class SpiderGame extends BaseBaseGame {
    /** 花色数：1=单色, 2=双色, 4=四色 */
    public static int suitMode = 1;

    @Override
    protected void loadingView() {
        setScreen(MainMenuScreen.class);
    }
}
