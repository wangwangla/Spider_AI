package com.tony.balatro;

import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.kw.gdx.BaseBaseGame;
import com.kw.gdx.asset.Asset;
import com.kw.gdx.constant.Constant;
import com.kw.gdx.resource.annotation.GameInfo;
import com.kw.gdx.utils.log.NLog;
import com.tony.balatro.screen.LoadingScreen;

@GameInfo(width = 1920, height = 1080, batch = Constant.COUPOLYGONBATCH)
public class BalatroBaseGame extends BaseBaseGame {
    public BalatroBaseGame() {
        otherDispose();
    }

    @Override
    public void create() {
        super.create();
    }

    @Override
    protected void loadingView() {
        super.loadingView();
        setScreen(LoadingScreen.class);
    }

    @Override
    protected void initViewport() {
        stageViewport = new ExtendViewport(Constant.WIDTH, Constant.HIGHT);
        Constant.camera = stageViewport.getCamera();
        Constant.camera.far = 7000;
        NLog.i("stageViewport :" + Constant.WIDTH + "," + Constant.HIGHT);
        NLog.i("camera far :" + 5000);
    }

    @Override
    protected void otherDispose() {
        Asset.disposeNull();
    }

}
