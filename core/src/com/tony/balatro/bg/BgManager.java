package com.tony.balatro.bg;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.kw.gdx.asset.Asset;
import com.kw.gdx.constant.Constant;
import com.tony.balatro.GameBgAnimation;

public class BgManager {
    private static BgManager bgManager;
    private GameBgAnimation gameBgAnimation;
    public void showBg(Stage stage){
        if (gameBgAnimation == null){
            gameBgAnimation = new GameBgAnimation();
            stage.addActor(gameBgAnimation);
            Image image = new Image(Asset.getAsset().getTexture("000.png"));
            gameBgAnimation.addActor(image);
            image.setOrigin(Align.center);
            image.setScale(Math.max(Constant.GAMEWIDTH/image.getWidth(),Constant.GAMEHIGHT/image.getHeight()) * 2);
            gameBgAnimation.setPosition(Constant.GAMEWIDTH/2f,Constant.GAMEHIGHT/2f,Align.center);
            image.setPosition(gameBgAnimation.getWidth()/2f, gameBgAnimation.getHeight()/2f,Align.center);
            image.setDebug(true);
        }else {
            stage.addActor(gameBgAnimation);
        }
        gameBgAnimation.toBack();
    }

    public static BgManager getBgManager() {
        if (bgManager == null){
            bgManager = new BgManager();
        }
        return bgManager;
    }
}
