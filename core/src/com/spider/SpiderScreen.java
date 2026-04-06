package com.spider;

import com.actor.CardActor;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.constant.CardConstant;
import com.kw.gdx.BaseBaseGame;
import com.kw.gdx.asset.Asset;
import com.kw.gdx.constant.Constant;
import com.kw.gdx.screen.BaseScreen;
import com.solvitaire.app.DealShuffler;
import com.solvitaire.app.SpiderSolutionStep;
import com.solvitaire.app.SpiderSolveResult;
import com.solvitaire.app.SpiderSolverService;
import com.utils.CardDrag;
import com.utils.CardHit;
import com.utils.CardModel;
import com.utils.SpiderStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.constant.CardConstant.*;

/**
 * Lightweight Spider implementation with a built-in solver.
 * - 10 tableau stacks, 5 stock deals (single suit for clarity).
 * - Drag to move runs, tap DEAL for next 10 cards.
 * - SOLVE runs the bundled solver and steps/auto-plays the moves.
 */
public class SpiderScreen extends BaseScreen {

    private BitmapFont font;
    private Texture background;
    private Map<Integer, Texture> cardFaces;
    private List<SpiderStack> stacks;
    private Deque<CardModel> stockQueue;
    private List<List<CardModel>> completedSuits;
    private SpiderSolverService solverService;
    private List<SpiderSolutionStep> solutionSteps;
    private int currentStepIndex = 0;
    private boolean autoPlay = false;
    private float autoTimer = 0f;
    private Label statusLabel;
    private Image stockPlaceholder;
    private List<Image> foundationSlots = new ArrayList<>(8);

    public SpiderScreen(BaseBaseGame baseBaseGame) {
        super(baseBaseGame);
    }

    @Override
    public void initView() {
        super.initView();
        this.cardFaces = new HashMap<>();
        this.stacks = new ArrayList<>();
        this.stockQueue = new ArrayDeque<>();
        this.completedSuits = new ArrayList<>();
        this.solverService = new SpiderSolverService();
        this.solutionSteps = new ArrayList<>();
        this.font = Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt");
        this.background = new Texture(Gdx.files.internal("background.png"));

        //10 列
        for (int i = 0; i < CardConstant.COLS; i++) {
            stacks.add(new SpiderStack());
        }
        Table topBar = buildUi();
        topBar.pack();
        topBar.setPosition(1300,1080,Align.topLeft);
        Image bg = new Image(background);
        bg.setFillParent(true);
        rootView.addActor(bg);
        stockPlaceholder = new Image(new TextureRegionDrawable(new TextureRegion(makeColorTexture(0x37474f))));
        stockPlaceholder.setSize(CARD_W, CARD_H);
        stockPlaceholder.setPosition(STOCK_X, STOCK_Y);
        rootView.addActor(stockPlaceholder);
        for (int i = 0; i < 8; i++) {
            Image slot = new Image(new TextureRegionDrawable(new TextureRegion(makeColorTexture(0x263238))));
            slot.setSize(CARD_W, CARD_H);
            slot.setPosition(FOUNDATION_X + i * 10, FOUNDATION_Y);
            foundationSlots.add(slot);
            rootView.addActor(slot);
        }
        rootView.addActor(topBar);

        statusLabel = new Label("Ready", new Label.LabelStyle(font, Color.WHITE));
        statusLabel.setAlignment(Align.left);
        statusLabel.setPosition(LEFT_X, 860f);
        rootView.addActor(statusLabel);
        rootView.addListener(cardInput);
        newGame();
        printMove();
        initTouchPanel();
    }

    private void initTouchPanel() {


    }

    public void printMove(){
        Image slot = new Image(new TextureRegionDrawable(new TextureRegion(makeColorTexture(0x263238))));
        rootView.addActor(slot);
        slot.setSize(500,900);
        slot.setPosition(1920-100,540,Align.right);
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
            stack.getCards().clear();
        }
        stockQueue.clear();

