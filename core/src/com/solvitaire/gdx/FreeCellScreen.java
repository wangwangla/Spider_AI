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
import com.solvitaire.app.FreeCellSolutionStep;
import com.solvitaire.app.FreeCellSolveResult;
import com.solvitaire.app.FreeCellSolverService;
import com.solvitaire.gdx.freecell.FreeCellBoard;
import com.solvitaire.gdx.freecell.FreeCellCard;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FreeCellScreen extends ScreenAdapter {
   private static final float WORLD_WIDTH = 1600f;
   private static final float WORLD_HEIGHT = 900f;
   private static final float CARD_WIDTH = 108f;
   private static final float CARD_HEIGHT = 150f;
   private static final float LEFT_X = 54f;
   private static final float TOP_ROW_Y = 710f;
   private static final float TABLEAU_TOP_Y = 610f;
   private static final float COLUMN_GAP = 18f;
   private static final float SLOT_GAP = 76f;
   private static final float CONTROL_X = 1145f;
   private static final float AUTOPLAY_DELAY = 0.35f;

   private final FitViewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
   private final Vector3 touchPoint = new Vector3();
   private final GlyphLayout glyphLayout = new GlyphLayout();
   private final SpriteBatch batch = new SpriteBatch();
   private final ShapeRenderer shapes = new ShapeRenderer();
   private final BitmapFont titleFont = Asset.getAsset().loadBitFont("cocos/font/ntcb_40.fnt");
   private final BitmapFont textFont = Asset.getAsset().loadBitFont("cocos/font/ntcb_40.fnt");
   private final BitmapFont smallFont =Asset.getAsset().loadBitFont("cocos/font/ntcb_40.fnt");
   private final ExecutorService executor = Executors.newSingleThreadExecutor();
   private final FreeCellBoard board = new FreeCellBoard();
   private final FreeCellSolverService solverService = new FreeCellSolverService();
   private final UiButton[] buttons;
   private final FreeCellBaseGame game;

   private Texture deckTexture;
   private TextureRegion[][] deckRegions;
   private Future<FreeCellSolveResult> solveFuture;
   private String activeSolveSignature;
   private String initialBoardState;
   private List<FreeCellSolutionStep> solutionSteps = Collections.emptyList();
   private int solutionIndex;
   private boolean autoplay;
   private boolean autoplayPending;
   private float autoplayTimer;
   private long currentSeed;
   private String statusText = "Click a movable card, then click a target pile.";
   private Selection selection;

   public FreeCellScreen(FreeCellBaseGame game) {
      this.game = game;
      this.buttons = new UiButton[]{
         new UiButton("SPIDER", CONTROL_X, 822f, 288f, 44f),
         new UiButton("NEW", CONTROL_X, 765f, 138f, 44f),
         new UiButton("RESET", CONTROL_X + 150f, 765f, 138f, 44f),
         new UiButton("SOLVE", CONTROL_X, 708f, 138f, 44f),
         new UiButton("STEP", CONTROL_X + 150f, 708f, 138f, 44f),
         new UiButton("AUTO", CONTROL_X, 651f, 138f, 44f),
         new UiButton("STOP", CONTROL_X + 150f, 651f, 138f, 44f)
      };
      this.titleFont.getData().setScale(1.35f);
      this.textFont.getData().setScale(1.0f);
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

      Gdx.gl.glClearColor(0.07f, 0.18f, 0.14f, 1f);
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
      if (this.solveFuture != null) {
         this.solveFuture.cancel(true);
      }
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
      this.board.generateDeal(this.currentSeed);
      this.initialBoardState = this.board.exportBoardState();
      this.selection = null;
      this.clearSolutionState();
      this.statusText = "New deal ready. Use SOLVE or AUTO when you want the solver.";
   }

   private void resetDeal() {
      if (this.initialBoardState == null) {
         return;
      }
      this.board.loadBoardState(this.initialBoardState, this.currentSeed);
      this.selection = null;
      this.clearSolutionState();
      this.statusText = "Board reset to the original deal.";
   }

   private void requestSolve(boolean autoStart) {
      if (this.solveFuture != null) {
         this.autoplayPending = this.autoplayPending || autoStart;
         this.statusText = "The solver is already running for the current request.";
         return;
      }
      if (this.solutionIndex < this.solutionSteps.size()) {
         if (autoStart) {
            this.autoplay = true;
            this.autoplayPending = false;
            this.autoplayTimer = 0f;
            this.statusText = "Continuing the existing solution.";
         } else {
            this.statusText = "A solution is already ready for this position.";
         }
         return;
      }

      final String boardState = this.board.exportBoardState();
      this.activeSolveSignature = boardState;
      this.autoplayPending = autoStart;
      this.solveFuture = this.executor.submit(new Callable<FreeCellSolveResult>() {
         @Override
         public FreeCellSolveResult call() {
            return FreeCellScreen.this.solverService.solveBoard(boardState);
         }
      });
      this.statusText = autoStart ? "Solving current position for autoplay..." : "Solving current position...";
   }

   private void pollSolveFuture() {
      if (this.solveFuture == null || !this.solveFuture.isDone()) {
         return;
      }

      try {
         FreeCellSolveResult result = this.solveFuture.get();
         if (!this.board.exportBoardState().equals(this.activeSolveSignature)) {
            this.statusText = "Board changed while solving. Discarded the stale result.";
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
         this.statusText = "Solver interrupted.";
      } catch (ExecutionException exception) {
         Throwable cause = exception.getCause() == null ? exception : exception.getCause();
         this.statusText = "Solver failed: " + cause.getMessage();
      } finally {
         this.clearAsyncSolveState();
      }
   }

   private void updateAutoplay(float delta) {
      if (!this.autoplay) {
         return;
      }
      if (this.solveFuture != null) {
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
         this.statusText = this.board.isSolved() ? "Puzzle solved." : "No more solution steps remain.";
         return false;
      }

      FreeCellSolutionStep step = this.solutionSteps.get(this.solutionIndex);
      if (!this.board.applySolutionStep(step)) {
         this.statusText = "The saved solution no longer matches the current board.";
         this.clearSolutionState();
         return false;
      }

      ++this.solutionIndex;
      this.selection = null;
      this.statusText = step.getDescription();
      if (this.board.isSolved()) {
         this.autoplay = false;
         this.statusText = "Puzzle solved.";
      }
      return true;
   }

   private void drawBackground() {
      this.shapes.begin(ShapeRenderer.ShapeType.Filled);
      this.shapes.setColor(new Color(0.06f, 0.14f, 0.11f, 1f));
      this.shapes.rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
      this.shapes.setColor(new Color(0.10f, 0.27f, 0.20f, 1f));
      this.shapes.rect(0f, 0f, 1115f, WORLD_HEIGHT);
      this.shapes.setColor(new Color(0.95f, 0.90f, 0.78f, 0.10f));
      this.shapes.rect(CONTROL_X - 26f, 72f, 350f, 760f);

      for (int index = 0; index < this.board.freeCellCount(); ++index) {
         this.drawSlot(this.cellX(index), TOP_ROW_Y, this.slotColor());
      }
      for (int index = 0; index < this.board.foundationCount(); ++index) {
         this.drawSlot(this.foundationX(index), TOP_ROW_Y, this.slotColor());
      }
      for (int index = 0; index < this.board.tableauCount(); ++index) {
         this.drawSlot(this.tableauX(index), TABLEAU_TOP_Y, this.slotColor());
      }

      for (UiButton button : this.buttons) {
         button.draw(this.shapes, this.isButtonEnabled(button.label));
      }
      this.shapes.end();
   }

   private void drawCards() {
      float tableauGap = this.tableauGap();
      this.batch.begin();

      for (int index = 0; index < this.board.freeCellCount(); ++index) {
         FreeCellCard card = this.board.getFreeCell(index);
         if (card != null) {
            this.drawCard(card, this.cellX(index), TOP_ROW_Y);
         }
      }

      for (int index = 0; index < this.board.foundationCount(); ++index) {
         List<FreeCellCard> foundation = this.board.getFoundation(index);
         if (!foundation.isEmpty()) {
            this.drawCard(foundation.get(foundation.size() - 1), this.foundationX(index), TOP_ROW_Y);
         }
      }

      for (int column = 0; column < this.board.tableauCount(); ++column) {
         List<FreeCellCard> tableau = this.board.getTableau(column);
         for (int row = 0; row < tableau.size(); ++row) {
            this.drawCard(tableau.get(row), this.tableauX(column), this.tableauCardY(row, tableauGap));
         }
      }

      this.batch.end();
   }

   private void drawHighlights() {
      float tableauGap = this.tableauGap();

      this.shapes.begin(ShapeRenderer.ShapeType.Filled);
      if (this.selection != null) {
         this.shapes.setColor(new Color(1f, 0.88f, 0.25f, 0.30f));
         if (this.selection.slot.area == FreeCellBoard.Area.CELL) {
            this.shapes.rect(this.cellX(this.selection.slot.index), TOP_ROW_Y, CARD_WIDTH, CARD_HEIGHT);
         } else {
            List<FreeCellCard> tableau = this.board.getTableau(this.selection.slot.index);
            for (int row = this.selection.tableauStartIndex; row < tableau.size(); ++row) {
               this.shapes.rect(this.tableauX(this.selection.slot.index), this.tableauCardY(row, tableauGap), CARD_WIDTH, CARD_HEIGHT);
            }
         }
      }
      this.shapes.end();

      this.shapes.begin(ShapeRenderer.ShapeType.Line);
      this.shapes.setColor(new Color(0.98f, 0.90f, 0.34f, 0.95f));
      if (this.selection != null) {
         if (this.selection.slot.area == FreeCellBoard.Area.CELL) {
            this.shapes.rect(this.cellX(this.selection.slot.index), TOP_ROW_Y, CARD_WIDTH, CARD_HEIGHT);
         } else {
            List<FreeCellCard> tableau = this.board.getTableau(this.selection.slot.index);
            for (int row = this.selection.tableauStartIndex; row < tableau.size(); ++row) {
               this.shapes.rect(this.tableauX(this.selection.slot.index), this.tableauCardY(row, tableauGap), CARD_WIDTH, CARD_HEIGHT);
            }
         }
      }
      this.shapes.end();
   }

   private void drawText() {
      this.batch.begin();

      this.titleFont.setColor(new Color(0.97f, 0.95f, 0.88f, 1f));
      this.titleFont.draw(this.batch, "FreeCell Solver", 52f, 850f);

      this.textFont.setColor(new Color(0.96f, 0.92f, 0.86f, 1f));
      this.textFont.draw(this.batch, "Free Cells", LEFT_X, 883f);
      this.textFont.draw(this.batch, "Foundations", this.foundationX(0), 883f);
      this.textFont.draw(this.batch, "Tableau", LEFT_X, 655f);

      for (int index = 0; index < this.board.foundationCount(); ++index) {
         Color suitColor = this.foundationSuitColor(index);
         this.textFont.setColor(suitColor);
         this.drawCentered(this.textFont, this.foundationSuitLabel(index), this.foundationX(index) + CARD_WIDTH / 2f, TOP_ROW_Y + CARD_HEIGHT / 2f + 6f);
      }
      this.textFont.setColor(new Color(0.96f, 0.92f, 0.86f, 1f));

      for (UiButton button : this.buttons) {
         button.drawLabel(this.batch, this.textFont);
      }

      this.textFont.draw(this.batch, "Seed: " + this.currentSeed, CONTROL_X - 4f, 590f);
      this.textFont.draw(this.batch, "Foundations: " + this.board.foundationCardCount() + " / 52", CONTROL_X - 4f, 562f);
      this.textFont.draw(this.batch, "Solver: " + this.solveStateText(), CONTROL_X - 4f, 534f);
      this.textFont.draw(this.batch, "Solution: " + this.solutionProgressText(), CONTROL_X - 4f, 506f);

      this.smallFont.setColor(new Color(0.92f, 0.90f, 0.84f, 1f));
      this.drawWrapped(this.smallFont, this.statusText, CONTROL_X - 4f, 470f, 300f);

      if (this.solutionIndex < this.solutionSteps.size()) {
         this.smallFont.setColor(new Color(0.84f, 0.93f, 0.88f, 1f));
         this.drawWrapped(this.smallFont, "Next: " + this.solutionSteps.get(this.solutionIndex).getDescription(), CONTROL_X - 4f, 364f, 300f);
      }

      this.smallFont.setColor(new Color(0.85f, 0.85f, 0.80f, 1f));
      this.drawWrapped(this.smallFont, "Mouse: select a card, then select a destination pile.", CONTROL_X - 4f, 260f, 300f);
      this.drawWrapped(this.smallFont, "Keys: N new, R reset, S solve, Space step, A auto, Esc clear.", CONTROL_X - 4f, 220f, 300f);

      this.batch.end();
   }

   private void drawCard(FreeCellCard card, float x, float y) {
      TextureRegion region = this.cardRegion(card);
      this.batch.setColor(Color.WHITE);
      this.batch.draw(region, x, y, CARD_WIDTH, CARD_HEIGHT);

      this.smallFont.setColor(card.isRed() ? new Color(0.78f, 0.18f, 0.19f, 1f) : new Color(0.12f, 0.14f, 0.18f, 1f));
      this.smallFont.draw(this.batch, card.getCode(), x + 7f, y + CARD_HEIGHT - 8f);
      this.smallFont.draw(this.batch, card.getCode(), x + 7f, y + 18f);
   }

   private TextureRegion cardRegion(FreeCellCard card) {
      return this.deckRegions[this.suitRow(card.getSuit())][this.rankColumn(card.getRank())];
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

      HitTarget target = this.findHitTarget(this.touchPoint.x, this.touchPoint.y);
      if (this.selection == null) {
         if (target == null || !target.selectableSource) {
            this.statusText = "Select a movable tableau tail or an occupied free cell.";
            return;
         }
         this.selection = new Selection(target.slot, target.tableauStartIndex);
         this.statusText = this.selectionMessage();
         return;
      }

      if (target == null) {
         this.selection = null;
         this.statusText = "Selection cleared.";
         return;
      }
      if (this.sameSelection(target)) {
         this.selection = null;
         this.statusText = "Selection cleared.";
         return;
      }

      FreeCellBoard.MoveResult result = this.board.move(this.selection.slot, this.selection.tableauStartIndex, target.slot);
      if (result.isSuccess()) {
         this.selection = null;
         this.autoplay = false;
         this.autoplayPending = false;
         this.solutionSteps = Collections.emptyList();
         this.solutionIndex = 0;
         this.statusText = this.board.isSolved() ? "Puzzle solved." : result.getMessage();
         return;
      }

      if (target.selectableSource) {
         this.selection = new Selection(target.slot, target.tableauStartIndex);
         this.statusText = this.selectionMessage();
      } else {
         this.statusText = result.getMessage();
      }
   }

   private void handleButton(String label) {
      if ("NEW".equals(label)) {
         this.startNewDeal();
      } else if ("SPIDER".equals(label)) {
         this.game.showSpider();
         return;
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
            this.statusText = "Autoplay started.";
         } else {
            this.requestSolve(true);
         }
      } else if ("STOP".equals(label)) {
         this.autoplay = false;
         this.autoplayPending = false;
         this.statusText = "Autoplay stopped.";
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

   private HitTarget findHitTarget(float x, float y) {
      for (int index = 0; index < this.board.freeCellCount(); ++index) {
         Rectangle bounds = new Rectangle(this.cellX(index), TOP_ROW_Y, CARD_WIDTH, CARD_HEIGHT);
         if (bounds.contains(x, y)) {
            return new HitTarget(new FreeCellBoard.Slot(FreeCellBoard.Area.CELL, index), 0, this.board.getFreeCell(index) != null);
         }
      }

      for (int index = 0; index < this.board.foundationCount(); ++index) {
         Rectangle bounds = new Rectangle(this.foundationX(index), TOP_ROW_Y, CARD_WIDTH, CARD_HEIGHT);
         if (bounds.contains(x, y)) {
            return new HitTarget(new FreeCellBoard.Slot(FreeCellBoard.Area.FOUNDATION, index), -1, false);
         }
      }

      float tableauGap = this.tableauGap();
      for (int column = 0; column < this.board.tableauCount(); ++column) {
         List<FreeCellCard> tableau = this.board.getTableau(column);
         if (tableau.isEmpty()) {
            Rectangle bounds = new Rectangle(this.tableauX(column), TABLEAU_TOP_Y, CARD_WIDTH, CARD_HEIGHT);
            if (bounds.contains(x, y)) {
               return new HitTarget(new FreeCellBoard.Slot(FreeCellBoard.Area.TABLEAU, column), -1, false);
            }
            continue;
         }

         for (int row = tableau.size() - 1; row >= 0; --row) {
            float cardY = this.tableauCardY(row, tableauGap);
            float hitY = row == tableau.size() - 1 ? cardY : cardY + CARD_HEIGHT - tableauGap;
            float hitHeight = row == tableau.size() - 1 ? CARD_HEIGHT : tableauGap;
            Rectangle bounds = new Rectangle(this.tableauX(column), hitY, CARD_WIDTH, hitHeight);
            if (bounds.contains(x, y)) {
               return new HitTarget(
                  new FreeCellBoard.Slot(FreeCellBoard.Area.TABLEAU, column),
                  row,
                  this.board.canSelectTableauTail(column, row)
               );
            }
         }
      }
      return null;
   }

   private boolean sameSelection(HitTarget target) {
      return this.selection != null
         && this.selection.slot.area == target.slot.area
         && this.selection.slot.index == target.slot.index
         && this.selection.tableauStartIndex == target.tableauStartIndex;
   }

   private float tableauGap() {
      int tallestColumn = 1;
      for (int column = 0; column < this.board.tableauCount(); ++column) {
         tallestColumn = Math.max(tallestColumn, this.board.getTableau(column).size());
      }
      if (tallestColumn <= 1) {
         return 36f;
      }
      float gap = (TABLEAU_TOP_Y - 72f) / (tallestColumn - 1);
      return Math.max(20f, Math.min(38f, gap));
   }

   private float tableauCardY(int row, float gap) {
      return TABLEAU_TOP_Y - row * gap;
   }

   private float cellX(int index) {
      return LEFT_X + index * (CARD_WIDTH + COLUMN_GAP);
   }

   private float foundationX(int index) {
      return LEFT_X + 4f * (CARD_WIDTH + COLUMN_GAP) + SLOT_GAP + index * (CARD_WIDTH + COLUMN_GAP);
   }

   private float tableauX(int index) {
      return LEFT_X + index * (CARD_WIDTH + COLUMN_GAP);
   }

   private void drawSlot(float x, float y, Color color) {
      this.shapes.setColor(color);
      this.shapes.rect(x, y, CARD_WIDTH, CARD_HEIGHT);
      this.shapes.setColor(new Color(0.97f, 0.92f, 0.82f, 0.18f));
      this.shapes.rect(x + 5f, y + 5f, CARD_WIDTH - 10f, CARD_HEIGHT - 10f);
   }

   private Color slotColor() {
      return new Color(0.18f, 0.33f, 0.27f, 0.92f);
   }

   private Color foundationSuitColor(int index) {
      int suit = this.foundationSuit(index);
      return suit == 2 || suit == 3 ? new Color(0.89f, 0.40f, 0.42f, 1f) : new Color(0.88f, 0.91f, 0.95f, 1f);
   }

   private String foundationSuitLabel(int index) {
      switch (this.foundationSuit(index)) {
         case 1:
            return "S";
         case 2:
            return "H";
         case 3:
            return "D";
         case 4:
            return "C";
         default:
            return "?";
      }
   }

   private int foundationSuit(int index) {
      switch (index) {
         case 0:
            return 2;
         case 1:
            return 4;
         case 2:
            return 3;
         case 3:
            return 1;
         default:
            return 0;
      }
   }

   private void drawCentered(BitmapFont font, String text, float centerX, float centerY) {
      this.glyphLayout.setText(font, text);
      font.draw(this.batch, text, centerX - this.glyphLayout.width / 2f, centerY + this.glyphLayout.height / 2f);
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
      if (this.selection.slot.area == FreeCellBoard.Area.CELL) {
         return "Selected the card in free cell " + (this.selection.slot.index + 1) + ".";
      }
      int cardCount = this.board.getTableau(this.selection.slot.index).size() - this.selection.tableauStartIndex;
      return "Selected a " + cardCount + "-card tail from tableau " + (this.selection.slot.index + 1) + ".";
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
      return true;
   }

   private void clearSolutionState() {
      this.solutionSteps = Collections.emptyList();
      this.solutionIndex = 0;
      this.autoplay = false;
      this.autoplayPending = false;
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
         FreeCellScreen.this.handlePointer(screenX, screenY);
         return true;
      }

      @Override
      public boolean keyDown(int keycode) {
         if (keycode == Input.Keys.N) {
            FreeCellScreen.this.handleButton("NEW");
            return true;
         }
         if (keycode == Input.Keys.R) {
            FreeCellScreen.this.handleButton("RESET");
            return true;
         }
         if (keycode == Input.Keys.S) {
            FreeCellScreen.this.handleButton("SOLVE");
            return true;
         }
         if (keycode == Input.Keys.SPACE) {
            FreeCellScreen.this.handleButton("STEP");
            return true;
         }
         if (keycode == Input.Keys.A) {
            FreeCellScreen.this.handleButton("AUTO");
            return true;
         }
         if (keycode == Input.Keys.ESCAPE) {
            FreeCellScreen.this.selection = null;
            FreeCellScreen.this.autoplay = false;
            FreeCellScreen.this.autoplayPending = false;
            FreeCellScreen.this.statusText = "Selection cleared.";
            return true;
         }
         return false;
      }
   }

   private static final class Selection {
      final FreeCellBoard.Slot slot;
      final int tableauStartIndex;

      Selection(FreeCellBoard.Slot slot, int tableauStartIndex) {
         this.slot = slot;
         this.tableauStartIndex = tableauStartIndex;
      }
   }

   private static final class HitTarget {
      final FreeCellBoard.Slot slot;
      final int tableauStartIndex;
      final boolean selectableSource;

      HitTarget(FreeCellBoard.Slot slot, int tableauStartIndex, boolean selectableSource) {
         this.slot = slot;
         this.tableauStartIndex = tableauStartIndex;
         this.selectableSource = selectableSource;
      }
   }

   private static final class UiButton {
      final String label;
      final Rectangle bounds;

      UiButton(String label, float x, float y, float width, float height) {
         this.label = label;
         this.bounds = new Rectangle(x, y, width, height);
      }

      void draw(ShapeRenderer shapes, boolean enabled) {
         shapes.setColor(enabled ? new Color(0.85f, 0.72f, 0.44f, 0.90f) : new Color(0.40f, 0.38f, 0.32f, 0.75f));
         shapes.rect(this.bounds.x, this.bounds.y, this.bounds.width, this.bounds.height);
         shapes.setColor(new Color(0.14f, 0.13f, 0.10f, 0.25f));
         shapes.rect(this.bounds.x + 5f, this.bounds.y + 5f, this.bounds.width - 10f, this.bounds.height - 10f);
      }

      void drawLabel(SpriteBatch batch, BitmapFont font) {
         GlyphLayout layout = new GlyphLayout(font, this.label);
         font.setColor(new Color(1, 1f, 1f, 1f));
         font.draw(batch, this.label, this.bounds.x + (this.bounds.width - layout.width) / 2f, this.bounds.y + (this.bounds.height + layout.height) / 2f);
      }
   }
}
