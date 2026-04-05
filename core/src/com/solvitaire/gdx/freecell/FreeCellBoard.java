package com.solvitaire.gdx.freecell;

import com.solvitaire.app.DealCardCodec;
import com.solvitaire.app.DealVariant;
import com.solvitaire.app.FreeCellSolutionStep;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FreeCellBoard {
   private static final int TABLEAU_COUNT = 8;
   private static final int CELL_COUNT = 4;
   private static final int FOUNDATION_COUNT = 4;
   private static final int[] DEFAULT_HEIGHTS = new int[]{7, 7, 7, 7, 6, 6, 6, 6};
   private static final int[] FOUNDATION_SUIT_BY_INDEX = new int[]{2, 4, 3, 1};

   private final ArrayList<ArrayList<FreeCellCard>> tableaus = new ArrayList<ArrayList<FreeCellCard>>(TABLEAU_COUNT);
   private final FreeCellCard[] freeCells = new FreeCellCard[CELL_COUNT];
   private final ArrayList<ArrayList<FreeCellCard>> foundations = new ArrayList<ArrayList<FreeCellCard>>(FOUNDATION_COUNT);

   private long seed;

   public FreeCellBoard() {
      for (int index = 0; index < TABLEAU_COUNT; ++index) {
         this.tableaus.add(new ArrayList<FreeCellCard>());
      }
      for (int index = 0; index < FOUNDATION_COUNT; ++index) {
         this.foundations.add(new ArrayList<FreeCellCard>());
      }
   }

   public void generateDeal(long seed) {
      this.seed = seed;
      this.loadBoardState(DealVariant.FREECELL.generate(0, seed), seed);
   }

   public void loadBoardState(String boardState, long seed) {
      this.seed = seed;
      this.clear();

      String[] lines = boardState.split("\\R");
      if (lines.length == 0) {
         throw new IllegalArgumentException("Board state is empty.");
      }

      String header = lines[0].trim();
      if (header.isEmpty()) {
         throw new IllegalArgumentException("Board state header is empty.");
      }

      int commaIndex = header.indexOf(',');
      int[] heights = commaIndex >= 0 ? this.parseHeights(header.substring(commaIndex + 1).trim()) : DEFAULT_HEIGHTS.clone();

      ArrayList<String> dataLines = new ArrayList<String>();
      for (int index = 1; index < lines.length; ++index) {
         String trimmed = lines[index].trim();
         if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
            dataLines.add(lines[index]);
         }
      }

      int maxHeight = this.maxHeight(heights);
      if (dataLines.size() < maxHeight) {
         throw new IllegalArgumentException("Board state is missing tableau rows.");
      }

      for (int row = 0; row < maxHeight; ++row) {
         String[] tokens = dataLines.get(row).split(",", -1);
         for (int column = 0; column < TABLEAU_COUNT; ++column) {
            if (row >= heights[column]) {
               continue;
            }

            String token = column < tokens.length ? tokens[column].trim() : "??";
            int cardId = DealCardCodec.parse(token);
            if (cardId <= 0) {
               throw new IllegalArgumentException("Expected a card at tableau " + column + " row " + row + ".");
            }
            this.tableaus.get(column).add(FreeCellCard.fromId(cardId));
         }
      }

      int dataIndex = maxHeight;
      if (dataIndex < dataLines.size()) {
         this.parseFreeCells(dataLines.get(dataIndex++));
      }
      if (dataIndex < dataLines.size()) {
         this.parseFoundations(dataLines.get(dataIndex));
      }
   }

   public String exportBoardState() {
      StringBuilder builder = new StringBuilder();
      int[] heights = this.currentHeights();
      boolean partialState = this.isPartialState(heights);

      builder.append("FreeCell");
      if (partialState) {
         builder.append(",");
         for (int index = 0; index < heights.length; ++index) {
            if (index > 0) {
               builder.append(":");
            }
            builder.append(heights[index]);
         }
      }
      builder.append('\n');

      int maxHeight = this.maxHeight(heights);
      int lineCount = 1;
      for (int row = 0; row < maxHeight; ++row) {
         int lastColumn = -1;
         for (int column = 0; column < TABLEAU_COUNT; ++column) {
            if (row < heights[column]) {
               lastColumn = column;
            }
         }

         if (lastColumn < 0) {
            builder.append('\n');
            ++lineCount;
            continue;
         }

         for (int column = 0; column <= lastColumn; ++column) {
            String token = row < heights[column] ? this.tableaus.get(column).get(row).getCode() : DealCardCodec.format(0).toUpperCase();
            builder.append(token).append(',');
         }
         builder.append('\n');
         ++lineCount;
      }

      if (partialState) {
         for (int index = 0; index < CELL_COUNT; ++index) {
            builder.append(this.freeCells[index] == null ? DealCardCodec.format(0).toUpperCase() : this.freeCells[index].getCode()).append(',');
         }
         builder.append('\n');
         ++lineCount;
         for (int index = 0; index < FOUNDATION_COUNT; ++index) {
            builder.append(this.topFoundationCode(index)).append(',');
         }
         builder.append('\n');
         ++lineCount;
         while (lineCount < 6) {
            builder.append('\n');
            ++lineCount;
         }
      }

      return builder.toString();
   }

   public List<FreeCellCard> getTableau(int index) {
      return Collections.unmodifiableList(this.tableaus.get(index));
   }

   public FreeCellCard getFreeCell(int index) {
      return this.freeCells[index];
   }

   public List<FreeCellCard> getFoundation(int index) {
      return Collections.unmodifiableList(this.foundations.get(index));
   }

   public long getSeed() {
      return this.seed;
   }

   public boolean canSelectTableauTail(int tableauIndex, int startIndex) {
      List<FreeCellCard> tableau = this.tableaus.get(tableauIndex);
      return startIndex >= 0 && startIndex < tableau.size() && this.isValidRun(tableau, startIndex);
   }

   public MoveResult move(Slot from, int tableauStartIndex, Slot to) {
      if (from == null || to == null) {
         return MoveResult.failure("Select a source card and a target pile.");
      }
      if (from.area == Area.FOUNDATION) {
         return MoveResult.failure("Cards cannot be moved out of foundations.");
      }
      if (from.area == to.area && from.index == to.index) {
         return MoveResult.failure("Choose a different target pile.");
      }

      ArrayList<FreeCellCard> movingCards = new ArrayList<FreeCellCard>();
      if (from.area == Area.CELL) {
         FreeCellCard card = this.freeCells[from.index];
         if (card == null) {
            return MoveResult.failure("The selected free cell is empty.");
         }
         movingCards.add(card);
      } else {
         ArrayList<FreeCellCard> tableau = this.tableaus.get(from.index);
         if (tableau.isEmpty()) {
            return MoveResult.failure("The selected tableau is empty.");
         }
         if (tableauStartIndex < 0 || tableauStartIndex >= tableau.size()) {
            return MoveResult.failure("Invalid tableau selection.");
         }
         if (!this.isValidRun(tableau, tableauStartIndex)) {
            return MoveResult.failure("Only ordered alternating tails can be moved.");
         }
         movingCards.addAll(tableau.subList(tableauStartIndex, tableau.size()));
      }

      int count = movingCards.size();
      if (to.area == Area.CELL) {
         if (count != 1) {
            return MoveResult.failure("Only one card can be moved into a free cell.");
         }
         if (this.freeCells[to.index] != null) {
            return MoveResult.failure("That free cell is occupied.");
         }
      } else if (to.area == Area.FOUNDATION) {
         if (count != 1) {
            return MoveResult.failure("Only one card can be moved to a foundation.");
         }
         if (!this.canMoveToFoundation(movingCards.get(0), to.index)) {
            return MoveResult.failure("That card does not fit on the foundation.");
         }
      } else {
         if (!this.canMoveToTableau(movingCards, to.index)) {
            return MoveResult.failure("That sequence does not fit on the tableau.");
         }
         if (count > 1 && count > this.maxMovableCards(to.index)) {
            return MoveResult.failure("Not enough empty cells and tableau columns for that move.");
         }
      }

      this.removeFromSource(from, tableauStartIndex);
      this.addToTarget(movingCards, to);
      return MoveResult.success(count == 1 ? "Moved 1 card." : "Moved " + count + " cards.");
   }

   public boolean applySolutionStep(FreeCellSolutionStep step) {
      Slot source = this.fromSolverGroup(step.getSourceGroupIndex(), step.getSourceStackIndex());
      Slot destination = this.fromSolverGroup(step.getDestinationGroupIndex(), step.getDestinationStackIndex());
      if (source == null || destination == null) {
         return false;
      }

      int tableauStartIndex = 0;
      if (source.area == Area.TABLEAU) {
         ArrayList<FreeCellCard> tableau = this.tableaus.get(source.index);
         if (tableau.size() < step.getCardCount()) {
            return false;
         }
         tableauStartIndex = tableau.size() - step.getCardCount();
      }

      return this.move(source, tableauStartIndex, destination).isSuccess();
   }

   public boolean isSolved() {
      return this.foundationCardCount() == 52;
   }

   public int foundationCardCount() {
      int total = 0;
      for (List<FreeCellCard> foundation : this.foundations) {
         total += foundation.size();
      }
      return total;
   }

   public int tableauCount() {
      return TABLEAU_COUNT;
   }

   public int freeCellCount() {
      return CELL_COUNT;
   }

   public int foundationCount() {
      return FOUNDATION_COUNT;
   }

   public static final class Slot {
      public final Area area;
      public final int index;

      public Slot(Area area, int index) {
         this.area = area;
         this.index = index;
      }
   }

   public enum Area {
      TABLEAU,
      CELL,
      FOUNDATION
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
      for (ArrayList<FreeCellCard> tableau : this.tableaus) {
         tableau.clear();
      }
      for (int index = 0; index < this.freeCells.length; ++index) {
         this.freeCells[index] = null;
      }
      for (ArrayList<FreeCellCard> foundation : this.foundations) {
         foundation.clear();
      }
   }

   private int[] parseHeights(String value) {
      if (value.isEmpty()) {
         return DEFAULT_HEIGHTS.clone();
      }

      String[] parts = value.split(":");
      if (parts.length != TABLEAU_COUNT) {
         throw new IllegalArgumentException("FreeCell header must contain 8 tableau heights.");
      }

      int[] heights = new int[TABLEAU_COUNT];
      for (int index = 0; index < parts.length; ++index) {
         heights[index] = Integer.parseInt(parts[index].trim());
      }
      return heights;
   }

   private void parseFreeCells(String line) {
      String[] tokens = line.split(",", -1);
      for (int index = 0; index < CELL_COUNT; ++index) {
         String token = index < tokens.length ? tokens[index].trim() : "??";
         int cardId = DealCardCodec.parse(token);
         this.freeCells[index] = cardId <= 0 ? null : FreeCellCard.fromId(cardId);
      }
   }

   private void parseFoundations(String line) {
      String[] tokens = line.split(",", -1);
      for (int index = 0; index < FOUNDATION_COUNT; ++index) {
         String token = index < tokens.length ? tokens[index].trim() : "??";
         int cardId = DealCardCodec.parse(token);
         if (cardId <= 0) {
            continue;
         }

         int suit = FOUNDATION_SUIT_BY_INDEX[index];
         int rank = cardId % 100;
         for (int value = 1; value <= rank; ++value) {
            this.foundations.get(index).add(FreeCellCard.fromId(suit * 100 + value));
         }
      }
   }

   private int[] currentHeights() {
      int[] heights = new int[TABLEAU_COUNT];
      for (int index = 0; index < TABLEAU_COUNT; ++index) {
         heights[index] = this.tableaus.get(index).size();
      }
      return heights;
   }

   private boolean isPartialState(int[] heights) {
      for (int index = 0; index < heights.length; ++index) {
         if (heights[index] != DEFAULT_HEIGHTS[index]) {
            return true;
         }
      }
      for (FreeCellCard freeCell : this.freeCells) {
         if (freeCell != null) {
            return true;
         }
      }
      for (List<FreeCellCard> foundation : this.foundations) {
         if (!foundation.isEmpty()) {
            return true;
         }
      }
      return false;
   }

   private int maxHeight(int[] heights) {
      int maxHeight = 0;
      for (int height : heights) {
         if (height > maxHeight) {
            maxHeight = height;
         }
      }
      return maxHeight;
   }

   private String topFoundationCode(int index) {
      List<FreeCellCard> foundation = this.foundations.get(index);
      return foundation.isEmpty() ? DealCardCodec.format(0).toUpperCase() : foundation.get(foundation.size() - 1).getCode();
   }

   private boolean isValidRun(List<FreeCellCard> tableau, int startIndex) {
      for (int index = startIndex; index < tableau.size() - 1; ++index) {
         FreeCellCard current = tableau.get(index);
         FreeCellCard next = tableau.get(index + 1);
         if (current.getRank() != next.getRank() + 1 || current.isRed() == next.isRed()) {
            return false;
         }
      }
      return true;
   }

   private boolean canMoveToFoundation(FreeCellCard card, int foundationIndex) {
      if (foundationIndex < 0 || foundationIndex >= FOUNDATION_COUNT) {
         return false;
      }
      if (card.getSuit() != FOUNDATION_SUIT_BY_INDEX[foundationIndex]) {
         return false;
      }

      List<FreeCellCard> foundation = this.foundations.get(foundationIndex);
      return foundation.isEmpty() ? card.getRank() == 1 : foundation.get(foundation.size() - 1).getRank() + 1 == card.getRank();
   }

   private boolean canMoveToTableau(List<FreeCellCard> movingCards, int tableauIndex) {
      if (tableauIndex < 0 || tableauIndex >= TABLEAU_COUNT) {
         return false;
      }

      ArrayList<FreeCellCard> tableau = this.tableaus.get(tableauIndex);
      if (tableau.isEmpty()) {
         return true;
      }

      FreeCellCard destinationCard = tableau.get(tableau.size() - 1);
      FreeCellCard movingBottom = movingCards.get(0);
      return destinationCard.getRank() == movingBottom.getRank() + 1 && destinationCard.isRed() != movingBottom.isRed();
   }

   private int maxMovableCards(int destinationTableauIndex) {
      int emptyCells = 0;
      for (FreeCellCard freeCell : this.freeCells) {
         if (freeCell == null) {
            ++emptyCells;
         }
      }

      int emptyTableaus = 0;
      for (ArrayList<FreeCellCard> tableau : this.tableaus) {
         if (tableau.isEmpty()) {
            ++emptyTableaus;
         }
      }

      if (this.tableaus.get(destinationTableauIndex).isEmpty()) {
         --emptyTableaus;
      }
      if (emptyTableaus < 0) {
         emptyTableaus = 0;
      }

      return (emptyCells + 1) * (1 << emptyTableaus);
   }

   private void removeFromSource(Slot from, int tableauStartIndex) {
      if (from.area == Area.CELL) {
         this.freeCells[from.index] = null;
         return;
      }

      ArrayList<FreeCellCard> tableau = this.tableaus.get(from.index);
      while (tableau.size() > tableauStartIndex) {
         tableau.remove(tableau.size() - 1);
      }
   }

   private void addToTarget(List<FreeCellCard> movingCards, Slot to) {
      if (to.area == Area.CELL) {
         this.freeCells[to.index] = movingCards.get(0);
      } else if (to.area == Area.FOUNDATION) {
         this.foundations.get(to.index).add(movingCards.get(0));
      } else {
         this.tableaus.get(to.index).addAll(movingCards);
      }
   }

   private Slot fromSolverGroup(int groupIndex, int stackIndex) {
      if (groupIndex == 0) {
         return new Slot(Area.TABLEAU, stackIndex);
      }
      if (groupIndex == 1) {
         return new Slot(Area.CELL, stackIndex);
      }
      if (groupIndex == 2) {
         return new Slot(Area.FOUNDATION, stackIndex);
      }
      return null;
   }
}