        int[] deck = DealShuffler.shuffleSpiderDeck(new Random().nextLong(), 1); // single suit
        int idx = 0;
        for (int col = 0; col < CardConstant.COLS; col++) {
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
        stack.getCards().add(toModel(code, faceUp));
    }

    private CardModel toModel(int code, boolean faceUp) {
        int suit = code / 100;
        int rank = code % 100;
        return new CardModel(code, suit, rank, faceUp);
    }

    private void refreshLayout() {
        Array<Actor> actors = new Array<>(rootView.getChildren());
        for (Actor actor : actors) {
            if (actor instanceof CardActor) {
                actor.remove();
            }
        }

        for (int col = 0; col < COLS; col++) {
            SpiderStack stack = stacks.get(col);
            float x = LEFT_X + col * (CARD_W + COL_GAP);
            float y = TOP_Y;
            for (int i = 0; i < stack.getCards().size(); i++) {
                CardModel card = stack.getCards().get(i);
                CardActor actor = new CardActor(card);
                actor.setOwnStack(stack);
                actor.setPosition(x, y - i * ROW_GAP);
                rootView.addActor(actor);
            }
        }

        // foundation display: show top card of each completed suit
        for (int i = 0; i < completedSuits.size(); i++) {
            List<CardModel> run = completedSuits.get(i);
            if (run.isEmpty()) continue;
            CardModel top = run.get(run.size() - 1);
            CardActor actor = new CardActor(top);
            actor.setPosition(FOUNDATION_X + i * (CARD_W + FOUNDATION_GAP), FOUNDATION_Y);
            rootView.addActor(actor);
        }

        // stock indicator
        stockPlaceholder.setVisible(!stockQueue.isEmpty());
    }

    private void dealNext() {
        if (stockQueue.size() < COLS) {
            statusLabel.setText("No more stock");
            return;
        }
        for (SpiderStack stack : stacks) {
            if (stack.getCards().isEmpty()) {
                statusLabel.setText("Fill empty columns before dealing.");
                return;
            }
        }
        for (SpiderStack stack : stacks) {
            CardModel card = stockQueue.removeFirst();
            card.setFaceUp(true);
            stack.getCards().add(card);
        }
        // A fresh deal might complete a run (rare but possible); auto-collect.
        for (SpiderStack stack : stacks) {
            checkCompleted(stack);
        }
        // animate from stock to targets
        List<CardModel> newlyDealt = new ArrayList<>();
        for (SpiderStack stack : stacks) {
            newlyDealt.add(stack.getCards().get(stack.getCards().size() - 1));
        }
        refreshLayout();
        for (int i = 0; i < newlyDealt.size(); i++) {
            CardModel card = newlyDealt.get(i);
            CardActor actor = findActor(card);
            if (actor != null) {
                float tx = actor.getX();
                float ty = actor.getY();
                actor.setPosition(STOCK_X, STOCK_Y);
                actor.toFront();
                actor.addAction(Actions.sequence(
                        Actions.delay(0.02f * i),
                        Actions.moveTo(tx, ty, 0.25f, Interpolation.pow2Out)
                ));
            }
        }
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
        if (source.getCards().isEmpty()) {
            return;
        }
        int start = Math.max(source.getCards().size() - count, 0);
        List<CardModel> moving = new ArrayList<>(source.getCards().subList(start, source.getCards().size()));
        source.getCards().subList(start, source.getCards().size()).clear();
        dest.getCards().addAll(moving);
        flipTop(source);
        checkCompleted(source);
        checkCompleted(dest);
    }

    private void flipTop(SpiderStack stack) {
        if (!stack.getCards().isEmpty()) {
            stack.getCards().get(stack.getCards().size() - 1).setFaceUp(true);
        }
    }

