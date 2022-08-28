package com.spider.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.spider.SpiderGame;
import com.spider.config.Configuration;
import com.spider.manager.GameManager;

public class GameScreen extends ScreenAdapter {
    private Stage stage;
    private GameManager manager;
    public GameScreen(){
        stage = new Stage(SpiderGame.getViewport(),SpiderGame.getBatch());
    }

    @Override
    public void show() {
        super.show();
        manager = new GameManager();
        manager.setSoundId();
        manager.setGuiProperty();
        Configuration configuration = new Configuration();
        configuration.readFromFile("");

    }

    @Override
    public void render(float delta) {
        super.render(delta);
        stage.act();
        stage.draw();
    }
}
