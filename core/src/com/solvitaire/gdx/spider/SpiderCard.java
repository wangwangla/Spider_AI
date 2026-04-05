package com.solvitaire.gdx.spider;

import com.solvitaire.app.DealCardCodec;

public final class SpiderCard {
   private final int cardId;
   private final int suit;
   private final int rank;
   private boolean faceUp;

   private SpiderCard(int cardId, boolean faceUp) {
      this.cardId = cardId;
      this.suit = cardId / 100;
      this.rank = cardId % 100;
      this.faceUp = faceUp;
   }

   public static SpiderCard fromId(int cardId, boolean faceUp) {
      if (cardId <= 0) {
         throw new IllegalArgumentException("Card id must be positive.");
      }
      return new SpiderCard(cardId, faceUp);
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

   public boolean isFaceUp() {
      return this.faceUp;
   }

   public void setFaceUp(boolean faceUp) {
      this.faceUp = faceUp;
   }

   public String getCode() {
      return DealCardCodec.format(this.cardId).toUpperCase();
   }
}
