package com.solvitaire.gdx.spider;

import com.solvitaire.app.DealCardCodec;
import com.solvitaire.app.DealVariant;
import com.solvitaire.app.SpiderSolutionStep;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SpiderBoard {
   private static final int COLUMN_COUNT = 10;
   private static final int STOCK_ROW_COUNT = 5;
   private static final int COMPLETED_COUNT = 8;
   private static final int[] DEFAULT_HEIGHTS = new int[]{6, 6, 6, 6, 5, 5, 5, 5, 5, 5};
   private static final int[] DEFAULT_FACEDOWN = new int[]{5, 5, 5, 5, 4, 4, 4, 4, 4, 4};

   private final ArrayList<ArrayList<SpiderCard>> tableaus = new ArrayList<ArrayList<SpiderCard>>(COLUMN_COUNT);
   private final SpiderCard[][] stockRows = new SpiderCard[STOCK_ROW_COUNT][COLUMN_COUNT];
   private final int[] completedSuitValues = new int[COMPLETED_COUNT];

   private long seed;
   private int suitMode = 1;
   private int nextDealRowIndex;

   public SpiderBoard() {
      for (int index = 0; index < COLUMN_COUNT; ++index) {
         this.tableaus.add(new ArrayList<SpiderCard>());
      }
   }

   public void generateDeal(int suitMode, long seed) {
      if (suitMode != 1 && suitMode != 2 && suitMode != 4) {
         throw new IllegalArgumentException("Spider suit mode must be 1, 2 or 4.");
      }

      this.clear();
      this.suitMode = suitMode;
      this.seed = seed;

      String[] lines = DealVariant.SPIDER.generate(suitMode, seed).split("\\R");
      for (int row = 0; row < 6; ++row) {
         String[] tokens = lines[row + 1].split(",", -1);
         for (int column = 0; column < DEFAULT_HEIGHTS.length; ++column) {
            if (row >= DEFAULT_HEIGHTS[column]) {
               continue;
            }

            int cardId = DealCardCodec.parse(tokens[column]);
            this.tableaus.get(column).add(SpiderCard.fromId(cardId, row >= DEFAULT_FACEDOWN[column]));
         }
      }

      String[] stockTokens = lines[7].split(",", -1);
      int tokenIndex = 0;
      for (int row = 0; row < STOCK_ROW_COUNT; ++row) {
         for (int column = 0; column < COLUMN_COUNT; ++column) {
            this.stockRows[row][column] = SpiderCard.fromId(DealCardCodec.parse(stockTokens[tokenIndex++]), false);
         }
      }
      this.nextDealRowIndex = 0;
   }

   public String exportBoardState() {
      StringBuilder builder = new StringBuilder();
      builder.append("Spider,").append(this.stockRemainingCards());
      for (int column = 0; column < COLUMN_COUNT; ++column) {
         builder.append(":").append(this.faceDownCount(column) * 100 + this.tableaus.get(column).size());
      }
      builder.append('\n');

      int maxHeight = this.maxHeight();
      for (int row = 0; row < maxHeight; ++row) {
         for (int column = 0; column < COLUMN_COUNT; ++column) {
            ArrayList<SpiderCard> tableau = this.tableaus.get(column);
            builder.append(row < tableau.size() ? tableau.get(row).getCode() : DealCardCodec.format(0).toUpperCase()).append(',');
         }
         builder.append('\n');
      }

      for (int row = 0; row < STOCK_ROW_COUNT; ++row) {
         for (int column = 0; column < COLUMN_COUNT; ++column) {
            SpiderCard card = row < this.nextDealRowIndex ? null : this.stockRows[row][column];
            builder.append(card == null ? DealCardCodec.format(0).toUpperCase() : card.getCode()).append(',');
         }
      }
      builder.append('\n');

      for (int index = 0; index < COMPLETED_COUNT; ++index) {
         builder.append(this.completedSuitValues[index] <= 0 ? DealCardCodec.format(0).toUpperCase() : DealCardCodec.format(this.completedSuitValues[index]).toUpperCase()).append(',');
      }
      builder.append('\n');
      return builder.toString();
   }

   public List<SpiderCard> getTableau(int index) {
      return Collections.unmodifiableList(this.tableaus.get(index));
   }

   public int tableauCount() {
      return COLUMN_COUNT;
   }

   public int getSuitMode() {
      return this.suitMode;
   }

   public long getSeed() {
      return this.seed;
   }

   public int getNextDealRowIndex() {
      return this.nextDealRowIndex;
   }

   public int stockRemainingCards() {
      return (STOCK_ROW_COUNT - this.nextDealRowIndex) * COLUMN_COUNT;
   }

   public int completedSuitCount() {
      int count = 0;
      while (count < this.completedSuitValues.length && this.completedSuitValues[count] > 0) {
         ++count;
      }
      return count;
   }

   public int getCompletedSuitValue(int index) {
      return this.completedSuitValues[index];
   }

   public boolean canDeal() {
      if (this.nextDealRowIndex >= STOCK_ROW_COUNT) {
         return false;
      }
      for (ArrayList<SpiderCard> tableau : this.tableaus) {
         if (tableau.isEmpty()) {
            return false;
         }
      }
      return true;
   }

   public MoveResult deal() {
      if (!this.canDeal()) {
         if (this.nextDealRowIndex >= STOCK_ROW_COUNT) {
            return MoveResult.failure("No stock rows remain.");
         }
         return MoveResult.failure("All tableau columns must contain at least one card before dealing.");
      }

      for (int column = 0; column < COLUMN_COUNT; ++column) {
         SpiderCard dealt = this.stockRows[this.nextDealRowIndex][column];
         dealt.setFaceUp(true);
         this.tableaus.get(column).add(dealt);
         this.collectCompletedRuns(column);
      }
      ++this.nextDealRowIndex;
      return MoveResult.success("Dealt a new row.");
   }

   public boolean canSelectRun(int column, int startIndex) {
      List<SpiderCard> tableau = this.tableaus.get(column);
      if (startIndex < 0 || startIndex >= tableau.size() || !tableau.get(startIndex).isFaceUp()) {
         return false;
      }
      for (int index = startIndex; index < tableau.size() - 1; ++index) {
         SpiderCard current = tableau.get(index);
         SpiderCard next = tableau.get(index + 1);
         if (!next.isFaceUp() || current.getRank() != next.getRank() + 1) {
            return false;
         }
      }
      return true;
   }

   public MoveResult move(int sourceColumn, int startIndex, int targetColumn) {
      if (sourceColumn == targetColumn) {
         return MoveResult.failure("Choose a different tableau column.");
      }
      if (!this.canSelectRun(sourceColumn, startIndex)) {
         return MoveResult.failure("Select a face-up descending sequence.");
      }

      ArrayList<SpiderCard> source = this.tableaus.get(sourceColumn);
      ArrayList<SpiderCard> target = this.tableaus.get(targetColumn);
      SpiderCard movingBottom = source.get(startIndex);

      if (!target.isEmpty()) {
         SpiderCard targetTop = target.get(target.size() - 1);
         if (!targetTop.isFaceUp() || targetTop.getRank() != movingBottom.getRank() + 1) {
            return MoveResult.failure("The target card must be exactly one rank higher.");
         }
      }

      ArrayList<SpiderCard> movingCards = new ArrayList<SpiderCard>(source.subList(startIndex, source.size()));
      while (source.size() > startIndex) {
         source.remove(source.size() - 1);
      }
      target.addAll(movingCards);
      this.revealTopCard(sourceColumn);
      this.collectCompletedRuns(targetColumn);

      return MoveResult.success(movingCards.size() == 1 ? "Moved 1 card." : "Moved " + movingCards.size() + " cards.");
   }

   public boolean applySolutionStep(SpiderSolutionStep step) {
      if (step.isDealMove()) {
         return this.deal().isSuccess();
      }

      ArrayList<SpiderCard> source = this.tableaus.get(step.getSourceStackIndex());
      int startIndex = source.size() - step.getCardCount();
      if (startIndex < 0) {
         return false;
      }
      return this.move(step.getSourceStackIndex(), startIndex, step.getDestinationStackIndex()).isSuccess();
   }

   public boolean isSolved() {
      return this.completedSuitCount() == COMPLETED_COUNT;
   }

   public static final class MoveResult {
      private final boolean success;
      private final String message;

      private MoveResult(boolean success, String message) {
         this.success = success;
         this.message = message;
      }

      public static MoveResult success(String message) {
         return new MoveResult(true, message);
      }

      public static MoveResult failure(String message) {
         return new MoveResult(false, message);
      }

      public boolean isSuccess() {
         return this.success;
      }

      public String getMessage() {
         return this.message;
      }
   }

   private void clear() {
      for (ArrayList<SpiderCard> tableau : this.tableaus) {
         tableau.clear();
      }
      for (int row = 0; row < STOCK_ROW_COUNT; ++row) {
         for (int column = 0; column < COLUMN_COUNT; ++column) {
            this.stockRows[row][column] = null;
         }
      }
      for (int index = 0; index < this.completedSuitValues.length; ++index) {
         this.completedSuitValues[index] = 0;
      }
      this.nextDealRowIndex = 0;
   }

   private int faceDownCount(int column) {
      int count = 0;
      ArrayList<SpiderCard> tableau = this.tableaus.get(column);
      while (count < tableau.size() && !tableau.get(count).isFaceUp()) {
         ++count;
      }
      return count;
   }

   private int maxHeight() {
      int maxHeight = 0;
      for (ArrayList<SpiderCard> tableau : this.tableaus) {
         if (tableau.size() > maxHeight) {
            maxHeight = tableau.size();
         }
      }
      return maxHeight;
   }

   private void revealTopCard(int column) {
      ArrayList<SpiderCard> tableau = this.tableaus.get(column);
      if (!tableau.isEmpty()) {
         tableau.get(tableau.size() - 1).setFaceUp(true);
      }
   }

   private void collectCompletedRuns(int column) {
      ArrayList<SpiderCard> tableau = this.tableaus.get(column);
      while (tableau.size() >= 13) {
         int start = tableau.size() - 13;
         SpiderCard first = tableau.get(start);
         if (!first.isFaceUp() || first.getRank() != 13) {
            return;
         }

         int suit = first.getSuit();
         boolean complete = true;
         for (int index = start; index < tableau.size(); ++index) {
            SpiderCard card = tableau.get(index);
            if (!card.isFaceUp() || card.getSuit() != suit || card.getRank() != 13 - (index - start)) {
               complete = false;
               break;
            }
         }

         if (!complete) {
            return;
         }

         for (int remove = 0; remove < 13; ++remove) {
            tableau.remove(tableau.size() - 1);
         }
         this.addCompletedSuit(suit);
         this.revealTopCard(column);
      }
   }

   private void addCompletedSuit(int suit) {
      for (int index = 0; index < this.completedSuitValues.length; ++index) {
         if (this.completedSuitValues[index] == 0) {
            this.completedSuitValues[index] = suit * 100 + 13;
            return;
         }
      }
      throw new IllegalStateException("Completed suit area is full.");
   }
}
