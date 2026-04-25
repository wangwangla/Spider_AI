package com.spider;

import com.actor.CardActor;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.constant.CardConstant;
import com.kw.gdx.BaseBaseGame;
import com.kw.gdx.asset.Asset;
import com.kw.gdx.constant.Constant;
import com.kw.gdx.screen.BaseScreen;
import com.solvitaire.app.DealShuffler;
import com.utils.CardModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Pyramid Solitaire
 * - 28 cards in a 7-row pyramid, 24 cards in stock
 * - Remove pairs that sum to 13 (K removed alone)
 * - Only exposed (uncovered) cards can be selected
 */
public class PyramidScreen extends BaseScreen {

    private static final int PYRAMID_ROWS = 7;
    private static final float CARD_W = CardConstant.CARD_W;
    private static final float CARD_H = CardConstant.CARD_H;

    // pyramid[row][col], null = removed
    private CardModel[][] pyramid;
    // stock & waste
    private List<CardModel> stock;
    private List<CardModel> waste;
    // discard pile (removed pairs)
    private List<CardModel> discarded;

    private HashMap<CardModel, CardActor> cardActorMap;
    private Group gamePanel;
    private Label statusLabel;

    // slot images
    private Image stockSlot;
    private Image wasteSlot;

    // 选中的第一张牌
    private CardModel selectedCard;
    private CardActor selectedActor;

    // 布局
    private float pyramidTopY;
    private float pyramidCenterX;
    private float stockX, stockY, wasteX;

    // 重发次数限制
    private int recycleCount;
    private static final int MAX_RECYCLE = 2;

    public PyramidScreen(BaseBaseGame baseBaseGame) {
        super(baseBaseGame);
    }

    @Override
    protected void initData() {
        pyramid = new CardModel[PYRAMID_ROWS][];
        for (int row = 0; row < PYRAMID_ROWS; row++) {
            pyramid[row] = new CardModel[row + 1];
        }
        stock = new ArrayList<>();
        waste = new ArrayList<>();
        discarded = new ArrayList<>();
        cardActorMap = new HashMap<>(52);
    }

