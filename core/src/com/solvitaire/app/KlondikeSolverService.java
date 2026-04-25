package com.solvitaire.app;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KlondikeSolverService {

    public KlondikeSolveResult solveBoard(String boardState) {
        Path tempDir = null;
        Path inputFile = null;
        Path solutionFile = null;

        try {
            tempDir = Files.createTempDirectory("klondike-solver-");
            inputFile = tempDir.resolve("board_klondike.txt");
            Files.write(inputFile, boardState.getBytes(StandardCharsets.UTF_8));
            solutionFile = tempDir.resolve("solution_" + inputFile.getFileName().toString());
            Files.deleteIfExists(solutionFile);

            SolverContext context = new SolverContext();
            context.logLevel = 99;
            context.variantId = 1;
            context.files = new SolverFileSet(inputFile);
            context.files.b = 1;
            context.files.variantSlug = "klondike";
            context.initialState = allocateKlondikeState(context);
            context.bestSolutionState = new GameState();
            context.playbackState = new GameState();

            BaseSolver solver = new KlondikeSolver(context);
            // Configure bridge for Klondike
            context.bridge.specialSourceGroupIndex = 1;
            context.bridge.specialDestinationGroupIndex = 2;
            solver.solve();

            int[] rawMoves = readMoves(solutionFile);
            ArrayList<Integer> visibleMoves = new ArrayList<>(rawMoves.length);
            for (int rawMove : rawMoves) {
                if (((rawMove >> 24) & 4) == 0) {
                    visibleMoves.add(rawMove);
                }
            }

            if (visibleMoves.isEmpty()) {
                return new KlondikeSolveResult(false, Collections.<KlondikeSolutionStep>emptyList(),
                        "No solution found.");
            }

            int[] filteredMoves = new int[visibleMoves.size()];
            for (int i = 0; i < visibleMoves.size(); i++) {
                filteredMoves[i] = visibleMoves.get(i);
            }

            String[] descriptions = Move.a(context.bridge, filteredMoves, 0, filteredMoves.length, false);
            List<KlondikeSolutionStep> steps = new ArrayList<>(filteredMoves.length);
            for (int i = 0; i < filteredMoves.length; i++) {
                steps.add(decode(filteredMoves[i], descriptions[i]));
            }
            return new KlondikeSolveResult(true, steps, "Solved in " + steps.size() + " moves.");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to solve Klondike board.", e);
        } finally {
            deleteQuietly(solutionFile);
            deleteQuietly(inputFile);
            deleteQuietly(tempDir);
        }
    }

    private static KlondikeSolutionStep decode(int rawMove, String description) {
        int moveFlags = rawMove >> 24;
        int sourceCode = (rawMove >> 8) & 255;
        int destinationCode = rawMove & 255;
        int cardCount = (rawMove >> 16) & 15;
        boolean dealMove = (moveFlags & 8) != 0;

        int sourceGroupIndex = sourceCode / 10;
        int sourceStackIndex = sourceCode % 10;
        int destGroupIndex = destinationCode / 10;
        int destStackIndex = destinationCode % 10;

        return new KlondikeSolutionStep(
                rawMove, sourceGroupIndex, sourceStackIndex,
                destGroupIndex, destStackIndex, cardCount,
                dealMove, description
        );
    }

    private static GameState allocateKlondikeState(SolverContext context) {
        GameState state = new GameState();
        state.stackGroups[0] = new StackGroup(context, "Stack", 0, 7, 1, 9);
        state.stackGroups[1] = new StackGroup(context, "Feed", 1, 1, 1, 258);
        state.stackGroups[2] = new StackGroup(context, "Pile", 2, 1, 1, 18);
        state.stackGroups[3] = new StackGroup(context, "Aces", 3, 4, 1, 2);
        for (int i = 0; i < 4; i++) {
            state.stackGroups[3].stacks[i].foundationSuit = -1;
        }
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
        String[] tokens = content.split("[,\\s]+");
        int[] moves = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            moves[i] = Integer.parseInt(tokens[i].trim());
        }
        return moves;
    }

    private static void deleteQuietly(Path path) {
        if (path != null) {
            try { Files.deleteIfExists(path); } catch (IOException ignored) {}
        }
    }
}
