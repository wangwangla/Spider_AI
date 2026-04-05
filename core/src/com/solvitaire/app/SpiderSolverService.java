package com.solvitaire.app;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SpiderSolverService {
   public SpiderSolveResult solveBoard(String boardState) {
      Path tempDir = null;
      Path inputFile = null;
      Path solutionFile = null;

      try {
         tempDir = Files.createTempDirectory("spider-solver-");
         inputFile = tempDir.resolve("board_spider.txt");
         Files.write(inputFile, boardState.getBytes(StandardCharsets.UTF_8));
         solutionFile = tempDir.resolve("solution_" + inputFile.getFileName().toString());
         Files.deleteIfExists(solutionFile);

         SolverContext context = new SolverContext();
         context.logLevel = 99;
         context.variantId = 2;
         context.files = new SolverFileSet(inputFile);
         context.files.b = 1;
         context.files.variantSlug = "spider";
         context.initialState = allocateSpiderState(context);
         context.bestSolutionState = new GameState();
         context.playbackState = new GameState();

         BaseSolver solver = new SpiderSolver(context);
         solver.solve();

         int[] rawMoves = readMoves(solutionFile);
         ArrayList<Integer> visibleMoves = new ArrayList<Integer>(rawMoves.length);
         for (int rawMove : rawMoves) {
            if (((rawMove >> 24) & 4) == 0) {
               visibleMoves.add(Integer.valueOf(rawMove));
            }
         }

         if (visibleMoves.isEmpty()) {
            return new SpiderSolveResult(false, Collections.<SpiderSolutionStep>emptyList(), "No solution found.");
         }

         int[] filteredMoves = new int[visibleMoves.size()];
         for (int index = 0; index < visibleMoves.size(); ++index) {
            filteredMoves[index] = visibleMoves.get(index).intValue();
         }

         String[] descriptions = Move.a(context.bridge, filteredMoves, 0, filteredMoves.length, false);
         List<SpiderSolutionStep> steps = new ArrayList<SpiderSolutionStep>(filteredMoves.length);
         for (int index = 0; index < filteredMoves.length; ++index) {
            steps.add(decode(filteredMoves[index], descriptions[index]));
         }
         return new SpiderSolveResult(true, steps, "Solved in " + steps.size() + " moves.");
      } catch (IOException exception) {
         throw new IllegalStateException("Failed to solve Spider board.", exception);
      } finally {
         deleteQuietly(solutionFile);
         deleteQuietly(inputFile);
         deleteQuietly(tempDir);
      }
   }

   private static SpiderSolutionStep decode(int rawMove, String description) {
      int moveFlags = rawMove >> 24;
      int sourceCode = rawMove >> 8 & 255;
      int destinationCode = rawMove & 255;
      return new SpiderSolutionStep(
         rawMove,
         sourceCode % 10,
         destinationCode % 10,
         rawMove >> 16 & 15,
         (moveFlags & 8) != 0,
         description
      );
   }

   private static GameState allocateSpiderState(SolverContext context) {
      GameState state = new GameState();
      state.stackGroups[0] = new StackGroup(context, "Stack", 0, 10, 7, 1);
      state.stackGroups[1] = new StackGroup(context, "Feed", 1, 1, 1, 2);
      state.stackGroups[2] = new StackGroup(context, "Suits", 2, 8, 0, 66);
      return state;
   }

   private static int[] readMoves(Path solutionFile) throws IOException {
      if (solutionFile == null || !Files.exists(solutionFile)) {
         return new int[0];
      }

      String content = new String(Files.readAllBytes(solutionFile), StandardCharsets.UTF_8).trim();
      if (content.isEmpty()) {
         return new int[0];
      }

      String[] parts = content.split(",");
      List<Integer> decoded = new ArrayList<Integer>(parts.length);
      for (String part : parts) {
         String token = part.trim();
         if (!token.isEmpty()) {
            decoded.add(Integer.valueOf(Move.b(Integer.parseInt(token))));
         }
      }

      int[] moves = new int[decoded.size()];
      for (int index = 0; index < decoded.size(); ++index) {
         moves[index] = decoded.get(index).intValue();
      }
      return moves;
   }

   private static void deleteQuietly(Path path) {
      if (path == null) {
         return;
      }

      try {
         Files.deleteIfExists(path);
      } catch (IOException ignored) {
      }
   }
}
