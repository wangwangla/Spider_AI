package com.spider;

import com.kw.gdx.BaseBaseGame;

public class SpiderGame extends BaseBaseGame {
    @Override
    protected void loadingView() {
        setScreen(SpiderScreen.class);
    }
}
