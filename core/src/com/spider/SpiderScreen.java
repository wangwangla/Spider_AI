package com.spider;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kw.gdx.asset.Asset;
import com.solvitaire.app.DealShuffler;
import com.solvitaire.app.SpiderSolutionStep;
import com.solvitaire.app.SpiderSolveResult;
import com.solvitaire.app.SpiderSolverService;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Lightweight Spider implementation with a built-in solver.
 * - 10 tableau stacks, 5 stock deals (single suit for clarity).
 * - Drag to move runs, tap DEAL for next 10 cards.
 * - SOLVE runs the bundled solver and steps/auto-plays the moves.
 */
public class SpiderScreen extends ScreenAdapter {
    private static final int COLS = 10;
    private static final int STOCK_DEALS = 5;
    private static final float CARD_W = 92f;
    private static final float CARD_H = 132f;
    private static final float COL_GAP = 16f;
    private static final float ROW_GAP = 28f;
    private static final float LEFT_X = 40f;
    private static final float TOP_Y = 720f;

    private final Stage stage;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Texture background;
    private final Texture cardBack;
    private final Map<Integer, Texture> cardFaces = new HashMap<>();
    private final List<SpiderStack> stacks = new ArrayList<>(COLS);
    private final Deque<CardModel> stockQueue = new ArrayDeque<>();
    private final List<List<CardModel>> completedSuits = new ArrayList<>();
    private final SpiderSolverService solverService = new SpiderSolverService();
    private List<SpiderSolutionStep> solutionSteps = new ArrayList<>();
    private int currentStepIndex = 0;
    private boolean autoPlay = false;
    private float autoTimer = 0f;
    private final Label statusLabel;

    public SpiderScreen() {
        this.batch = new SpriteBatch();
        this.stage = new Stage(new FitViewport(1400, 900), batch);
        this.font = Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt");
        this.background = new Texture(Gdx.files.internal("background.png"));
        this.cardBack = new Texture(Gdx.files.internal("cardback.png"));

        for (int i = 0; i < COLS; i++) {
            stacks.add(new SpiderStack());
        }

        Table topBar = buildUi();
        topBar.setFillParent(true);
        Image bg = new Image(background);
        bg.setFillParent(true);
        stage.addActor(bg);
        stage.addActor(topBar);

        statusLabel = new Label("Ready", new Label.LabelStyle(font, Color.WHITE));
        statusLabel.setAlignment(Align.left);
        statusLabel.setPosition(LEFT_X, 860f);
        stage.addActor(statusLabel);

        Gdx.input.setInputProcessor(new InputMultiplexer(stage, cardInput));
        newGame();
    }

    private Table buildUi() {
        TextureRegionDrawable btnDrawable = new TextureRegionDrawable(new TextureRegion(makeColorTexture(0x2e7d32)));
        TextureRegionDrawable btnDown = new TextureRegionDrawable(new TextureRegion(makeColorTexture(0x1b5e20)));
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(btnDrawable, btnDown, btnDrawable, font);
        style.fontColor = Color.WHITE;

        TextButton newBtn = new TextButton("NEW", style);
        newBtn.addListener(simpleClick(() -> newGame()));

        TextButton dealBtn = new TextButton("DEAL", style);
        dealBtn.addListener(simpleClick(this::dealNext));

        TextButton solveBtn = new TextButton("SOLVE", style);
        solveBtn.addListener(simpleClick(this::solveCurrent));

        TextButton stepBtn = new TextButton("STEP", style);
        stepBtn.addListener(simpleClick(this::playOneStep));

        TextButton autoBtn = new TextButton("AUTO", style);
        autoBtn.addListener(simpleClick(() -> {
            autoPlay = !autoPlay;
            statusLabel.setText(autoPlay ? "Auto play on" : "Auto play off");
        }));

        Table bar = new Table();
        bar.top().right().pad(10);
        bar.add(newBtn).pad(4);
        bar.add(dealBtn).pad(4);
        bar.add(solveBtn).pad(4);
        bar.add(stepBtn).pad(4);
        bar.add(autoBtn).pad(4);
        return bar;
    }

