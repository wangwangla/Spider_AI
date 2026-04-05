package com.spider;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class GameScreen extends ScreenAdapter {
    @Override
    public void show() {
        super.show();
        showBg();
    }

    private void showBg() {
        Image bgImg = new Image();
    }
}
