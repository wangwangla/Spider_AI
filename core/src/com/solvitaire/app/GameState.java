/*
 * Decompiled with CFR 0.152.
 */
package com.solvitaire.app;

import com.solvitaire.app.StackGroup;
import java.util.Arrays;

public final class GameState {
    StackGroup[] stackGroups = new StackGroup[10];
    int dealIndex;
    int progressIndex;
    int[] moves;
    int[] moveAnnotations;
    int[] auxiliaryValues;
    private int[] reservedValues;
    int[] scoreByDepth;
    int depth;
    int solutionLength;

    GameState() {
        this.moves = new int[350];
        this.moveAnnotations = new int[350];
        this.auxiliaryValues = new int[350];
        this.reservedValues = new int[350];
        this.scoreByDepth = new int[350];
    }

    GameState(GameState sourceState, boolean workingCopy) {
        int n2 = 0;
        while (n2 < 10) {
            if (sourceState.stackGroups[n2] != null) {
                this.stackGroups[n2] = new StackGroup(sourceState.stackGroups[n2], workingCopy);
            }
            ++n2;
        }
        this.dealIndex = sourceState.dealIndex;
        this.progressIndex = sourceState.progressIndex;
        this.moves = Arrays.copyOf(sourceState.moves, sourceState.moves.length);
        this.depth = sourceState.depth;
        this.solutionLength = sourceState.solutionLength;
        this.moveAnnotations = Arrays.copyOf(sourceState.moveAnnotations, sourceState.moveAnnotations.length);
        this.auxiliaryValues = Arrays.copyOf(sourceState.auxiliaryValues, sourceState.auxiliaryValues.length);
        this.reservedValues = Arrays.copyOf(sourceState.reservedValues, sourceState.reservedValues.length);
        this.scoreByDepth = Arrays.copyOf(sourceState.scoreByDepth, sourceState.scoreByDepth.length);
    }

    final void reset() {
        this.dealIndex = 0;
        this.progressIndex = 0;
        this.depth = 0;
        this.solutionLength = 0;
    }
}





