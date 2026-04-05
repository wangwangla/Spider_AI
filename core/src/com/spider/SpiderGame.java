package com.spider;

import com.kw.gdx.BaseBaseGame;
import com.kw.gdx.resource.annotation.GameInfo;
import com.kw.gdx.resource.annotation.ScreenResource;

@GameInfo(width = 1920, height = 1080)
public class SpiderGame extends BaseBaseGame {
    @Override
    protected void loadingView() {
        setScreen(SpiderScreen.class);
    }
}
