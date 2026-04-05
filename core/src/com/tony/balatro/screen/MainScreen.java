package com.tony.balatro.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.kw.gdx.BaseBaseGame;
import com.kw.gdx.listener.OrdinaryButtonListener;
import com.kw.gdx.resource.annotation.ScreenResource;
import com.kw.gdx.screen.BaseScreen;
import com.tony.balatro.bg.BgManager;
import com.tony.balatro.migration.LuaProjectMirror;
import com.tony.balatro.migration.LuaProjectSummary;
import com.tony.balatro.view.IconCardGroup;

@ScreenResource("cocos/MainScene.json")
public class MainScreen extends BaseScreen {
    private BitmapFont migrationFont;

    public MainScreen(BaseBaseGame game) {
        super(game);
    }

    @Override
    public void initView() {
        super.initView();
        BgManager.getBgManager().showBg(stage);
        IconCardGroup iconCardGroup = new IconCardGroup();
        iconCardGroup.setPosition(960,640+50, Align.center);
        rootView.addActor(iconCardGroup);


        Group mainPanel = rootView.findActor("mainPanel");
        Actor playerBtn = mainPanel.findActor("playerBtn");
        playerBtn.addListener(new OrdinaryButtonListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                setScreen(GameScreen.class);
            }
        });

        migrationFont = new BitmapFont();
        migrationFont.getData().setScale(1.0f);

        LuaProjectSummary summary = LuaProjectMirror.getSummary();
        Label.LabelStyle statusStyle = new Label.LabelStyle(migrationFont, Color.WHITE);
        Label statusLabel = new Label(summary.toStatusLine(), statusStyle);
        statusLabel.setPosition(40, 70);
        statusLabel.setAlignment(Align.left);
        rootView.addActor(statusLabel);

        Label.LabelStyle entryStyle = new Label.LabelStyle(migrationFont, Color.LIGHT_GRAY);
        Label entryLabel = new Label(summary.toEntryLine(), entryStyle);
        entryLabel.setPosition(40, 40);
        entryLabel.setAlignment(Align.left);
        rootView.addActor(entryLabel);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (migrationFont != null) {
            migrationFont.dispose();
            migrationFont = null;
        }
    }

}
