package com.spider;

import com.actor.CardActor;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.constant.CardConstant;
import com.kw.gdx.BaseBaseGame;
import com.kw.gdx.asset.Asset;
import com.kw.gdx.constant.Constant;
import com.kw.gdx.screen.BaseScreen;
import com.solvitaire.app.DealCardCodec;
import com.solvitaire.app.DealShuffler;
import com.solvitaire.app.KlondikeSolutionStep;
import com.solvitaire.app.KlondikeSolveResult;
import com.solvitaire.app.KlondikeSolverService;
import com.utils.CardDrag;
import com.utils.CardModel;
import com.utils.SpiderStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Klondike Solitaire
 * - 7 tableau columns (col i has i+1 cards, top face up)
 * - Stock pile, waste pile (draw 1)
 * - 4 foundation piles (build up by suit, A→K)
 * - Tableau: build descending alternating color
 */
public class KlondikeScreen extends BaseScreen {

    private static final int TABLEAU_COUNT = 7;
    private static final int FOUNDATION_COUNT = 4;
    private static final float CARD_W = CardConstant.CARD_W;
    private static final float CARD_H = CardConstant.CARD_H;
    private static final float ROW_GAP = CardConstant.ROW_GAP;
    private static final float FACEDOWN_GAP = 10;

    // 7 tableau columns
    private List<SpiderStack> tableau;
    // 4 foundations
    private List<List<CardModel>> foundations;
    // stock & waste
    private List<CardModel> stock;
    private List<CardModel> waste;

    private HashMap<CardModel, CardActor> cardActorMap;
    private Group gamePanel;
    private Group solverPanel;
    private Label statusLabel;

    // slot images
    private Image[] foundationSlots;
    private Image[] tableauSlots;
    private Image stockSlot;
    private Image wasteSlot;

    // layout
    private float colGap;
    private float leftX;
    private float topY;
    private float tableauTopY;
    private float stockX, stockY, wasteX;

    // recycle
    private int recycleCount;
    private static final int MAX_RECYCLE = -1; // unlimited

    // solver
    private KlondikeSolverService solverService;
    private List<KlondikeSolutionStep> solutionSteps;
    private int currentStepIndex = 0;
    private boolean autoPlay = false;
    private float autoTimer = 0f;
    private float autoDelay = 0.5f;
    private Table stepListTable;
    private ScrollPane stepScrollPane;
    private List<Label> stepLabels = new ArrayList<>();
    private Deque<BoardSnapshot> undoStack = new ArrayDeque<>();

    // 保存发牌种子以便solver重建
    private long currentSeed;

    /** 棋盘快照 */
    private static class BoardSnapshot {
        final List<List<int[]>> tableauCards;
        final List<List<int[]>> foundationCards;
        final List<int[]> stockCards;
        final List<int[]> wasteCards;
        final int stepIndex;

        BoardSnapshot(List<SpiderStack> tableau, List<List<CardModel>> foundations,
                      List<CardModel> stock, List<CardModel> waste, int stepIndex) {
            this.stepIndex = stepIndex;
            this.tableauCards = new ArrayList<>();
            for (SpiderStack s : tableau) {
                List<int[]> col = new ArrayList<>();
                for (CardModel c : s.getCards()) {
                    col.add(new int[]{c.getCode(), c.getSuit(), c.getRank(), c.isFaceUp() ? 1 : 0});
                }
                tableauCards.add(col);
            }
            this.foundationCards = new ArrayList<>();
            for (List<CardModel> pile : foundations) {
                List<int[]> f = new ArrayList<>();
                for (CardModel c : pile) {
                    f.add(new int[]{c.getCode(), c.getSuit(), c.getRank(), 1});
                }
                foundationCards.add(f);
            }
            this.stockCards = new ArrayList<>();
            for (CardModel c : stock) {
                stockCards.add(new int[]{c.getCode(), c.getSuit(), c.getRank(), c.isFaceUp() ? 1 : 0});
            }
            this.wasteCards = new ArrayList<>();
            for (CardModel c : waste) {
                wasteCards.add(new int[]{c.getCode(), c.getSuit(), c.getRank(), c.isFaceUp() ? 1 : 0});
            }
        }
    }

    public KlondikeScreen(BaseBaseGame baseBaseGame) {
        super(baseBaseGame);
    }

