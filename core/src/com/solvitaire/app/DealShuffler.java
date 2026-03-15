package com.solvitaire.app;

public final class DealShuffler {
   private static final long MULTIPLIER = 214013L;
   private static final long INCREMENT = 2531011L;
   private static final long MASK = Integer.MAX_VALUE;

   public static int[] shuffleSingleDeck(long seed) {
      int[] ordered = new int[52];
      for (int index = 0; index < ordered.length; ++index) {
         int rank = index / 4 + 1;
         int suit = 4 - index % 4;
         ordered[index] = suit * 100 + rank;
      }
      return shuffle(seed, ordered);
   }

   public static int[] shuffleSpiderDeck(long seed, int suitMode) {
      if (suitMode != 1 && suitMode != 2 && suitMode != 4) {
         throw new IllegalArgumentException("Spider suit mode must be 1, 2 or 4");
      }
      /**
       * spider 104 张
       */
      int[] ordered = new int[104];
      for (int index = 0; index < ordered.length; ++index) {
         int rank = index / 8 + 1;
         int suit;
         if (suitMode == 1) {
            suit = 1;
         } else if (suitMode == 2) {
            suit = 2 - index % 2;
         } else {
            suit = 4 - index % 4;
         }
         ordered[index] = suit * 100 + rank;
      }
      return shuffle(seed, ordered);
   }

   /**
    * 打乱
    * @param seed
    * @param ordered
    * @return
    */
   private static int[] shuffle(long seed, int[] ordered) {
      int[] shuffled = new int[ordered.length];
      int remaining = ordered.length;
      int writeIndex = 0;

      while (remaining > 0) {
         seed = MULTIPLIER * seed + INCREMENT & MASK;
         int randomValue = (int)(seed >> 16);
         int pickIndex = randomValue % remaining;
         shuffled[writeIndex++] = ordered[pickIndex];
         ordered[pickIndex] = ordered[remaining - 1];
         --remaining;
      }

      return shuffled;
   }
}

