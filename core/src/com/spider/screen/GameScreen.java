package com.spider.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.spider.SpiderGame;
import com.spider.constant.Constant;
import com.spider.log.NLog;
import com.spider.manager.GameManager;

public class GameScreen extends ScreenAdapter {
    private Stage stage;
    private GameManager manager;
    private Group cardGroup;
    private Group finishGroup;
    private Group sendCardGroup;
    private final Vector2 touchDownPos;

    public GameScreen(){
        NLog.e("create gameScreen !");
        //点击下的位置
        touchDownPos = new Vector2();
        stage = new Stage(SpiderGame.getViewport(),SpiderGame.getBatch());
        Gdx.input.setInputProcessor(stage);
    }


    @Override
    public void show() {
        super.show();
        showBg();
        showGameGroup();
        initManager();
        stage.addAction(Actions.delay(0.2f,Actions.run(()->{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    manager.auto();
                }
            }).start();
/*
            stage.addAction(Actions.delay(2,Actions.forever(Actions.delay(1,Actions.run(()->{
                manager.nextStep();
            })))));*/
        })));


        Image image = new Image(new Texture("Resource/cardmask.png"));
        stage.addActor(image);
        image.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                manager.nextStep();
            }
        });
    }

    private void showGameGroup() {
        cardGroup = new Group();
        cardGroup.setSize(Constant.worldWidth,92F);
        cardGroup.setY(Constant.worldHeight-100);

        finishGroup = new Group();
        finishGroup.setSize(76,92);
        finishGroup.setPosition(100,20);

        sendCardGroup = new Group();
        sendCardGroup.setSize(76,92);
        sendCardGroup.setPosition(Constant.worldWidth - 100,20);

        Image image = new Image(SpiderGame.getAssetUtil().loadTexture("Resource/cardmask.png"));
        image.setSize(100,100);
        stage.addActor(image);
        image.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                manager.recod();
            }
        });
        image.setPosition(92,92);
        stage.addActor(sendCardGroup);
        stage.addActor(finishGroup);
        stage.addActor(cardGroup);
    }

    private void initManager() {
        NLog.e("init manager");
        manager = new GameManager(cardGroup,finishGroup,sendCardGroup);
        manager.setGuiProperty();
        manager.newGame(1);
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
                manager.touchDown(event.getTarget(),x,y);
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                touchDownPos.set(x,y);
                manager.OnMouseMove(touchDownPos);
            }

            @Override
            public void cancel() {
                super.cancel();
                manager.GiveUpDrag();
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                manager.OnLButtonUp();
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
