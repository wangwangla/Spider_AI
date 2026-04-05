/*
 * Decompiled with CFR 0.152.
 */
package com.solvitaire.app;

import com.solvitaire.app.Move;
import com.solvitaire.app.Card;
import com.solvitaire.app.GameState;
import com.solvitaire.app.CardRun;
import com.solvitaire.app.SolverContext;
import com.solvitaire.app.BaseSolver;
import com.solvitaire.app.SpiderBridge;
import com.solvitaire.app.CardStack;
import com.solvitaire.app.StackGroup;
import java.io.File;
import java.util.HashMap;
import java.util.Set;

/*
 * Renamed from com.solvitaire.app.oq
 */
final class SpiderSolver
extends BaseSolver {
    private int[] activeDealProgressBaseScores;
    private int[] dealProgressCaps = new int[]{30, 60, 30, 30, 30, 30, 30};
    private int[] exposeDealProgressBaseScores = new int[]{30, 60, 75, 75, 30, 30, 30};
    private int[] tightMoveDealProgressBaseScores = this.dealProgressCaps;
    private double[] dealProgressJoinWeights = new double[]{0.0, 0.0, -3.0, -2.0, 0.0, 0.0, 0.0};
    private int[] progressComplexityBases = new int[]{100, 150, 400, 400, 180, 500, 500};
    private double[] progressComplexityWeights = new double[]{-50.0, -50.0, -60.0, -40.0, -3.0, -27.0, -9.0};
    private int defaultMaxDealRows = 7;
    private int minimumProgressScore = 30;
    private int[] joinPenaltyBySuitMode = new int[]{-15, -4, -5, -5, -15};
    private int matchingThreshold;
    private int fromSpaceThreshold;
    private int toSpaceThreshold;
    private int kingToSpaceThreshold;
    private int crossSuitThreshold;
    private int shiftThreshold;
    private int[] activeSuitCompletionAdjustments;
    private int[] defaultSuitCompletionAdjustments = new int[]{-30, -10, -20, -20, -30};
    private int[] tightMoveSuitCompletionAdjustments = new int[]{-10, -10, -10, -10, -10};
    private int[][] dealPenaltyStats;
    private int matchingAttemptCount = 0;
    private int crossSuitAttemptCount = 0;
    private int fromSpaceAttemptCount = 0;
    private int toSpaceAttemptCount = 0;
    private int kingToSpaceAttemptCount = 0;
    private int shiftAttemptCount = 0;
    private int suitCompletionAdjustmentCount = 0;
    private int dealAttemptCount = 0;
    private boolean exposeMode;
    private boolean tightMoveMode = false;
    private int runningScore;
    int[] completedSuitCards = new int[8];
    private int maxDealRows;
    static private int[] suitCompletionMasks;

    static {
        int[] nArray = new int[5];
        nArray[1] = 1;
        nArray[2] = 16;
        nArray[3] = 256;
        nArray[4] = 4096;
        suitCompletionMasks = new int[]{1, 2, 4, 8, 16, 32, 1024, 2048, 256, 512};
    }

    SpiderSolver(SolverContext om_02) {
        super(om_02, 2000);
        this.f = 10;
        this.n = 2;
        this.o = 104;
        this.v = 5;
        this.q = true;
    }

    @Override
    final String getSolverName() {
        return "Spider";
    }

    @Override
    final boolean initializeSolver() {
        this.g = new int[50][this.f];
        this.h = new int[this.f];
        this.i = new int[this.f];
        this.r = new int[this.f][10];
        this.y = new int[5][this.f];
        this.completedSuitCards = new int[8];
        this.maxDealRows = this.defaultMaxDealRows;
        this.j = this.progressComplexityBases;
        this.k = this.progressComplexityWeights;
        this.l = this.minimumProgressScore;
        this.dealPenaltyStats = new int[this.maxDealRows][10];
        this.initializeBaseState();
        this.e = String.valueOf(this.d.workspaceRoot) + "spider" + File.separator;
        if (this.C) {
            if (this.d.logLevel <= 4) {
                this.d.log("Running Spider");
            }
            this.d.bridge = new SpiderBridge(this);
            if (this.d.logLevel <= 3) {
                this.d.log("Auto constructed");
            }
            this.d.bridge.d();
        }
        this.suitCount = 0;
        if (!this.d.bridge.e()) {
            return false;
        }
        this.d.searchState = new GameState(this.d.initialState, true);
        if (this.d.logLevel <= 4) {
            this.d.log("Max club " + this.d.fontStats.e + " min spade " + this.d.fontStats.f);
        }
        this.a((CardStack)null);
        this.exposeMode = false;
        this.F = 0;
        this.G = 0;
        this.runningScore = this.d.files.l;
        this.tightMoveMode = false;
        return true;
    }

    @Override
    final void search(int lastMove, int depthLimit) {
        this.searchInternal(lastMove, depthLimit, false);
    }

    private void searchInternal(int lastMove, int depthLimit, boolean skipStandardMoves) {
        if (this.d.ai) {
            this.d.ai = false;
            this.matchingThreshold = this.joinPenaltyBySuitMode[this.suitCount];
            this.fromSpaceThreshold = 7;
            this.toSpaceThreshold = 13;
            this.kingToSpaceThreshold = -15;
            this.crossSuitThreshold = 27;
            this.shiftThreshold = 37;
            this.activeSuitCompletionAdjustments = this.defaultSuitCompletionAdjustments;
            this.activeDealProgressBaseScores = this.exposeDealProgressBaseScores;
            if (this.d.files.maxMoves < 999) {
                this.exposeMode = false;
                this.tightMoveMode = true;
                this.fromSpaceThreshold = this.fromSpaceThreshold * 200 / (this.d.files.maxMoves + 80);
                this.toSpaceThreshold = this.toSpaceThreshold * 200 / (this.d.files.maxMoves + 80);
                this.crossSuitThreshold = this.crossSuitThreshold * 200 / (this.d.files.maxMoves + 50);
                this.shiftThreshold = this.shiftThreshold * 200 / (this.d.files.maxMoves + 50);
                this.activeSuitCompletionAdjustments = this.tightMoveSuitCompletionAdjustments;
                this.activeDealProgressBaseScores = this.tightMoveDealProgressBaseScores;
            } else {
                this.exposeMode = true;
                this.d.complexity += -300;
                int exposeDepthLimit = this.d.searchState.depth + 5;
                if (this.d.logLevel < 3) {
                    this.d.log("Expose triggered, complexity to " + this.d.complexity + " target depth now " + exposeDepthLimit + " do expose solve");
                }
                this.searchInternal(lastMove, exposeDepthLimit, false);
                this.d.complexity -= -300;
                if (this.d.logLevel < 5) {
                    this.d.log("Back from expose solve, expose state " + this.exposeMode + " depth " + depthLimit + " complexity " + this.d.complexity);
                }
                if (this.B) {
                    if (this.d.logLevel <= 5) {
                        this.d.log("Solved in expose!");
                    }
                    this.D = 999;
                    this.exposeMode = false;
                    return;
                }
            }
        }
        if (this.exposeMode) {
            if (depthLimit == 0) {
                if (this.d.logLevel < 5) {
                    this.d.log("Just exited expose state at recursion " + this.d.searchState.depth + ", complexity now " + this.d.complexity);
                }
                this.exposeMode = false;
            } else if (this.d.searchState.depth >= depthLimit) {
                return;
            }
        }
        this.l();
        if (this.d.searchState.depth > this.maxSearchDepth) {
            if (this.d.logLevel <= 2) {
                this.d.log("Exiting solve due to recursion limit");
            }
            return;
        }
        if (this.d.logLevel <= 3) {
            this.d.log("into solve level " + this.d.searchState.depth);
        }
        if (this.d.searchStepCount++ % 100000L == 0L) {
            this.b(4);
            this.a(4);
            this.b(4, this.d.searchState.stackGroups[0]);
        }
        if (!this.B) {
            int searchResult = this.a(this.d.searchState, lastMove, false);
            if (searchResult == 2) {
                this.D = 999;
            } else if (searchResult == 1) {
                return;
            }
        }
        if (this.d.complexity > 0) {
            return;
        }
        if (this.D < 0 && this.exposeMode && !skipStandardMoves) {
            this.tryJoinMoves(8, "exposematch", lastMove, depthLimit);
        }
        if (this.D < 0 && this.exposeMode && !skipStandardMoves) {
            this.tryJoinMoves(9, "exposeany", lastMove, depthLimit);
        }
        if (this.D < 0 && !skipStandardMoves) {
            int previousComplexity = this.j(this.matchingThreshold);
            if (previousComplexity < 999999) {
                ++this.matchingAttemptCount;
                this.tryJoinMoves(1, "matching", lastMove, depthLimit);
                --this.matchingAttemptCount;
                this.d.complexity = previousComplexity;
            }
        }
        if (this.D < 0 && !skipStandardMoves && !this.tightMoveMode) {
            int previousComplexity = this.j(this.crossSuitThreshold);
            if (previousComplexity < 999999) {
                ++this.crossSuitAttemptCount;
                this.tryJoinMoves(5, "matchdiff", lastMove, depthLimit);
                --this.crossSuitAttemptCount;
                this.d.complexity = previousComplexity;
            }
        }
        int emptyStackCount = this.d.searchState.stackGroups[0].emptyStackCount;
        if (this.D < 0 && !skipStandardMoves && emptyStackCount == 0) {
            int previousComplexity = this.j(this.fromSpaceThreshold);
            if (previousComplexity < 999999) {
                ++this.fromSpaceAttemptCount;
                this.tryJoinMoves(4, "fromspace", lastMove, depthLimit);
                --this.fromSpaceAttemptCount;
                this.d.complexity = previousComplexity;
            }
        }
        if (this.D < 0 && !skipStandardMoves && emptyStackCount > 0) {
            int previousComplexity = emptyStackCount > 1 && !this.tightMoveMode ? this.j(0) : this.j(this.toSpaceThreshold);
            if (previousComplexity < 999999) {
                ++this.toSpaceAttemptCount;
                this.tryJoinMoves(2, "tospace", lastMove, depthLimit);
                --this.toSpaceAttemptCount;
                this.d.complexity = previousComplexity;
            }
        }
        if (this.D < 0 && !skipStandardMoves && emptyStackCount > 0) {
            int previousComplexity = emptyStackCount > 1 ? this.j(this.kingToSpaceThreshold) : this.j(this.toSpaceThreshold);
            if (previousComplexity < 999999) {
                ++this.kingToSpaceAttemptCount;
                this.tryJoinMoves(3, "kingtospace", lastMove, depthLimit);
                --this.kingToSpaceAttemptCount;
                this.d.complexity = previousComplexity;
            }
        }
        if (this.D < 0 && !skipStandardMoves && this.d.searchState.dealIndex < this.v) {
            int previousComplexity = this.j(this.shiftThreshold);
            if (previousComplexity < 999999) {
                ++this.shiftAttemptCount;
                this.tryJoinMoves(6, "shift", lastMove, depthLimit);
                --this.shiftAttemptCount;
                this.d.complexity = previousComplexity;
            }
        }
        if (this.D < 0 && this.d.searchState.progressIndex < this.maxDealRows) {
            if (this.d.logLevel <= 2) {
                this.d.log("At level " + this.d.searchState.depth + " try a deal");
            }
            this.tryDealMove(lastMove, depthLimit);
        }
        if (this.D < 0 && !skipStandardMoves && this.tightMoveMode) {
            int previousComplexity = this.j(this.crossSuitThreshold);
            if (previousComplexity < 999999) {
                ++this.crossSuitAttemptCount;
                this.tryJoinMoves(5, "matchdiff", lastMove, depthLimit);
                --this.crossSuitAttemptCount;
                this.d.complexity = previousComplexity;
            }
        }
        if (this.D < 0 && !skipStandardMoves) {
            int previousComplexity = this.j(this.shiftThreshold);
            if (previousComplexity < 999999) {
                ++this.shiftAttemptCount;
                this.tryJoinMoves(7, "shiftfordeal", lastMove, depthLimit);
                --this.shiftAttemptCount;
                this.d.complexity = previousComplexity;
            }
        }
        if (this.D >= 0) {
            --this.D;
            if (this.d.logLevel <= 1) {
                this.d.log("Backout now " + this.D);
            }
        }
        if (this.d.logLevel < 3) {
            this.d.log("All done at level " + this.d.searchState.depth + " complexity " + this.d.complexity + " backout " + this.D);
        }
    }

    @Override
    final long computeStateHash() {
        long l2 = 0L;
        CardStack[] os_0Array = this.d.searchState.stackGroups[0].stacks;
        int n2 = this.d.searchState.stackGroups[0].stacks.length;
        int n3 = 0;
        while (n3 < n2) {
            CardStack os_02 = os_0Array[n3];
            l2 += this.a(os_02, l2, true, false);
            ++n3;
        }
        return l2;
    }

    @Override
    final int[] d() {
        HashMap hashMap = new HashMap(104);
        this.countKnownCards(hashMap);
        return this.b(hashMap, 2);
    }

    private int countKnownCards(HashMap hashMap) {
        StackGroup tableauGroup = this.d.initialState.stackGroups[0];
        StackGroup completedSuitsGroup = this.d.initialState.stackGroups[2];
        int dealIndex = this.d.initialState.dealIndex;
        int maxCopiesPerCard = 8;
        if (this.suitCount > 0) {
            maxCopiesPerCard = 8 / this.suitCount;
        }
        if (this.d.logLevel <= 4) {
            this.d.log("numSuits is " + this.suitCount + " so maxCards is " + maxCopiesPerCard);
        }
        int knownCardCount = this.a(hashMap, tableauGroup, maxCopiesPerCard);
        if (this.d.logLevel <= 3) {
            this.d.log("Totals numcards after stacks is " + knownCardCount);
        }
        knownCardCount += this.a(hashMap, completedSuitsGroup, maxCopiesPerCard);
        if (this.d.logLevel <= 3) {
            this.d.log("Total numcards after removed suits is " + knownCardCount);
        }
        int dealRow = dealIndex;
        while (dealRow < 5) {
            if (this.y[dealRow][0] == 0) break;
            dealIndex = 0;
            while (dealIndex < this.f) {
                int copiesSeen;
                if (this.d.logLevel <= 1) {
                    this.d.log("Testing deal " + dealRow + " stack " + dealIndex + " card " + this.y[dealRow][dealIndex]);
                }
                if ((copiesSeen = this.a(hashMap, this.y[dealRow][dealIndex])) > maxCopiesPerCard) {
                    this.d.fontStats.reset();
                    this.d.fail("ERROR - Too many " + SpiderSolver.f(this.y[dealRow][dealIndex]) + " of " + SpiderSolver.d(this.y[dealRow][dealIndex]) + "s in the deck");
                }
                ++knownCardCount;
                ++dealIndex;
            }
            ++dealRow;
        }
        if (this.suitCount == 0) {
            this.suitCount = 1;
            Set set = hashMap.keySet();
            for (Object object : set) {
                Integer cardId = (Integer)object;
                if (cardId / 100 > 2) {
                    if (this.d.logLevel <= 4) {
                        this.d.log("Key of " + cardId + " triggers numSuits of 4");
                    }
                    this.suitCount = 4;
                    break;
                }
                if (cardId / 100 <= 1) continue;
                if (this.d.logLevel <= 4) {
                    this.d.log("Key of " + cardId + " triggers numSuits of 2");
                }
                this.suitCount = 2;
            }
        }
        if (this.d.logLevel <= 4) {
            this.d.log("Count of known cards is now " + knownCardCount);
        }
        return knownCardCount;
    }

    final void addCompletedSuitRunToInitialState(int suit) {
        StackGroup completedSuitGroup = this.d.initialState.stackGroups[2];
        int nextSuitSlot = 0;
        while (nextSuitSlot < completedSuitGroup.stacks.length) {
            if (completedSuitGroup.stacks[nextSuitSlot].topRun == null) break;
            ++nextSuitSlot;
        }
        CardRun completedRun = new CardRun();
        int rank = 13;
        while (rank > 0) {
            CardStack completedSuitStack = completedSuitGroup.stacks[nextSuitSlot];
            completedRun.cards[completedRun.cardCount++] = this.b(completedSuitStack, suit * 100 + rank);
            --rank;
        }
        completedSuitGroup.addCompletedSuitRun(completedRun);
    }

    @Override
    final int a(CardStack os_02) {
        StackGroup ot_02 = this.d.initialState.stackGroups[0];
        HashMap hashMap = new HashMap();
        int n2 = this.countKnownCards(hashMap);
        if (this.d.logLevel <= 4) {
            this.d.log("numCards is now " + n2);
        }
        if (n2 == 103 && this.d.searchState != null) {
            int n3 = 8 / this.suitCount;
            if (this.d.logLevel <= 5) {
                this.d.log("Found 103 cards");
            }
            int n4 = 1;
            while (n4 < 14) {
                int n5 = 1;
                while (n5 < this.suitCount + 1) {
                    int n6 = n5 * 100 + n4;
                    Integer integer = (Integer)hashMap.get(n6);
                    if (integer != null && integer == n3 - 1) {
                        CardStack[] os_0Array = ot_02.stacks;
                        int n7 = ot_02.stacks.length;
                        int n8 = 0;
                        while (n8 < n7) {
                            CardStack os_03 = os_0Array[n8];
                            if (os_03.runs.size() > 0) {
                                CardRun ok_02 = (CardRun)os_03.runs.getFirst();
                                if (ok_02.cardCount > 0 && ok_02.cards[0].cardId == 0) {
                                    if (this.d.logLevel <= 5) {
                                        this.d.log("***Final card (on stack " + os_03.stackIndex + ") must be " + n6);
                                    }
                                    this.g[0][os_03.stackIndex] = n6;
                                    ok_02.a(n6);
                                    if (this.d.searchState != null) {
                                        CardRun ok_03 = (CardRun)this.d.searchState.stackGroups[0].stacks[os_03.stackIndex].runs.getFirst();
                                        ok_03.a(n6);
                                    }
                                }
                            }
                            ++n8;
                        }
                    }
                    ++n5;
                }
                ++n4;
            }
        }
        return n2;
    }

    private void updateSuitModeFromCard(int cardId) {
        if (cardId / 100 > 2) {
            this.suitCount = 4;
            return;
        }
        if (cardId / 100 > 1) {
            this.suitCount = 2;
        }
    }

    @Override
    final int a(HashMap hashMap) {
        this.suitCount = 1;
        StackGroup tableauGroup = this.d.initialState.stackGroups[0];
        int knownCardCount = 0;
        int stackIndex = 0;
        while (stackIndex < this.f) {
            int rowIndex = 0;
            while (rowIndex < 6) {
                if (stackIndex < 4 || rowIndex < 5) {
                    ++knownCardCount;
                    this.a(hashMap, this.g[rowIndex][stackIndex], "stack");
                    this.updateSuitModeFromCard(this.g[rowIndex][stackIndex]);
                }
                ++rowIndex;
            }
            ++stackIndex;
        }
        int dealRow = this.d.initialState.dealIndex;
        while (dealRow < this.v) {
            if (this.y[dealRow][0] == 0) break;
            int dealStackIndex = 0;
            while (dealStackIndex < tableauGroup.stacks.length) {
                this.a(hashMap, this.y[dealRow][dealStackIndex], "deal ");
                ++knownCardCount;
                this.updateSuitModeFromCard(this.y[dealRow][dealStackIndex]);
                ++dealStackIndex;
            }
            ++dealRow;
        }
        return knownCardCount;
    }

    private void tryJoinMoves(int mode, String modeName, int lastMove, int depthLimit) {
        if (this.d.logLevel <= 3) {
            this.d.log(String.format("Entered dojoins mode %s complexity %d lastmove %08x backout %d", modeName, this.d.complexity, lastMove, this.D));
        }
        CardStack[] tableauStacks = this.d.searchState.stackGroups[0].stacks;
        for (CardStack sourceStack : tableauStacks) {
            if (sourceStack.topRun == null) {
                continue;
            }
            int sourceRunCount = sourceStack.runs.size();
            if ((mode == 8 || mode == 2 || mode == 3 || mode == 11 || mode == 10 || mode == 9) && sourceRunCount < 2) {
                continue;
            }
            if (this.D > 0) {
                break;
            }
            if (mode != 1 && mode != 8 && mode != 2 && mode != 3 && mode != 10 && mode != 11) {
                for (CardStack targetStack : tableauStacks) {
                    if (this.D > 0) {
                        break;
                    }
                    if (targetStack == sourceStack) {
                        continue;
                    }
                    byte joinRule;
                    switch (mode) {
                        case 4: {
                            joinRule = 3;
                            break;
                        }
                        case 5: {
                            joinRule = 4;
                            break;
                        }
                        case 9: {
                            if (((CardRun)sourceStack.runs.get(sourceRunCount - 2)).cards[0].cardId != 0) continue;
                            joinRule = 4;
                            break;
                        }
                        default: {
                            joinRule = 5;
                        }
                    }
                    int joinSize = targetStack.a(sourceStack, joinRule, false);
                    if (joinSize < 0) {
                        continue;
                    }
                    this.exploreJoinMove(mode, modeName, joinRule, depthLimit, sourceStack, targetStack, joinSize, sourceStack.topRun.cardCount, lastMove);
                    if ((sourceRunCount = sourceStack.runs.size()) < 2) {
                        break;
                    }
                }
                continue;
            }
            CardStack selectedTarget = null;
            int selectedJoinSize = -1;
            int bestCombinedLength = -1;
            byte joinRule = 1;
            for (CardStack targetStack : tableauStacks) {
                if (this.D > 0) {
                    break;
                }
                if (targetStack == sourceStack) {
                    continue;
                }
                switch (mode) {
                    case 1: {
                        joinRule = 1;
                        break;
                    }
                    case 2: {
                        if (targetStack.topRun != null || sourceStack.topRun.cards[0].rank == 13) continue;
                        joinRule = 2;
                        break;
                    }
                    case 3: {
                        if (targetStack.topRun != null || sourceStack.topRun.cards[0].rank != 13) continue;
                        joinRule = 2;
                        break;
                    }
                    case 8: {
                        if (((CardRun)sourceStack.runs.get(sourceRunCount - 2)).cards[0].cardId != 0) continue;
                        joinRule = 1;
                        break;
                    }
                    case 10: {
                        if (targetStack.topRun != null) continue;
                        joinRule = 6;
                        break;
                    }
                    case 11: {
                        if (targetStack.topRun != null) continue;
                        joinRule = 2;
                    }
                }
                int joinSize = targetStack.a(sourceStack, joinRule, false);
                if (joinSize >= 0 && targetStack.topRun != null && sourceStack.topRun.cards[sourceStack.topRun.cardCount - 1].rank == 1) {
                    int combinedRunLength = targetStack.topRun.cardCount + sourceStack.topRun.cardCount;
                    CardRun previousRun = targetStack.getPreviousRun();
                    if (targetStack.runs.size() + combinedRunLength >= 12 && previousRun != null && !previousRun.faceDown && previousRun.cards[previousRun.cardCount - 1].cardId == 0) {
                        this.d.log("Disallow join of " + sourceStack + " to " + targetStack + " due to possible suit completion");
                        joinSize = -1;
                    }
                }
                if (joinSize < 0) {
                    continue;
                }
                if (mode != 1 && mode != 8) {
                    selectedJoinSize = joinSize;
                    selectedTarget = targetStack;
                    break;
                }
                int combinedRunLength = targetStack.topRun.cardCount + sourceStack.topRun.cardCount;
                if (combinedRunLength <= bestCombinedLength) {
                    continue;
                }
                bestCombinedLength = combinedRunLength;
                selectedJoinSize = joinSize;
                selectedTarget = targetStack;
            }
            if (selectedJoinSize < 0) {
                continue;
            }
            this.exploreJoinMove(mode, modeName, joinRule, depthLimit, sourceStack, selectedTarget, selectedJoinSize, sourceStack.topRun.cardCount, lastMove);
        }
        if (this.d.logLevel <= 3) {
            this.d.log("Exited dojoins for mode " + modeName);
        }
    }

    private void exploreJoinMove(int mode, String modeName, int joinRule, int depthLimit, CardStack sourceStack, CardStack targetStack, int joinSize, int movedCards, int lastMove) {
        boolean skipStandardMoves = false;
        if (joinSize == 0 && lastMove > 0) {
            int previousSourceStack = lastMove & 0xFF;
            int previousTargetStack = lastMove >> 8 & 0xFF;
            int previousMovedCards = (lastMove & 0xF0000) >> 16;
            if (sourceStack.stackIndex == previousSourceStack && movedCards == previousMovedCards) {
                if (targetStack.stackIndex != previousTargetStack) {
                    joinSize = -1;
                }
            } else if (mode == 6 || mode == 5) {
                if (mode == 6) {
                    if (targetStack.stackIndex == previousTargetStack || sourceStack.stackIndex == previousSourceStack) {
                        joinSize = -1;
                    } else if (sourceStack.stackIndex != previousTargetStack && targetStack.stackIndex != previousSourceStack) {
                        joinSize = -1;
                    }
                }
                if (joinSize >= 0 && this.shouldAvoidEmptyStackMove(sourceStack)) {
                    joinSize = -1;
                }
            } else if (mode == 7 || mode == 10 || mode == 11) {
                skipStandardMoves = true;
            }
        } else if (joinSize > 0) {
            if (mode == 2) {
                if (this.shouldAvoidEmptyStackMove(sourceStack)) {
                    joinSize = -1;
                }
            } else if (mode == 10 || mode == 11) {
                skipStandardMoves = true;
            }
        }
        if (joinSize < 0) {
            return;
        }
        int appliedJoinSize = targetStack.a(sourceStack, joinSize, this.d.searchState.stackGroups[2]);
        boolean splitMove = joinRule == 1;
        int encodedMove = Move.a(appliedJoinSize, movedCards, sourceStack, targetStack, splitMove);
        this.d.searchState.moves[this.d.searchState.depth] = encodedMove;
        ++this.d.searchState.depth;
        long stateHash = this.computeStateHash();
        if (!this.b(targetStack, sourceStack)) {
            if (this.D < 0) {
                this.D = this.b(stateHash);
                if (this.D < 0) {
                    if (!this.exposeMode) {
                        this.a(stateHash);
                    }
                    int savedComplexity = this.d.complexity;
                    int savedSuitCompletionCount = this.suitCompletionAdjustmentCount;
                    int savedRunningScore = this.runningScore;
                    int completedSuitCount = (encodedMove & 0x70000000) >> 28;
                    if ((mode == 1 || mode == 8 || mode == 4) && completedSuitCount != 0) {
                        if (this.d.logLevel < 3) {
                            this.d.log("Completed a suit");
                        }
                        int suitCompletionAdjustment = this.activeSuitCompletionAdjustments[this.suitCount];
                        this.d.complexity += this.a(suitCompletionAdjustment, 0, 0, 0.0);
                        ++this.suitCompletionAdjustmentCount;
                        this.runningScore += 100;
                    }
                    if (this.d.logLevel <= 2) {
                        this.d.log("Entering level " + this.d.searchState.depth + " for mode " + modeName + " complexity " + this.d.complexity);
                    }
                    boolean sourceBlockedFlag = false;
                    boolean targetBlockedFlag = false;
                    if (sourceStack.topRun != null) {
                        sourceBlockedFlag = sourceStack.topRun.faceDown;
                        sourceStack.topRun.faceDown = false;
                        if (sourceStack.topRun.cardCount == 1 && sourceStack.topRun.cards[0].cardId == 0) {
                            if (this.d.logLevel <= 3) {
                                this.d.log("Exposed a card, try reading it");
                            }
                            if (!this.a(sourceStack, lastMove)) {
                                if (this.d.logLevel <= 5) {
                                    this.d.log("checkReadCard failed, backing out 1");
                                }
                                this.D = 0;
                            } else {
                                Card exposedCard = sourceStack.getTopCard();
                                if (this.d.logLevel <= 2) {
                                    this.d.log("Just read card " + exposedCard.cardId);
                                }
                                CardRun targetTopRun = targetStack.topRun;
                                if (exposedCard != null && targetTopRun != null && targetTopRun.cardCount >= movedCards) {
                                    Card splitCard = targetTopRun.cards[targetTopRun.cardCount - movedCards];
                                    if (exposedCard.cardId == splitCard.cardId + 1 && !sourceBlockedFlag) {
                                        this.d.searchState.moves[this.d.searchState.depth - 1] = encodedMove |= 0x1000000;
                                        this.d.initialState.moves[this.d.searchState.depth - 1] = encodedMove;
                                        this.d.bridge.bridgeSlots[this.d.bridge.activeBridgeSlotIndex].encodedMove = encodedMove;
                                        if (appliedJoinSize < 20) {
                                            appliedJoinSize += 20;
                                        }
                                        if (this.d.logLevel <= 5) {
                                            this.d.log(String.format("Added missing split flag on move giving %08x at level %d, split %d", encodedMove, this.d.searchState.depth - 1, appliedJoinSize));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!this.B && this.D < 0 && targetStack.topRun != null) {
                        targetBlockedFlag = targetStack.topRun.faceDown;
                        targetStack.topRun.faceDown = false;
                        if (targetStack.topRun.cardCount == 1 && targetStack.topRun.cards[0].cardId == 0 && !this.a(targetStack, lastMove)) {
                            if (this.d.logLevel <= 5) {
                                this.d.log("checkReadCard failed on suit completion, backing out 1");
                            }
                            this.D = 0;
                        }
                    }
                    if (!this.B && this.D < 0) {
                        if (this.runningScore > 0 && ((encodedMove & 0x70000000) >> 28) == 0) {
                            --this.runningScore;
                        }
                        this.searchInternal(encodedMove, depthLimit, skipStandardMoves);
                    }
                    if (sourceStack.topRun != null) {
                        sourceStack.topRun.faceDown = sourceBlockedFlag;
                    }
                    if (targetStack.topRun != null) {
                        targetStack.topRun.faceDown = targetBlockedFlag;
                    }
                    if (this.d.logLevel <= 2) {
                        this.d.log("Exited level " + this.d.searchState.depth + " for mode " + modeName);
                    }
                    this.d.complexity = savedComplexity;
                    this.suitCompletionAdjustmentCount = savedSuitCompletionCount;
                    this.runningScore = savedRunningScore;
                }
            }
            if (this.D >= 0) {
                --this.D;
            }
        }
        --this.d.searchState.depth;
        targetStack.b(sourceStack, appliedJoinSize, this.d.searchState.stackGroups[2]);
    }

    private boolean shouldAvoidEmptyStackMove(CardStack sourceStack) {
        Card sourceTopCard = sourceStack.topRun.cards[0];
        if (sourceTopCard.rank == 13) {
            return false;
        }
        int requiredNextCard = sourceTopCard.cardId + 1;
        CardStack[] tableauStacks = this.d.searchState.stackGroups[0].stacks;
        for (CardStack candidateStack : tableauStacks) {
            if (candidateStack == sourceStack) {
                continue;
            }
            int candidateRunCount = candidateStack.runs.size();
            if (candidateRunCount <= 1 || candidateStack.topRun.cards[0].rank != sourceTopCard.rank) {
                continue;
            }
            CardRun previousCandidateRun = (CardRun)candidateStack.runs.get(candidateRunCount - 2);
            Card candidateConnector = previousCandidateRun.cards[previousCandidateRun.cardCount - 1];
            if (candidateConnector.cardId == 0 || candidateConnector.cardId != requiredNextCard) {
                continue;
            }
            sourceTopCard = candidateStack.topRun.cards[0];
            requiredNextCard = sourceTopCard.cardId + 1;
            int sourceRunCount = sourceStack.runs.size();
            CardRun previousSourceRun = (CardRun)sourceStack.runs.get(sourceRunCount - 2);
            Card sourceConnector = previousSourceRun.cards[previousSourceRun.cardCount - 1];
            if (sourceConnector.cardId != requiredNextCard || sourceStack.stackIndex > candidateStack.stackIndex) {
                return true;
            }
        }
        return false;
    }

    @Override
    final boolean a(CardStack os_02, CardStack os_03) {
        if (os_02.topRun == null) {
            return false;
        }
        if (this.d.searchState.dealIndex == 5 && this.d.searchState.depth > 2) {
            int n2 = this.d.searchState.moves[this.d.searchState.depth - 3];
            int n3 = (n2 & 0xF0000) >> 16;
            n2 = this.d.searchState.moves[this.d.searchState.depth - 3];
            int n4 = n2 & 0xFF;
            n2 = this.d.searchState.moves[this.d.searchState.depth - 3];
            int n5 = n2 >> 8 & 0xFF;
            n2 = this.d.searchState.moves[this.d.searchState.depth - 2];
            int n6 = (n2 & 0xF0000) >> 16;
            n2 = this.d.searchState.moves[this.d.searchState.depth - 2];
            int n7 = n2 & 0xFF;
            n2 = this.d.searchState.moves[this.d.searchState.depth - 2];
            n2 = n2 >> 8 & 0xFF;
            int n8 = os_02.topRun.cardCount;
            int n9 = os_02.stackIndex;
            int n10 = os_03.stackIndex;
            if (n3 == n8 && n6 == n8 && n4 == n10 && n5 == n2 && n7 == n9) {
                CardStack os_04 = this.d.searchState.stackGroups[0].stacks[n5];
                n9 = 1;
                CardStack[] os_0Array = this.d.searchState.stackGroups[0].stacks;
                n5 = this.d.searchState.stackGroups[0].stacks.length;
                n4 = 0;
                while (n4 < n5) {
                    int n11;
                    CardStack os_05 = os_0Array[n4];
                    if (os_04 != os_05 && (n11 = os_04.a(os_05, 1, false)) >= 0) {
                        n9 = 0;
                        break;
                    }
                    ++n4;
                }
                if (n9 != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    final void dumpState(int n2, boolean bl) {
        if (this.d.logLevel <= n2) {
            this.b(n2);
            this.a(n2, this.d.searchState.stackGroups[0]);
        }
    }

    private int scoreDealProgress(int completedSuitMask) {
        int progressPenalty = this.a(
            this.activeDealProgressBaseScores[this.d.searchState.progressIndex - 1],
            this.dealProgressCaps[this.d.searchState.progressIndex - 1],
            this.matchingAttemptCount - this.crossSuitAttemptCount / 2,
            this.dealProgressJoinWeights[this.d.searchState.progressIndex - 1]
        );
        if (this.d.searchState.progressIndex < 5) {
            if (completedSuitMask > 0) {
                if (this.d.logLevel < 3) {
                    this.d.log("Completed a suit");
                }
                int suitCompletionAdjustment = this.activeSuitCompletionAdjustments[this.suitCount];
                progressPenalty += this.a(suitCompletionAdjustment, 0, 0, 0.0);
                ++this.suitCompletionAdjustmentCount;
            }
            for (CardStack stack : this.d.searchState.stackGroups[0].stacks) {
                int joinPenalty = this.a(this.joinPenaltyBySuitMode[this.suitCount], 0, 0, 0.0);
                if (stack.runs.size() == 1) {
                    progressPenalty += joinPenalty;
                    ++this.matchingAttemptCount;
                } else if (stack.topRun != null && stack.topRun.cardCount > 1) {
                    progressPenalty += joinPenalty;
                    ++this.matchingAttemptCount;
                }
            }
        }
        return progressPenalty;
    }

    private void tryDealMove(int lastMove, int depthLimit) {
        boolean canDealImmediately = true;
        CardStack[] tableauStacks = this.d.searchState.stackGroups[0].stacks;
        for (CardStack stack : tableauStacks) {
            if (stack.topRun != null) {
                continue;
            }
            canDealImmediately = false;
            boolean foundMultiRunSource = false;
            for (CardStack candidateStack : tableauStacks) {
                if (candidateStack != stack && candidateStack.runs != null && candidateStack.runs.size() > 1) {
                    this.tryJoinMoves(11, "tospacefordeal", lastMove, depthLimit);
                    foundMultiRunSource = true;
                    break;
                }
            }
            if (!foundMultiRunSource && this.d.searchState.dealIndex < this.v) {
                this.tryJoinMoves(10, "tospacesingle", lastMove, depthLimit);
            }
        }
        if (!canDealImmediately) {
            return;
        }
        int dealResult;
        if (this.d.searchState.dealIndex < this.v) {
            if (this.v < 5) {
                ++this.v;
                if (this.d.logLevel <= 4) {
                    this.d.log("In deal() incremented dealMaxIndex to " + this.v);
                }
            }
            dealResult = this.a(this.d.searchState.stackGroups[0], this.d.searchState.stackGroups[1].stacks[0], this.d.searchState.stackGroups[2]);
            ++this.d.searchState.dealIndex;
            ++this.d.searchState.progressIndex;
            if (this.d.logLevel < 3) {
                this.d.log("Done a deal, dealIndex now " + this.d.searchState.dealIndex + " progress " + this.d.searchState.progressIndex);
            }
        } else if (this.d.searchState.dealIndex >= 5 && this.d.searchState.progressIndex < this.maxDealRows) {
            ++this.d.searchState.progressIndex;
            dealResult = 0;
            if (this.d.logLevel < 3) {
                this.d.log("Progress pseudo-deal, index now " + this.d.searchState.progressIndex);
            }
        } else {
            if (this.d.logLevel < 3) {
                this.d.log("No more deals to do right now");
            }
            dealResult = -1;
        }
        if (dealResult < 0) {
            return;
        }
        int savedSuitCompletionCount = this.suitCompletionAdjustmentCount;
        int savedMatchingAttemptCount = this.matchingAttemptCount;
        int savedRunningScore = this.runningScore;
        if (dealResult > 0) {
            int completedSuitCount = 0;
            for (int stackIndex = 0; stackIndex < 10; ++stackIndex) {
                if ((dealResult & 1 << stackIndex) != 0) {
                    ++completedSuitCount;
                }
            }
            this.runningScore += completedSuitCount * 101;
        }
        int encodedMove = this.d.searchState.progressIndex <= 5 ? Move.a(8, 0, dealResult >> 8, dealResult & 0xFF) : Move.a(4, 0, 0, 0);
        int dealPenalty = this.scoreDealProgress(dealResult);
        this.d.complexity += dealPenalty;
        int weightedJoinCount = this.matchingAttemptCount - this.crossSuitAttemptCount / 2;
        int complexityLimit = (int)((double)this.j[this.d.searchState.progressIndex - 1] + (double)weightedJoinCount * this.k[this.d.searchState.progressIndex - 1]);
        if (complexityLimit < this.l) {
            complexityLimit = this.l;
        }
        if (this.d.complexity <= -complexityLimit) {
            this.dealPenaltyStats[this.d.searchState.progressIndex - 1][0] = this.d.complexity;
            this.dealPenaltyStats[this.d.searchState.progressIndex - 1][2] = dealPenalty;
            this.dealPenaltyStats[this.d.searchState.progressIndex - 1][1] = -complexityLimit;
            this.dealPenaltyStats[this.d.searchState.progressIndex - 1][3] = this.matchingAttemptCount;
            this.dealPenaltyStats[this.d.searchState.progressIndex - 1][4] = this.fromSpaceAttemptCount;
            this.dealPenaltyStats[this.d.searchState.progressIndex - 1][5] = this.toSpaceAttemptCount;
            this.dealPenaltyStats[this.d.searchState.progressIndex - 1][6] = this.kingToSpaceAttemptCount;
            this.dealPenaltyStats[this.d.searchState.progressIndex - 1][7] = this.crossSuitAttemptCount;
            this.dealPenaltyStats[this.d.searchState.progressIndex - 1][8] = this.shiftAttemptCount;
            this.dealPenaltyStats[this.d.searchState.progressIndex - 1][9] = this.suitCompletionAdjustmentCount;
            this.d.searchState.moves[this.d.searchState.depth] = encodedMove;
            ++this.d.searchState.depth;
            if (this.d.logLevel <= 2) {
                this.d.log("Entering new progress/deal " + this.d.searchState.dealIndex + "/" + this.d.searchState.progressIndex + " at depth " + this.d.searchState.depth);
            }
            ++this.dealAttemptCount;
            boolean[] originalBlockedFlags = null;
            boolean readSucceeded = true;
            if (dealResult > 0) {
                originalBlockedFlags = new boolean[this.d.searchState.stackGroups[0].stacks.length];
                for (int stackIndex = 0; stackIndex < originalBlockedFlags.length; ++stackIndex) {
                    CardStack stack = this.d.searchState.stackGroups[0].stacks[stackIndex];
                    if (stack.topRun != null) {
                        originalBlockedFlags[stackIndex] = stack.topRun.faceDown;
                        if (stack.topRun.cardCount == 1 && stack.topRun.cards[0].cardId == 0) {
                            stack.topRun.faceDown = false;
                            readSucceeded = this.a(stack, encodedMove);
                        }
                    }
                }
            }
            if (this.runningScore > 0) {
                --this.runningScore;
            }
            if (readSucceeded) {
                this.searchInternal(-1, depthLimit, false);
            }
            if (originalBlockedFlags != null) {
                for (int stackIndex = 0; stackIndex < this.d.searchState.stackGroups[0].stacks.length; ++stackIndex) {
                    CardStack stack = this.d.searchState.stackGroups[0].stacks[stackIndex];
                    if (stack.topRun != null) {
                stack.topRun.faceDown = originalBlockedFlags[stackIndex];
                    }
                }
            }
            --this.dealAttemptCount;
            if (this.d.logLevel <= 2) {
                this.d.log("Exiting progress/deal " + this.d.searchState.dealIndex + "/" + this.d.searchState.progressIndex + " from depth " + this.d.searchState.depth);
            }
            --this.d.searchState.depth;
        }
        if (this.d.searchState.progressIndex > 5) {
            --this.d.searchState.progressIndex;
        } else {
            --this.d.searchState.progressIndex;
            --this.d.searchState.dealIndex;
            this.a(encodedMove, this.d.searchState.stackGroups[0], this.d.searchState.stackGroups[1].stacks[0], this.d.searchState.stackGroups[2]);
        }
        this.b(2, this.d.searchState.stackGroups[0]);
        this.d.complexity -= dealPenalty;
        this.suitCompletionAdjustmentCount = savedSuitCompletionCount;
        this.matchingAttemptCount = savedMatchingAttemptCount;
        this.runningScore = savedRunningScore;
    }

    final int a(StackGroup ot_02, CardStack os_02, StackGroup ot_03) {
        int n2 = 0;
        int n3 = 0;
        while (n3 < ot_02.stacks.length) {
            CardRun ok_02 = os_02.popTopRun();
            CardStack os_03 = ot_02.stacks[n3];
            if (os_03.topRun == null) {
                this.d.fail("Spider dealing to empty stack??");
                os_03.a(ok_02);
            } else {
                CardStack os_04 = os_03;
                int n4 = os_04.a(os_04.topRun, ok_02, false, false);
                if (n4 < 0) {
                    os_03.a(ok_02);
                    ok_02.faceDown = false;
                } else {
                    os_03.topRun.a(ok_02, n4);
                    if (n4 > 0 && os_03.topRun.cardCount == 13) {
                        if (this.d.logLevel <= 3) {
                            this.d.log("Deal completed a suit!!");
                        }
                        ok_02 = os_03.popTopRun();
                        ot_03.addCompletedSuitRun(ok_02);
                        os_03.a(false);
                        int n5 = os_03.stackIndex;
                        n2 |= suitCompletionMasks[n5];
                    }
                }
            }
            ++n3;
        }
        return n2;
    }

    final void a(int n2, StackGroup ot_02, CardStack os_02, StackGroup ot_03) {
        int n3 = ot_02.stacks.length - 1;
        while (n3 >= 0) {
            Object object;
            int n4 = n2;
            int n5 = ot_02.stacks[n3].stackIndex;
            n4 = (n4 & suitCompletionMasks[n5]) != 0 ? 1 : 0;
            if (n4 != 0) {
                CardRun ok_02 = ot_03.removeCompletedSuitRun();
                ot_02.stacks[n3].a(ok_02);
            }
            if (ot_02.stacks[n3].topRun.cardCount > 1) {
                object = ot_02.stacks[n3].topRun.cards[ot_02.stacks[n3].topRun.cardCount - 1];
                --ot_02.stacks[n3].topRun.cardCount;
                object = new CardRun((Card)object);
            } else {
                object = (CardRun)ot_02.stacks[n3].runs.getLast();
                ot_02.stacks[n3].b((CardRun)object);
            }
            ((CardRun)object).faceDown = true;
            os_02.a((CardRun)object);
            --n3;
        }
    }

    @Override
    final StringBuffer createStateHeader(String string, int n2) {
        return new StringBuffer(String.format("%s[%03d:%02d,%02d,%02d,%02d,%02d,%02d,%02d,%02d]: ", string, n2, this.matchingAttemptCount, this.dealAttemptCount, this.fromSpaceAttemptCount, this.toSpaceAttemptCount, this.crossSuitAttemptCount, this.shiftAttemptCount, this.kingToSpaceAttemptCount, this.suitCompletionAdjustmentCount));
    }

    @Override
    final boolean loadStateFromLines(String string, String[] stringArray, int n2) {
        CardRun ok_02;
        Object object;
        int n3;
        int n4;
        StackGroup ot_02 = this.d.initialState.stackGroups[0];
        this.d.initialState.dealIndex = 0;
        int[] nArray = new int[]{6, 6, 6, 6, 5, 5, 5, 5, 5, 5};
        int[] nArray2 = new int[]{5, 5, 5, 5, 4, 4, 4, 4, 4, 4};
        int n5 = 6;
        boolean bl = false;
        if (string != null && !string.equals("60")) {
            bl = true;
            n5 = 0;
            String[] stringArray3 = string.split(":");
            try {
                int n6 = Integer.parseInt(stringArray3[0]);
                this.d.initialState.progressIndex = this.d.initialState.dealIndex = (50 - n6) / 10;
                n6 = 0;
                while (n6 < 10) {
                    n4 = Integer.parseInt(stringArray3[n6 + 1]);
                    int n7 = n4 % 100;
                    n3 = n4 / 100;
                    nArray[n6] = n7;
                    nArray2[n6] = n3;
                    if (n7 > n5) {
                        n5 = n7;
                    }
                    ++n6;
                }
            }
            catch (NumberFormatException numberFormatException) {
                this.d.fail("Error parsing Spider options for partially completed deck");
            }
        }
        int n8 = 0;
        while (n8 < n5) {
            String[] stringArray3 = stringArray[n8 + 1].split(",");
            n4 = 0;
            while (n4 < stringArray3.length) {
                if (nArray[n4] > n8) {
                    int n9;
                    CardStack os_02 = ot_02.stacks[n4];
                    object = stringArray3[n4];
                    this.g[n8][n4] = n9 = this.d.parseCardCode((String)object);
                    if (this.d.logLevel <= 2) {
                        this.d.log("Loading card " + n9 + " into stack " + n4 + " level " + n8);
                    }
                    if (nArray2[n4] > n8) {
                        ok_02 = new CardRun(this.b(os_02, n9));
                        os_02.a(ok_02);
                        ok_02.faceDown = true;
                    } else {
                        ok_02 = os_02.topRun;
                        CardRun ok_03 = new CardRun(this.b(os_02, n9));
                        if (ok_02 != null && !ok_02.faceDown) {
                            int n10 = os_02.a(ok_02, ok_03, false, false);
                            if (n10 > 0) {
                                ok_02.a(ok_03, n10);
                            } else {
                                os_02.a(ok_03);
                            }
                        } else {
                            os_02.a(ok_03);
                        }
                    }
                }
                ++n4;
            }
            ++n8;
        }
        String[] stringArray4 = stringArray[n5 + 1].split(",");
        try {
            int n11 = this.d.initialState.dealIndex;
            while (n11 < 5) {
                n4 = 0;
                while (n4 < 10) {
                    String string2 = stringArray4[n11 * 10 + n4];
                    this.y[n11][n4] = n3 = this.d.parseCardCode(string2);
                    object = this.d.initialState.stackGroups[1].stacks[0];
                    ok_02 = new CardRun(this.b((CardStack)object, n3));
                        ok_02.faceDown = true;
                    ((CardStack)object).c(ok_02);
                    if (this.d.logLevel <= 2) {
                        this.d.log("Loading card " + n3 + " into deck level " + n11 + " stack " + n4);
                    }
                    ++n4;
                }
                ++n11;
            }
        }
        catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
            this.d.fail("Insufficient cards in the deal pile");
        }
        catch (Exception exception) {
            this.d.fail("Problem reading the cards in the deal pile: " + exception);
        }
        if (bl) {
            stringArray4 = stringArray[n5 + 2].split(",");
            int n12 = 0;
            while (n12 < 8) {
                String string3 = stringArray4[n12];
                int n13 = this.d.parseCardCode(string3);
                if (n13 > 0) {
                    this.completedSuitCards[n12] = n13;
                    this.addCompletedSuitRunToInitialState(n13 / 100);
                    if (this.d.logLevel <= 2) {
                        this.d.log("Loaded suit card " + n13);
                    }
                }
                ++n12;
            }
        }
        this.a((CardStack)null);
        if (this.d.solverMode == 1) {
            int n14 = 0;
            while (n14 < 104) {
                if (this.z[n14].cardId <= 0) {
                    this.d.log("For ALLCARDS the input file must have all 104 cards");
                    return false;
                }
                ++n14;
            }
        }
        this.d.bridge.g();
        return true;
    }

    @Override
    final void appendBoardState(StringBuffer stringBuffer) {
        int n2;
        stringBuffer.append(SolverContext.VARIANT_NAMES[this.d.variantId]);
        int n3 = 0;
        boolean bl = false;
        int n4 = 0;
        while (n4 < 4) {
            if (this.h[n4] > n3) {
                n3 = this.h[n4];
            }
            if (this.h[n4] != 6) {
                bl = true;
            }
            ++n4;
        }
        n4 = 4;
        while (n4 < 10) {
            if (this.h[n4] > n3) {
                n3 = this.h[n4];
            }
            if (this.h[n4] != 5) {
                bl = true;
            }
            ++n4;
        }
        if (bl || this.dealAreaCardCount != 50) {
            bl = true;
            stringBuffer.append(",");
            stringBuffer.append(this.dealAreaCardCount);
            n4 = 0;
            while (n4 < 10) {
                stringBuffer.append(":" + (this.i[n4] * 100 + this.h[n4]));
                ++n4;
            }
        }
        stringBuffer.append("\n# Stacks:\n");
        n4 = 0;
        while (n4 < n3) {
            int n5;
            n2 = this.f;
            if (n4 == n3 - 1) {
                n5 = this.f - 1;
                while (n5 >= 0) {
                    if (this.g[n4][n5] != 0) {
                        n2 = n5 + 1;
                        break;
                    }
                    --n5;
                }
            }
            n5 = 0;
            while (n5 < n2) {
                stringBuffer.append(SpiderSolver.i(this.g[n4][n5]));
                stringBuffer.append(",");
                ++n5;
            }
            stringBuffer.append("\n");
            ++n4;
        }
        stringBuffer.append("# Deck:\n");
        n4 = 0;
        while (n4 < 5) {
            n2 = 0;
            while (n2 < this.f) {
                stringBuffer.append(SpiderSolver.i(this.y[n4][n2]));
                stringBuffer.append(",");
                ++n2;
            }
            ++n4;
        }
        if (bl) {
            stringBuffer.append("\n# Suits:\n");
            n4 = 0;
            while (n4 < 8) {
                stringBuffer.append(SpiderSolver.i(this.completedSuitCards[n4]));
                stringBuffer.append(",");
                ++n4;
            }
        }
    }

    @Override
    final void a(int n2) {
        if (this.d.logLevel <= 4) {
            n2 = 0;
            while (n2 < this.d.searchState.progressIndex) {
                this.d.log(String.format("Deal%1d penalty stats %4d/%4d(%3d) %3d,%3d,%3d,%3d,%3d,%3d,%3d", n2, this.dealPenaltyStats[n2][0], this.dealPenaltyStats[n2][1], this.dealPenaltyStats[n2][2], this.dealPenaltyStats[n2][3], this.dealPenaltyStats[n2][4], this.dealPenaltyStats[n2][5], this.dealPenaltyStats[n2][6], this.dealPenaltyStats[n2][7], this.dealPenaltyStats[n2][8], this.dealPenaltyStats[n2][9]));
                ++n2;
            }
            this.d.log(String.format("Tip   penalty stats %4d           %3d,%3d,%3d,%3d,%3d,%3d,%3d", this.d.complexity, this.matchingAttemptCount, this.fromSpaceAttemptCount, this.toSpaceAttemptCount, this.kingToSpaceAttemptCount, this.crossSuitAttemptCount, this.shiftAttemptCount, this.suitCompletionAdjustmentCount));
        }
    }

    @Override
    final boolean a(GameState nY2, int n2) {
        if (nY2 == null) {
            return false;
        }
        if (nY2.stackGroups[0] == null) {
            return false;
        }
        CardStack[] os_0Array = nY2.stackGroups[0].stacks;
        int n3 = os_0Array.length;
        n2 = 0;
        while (n2 < n3) {
            CardStack os_02 = os_0Array[n2];
            if (os_02.topRun != null) {
                return false;
            }
            ++n2;
        }
        return true;
    }

    @Override
    final int b(GameState nY2, boolean bl) {
        if (bl) {
            return 0;
        }
        if (nY2 == null) {
            return 0;
        }
        if (nY2.stackGroups[2] == null) {
            return 0;
        }
        int n2 = 0;
        CardStack[] os_0Array = nY2.stackGroups[2].stacks;
        int n3 = os_0Array.length;
        int n4 = 0;
        while (n4 < n3) {
            CardStack os_02 = os_0Array[n4];
            if (os_02.topRun == null) break;
            ++n2;
            ++n4;
        }
        return n2;
    }

    @Override
    final int a(GameState nY2, boolean bl) {
        if (bl) {
            return 0;
        }
        return this.runningScore;
    }

    @Override
    final int a(GameState nY2) {
        int n2;
        int n3;
        if (nY2 == null) {
            return 999;
        }
        if (nY2.stackGroups[0] == null) {
            return 999;
        }
        int n4 = 0;
        while (n4 < nY2.stackGroups[2].stacks.length) {
            if (nY2.stackGroups[2].stacks[n4].topRun == null) break;
            ++n4;
        }
        if (this.d.files.b == 1 || this.d.files.b == 2) {
            int n5 = 8 - n4;
            int n6 = this.suitCount << 1;
            if (n6 < 8) {
                n6 += 2;
            }
            n4 = (5 - this.d.searchState.dealIndex) * n6;
            CardStack[] os_0Array = nY2.stackGroups[0].stacks;
            int n7 = nY2.stackGroups[0].stacks.length;
            int n8 = 0;
            while (n8 < n7) {
                CardStack os_02 = os_0Array[n8];
                n4 += os_02.runs.size();
                ++n8;
            }
            return nY2.depth + (n4 -= n5);
        }
        if (this.d.files.b == 5) {
            n3 = this.d.files.i - n4;
        } else {
            n2 = (this.d.files.g - this.d.files.f + 99) / 100 + 1;
            n3 = n2 - n4;
        }
        n2 = 0;
        CardStack[] os_0Array = nY2.stackGroups[0].stacks;
        int n9 = nY2.stackGroups[0].stacks.length;
        int n10 = 0;
        while (n10 < n9) {
            CardStack os_03 = os_0Array[n10];
            if (os_03.topRun != null && os_03.topRun.cards[0].rank == 13) {
                ++n2;
                --n3;
            }
            if (n3 == 0) break;
            ++n10;
        }
        int n11 = 0;
        while (n11 < n3) {
            n2 += 2;
            ++n11;
        }
        return nY2.depth + n2;
    }

    @Override
    final int a(CardStack sourceStack, CardStack targetStack, int requestedCards) {
        int move = this.buildUserMoveByMode(sourceStack, targetStack, requestedCards, 3);
        if (move <= 0) {
            move = this.buildUserMoveByMode(sourceStack, targetStack, requestedCards, 0);
        }
        if (move <= 0) {
            move = this.buildUserMoveByMode(sourceStack, targetStack, requestedCards, 1);
        }
        return move;
    }

    private int buildUserMoveByMode(CardStack sourceStack, CardStack targetStack, int requestedCards, int moveMode) {
        int moveFlags;
        int moveCardCount;
        int encodedMove = -1;
        int sourceRunLength = sourceStack.topRun.cardCount;
        int joinRule = 1;
        switch (moveMode) {
            case 3: 
            case 6: {
                joinRule = 1;
                break;
            }
            case 0: 
            case 4: {
                joinRule = 4;
                break;
            }
            case 1: 
            case 5: {
                joinRule = 2;
            }
        }
        int joinSize = targetStack.a(sourceStack, joinRule, true);
        if (moveMode == 4 && joinSize > 0) {
            joinSize = -1;
        } else if (moveMode == 5 && joinSize != sourceStack.topRun.cardCount) {
            joinSize = -1;
        }
        int effectiveMoveCount = moveCardCount = joinSize == 0 ? sourceRunLength : joinSize;
        if (joinSize >= 0 && moveMode == 6 && (moveFlags = moveCardCount + targetStack.topRun.cardCount) < 13) {
            joinSize = -1;
        }
        if (joinSize >= 0) {
            if (requestedCards > 0 && requestedCards != moveCardCount) {
                if (moveMode == 1) {
                    moveCardCount = requestedCards;
                } else {
                    return -1;
                }
            }
            if (this.d.logLevel <= 5) {
                this.d.log("User move will be permitted " + joinSize);
            }
            moveFlags = joinRule == 1 ? 2 : 0;
            moveFlags = moveFlags | (moveCardCount < sourceRunLength ? 1 : 0);
            if (targetStack.topRun != null && targetStack.topRun.cardCount + moveCardCount == 13 && targetStack.topRun.cards[0].suit == sourceStack.topRun.cards[0].suit) {
                moveFlags |= targetStack.topRun.cards[0].suit << 4;
            }
            encodedMove = Move.a(moveFlags, moveCardCount, sourceStack, targetStack);
            this.k(encodedMove);
        }
        return encodedMove;
    }

    @Override
    final int a(Card clickedCard) {
        CardStack sourceStack = clickedCard.stack;
        if (sourceStack.group.groupIndex == this.d.bridge.specialSourceGroupIndex) {
            if (this.d.logLevel <= 5) {
                this.d.log("Got double click on feed stack");
            }
            int dealMove = -1;
            CardStack[] tableauStacks = this.d.initialState.stackGroups[0].stacks;
            int stackCount = tableauStacks.length;
            int stackIndex = 0;
            while (stackIndex < stackCount) {
                if (tableauStacks[stackIndex].topRun == null) {
                    return -1;
                }
                ++stackIndex;
            }
            if (this.d.initialState.dealIndex < this.v) {
                int dealResult = this.a(this.d.initialState.stackGroups[0], this.d.initialState.stackGroups[1].stacks[0], this.d.initialState.stackGroups[2]);
                if (this.d.logLevel <= 4) {
                    this.d.log("Done a deal, dealIndex now " + (this.d.initialState.dealIndex + 1));
                }
                if (dealResult >= 0) {
                    dealMove = Move.a(8, 0, dealResult >> 8, dealResult & 0xFF);
                    this.a(dealMove, this.d.initialState.stackGroups[0], this.d.initialState.stackGroups[1].stacks[0], this.d.initialState.stackGroups[2]);
                    this.k(dealMove);
                }
            } else if (this.d.logLevel <= 4) {
                this.d.log("No more deals to do right now");
            }
            return dealMove;
        }
        return this.buildDoubleClickMove(sourceStack, clickedCard);
    }

    private int buildDoubleClickMove(CardStack sourceStack, Card clickedCard) {
        int encodedMove = -1;
        int requestedCards = -1;
        if (clickedCard != sourceStack.getTopCard()) {
            requestedCards = sourceStack.topRun.cardCount - clickedCard.runIndex;
        }
        CardStack[] tableauStacks = this.d.initialState.stackGroups[0].stacks;
        int stackCount = this.d.initialState.stackGroups[0].stacks.length;
        CardStack targetStack;
        for (int index = 0; index < stackCount && ((targetStack = tableauStacks[index]) == sourceStack || (encodedMove = this.buildUserMoveByMode(sourceStack, targetStack, requestedCards, 6)) <= 0); ++index) {
        }
        if (encodedMove <= 0) {
            tableauStacks = this.d.initialState.stackGroups[0].stacks;
            stackCount = this.d.initialState.stackGroups[0].stacks.length;
            CardStack kingSpaceTarget;
            for (int index = 0; index < stackCount && ((kingSpaceTarget = tableauStacks[index]) == sourceStack || (encodedMove = this.buildUserMoveByMode(sourceStack, kingSpaceTarget, requestedCards, 3)) <= 0); ++index) {
            }
        }
        if (encodedMove <= 0) {
            tableauStacks = this.d.initialState.stackGroups[0].stacks;
            stackCount = this.d.initialState.stackGroups[0].stacks.length;
            CardStack crossSuitTarget;
            for (int index = 0; index < stackCount && ((crossSuitTarget = tableauStacks[index]) == sourceStack || (encodedMove = this.buildUserMoveByMode(sourceStack, crossSuitTarget, requestedCards, 4)) <= 0); ++index) {
            }
        }
        if (encodedMove <= 0) {
            tableauStacks = this.d.initialState.stackGroups[0].stacks;
            stackCount = this.d.initialState.stackGroups[0].stacks.length;
            CardStack fullRunTarget;
            for (int index = 0; index < stackCount && ((fullRunTarget = tableauStacks[index]) == sourceStack || (encodedMove = this.buildUserMoveByMode(sourceStack, fullRunTarget, requestedCards, 5)) <= 0); ++index) {
            }
        }
        if (encodedMove <= 0) {
            tableauStacks = this.d.initialState.stackGroups[0].stacks;
            stackCount = this.d.initialState.stackGroups[0].stacks.length;
            CardStack relaxedTarget;
            for (int index = 0; index < stackCount && ((relaxedTarget = tableauStacks[index]) == sourceStack || (encodedMove = this.buildUserMoveByMode(sourceStack, relaxedTarget, requestedCards, 0)) <= 0); ++index) {
            }
        }
        if (encodedMove <= 0) {
            tableauStacks = this.d.initialState.stackGroups[0].stacks;
            stackCount = this.d.initialState.stackGroups[0].stacks.length;
            CardStack exactTarget;
            for (int index = 0; index < stackCount && ((exactTarget = tableauStacks[index]) == sourceStack || (encodedMove = this.buildUserMoveByMode(sourceStack, exactTarget, requestedCards, 1)) <= 0); ++index) {
            }
        }
        return encodedMove;
    }
}




