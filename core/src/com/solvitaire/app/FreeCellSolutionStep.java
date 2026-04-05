package com.solvitaire.app;

public final class FreeCellSolutionStep {
   private final int rawMove;
   private final int sourceGroupIndex;
   private final int sourceStackIndex;
   private final int destinationGroupIndex;
   private final int destinationStackIndex;
   private final int cardCount;
   private final boolean autoMove;
   private final boolean splitMove;
   private final String description;

   FreeCellSolutionStep(int rawMove, int sourceGroupIndex, int sourceStackIndex, int destinationGroupIndex,
         int destinationStackIndex, int cardCount, boolean autoMove, boolean splitMove, String description) {
      this.rawMove = rawMove;
      this.sourceGroupIndex = sourceGroupIndex;
      this.sourceStackIndex = sourceStackIndex;
      this.destinationGroupIndex = destinationGroupIndex;
      this.destinationStackIndex = destinationStackIndex;
      this.cardCount = cardCount;
      this.autoMove = autoMove;
      this.splitMove = splitMove;
      this.description = description;
   }

   public int getRawMove() {
      return this.rawMove;
   }

   public int getSourceGroupIndex() {
      return this.sourceGroupIndex;
   }

   public int getSourceStackIndex() {
      return this.sourceStackIndex;
   }

   public int getDestinationGroupIndex() {
      return this.destinationGroupIndex;
   }

   public int getDestinationStackIndex() {
      return this.destinationStackIndex;
   }

   public int getCardCount() {
      return this.cardCount;
   }

   public boolean isAutoMove() {
      return this.autoMove;
   }

   public boolean isSplitMove() {
      return this.splitMove;
   }

   public String getDescription() {
      return this.description;
   }
}
