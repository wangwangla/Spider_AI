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
import com.utils.CardDrag;
import com.utils.CardModel;
import com.utils.SpiderStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Freecell Solitaire
 * - 8 cascades, 4 free cells, 4 foundations
 * - Standard 52-card deck, all face up
 * - Build descending alternating color on cascades
 * - Build ascending by suit on foundations
 */
public class FreecellScreen extends BaseScreen {

    private static final int CASCADE_COUNT = 8;
    private static final int FREECELL_COUNT = 4;
    private static final int FOUNDATION_COUNT = 4;
    private static final float CARD_W = CardConstant.CARD_W;
    private static final float CARD_H = CardConstant.CARD_H;
    private static final float ROW_GAP = CardConstant.ROW_GAP;

    // 8 列
    private List<SpiderStack> cascades;
    // 4 个自由格 (null = empty)
    private CardModel[] freeCells;
    // 4 个基础堆 (top card, null = empty)
    private CardModel[] foundations;
    // foundation完整记录
    private List<List<CardModel>> foundationPiles;

    private HashMap<CardModel, CardActor> cardActorMap;
    private Group gamePanel;
    private Label statusLabel;

    // 槽位占位图
    private Image[] freeCellSlots;
    private Image[] foundationSlots;
    private Image[] cascadeSlots;

    // 布局参数
    private float colGap;
    private float topY;
    private float cascadeTopY;
    private float leftX;

    public FreecellScreen(BaseBaseGame baseBaseGame) {
        super(baseBaseGame);
    }