    @Override
    protected void initData() {
        tableau = new ArrayList<>();
        for (int i = 0; i < TABLEAU_COUNT; i++) {
            tableau.add(new SpiderStack());
        }
        foundations = new ArrayList<>();
        for (int i = 0; i < FOUNDATION_COUNT; i++) {
            foundations.add(new ArrayList<>());
        }
        stock = new ArrayList<>();
        waste = new ArrayList<>();
        cardActorMap = new HashMap<>(52);
        foundationSlots = new Image[FOUNDATION_COUNT];
        tableauSlots = new Image[TABLEAU_COUNT];
        solverService = new KlondikeSolverService();
        solutionSteps = new ArrayList<>();
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
        gamePanel.setSize(Constant.GAMEWIDTH - 600, Constant.GAMEHIGHT);
        rootView.addActor(gamePanel);

        solverPanel = new Group();
        solverPanel.setSize(600, Constant.GAMEHIGHT);
        solverPanel.setPosition(Constant.GAMEWIDTH, Constant.GAMEHIGHT / 2f, Align.right);
        rootView.addActor(solverPanel);
        buildSolverPanel();

        float panelW = gamePanel.getWidth();
        colGap = (panelW - 80) / 7f;
        leftX = 40 + colGap / 2f;
        topY = Constant.GAMEHIGHT - 60;
        tableauTopY = topY - CARD_H - 40;

        // Stock slot (top left)
        stockX = leftX;
        stockY = topY;
        stockSlot = makeSlot(0x4E342E);
        stockSlot.setPosition(stockX, stockY, Align.top);
        gamePanel.addActor(stockSlot);

        // Waste slot (next to stock)
        wasteX = leftX + colGap;
        wasteSlot = makeSlot(0x263238);
        wasteSlot.setPosition(wasteX, topY, Align.top);
        gamePanel.addActor(wasteSlot);

        // Foundation slots (top right 4)
        for (int i = 0; i < FOUNDATION_COUNT; i++) {
            Image slot = makeSlot(0x1565C0);
            float x = leftX + (i + 3) * colGap;
            slot.setPosition(x, topY, Align.top);
            gamePanel.addActor(slot);
            foundationSlots[i] = slot;
        }

        // Tableau column placeholders
        for (int i = 0; i < TABLEAU_COUNT; i++) {
            Image slot = makeSlot(0x263238);
            float x = leftX + i * colGap;
            slot.setPosition(x, tableauTopY, Align.top);
            gamePanel.addActor(slot);
            tableauSlots[i] = slot;
        }

        // Status label
        statusLabel = new Label("Ready", new Label.LabelStyle(
                Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt"), Color.WHITE));
        statusLabel.setAlignment(Align.left);
        statusLabel.setPosition(40, 10);
        gamePanel.addActor(statusLabel);

        // Top bar buttons
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(
                Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt"));
        style.fontColor = Color.WHITE;

        TextButton menuBtn = new TextButton("MENU", style);
        menuBtn.addListener(simpleClick(() -> setScreen(MainMenuScreen.class)));

        TextButton newBtn = new TextButton("NEW", style);
        newBtn.addListener(simpleClick(() -> setScreen(KlondikeScreen.class)));

        Table bar = new Table();
        bar.top().right().pad(10);
        bar.add(menuBtn).pad(4);
        bar.add(newBtn).pad(4);
        bar.pack();
        bar.setPosition(panelW - 20, Constant.GAMEHIGHT, Align.topRight);
        rootView.addActor(bar);

        gamePanel.addListener(new KlondikeInputListener());
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
        solutionSteps = new ArrayList<>();
        currentStepIndex = 0;
        autoPlay = false;
        undoStack.clear();

        for (SpiderStack s : tableau) s.getCards().clear();
        for (List<CardModel> f : foundations) f.clear();
        stock.clear();
        waste.clear();
        recycleCount = 0;

        for (CardActor actor : cardActorMap.values()) {
            actor.remove();
        }
        cardActorMap.clear();

        currentSeed = new Random().nextLong();
        int[] deck = DealShuffler.shuffleSingleDeck(currentSeed);
        int idx = 0;

        // Deal tableau: col i gets i+1 cards, only top face up
        for (int col = 0; col < TABLEAU_COUNT; col++) {
            for (int row = 0; row <= col; row++) {
                int code = deck[idx++];
                int suit = code / 100;
                int rank = code % 100;
                boolean faceUp = (row == col);
                CardModel card = new CardModel(code, suit, rank, faceUp);
                tableau.get(col).getCards().add(card);
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
        statusLabel.setText("Klondike - New Game");
    }

    private void createAllActors() {
        for (SpiderStack stack : tableau) {
            for (CardModel card : stack.getCards()) {
                CardActor actor = new CardActor(card);
                cardActorMap.put(card, actor);
                gamePanel.addActor(actor);
            }
        }
        for (CardModel card : stock) {
            CardActor actor = new CardActor(card);
            cardActorMap.put(card, actor);
            gamePanel.addActor(actor);
        }
    }

    private boolean isRed(CardModel card) {
        return card.getSuit() == 2 || card.getSuit() == 3;
    }

    private void refreshLayout(boolean create) {
        // Tableau
        for (int col = 0; col < TABLEAU_COUNT; col++) {
            SpiderStack stack = tableau.get(col);
            float x = leftX + col * colGap;
            float yOffset = 0;
            for (int i = 0; i < stack.getCards().size(); i++) {
                CardModel card = stack.getCards().get(i);
                CardActor actor = cardActorMap.get(card);
                if (actor == null) continue;
                actor.clearActions();
                actor.checkFaceUp();
                float gap = card.isFaceUp() ? ROW_GAP : FACEDOWN_GAP;
                float y = tableauTopY - yOffset;
                if (create) {
                    actor.setPosition(Constant.GAMEWIDTH / 2f, -100, Align.center);
                    actor.addAction(Actions.sequence(
                            Actions.delay(col * 0.05f + i * 0.04f),
                            Actions.moveToAligned(x, y, Align.top, 0.2f)
                    ));
                } else {
                    actor.addAction(Actions.moveToAligned(x, y, Align.top, 0.15f));
                }
                actor.toFront();
                yOffset += gap;
            }
        }

        // Stock
        for (int i = 0; i < stock.size(); i++) {
            CardModel card = stock.get(i);
            CardActor actor = cardActorMap.get(card);
            if (actor == null) continue;
            actor.clearActions();
            actor.checkFaceUp();
            if (create) {
                actor.setPosition(stockX, -50, Align.top);
                actor.addAction(Actions.sequence(
                        Actions.delay(1.5f + i * 0.01f),
                        Actions.moveToAligned(stockX, topY, Align.top, 0.1f)
                ));
            } else {
                actor.setPosition(stockX, topY, Align.top);
            }
            actor.toFront();
        }

        // Waste
        refreshWaste();

        // Foundations
        for (int i = 0; i < FOUNDATION_COUNT; i++) {
            List<CardModel> pile = foundations.get(i);
            if (!pile.isEmpty()) {
                CardModel top = pile.get(pile.size() - 1);
                CardActor actor = cardActorMap.get(top);
                if (actor != null) {
                    actor.clearActions();
                    float x = leftX + (i + 3) * colGap;
                    actor.addAction(Actions.moveToAligned(x, topY, Align.top, 0.15f));
                    actor.toFront();
                }
            }
        }

        // Auto-foundation
        gamePanel.addAction(Actions.sequence(
                Actions.delay(0.3f),
                Actions.run(this::autoFoundation)
        ));
    }

    private void refreshLayout() {
        refreshLayout(false);
    }

    private void refreshWaste() {
        for (int i = 0; i < waste.size(); i++) {
            CardModel card = waste.get(i);
            CardActor actor = cardActorMap.get(card);
            if (actor == null) continue;
            actor.clearActions();
            actor.checkFaceUp();
            actor.setPosition(wasteX, topY, Align.top);
            actor.toFront();
        }
    }

    /** 翻面最顶的暗牌 */
    private void flipTopCards() {
        for (int col = 0; col < TABLEAU_COUNT; col++) {
            List<CardModel> cards = tableau.get(col).getCards();
            if (cards.isEmpty()) continue;
            CardModel top = cards.get(cards.size() - 1);
            if (!top.isFaceUp()) {
                top.setFaceUp(true);
                CardActor actor = cardActorMap.get(top);
                if (actor != null) {
                    actor.checkFaceUp();
                }
            }
        }
    }

    /** 从stock翻牌到waste */
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
                actor.addAction(Actions.moveToAligned(wasteX, topY, Align.top, 0.15f));
            }
        } else if (!waste.isEmpty()) {
            // Recycle waste back to stock
            recycleCount++;
            while (!waste.isEmpty()) {
                CardModel card = waste.remove(waste.size() - 1);
                card.setFaceUp(false);
                stock.add(card);
            }
            for (int i = 0; i < stock.size(); i++) {
                CardModel card = stock.get(i);
                CardActor actor = cardActorMap.get(card);
                if (actor == null) continue;
                actor.clearActions();
                actor.checkFaceUp();
                actor.setPosition(stockX, topY, Align.top);
                actor.toFront();
            }
            statusLabel.setText("Stock recycled");
        }
    }

    /** 检查是否可以放到foundation */
    private boolean canPlaceOnFoundation(CardModel card, int slot) {
        List<CardModel> pile = foundations.get(slot);
        if (pile.isEmpty()) {
            return card.getRank() == 1;
        }
        CardModel top = pile.get(pile.size() - 1);
        return top.getSuit() == card.getSuit() && top.getRank() == card.getRank() - 1;
    }

    private int findFoundationSlot(CardModel card) {
        // 先找同suit的
        for (int i = 0; i < FOUNDATION_COUNT; i++) {
            if (canPlaceOnFoundation(card, i)) {
                List<CardModel> pile = foundations.get(i);
                if (!pile.isEmpty() && pile.get(0).getSuit() == card.getSuit()) {
                    return i;
                }
                if (pile.isEmpty() && card.getRank() == 1) {
                    return i;
                }
            }
        }
        return -1;
    }

    /** 检查是否可以放到tableau列 */
    private boolean canPlaceOnTableau(CardModel card, int col) {
        List<CardModel> target = tableau.get(col).getCards();
        if (target.isEmpty()) {
            return card.getRank() == 13; // Only Kings on empty
        }
        CardModel top = target.get(target.size() - 1);
        return isRed(top) != isRed(card) && top.getRank() == card.getRank() + 1;
    }

    /** 检查从tableau[col]的cardIndex开始是否是合法可拖动序列 */
    private boolean isValidRun(int col, int cardIndex) {
        List<CardModel> cards = tableau.get(col).getCards();
        CardModel card = cards.get(cardIndex);
        if (!card.isFaceUp()) return false;
        for (int i = cardIndex; i < cards.size() - 1; i++) {
            CardModel c1 = cards.get(i);
            CardModel c2 = cards.get(i + 1);
            if (isRed(c1) == isRed(c2)) return false;
            if (c1.getRank() != c2.getRank() + 1) return false;
        }
        return true;
    }

    /** 是否安全自动上foundation */
    private boolean isSafeForAutoFoundation(CardModel card) {
        int slot = findFoundationSlot(card);
        if (slot < 0) return false;
        if (card.getRank() <= 2) return true;

        int needed = card.getRank() - 1;
        boolean cardIsRed = isRed(card);
        int oppositeFound = 0;
        int minOpposite = Integer.MAX_VALUE;
        for (int i = 0; i < FOUNDATION_COUNT; i++) {
            List<CardModel> pile = foundations.get(i);
            if (!pile.isEmpty() && isRed(pile.get(0)) != cardIsRed) {
                oppositeFound++;
                minOpposite = Math.min(minOpposite, pile.get(pile.size() - 1).getRank());
            }
        }
        if (oppositeFound < 2) {
            return card.getRank() <= 2;
        }
        return minOpposite >= needed;
    }

    /** 自动将安全的牌移到foundation */
    private void autoFoundation() {
        boolean moved;
        do {
            moved = false;
            // 检查tableau顶牌
            for (int col = 0; col < TABLEAU_COUNT; col++) {
                List<CardModel> cards = tableau.get(col).getCards();
                if (cards.isEmpty()) continue;
                CardModel top = cards.get(cards.size() - 1);
                if (!top.isFaceUp()) continue;
                if (isSafeForAutoFoundation(top)) {
                    int slot = findFoundationSlot(top);
                    if (slot >= 0) {
                        cards.remove(cards.size() - 1);
                        foundations.get(slot).add(top);
                        animateToFoundation(top, slot);
                        flipTopCards();
                        moved = true;
                    }
                }
            }
            // 检查waste顶牌
            if (!waste.isEmpty()) {
                CardModel top = waste.get(waste.size() - 1);
                if (isSafeForAutoFoundation(top)) {
                    int slot = findFoundationSlot(top);
                    if (slot >= 0) {
                        waste.remove(waste.size() - 1);
                        foundations.get(slot).add(top);
                        animateToFoundation(top, slot);
                        moved = true;
                    }
                }
            }
        } while (moved);

        checkWin();
    }

    private void animateToFoundation(CardModel card, int slot) {
        CardActor actor = cardActorMap.get(card);
        if (actor != null) {
            actor.toFront();
            float fx = leftX + (slot + 3) * colGap;
            actor.clearActions();
            actor.addAction(Actions.moveToAligned(fx, topY, Align.top, 0.2f));
        }
    }

    private void checkWin() {
        int total = 0;
        for (List<CardModel> pile : foundations) total += pile.size();
        if (total == 52) {
            statusLabel.setText("You Win!");
        }
    }

    /** 找牌在哪个tableau列 */
    private int[] findInTableau(CardModel card) {
        for (int col = 0; col < TABLEAU_COUNT; col++) {
            List<CardModel> cards = tableau.get(col).getCards();
            for (int i = 0; i < cards.size(); i++) {
                if (cards.get(i) == card) return new int[]{col, i};
            }
        }
        return null;
    }

    private boolean isWasteTop(CardModel card) {
        return !waste.isEmpty() && waste.get(waste.size() - 1) == card;
    }

    private boolean isStockCard(CardModel card) {
        return stock.contains(card);
    }

    // ==================== Drag & Drop ====================

    private static final int SRC_TABLEAU = 0;
    private static final int SRC_WASTE = 1;

    private class KlondikeInputListener extends ClickListener {
        private CardDrag drag;
        private int dragSource;
        private int dragSourceIndex;

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            Actor target = event.getTarget();

            // 点击stock区域 -> 翻牌
            if (isNearStock(x, y) && !(target instanceof CardActor && isWasteTop(((CardActor) target).getCard()))) {
                drawFromStock();
                return true;
            }

            if (!(target instanceof CardActor)) return super.touchDown(event, x, y, pointer, button);

            CardActor cardActor = (CardActor) target;
            CardModel card = cardActor.getCard();

            // stock顶牌点击 -> 翻牌
            if (isStockCard(card)) {
                drawFromStock();
                return true;
            }

            // 从tableau拖动
            int[] tabInfo = findInTableau(card);
            if (tabInfo != null) {
                int col = tabInfo[0];
                int cardIdx = tabInfo[1];
                if (!isValidRun(col, cardIdx)) return super.touchDown(event, x, y, pointer, button);

                List<CardModel> run = new ArrayList<>(
                        tableau.get(col).getCards().subList(cardIdx, tableau.get(col).getCards().size()));
                List<CardActor> runActors = new ArrayList<>();
                for (CardModel c : run) {
                    runActors.add(cardActorMap.get(c));
                }

                Vector2 v = new Vector2(x, y);
                gamePanel.localToStageCoordinates(v);
                target.stageToLocalCoordinates(v);

                drag = new CardDrag(col, new ArrayList<>(run), new ArrayList<>(runActors), v);
                drag.setTouchTarget(target);
                dragSource = SRC_TABLEAU;
                dragSourceIndex = col;
                bringToFront(run);
                return true;
            }

            // 从waste拖动（仅顶牌）
            if (isWasteTop(card)) {
                List<CardModel> run = new ArrayList<>();
                run.add(card);
                List<CardActor> runActors = new ArrayList<>();
                runActors.add(cardActorMap.get(card));

                Vector2 v = new Vector2(x, y);
                gamePanel.localToStageCoordinates(v);
                target.stageToLocalCoordinates(v);

                drag = new CardDrag(-1, new ArrayList<>(run), new ArrayList<>(runActors), v);
                drag.setTouchTarget(target);
                dragSource = SRC_WASTE;
                dragSourceIndex = -1;
                bringToFront(run);
                return true;
            }

            return super.touchDown(event, x, y, pointer, button);
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            super.touchDragged(event, x, y, pointer);
            if (drag == null) return;
            for (int i = 0; i < drag.getMovingActor().size(); i++) {
                CardActor actor = drag.getMovingActor().get(i);
                actor.setPosition(x - drag.getTouchDownV2().x,
                        y - drag.getTouchDownV2().y - i * ROW_GAP);
            }
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            super.touchUp(event, x, y, pointer, button);
            if (drag == null) return;

            List<CardModel> moving = drag.getMoving();
            CardModel first = moving.get(0);
            Actor hit = drag.getMovingActor().get(0);
            float hx = hit.getX(Align.center);
            float hy = hit.getY(Align.center);

            boolean placed = false;

            // 尝试放到foundation（仅单张）
            if (moving.size() == 1) {
                int fSlot = findClosestFoundation(hx, hy);
                if (fSlot >= 0 && canPlaceOnFoundation(first, fSlot)) {
                    removeFromSource(moving);
                    foundations.get(fSlot).add(first);
                    placed = true;
                }
            }

            // 尝试放到tableau
            if (!placed) {
                int targetCol = findClosestTableau(hx);
                if (targetCol >= 0) {
                    boolean sameSource = (dragSource == SRC_TABLEAU && targetCol == dragSourceIndex);
                    if (!sameSource && canPlaceOnTableau(first, targetCol)) {
                        removeFromSource(moving);
                        tableau.get(targetCol).getCards().addAll(moving);
                        placed = true;
                    }
                }
            }

            // 放到空tableau（仅K）
            if (!placed) {
                int targetCol = findClosestTableau(hx);
                if (targetCol >= 0 && tableau.get(targetCol).getCards().isEmpty()) {
                    boolean sameSource = (dragSource == SRC_TABLEAU && targetCol == dragSourceIndex);
                    if (!sameSource && first.getRank() == 13) {
                        removeFromSource(moving);
                        tableau.get(targetCol).getCards().addAll(moving);
                        placed = true;
                    }
                }
            }

            if (placed) {
                flipTopCards();
            }

            drag = null;
            refreshLayout();
        }

        private void removeFromSource(List<CardModel> moving) {
            if (dragSource == SRC_TABLEAU) {
                List<CardModel> source = tableau.get(dragSourceIndex).getCards();
                for (CardModel c : moving) source.remove(c);
            } else if (dragSource == SRC_WASTE) {
                waste.remove(waste.size() - 1);
            }
        }

        private int findClosestTableau(float x) {
            int best = -1;
            float bestDist = CARD_W;
            for (int i = 0; i < TABLEAU_COUNT; i++) {
                float cx = leftX + i * colGap;
                float dist = Math.abs(x - cx);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = i;
                }
            }
            return best;
        }

        private int findClosestFoundation(float hx, float hy) {
            int best = -1;
            float bestDist = CARD_W * 1.5f;
            for (int i = 0; i < FOUNDATION_COUNT; i++) {
                float sx = foundationSlots[i].getX(Align.center);
                float sy = foundationSlots[i].getY(Align.center);
                if (Math.abs(hy - sy) > CARD_H) continue;
                float dist = Math.abs(hx - sx);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = i;
                }
            }
            return best;
        }

