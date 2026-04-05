package com.solvitaire.app;

import java.awt.Point;
import java.awt.Rectangle;

public final class Card {
   static int CARD_WIDTH = 80;
   static int CARD_HEIGHT = 110;

   SolverContext context;
   int suit;
   int rank;
   int cardId;
   Point location = new Point();
   Object primaryUiHandle;
   Object secondaryUiHandle;
   boolean faceDown;
   boolean reservedFlag1;
   boolean reservedFlag2;
   CardStack stack;
   CardRun parentRun;
   int runIndex;
   private Rectangle bounds;
   private boolean highlighted;
   private boolean marked;

   Card(SolverContext context) {
      this.context = context;
   }

   final void a(boolean bl) {
      this.highlighted = bl;
   }

   final boolean a() {
      return this.highlighted;
   }

   final void b(boolean bl) {
      this.marked = bl;
   }

   final boolean isMarked() {
      return this.marked;
   }

   final void a(int n2) {
      this.cardId = n2;
      this.rank = n2 % 100;
      this.suit = n2 / 100;
   }

   final void setLocation(int n2, int n3) {
      this.location.setLocation(n2, n3);
      this.bounds = new Rectangle(n2, n3, CARD_WIDTH, CARD_HEIGHT);
   }

   final void c(boolean bl) {
      this.faceDown = bl;
   }

   final void a(int n2, int n3, int n4, int n5, int n6, int n7) {
      this.reservedFlag2 = n7 == 1;
      this.setLocation(n4, n5);
   }

   final void c() {
   }

   final boolean b(int n2, int n3) {
      return this.bounds != null && this.bounds.contains(n2, n3);
   }

   public String toString() {
      return Integer.toString(this.cardId);
   }
}




