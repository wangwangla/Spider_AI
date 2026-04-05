package com.solvitaire.app;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FreeCellSolverService {
   public FreeCellSolveResult solveBoard(String boardState) {
      Path tempDir = null;
      Path inputFile = null;
      Path solutionFile = null;

      try {
         tempDir = Files.createTempDirectory("freecell-solver-");
         inputFile = tempDir.resolve("board_freecell.txt");
         Files.write(inputFile, boardState.getBytes(StandardCharsets.UTF_8));
         solutionFile = tempDir.resolve("solution_" + inputFile.getFileName().toString());
         Files.deleteIfExists(solutionFile);

         SolverContext context = new SolverContext();
         context.logLevel = 99;
         context.variantId = 3;
         context.files = new SolverFileSet(inputFile);
         context.files.b = 1;
         context.files.variantSlug = "freecell";
         context.initialState = allocateFreeCellState(context);
         context.bestSolutionState = new GameState();
         context.playbackState = new GameState();

         BaseSolver solver = new FreeCellSolver(context);
         solver.solve();

         int[] rawMoves = readMoves(solutionFile);
         if (rawMoves.length == 0) {
            return new FreeCellSolveResult(false, Collections.<FreeCellSolutionStep>emptyList(), "No solution found.");
         }

         String[] descriptions = Move.a(context.bridge, rawMoves, 0, rawMoves.length, false);
         List<FreeCellSolutionStep> steps = new ArrayList<FreeCellSolutionStep>(rawMoves.length);
         for (int index = 0; index < rawMoves.length; ++index) {
            steps.add(decode(rawMoves[index], descriptions[index]));
         }
         return new FreeCellSolveResult(true, steps, "Solved in " + steps.size() + " moves.");
      } catch (IOException exception) {
         throw new IllegalStateException("Failed to solve FreeCell board.", exception);
      } finally {
         deleteQuietly(solutionFile);
         deleteQuietly(inputFile);
         deleteQuietly(tempDir);
      }
   }

   private static FreeCellSolutionStep decode(int rawMove, String description) {
      int moveFlags = rawMove >> 24;
      int sourceCode = rawMove >> 8 & 255;
      int destinationCode = rawMove & 255;
      return new FreeCellSolutionStep(
         rawMove,
         sourceCode / 10,
         sourceCode % 10,
         destinationCode / 10,
         destinationCode % 10,
         rawMove >> 16 & 15,
         (moveFlags & 16) != 0,
         (moveFlags & 1) != 0,
         description
      );
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
