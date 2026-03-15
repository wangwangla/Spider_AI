package com.solvitaire.app;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DealFileIO {
   public static SavedDeal read(Path path) throws IOException {
      String content = Files.readString(path, StandardCharsets.US_ASCII).toLowerCase(Locale.ROOT);
      String[] rawLines = content.split("\n");
      List<String> normalizedLines = new ArrayList<>(rawLines.length);

      for (String rawLine : rawLines) {
         String line = rawLine.replace("\r", "").trim();
         if (!line.isEmpty() && !line.startsWith("#")) {
            normalizedLines.add(line);
         }
      }

      if (normalizedLines.isEmpty()) {
         throw new IllegalArgumentException("Deal file is empty: " + path);
      }

      DealVariant variant = DealVariant.fromHeader(normalizedLines.get(0));
      List<Integer> cards = extractCards(normalizedLines);
      int parameter = variant.parseHeaderParameter(normalizedLines.get(0));
      if (variant == DealVariant.SPIDER) {
         parameter = inferSpiderSuitMode(cards);
      }

      return new SavedDeal(path.toAbsolutePath(), variant, parameter, normalizedLines, cards);
   }

   public static Path writeGameStyleDeal(Path outputRoot, DealVariant variant, int parameter, long seed) throws IOException {
      // 生成关
      String content = variant.generate(parameter, seed);
      // 创建文件
      Path variantDirectory = outputRoot.resolve(variant.directoryName(parameter));
      Files.createDirectories(variantDirectory);
      // 写入关的数据
      Path cardsFile = variantDirectory.resolve("cards" + seed + ".txt");
      Files.writeString(cardsFile, content, StandardCharsets.US_ASCII);
      Files.writeString(variantDirectory.resolve("chkpt.txt"), Long.toString(seed), StandardCharsets.US_ASCII);
      return cardsFile;
   }

   private static List<Integer> extractCards(List<String> normalizedLines) {
      List<Integer> cards = new ArrayList<>();
      for (int lineIndex = 1; lineIndex < normalizedLines.size(); ++lineIndex) {
         String[] tokens = normalizedLines.get(lineIndex).split(",");
         for (String token : tokens) {
            String value = token.trim();
            if (value.isEmpty()) {
               continue;
            }
            int cardId = DealCardCodec.parse(value);
            if (cardId > 0) {
               cards.add(cardId);
            }
         }
      }
      return cards;
   }

   private static int inferSpiderSuitMode(List<Integer> cards) {
      if (cards.isEmpty()) {
         return 0;
      }

      Map<Integer, Integer> counts = new HashMap<>();
      for (int card : cards) {
         counts.put(card, counts.getOrDefault(card, 0) + 1);
      }

      boolean oneSuit = counts.size() == 13;
      if (oneSuit) {
         for (int rank = 1; rank <= 13; ++rank) {
            if (counts.getOrDefault(100 + rank, 0) != 8) {
               oneSuit = false;
               break;
            }
         }
         return 1;
      }

      boolean twoSuit = counts.size() == 26;
      if (twoSuit) {
         for (int rank = 1; rank <= 13; ++rank) {
            if (counts.getOrDefault(100 + rank, 0) != 4 || counts.getOrDefault(200 + rank, 0) != 4) {
               twoSuit = false;
               break;
            }
         }
         return 2;
      }

      boolean fourSuit = counts.size() == 52;
      if (fourSuit) {
         for (int suit = 1; suit <= 4; ++suit) {
            for (int rank = 1; rank <= 13; ++rank) {
               if (counts.getOrDefault(suit * 100 + rank, 0) != 2) {
                  fourSuit = false;
                  break;
               }
            }
            if (!fourSuit) {
               break;
            }
         }
         return 4;
      }
      return 0;
   }
}