    @Override
    public void initView() {
        super.initView();
        initBg();
        initLayout();
        newGame();
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

    private void initLayout() {
        gamePanel = new Group();
        gamePanel.setSize(Constant.GAMEWIDTH, Constant.GAMEHIGHT);
        rootView.addActor(gamePanel);

        pyramidCenterX = Constant.GAMEWIDTH / 2f;
        pyramidTopY = Constant.GAMEHIGHT - 80;

        // Stock & waste at bottom left
        stockX = 200;
        stockY = 100 + CARD_H / 2f;
        wasteX = stockX + CARD_W + 30;

        stockSlot = makeSlot(0x4E342E);
        stockSlot.setPosition(stockX, stockY, Align.center);
        gamePanel.addActor(stockSlot);

        wasteSlot = makeSlot(0x263238);
        wasteSlot.setPosition(wasteX, stockY, Align.center);
        gamePanel.addActor(wasteSlot);

        // Status label
        statusLabel = new Label("Ready", new Label.LabelStyle(
                Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt"), Color.WHITE));
        statusLabel.setAlignment(Align.center);
        statusLabel.setPosition(Constant.GAMEWIDTH / 2f, 30, Align.center);
        gamePanel.addActor(statusLabel);

        // Top bar buttons
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(
                Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt"));
        style.fontColor = Color.WHITE;

        TextButton menuBtn = new TextButton("MENU", style);
        menuBtn.addListener(simpleClick(() -> setScreen(MainMenuScreen.class)));

        TextButton newBtn = new TextButton("NEW", style);
        newBtn.addListener(simpleClick(() -> setScreen(PyramidScreen.class)));

        Table bar = new Table();
        bar.top().right().pad(10);
        bar.add(menuBtn).pad(4);
        bar.add(newBtn).pad(4);
        bar.pack();
        bar.setPosition(Constant.GAMEWIDTH - 20, Constant.GAMEHIGHT, Align.topRight);
        rootView.addActor(bar);

        gamePanel.addListener(new PyramidInputListener());
    }

    private Image makeSlot(int rgb) {
        Image img = new Image(Asset.getAsset().getTexture("white.png"));
        img.setColor(new Color(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f,
                (rgb & 0xFF) / 255f, 0.6f));
        img.setSize(CARD_W, CARD_H);
        return img;
    }

    private InputListener simpleClick(Runnable action) {
        return new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                action.run();
            }
        };
    }

    // ==================== Game Logic ====================

    private void newGame() {
        // Clear
        for (int row = 0; row < PYRAMID_ROWS; row++) {
            for (int col = 0; col <= row; col++) {
                pyramid[row][col] = null;
            }
        }
        stock.clear();
        waste.clear();
        discarded.clear();
        selectedCard = null;
        selectedActor = null;
        recycleCount = 0;

        for (CardActor actor : cardActorMap.values()) {
            actor.remove();
        }
        cardActorMap.clear();

        int[] deck = DealShuffler.shuffleSingleDeck(new Random().nextLong());
        int idx = 0;

        // Deal pyramid: 28 cards
        for (int row = 0; row < PYRAMID_ROWS; row++) {
            for (int col = 0; col <= row; col++) {
                int code = deck[idx++];
                int suit = code / 100;
                int rank = code % 100;
                CardModel card = new CardModel(code, suit, rank, true);
                pyramid[row][col] = card;
            }
        }

        // Remaining 24 cards into stock (face down)
        for (; idx < 52; idx++) {
            int code = deck[idx];
            int suit = code / 100;
            int rank = code % 100;
            CardModel card = new CardModel(code, suit, rank, false);
            stock.add(card);
        }

        createAllActors();
        refreshLayout(true);
        statusLabel.setText("Pyramid - Remove pairs summing to 13");
    }

    private void createAllActors() {
        for (int row = 0; row < PYRAMID_ROWS; row++) {
            for (int col = 0; col <= row; col++) {
                CardModel card = pyramid[row][col];
                if (card != null) {
                    CardActor actor = new CardActor(card);
                    cardActorMap.put(card, actor);
                    gamePanel.addActor(actor);
                }
            }
        }
        for (CardModel card : stock) {
            CardActor actor = new CardActor(card);
            cardActorMap.put(card, actor);
            gamePanel.addActor(actor);
        }
    }

    private float[] getPyramidPos(int row, int col) {
        float colSpacing = CARD_W + 10;
        float rowSpacing = CARD_H * 0.45f;
        float rowWidth = (row + 1) * colSpacing;
        float startX = pyramidCenterX - rowWidth / 2f + colSpacing / 2f;
        float x = startX + col * colSpacing;
        float y = pyramidTopY - row * rowSpacing;
        return new float[]{x, y};
    }

    private void refreshLayout(boolean create) {
        // Pyramid cards
        for (int row = 0; row < PYRAMID_ROWS; row++) {
            for (int col = 0; col <= row; col++) {
                CardModel card = pyramid[row][col];
                if (card == null) continue;
                CardActor actor = cardActorMap.get(card);
                if (actor == null) continue;
                actor.clearActions();
                actor.checkFaceUp();
                float[] pos = getPyramidPos(row, col);
                if (create) {
                    actor.setPosition(pyramidCenterX, Constant.GAMEHIGHT + 50, Align.center);
                    actor.addAction(Actions.sequence(
                            Actions.delay(row * 0.08f + col * 0.04f),
                            Actions.moveToAligned(pos[0], pos[1], Align.top, 0.2f)
                    ));
                } else {
                    actor.setPosition(pos[0], pos[1], Align.top);
                }
                // Bring bottom rows to front
                actor.toFront();
            }
        }

        // Stock pile
        for (int i = 0; i < stock.size(); i++) {
            CardModel card = stock.get(i);
            CardActor actor = cardActorMap.get(card);
            if (actor == null) continue;
            actor.clearActions();
            actor.checkFaceUp();
            if (create) {
                actor.setPosition(stockX, -50, Align.center);
                actor.addAction(Actions.sequence(
                        Actions.delay(1.5f + i * 0.02f),
                        Actions.moveToAligned(stockX, stockY, Align.center, 0.15f)
                ));
            } else {
                actor.setPosition(stockX, stockY, Align.center);
            }
            actor.toFront();
        }

        // Waste pile
        refreshWasteDisplay();

        updateSelection();
    }

    private void refreshLayout() {
        refreshLayout(false);
    }

    private void refreshWasteDisplay() {
        // Show top waste card
        for (int i = 0; i < waste.size(); i++) {
            CardModel card = waste.get(i);
            CardActor actor = cardActorMap.get(card);
            if (actor == null) continue;
            actor.clearActions();
            actor.checkFaceUp();
            actor.setPosition(wasteX, stockY, Align.center);
            actor.toFront();
        }
    }

    /** 检查金字塔中的牌是否暴露（没有被下面行的牌覆盖） */
    private boolean isExposed(int row, int col) {
        if (pyramid[row][col] == null) return false;
        if (row == PYRAMID_ROWS - 1) return true; // 最底行总是暴露
        // 被下一行的 [row+1][col] 和 [row+1][col+1] 覆盖
        boolean leftCover = pyramid[row + 1][col] != null;
        boolean rightCover = pyramid[row + 1][col + 1] != null;
        return !leftCover && !rightCover;
    }

    /** 找到牌在金字塔中的位置 */
    private int[] findInPyramid(CardModel card) {
        for (int row = 0; row < PYRAMID_ROWS; row++) {
            for (int col = 0; col <= row; col++) {
                if (pyramid[row][col] == card) return new int[]{row, col};
            }
        }
        return null;
    }

    /** 从牌堆中移除一张牌 */
    private void removeCard(CardModel card) {
        int[] pos = findInPyramid(card);
        if (pos != null) {
            pyramid[pos[0]][pos[1]] = null;
        } else if (!waste.isEmpty() && waste.get(waste.size() - 1) == card) {
            waste.remove(waste.size() - 1);
        }
        discarded.add(card);
        CardActor actor = cardActorMap.get(card);
        if (actor != null) {
            actor.clearActions();
            actor.addAction(Actions.sequence(
                    Actions.parallel(
                            Actions.moveToAligned(Constant.GAMEWIDTH - 150, stockY, Align.center, 0.3f),
                            Actions.fadeOut(0.3f)
                    ),
                    Actions.run(() -> actor.remove())
            ));
        }
    }

    /** 翻股牌 */
    private void drawFromStock() {
        if (!stock.isEmpty()) {
            CardModel card = stock.remove(stock.size() - 1);
            card.setFaceUp(true);
            waste.add(card);
            CardActor actor = cardActorMap.get(card);
            if (actor != null) {
                actor.clearActions();
                actor.checkFaceUp();
                actor.toFront();
                actor.addAction(Actions.moveToAligned(wasteX, stockY, Align.center, 0.15f));
            }
            updateStockDisplay();
        } else if (!waste.isEmpty() && recycleCount < MAX_RECYCLE) {
            // Recycle waste back to stock
            recycleCount++;
            while (!waste.isEmpty()) {
                CardModel card = waste.remove(waste.size() - 1);
                card.setFaceUp(false);
                stock.add(card);
            }
            // Animate stock cards back
            for (int i = 0; i < stock.size(); i++) {
                CardModel card = stock.get(i);
                CardActor actor = cardActorMap.get(card);
                if (actor == null) continue;
                actor.clearActions();
                actor.checkFaceUp();
                actor.addAction(Actions.moveToAligned(stockX, stockY, Align.center, 0.15f));
                actor.toFront();
            }
            statusLabel.setText("Stock recycled (" + (MAX_RECYCLE - recycleCount) + " remaining)");
        } else {
            statusLabel.setText("No more draws!");
        }
    }

    private void updateStockDisplay() {
        // stock top card stays at stockX position
    }

    private void clearSelection() {
        if (selectedActor != null) {
            selectedActor.setColor(Color.WHITE);
        }
        selectedCard = null;
        selectedActor = null;
    }

    private void updateSelection() {
        // Highlight selected card
        if (selectedActor != null) {
            selectedActor.setColor(Color.YELLOW);
        }
    }

    /** 尝试配对两张牌 */
    private boolean tryPair(CardModel card1, CardModel card2) {
        if (card1.getRank() + card2.getRank() == 13) {
            clearSelection();
            removeCard(card1);
            removeCard(card2);
            refreshWasteDisplay();
            checkWin();
            return true;
        }
        return false;
    }

    /** 检查是否胜利 */
    private void checkWin() {
        for (int row = 0; row < PYRAMID_ROWS; row++) {
            for (int col = 0; col <= row; col++) {
                if (pyramid[row][col] != null) return;
            }
        }
        statusLabel.setText("You Win! Pyramid cleared!");
    }

    /** 点击金字塔牌 */
    private void onPyramidCardClicked(CardModel card, int row, int col) {
        if (!isExposed(row, col)) return;

        // K单独移除
        if (card.getRank() == 13) {
            clearSelection();
            removeCard(card);
            checkWin();
            return;
        }

        if (selectedCard == null) {
            // 选中第一张
            selectedCard = card;
            selectedActor = cardActorMap.get(card);
            updateSelection();
        } else if (selectedCard == card) {
            // 取消选中
            clearSelection();
        } else {
            // 尝试配对
            if (!tryPair(selectedCard, card)) {
                // 配对失败，换选中
                clearSelection();
                selectedCard = card;
                selectedActor = cardActorMap.get(card);
                updateSelection();
            }
        }
    }

    /** 点击废牌堆顶牌 */
    private void onWasteCardClicked(CardModel card) {
        // K单独移除
        if (card.getRank() == 13) {
            clearSelection();
            removeCard(card);
            refreshWasteDisplay();
            return;
        }

        if (selectedCard == null) {
            selectedCard = card;
            selectedActor = cardActorMap.get(card);
            updateSelection();
        } else if (selectedCard == card) {
            clearSelection();
        } else {
            if (!tryPair(selectedCard, card)) {
                clearSelection();
                selectedCard = card;
                selectedActor = cardActorMap.get(card);
                updateSelection();
            }
        }
    }

    // ==================== Input ====================

    private class PyramidInputListener extends ClickListener {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            Actor target = event.getTarget();

            // 点击stock区域 -> 翻牌
            if (isNearStock(x, y)) {
                clearSelection();
                drawFromStock();
                return;
            }

            if (!(target instanceof CardActor)) {
                clearSelection();
                return;
            }

            CardActor cardActor = (CardActor) target;
            CardModel card = cardActor.getCard();

            // 检查是否是金字塔中的牌
            int[] pos = findInPyramid(card);
            if (pos != null) {
                onPyramidCardClicked(card, pos[0], pos[1]);
                return;
            }

            // 检查是否是waste顶牌
            if (!waste.isEmpty() && waste.get(waste.size() - 1) == card) {
                onWasteCardClicked(card);
                return;
            }

            // 检查是否是stock顶牌（点击翻牌）
            if (!stock.isEmpty() && stock.get(stock.size() - 1) == card) {
                clearSelection();
                drawFromStock();
                return;
            }
        }

        private boolean isNearStock(float x, float y) {
            return Math.abs(x - stockX) < CARD_W && Math.abs(y - stockY) < CARD_H;
        }
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
