package com.solvitaire.app;

public final class SpiderSolutionStep {
   private final int rawMove;
   private final int sourceStackIndex;
   private final int destinationStackIndex;
   private final int cardCount;
   private final boolean dealMove;
   private final String description;

   SpiderSolutionStep(int rawMove, int sourceStackIndex, int destinationStackIndex, int cardCount, boolean dealMove,
         String description) {
      this.rawMove = rawMove;
      this.sourceStackIndex = sourceStackIndex;
      this.destinationStackIndex = destinationStackIndex;
      this.cardCount = cardCount;
      this.dealMove = dealMove;
      this.description = description;
   }

   public int getRawMove() {
      return this.rawMove;
   }

   public int getSourceStackIndex() {
      return this.sourceStackIndex;
   }

   public int getDestinationStackIndex() {
      return this.destinationStackIndex;
   }

   public int getCardCount() {
      return this.cardCount;
   }

   public boolean isDealMove() {
      return this.dealMove;
   }

   public String getDescription() {
      return this.description;
   }
}
