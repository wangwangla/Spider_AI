package com.spider;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.spider.screen.GameScreen;

public class SpiderGame extends Game {
    private static SpriteBatch batch;
    private static ExtendViewport viewport;
    @Override
    public void create() {
        setScreen(new GameScreen());
    }

    public static ExtendViewport getViewport() {
        if (viewport == null){
            viewport = new ExtendViewport(720,1280);
        }
        return viewport;
    }

    public static SpriteBatch getBatch() {
        if (batch == null){
            batch = new SpriteBatch();
        }
        return batch;
    }
}
