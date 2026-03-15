package com.solvitaire.app;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SolverMain {
   private SolverMain() {
   }

   public static void main(String[] args) {

      if (args.length != 1) {
         System.err.println("Usage: java com.solvitaire.app.SolverMain <cards-file>");
         System.exit(1);
      }

      Path inputFile = Paths.get(args[0]).toAbsolutePath();
      SanitizedInput sanitizedInput = sanitizeInput(inputFile);
      Path actualInputFile = sanitizedInput.inputFile;
      Path originalSolutionFile = solutionFileFor(inputFile);
      Path actualSolutionFile = solutionFileFor(actualInputFile);

      try {
         SolverContext context = new SolverContext();
         context.logLevel = 99;
         context.variantId = sanitizedInput.variant;
         context.files = new SolverFileSet(actualInputFile);
         context.files.b = 1;
         context.files.variantSlug = variantSlug(sanitizedInput.variant);
         context.initialState = allocateState(context, sanitizedInput.variant);
         context.bestSolutionState = new GameState();
         context.playbackState = new GameState();

         BaseSolver solver = createSolver(context, sanitizedInput.variant);
         solver.C = false;
         context.bridge = new SolverBridge(solver) {
         };
         configureReader(context.bridge, sanitizedInput.variant);

         deleteIfExists(actualSolutionFile);
         if (!actualSolutionFile.equals(originalSolutionFile)) {
            deleteIfExists(originalSolutionFile);
         }

         solver.solve();

         if (!actualSolutionFile.equals(originalSolutionFile) && Files.exists(actualSolutionFile)) {
            Files.move(actualSolutionFile, originalSolutionFile, StandardCopyOption.REPLACE_EXISTING);
         }

         Path solutionFile = Files.exists(originalSolutionFile) ? originalSolutionFile : actualSolutionFile;
         int[] moves = readMoves(solutionFile);
         if (moves.length == 0) {
            System.out.println("No solution found.");
            return;
         }

         System.out.println("Solved " + SolverContext.VARIANT_NAMES[sanitizedInput.variant] + " in " + moves.length + " moves");
         for (String step : Move.a(context.bridge, moves, 0, moves.length, false)) {
            System.out.println(step);
         }
      } catch (IOException iOException) {
         throw new IllegalStateException("Failed to prepare solver files for " + inputFile, iOException);
      } finally {
         cleanupTemporaryFiles(sanitizedInput, actualSolutionFile);
      }
   }

   private static SanitizedInput sanitizeInput(Path inputFile) {
      try {
         List<String> lines = Files.readAllLines(inputFile, StandardCharsets.UTF_8);
         List<String> cleanedLines = new ArrayList<>(lines.size());
         boolean changed = false;

         for (String line : lines) {
            String trimmed = normalizeLine(line).trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
               changed = true;
               continue;
            }
            cleanedLines.add(line);
         }

         if (cleanedLines.isEmpty()) {
            throw new IllegalArgumentException("Input file is empty after removing comments: " + inputFile);
         }

         int variant = detectVariant(cleanedLines.get(0));
         if (!changed) {
            return new SanitizedInput(inputFile, null, variant);
         }

         Path tempDir = Files.createTempDirectory("solvitaire-solver-");
         Path tempInputFile = tempDir.resolve(inputFile.getFileName().toString());
         Files.write(tempInputFile, cleanedLines, StandardCharsets.UTF_8);
         return new SanitizedInput(tempInputFile, tempDir, variant);
      } catch (IOException iOException) {
         throw new IllegalStateException("Failed to read " + inputFile, iOException);
      }
   }

   private static void cleanupTemporaryFiles(SanitizedInput sanitizedInput, Path actualSolutionFile) {
      if (sanitizedInput.cleanupDir == null) {
         return;
      }

      try {
         deleteIfExists(actualSolutionFile);
         deleteIfExists(sanitizedInput.inputFile);
         deleteIfExists(sanitizedInput.cleanupDir);
      } catch (IOException ignored) {
      }
   }

   private static int detectVariant(String line) {
      String normalized = normalizeLine(line).trim().toLowerCase();
      int commaIndex = normalized.indexOf(',');
      String variant = commaIndex >= 0 ? normalized.substring(0, commaIndex) : normalized;
      switch (variant) {
         case "klondike":
            return 1;
         case "spider":
            return 2;
         case "freecell":
            return 3;
         default:
            throw new IllegalArgumentException("Unsupported solver variant in input file: " + line);
      }
   }

   private static String normalizeLine(String line) {
      if (line != null && !line.isEmpty() && line.charAt(0) == '\ufeff') {
         return line.substring(1);
      }
      return line;
   }

   private static String variantSlug(int variant) {
      switch (variant) {
         case 1:
            return "klondike";
         case 2:
            return "spider";
         case 3:
            return "freecell";
         default:
            throw new IllegalArgumentException("Unsupported variant: " + variant);
      }
   }

   private static BaseSolver createSolver(SolverContext context, int variant) {
      switch (variant) {
         case 1:
            return new KlondikeSolver(context);
         case 2:
            return new SpiderSolver(context);
         case 3:
            return new FreeCellSolver(context);
         default:
            throw new IllegalArgumentException("Unsupported variant: " + variant);
      }
   }

   private static void configureReader(SolverBridge reader, int variant) {
      if (variant == 1) {
         reader.specialSourceGroupIndex = 1;
         reader.specialDestinationGroupIndex = 2;
      } else if (variant == 2) {
         reader.specialSourceGroupIndex = 1;
      }
   }

   private static GameState allocateState(SolverContext context, int variant) {
      switch (variant) {
         case 1:
            return allocateKlondikeState(context);
         case 2:
            return allocateSpiderState(context);
         case 3:
            return allocateFreeCellState(context);
         default:
            throw new IllegalArgumentException("Unsupported variant: " + variant);
      }
   }

   private static GameState allocateKlondikeState(SolverContext context) {
      GameState state = new GameState();
      state.stackGroups[0] = new StackGroup(context, "Stack", 0, 7, 1, 9);
      state.stackGroups[1] = new StackGroup(context, "Feed", 1, 1, 1, 258);
      state.stackGroups[2] = new StackGroup(context, "Pile", 2, 1, 1, 18);
      state.stackGroups[3] = new StackGroup(context, "Aces", 3, 4, 1, 2);
      for (int index = 0; index < 4; ++index) {
         state.stackGroups[3].stacks[index].foundationSuit = -1;
      }
      return state;
   }

   private static GameState allocateSpiderState(SolverContext context) {
      GameState state = new GameState();
      state.stackGroups[0] = new StackGroup(context, "Stack", 0, 10, 7, 1);
      state.stackGroups[1] = new StackGroup(context, "Feed", 1, 1, 1, 2);
      state.stackGroups[2] = new StackGroup(context, "Suits", 2, 8, 0, 66);
      return state;
   }

   private static GameState allocateFreeCellState(SolverContext context) {
      GameState state = new GameState();
      state.stackGroups[0] = new StackGroup(context, "Stack", 0, 8, 7, 9);
      state.stackGroups[1] = new StackGroup(context, "WorkArea", 1, 4, 1, 2);
      state.stackGroups[2] = new StackGroup(context, "Aces", 2, 4, 1, 2);
      state.stackGroups[2].stacks[0].foundationSuit = 2;
      state.stackGroups[2].stacks[1].foundationSuit = 4;
      state.stackGroups[2].stacks[2].foundationSuit = 3;
      state.stackGroups[2].stacks[3].foundationSuit = 1;
      return state;
   }

   private static Path solutionFileFor(Path inputFile) {
      return inputFile.resolveSibling("solution_" + inputFile.getFileName());
   }

   private static void deleteIfExists(Path path) throws IOException {
      Files.deleteIfExists(path);
   }

   private static int[] readMoves(Path solutionFile) {
      try {
         if (!Files.exists(solutionFile)) {
            return new int[0];
         }

         String content = Files.readString(solutionFile, StandardCharsets.UTF_8).trim();
         if (content.isEmpty()) {
            return new int[0];
         }

         String[] parts = content.split(",");
         List<Integer> moves = new ArrayList<>(parts.length);
         for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
               continue;
            }
            moves.add(Move.b(Integer.parseInt(trimmed)));
         }

         int[] values = new int[moves.size()];
         for (int index = 0; index < moves.size(); ++index) {
            values[index] = moves.get(index);
         }
         return values;
      } catch (IOException iOException) {
         throw new IllegalStateException("Failed to read solution file " + solutionFile, iOException);
      }
   }

   /**
    * Programmatic entry：直接用牌局文本（含首行 spider 头）求解，返回编码后的移动序列。
    */
   public static int[] solveDealText(String dealText) {
      SanitizedInput sanitizedInput = null;
      Path tempDir = null;
      Path actualSolutionFile = null;
      try {
         tempDir = Files.createTempDirectory("solvitaire-solver-");
         Path tempInputFile = tempDir.resolve("cards.txt");
         Files.writeString(tempInputFile, dealText, StandardCharsets.UTF_8);

         sanitizedInput = sanitizeInput(tempInputFile);
         Path actualInputFile = sanitizedInput.inputFile;
         actualSolutionFile = solutionFileFor(actualInputFile);
         deleteIfExists(actualSolutionFile);

         SolverContext context = new SolverContext();
         context.logLevel = 1;
         context.variantId = sanitizedInput.variant;
         context.files = new SolverFileSet(actualInputFile);
         context.files.b = 1;
         context.files.variantSlug = variantSlug(sanitizedInput.variant);
         context.initialState = allocateState(context, sanitizedInput.variant);
         context.bestSolutionState = new GameState();
         context.playbackState = new GameState();

         BaseSolver solver = createSolver(context, sanitizedInput.variant);
         solver.C = false;
         context.bridge = new SolverBridge(solver) {
         };
         configureReader(context.bridge, sanitizedInput.variant);

         solver.solve();
         return readMoves(actualSolutionFile);
      } catch (Exception e) {
         return new int[0];
      } finally {
         if (sanitizedInput != null && actualSolutionFile != null) {
            cleanupTemporaryFiles(sanitizedInput, actualSolutionFile);
         }
         if (tempDir != null) {
            try {
               Files.deleteIfExists(tempDir);
            } catch (Exception ignored) {
            }
         }
      }
   }

   private static final class SanitizedInput {
      final Path inputFile;
      final Path cleanupDir;
      final int variant;

      SanitizedInput(Path inputFile, Path cleanupDir, int variant) {
         this.inputFile = inputFile;
         this.cleanupDir = cleanupDir;
         this.variant = variant;
      }
   }
}


