package com.spider.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.spider.SpiderGame;
import com.spider.config.Configuration;
import com.spider.constant.Constant;
import com.spider.constant.ResourceConstant;
import com.spider.log.NLog;
import com.spider.manager.GameManager;
import com.spider.manager.ReleaseCorner;

public class GameScreen extends ScreenAdapter {
    private Stage stage;
    private GameManager manager;
    private Group cardGroup;
    private Group finishGroup;
    private Group sendCardGroup;

    public GameScreen(){
        NLog.e("create gameScreen !");
        stage = new Stage(SpiderGame.getViewport(),SpiderGame.getBatch());
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void show() {
        super.show();
        showBg();
        showGameGroup();
        initManager();
        initConfig();
    }

    private void initConfig() {
        Configuration configuration = new Configuration();
        configuration.readFromFile("");
    }

    private void showGameGroup() {
        cardGroup = new Group();
        cardGroup.setSize(Constant.worldWidth,92F);
        cardGroup.setY(Constant.worldHeight-100);
        cardGroup.setDebug(true);
        finishGroup = new Group();
        finishGroup.setSize(76,92);
        finishGroup.setDebug(true);
        finishGroup.setPosition(100,20);
        sendCardGroup = new Group();
        sendCardGroup.setSize(76,92);
        sendCardGroup.setPosition(Constant.worldWidth - 100,20);
        sendCardGroup.setDebug(true);
        stage.addActor(cardGroup);
        stage.addActor(finishGroup);
        stage.addActor(sendCardGroup);
    }

    private void initManager() {
        NLog.e("init manager");
        manager = new GameManager(cardGroup,finishGroup,sendCardGroup);
        manager.setSoundId();
        manager.setGuiProperty(
                ResourceConstant.IDB_CARDEMPTY,
                ResourceConstant.IDB_CARDBACK,
                ResourceConstant.IDB_CARD1,
                ResourceConstant.IDB_CARDMASK);
        manager.newGame(1);

        stage.addAction(Actions.delay(3,Actions.run(new Runnable() {
            @Override
            public void run() {

                manager.AutoSolve(false);

            }
        })));
        final Vector2 tempV = new Vector2();
        sendCardGroup.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                manager.faPai();
            }
        });
        stage.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                manager.touchDown(event.getTarget());
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                tempV.set(x,y);
                manager.OnMouseMove(tempV);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                tempV.set(x,y);
                manager.OnLButtonUp(tempV);
            }
        });
    }

    private void showBg() {
        NLog.e("show Bg ");
        Texture texture = SpiderGame.getAssetUtil().loadTexture("Resource/background.png");
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        TextureRegion region = new TextureRegion(texture);
        region.setRegionWidth((int) Constant.worldWidth);
        region.setRegionHeight((int) Constant.worldHeight);
        Image image = new Image(region);
        stage.addActor(image);
        image.setSize(Constant.worldWidth,Constant.worldHeight);
        image.setPosition(Constant.worldWidth/2,Constant.worldHeight/2, Align.center);

    }

    @Override
    public void render(float delta) {
        super.render(delta);
        stage.act();
        stage.draw();
    }
}