    private Texture makeColorTexture(int rgb) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, 1f));
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
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

    private void newGame() {
        solutionSteps = new ArrayList<>();
        currentStepIndex = 0;
        autoPlay = false;
        completedSuits.clear();
        for (SpiderStack stack : stacks) {
            stack.cards.clear();
        }
        stockQueue.clear();

        int[] deck = DealShuffler.shuffleSpiderDeck(new Random().nextLong(), 1); // single suit
        int idx = 0;
        for (int col = 0; col < COLS; col++) {
            int cardsInCol = col < 4 ? 6 : 5;
            for (int i = 0; i < cardsInCol; i++) {
                boolean faceUp = i == cardsInCol - 1;
                addCardToStack(stacks.get(col), deck[idx++], faceUp);
            }
        }

        // remaining 50 cards go to stock
        while (idx < deck.length) {
            stockQueue.addLast(toModel(deck[idx++], false));
        }
        refreshLayout();
        statusLabel.setText("New game - stock " + stockQueue.size());
    }

    private void addCardToStack(SpiderStack stack, int code, boolean faceUp) {
        stack.cards.add(toModel(code, faceUp));
    }

    private CardModel toModel(int code, boolean faceUp) {
        int suit = code / 100;
        int rank = code % 100;
        return new CardModel(code, suit, rank, faceUp);
    }

    private void refreshLayout() {
        Array<com.badlogic.gdx.scenes.scene2d.Actor> actors = new Array<>(stage.getActors());
        for (com.badlogic.gdx.scenes.scene2d.Actor actor : actors) {
            if (actor instanceof CardActor) {
                actor.remove();
            }
        }

        for (int col = 0; col < COLS; col++) {
            SpiderStack stack = stacks.get(col);
            float x = LEFT_X + col * (CARD_W + COL_GAP);
            float y = TOP_Y;
            for (int i = 0; i < stack.cards.size(); i++) {
                CardModel card = stack.cards.get(i);
                CardActor actor = new CardActor(card, col, i);
                actor.setPosition(x, y - i * ROW_GAP);
                stage.addActor(actor);
            }
        }
    }

    private void dealNext() {
        if (stockQueue.size() < COLS) {
            statusLabel.setText("No more stock");
            return;
        }
        for (SpiderStack stack : stacks) {
            if (stack.cards.isEmpty()) {
                statusLabel.setText("Fill empty columns before dealing.");
                return;
            }
        }
        for (SpiderStack stack : stacks) {
            CardModel card = stockQueue.removeFirst();
            card.faceUp = true;
            stack.cards.add(card);
        }
        refreshLayout();
        statusLabel.setText("Dealt 10 cards. Stock " + stockQueue.size());
    }

    private void solveCurrent() {
        try {
            String board = buildBoardState();
            SpiderSolveResult result = solverService.solveBoard(board);
            solutionSteps = result.getSteps();
            currentStepIndex = 0;
            autoPlay = false;
            statusLabel.setText(result.getSummary());
        } catch (Exception ex) {
            statusLabel.setText("Solver error: " + ex.getMessage());
        }
    }

    private void playOneStep() {
        if (currentStepIndex >= solutionSteps.size()) {
            statusLabel.setText("No more steps");
            autoPlay = false;
            return;
        }
        SpiderSolutionStep step = solutionSteps.get(currentStepIndex++);
        if (step.isDealMove()) {
            dealNext();
        } else {
            applyMove(step.getSourceStackIndex(), step.getDestinationStackIndex(), step.getCardCount());
        }
        refreshLayout();
        statusLabel.setText(step.getDescription());
    }

    private void applyMove(int from, int to, int count) {
        SpiderStack source = stacks.get(from);
        SpiderStack dest = stacks.get(to);
        if (source.cards.isEmpty()) {
            return;
        }
        int start = Math.max(source.cards.size() - count, 0);
        List<CardModel> moving = new ArrayList<>(source.cards.subList(start, source.cards.size()));
        source.cards.subList(start, source.cards.size()).clear();
        dest.cards.addAll(moving);
        flipTop(source);
        checkCompleted(dest);
    }

    private void flipTop(SpiderStack stack) {
        if (!stack.cards.isEmpty()) {
            stack.cards.get(stack.cards.size() - 1).faceUp = true;
        }
    }

    private void checkCompleted(SpiderStack stack) {
        if (stack.cards.size() < 13) return;
        int start = stack.cards.size() - 13;
        int suit = stack.cards.get(start).suit;
        for (int i = 0; i < 13; i++) {
            CardModel card = stack.cards.get(start + i);
            if (!card.faceUp || card.suit != suit || card.rank != 13 - i) {
                return;
            }
        }
        List<CardModel> run = new ArrayList<>(stack.cards.subList(start, stack.cards.size()));
        stack.cards.subList(start, stack.cards.size()).clear();
        completedSuits.add(run);
    }

    private String buildBoardState() {
        int[] totals = new int[COLS];
        int[] faceDown = new int[COLS];
        int max = 0;
        for (int c = 0; c < COLS; c++) {
            SpiderStack stack = stacks.get(c);
            totals[c] = stack.cards.size();
            faceDown[c] = (int) stack.cards.stream().filter(card -> !card.faceUp).count();
            max = Math.max(max, totals[c]);
        }
        int dealsRemaining = stockQueue.size();
        StringBuilder sb = new StringBuilder();
        sb.append("Spider,").append(dealsRemaining);
        for (int c = 0; c < COLS; c++) {
            sb.append(":").append(faceDown[c] * 100 + totals[c]);
        }
        sb.append("\n# Stacks:\n");
        for (int row = 0; row < max; row++) {
            for (int col = 0; col < COLS; col++) {
                SpiderStack stack = stacks.get(col);
                String code = "";
                if (row < stack.cards.size()) {
                    code = toCode(stack.cards.get(row));
                }
                sb.append(code).append(",");
            }
            sb.append("\n");
        }
        sb.append("# Deck:\n");
        int dealtDeals = (50 - dealsRemaining) / 10;
        int deckSlots = STOCK_DEALS * COLS;
        List<String> deckTokens = new ArrayList<>(deckSlots);
        for (int i = 0; i < dealtDeals * COLS; i++) {
            deckTokens.add("");
        }
        for (CardModel card : stockQueue) {
            deckTokens.add(toCode(card));
        }
        while (deckTokens.size() < deckSlots) {
            deckTokens.add("");
        }
        for (String token : deckTokens) {
            sb.append(token).append(",");
        }
        sb.append("\n# Suits:\n");
        for (int i = 0; i < 8; i++) {
            if (i < completedSuits.size()) {
                CardModel top = completedSuits.get(i).get(0);
                sb.append(toCode(top));
            }
            sb.append(",");
        }
        return sb.toString();
    }

    private String toCode(CardModel card) {
        String rank;
        switch (card.rank) {
            case 1:
                rank = "a";
                break;
            case 10:
                rank = "10";
                break;
            case 11:
                rank = "j";
                break;
            case 12:
                rank = "q";
                break;
            case 13:
                rank = "k";
                break;
            default:
                rank = Integer.toString(card.rank);
        }
        char suitChar = "0shdc".charAt(card.suit); // 1=s,2=h,3=d,4=c
        return rank + suitChar;
    }

    private Texture faceFor(CardModel card) {
        int index = (card.suit - 1) * 13 + card.rank;
        return cardFaces.computeIfAbsent(index, k -> new Texture(Gdx.files.internal("card/CARD" + index + ".png")));
    }

    private final InputProcessor cardInput = new InputAdapter();

    private class InputAdapter implements InputProcessor {
        private CardDrag drag;

        @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            Vector2 world = stage.getViewport().unproject(new Vector2(screenX, screenY));
            CardHit hit = findTopCard(world.x, world.y);
            if (hit == null || !hit.card.faceUp) {
                return false;
            }
            List<CardModel> run = hit.stack.cards.subList(hit.index, hit.stack.cards.size());
            if (!isMovableRun(run)) {
                if (run.size() > 1) {
                    run = run.subList(run.size() - 1, run.size());
                }
            }
            drag = new CardDrag(hit.stackIndex, new ArrayList<>(run), world);
            bringToFront(drag.moving);
            return true;
        }

        @Override public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (drag == null) return false;
            Vector2 world = stage.getViewport().unproject(new Vector2(screenX, screenY));
            drag.offset.set(world);
            float dx = world.x - drag.start.x;
            float dy = world.y - drag.start.y;
            for (CardModel card : drag.moving) {
                CardActor actor = findActor(card);
                if (actor != null) {
                    actor.moveBy(dx, dy);
                }
            }
            return true;
        }

        @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (drag == null) return false;
            Vector2 world = stage.getViewport().unproject(new Vector2(screenX, screenY));
            int targetCol = columnAt(world.x, world.y);
            if (targetCol >= 0 && canDrop(targetCol, drag.moving)) {
                moveCards(drag.fromCol, targetCol, drag.moving.size());
                statusLabel.setText("Moved " + drag.moving.size() + " card(s)");
            } else {
                statusLabel.setText("Illegal move");
            }
            drag = null;
            refreshLayout();
            return true;
        }

        private boolean isMovableRun(List<CardModel> run) {
            if (run.isEmpty()) return false;
            for (int i = 0; i < run.size() - 1; i++) {
                CardModel a = run.get(i);
                CardModel b = run.get(i + 1);
                if (a.rank != b.rank + 1 || a.suit != b.suit) {
                    return false;
                }
            }
            return true;
        }

        private boolean canDrop(int targetCol, List<CardModel> run) {
            SpiderStack dest = stacks.get(targetCol);
            if (dest.cards.isEmpty()) return true;
            CardModel top = dest.cards.get(dest.cards.size() - 1);
            CardModel bottom = run.get(0);
            return top.rank == bottom.rank + 1;
        }

        private void moveCards(int fromCol, int toCol, int count) {
            SpiderStack source = stacks.get(fromCol);
            SpiderStack dest = stacks.get(toCol);
            int start = source.cards.size() - count;
            List<CardModel> moving = new ArrayList<>(source.cards.subList(start, source.cards.size()));
            source.cards.subList(start, source.cards.size()).clear();
            dest.cards.addAll(moving);
            flipTop(source);
            checkCompleted(dest);
        }

        private int columnAt(float x, float y) {
            for (int col = 0; col < COLS; col++) {
                float cx = LEFT_X + col * (CARD_W + COL_GAP);
                if (x >= cx && x <= cx + CARD_W) return col;
            }
            return -1;
        }

        @Override public boolean keyDown(int keycode) { return false; }
        @Override public boolean keyUp(int keycode) { return false; }
        @Override public boolean keyTyped(char character) { return false; }
        @Override public boolean scrolled(float amountX, float amountY) { return false; }
        @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
        @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    }

    private CardActor findActor(CardModel card) {
        for (com.badlogic.gdx.scenes.scene2d.Actor actor : stage.getActors()) {
            if (actor instanceof CardActor) {
                if (((CardActor) actor).card == card) {
                    return (CardActor) actor;
                }
            }
        }
        return null;
    }

    private void bringToFront(List<CardModel> cards) {
        for (CardModel card : cards) {
            CardActor actor = findActor(card);
            if (actor != null) {
                actor.toFront();
            }
        }
    }

    private CardHit findTopCard(float x, float y) {
        CardHit hit = null;
        for (int col = 0; col < COLS; col++) {
            SpiderStack stack = stacks.get(col);
            for (int i = stack.cards.size() - 1; i >= 0; i--) {
                float cx = LEFT_X + col * (CARD_W + COL_GAP);
                float cy = TOP_Y - i * ROW_GAP;
                Rectangle rect = new Rectangle(cx, cy, CARD_W, CARD_H);
                if (rect.contains(x, y)) {
                    hit = new CardHit(col, stack, i, stack.cards.get(i));
                    return hit;
                }
            }
        }
        return hit;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (autoPlay) {
            autoTimer += delta;
            if (autoTimer > 0.35f) {
                autoTimer = 0f;
                playOneStep();
            }
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
        font.dispose();
        background.dispose();
        cardBack.dispose();
        cardFaces.values().forEach(Texture::dispose);
    }

    private static class SpiderStack {
        final List<CardModel> cards = new ArrayList<>();
    }

    private static class CardModel {
        final int code;
        final int suit; // 1 spade, 2 heart, 3 diamond, 4 club
        final int rank; // 1..13
        boolean faceUp;

        CardModel(int code, int suit, int rank, boolean faceUp) {
            this.code = code;
            this.suit = suit;
            this.rank = rank;
            this.faceUp = faceUp;
        }
    }

    private class CardActor extends Image {
        final CardModel card;

        CardActor(CardModel card, int col, int index) {
            super(card.faceUp ? faceFor(card) : cardBack);
            this.card = card;
            setSize(CARD_W, CARD_H);
            setTouchable(Touchable.disabled); // we handle input centrally
        }
    }

    private static class CardHit {
        final int stackIndex;
        final SpiderStack stack;
        final int index;
        final CardModel card;

        CardHit(int stackIndex, SpiderStack stack, int index, CardModel card) {
            this.stackIndex = stackIndex;
            this.stack = stack;
            this.index = index;
            this.card = card;
        }
    }

    private static class CardDrag {
        final int fromCol;
        final List<CardModel> moving;
        final Vector2 start;
        final Vector2 offset = new Vector2();

        CardDrag(int fromCol, List<CardModel> moving, Vector2 start) {
            this.fromCol = fromCol;
            this.moving = moving;
            this.start = new Vector2(start);
        }
    }
}