    @Override
    protected void initData() {
        cascades = new ArrayList<>();
        for (int i = 0; i < CASCADE_COUNT; i++) {
            cascades.add(new SpiderStack());
        }
        freeCells = new CardModel[FREECELL_COUNT];
        foundations = new CardModel[FOUNDATION_COUNT];
        foundationPiles = new ArrayList<>();
        for (int i = 0; i < FOUNDATION_COUNT; i++) {
            foundationPiles.add(new ArrayList<>());
        }
        cardActorMap = new HashMap<>(52);
        freeCellSlots = new Image[FREECELL_COUNT];
        foundationSlots = new Image[FOUNDATION_COUNT];
        cascadeSlots = new Image[CASCADE_COUNT];
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

        colGap = (Constant.GAMEWIDTH - 80) / 8f;
        leftX = 40 + colGap / 2f;
        topY = Constant.GAMEHIGHT - 60;
        cascadeTopY = topY - CARD_H - 40;

        // Free cell slots (top left 4)
        for (int i = 0; i < FREECELL_COUNT; i++) {
            Image slot = makeSlot(0x2E7D32);
            float x = leftX + i * colGap;
            slot.setPosition(x, topY, Align.top);
            gamePanel.addActor(slot);
            freeCellSlots[i] = slot;
        }

        // Foundation slots (top right 4)
        for (int i = 0; i < FOUNDATION_COUNT; i++) {
            Image slot = makeSlot(0x1565C0);
            float x = leftX + (i + 4) * colGap;
            slot.setPosition(x, topY, Align.top);
            gamePanel.addActor(slot);
            foundationSlots[i] = slot;
        }

        // Cascade column placeholders
        for (int i = 0; i < CASCADE_COUNT; i++) {
            Image slot = makeSlot(0x263238);
            float x = leftX + i * colGap;
            slot.setPosition(x, cascadeTopY, Align.top);
            gamePanel.addActor(slot);
            cascadeSlots[i] = slot;
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
        newBtn.addListener(simpleClick(() -> setScreen(FreecellScreen.class)));

        Table bar = new Table();
        bar.top().right().pad(10);
        bar.add(menuBtn).pad(4);
        bar.add(newBtn).pad(4);
        bar.pack();
        bar.setPosition(Constant.GAMEWIDTH - 20, Constant.GAMEHIGHT, Align.topRight);
        rootView.addActor(bar);

        gamePanel.addListener(new FreecellInputListener());
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
        for (SpiderStack s : cascades) s.getCards().clear();
        for (int i = 0; i < FREECELL_COUNT; i++) freeCells[i] = null;
        for (int i = 0; i < FOUNDATION_COUNT; i++) foundations[i] = null;
        for (List<CardModel> pile : foundationPiles) pile.clear();

        // 清除旧Actor
        for (CardActor actor : cardActorMap.values()) {
            actor.remove();
        }
        cardActorMap.clear();

        int[] deck = DealShuffler.shuffleSingleDeck(new Random().nextLong());
        int idx = 0;
        for (int col = 0; col < CASCADE_COUNT; col++) {
            int count = col < 4 ? 7 : 6;
            for (int r = 0; r < count; r++) {
                int code = deck[idx++];
                int suit = code / 100;
                int rank = code % 100;
                CardModel card = new CardModel(code, suit, rank, true);
                cascades.get(col).getCards().add(card);
            }
        }

        refreshLayout(true);
        statusLabel.setText("Freecell - New Game");
    }

    private void refreshLayout(boolean create) {
        for (int col = 0; col < CASCADE_COUNT; col++) {
            SpiderStack stack = cascades.get(col);
            float x = leftX + col * colGap;
            for (int i = 0; i < stack.getCards().size(); i++) {
                CardModel card = stack.getCards().get(i);
                CardActor actor = getOrCreateActor(card, create);
                actor.clearActions();
                actor.checkFaceUp();
                float y = cascadeTopY - i * ROW_GAP;
                if (create) {
                    actor.setPosition(Constant.GAMEWIDTH / 2f, -100, Align.center);
                    actor.addAction(Actions.sequence(
                            Actions.delay(col * 0.03f + i * 0.05f),
                            Actions.moveToAligned(x, y, Align.top, 0.25f)
                    ));
                } else {
                    actor.addAction(Actions.moveToAligned(x, y, Align.top, 0.2f));
                }
            }
        }

        // Free cells
        for (int i = 0; i < FREECELL_COUNT; i++) {
            CardModel card = freeCells[i];
            if (card != null) {
                CardActor actor = getOrCreateActor(card, false);
                actor.clearActions();
                float x = leftX + i * colGap;
                actor.addAction(Actions.moveToAligned(x, topY, Align.top, 0.2f));
                actor.toFront();
            }
        }

        // Foundations (show top card)
        for (int i = 0; i < FOUNDATION_COUNT; i++) {
            List<CardModel> pile = foundationPiles.get(i);
            if (!pile.isEmpty()) {
                CardModel top = pile.get(pile.size() - 1);
                CardActor actor = getOrCreateActor(top, false);
                actor.clearActions();
                float x = leftX + (i + 4) * colGap;
                actor.addAction(Actions.moveToAligned(x, topY, Align.top, 0.2f));
                actor.toFront();
            }
        }

        // Auto-foundation after layout settles
        gamePanel.addAction(Actions.sequence(
                Actions.delay(0.3f),
                Actions.run(this::autoFoundation)
        ));
    }

    private void refreshLayout() {
        refreshLayout(false);
    }

    private CardActor getOrCreateActor(CardModel card, boolean create) {
        CardActor actor = cardActorMap.get(card);
        if (actor == null && create) {
            actor = new CardActor(card);
            cardActorMap.put(card, actor);
            gamePanel.addActor(actor);
        }
        return actor;
    }

    /** 判断花色是否为红色 (heart=2, diamond=3) */
    private boolean isRed(CardModel card) {
        return card.getSuit() == 2 || card.getSuit() == 3;
    }

    /** 计算最大可移动张数 */
    private int maxMovable(int excludeCascade) {
        int freeCellEmpty = 0;
        for (CardModel fc : freeCells) {
            if (fc == null) freeCellEmpty++;
        }
        int emptyCascades = 0;
        for (int i = 0; i < CASCADE_COUNT; i++) {
            if (i != excludeCascade && cascades.get(i).getCards().isEmpty()) {
                emptyCascades++;
            }
        }
        return (freeCellEmpty + 1) * (1 << emptyCascades);
    }

    /** 检查从cascade[col]的cardIndex开始是否是合法可拖动序列（降序交替颜色） */
    private boolean isValidRun(int col, int cardIndex) {
        List<CardModel> cards = cascades.get(col).getCards();
        CardModel card = cards.get(cardIndex);
        if (!card.isFaceUp()) return false;
        for (int i = cardIndex; i < cards.size() - 1; i++) {
            CardModel c1 = cards.get(i);
            CardModel c2 = cards.get(i + 1);
            if (isRed(c1) == isRed(c2)) return false; // 同色
            if (c1.getRank() != c2.getRank() + 1) return false; // 非递减
        }
        return true;
    }

    /** 能否放到cascade目标列 */
    private boolean canPlaceOnCascade(CardModel card, int targetCol) {
        List<CardModel> target = cascades.get(targetCol).getCards();
        if (target.isEmpty()) return true;
        CardModel top = target.get(target.size() - 1);
        return isRed(top) != isRed(card) && top.getRank() == card.getRank() + 1;
    }

    /** 能否放到foundation */
    private boolean canPlaceOnFoundation(CardModel card) {
        for (int i = 0; i < FOUNDATION_COUNT; i++) {
            if (canPlaceOnFoundationSlot(card, i)) return true;
        }
        return false;
    }

    private boolean canPlaceOnFoundationSlot(CardModel card, int slot) {
        List<CardModel> pile = foundationPiles.get(slot);
        if (pile.isEmpty()) {
            return card.getRank() == 1; // Only Ace on empty
        }
        CardModel top = pile.get(pile.size() - 1);
        return top.getSuit() == card.getSuit() && top.getRank() == card.getRank() - 1;
    }

    /** 自动将安全的牌移到foundation */
    private void autoFoundation() {
        boolean moved;
        do {
            moved = false;
            // 检查cascade顶牌
            for (int col = 0; col < CASCADE_COUNT; col++) {
                List<CardModel> cards = cascades.get(col).getCards();
                if (cards.isEmpty()) continue;
                CardModel top = cards.get(cards.size() - 1);
                if (isSafeForAutoFoundation(top)) {
                    int slot = findFoundationSlot(top);
                    if (slot >= 0) {
                        cards.remove(cards.size() - 1);
                        foundationPiles.get(slot).add(top);
                        foundations[slot] = top;
                        CardActor actor = cardActorMap.get(top);
                        if (actor != null) {
                            actor.toFront();
                            float fx = leftX + (slot + 4) * colGap;
                            actor.clearActions();
                            actor.addAction(Actions.moveToAligned(fx, topY, Align.top, 0.2f));
                        }
                        moved = true;
                    }
                }
            }
            // 检查freecell
            for (int i = 0; i < FREECELL_COUNT; i++) {
                CardModel fc = freeCells[i];
                if (fc == null) continue;
                if (isSafeForAutoFoundation(fc)) {
                    int slot = findFoundationSlot(fc);
                    if (slot >= 0) {
                        freeCells[i] = null;
                        foundationPiles.get(slot).add(fc);
                        foundations[slot] = fc;
                        CardActor actor = cardActorMap.get(fc);
                        if (actor != null) {
                            actor.toFront();
                            float fx = leftX + (slot + 4) * colGap;
                            actor.clearActions();
                            actor.addAction(Actions.moveToAligned(fx, topY, Align.top, 0.2f));
                        }
                        moved = true;
                    }
                }
            }
        } while (moved);

        // 检查胜利
        int total = 0;
        for (List<CardModel> pile : foundationPiles) total += pile.size();
        if (total == 52) {
            statusLabel.setText("You Win!");
        }
    }

    /** 是否安全自动上基础堆（不会阻碍其他牌） */
    private boolean isSafeForAutoFoundation(CardModel card) {
        int slot = findFoundationSlot(card);
        if (slot < 0) return false;
        if (card.getRank() <= 2) return true; // A和2总是安全的

        // 只有当两个异色foundation都已经到了rank-1时才安全
        int needed = card.getRank() - 1;
        boolean cardIsRed = isRed(card);
        for (int i = 0; i < FOUNDATION_COUNT; i++) {
            List<CardModel> pile = foundationPiles.get(i);
            CardModel top = pile.isEmpty() ? null : pile.get(pile.size() - 1);
            int topRank = top == null ? 0 : top.getRank();
            // 检查异色foundation
            if (top != null && isRed(top) != cardIsRed) {
                if (topRank < needed) return false;
            } else if (top == null) {
                // 空的异色foundation意味着还没放A
                // 需要判断这个空位是否对应异色花色
                // 简化：对于空位，检查是否有该异色suit的foundation
            }
        }
        // 简化判断：检查所有异色suit的foundation top rank >= card.rank - 1
        int minOpposite = Integer.MAX_VALUE;
        for (int i = 0; i < FOUNDATION_COUNT; i++) {
            List<CardModel> pile = foundationPiles.get(i);
            if (pile.isEmpty()) {
                // 该suit还没有A
                // 如果suit颜色与card异色，则不安全
                // 但我们不知道空位对应什么suit... 检查已占位的
                continue;
            }
            CardModel top = pile.get(pile.size() - 1);
            if (isRed(top) != cardIsRed) {
                minOpposite = Math.min(minOpposite, top.getRank());
            }
        }
        // 如果有异色foundation还没出现（比如还没有红色A），不安全
        int oppositeFoundationsNeeded = cardIsRed ? 2 : 2; // 黑色2花色或红色2花色
        int oppositeFoundationsFound = 0;
        for (int i = 0; i < FOUNDATION_COUNT; i++) {
            List<CardModel> pile = foundationPiles.get(i);
            if (!pile.isEmpty() && isRed(pile.get(0)) != cardIsRed) {
                oppositeFoundationsFound++;
            }
        }
        if (oppositeFoundationsFound < oppositeFoundationsNeeded) {
            // 还有异色suit没上foundation
            if (card.getRank() > 2) return false;
        }

        return minOpposite >= needed;
    }

    /** 找到可以放card的foundation slot */
    private int findFoundationSlot(CardModel card) {
        // 先找已有同suit的
        for (int i = 0; i < FOUNDATION_COUNT; i++) {
            if (canPlaceOnFoundationSlot(card, i)) {
                List<CardModel> pile = foundationPiles.get(i);
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

    // ==================== Drag & Drop ====================

    /** 查找点击位置对应的区域: cascade / freecell / foundation */
    private static final int AREA_NONE = -1;
    private static final int AREA_CASCADE = 0;
    private static final int AREA_FREECELL = 1;
    private static final int AREA_FOUNDATION = 2;

    private int findCascadeCol(float x) {
        for (int i = 0; i < CASCADE_COUNT; i++) {
            float cx = leftX + i * colGap;
            if (Math.abs(x - cx) < CARD_W * 0.7f) return i;
        }
        return -1;
    }

    private int findFreeCellSlot(float x, float y) {
        for (int i = 0; i < FREECELL_COUNT; i++) {
            float sx = freeCellSlots[i].getX(Align.center);
            float sy = freeCellSlots[i].getY(Align.center);
            if (Math.abs(x - sx) < CARD_W && Math.abs(y - sy) < CARD_H) return i;
        }
        return -1;
    }

    private int findFoundationSlotAt(float x, float y) {
        for (int i = 0; i < FOUNDATION_COUNT; i++) {
            float sx = foundationSlots[i].getX(Align.center);
            float sy = foundationSlots[i].getY(Align.center);
            if (Math.abs(x - sx) < CARD_W && Math.abs(y - sy) < CARD_H) return i;
        }
        return -1;
    }

    /** 根据CardActor找到它在哪个cascade和index */
    private int[] findCardInCascade(CardModel card) {
        for (int col = 0; col < CASCADE_COUNT; col++) {
            List<CardModel> cards = cascades.get(col).getCards();
            for (int i = 0; i < cards.size(); i++) {
                if (cards.get(i) == card) return new int[]{col, i};
            }
        }
        return null;
    }

    private int findCardInFreeCell(CardModel card) {
        for (int i = 0; i < FREECELL_COUNT; i++) {
            if (freeCells[i] == card) return i;
        }
        return -1;
    }

    private class FreecellInputListener extends ClickListener {
        private CardDrag drag;
        private int dragSource; // 0=cascade, 1=freecell
        private int dragSourceIndex; // col or freecell slot

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            Actor target = event.getTarget();
            if (!(target instanceof CardActor)) return super.touchDown(event, x, y, pointer, button);

            CardActor cardActor = (CardActor) target;
            CardModel card = cardActor.getCard();

            // 从cascade拖动
            int[] cascadeInfo = findCardInCascade(card);
            if (cascadeInfo != null) {
                int col = cascadeInfo[0];
                int cardIdx = cascadeInfo[1];
                if (!isValidRun(col, cardIdx)) return super.touchDown(event, x, y, pointer, button);

                int runSize = cascades.get(col).getCards().size() - cardIdx;
                // 需要检查是否有足够空间移动这么多张
                List<CardModel> run = new ArrayList<>(
                        cascades.get(col).getCards().subList(cardIdx, cascades.get(col).getCards().size()));
                List<CardActor> runActors = new ArrayList<>();
                for (CardModel c : run) {
                    runActors.add(cardActorMap.get(c));
                }

                Vector2 v = new Vector2(x, y);
                gamePanel.localToStageCoordinates(v);
                target.stageToLocalCoordinates(v);

                drag = new CardDrag(col, new ArrayList<>(run), new ArrayList<>(runActors), v);
                drag.setTouchTarget(target);
                dragSource = AREA_CASCADE;
                dragSourceIndex = col;
                bringToFront(run);
                return true;
            }

            // 从freecell拖动
            int fcSlot = findCardInFreeCell(card);
            if (fcSlot >= 0) {
                List<CardModel> run = new ArrayList<>();
                run.add(card);
                List<CardActor> runActors = new ArrayList<>();
                runActors.add(cardActorMap.get(card));

                Vector2 v = new Vector2(x, y);
                gamePanel.localToStageCoordinates(v);
                target.stageToLocalCoordinates(v);

                drag = new CardDrag(-1, new ArrayList<>(run), new ArrayList<>(runActors), v);
                drag.setTouchTarget(target);
                dragSource = AREA_FREECELL;
                dragSourceIndex = fcSlot;
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
                actor.setPosition(x - drag.getTouchDownV2().x, y - drag.getTouchDownV2().y - i * 20);
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
                if (fSlot >= 0 && canPlaceOnFoundationSlot(first, fSlot)) {
                    removeFromSource(moving);
                    foundationPiles.get(fSlot).add(first);
                    foundations[fSlot] = first;
                    placed = true;
                }
            }

            // 尝试放到freecell（仅单张）
            if (!placed && moving.size() == 1) {
                int fcSlot = findClosestFreeCell(hx, hy);
                if (fcSlot >= 0 && freeCells[fcSlot] == null) {
                    removeFromSource(moving);
                    freeCells[fcSlot] = first;
                    placed = true;
                }
            }

            // 尝试放到cascade
            if (!placed) {
                int targetCol = findClosestCascade(hx);
                if (targetCol >= 0 && targetCol != dragSourceIndex) {
                    if (canPlaceOnCascade(first, targetCol)) {
                        int max = maxMovable(targetCol);
                        if (moving.size() <= max) {
                            removeFromSource(moving);
                            cascades.get(targetCol).getCards().addAll(moving);
                            placed = true;
                        }
                    }
                }
            }

            // 如果放到空cascade
            if (!placed) {
                int targetCol = findClosestCascade(hx);
                if (targetCol >= 0 && cascades.get(targetCol).getCards().isEmpty()
                        && (dragSource != AREA_CASCADE || targetCol != dragSourceIndex)) {
                    int max = maxMovable(targetCol);
                    if (moving.size() <= max) {
                        removeFromSource(moving);
                        cascades.get(targetCol).getCards().addAll(moving);
                        placed = true;
                    }
                }
            }

            drag = null;
            refreshLayout();
        }

        private void removeFromSource(List<CardModel> moving) {
            if (dragSource == AREA_CASCADE) {
                List<CardModel> source = cascades.get(dragSourceIndex).getCards();
                for (CardModel c : moving) source.remove(c);
            } else if (dragSource == AREA_FREECELL) {
                freeCells[dragSourceIndex] = null;
            }
        }

        private int findClosestCascade(float x) {
            int best = -1;
            float bestDist = CARD_W;
            for (int i = 0; i < CASCADE_COUNT; i++) {
                float cx = leftX + i * colGap;
                float dist = Math.abs(x - cx);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = i;
                }
            }
            return best;
        }

        private int findClosestFreeCell(float hx, float hy) {
            int best = -1;
            float bestDist = CARD_W * 1.5f;
            for (int i = 0; i < FREECELL_COUNT; i++) {
                float sx = freeCellSlots[i].getX(Align.center);
                float sy = freeCellSlots[i].getY(Align.center);
                if (Math.abs(hy - sy) > CARD_H) continue;
                float dist = Math.abs(hx - sx);
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
    }

    private void bringToFront(List<CardModel> cards) {
        for (CardModel card : cards) {
            CardActor actor = cardActorMap.get(card);
            if (actor != null) actor.toFront();
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
