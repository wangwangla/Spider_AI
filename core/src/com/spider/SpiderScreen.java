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
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.bean.TouchInfo;
import com.bean.TouchUpBean;
import com.constant.CardConstant;
import com.kw.gdx.BaseBaseGame;
import com.kw.gdx.asset.Asset;
import com.kw.gdx.constant.Constant;
import com.kw.gdx.listener.OrdinaryButtonListener;
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
import java.util.Random;

import static com.constant.CardConstant.*;

/**
 * Lightweight Spider implementation with a built-in solver.
 * - 10 tableau stacks, 5 stock deals (single suit for clarity).
 * - Drag to move runs, tap DEAL for next 10 cards.
 * - SOLVE runs the bundled solver and steps/auto-plays the moves.
 */
public class SpiderScreen extends BaseScreen {
    private List<SpiderStack> stacks;
    private Deque<CardModel> stockQueue;
    private List<List<CardModel>> completedSuits;
    private SpiderSolverService solverService;
    private List<SpiderSolutionStep> solutionSteps;
    private int currentStepIndex = 0;
    private boolean autoPlay = false;
    private float autoTimer = 0f;
    private Label statusLabel;
    private Group stockPlaceholder;
    private int foundationSlotIndex = 0;
    private List<Image> foundationSlots = new ArrayList<>(8);
    private ArrayList<Image> holderImgs = new ArrayList<>(4);
    private ArrayList<Image> deckImgs = new ArrayList<>(10);
    private HashMap<CardModel,CardActor> cardModelCardActorHashMap;
    private Group gamePanel;
    private Group solverPanel;

    public SpiderScreen(BaseBaseGame baseBaseGame) {
        super(baseBaseGame);
    }

    @Override
    public void initView() {
        super.initView();
        initGameBg();
        initPanel();

        newGame();
        printMove();
    }