    /**
     * Auto-collect any completed K->A run on top of a stack. Loops in case multiple runs exist.
     */
    private void checkCompleted(SpiderStack stack) {
        boolean removed;
        do {
            removed = false;
            if (stack.getCards().size() < 13) {
                break;
            }
            int start = stack.getCards().size() - 13;
            int suit = stack.getCards().get(start).getSuit();
            boolean ok = true;
            for (int i = 0; i < 13; i++) {
                CardModel card = stack.getCards().get(start + i);
                if (!card.isFaceUp() || card.getSuit() != suit || card.getRank() != 13 - i) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                List<CardModel> run = new ArrayList<>(stack.getCards().subList(start, stack.getCards().size()));
                // animate top card to foundation slot
                int slot = completedSuits.size();
                CardModel top = run.get(run.size() - 1);
                CardActor actor = findActor(top);
                float fx = FOUNDATION_X + slot * (CARD_W + FOUNDATION_GAP);
                float fy = FOUNDATION_Y;
                if (actor != null) {
                    actor.toFront();
                    actor.addAction(Actions.sequence(
                            Actions.moveTo(fx, fy, 0.3f, Interpolation.pow2Out),
                            Actions.run(this::refreshLayout)
                    ));
                }
                stack.getCards().subList(start, stack.getCards().size()).clear();
                completedSuits.add(run);
                if (actor == null) {
                    refreshLayout();
                }
                removed = true;
            }
        } while (removed);
    }

    private String buildBoardState() {
        int[] totals = new int[COLS];
        int[] faceDown = new int[COLS];
        int max = 0;
        for (int c = 0; c < COLS; c++) {
            SpiderStack stack = stacks.get(c);
            totals[c] = stack.getCards().size();
            faceDown[c] = (int) stack.getCards().stream().filter(card -> !card.isFaceUp()).count();
            max = Math.max(max, totals[c]);
        }
        int dealsRemaining = stockQueue.size();
        StringBuilder sb = new StringBuilder();
        sb.append("Spider,").append(dealsRemaining);
        for (int c = 0; c < COLS; c++) {
            sb.append(":").append(faceDown[c] * 100 + totals[c]);
        }
        sb.append("\n");
        // Rows are output bottom-up to match solver format.
        for (int row = 0; row < max; row++) {
            for (int col = 0; col < COLS; col++) {
                SpiderStack stack = stacks.get(col);
                String code = "";
                int size = stack.getCards().size();
                if (row < size) {
                    code = safeCode(stack.getCards().get(row)); // index 0 is bottom
                }
                sb.append(code).append(",");
            }
            sb.append("\n");
        }
        sb.append("# Deck:\n");
        // Deck: top of stock first. Fill exactly 50 entries.
        List<CardModel> stockList = new ArrayList<>(stockQueue);
        for (int i = 0; i < STOCK_DEALS * COLS; i++) {
            String token = i < stockList.size() ? safeCode(stockList.get(i)) : "";
            sb.append(token).append(",");
        }
        sb.append("\n");
        for (int i = 0; i < 8; i++) {
            if (i < completedSuits.size()) {
                CardModel top = completedSuits.get(i).get(0);
                sb.append(safeCode(top));
            }
            sb.append(",");
        }
        return sb.toString();
    }

    private String toCode(CardModel card) {
        String rank;
        switch (card.getRank()) {
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
                rank = Integer.toString(card.getRank());
        }
        char suitChar = "0shdc".charAt(card.getSuit()); // 1=s,2=h,3=d,4=c
        return rank + suitChar;
    }

    /**
     * Ensures token length is valid for solver; otherwise returns empty.
     */
    private String safeCode(CardModel card) {
        String token = toCode(card);
        int n2 = token.length();
        if (n2 == 2) return token;
        if (n2 == 3 && token.charAt(0) == '1' && token.charAt(1) == '0') return token;
        return "";
    }

    private InputListener cardInput = new CardInputListener();

    private class CardInputListener extends InputListener {
        private CardDrag drag;

