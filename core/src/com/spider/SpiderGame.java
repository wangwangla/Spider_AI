package com.spider;

import com.kw.gdx.BaseBaseGame;
import com.badlogic.gdx.ScreenAdapter;

public class SpiderGame extends BaseBaseGame {
    @Override
    protected void loadingView() {
        setScreen(new SpiderScreen());
    }
}
