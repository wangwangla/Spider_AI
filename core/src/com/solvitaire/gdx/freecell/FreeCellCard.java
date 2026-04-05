package com.solvitaire.gdx.freecell;

import com.solvitaire.app.DealCardCodec;

public final class FreeCellCard {
   private final int cardId;
   private final int suit;
   private final int rank;

   private FreeCellCard(int cardId) {
      this.cardId = cardId;
      this.suit = cardId / 100;
      this.rank = cardId % 100;
   }

   public static FreeCellCard fromId(int cardId) {
      if (cardId <= 0) {
         throw new IllegalArgumentException("Card id must be positive.");
      }
      return new FreeCellCard(cardId);
   }

   public int getCardId() {
      return this.cardId;
   }

   public int getSuit() {
      return this.suit;
   }

   public int getRank() {
      return this.rank;
   }

   public boolean isRed() {
      return this.suit == 2 || this.suit == 3;
   }

   public String getCode() {
      return DealCardCodec.format(this.cardId).toUpperCase();
   }
}