        private boolean isNearStock(float x, float y) {
            float sy = topY - CARD_H / 2f;
            return Math.abs(x - stockX) < CARD_W && Math.abs(y - sy) < CARD_H;
        }
    }

    private void bringToFront(List<CardModel> cards) {
        for (CardModel card : cards) {
            CardActor actor = cardActorMap.get(card);
            if (actor != null) actor.toFront();
        }
    }

    // ==================== Solver Panel ====================

    private void buildSolverPanel() {
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle(
                Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt"));
        btnStyle.fontColor = Color.WHITE;

        Label.LabelStyle labelStyle = new Label.LabelStyle(
                Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt"), Color.WHITE);

        Label title = new Label("Solver", labelStyle);
        title.setPosition(300, Constant.GAMEHIGHT - 30, Align.top);
        solverPanel.addActor(title);

        TextButton solveBtn = new TextButton("SOLVE", btnStyle);
        solveBtn.addListener(simpleClick(this::solveCurrent));

        TextButton stepBtn = new TextButton("STEP", btnStyle);
        stepBtn.addListener(simpleClick(this::playOneStep));

        TextButton undoBtn = new TextButton("UNDO", btnStyle);
        undoBtn.addListener(simpleClick(this::undoOneStep));

        TextButton autoBtn = new TextButton("AUTO", btnStyle);
        autoBtn.addListener(simpleClick(() -> {
            autoPlay = !autoPlay;
            statusLabel.setText(autoPlay ? "Auto play on" : "Auto play off");
        }));

        Table btnBar = new Table();
        btnBar.add(solveBtn).pad(4);
        btnBar.add(stepBtn).pad(4);
        btnBar.add(undoBtn).pad(4);
        btnBar.add(autoBtn).pad(4);
        btnBar.pack();
        btnBar.setPosition(300, Constant.GAMEHIGHT - 80, Align.top);
        solverPanel.addActor(btnBar);

        stepListTable = new Table();
        stepListTable.top().left().pad(8);

        stepScrollPane = new ScrollPane(stepListTable);
        stepScrollPane.setSize(580, Constant.GAMEHIGHT - 140);
        stepScrollPane.setPosition(10, 10);
        stepScrollPane.setScrollingDisabled(true, false);
        solverPanel.addActor(stepScrollPane);
    }

    private void refreshStepList() {
        stepListTable.clearChildren();
        stepLabels.clear();

        Label.LabelStyle normalStyle = new Label.LabelStyle(
                Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt"), Color.LIGHT_GRAY);
        Label.LabelStyle doneStyle = new Label.LabelStyle(
                Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt"), Color.GREEN);
        Label.LabelStyle currentStyle = new Label.LabelStyle(
                Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt"), Color.YELLOW);

        for (int i = 0; i < solutionSteps.size(); i++) {
            KlondikeSolutionStep step = solutionSteps.get(i);
            String text = (i + 1) + ". " + step.getDescription();
            Label label;
            if (i < currentStepIndex) {
                label = new Label(text, doneStyle);
            } else if (i == currentStepIndex) {
                label = new Label(text, currentStyle);
            } else {
                label = new Label(text, normalStyle);
            }
            label.setScale(0.6f);
            stepLabels.add(label);
            stepListTable.add(label).width(560).height(label.getPrefHeight()).padBottom(4).left().row();
        }

        stepListTable.layout();
        if (currentStepIndex > 0 && currentStepIndex <= stepLabels.size()) {
            Label target = stepLabels.get(Math.min(currentStepIndex, stepLabels.size() - 1));
            stepScrollPane.layout();
            stepScrollPane.scrollTo(0, target.getY(), target.getWidth(), target.getHeight(), true, true);
        }
    }

    private void updateStepHighlight() {
        for (int i = 0; i < stepLabels.size(); i++) {
            Label label = stepLabels.get(i);
            if (i < currentStepIndex) {
                label.getStyle().fontColor = Color.GREEN;
            } else if (i == currentStepIndex) {
                label.getStyle().fontColor = Color.YELLOW;
            } else {
                label.getStyle().fontColor = Color.LIGHT_GRAY;
            }
        }
        if (currentStepIndex > 0 && currentStepIndex <= stepLabels.size()) {
            Label target = stepLabels.get(Math.min(currentStepIndex, stepLabels.size() - 1));
            stepScrollPane.scrollTo(0, target.getY(), target.getWidth(), target.getHeight(), true, true);
        }
    }

    private void saveSnapshot() {
        undoStack.push(new BoardSnapshot(tableau, foundations, stock, waste, currentStepIndex));
    }

    private void restoreSnapshot(BoardSnapshot snapshot) {
        for (CardModel key : cardActorMap.keySet()) {
            CardActor actor = cardActorMap.get(key);
            if (actor != null) actor.remove();
        }
        cardActorMap.clear();

        for (int i = 0; i < tableau.size(); i++) {
            tableau.get(i).getCards().clear();
            for (int[] data : snapshot.tableauCards.get(i)) {
                CardModel card = new CardModel(data[0], data[1], data[2], data[3] == 1);
                tableau.get(i).getCards().add(card);
            }
        }
        for (int i = 0; i < foundations.size(); i++) {
            foundations.get(i).clear();
            for (int[] data : snapshot.foundationCards.get(i)) {
                CardModel card = new CardModel(data[0], data[1], data[2], data[3] == 1);
                foundations.get(i).add(card);
            }
        }
        stock.clear();
        for (int[] data : snapshot.stockCards) {
            stock.add(new CardModel(data[0], data[1], data[2], data[3] == 1));
        }
        waste.clear();
        for (int[] data : snapshot.wasteCards) {
            waste.add(new CardModel(data[0], data[1], data[2], data[3] == 1));
        }

        currentStepIndex = snapshot.stepIndex;
        createAllActors();
        // Also create actors for waste and foundation cards
        for (CardModel card : waste) {
            if (!cardActorMap.containsKey(card)) {
                CardActor actor = new CardActor(card);
                cardActorMap.put(card, actor);
                gamePanel.addActor(actor);
            }
        }
        for (List<CardModel> pile : foundations) {
            for (CardModel card : pile) {
                if (!cardActorMap.containsKey(card)) {
                    CardActor actor = new CardActor(card);
                    cardActorMap.put(card, actor);
                    gamePanel.addActor(actor);
                }
            }
        }
        refreshLayout(true);
        updateStepHighlight();
    }

    private void undoOneStep() {
        if (undoStack.isEmpty()) {
            statusLabel.setText("No more undo");
            return;
        }
        autoPlay = false;
        BoardSnapshot snapshot = undoStack.pop();
        restoreSnapshot(snapshot);
        statusLabel.setText("Undo -> step " + currentStepIndex);
    }

    private void solveCurrent() {
        try {
            String board = buildBoardState();
            KlondikeSolveResult result = solverService.solveBoard(board);
            solutionSteps = result.getSteps();
            currentStepIndex = 0;
            autoPlay = false;
            undoStack.clear();
            statusLabel.setText(result.getSummary());
            refreshStepList();
        } catch (Exception ex) {
            statusLabel.setText("Solver error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void playOneStep() {
        if (currentStepIndex >= solutionSteps.size()) {
            statusLabel.setText("No more steps");
            autoPlay = false;
            return;
        }
        saveSnapshot();
        KlondikeSolutionStep step = solutionSteps.get(currentStepIndex++);

        if (step.isDealMove()) {
            // Deal move: draw from stock to waste, possibly with recycle
            drawFromStock();
            autoDelay = 0.5f;
        } else if (step.isFromWaste() && step.isToTableau()) {
            // Waste -> Tableau
            int destCol = step.getDestStackIndex();
            if (!waste.isEmpty()) {
                CardModel card = waste.remove(waste.size() - 1);
                tableau.get(destCol).getCards().add(card);
                flipTopCards();
            }
            autoDelay = 0.5f;
        } else if (step.isFromWaste() && step.isToFoundation()) {
            // Waste -> Foundation
            int destSlot = step.getDestStackIndex();
            if (!waste.isEmpty()) {
                CardModel card = waste.remove(waste.size() - 1);
                foundations.get(destSlot).add(card);
            }
            autoDelay = 0.5f;
        } else if (step.isFromTableau() && step.isToTableau()) {
            // Tableau -> Tableau
            int srcCol = step.getSourceStackIndex();
            int destCol = step.getDestStackIndex();
            int count = step.getCardCount();
            applyTableauMove(srcCol, destCol, count);
            autoDelay = 0.5f;
        } else if (step.isFromTableau() && step.isToFoundation()) {
            // Tableau -> Foundation
            int srcCol = step.getSourceStackIndex();
            int destSlot = step.getDestStackIndex();
            List<CardModel> cards = tableau.get(srcCol).getCards();
            if (!cards.isEmpty()) {
                CardModel card = cards.remove(cards.size() - 1);
                foundations.get(destSlot).add(card);
                flipTopCards();
            }
            autoDelay = 0.5f;
        } else {
            // Other moves (foundation->tableau, etc.)
            autoDelay = 0.5f;
        }

        refreshLayout();
        statusLabel.setText(step.getDescription());
        updateStepHighlight();
    }

    private void applyTableauMove(int from, int to, int count) {
        List<CardModel> source = tableau.get(from).getCards();
        if (source.isEmpty()) return;
        int start = Math.max(source.size() - count, 0);
        List<CardModel> moving = new ArrayList<>(source.subList(start, source.size()));
        source.subList(start, source.size()).clear();
        tableau.get(to).getCards().addAll(moving);
        flipTopCards();
    }

    /** Build board state string for the Klondike solver */
    private String buildBoardState() {
        // Format: "klondike,1\n" + tableau rows + stock row
        // Tableau: row i has cards for columns >= i
        // Row 0 = bottom card of each column
        // Row 1 = 2nd card from bottom, columns 1-6
        // etc.
        int maxHeight = 0;
        for (SpiderStack s : tableau) {
            maxHeight = Math.max(maxHeight, s.getCards().size());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("klondike,1\n");

        for (int row = 0; row < maxHeight; row++) {
            for (int col = 0; col < TABLEAU_COUNT; col++) {
                List<CardModel> cards = tableau.get(col).getCards();
                if (row < cards.size()) {
                    sb.append(DealCardCodec.format(cards.get(row).getCode()));
                }
                sb.append(",");
            }
            sb.append("\n");
        }

        // Stock + waste: the solver expects the "feed" line
        // Feed order: waste (bottom to top) then stock (top to bottom)
        // All as one comma-separated line
        for (int i = 0; i < waste.size(); i++) {
            sb.append(DealCardCodec.format(waste.get(i).getCode())).append(",");
        }
        for (int i = stock.size() - 1; i >= 0; i--) {
            sb.append(DealCardCodec.format(stock.get(i).getCode())).append(",");
        }
        sb.append("\n");

        // Foundation state (aces line)
        for (int i = 0; i < FOUNDATION_COUNT; i++) {
            List<CardModel> pile = foundations.get(i);
            for (CardModel card : pile) {
                sb.append(DealCardCodec.format(card.getCode())).append(",");
            }
        }
        sb.append("\n");

        System.out.println(sb);
        return sb.toString();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render(delta);

        if (autoPlay && currentStepIndex < solutionSteps.size()) {
            autoTimer += delta;
            if (autoTimer >= autoDelay) {
                autoTimer = 0f;
                playOneStep();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
}
