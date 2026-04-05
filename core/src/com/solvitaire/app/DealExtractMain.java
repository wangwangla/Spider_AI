package com.solvitaire.app;

import java.io.IOException;
import java.nio.file.Path;

public final class DealExtractMain {
   private DealExtractMain() {
   }

   public static void main(String[] args) throws IOException {
      if (args.length == 0) {
         printUsage();
         System.exit(1);
      }

      String command = args[0].trim().toLowerCase();
      switch (command) {
         case "generate":
            generate(args);
            return;
         case "inspect":
            inspect(args);
            return;
         default:
            printUsage();
            throw new IllegalArgumentException("Unknown command: " + args[0]);
      }
   }

   private static void generate(String[] args) throws IOException {
      if (args.length != 5) {
         throw new IllegalArgumentException("Usage: generate <variant> <parameter> <seed> <output-root>");
      }

      DealVariant variant = DealVariant.fromSlug(args[1]);
      int parameter = Integer.parseInt(args[2]);
      long seed = Long.parseLong(args[3]);
      Path outputRoot = Path.of(args[4]).toAbsolutePath();

      Path savedPath = DealFileIO.writeGameStyleDeal(outputRoot, variant, parameter, seed);
      System.out.println("Saved " + variant.slug() + " seed " + seed + " to " + savedPath);
   }

   private static void inspect(String[] args) throws IOException {
      if (args.length != 2) {
         throw new IllegalArgumentException("Usage: inspect <cards-file>");
      }

      SavedDeal deal = DealFileIO.read(Path.of(args[1]).toAbsolutePath());
      System.out.println("file=" + deal.sourcePath());
      System.out.println("variant=" + deal.variant().slug());
      if (deal.parameter() > 0) {
         System.out.println("parameter=" + deal.parameter());
      }
      System.out.println("cards=" + deal.cardCount());
      System.out.println("expectedCards=" + deal.variant().expectedCardCount());
      System.out.println("countMatches=" + deal.hasExpectedCardCount());
      System.out.println("header=" + deal.normalizedLines().get(0));
   }

   private static void printUsage() {
      System.err.println("Usage:");
      System.err.println("  java com.solvitaire.app.DealExtractMain generate <variant> <parameter> <seed> <output-root>");
      System.err.println("  java com.solvitaire.app.DealExtractMain inspect <cards-file>");
      System.err.println("Examples:");
      System.err.println("  java com.solvitaire.app.DealExtractMain generate klondike 3 12345 .");
      System.err.println("  java com.solvitaire.app.DealExtractMain generate spider 4 321 .");
      System.err.println("  java com.solvitaire.app.DealExtractMain inspect .\\klondike3\\cards12345.txt");
   }
}

