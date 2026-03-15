package com.solvitaire.app;

import java.util.Locale;

/**
 * 类型
 */
public enum DealVariant {
   /**
    * 类型
    */
   KLONDIKE("klondike", 52) {
      @Override
      public void validateParameter(int parameter) {
         if (parameter != 1 && parameter != 3) {
            throw new IllegalArgumentException("Klondike parameter must be 1 or 3");
         }
      }

      @Override
      public int parseHeaderParameter(String header) {
         String[] parts = header.split(",", -1);
         if (parts.length < 2) {
            throw new IllegalArgumentException("Klondike header must be 'klondike,1' or 'klondike,3'");
         }
         int parameter = Integer.parseInt(parts[1].trim());
         validateParameter(parameter);
         return parameter;
      }

      @Override
      public String generate(int parameter, long seed) {
         validateParameter(parameter);
         int[] cards = DealShuffler.shuffleSingleDeck(seed);
         int index = 0;
         StringBuilder builder = new StringBuilder();
         builder.append(slug()).append(',').append(parameter).append('\n');
         for (int row = 0; row < 7; ++row) {
            for (int column = 0; column < 7; ++column) {
               if (column < row) {
                  builder.append("   ");
               } else {
                  builder.append(DealCardCodec.format(cards[index++])).append(',');
               }
            }
            builder.append('\n');
         }
         appendCommaSeparated(builder, cards, index, 24);
         return builder.toString();
      }
   },
   SPIDER("spider", 104) {
      /**
       * 校验花色  spider支持1 2 4
       * @param parameter
       */
      @Override
      public void validateParameter(int parameter) {
         if (parameter != 1 && parameter != 2 && parameter != 4) {
            throw new IllegalArgumentException("Spider parameter must be 1, 2 or 4");
         }
      }

      /**
       * 两副牌 52 + 52 = 104
       * @param parameter
       * @param seed
       * @return
       */
      @Override
      public String generate(int parameter, long seed) {
         validateParameter(parameter);
         // 这部分没有啥特殊的 ， 就是生成和随机
         int[] cards = DealShuffler.shuffleSpiderDeck(seed, parameter);
         int index = 0;
         StringBuilder builder = new StringBuilder();
         builder.append(slug()).append('\n');
         //发牌5x10      5x10 + 4 = 54  发出去54张
         for (int row = 0; row < 5; ++row) {
            index = appendCommaSeparated(builder, cards, index, 10);
            builder.append('\n');
         }
         //4 发4
         index = appendCommaSeparated(builder, cards, index, 4);
         builder.append('\n');
         // 50
         appendCommaSeparated(builder, cards, index, 50);
         return builder.toString();
      }
   },
   FREECELL("freecell", 52) {
      @Override
      public String generate(int parameter, long seed) {
         validateParameter(parameter);
         int[] cards = DealShuffler.shuffleSingleDeck(seed);
         int index = 0;
         StringBuilder builder = new StringBuilder();
         builder.append(slug()).append('\n');
         for (int row = 0; row < 6; ++row) {
            index = appendCommaSeparated(builder, cards, index, 8);
            builder.append('\n');
         }
         appendCommaSeparated(builder, cards, index, 4);
         return builder.toString();
      }
   },
   PYRAMID("pyramid", 52) {
      @Override
      public String generate(int parameter, long seed) {
         validateParameter(parameter);
         int[] cards = DealShuffler.shuffleSingleDeck(seed);
         int index = 0;
         StringBuilder builder = new StringBuilder();
         builder.append(slug()).append('\n');
         for (int row = 0; row < 7; ++row) {
            index = appendCommaSeparated(builder, cards, index, row + 1);
            builder.append('\n');
         }
         appendCommaSeparated(builder, cards, index, 24);
         return builder.toString();
      }
   },
   TRIPEAKS("tripeaks", 52) {
      @Override
      public String generate(int parameter, long seed) {
         validateParameter(parameter);
         int[] cards = DealShuffler.shuffleSingleDeck(seed);
         int index = 0;
         StringBuilder builder = new StringBuilder();
         builder.append(slug()).append('\n');
         for (int i = 0; i < 3; ++i) {
            builder.append("         ").append(DealCardCodec.format(cards[index++])).append(',');
         }
         builder.append('\n');
         for (int i = 0; i < 3; ++i) {
            builder.append("     ")
               .append(DealCardCodec.format(cards[index++]))
               .append(',')
               .append(DealCardCodec.format(cards[index++]))
               .append(',');
         }
         builder.append('\n');
         builder.append("   ");
         index = appendCommaSeparated(builder, cards, index, 9);
         builder.append('\n');
         index = appendCommaSeparated(builder, cards, index, 10);
         builder.append('\n');
         appendCommaSeparated(builder, cards, index, 24);
         return builder.toString();
      }
   };

   private final String slug;
   private final int expectedCardCount;

   DealVariant(String slug, int expectedCardCount) {
      this.slug = slug;
      this.expectedCardCount = expectedCardCount;
   }

   public String slug() {
      return this.slug;
   }

   public int expectedCardCount() {
      return this.expectedCardCount;
   }

   public void validateParameter(int parameter) {
      if (parameter != 0) {
         throw new IllegalArgumentException(this.name() + " does not use a numeric parameter");
      }
   }

   public int parseHeaderParameter(String header) {
      return 0;
   }

   public String directoryName(int parameter) {
      validateParameter(parameter);
      return parameter > 0 ? this.slug + parameter : this.slug;
   }

   public abstract String generate(int parameter, long seed);

   public static DealVariant fromHeader(String header) {
      String normalized = header.trim().toLowerCase(Locale.ROOT);
      int commaIndex = normalized.indexOf(',');
      String slug = commaIndex >= 0 ? normalized.substring(0, commaIndex) : normalized;
      return fromSlug(slug);
   }

   public static DealVariant fromSlug(String slug) {
      String normalized = slug.trim().toLowerCase(Locale.ROOT);
      for (DealVariant variant : values()) {
         if (variant.slug.equals(normalized)) {
            return variant;
         }
      }
      throw new IllegalArgumentException("Unsupported variant: " + slug);
   }

   private static int appendCommaSeparated(StringBuilder builder, int[] cards, int start, int count) {
      for (int index = 0; index < count; ++index) {
         builder.append(DealCardCodec.format(cards[start + index])).append(',');
      }
      return start + count;
   }
}

