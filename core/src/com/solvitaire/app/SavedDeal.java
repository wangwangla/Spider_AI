package com.solvitaire.app;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SavedDeal {
   private final Path sourcePath;
   private final DealVariant variant;
   private final int parameter;
   private final List<String> normalizedLines;
   private final List<Integer> cards;

   SavedDeal(Path sourcePath, DealVariant variant, int parameter, List<String> normalizedLines, List<Integer> cards) {
      this.sourcePath = sourcePath;
      this.variant = variant;
      this.parameter = parameter;
      this.normalizedLines = Collections.unmodifiableList(new ArrayList<>(normalizedLines));
      this.cards = Collections.unmodifiableList(new ArrayList<>(cards));
   }

   public Path sourcePath() {
      return this.sourcePath;
   }

   public DealVariant variant() {
      return this.variant;
   }

   public int parameter() {
      return this.parameter;
   }

   public List<String> normalizedLines() {
      return this.normalizedLines;
   }

   public List<Integer> cards() {
      return this.cards;
   }

   public int cardCount() {
      return this.cards.size();
   }

   public boolean hasExpectedCardCount() {
      return this.cardCount() == this.variant.expectedCardCount();
   }
}