    private void initPanel() {
        this.gamePanel = new Group();
        this.solverPanel = new Group();
        //发牌
        this.stockPlaceholder = new Group();
        Table topBar = buildUi();

        gamePanel.setSize(Constant.GAMEWIDTH - 600,Constant.GAMEHIGHT);
        gamePanel.setDebug(true);
        rootView.addActor(gamePanel);

        solverPanel.setSize(600,Constant.GAMEHIGHT);
        solverPanel.setDebug(true);
        rootView.addActor(solverPanel);
        solverPanel.setPosition(Constant.GAMEWIDTH,Constant.GAMEHIGHT/2f,Align.right);

        rootView.addActor(topBar);
        topBar.pack();
        topBar.setPosition(1300,1080,Align.topLeft);

        stockPlaceholder.setSize(CARD_W, CARD_H);
        stockPlaceholder.setPosition(gamePanel.getWidth()-100, Constant.GAMEHIGHT-150,Align.bottomRight);
        gamePanel.addActor(stockPlaceholder);
        stockPlaceholder.addListener(new OrdinaryButtonListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                dealNext();
            }
        });

        for (int i = 0; i < 4; i++) {
            Image holdImg = new Image(Asset.getAsset().getTexture("cardback.png"));
            stockPlaceholder.addActor(holdImg);
            holdImg.setX(i * 10);
            holderImgs.add(holdImg);
            holdImg.setSize(CARD_W,CARD_H);
        }

        for (int i = 0; i < 8; i++) {
            Image slot = new Image(Asset.getAsset().getTexture("white.png"));
            slot.setSize(CARD_W, CARD_H);
            slot.setPosition(40 + i * 20, 1080-150);
            foundationSlots.add(slot);
            gamePanel.addActor(slot);
//            slot.setVisible(false);
        }
        TOP_Y = Constant.GAMEHIGHT-300;
        float gap = (Constant.GAMEWIDTH - 700) / 10.f;
        for (int col = 0; col < COLS; col++) {
            float x = LEFT_X + col * gap + gap/2f;
            Image deckCard = makeColorTexture(0x263238);
            deckCard.setSize(CARD_W,CARD_H);
            deckCard.setPosition(x, TOP_Y,Align.bottom);
            gamePanel.addActor(deckCard);
            deckImgs.add(deckCard);
        }

        statusLabel = new Label("Ready", new Label.LabelStyle(Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt"), Color.WHITE));
        statusLabel.setAlignment(Align.left);
        statusLabel.setPosition(LEFT_X, 10);
        gamePanel.addActor(statusLabel);
        gamePanel.addListener(cardInput);
    }

    @Override
    protected void initData() {
        this.stacks = new ArrayList<>();
        this.stockQueue = new ArrayDeque<>();
        this.completedSuits = new ArrayList<>();
        this.solverService = new SpiderSolverService();
        this.solutionSteps = new ArrayList<>();
        this.cardModelCardActorHashMap = new HashMap<>(104);
        //10 列
        for (int i = 0; i < CardConstant.COLS; i++) {
            stacks.add(new SpiderStack());
        }
    }

    private void initGameBg() {
        Texture background = Asset.getAsset().getTexture("background.png");
        background.setWrap(Texture.TextureWrap.Repeat,Texture.TextureWrap.Repeat);
        TextureRegion region = new TextureRegion(background);
        region.setRegionWidth((int) (Constant.GAMEWIDTH+0.5f));
        region.setRegionHeight((int) (Constant.GAMEHIGHT+0.5f));
        Image bg = new Image(region);
        bg.setPosition(960,540,Align.center);
        rootView.addActor(bg);
    }

    public void printMove(){
        Image slot = makeColorTexture(0x263238);;
        rootView.addActor(slot);
        slot.setSize(500,900);
        slot.setPosition(1920-100,540,Align.right);
    }

    private Table buildUi() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(Asset.getAsset().loadBitFont("bitfont/ntcb_40.fnt"));
        style.fontColor = Color.WHITE;

        TextButton menuBtn = new TextButton("MENU", style);
        menuBtn.addListener(simpleClick(() -> {
            setScreen(MainMenuScreen.class);
        }));

        TextButton newBtn = new TextButton("NEW", style);
        newBtn.addListener(simpleClick(() -> {
            setScreen(SpiderScreen.class);
        }));

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
        bar.add(menuBtn).pad(4);
        bar.add(newBtn).pad(4);
        bar.add(dealBtn).pad(4);
        bar.add(solveBtn).pad(4);
        bar.add(stepBtn).pad(4);
        bar.add(autoBtn).pad(4);
        return bar;
    }

    private Image makeColorTexture(int rgb) {
        Image img = new Image(Asset.getAsset().getTexture("white.png"));
        img.setColor(new Color(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, 1f));
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

    private void newGame() {
        solutionSteps = new ArrayList<>();
        currentStepIndex = 0;
        autoPlay = false;
        completedSuits.clear();
        for (SpiderStack stack : stacks) {
            stack.getCards().clear();
        }
        stockQueue.clear();

        int[] deck = DealShuffler.shuffleSpiderDeck(new Random().nextLong(), SpiderGame.suitMode);
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
        refreshLayout(true,2);
        statusLabel.setText(SpiderGame.suitMode + " suit - stock " + stockQueue.size());
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
        refreshLayout(false,0);
    }

    private void refreshLayout(boolean create,int mode) {
        float gap = (Constant.GAMEWIDTH - 700) / 10.f;
        int index = 0;
        for (int col = 0; col < COLS; col++) {
            SpiderStack stack = stacks.get(col);
            float x = LEFT_X + col * gap + gap/2f;
            float y = TOP_Y;
            for (int i = 0; i < stack.getCards().size(); i++) {
                CardModel card = stack.getCards().get(i);
                CardActor cardActor;
                if (create){
                    cardActor = new CardActor(card);

                    if (!cardModelCardActorHashMap.containsKey(card)) {
                        cardModelCardActorHashMap.put(card,cardActor);
                        gamePanel.addActor(cardActor);
                        if (mode == 2){
                            cardActor.setPosition(gamePanel.getWidth()/2f,-100,Align.center);
                        }else if (mode == 1){
                            cardActor.zhuan();
                            cardActor.setPosition(stockPlaceholder.getX(Align.center),stockPlaceholder.getY(Align.center),Align.center);
                        }
                    }
                }else {
                    cardActor = cardModelCardActorHashMap.get(card);
                }
                cardActor.checkFaceUp();
                cardActor.clearActions();
                float v = y - i * ROW_GAP;
                if (cardActor.getX() != x || cardActor.getY() != v) {
                    if (mode == 2) {
                        cardActor.addAction(
                                Actions.sequence(
                                        Actions.delay(col*0.02f+0.2f*i),
                                        Actions.moveToAligned(x, v, Align.bottom, 0.3f)
                                ));
                    }else if (mode == 1){
                        cardActor.addAction(
                                Actions.sequence(
                                        Actions.delay(col*0.1f),
                                        Actions.moveToAligned(x, v, Align.bottom, 0.3f),
                                        Actions.run(()->{
                                            cardActor.zhuan();
                                        })
                                ));
                    }else {
                        cardActor.addAction(Actions.moveToAligned(x, v, Align.bottom, 0.3f));
                    }
                }
            }
        }
        autoSp();

        stockPlaceholder.setVisible(!stockQueue.isEmpty());
    }

    int index = 0;

    private void autoSp() {
        int xx = 13;
        for (SpiderStack stack : stacks) {
            List<CardModel> cards = stack.getCards();
            if (cards.size()>=xx){
                CardModel cardModel = cards.get(cards.size() - 13);
                if (!cardModel.isFaceUp()){
                    break;
                }
                if (cardModel.getRank() != 13) {
                    break;
                }
                boolean auto = true;
                for (int i = cards.size()-13; i < cards.size() - 1; i++) {
                    CardModel cardModel1 = cards.get(i);
                    CardModel cardModel2 = cards.get(i+1);
                    if (cardModel1.getSuit() !=cardModel2.getSuit()){
                        auto = false;
                        break;
                    }
                    if (cardModel1.getRank() - 1!=cardModel2.getRank()){
                        auto = false;
                        break;
                    }
                }
                if (cards.size()>13){
                    auto = true;
                }
                if (auto){
                    Image image = foundationSlots.get(foundationSlotIndex);
                    foundationSlotIndex ++;
                    ArrayList<CardModel> cardModels =new ArrayList<>();
                    completedSuits.add(cardModels);
                    int i = cards.size() - xx;

                    for (int i1 = i; i1 < cards.size(); i1++) {
                        cardModels.add(cards.get(i1));
                    }
                    for (CardModel model : cardModels) {
                        cards.remove(model);
                    }
                    if (cards.size()>0) {
                        CardModel cardModel1 = cards.get(cards.size() - 1);
                        cardModel1.setFaceUp(true);
                    }
                    int index = 0;
                    moveAnimation(cardModels, index, image);
                }
            }
        }
    }

    private void moveAnimation(ArrayList<CardModel> cardModels, int index, Image image) {
        for (int i1 = 0; i1 < cardModels.size(); i1++) {
            CardModel model = cardModels.get(cardModels.size() - 1 - i1);
            CardActor cardActor = cardModelCardActorHashMap.get(model);
            cardActor.addAction(
                Actions.sequence(
                    Actions.delay(0.3f+ index++*0.1f),
                    Actions.parallel(
                            Actions.sequence(
                                Actions.moveToAligned(
                                        image.getX(Align.center),
                                        image.getY(Align.center),
                                        Align.center,
                                        0.2f),
                                    Actions.run(()->{
                                        for (SpiderStack spiderStack : stacks) {
                                            List<CardModel> cards1 = spiderStack.getCards();
                                            if (cards1.size()>0){
                                                CardModel cardModel1 = cards1.get(cards1.size() - 1);
                                                cardModelCardActorHashMap.get(cardModel1).checkFaceUp();
                                            }
                                        }
                                    })
                            ),
                            Actions.delay(0.17f,Actions.run(()->{
                                cardActor.toFront();
                            }))
                        )
                )
            );
        }
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
            card.setFaceUp(false);
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
        refreshLayout(true,1);
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
                CardActor actor = cardModelCardActorHashMap.get(top);
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
        // Output the board in the same layout the solver expects:
        // header line "spider"
        // tableau rows, bottom-up, 10 comma-separated columns (empty = "")
        // final line: 50 stock cards, top of stock first (empty tokens allowed)
        int max = 0;
        for (SpiderStack stack : stacks) {
            max = Math.max(max, stack.getCards().size());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("spider\n");
        // tableau rows (bottom to top)
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
        // stock row: top of stock first, exactly 50 entries (blank if missing)
        List<CardModel> stockList = new ArrayList<>(stockQueue);
        for (int i = 0; i < STOCK_DEALS * COLS; i++) {
            String token = i < stockList.size() ? safeCode(stockList.get(i)) : "";
            sb.append(token).append(",");
        }
        sb.append("\n");
        System.out.println(
                sb
        );
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

    public TouchInfo findTarget(Actor actor){
        if (actor instanceof CardActor) {
            CardActor cardActor = (CardActor) (actor);
            CardModel cardModel = cardActor.getCard();
            return findTochInfoByCardModel(cardModel);
        }
        return null;
    }

    private TouchInfo findTochInfoByCardModel(CardModel cardModel) {
        for (int i = 0; i < stacks.size(); i++) {
            SpiderStack stack = stacks.get(i);
            for (int i1 = 0; i1 < stack.getCards().size(); i1++) {
                CardModel card = stack.getCards().get(i1);
                if (card == cardModel) {
                    TouchInfo touchInfo = new TouchInfo();
                    touchInfo.setStackIndex(i);
                    touchInfo.setCardIndex(i1);
                    return touchInfo;
                }
            }
        }
        return null;
    }

    private class CardInputListener extends ClickListener {
        private CardDrag drag;

        @Override public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            Actor target = event.getTarget();
            Vector2 vector2 = new Vector2(x,y);
            if (target instanceof CardActor){
//                根据target找下标
                gamePanel.localToStageCoordinates(vector2);
                target.stageToLocalCoordinates(vector2);
                TouchInfo touchInfo = findTarget(target);
                if (touchInfo!=null){
                    if (checkValid(touchInfo)) {
                        List<CardModel> run =
                                stacks.get(touchInfo.getStackIndex())
                                        .getCards()
                                        .subList(touchInfo.getCardIndex(),
                                                stacks.get(touchInfo.getStackIndex())
                                                        .getCards().size());
                        List<CardActor> runCard = new ArrayList<>();
                        for (CardModel cardModel : run) {
                            CardActor cardActor = cardModelCardActorHashMap.get(cardModel);
                            runCard.add(cardActor);
                        }
                        drag = new CardDrag(touchInfo.getStackIndex(),
                                new ArrayList<CardModel>(run),
                                new ArrayList<CardActor>(runCard),
                                vector2);
                        drag.setTouchTarget(target);
                        bringToFront(drag.getMoving());
                    }
                }
            }
            return super.touchDown(event,x,y,pointer,button);
        }

        @Override public void touchDragged(InputEvent event, float x, float y, int pointer) {
            super.touchDragged(event,x,y,pointer);
            if (drag == null) return;
            for (int i = 0; i < drag.getMovingActor().size(); i++) {
                CardActor cardActor = drag.getMovingActor().get(i);
                cardActor.setPosition(x - drag.getTouchDownV2().x,y - drag.getTouchDownV2().y - i*20);
            }
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            super.touchUp(event, x, y, pointer, button);
            if (drag == null) return;
            //找目标
            Actor hit = drag.getTarget();
            if (hit instanceof CardActor) {
                TouchUpBean moveTarget = findMoveTarget((CardActor) hit,drag.getMoving());
                if (moveTarget.getCardActor()!=null){
                    List<CardModel> moving = drag.getMoving();
                    List<CardModel> targetCards = stacks.get(moveTarget.getTargetStackIndex()).getCards();
                    List<CardModel> sourceCards = stacks.get(drag.getFromCol()).getCards();
                    CardModel cardModel = moving.get(0);
                    if (targetCards.size()>0) {
                        CardModel temp = targetCards.get(targetCards.size()-1);
                        // 放置只要求rank递减1，不要求同花色（多色模式可跨花色放置）
                        if (temp.getRank() - 1 == cardModel.getRank()) {
                            for (CardModel model : moving) {
                                sourceCards.remove(model);
                                targetCards.add(model);
                            }
                            if (sourceCards.size()>0) {
                                CardModel cardModel1 = sourceCards.get(sourceCards.size() - 1);
                                cardModel1.setFaceUp(true);
                            }
                        }
                    }else {
                        for (CardModel model : moving) {
                            sourceCards.remove(model);
                            targetCards.add(model);
                        }
                    }
                }else {
                    if (moveTarget.getDistance()!=Integer.MAX_VALUE) {
                        List<CardModel> moving = drag.getMoving();
                        List<CardModel> targetCards = stacks.get(moveTarget.getTargetStackIndex()).getCards();
                        List<CardModel> sourceCards = stacks.get(drag.getFromCol()).getCards();
                        for (CardModel model : moving) {
                            sourceCards.remove(model);
                            targetCards.add(model);
                        }
                        if (sourceCards.size()>0) {
                            CardModel cardModel1 = sourceCards.get(sourceCards.size() - 1);
                            cardModel1.setFaceUp(true);
                        }
                    }
                }
                drag = null;
                refreshLayout();
            }
        }
    }

    private TouchUpBean findMoveTarget(CardActor hit, List<CardModel> moving) {
        TouchUpBean touchUpBean = new TouchUpBean();
        touchUpBean.setDistance(Integer.MAX_VALUE);
        for (int i = 0; i < stacks.size(); i++) {
            SpiderStack stack = stacks.get(i);
            if (stack.getCards().size()>0) {
                CardModel cardModel = stack.getCards().get(stack.getCards().size() - 1);
                if (!moving.contains(cardModel)) {
                    CardActor cardActor = cardModelCardActorHashMap.get(cardModel);
                    float x = cardActor.getX(Align.center);
                    float y = cardActor.getY(Align.center);
                    float x1 = hit.getX(Align.center);
                    float y1 = hit.getY(Align.center);
                    if (Math.abs(x - x1)< CardConstant.CARD_W&&Math.abs(y - y1)< CARD_H) {
                        float currentMinDistance = Math.abs(x - x1) * Math.abs(y - y1);
                        if (currentMinDistance<touchUpBean.getDistance()){
                            touchUpBean.setDistance(currentMinDistance);
                            touchUpBean.setCardActor(cardModel);
                            touchUpBean.setTargetStackIndex(i);
                        }
                    }
                }
            }else {
                Image image = deckImgs.get(i);
                float x = image.getX(Align.center);
                float y = image.getY(Align.center);
                float x1 = hit.getX(Align.center);
                float y1 = hit.getY(Align.center);
                if (Math.abs(x - x1)< CardConstant.CARD_W&&Math.abs(y - y1)< CARD_H) {
                    float currentMinDistance = Math.abs(x - x1) * Math.abs(y - y1);
                    if (currentMinDistance<touchUpBean.getDistance()){
                        touchUpBean.setDistance(currentMinDistance);
                        touchUpBean.setCardActor(null);
                        touchUpBean.setTargetStackIndex(i);
                    }
                }
            }
        }
        return touchUpBean;
    }


    private boolean checkValid(TouchInfo touchInfo) {
        SpiderStack stack = stacks.get(touchInfo.getStackIndex());
        CardModel cardModel = stack.getCards().get(touchInfo.getCardIndex());
        if (!cardModel.isFaceUp()) return false;
        for (int i = touchInfo.getCardIndex(); i < stack.getCards().size() - 1; i++) {
            CardModel temp1 = stack.getCards().get(i);
            CardModel temp2 = stack.getCards().get(i+1);
            // 只有同花色且递减的序列才能一起拖动
            if (temp1.getSuit() != temp2.getSuit() || temp1.getRank() != temp2.getRank() + 1) {
                return false;
            }
        }
        return true;
    }

    private CardActor findActor(CardModel card) {
        for (Actor actor : gamePanel.getChildren()) {
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
            CardActor actor = cardModelCardActorHashMap.get(card);
            if (actor != null) {
                actor.toFront();
            }
        }
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
}
