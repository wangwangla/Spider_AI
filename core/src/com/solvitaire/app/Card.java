package com.solvitaire.app;

public final class Card {
   SolverContext context;
   int suit;
   int rank;
   int cardId;
   Object primaryUiHandle;
   Object secondaryUiHandle;
   boolean faceDown;
   CardStack stack;
   int runIndex;

   Card(SolverContext context) {
      this.context = context;
   }

   final void dictCardValue(int n2) {
      this.cardId = n2;
      this.rank = n2 % 100;
      this.suit = n2 / 100;
   }

   public String toString() {
      return Integer.toString(this.cardId);
   }
}




