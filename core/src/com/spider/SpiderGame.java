package com.spider;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.spider.asset.AssetUtil;
import com.spider.constant.Constant;
import com.spider.log.NLog;
import com.spider.screen.GameScreen;

public class SpiderGame extends Game {
    private static SpriteBatch batch;
    private static ExtendViewport viewport;
    private static AssetUtil assetUtil;

    @Override
    public void create() {
        NLog.e("game create");
        assetUtil = new AssetUtil();
        viewport = new ExtendViewport(640,360);
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                setScreen(new GameScreen());
            }
        });
    }

    public static ExtendViewport getViewport() {

        return viewport;
    }

    public static SpriteBatch getBatch() {
        if (batch == null){
            batch = new SpriteBatch();
        }
        return batch;
    }

    public static AssetUtil getAssetUtil() {
        return assetUtil;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width,height);
        Constant.worldHeight = viewport.getWorldHeight();
        Constant.worldWidth = viewport.getWorldWidth();
    }
}