        @Override public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            Actor target = event.getTarget();
            if (target == null)return super.touchDown(event,x,y,pointer,button);
            if (target instanceof CardActor){
                CardActor cardActor = (CardActor) (target);
                SpiderStack ownStack = cardActor.getOwnStack();
                int cardIndex = ownStack.findCardIndex(cardActor);
                if (cardIndex>0){
                    List<CardModel> run = ownStack.getCards().subList(cardIndex, ownStack.getCards().size());
                    if (!isMovableRun(run)) {
                        if (run.size() > 1) {
                            run = run.subList(run.size() - 1, run.size());
                        }
                    }
                    Vector2 vector2 = new Vector2(x, y);
                    rootView.localToStageCoordinates(vector2);
                    target.stageToLocalCoordinates(vector2);
                    drag = new CardDrag(cardIndex, new ArrayList<>(run),vector2);
                    bringToFront(drag.getMoving());
                }else {
                    return super.touchDown(event,x,y,pointer,button);
                }
            }
            return true;
        }

        @Override public void touchDragged(InputEvent event, float x, float y, int pointer) {
            if (drag == null) return;
            for (CardModel card : drag.getMoving()) {
                CardActor actor = findActor(card);
                if (actor != null) {
                    actor.setPosition(x,y);
                }
            }
        }

        @Override public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            if (drag == null) return;
            Vector2 world = new Vector2(event.getStageX(), event.getStageY());
            int targetCol = columnAt(world.x, world.y);
            if (targetCol >= 0 && canDrop(targetCol, drag.getMoving())) {
                moveCards(drag.getFromCol(), targetCol, drag.getMoving().size());
                statusLabel.setText("Moved " + drag.getMoving().size() + " card(s)");
            } else {
                statusLabel.setText("Illegal move");
            }
            drag = null;
            refreshLayout();
        }

        private boolean isMovableRun(List<CardModel> run) {
            if (run.isEmpty()) return false;
            for (int i = 0; i < run.size() - 1; i++) {
                CardModel a = run.get(i);
                CardModel b = run.get(i + 1);
                if (a.getRank() != b.getRank() + 1 || a.getSuit() != b.getSuit()) {
                    return false;
                }
            }
            return true;
        }

        private boolean canDrop(int targetCol, List<CardModel> run) {
            SpiderStack dest = stacks.get(targetCol);
            if (dest.getCards().isEmpty()) return true;
            CardModel top = dest.getCards().get(dest.getCards().size() - 1);
            CardModel bottom = run.get(0);
            return top.getRank() == bottom.getRank() + 1;
        }

        private void moveCards(int fromCol, int toCol, int count) {
            SpiderStack source = stacks.get(fromCol);
            SpiderStack dest = stacks.get(toCol);
            int start = source.getCards().size() - count;
            List<CardModel> moving = new ArrayList<>(source.getCards().subList(start, source.getCards().size()));
            source.getCards().subList(start, source.getCards().size()).clear();
            dest.getCards().addAll(moving);
            flipTop(source);
            checkCompleted(source);
            checkCompleted(dest);
        }

        private int columnAt(float x, float y) {
            for (int col = 0; col < COLS; col++) {
                float cx = LEFT_X + col * (CARD_W + COL_GAP);
                if (x >= cx && x <= cx + CARD_W) return col;
            }
            return -1;
        }

    }

    private CardActor findActor(CardModel card) {
        for (Actor actor : rootView.getChildren()) {
            if (actor instanceof CardActor) {
                if (((CardActor) actor).getCard() == card) {
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
            for (int i = stack.getCards().size() - 1; i >= 0; i--) {
                float cx = LEFT_X + col * (CARD_W + COL_GAP);
                float cy = TOP_Y - i * ROW_GAP;
                Rectangle rect = new Rectangle(cx, cy, CARD_W, CARD_H);
                if (rect.contains(x, y)) {
                    hit = new CardHit(col, stack, i, stack.getCards().get(i));
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

        super.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        super.dispose();
        font.dispose();
        background.dispose();
        cardFaces.values().forEach(Texture::dispose);
    }

}
