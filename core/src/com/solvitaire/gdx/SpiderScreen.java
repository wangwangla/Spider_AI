package com.solvitaire.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kw.gdx.asset.Asset;
import com.solvitaire.app.SpiderSolveResult;
import com.solvitaire.app.SpiderSolutionStep;
import com.solvitaire.app.SpiderSolverService;
import com.solvitaire.gdx.spider.SpiderBoard;
import com.solvitaire.gdx.spider.SpiderCard;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SpiderScreen extends ScreenAdapter {
   private static final float WORLD_WIDTH = 1760f;
   private static final float WORLD_HEIGHT = 920f;
   private static final float CARD_WIDTH = 92f;
   private static final float CARD_HEIGHT = 132f;
   private static final float LEFT_X = 38f;
   private static final float TABLEAU_TOP_Y = 770f;
   private static final float COLUMN_GAP = 10f;
   private static final float CONTROL_X = 1080f;
   private static final float AUTOPLAY_DELAY = 0.34f;

   private final FitViewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
   private final Vector3 touchPoint = new Vector3();
   private final GlyphLayout glyphLayout = new GlyphLayout();
   private final SpriteBatch batch = new SpriteBatch();
   private final ShapeRenderer shapes = new ShapeRenderer();
   private final BitmapFont titleFont = Asset.getAsset().loadBitFont("cocos/font/ntcb_40.fnt");
   private final BitmapFont textFont =  Asset.getAsset().loadBitFont("cocos/font/ntcb_40.fnt");
   private final BitmapFont smallFont =  Asset.getAsset().loadBitFont("cocos/font/ntcb_40.fnt");
   private final ExecutorService executor = Executors.newSingleThreadExecutor();
   private final SpiderBoard board = new SpiderBoard();
   private final SpiderSolverService solverService = new SpiderSolverService();
   private final FreeCellBaseGame game;
   private final UiButton[] buttons;

   private Texture deckTexture;
   private TextureRegion[][] deckRegions;
   private Future<SpiderSolveResult> solveFuture;
   private String activeSolveSignature;
   private List<SpiderSolutionStep> solutionSteps = Collections.emptyList();
   private int solutionIndex;
   private boolean autoplay;
   private boolean autoplayPending;
   private float autoplayTimer;
   private long currentSeed;
   private int selectedSuitMode = 1;
   private String statusText = "Select a face-up descending run, then choose a target tableau.";
   private Selection selection;

   public SpiderScreen(FreeCellBaseGame game) {
      this.game = game;
      this.buttons = new UiButton[]{
         new UiButton("FREECELL", CONTROL_X, 840f, 302f, 42f),
         new UiButton("NEW", CONTROL_X, 782f, 146f, 42f),
         new UiButton("RESET", CONTROL_X + 156f, 782f, 146f, 42f),
         new UiButton("SOLVE", CONTROL_X, 726f, 146f, 42f),
         new UiButton("STEP", CONTROL_X + 156f, 726f, 146f, 42f),
         new UiButton("AUTO", CONTROL_X, 670f, 146f, 42f),
         new UiButton("STOP", CONTROL_X + 156f, 670f, 146f, 42f),
         new UiButton("DEAL", CONTROL_X, 614f, 302f, 42f),
         new UiButton("S1", CONTROL_X, 558f, 94f, 42f),
         new UiButton("S2", CONTROL_X + 104f, 558f, 94f, 42f),
         new UiButton("S4", CONTROL_X + 208f, 558f, 94f, 42f)
      };
      this.titleFont.getData().setScale(1.35f);
      this.textFont.getData().setScale(1f);
      this.smallFont.getData().setScale(0.82f);
      this.startNewDeal();
   }

   @Override
   public void show() {
      Gdx.input.setInputProcessor(new InputHandler());
      this.deckTexture = new Texture(Gdx.files.internal("texture/8BitDeck.png"));
      this.deckRegions = TextureRegion.split(this.deckTexture, this.deckTexture.getWidth() / 13, this.deckTexture.getHeight() / 4);
   }

   @Override
   public void render(float delta) {
      this.pollSolveFuture();
      this.updateAutoplay(delta);

      Gdx.gl.glClearColor(0.08f, 0.11f, 0.14f, 1f);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

      this.viewport.apply();
      this.batch.setProjectionMatrix(this.viewport.getCamera().combined);
      this.shapes.setProjectionMatrix(this.viewport.getCamera().combined);

      this.drawBackground();
      this.drawCards();
      this.drawHighlights();
      this.drawText();
   }

   @Override
   public void resize(int width, int height) {
      this.viewport.update(width, height, true);
   }

   @Override
   public void dispose() {
      this.cancelSolveRequest();
      this.executor.shutdownNow();
      this.batch.dispose();
      this.shapes.dispose();
      this.titleFont.dispose();
      this.textFont.dispose();
      this.smallFont.dispose();
      if (this.deckTexture != null) {
         this.deckTexture.dispose();
      }
   }

   private void startNewDeal() {
      this.currentSeed = System.currentTimeMillis() & Integer.MAX_VALUE;
      this.board.generateDeal(this.selectedSuitMode, this.currentSeed);
      this.selection = null;
      this.clearSolutionState();
      this.statusText = "New Spider deal ready. Use DEAL for a new row or SOLVE for the current position.";
   }

   private void resetDeal() {
      this.board.generateDeal(this.selectedSuitMode, this.currentSeed);
      this.selection = null;
      this.clearSolutionState();
      this.statusText = "Board reset to the initial Spider deal.";
   }

   private void setSuitMode(int suitMode) {
      if (this.selectedSuitMode == suitMode) {
         return;
      }
      this.selectedSuitMode = suitMode;
      this.startNewDeal();
   }

   private void requestSolve(boolean autoStart) {
      if (this.solveFuture != null) {
         this.autoplayPending = this.autoplayPending || autoStart;
         this.statusText = "The solver is already running for the current Spider position.";
         return;
      }
      if (this.solutionIndex < this.solutionSteps.size()) {
         if (autoStart) {
            this.autoplay = true;
            this.autoplayPending = false;
            this.autoplayTimer = 0f;
            this.statusText = "Continuing the existing Spider solution.";
         } else {
            this.statusText = "A Spider solution is already available for this position.";
         }
         return;
      }

      final String boardState = this.board.exportBoardState();
      this.activeSolveSignature = boardState;
      this.autoplayPending = autoStart;
      this.solveFuture = this.executor.submit(new Callable<SpiderSolveResult>() {
         @Override
         public SpiderSolveResult call() {
            return SpiderScreen.this.solverService.solveBoard(boardState);
         }
      });
      this.statusText = autoStart ? "Solving Spider for autoplay..." : "Solving Spider...";
   }

   private void pollSolveFuture() {
      if (this.solveFuture == null || !this.solveFuture.isDone()) {
         return;
      }

      try {
         SpiderSolveResult result = this.solveFuture.get();
         if (!this.board.exportBoardState().equals(this.activeSolveSignature)) {
            this.statusText = "Board changed while solving. Discarded the stale Spider result.";
            this.clearAsyncSolveState();
            return;
         }

         if (result.isSolved()) {
            this.solutionSteps = result.getSteps();
            this.solutionIndex = 0;
            this.statusText = result.getSummary();
            if (this.autoplayPending) {
               this.autoplay = true;
               this.autoplayTimer = 0f;
               this.autoplayPending = false;
            }
         } else {
            this.solutionSteps = Collections.emptyList();
            this.solutionIndex = 0;
            this.autoplay = false;
            this.autoplayPending = false;
            this.statusText = result.getSummary();
         }
      } catch (InterruptedException exception) {
         Thread.currentThread().interrupt();
         this.statusText = "Spider solver interrupted.";
      } catch (ExecutionException exception) {
         Throwable cause = exception.getCause() == null ? exception : exception.getCause();
         this.statusText = "Spider solver failed: " + cause.getMessage();
      } finally {
         this.clearAsyncSolveState();
      }
   }

   private void updateAutoplay(float delta) {
      if (!this.autoplay || this.solveFuture != null) {
         return;
      }
      if (this.solutionIndex >= this.solutionSteps.size()) {
         this.autoplay = false;
         return;
      }

      this.autoplayTimer += delta;
      if (this.autoplayTimer >= AUTOPLAY_DELAY) {
         this.autoplayTimer = 0f;
         if (!this.executeNextSolutionStep()) {
            this.autoplay = false;
         }
      }
   }

   private boolean executeNextSolutionStep() {
      if (this.solutionIndex >= this.solutionSteps.size()) {
         this.statusText = this.board.isSolved() ? "Spider solved." : "No more Spider solution steps remain.";
         return false;
      }

      SpiderSolutionStep step = this.solutionSteps.get(this.solutionIndex);
      if (!this.board.applySolutionStep(step)) {
         this.statusText = "The stored Spider solution no longer matches the current board.";
         this.clearSolutionState();
         return false;
      }

      ++this.solutionIndex;
      this.selection = null;
      this.statusText = step.getDescription();
      if (this.board.isSolved()) {
         this.autoplay = false;
         this.statusText = "Spider solved.";
      }
      return true;
   }

   private void drawBackground() {
      this.shapes.begin(ShapeRenderer.ShapeType.Filled);
      this.shapes.setColor(new Color(0.08f, 0.11f, 0.14f, 1f));
      this.shapes.rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
      this.shapes.setColor(new Color(0.12f, 0.20f, 0.28f, 1f));
      this.shapes.rect(0f, 0f, 1044f, WORLD_HEIGHT);
      this.shapes.setColor(new Color(0.92f, 0.84f, 0.70f, 0.10f));
      this.shapes.rect(CONTROL_X - 24f, 48f, 350f, 850f);

      for (int column = 0; column < this.board.tableauCount(); ++column) {
         this.drawSlot(this.tableauX(column), TABLEAU_TOP_Y, new Color(0.17f, 0.25f, 0.35f, 0.90f));
      }

      this.drawSlot(this.stockX(), 390f, new Color(0.16f, 0.23f, 0.32f, 0.92f));
      for (int index = 0; index < 8; ++index) {
         this.drawSlot(this.completedX(index), this.completedY(index), new Color(0.16f, 0.23f, 0.32f, 0.92f));
      }

      for (UiButton button : this.buttons) {
         button.draw(this.shapes, this.isButtonEnabled(button.label), this.isButtonActive(button.label));
      }
      this.shapes.end();
   }

   private void drawCards() {
      float tableauGap = this.tableauGap();

      this.shapes.begin(ShapeRenderer.ShapeType.Filled);
      int stockRowsRemaining = 5 - this.board.getNextDealRowIndex();
      for (int row = 0; row < stockRowsRemaining; ++row) {
         this.drawFaceDownCardShape(this.stockX() + row * 7f, 390f - row * 5f);
      }

      for (int column = 0; column < this.board.tableauCount(); ++column) {
         List<SpiderCard> tableau = this.board.getTableau(column);
         for (int row = 0; row < tableau.size(); ++row) {
            SpiderCard card = tableau.get(row);
            if (!card.isFaceUp()) {
               this.drawFaceDownCardShape(this.tableauX(column), this.tableauCardY(row, tableauGap));
            }
         }
      }
      this.shapes.end();

      this.batch.begin();
      for (int column = 0; column < this.board.tableauCount(); ++column) {
         List<SpiderCard> tableau = this.board.getTableau(column);
         for (int row = 0; row < tableau.size(); ++row) {
            SpiderCard card = tableau.get(row);
            if (card.isFaceUp()) {
               this.drawFaceUpCard(card.getCardId(), this.tableauX(column), this.tableauCardY(row, tableauGap));
            }
         }
      }

      for (int index = 0; index < this.board.completedSuitCount(); ++index) {
         int completedValue = this.board.getCompletedSuitValue(index);
         if (completedValue > 0) {
            this.drawFaceUpCard(completedValue, this.completedX(index), this.completedY(index));
         }
      }
      this.batch.end();
   }

   private void drawHighlights() {
      if (this.selection == null) {
         return;
      }

      float tableauGap = this.tableauGap();
      List<SpiderCard> tableau = this.board.getTableau(this.selection.column);

      this.shapes.begin(ShapeRenderer.ShapeType.Filled);
      this.shapes.setColor(new Color(1f, 0.86f, 0.24f, 0.30f));
      for (int row = this.selection.startIndex; row < tableau.size(); ++row) {
         this.shapes.rect(this.tableauX(this.selection.column), this.tableauCardY(row, tableauGap), CARD_WIDTH, CARD_HEIGHT);
      }
      this.shapes.end();

      this.shapes.begin(ShapeRenderer.ShapeType.Line);
      this.shapes.setColor(new Color(1f, 0.92f, 0.28f, 0.96f));
      for (int row = this.selection.startIndex; row < tableau.size(); ++row) {
         this.shapes.rect(this.tableauX(this.selection.column), this.tableauCardY(row, tableauGap), CARD_WIDTH, CARD_HEIGHT);
      }
      this.shapes.end();
   }

   private void drawText() {
      this.batch.begin();

      this.titleFont.setColor(new Color(0.97f, 0.95f, 0.90f, 1f));
      this.titleFont.draw(this.batch, "Spider Solver", 40f, 885f);

      this.textFont.setColor(new Color(0.95f, 0.92f, 0.86f, 1f));
      this.textFont.draw(this.batch, "Tableau", LEFT_X, 835f);
      this.textFont.draw(this.batch, "Stock", this.stockX(), 545f);
      this.textFont.draw(this.batch, "Completed", this.completedX(0), 545f);

      for (UiButton button : this.buttons) {
         button.drawLabel(this.batch, this.textFont);
      }

      this.textFont.draw(this.batch, "Seed: " + this.currentSeed, CONTROL_X - 2f, 504f);
      this.textFont.draw(this.batch, "Suit mode: " + this.selectedSuitMode, CONTROL_X - 2f, 476f);
      this.textFont.draw(this.batch, "Stock cards: " + this.board.stockRemainingCards(), CONTROL_X - 2f, 448f);
      this.textFont.draw(this.batch, "Completed suits: " + this.board.completedSuitCount() + " / 8", CONTROL_X - 2f, 420f);
      this.textFont.draw(this.batch, "Solver: " + this.solveStateText(), CONTROL_X - 2f, 392f);
      this.textFont.draw(this.batch, "Solution: " + this.solutionProgressText(), CONTROL_X - 2f, 364f);

      this.smallFont.setColor(new Color(0.91f, 0.89f, 0.83f, 1f));
      this.drawWrapped(this.smallFont, this.statusText, CONTROL_X - 2f, 330f, 306f);
      if (this.solutionIndex < this.solutionSteps.size()) {
         this.smallFont.setColor(new Color(0.84f, 0.93f, 0.88f, 1f));
         this.drawWrapped(this.smallFont, "Next: " + this.solutionSteps.get(this.solutionIndex).getDescription(), CONTROL_X - 2f, 230f, 306f);
      }

      this.smallFont.setColor(new Color(0.84f, 0.85f, 0.80f, 1f));
      this.drawWrapped(this.smallFont, "Click stock or DEAL to deal a new row. Empty columns block dealing.", CONTROL_X - 2f, 136f, 306f);
      this.drawWrapped(this.smallFont, "Keys: D deal, 1/2/4 suit mode, N new, R reset, S solve, Space step, A auto.", CONTROL_X - 2f, 98f, 306f);

      this.batch.end();
   }

   private void drawFaceUpCard(int cardId, float x, float y) {
      TextureRegion region = this.cardRegion(cardId / 100, cardId % 100);
      this.batch.setColor(Color.WHITE);
      this.batch.draw(region, x, y, CARD_WIDTH, CARD_HEIGHT);

      SpiderCard helper = SpiderCard.fromId(cardId, true);
      this.smallFont.setColor(helper.getSuit() == 2 || helper.getSuit() == 3 ? new Color(0.78f, 0.19f, 0.19f, 1f) : new Color(0.12f, 0.14f, 0.18f, 1f));
      this.smallFont.draw(this.batch, helper.getCode(), x + 6f, y + CARD_HEIGHT - 8f);
      this.smallFont.draw(this.batch, helper.getCode(), x + 6f, y + 17f);
   }

   private void drawFaceDownCardShape(float x, float y) {
      this.shapes.setColor(new Color(0.16f, 0.25f, 0.46f, 0.98f));
      this.shapes.rect(x, y, CARD_WIDTH, CARD_HEIGHT);
      this.shapes.setColor(new Color(0.85f, 0.86f, 0.92f, 0.14f));
      this.shapes.rect(x + 6f, y + 6f, CARD_WIDTH - 12f, CARD_HEIGHT - 12f);
      this.shapes.setColor(new Color(0.31f, 0.44f, 0.70f, 0.42f));
      this.shapes.rect(x + 12f, y + 12f, CARD_WIDTH - 24f, CARD_HEIGHT - 24f);
   }

   private TextureRegion cardRegion(int suit, int rank) {
      return this.deckRegions[this.suitRow(suit)][this.rankColumn(rank)];
   }

   private int suitRow(int suit) {
      switch (suit) {
         case 1:
            return 3;
         case 2:
            return 0;
         case 3:
            return 2;
         case 4:
            return 1;
         default:
            return 0;
      }
   }

   private int rankColumn(int rank) {
      return rank == 1 ? 12 : rank - 2;
   }

   private void handlePointer(float screenX, float screenY) {
      this.touchPoint.set(screenX, screenY, 0f);
      this.viewport.unproject(this.touchPoint);

      UiButton button = this.findButton(this.touchPoint.x, this.touchPoint.y);
      if (button != null) {
         this.handleButton(button.label);
         return;
      }

      if (this.stockBounds().contains(this.touchPoint.x, this.touchPoint.y)) {
         this.selection = null;
         SpiderBoard.MoveResult dealResult = this.board.deal();
         if (dealResult.isSuccess()) {
            this.clearSolvedPathAfterManualAction();
         }
         this.statusText = dealResult.getMessage();
         return;
      }

      TableauHit hit = this.findTableauHit(this.touchPoint.x, this.touchPoint.y);
      if (this.selection == null) {
         if (hit == null || !hit.selectable) {
            this.statusText = "Select a face-up descending Spider run.";
            return;
         }
         this.selection = new Selection(hit.column, hit.startIndex);
         this.statusText = this.selectionMessage();
         return;
      }

      if (hit == null) {
         this.selection = null;
         this.statusText = "Selection cleared.";
         return;
      }
      if (this.selection.column == hit.column && this.selection.startIndex == hit.startIndex) {
         this.selection = null;
         this.statusText = "Selection cleared.";
         return;
      }

      SpiderBoard.MoveResult result = this.board.move(this.selection.column, this.selection.startIndex, hit.column);
      if (result.isSuccess()) {
         this.selection = null;
         this.clearSolvedPathAfterManualAction();
         this.statusText = this.board.isSolved() ? "Spider solved." : result.getMessage();
      } else if (hit.selectable) {
         this.selection = new Selection(hit.column, hit.startIndex);
         this.statusText = this.selectionMessage();
      } else {
         this.statusText = result.getMessage();
      }
   }

   private void handleButton(String label) {
      if ("FREECELL".equals(label)) {
         return;
      }
      if ("NEW".equals(label)) {
         this.startNewDeal();
      } else if ("RESET".equals(label)) {
         this.resetDeal();
      } else if ("SOLVE".equals(label)) {
         this.requestSolve(false);
      } else if ("STEP".equals(label)) {
         if (this.solutionIndex < this.solutionSteps.size()) {
            this.executeNextSolutionStep();
         } else {
            this.requestSolve(false);
         }
      } else if ("AUTO".equals(label)) {
         if (this.solutionIndex < this.solutionSteps.size()) {
            this.autoplay = true;
            this.autoplayTimer = 0f;
            this.statusText = "Spider autoplay started.";
         } else {
            this.requestSolve(true);
         }
      } else if ("STOP".equals(label)) {
         this.autoplay = false;
         this.autoplayPending = false;
         this.statusText = "Spider autoplay stopped.";
      } else if ("DEAL".equals(label)) {
         SpiderBoard.MoveResult result = this.board.deal();
         if (result.isSuccess()) {
            this.clearSolvedPathAfterManualAction();
         }
         this.statusText = result.getMessage();
      } else if ("S1".equals(label)) {
         this.setSuitMode(1);
      } else if ("S2".equals(label)) {
         this.setSuitMode(2);
      } else if ("S4".equals(label)) {
         this.setSuitMode(4);
      }
      this.selection = null;
   }

   private UiButton findButton(float x, float y) {
      for (UiButton button : this.buttons) {
         if (button.bounds.contains(x, y)) {
            return button;
         }
      }
      return null;
   }

   private TableauHit findTableauHit(float x, float y) {
      float tableauGap = this.tableauGap();
      for (int column = 0; column < this.board.tableauCount(); ++column) {
         List<SpiderCard> tableau = this.board.getTableau(column);
         if (tableau.isEmpty()) {
            Rectangle bounds = new Rectangle(this.tableauX(column), TABLEAU_TOP_Y, CARD_WIDTH, CARD_HEIGHT);
            if (bounds.contains(x, y)) {
               return new TableauHit(column, -1, false);
            }
            continue;
         }

         for (int row = tableau.size() - 1; row >= 0; --row) {
            float cardY = this.tableauCardY(row, tableauGap);
            float hitY = row == tableau.size() - 1 ? cardY : cardY + CARD_HEIGHT - tableauGap;
            float hitHeight = row == tableau.size() - 1 ? CARD_HEIGHT : tableauGap;
            Rectangle bounds = new Rectangle(this.tableauX(column), hitY, CARD_WIDTH, hitHeight);
            if (bounds.contains(x, y)) {
               return new TableauHit(column, row, this.board.canSelectRun(column, row));
            }
         }
      }
      return null;
   }

   private float tableauGap() {
      int tallest = 1;
      for (int column = 0; column < this.board.tableauCount(); ++column) {
         tallest = Math.max(tallest, this.board.getTableau(column).size());
      }
      if (tallest <= 1) {
         return 30f;
      }
      float gap = (TABLEAU_TOP_Y - 72f) / (tallest - 1);
      return Math.max(16f, Math.min(30f, gap));
   }

   private float tableauCardY(int row, float gap) {
      return TABLEAU_TOP_Y - row * gap;
   }

   private float tableauX(int column) {
      return LEFT_X + column * (CARD_WIDTH + COLUMN_GAP);
   }

   private float stockX() {
      return CONTROL_X + 16f;
   }

   private float completedX(int index) {
      return CONTROL_X + 16f + (index % 4) * (CARD_WIDTH + 8f);
   }

   private float completedY(int index) {
      return 375f - (index / 4) * (CARD_HEIGHT + 14f);
   }

   private Rectangle stockBounds() {
      return new Rectangle(this.stockX(), 390f - 24f, CARD_WIDTH + 30f, CARD_HEIGHT + 28f);
   }

   private void drawSlot(float x, float y, Color color) {
      this.shapes.setColor(color);
      this.shapes.rect(x, y, CARD_WIDTH, CARD_HEIGHT);
      this.shapes.setColor(new Color(0.95f, 0.91f, 0.84f, 0.14f));
      this.shapes.rect(x + 5f, y + 5f, CARD_WIDTH - 10f, CARD_HEIGHT - 10f);
   }

   private void drawWrapped(BitmapFont font, String text, float x, float y, float width) {
      this.glyphLayout.setText(font, text, Color.WHITE, width, 8, true);
      font.draw(this.batch, this.glyphLayout, x, y);
   }

   private String solveStateText() {
      if (this.solveFuture != null) {
         return "running";
      }
      return this.autoplay ? "autoplay" : "idle";
   }

   private String solutionProgressText() {
      if (this.solutionSteps.isEmpty()) {
         return "not ready";
      }
      return this.solutionIndex + " / " + this.solutionSteps.size();
   }

   private String selectionMessage() {
      if (this.selection == null) {
         return "Selection cleared.";
      }
      int cardCount = this.board.getTableau(this.selection.column).size() - this.selection.startIndex;
      return "Selected a " + cardCount + "-card run from tableau " + (this.selection.column + 1) + ".";
   }

   private boolean isButtonEnabled(String label) {
      if ("STEP".equals(label)) {
         return this.solutionIndex < this.solutionSteps.size() || this.solveFuture == null;
      }
      if ("AUTO".equals(label)) {
         return this.solveFuture == null || this.solutionIndex < this.solutionSteps.size();
      }
      if ("STOP".equals(label)) {
         return this.autoplay || this.solveFuture != null;
      }
      if ("DEAL".equals(label)) {
         return this.board.canDeal();
      }
      return true;
   }

   private boolean isButtonActive(String label) {
      return ("S1".equals(label) && this.selectedSuitMode == 1)
         || ("S2".equals(label) && this.selectedSuitMode == 2)
         || ("S4".equals(label) && this.selectedSuitMode == 4);
   }

   private void clearSolvedPathAfterManualAction() {
      this.selection = null;
      this.autoplay = false;
      this.autoplayPending = false;
      this.solutionSteps = Collections.emptyList();
      this.solutionIndex = 0;
      this.cancelSolveRequest();
   }

   private void clearSolutionState() {
      this.autoplay = false;
      this.autoplayPending = false;
      this.solutionSteps = Collections.emptyList();
      this.solutionIndex = 0;
      this.cancelSolveRequest();
   }

   private void clearAsyncSolveState() {
      this.solveFuture = null;
      this.activeSolveSignature = null;
   }

   private void cancelSolveRequest() {
      if (this.solveFuture != null) {
         this.solveFuture.cancel(true);
      }
      this.clearAsyncSolveState();
   }

   private final class InputHandler extends InputAdapter {
      @Override
      public boolean touchDown(int screenX, int screenY, int pointer, int button) {
         if (button != Input.Buttons.LEFT) {
            return false;
         }
         SpiderScreen.this.handlePointer(screenX, screenY);
         return true;
      }

      @Override
      public boolean keyDown(int keycode) {
         if (keycode == Input.Keys.F) {
            SpiderScreen.this.handleButton("FREECELL");
            return true;
         }
         if (keycode == Input.Keys.N) {
            SpiderScreen.this.handleButton("NEW");
            return true;
         }
         if (keycode == Input.Keys.R) {
            SpiderScreen.this.handleButton("RESET");
            return true;
         }
         if (keycode == Input.Keys.S) {
            SpiderScreen.this.handleButton("SOLVE");
            return true;
         }
         if (keycode == Input.Keys.SPACE) {
            SpiderScreen.this.handleButton("STEP");
            return true;
         }
         if (keycode == Input.Keys.A) {
            SpiderScreen.this.handleButton("AUTO");
            return true;
         }
         if (keycode == Input.Keys.D) {
            SpiderScreen.this.handleButton("DEAL");
            return true;
         }
         if (keycode == Input.Keys.NUM_1) {
            SpiderScreen.this.handleButton("S1");
            return true;
         }
         if (keycode == Input.Keys.NUM_2) {
            SpiderScreen.this.handleButton("S2");
            return true;
         }
         if (keycode == Input.Keys.NUM_4) {
            SpiderScreen.this.handleButton("S4");
            return true;
         }
         if (keycode == Input.Keys.ESCAPE) {
            SpiderScreen.this.selection = null;
            SpiderScreen.this.autoplay = false;
            SpiderScreen.this.autoplayPending = false;
            SpiderScreen.this.statusText = "Selection cleared.";
            return true;
         }
         return false;
      }
   }

   private static final class Selection {
      final int column;
      final int startIndex;

      Selection(int column, int startIndex) {
         this.column = column;
         this.startIndex = startIndex;
      }
   }

   private static final class TableauHit {
      final int column;
      final int startIndex;
      final boolean selectable;

      TableauHit(int column, int startIndex, boolean selectable) {
         this.column = column;
         this.startIndex = startIndex;
         this.selectable = selectable;
      }
   }

   private static final class UiButton {
      final String label;
      final Rectangle bounds;

      UiButton(String label, float x, float y, float width, float height) {
         this.label = label;
         this.bounds = new Rectangle(x, y, width, height);
      }

      void draw(ShapeRenderer shapes, boolean enabled, boolean active) {
         Color fill = active ? new Color(0.83f, 0.67f, 0.32f, 0.96f)
            : enabled ? new Color(0.63f, 0.71f, 0.83f, 0.92f)
            : new Color(0.34f, 0.36f, 0.41f, 0.74f);
         shapes.setColor(fill);
         shapes.rect(this.bounds.x, this.bounds.y, this.bounds.width, this.bounds.height);
         shapes.setColor(new Color(0.11f, 0.13f, 0.17f, 0.20f));
         shapes.rect(this.bounds.x + 4f, this.bounds.y + 4f, this.bounds.width - 8f, this.bounds.height - 8f);
      }

      void drawLabel(SpriteBatch batch, BitmapFont font) {
         GlyphLayout layout = new GlyphLayout(font, this.label);
         font.setColor(new Color(1f, 1f, 1f, 1f));
         font.draw(batch, this.label, this.bounds.x + (this.bounds.width - layout.width) / 2f, this.bounds.y + (this.bounds.height + layout.height) / 2f);
      }
   }
}
