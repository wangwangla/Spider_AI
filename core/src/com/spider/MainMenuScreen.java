package com.spider;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.kw.gdx.BaseBaseGame;
import com.kw.gdx.asset.Asset;
import com.kw.gdx.constant.Constant;
import com.kw.gdx.screen.BaseScreen;

/**
 * 主菜单 - 选择花色数（1色/2色/4色）
 */
public class MainMenuScreen extends BaseScreen {

    public MainMenuScreen(BaseBaseGame baseBaseGame) {
        super(baseBaseGame);
    }

    @Override
    public void initView() {
        super.initView();
        initBg();
        initMenu();
    }

    private void initBg() {
        Texture background = Asset.getAsset().getTexture("background.png");
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        TextureRegion region = new TextureRegion(background);
        region.setRegionWidth((int) (Constant.GAMEWIDTH + 0.5f));
        region.setRegionHeight((int) (Constant.GAMEHIGHT + 0.5f));
        Image bg = new Image(region);
        bg.setPosition(960, 540, Align.center);
        rootView.addActor(bg);
    }

    private void initMenu() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(
                Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt"));
        style.fontColor = Color.WHITE;

        Label.LabelStyle titleStyle = new Label.LabelStyle(
                Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt"), Color.WHITE);

        Label title = new Label("Spider Solitaire", titleStyle);
        title.setFontScale(1.5f);

        TextButton btn1Suit = new TextButton("1 Suit  (Easy)", style);
        btn1Suit.addListener(startGame(1));

        TextButton btn2Suit = new TextButton("2 Suits (Medium)", style);
        btn2Suit.addListener(startGame(2));

        TextButton btn4Suit = new TextButton("4 Suits (Hard)", style);
        btn4Suit.addListener(startGame(4));

        // 花色预览标签
        Label.LabelStyle descStyle = new Label.LabelStyle(
                Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt"), Color.LIGHT_GRAY);

        Label desc1 = new Label("All Spades", descStyle);
        Label desc2 = new Label("Spades + Hearts", descStyle);
        Label desc4 = new Label("All Four Suits", descStyle);

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        table.add(title).colspan(2).padBottom(80).row();

        table.add(btn1Suit).width(400).height(80).padBottom(10);
        table.add(desc1).padLeft(20).padBottom(10).row();

        table.add(btn2Suit).width(400).height(80).padBottom(10);
        table.add(desc2).padLeft(20).padBottom(10).row();

        table.add(btn4Suit).width(400).height(80).padBottom(10);
        table.add(desc4).padLeft(20).padBottom(10).row();

        rootView.addActor(table);
    }

    private InputListener startGame(final int suitMode) {
        return new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                SpiderGame.suitMode = suitMode;
                setScreen(SpiderScreen.class);
            }
        };
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
}
