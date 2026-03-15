package com.solvitaire.app;

import java.util.Locale;

public final class DealCardCodec {
    /**
     * 转换格式   数组中拿到的是100*花色 + 值
     * @param cardId
     * @return
     */
   public static String format(int cardId) {
      if (cardId <= 0) {
         return "??";
      }
      // 取模  得到的是最后的一位值    花色值
      return rankText(cardId % 100) + suitText(cardId / 100);
   }

   public static int parse(String token) {
      String value = token.trim().toLowerCase(Locale.ROOT);
      if (value.isEmpty() || "?".equals(value) || "??".equals(value)) {
         return 0;
      }

      if (value.length() != 2 && value.length() != 3) {
         throw new IllegalArgumentException("Invalid card token: " + token);
      }

      String rankToken = value.substring(0, value.length() - 1);
      char suitToken = value.charAt(value.length() - 1);
      return suitBase(suitToken) + rankValue(rankToken);
   }

   private static String rankText(int rank) {
      switch (rank) {
         case 1:
            return "A";
         case 11:
            return "J";
         case 12:
            return "Q";
         case 13:
            return "K";
         default:
            return Integer.toString(rank);
      }
   }

   private static String suitText(int suit) {
      switch (suit) {
         case 1:
            return "s";   //黑
         case 2:
            return "h";  //红
         case 3:
            return "d";  // 方
         case 4:
            return "c"; //梅花
         default:
            throw new IllegalArgumentException("Invalid suit id: " + suit);
      }
   }

   private static int rankValue(String token) {
      switch (token) {
         case "a":
            return 1;
         case "j":
            return 11;
         case "q":
            return 12;
         case "k":
            return 13;
         case "10":
            return 10;
         default:
            if (token.length() == 1 && token.charAt(0) >= '2' && token.charAt(0) <= '9') {
               return token.charAt(0) - '0';
            }
            throw new IllegalArgumentException("Invalid rank token: " + token);
      }
   }

   private static int suitBase(char suit) {
      switch (suit) {
         case 's':
            return 100;
         case 'h':
            return 200;
         case 'd':
            return 300;
         case 'c':
            return 400;
         case '?':
            return 0;
         default:
            throw new IllegalArgumentException("Invalid suit token: " + suit);
      }
   }
}

