package com.tony.balatro.screen;

import com.kw.gdx.BaseBaseGame;
import com.kw.gdx.resource.annotation.ScreenResource;
import com.kw.gdx.screen.BaseScreen;
import com.tony.balatro.bg.BgManager;

@ScreenResource("cocos/GameScene.json")
public class GameScreen extends BaseScreen {
    public GameScreen(BaseBaseGame game) {
        super(game);
    }

    @Override
    public void initView() {
        super.initView();
        BgManager.getBgManager().showBg(stage);
    }
}
