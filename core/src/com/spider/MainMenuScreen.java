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
 * 主菜单 - 选择游戏类型
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

        Label.LabelStyle descStyle = new Label.LabelStyle(
                Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt"), Color.LIGHT_GRAY);

        Label title = new Label("Solitaire Collection", titleStyle);
        title.setFontScale(1.5f);

        // ---- Spider 部分 ----
        Label spiderTitle = new Label("-- Spider --", titleStyle);

        TextButton btn1Suit = new TextButton("1 Suit  (Easy)", style);
        btn1Suit.addListener(startSpider(1));

        TextButton btn2Suit = new TextButton("2 Suits (Medium)", style);
        btn2Suit.addListener(startSpider(2));

        TextButton btn4Suit = new TextButton("4 Suits (Hard)", style);
        btn4Suit.addListener(startSpider(4));

        Label desc1 = new Label("All Spades", descStyle);
        Label desc2 = new Label("Spades + Hearts", descStyle);
        Label desc4 = new Label("All Four Suits", descStyle);

        // ---- Freecell 部分 ----
        Label freecellTitle = new Label("-- Freecell --", titleStyle);

        TextButton btnFreecell = new TextButton("Play Freecell", style);
        btnFreecell.addListener(startFreecell());

        Label descFC = new Label("Classic 52-card Freecell", descStyle);

        // ---- Pyramid 部分 ----
        Label pyramidTitle = new Label("-- Pyramid --", titleStyle);

        TextButton btnPyramid = new TextButton("Play Pyramid", style);
        btnPyramid.addListener(startPyramid());

        Label descPY = new Label("Remove pairs summing to 13", descStyle);

        // ---- Klondike 部分 ----
        Label klondikeTitle = new Label("-- Klondike --", titleStyle);

        TextButton btnKlondike = new TextButton("Play Klondike", style);
        btnKlondike.addListener(startKlondike());

        Label descKL = new Label("Classic draw-1 Solitaire", descStyle);

        // ---- 布局 ----
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        table.add(title).colspan(2).padBottom(50).row();

        table.add(spiderTitle).colspan(2).padBottom(15).row();

        table.add(btn1Suit).width(400).height(70).padBottom(8);
        table.add(desc1).padLeft(20).padBottom(8).row();

        table.add(btn2Suit).width(400).height(70).padBottom(8);
        table.add(desc2).padLeft(20).padBottom(8).row();

        table.add(btn4Suit).width(400).height(70).padBottom(8);
        table.add(desc4).padLeft(20).padBottom(8).row();

        table.add(freecellTitle).colspan(2).padTop(30).padBottom(15).row();

        table.add(btnFreecell).width(400).height(70).padBottom(8);
        table.add(descFC).padLeft(20).padBottom(8).row();

        table.add(pyramidTitle).colspan(2).padTop(30).padBottom(15).row();

        table.add(btnPyramid).width(400).height(70).padBottom(8);
        table.add(descPY).padLeft(20).padBottom(8).row();

        table.add(klondikeTitle).colspan(2).padTop(30).padBottom(15).row();

        table.add(btnKlondike).width(400).height(70).padBottom(8);
        table.add(descKL).padLeft(20).padBottom(8).row();

        rootView.addActor(table);
    }

    private InputListener startSpider(final int suitMode) {
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

    private InputListener startFreecell() {
        return new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                setScreen(FreecellScreen.class);
            }
        };
    }

    private InputListener startPyramid() {
        return new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                setScreen(PyramidScreen.class);
            }
        };
    }

    private InputListener startKlondike() {
        return new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                setScreen(KlondikeScreen.class);
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
