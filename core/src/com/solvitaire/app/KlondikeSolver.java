/*
 * Decompiled with CFR 0.152.
 */
package com.solvitaire.app;

import com.solvitaire.app.KlondikeBridge;
import com.solvitaire.app.Move;
import com.solvitaire.app.Card;
import com.solvitaire.app.GameState;
import com.solvitaire.app.CardRun;
import com.solvitaire.app.SolverContext;
import com.solvitaire.app.BaseSolver;
import com.solvitaire.app.CardStack;
import com.solvitaire.app.StackGroup;
import java.io.File;
import java.util.HashMap;

final class KlondikeSolver
extends BaseSolver {
    private boolean useHiddenRunAgePenalty;
    private boolean boardSequencedForFinish;
    private int pileToStackPenalty;
    private int stackToStackPenalty;
    private int pileToAcePenalty;
    private int stackToAceAttempts = 0;
    private int pileToStackAttempts = 0;
    private int stackToStackAttempts = 0;
    private int pileToAceAttempts = 0;
    private int splitRunAttempts = 0;
    private int aceToStackAttempts = 0;
    private int dealAttempts = 0;
    private int hiddenCardCount = 21;
    private long[][] hiddenRunAgeTotals;
    private long[][] hiddenRunAgeStartMoves;
    int[] foundationTopCards;
    private int runningScore;

    KlondikeSolver(SolverContext om_02) {
        super(om_02, 2000);
        this.f = 7;
        this.n = 1;
        this.o = 52;
        this.q = true;
    }

    @Override
    final String getSolverName() {
        return "Klondike";
    }

    @Override
    final boolean initializeSolver() {
        this.initializeBaseState();
        this.e = String.valueOf(this.d.workspaceRoot) + "klondike" + File.separator;
        this.g = new int[50][this.f];
        this.h = new int[this.f];
        this.i = new int[this.f];
        this.y = new int[24][1];
        this.foundationTopCards = new int[4];
        this.r = new int[this.f][10];
        if (this.d.logLevel <= 3) {
            this.d.log("Constructed stacks");
        }
        if (this.C) {
            if (this.d.logLevel <= 4) {
                this.d.log("Running Klondike");
            }
            this.d.bridge = new KlondikeBridge(this);
            if (this.d.logLevel <= 3) {
                this.d.log("Auto constructed");
            }
            this.d.bridge.d();
        }
        if (!this.d.bridge.e()) {
            return false;
        }
        this.d.searchState = new GameState(this.d.initialState, true);
        int n2 = 0;
        while (n2 < 7) {
            for (Object object : this.d.searchState.stackGroups[0].stacks[n2].runs) {
                CardRun ok_02 = (CardRun)object;
                if (ok_02 == this.d.searchState.stackGroups[0].stacks[n2].topRun) continue;
                ok_02.faceDown = true;
            }
            ++n2;
        }
        this.hiddenRunAgeTotals = new long[7][7];
        this.hiddenRunAgeStartMoves = new long[7][7];
        this.useHiddenRunAgePenalty = false;
        this.F = 0;
        this.G = 0;
        this.runningScore = this.d.files.l;
        this.boardSequencedForFinish = false;
        return true;
    }

    private double c(int n2, int n3) {
        double d2 = this.hiddenRunAgeTotals[n2][n3];
        if (this.hiddenRunAgeStartMoves[n2][n3] >= 0L) {
            d2 += (double)(this.d.searchStepCount - this.hiddenRunAgeStartMoves[n2][n3]);
        }
        return d2;
    }

    private int d(int n2, int n3) {
        double d2 = this.c(n2, n3);
        d2 = 1.0 / (1.1 - d2 / (double)this.d.searchStepCount) - 0.9090909090909091;
        n2 = (int)(d2 * -6.0);
        return n2;
    }

    @Override
    final void dumpState(int n2, boolean bl) {
        if (this.d.logLevel <= n2) {
            this.b(n2);
            if (bl) {
                this.a(n2, this.d.initialState.stackGroups[3]);
                this.a(n2, this.d.initialState.stackGroups[1]);
                this.a(n2, this.d.initialState.stackGroups[2]);
                this.a(n2, this.d.initialState.stackGroups[0]);
                return;
            }
            this.a(n2, this.d.searchState.stackGroups[3]);
            this.a(n2, this.d.searchState.stackGroups[1]);
            this.a(n2, this.d.searchState.stackGroups[2]);
            this.a(n2, this.d.searchState.stackGroups[0]);
            this.d.log(String.format("Hidden counts: %f  %f,%f  %f,%f,%f  %f,%f,%f,%f  %f,%f,%f,%f,%f  %f,%f,%f,%f,%f,%f", this.c(1, 0), this.c(2, 0), this.c(2, 1), this.c(3, 0), this.c(3, 1), this.c(3, 2), this.c(4, 0), this.c(4, 1), this.c(4, 2), this.c(4, 3), this.c(5, 0), this.c(5, 1), this.c(5, 2), this.c(5, 3), this.c(5, 4), this.c(6, 0), this.c(6, 1), this.c(6, 2), this.c(6, 3), this.c(6, 4), this.c(6, 5)));
            this.d.log(String.format("Hidden factors: %d  %d,%d  %d,%d,%d  %d,%d,%d,%d  %d,%d,%d,%d,%d  %d,%d,%d,%d,%d,%d", this.d(1, 0), this.d(2, 0), this.d(2, 1), this.d(3, 0), this.d(3, 1), this.d(3, 2), this.d(4, 0), this.d(4, 1), this.d(4, 2), this.d(4, 3), this.d(5, 0), this.d(5, 1), this.d(5, 2), this.d(5, 3), this.d(5, 4), this.d(6, 0), this.d(6, 1), this.d(6, 2), this.d(6, 3), this.d(6, 4), this.d(6, 5)));
        }
    }

    @Override
    final void search(int n2, int n3) {
        if (this.d.files.b == 6) {
            this.pileToStackPenalty = -1;
            this.stackToStackPenalty = -3;
            this.pileToAcePenalty = 0;
        } else {
            this.pileToStackPenalty = 0;
            this.stackToStackPenalty = 0;
            this.pileToAcePenalty = -3;
        }
        n3 = this.d.searchState.stackGroups[1].stacks[0].runs.size();
        if (this.d.searchStepCount > 100000L && !this.useHiddenRunAgePenalty) {
            this.useHiddenRunAgePenalty = true;
            this.d.searchCredit += 30;
            this.d.complexity = this.d.searchCredit;
        }
        if (this.d.searchState.stackGroups[2].stacks[0].topRun == null && this.d.searchState.stackGroups[1].stacks[0].topRun != null) {
            this.a(n2, n3, false, false);
            return;
        }
        this.a(n2, n3, false, false, false);
    }

    private boolean e(int n2, int n3) {
        this.d.complexity = n2 + n3;
        return this.d.complexity <= 0;
    }

    private boolean c(Card nT2) {
        boolean bl;
        if (nT2.rank < 3) {
            bl = true;
        } else {
            bl = true;
            int n2 = 0;
            while (n2 < 4) {
                Card nT3 = this.d.searchState.stackGroups[3].stacks[n2].getTopCard();
                if (nT3 == null || nT3.suit != nT2.suit && nT3.rank < nT2.rank) {
                    bl = false;
                    break;
                }
                ++n2;
            }
        }
        return bl;
    }

    private void a(int n2, int n3, boolean bl, boolean bl2, boolean bl3) {
        Object object;
        int n4;
        Object object2;
        int n5;
        int n6 = this.d.complexity;
        this.l();
        if (this.d.searchState.depth > this.maxSearchDepth) {
            return;
        }
        if (this.d.searchStepCount++ % 5000000L == 0L) {
            this.b(4);
            this.dumpState(4, false);
        }
        if (!this.B && this.D < 0) {
            KlondikeSolver nB2 = this;
            n5 = n2;
            object2 = nB2.d.searchState;
            KlondikeSolver nB3 = nB2;
            n4 = nB2.a((GameState)object2, n5, false);
            if (n4 != 2 && this.d.searchState.depth > 0 && this.d.searchState.moves[this.d.searchState.depth - 1] == Move.a(24, 0, 0, 0)) {
                --this.d.searchState.depth;
            }
            if (n4 == 2 || n4 == 1) {
                return;
            }
        }
        if (!this.B && this.D < 0 && !bl3 && this.e(n6, this.stackToStackPenalty)) {
            this.f(n2, n3);
        }
        if (!this.B && this.D < 0 && this.e(n6, this.pileToStackPenalty)) {
            this.g(n2, n3);
        }
        if (!this.B && this.D < 0 && !bl3 && this.e(n6, -3)) {
            n4 = 0;
            while (n4 < 7) {
                object2 = this.d.searchState.stackGroups[0].stacks[n4];
                boolean bl4 = this.a(n2, (CardStack)object2, n3);
                n5 = bl4 ? 1 : 0;
                if (bl4) {
                    object = ((CardStack)object2).topRun.cards[0];
                    if (this.d.table.drawCount == 1 && this.c((Card)object)) {
                        return;
                    }
                }
                ++n4;
            }
        }
        if (!this.B && this.D < 0 && this.e(n6, this.pileToAcePenalty)) {
            boolean bl5 = this.h(n2, n3);
            n4 = bl5 ? 1 : 0;
            if (bl5 && (object2 = this.d.searchState.stackGroups[2].stacks[0].getTopCard()) != null && this.d.table.drawCount == 1 && this.c((Card)object2)) {
                return;
            }
        }
        if (!this.B && this.D < 0 && !bl3 && this.e(n6, 0)) {
            ++this.splitRunAttempts;
            if (this.d.logLevel <= 2) {
                this.d.log("Try split run");
            }
            n4 = 0;
            while (n4 < 7) {
                object2 = this.d.searchState.stackGroups[0].stacks[n4];
                n5 = 0;
                while (n5 < 7) {
                    object = this.d.searchState.stackGroups[0].stacks[n5];
                    this.a((CardStack)object, (CardStack)object2, 5, n2, n3);
                    ++n5;
                }
                ++n4;
            }
            if (this.d.logLevel <= 2) {
                this.d.log("After split run");
            }
            --this.splitRunAttempts;
        }
        if (!this.B && this.D < 0 && !bl2) {
            this.d.complexity = n6;
            if (bl) {
                this.e(n6, 2);
                this.a(n2, n3, true, true);
            } else {
                int cfr_ignored_0 = this.d.table.drawCount;
                if (this.e(n6, 15)) {
                    this.a(n2, n3, true, true);
                }
            }
        }
        if (!this.B && this.D < 0 && !bl3) {
            this.d.complexity = n6 + 3;
            if (this.d.complexity < 0) {
                ++this.aceToStackAttempts;
                if (this.d.logLevel <= 2) {
                    this.d.log("Try ace to stack");
                }
                n4 = 0;
                while (n4 < 4) {
                    object2 = this.d.searchState.stackGroups[3].stacks[n4];
                    n5 = 0;
                    while (n5 < 7) {
                        object = this.d.searchState.stackGroups[0].stacks[n5];
                        this.runningScore -= 10;
                        this.a((CardStack)object, (CardStack)object2, 6, n2, n3);
                        this.runningScore += 10;
                        ++n5;
                    }
                    ++n4;
                }
                if (this.d.logLevel <= 2) {
                    this.d.log("After ace to stack");
                }
                --this.aceToStackAttempts;
            }
        }
        this.d.complexity = n6;
    }

    @Override
    final int a(GameState nY2) {
        int n2;
        this.boardSequencedForFinish = true;
        int n3 = 0;
        if (nY2.stackGroups[1].stacks[0] == null || nY2.stackGroups[2].stacks[0] == null) {
            this.boardSequencedForFinish = false;
            return nY2.depth + 1;
        }
        if (nY2.stackGroups[1].stacks[0].topRun != null || nY2.stackGroups[2].stacks[0].topRun != null) {
            this.boardSequencedForFinish = false;
            return nY2.depth + 1;
        }
        CardStack[] os_0Array = nY2.stackGroups[0].stacks;
        int n4 = nY2.stackGroups[0].stacks.length;
        int n5 = 0;
        while (n5 < n4) {
            CardStack os_02 = os_0Array[n5];
            CardRun ok_02 = os_02.topRun;
            if (ok_02 != null) {
                if (os_02.runs.size() == 1) {
                    n3 += ok_02.cardCount;
                } else {
                    this.boardSequencedForFinish = false;
                    return nY2.depth + 1;
                }
            }
            ++n5;
        }
        if (this.d.files.maxMoves == 999) {
            if (this.d.logLevel <= 3) {
                this.d.log("No max moves so return solve depth of " + (nY2.depth + 1));
            }
            return nY2.depth + 1;
        }
        if (this.d.files.b == 1 || this.d.files.b == 2) {
            n2 = nY2.depth + n3;
        } else {
            n2 = nY2.depth + n3;
            if (n2 > this.d.files.maxMoves) {
                this.boardSequencedForFinish = false;
                n2 = nY2.depth + 1;
            }
        }
        return n2;
    }

    private int b(GameState nY2) {
        int n2 = this.a(nY2);
        return this.b(nY2, n2, false);
    }

    private int b(GameState nY2, int n2, boolean bl) {
        if (!this.boardSequencedForFinish) {
            return -1;
        }
        if (n2 > this.d.files.maxMoves) {
            this.boardSequencedForFinish = false;
            return -1;
        }
        nY2.solutionLength = n2;
        n2 -= nY2.depth;
        if (bl) {
            nY2.moves[nY2.depth] = Move.a(24, 0, 0, 0);
            ++nY2.depth;
        }
        return n2;
    }

    private void f(int n2, int n3) {
        ++this.stackToStackAttempts;
        if (this.d.logLevel <= 2) {
            this.d.log("Try stack to stack");
        }
        int n4 = 0;
        while (n4 < 7) {
            block11: {
                CardStack os_02;
                block12: {
                    os_02 = this.d.searchState.stackGroups[0].stacks[n4];
                    if (os_02.topRun == null) break block11;
                    if (os_02.topRun.cardCount != 1) break block12;
                    Card nT2 = os_02.topRun.cards[0];
                    if (nT2.cardId == 0) {
                        int n5 = this.e(os_02);
                        if (n5 > 0) {
                            os_02.topRun.cards[0].a(n5);
                        } else if (this.d.t && this.d.solverMode == 1) {
                            this.d.log("Skipping because not all cards known");
                            this.B = true;
                            return;
                        }
                    }
                    if (nT2.rank == 1) break block11;
                }
                int n6 = 0;
                while (n6 < 7) {
                    CardStack os_03 = this.d.searchState.stackGroups[0].stacks[n6];
                    this.a(os_03, os_02, 3, n2, n3);
                    ++n6;
                }
            }
            ++n4;
        }
        if (this.d.logLevel <= 2) {
            this.d.log("After stack to stack");
        }
        --this.stackToStackAttempts;
    }

    private void g(int n2, int n3) {
        if (this.d.searchState.stackGroups[2].stacks[0].topRun == null) {
            return;
        }
        ++this.pileToStackAttempts;
        if (this.d.logLevel <= 2) {
            this.d.log("Try pile to stack");
        }
        CardStack os_02 = this.d.searchState.stackGroups[2].stacks[0];
        int n4 = 0;
        while (n4 < 7) {
            CardStack os_03 = this.d.searchState.stackGroups[0].stacks[n4];
            this.runningScore += 5;
            this.a(os_03, os_02, 2, n2, n3);
            this.runningScore -= 5;
            ++n4;
        }
        if (this.d.logLevel <= 2) {
            this.d.log("After pile to stack");
        }
        --this.pileToStackAttempts;
    }

    private boolean h(int n2, int n3) {
        boolean bl = false;
        if (this.d.searchState.stackGroups[2].stacks[0].topRun == null) {
            return false;
        }
        ++this.pileToAceAttempts;
        if (this.d.logLevel <= 2) {
            this.d.log("Try pile to ace");
        }
        int n4 = 0;
        while (n4 < 4) {
            CardStack os_02 = this.d.searchState.stackGroups[3].stacks[n4];
            this.runningScore += 10;
            bl = this.a(os_02, this.d.searchState.stackGroups[2].stacks[0], 4, n2, n3);
            this.runningScore -= 10;
            if (bl || os_02.topRun == null && this.d.searchState.stackGroups[2].stacks[0].getTopRank() == 1) break;
            ++n4;
        }
        if (this.d.logLevel <= 2) {
            this.d.log("After pile to ace");
        }
        --this.pileToAceAttempts;
        return bl;
    }

    private boolean a(int n2, CardStack os_02, int n3) {
        boolean bl = false;
        ++this.stackToAceAttempts;
        if (this.d.logLevel <= 2) {
            this.d.log("Try stack to aces for stack " + os_02.stackIndex);
        }
        int n4 = 0;
        while (n4 < 4) {
            CardStack os_03 = this.d.searchState.stackGroups[3].stacks[n4];
            this.runningScore += 10;
            bl = this.a(os_03, os_02, 1, n2, n3);
            this.runningScore -= 10;
            if (bl || os_03.topRun == null && os_02.getTopRank() == 1) break;
            ++n4;
        }
        if (this.d.logLevel <= 2) {
            this.d.log("After stack to aces");
        }
        --this.stackToAceAttempts;
        return bl;
    }

    private void a(int n2, int n3, boolean bl, boolean bl2) {
        ++this.dealAttempts;
        int n4 = this.a(n3, this.d.searchState.stackGroups[1].stacks[0], this.d.searchState.stackGroups[2].stacks[0], true);
        if (n4 > 0) {
            int n5 = n4;
            if ((n5 >> 16 & 0xFF) != 0) {
                n3 |= 0x80;
            }
            this.d.searchState.moves[this.d.searchState.depth] = n4;
            ++this.d.searchState.depth;
            long l2 = 0L;
            if (this.d.table.drawCount > 1) {
                l2 = this.computeStateHash();
                this.D = this.b(l2);
            }
            if (this.D < 0) {
                if (this.d.table.drawCount > 1) {
                    this.a(l2);
                }
                if (this.d.logLevel <= 3) {
                    this.d.log("Recursing after deal: " + Move.a(n4) + " index " + this.d.searchState.dealIndex);
                }
                this.a(n2, n3, bl, false, bl2);
                if (this.d.logLevel <= 3) {
                    this.d.log("Returned after trying deal " + Move.a(n4) + " with index " + this.d.searchState.dealIndex);
                }
            }
            if (this.D >= 0) {
                --this.D;
            }
            --this.d.searchState.depth;
            this.b(n4, this.d.searchState.stackGroups[1].stacks[0], this.d.searchState.stackGroups[2].stacks[0], true);
        }
        --this.dealAttempts;
    }

    @Override
    final long computeStateHash() {
        long l2 = 0L;
        this.m = 0;
        int n2 = 0;
        while (n2 < 4) {
            KlondikeSolver nB2 = this;
            l2 += nB2.a(nB2.d.searchState.stackGroups[3].stacks[n2], l2, false, false);
            ++n2;
        }
        if (this.d.table.drawCount > 1) {
            KlondikeSolver nB3 = this;
            long l3 = nB3.a(nB3.d.searchState.stackGroups[2].stacks[0], l2, false, false);
            KlondikeSolver nB4 = this;
            long l4 = nB4.a(nB4.d.searchState.stackGroups[1].stacks[0], l2 += l3, false, false);
            l2 += l4;
        }
        n2 = 0;
        while (n2 < this.f) {
            KlondikeSolver nB5 = this;
            l2 += nB5.a(nB5.d.searchState.stackGroups[0].stacks[n2], l2, true, false);
            ++n2;
        }
        return l2;
    }

    private boolean a(CardStack os_02, CardStack os_03, int n2, int n3, int n4) {
        int n5;
        int n6;
        if (this.D >= 0) {
            return false;
        }
        boolean bl = false;
        if (os_02 == os_03) {
            return false;
        }
        if (os_03.topRun == null) {
            return false;
        }
        int n7 = os_03.topRun.cardCount;
        boolean bl2 = false;
        int n8 = 1;
        if (os_02.group.groupIndex == 0 && os_02.topRun == null) {
            if (os_03.group.groupIndex == 0 && os_03.runs.size() == 1) {
                return false;
            }
                        if (os_03.topRun.cards[0].rank == 13) {
                n8 = 2;
            }
        }
        if ((n8 = os_02.a(os_03, n8, false)) > 0) {
            if (n3 > 0) {
                n6 = n3;
                n5 = n6 & 0xFF;
                n6 = n3;
                if (n8 == (n6 = (n6 & 0xF0000) >> 16) && os_03.group.groupIndex * 10 + os_03.stackIndex == n5) {
                    return false;
                }
            }
            if (n8 > 0) {
                switch (n2) {
                    case 3: {
                        if (n8 >= os_03.topRun.cardCount) break;
                        n8 = -1;
                        break;
                    }
                    case 2: {
                        if (os_03.topRun.cards[0].rank != 1) break;
                        n8 = -1;
                        break;
                    }
                    case 5: {
                        if (n8 == os_03.topRun.cardCount) {
                            n8 = -1;
                            break;
                        }
                        n5 = os_03.topRun.cards[os_03.topRun.cardCount - n8 - 1].cardId;
                        if (this.d.searchState.stackGroups[3].stacks[0].getTopCardValue() + 1 != n5 && this.d.searchState.stackGroups[3].stacks[1].getTopCardValue() + 1 != n5 && this.d.searchState.stackGroups[3].stacks[2].getTopCardValue() + 1 != n5 && this.d.searchState.stackGroups[3].stacks[3].getTopCardValue() + 1 != n5) {
                            n8 = -1;
                            break;
                        }
                        if (this.d.logLevel > 3) break;
                        this.d.log("Productive split");
                        break;
                    }
                    case 6: {
                        bl = true;
                        if (n8 != 1) {
                            n8 = -1;
                            break;
                        }
                        if (os_03.topRun.cards[os_03.topRun.cardCount - 1].rank >= 3) break;
                        n8 = -1;
                    }
                }
            }
        }
        if (n8 >= 0) {
            n5 = n8 > 0 && os_02.topRun != null ? 1 : 0;
            if ((n8 = os_02.a(os_03, n8, null)) >= 0) {
                if (this.d.logLevel <= 2) {
                    this.d.log("Completed join with split of " + n8);
                }
                this.d.searchState.moves[this.d.searchState.depth] = n6 = Move.a(n8, n7, os_03, os_02, n5 != 0);
                ++this.d.searchState.depth;
                if (!this.b(os_02, os_03)) {
                    bl2 = true;
                    long l2 = this.computeStateHash();
                    this.D = this.b(l2);
                    if (this.D < 0) {
                        this.a(l2);
                        n7 = this.d.complexity;
                        if (this.d.logLevel <= 3) {
                            this.d.log("Join mode " + n2 + " productive for src " + os_03 + " and dest " + os_02);
                        }
                        if (os_03.group.groupIndex == this.d.bridge.specialDestinationGroupIndex) {
                            n4 = this.d.searchState.stackGroups[1].stacks[0].runs.size();
                        }
                        n2 = 0;
                        n5 = os_03.runs.size() - 1;
                        if (n5 >= 0 && os_03.group.groupIndex == 0 && os_03.topRun.faceDown) {
                            os_03.topRun.faceDown = false;
                            n2 = 1;
                            --this.hiddenCardCount;
                            this.runningScore += 5;
                            long l3 = this.d.searchStepCount - this.hiddenRunAgeStartMoves[os_03.stackIndex][n5];
                            long[] lArray = this.hiddenRunAgeTotals[os_03.stackIndex];
                            int n9 = n5;
                            lArray[n9] = lArray[n9] + l3;
                            this.hiddenRunAgeStartMoves[os_03.stackIndex][n5] = -1L;
                            if (this.useHiddenRunAgePenalty) {
                                this.d.complexity += this.d(os_03.stackIndex, n5);
                            }
                        }
                        if (!this.B) {
                            if (!this.a(os_03, n3)) {
                                if (this.d.logLevel <= 5) {
                                    this.d.log("Skipping call to solve because checkReadCard failed");
                                }
                                this.D = 0;
                            } else if (!this.B) {
                                this.a(n6, n4, false, bl, false);
                            }
                        }
                        if (n2 != 0) {
                            this.runningScore -= 5;
                            os_03.topRun.faceDown = true;
                            ++this.hiddenCardCount;
                            this.hiddenRunAgeStartMoves[os_03.stackIndex][n5] = this.d.searchStepCount;
                        }
                        this.d.complexity = n7;
                    }
                    if (this.D >= 0) {
                        --this.D;
                    }
                }
                --this.d.searchState.depth;
                os_02.b(os_03, n8, null);
            }
        }
        return bl2;
    }

    @Override
    final int[] d() {
        KlondikeSolver nB2 = this;
        return nB2.b(nB2.n(), 1);
    }

    private HashMap n() {
        HashMap hashMap = new HashMap(52);
        this.a(hashMap, this.d.searchState.stackGroups[0], 1);
        this.a(hashMap, this.d.searchState.stackGroups[3], 1);
        this.a(hashMap, this.d.searchState.stackGroups[1], 1);
        this.a(hashMap, this.d.searchState.stackGroups[2], 1);
        return hashMap;
    }

    @Override
    final int a(CardStack os_02) {
        HashMap hashMap = this.n();
        int n2 = hashMap.size();
        if (n2 == 51) {
            int n3 = 1;
            while (n3 < 14) {
                int n4 = 1;
                while (n4 < 5) {
                    int n5 = n4 * 100 + n3;
                    if (!hashMap.containsKey(n5)) {
                        if (this.d.logLevel <= 4) {
                            this.d.log("Final card must be " + n5);
                        }
                        CardStack[] os_0Array = this.d.searchState.stackGroups[0].stacks;
                        int n6 = this.d.searchState.stackGroups[0].stacks.length;
                        int n7 = 0;
                        while (n7 < n6) {
                            CardStack os_03 = os_0Array[n7];
                            if (os_03.runs.size() != 0) {
                                CardRun ok_02 = (CardRun)os_03.runs.getFirst();
                                if (ok_02.cardCount > 0 && ok_02.cards[0].cardId == 0) {
                                    if (this.d.logLevel <= 6) {
                                        this.d.log("***Final card (on stack " + os_03.stackIndex + ") must be " + n5);
                                    }
                                    this.g[0][os_03.stackIndex] = n5;
                                    ok_02.a(n5);
                                    ok_02 = (CardRun)this.d.initialState.stackGroups[0].stacks[os_03.stackIndex].runs.getFirst();
                                    ok_02.a(n5);
                                }
                            }
                            ++n7;
                        }
                    }
                    ++n4;
                }
                ++n3;
            }
        }
        return n2;
    }

    @Override
    final boolean a(CardStack os_02, CardStack os_03) {
        return false;
    }

    @Override
    final int a(GameState nY2, boolean bl, int n2, int n3) {
        if (bl) {
            return 0;
        }
        if (nY2 == null) {
            return 0;
        }
        if (nY2.stackGroups[3] == null) {
            return 0;
        }
        int n4 = 0;
        if (this.b(nY2) >= 0) {
            n4 = n2 > 0 ? 1 : 4;
        } else {
            CardStack[] os_0Array = nY2.stackGroups[3].stacks;
            int n5 = os_0Array.length;
            int n6 = 0;
            while (n6 < n5) {
                CardStack os_02 = os_0Array[n6];
                if (os_02.topRun != null && os_02.topRun.cardCount >= n3 && (n2 == 0 || os_02.topRun.cards[os_02.topRun.cardCount - 1].suit == n2)) {
                    ++n4;
                }
                ++n6;
            }
        }
        return n4;
    }

    @Override
    final boolean a(GameState nY2, int n2) {
        return this.b(nY2, n2, true) >= 0;
    }

    @Override
    final boolean loadStateFromLines(String string, String[] stringArray, int n2) {
        Object object;
        int n3;
        SolverContext om_02;
        String string2;
        boolean bl = false;
        StackGroup ot_02 = this.d.initialState.stackGroups[0];
        CardStack os_02 = this.d.initialState.stackGroups[this.d.bridge.specialSourceGroupIndex].stacks[0];
        CardStack os_03 = this.d.initialState.stackGroups[this.d.bridge.specialDestinationGroupIndex].stacks[0];
        if (string == null) {
            string2 = "You must specify deal option 1 or 3 for Klondike";
            om_02 = this.d;
            om_02.invalidInput(string2, false);
        }
        this.d.initialState.dealIndex = 0;
        this.dealAreaCardCount = 24;
        this.stockCardCount = 24;
        int n4 = 0;
        while (n4 < 7) {
            int n5 = n4++;
            this.h[n5] = n5 + 1;
        }
        int[] nArray = new int[7];
        nArray[1] = 1;
        nArray[2] = 2;
        nArray[3] = 3;
        nArray[4] = 4;
        nArray[5] = 5;
        nArray[6] = 6;
        int[] nArray2 = nArray;
        int n6 = 7;
        if (string != null) {
            String[] stringArray2 = string.trim().split(":");
            try {
                this.d.table.drawCount = Integer.parseInt(stringArray2[0]);
                if (this.d.table.drawCount != 1 && this.d.table.drawCount != 3) {
                    string2 = "You must specify deal option 1 or 3 for Klondike";
                    om_02 = this.d;
                    om_02.invalidInput(string2, false);
                }
                if (stringArray2.length == 1) {
                    if (n2 != 9) {
                        string2 = "Klondike input file must have 8 rows of cards";
                        om_02 = this.d;
                        om_02.invalidInput(string2, false);
                    }
                } else {
                    bl = true;
                    n6 = 0;
                    this.dealAreaCardCount = Integer.parseInt(stringArray2[1]);
                    this.stockCardCount = Integer.parseInt(stringArray2[2]);
                    n2 = 0;
                    while (n2 < 7) {
                        int n7 = Integer.parseInt(stringArray2[n2 + 3]);
                        int n8 = n7 % 100;
                        n3 = n7 / 100;
                        this.h[n2] = n8;
                        nArray2[n2] = n3;
                        if (n8 > n6) {
                            n6 = n8;
                        }
                        ++n2;
                    }
                }
            }
            catch (NumberFormatException numberFormatException) {
                string2 = "Error parsing Klondike options for partially completed deck";
                om_02 = this.d;
                om_02.invalidInput(string2, false);
            }
        }
        int n9 = 0;
        while (n9 < n6) {
            String[] stringArray3 = stringArray[n9 + 1].split(",");
            int n10 = 0;
            while (n10 < 7) {
                Object object2;
                CardStack os_04 = ot_02.stacks[n10];
                n3 = -1;
                if (bl) {
                    if (n9 < this.h[n10]) {
                        object2 = stringArray3[n10];
                        n3 = this.d.parseCardCode((String)object2);
                    }
                } else if (n10 >= n9) {
                    object2 = stringArray3[n10 - n9];
                    n3 = this.d.parseCardCode((String)object2);
                }
                if (n3 >= 0) {
                    if (this.d.logLevel <= 3) {
                        this.d.log("Loading card " + n3 + " into initcards " + n10 + "," + n9);
                    }
                    this.g[n9][n10] = n3;
                    if (nArray2[n10] > n9) {
                        object2 = new CardRun(this.b(os_04, n3));
                        os_04.a((CardRun)object2);
                        ((CardRun)object2).faceDown = true;
                    } else {
                        if (this.d.logLevel <= 2) {
                            this.d.log("Loading card " + n3 + " into stack " + n10 + " level " + n9);
                        }
                        object2 = os_04.topRun;
                        object = new CardRun(this.b(os_04, n3));
                        int n11 = 0;
                        if (bl && object2 != null) {
                            n11 = os_04.a((CardRun)object2, (CardRun)object, false, false);
                        }
                        if (n11 > 0) {
                            ((CardRun)object2).a((CardRun)object, n11);
                        } else {
                            os_04.a((CardRun)object);
                        }
                    }
                }
                ++n10;
            }
            ++n9;
        }
        this.d.bridge.g();
        String[] stringArray4 = stringArray[n6 + 1].split(",");
        try {
            int n12 = 0;
            while (n12 < this.dealAreaCardCount) {
                int n13;
                String string3 = stringArray4[n12];
                this.y[n12][0] = n13 = this.d.parseCardCode(string3);
                if (this.d.logLevel <= 2) {
                    this.d.log("Loading card " + n13 + " into deck level " + n12);
                }
                if (n12 < this.dealAreaCardCount - this.stockCardCount) {
                    os_03.a(new CardRun(this.b(os_03, n13)));
                } else {
                    CardRun ok_02 = new CardRun(this.b(os_02, n13));
                    os_02.a(ok_02);
                    ok_02.faceDown = true;
                }
                ++n12;
            }
        }
        catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
            String string3 = "Too few cards in the deal pile";
            SolverContext om_03 = this.d;
            om_03.invalidInput(string3, false);
        }
        if (bl) {
            stringArray4 = stringArray.length > n6 + 2 ? stringArray[n6 + 2].split(",") : "??,??,??,??,".split(",");
            int n14 = 0;
            while (n14 < 4) {
                int n15;
                String string4 = stringArray4[n14];
                this.foundationTopCards[n14] = n15 = this.d.parseCardCode(string4);
                if (n15 > 0) {
                    int n16 = n15 / 100;
                    int n17 = n15 % 100;
                    object = this.d.initialState.stackGroups[3].stacks[n14];
                    CardRun ok_03 = new CardRun(this.b((CardStack)object, n16 * 100 + 1));
                    ((CardStack)object).a(ok_03);
                    int n18 = 2;
                    while (n18 <= n17) {
                        ok_03.a(new CardRun(this.b((CardStack)object, n16 * 100 + n18)), 1);
                        ++n18;
                    }
                }
                ++n14;
            }
        }
        boolean bl2 = this.b(1, false);
        return bl2;
    }

    @Override
    final int a(HashMap hashMap) {
        int n2;
        int n3 = 0;
        int n4 = 0;
        while (n4 < this.f) {
            n2 = 0;
            while (n2 < this.h[n4]) {
                ++n3;
                this.a(hashMap, this.g[n2][n4], "stack");
                if (this.d.logLevel <= 3) {
                    this.d.log("Add card " + n4 + "," + n2 + " to check:" + this.g[n2][n4]);
                }
                ++n2;
            }
            ++n4;
        }
        n4 = 0;
        while (n4 < this.dealAreaCardCount) {
            if (this.y[n4][0] == 0) break;
            this.a(hashMap, this.y[n4][0], "deal ");
            ++n3;
            ++n4;
        }
        n4 = 0;
        while (n4 < 4) {
            if (this.foundationTopCards[n4] != 0) {
                n2 = this.foundationTopCards[n4];
                int n5 = n2 / 100;
                n2 %= 100;
                int n6 = 1;
                while (n6 <= n2) {
                    this.a(hashMap, n5 * 100 + n6, "aces ");
                    ++n3;
                    ++n6;
                }
            }
            ++n4;
        }
        return n3;
    }

    @Override
    final void appendBoardState(StringBuffer stringBuffer) {
        int n2;
        int n3;
        stringBuffer.append(SolverContext.VARIANT_NAMES[this.d.variantId]);
        int n4 = 0;
        int[] nArray = new int[this.f];
        int n5 = 0;
        while (n5 < 49) {
            n3 = 1;
            n2 = 0;
            while (n2 < this.f) {
                if (this.g[n5][n2] != 0) {
                    nArray[n2] = n5 + 1;
                    n3 = 0;
                }
                ++n2;
            }
            if (n3 != 0) break;
            ++n5;
        }
        n3 = 0;
        while (n3 < 7) {
            if (nArray[n3] != n3 + 1) {
                n4 = 1;
                break;
            }
            ++n3;
        }
        stringBuffer.append(",");
        stringBuffer.append(this.d.table.drawCount);
        n3 = n4 == 0 && this.dealAreaCardCount == 24 && this.stockCardCount == 24 ? 0 : 1;
        if (n3 != 0) {
            stringBuffer.append(":");
            stringBuffer.append(this.dealAreaCardCount);
            stringBuffer.append(":");
            stringBuffer.append(this.stockCardCount);
            n2 = 0;
            while (n2 < 7) {
                stringBuffer.append(":" + (this.i[n2] * 100 + nArray[n2]));
                ++n2;
            }
        }
        stringBuffer.append("\n# Stacks:\n");
        n2 = 0;
        while (n2 < n5) {
            n4 = 0;
            while (n4 < this.f) {
                if (n3 == 0 && n4 < n2) {
                    stringBuffer.append("     ");
                } else {
                    stringBuffer.append(KlondikeSolver.i(this.g[n2][n4]));
                    stringBuffer.append(",");
                }
                ++n4;
            }
            stringBuffer.append("\n");
            ++n2;
        }
        stringBuffer.append("# Deck:\n");
        n2 = 0;
        while (n2 < this.dealAreaCardCount) {
            stringBuffer.append(KlondikeSolver.i(this.y[n2][0]));
            stringBuffer.append(",");
            ++n2;
        }
        if (n3 != 0) {
            stringBuffer.append("\n# Aces:\n");
            n2 = 0;
            while (n2 < 4) {
                stringBuffer.append(KlondikeSolver.i(this.foundationTopCards[n2]));
                stringBuffer.append(",");
                ++n2;
            }
        }
    }

    @Override
    final StringBuffer createStateHeader(String string, int n2) {
        return new StringBuffer(String.format("%s[%03d:%02d,%02d,%02d,%02d,%02d,%02d,%02d,%02d]: ", string, n2, this.stackToAceAttempts, this.pileToStackAttempts, this.stackToStackAttempts, this.pileToAceAttempts, this.splitRunAttempts, this.aceToStackAttempts, this.dealAttempts, this.hiddenCardCount));
    }

    @Override
    final void a(int n2) {
    }

    final int a(int n2, CardStack os_02, CardStack os_03, boolean bl) {
        int n3 = 0;
        int n4 = 0;
        int n5 = 0;
        int n6 = n2 & 0xFFFFFF7F;
        if (os_02.topRun == null) {
            if (os_03.topRun == null) {
                if (this.d.logLevel <= 3) {
                    this.d.log(" pilestack empty so rejecting deal switch");
                }
                return -1;
            }
            if ((n2 & 0x80) != 0) {
                if (this.d.logLevel <= 3) {
                    this.d.log("Hit end feed after flip with nothing productive since so reject another flip");
                }
                return -1;
            }
            n2 = os_03.runs.size();
            if (this.d.logLevel <= 3) {
                this.d.log("Flipping pile remaining of " + n2 + " cards");
            }
            CardStack.a(os_02, os_03);
            n5 = n2;
            ++n4;
        }
        n2 = 0;
        while (n2 < this.d.table.drawCount) {
            if (os_02.topRun != null) {
                CardRun ok_02 = os_02.popFirstRun();
                if (this.d.logLevel <= 3) {
                    this.d.log("Moving " + ok_02.cards[0] + " to pile");
                }
                if (ok_02.cards[0].cardId == 0) {
                    this.d.log("Logic error - moving unknown card");
                }
                os_03.a(ok_02);
                ++n3;
            }
            ++n2;
        }
        n2 = -1;
        if (bl) {
            ++this.d.searchState.dealIndex;
            n2 = Move.a(8, n4, n5, n3);
            int n7 = os_02.runs.size();
            if (n6 == n7) {
                this.b(n2, os_02, os_03, true);
                return -1;
            }
        }
        return n2;
    }

    final void b(int n2, CardStack os_02, CardStack os_03, boolean bl) {
        int n3 = n2;
        n3 &= 0xFF;
        int n4 = 0;
        while (n4 < n3) {
            CardRun ok_02 = os_03.popTopRun();
            if (this.d.logLevel <= 3) {
                this.d.log("Undeal card " + ok_02.cards[0]);
            }
            os_02.c(ok_02);
            ++n4;
        }
        n3 = n2;
        if ((n3 >> 8 & 0xFF) > 0) {
            CardStack.a(os_02, os_03);
        }
        if (bl) {
            --this.d.searchState.dealIndex;
        }
    }

    @Override
    final int a(GameState nY2, boolean bl) {
        if (bl) {
            return 0;
        }
        int n3 = this.runningScore;
        int n2 = n3;
        int n4 = this.b(nY2) * 10;
        if (n4 > 0) {
            nY2.scoreByDepth[nY2.depth - 1] = n3;
            n2 = n3 + n4;
        }
        return n2;
    }

    @Override
    final int a(CardStack os_02, CardStack os_03, int n2) {
        return this.a(os_02, os_03, n2, 0);
    }

    private int a(CardStack os_02, CardStack os_03, int n2, int n3) {
        int n4 = -1;
        if (os_03.group.groupIndex == 1 || os_03.group.groupIndex == 2) {
            return -1;
        }
        if (os_03.group.groupIndex >= 3 ? os_03.topRun == null && os_02.topRun.cards[os_02.topRun.cardCount - 1].rank != 1 : os_03.topRun == null && (os_02.group.groupIndex >= 3 || os_02.topRun.cards[0].rank != 13)) {
            return -1;
        }
        int n5 = os_02.topRun.cardCount;
        int n6 = 1;
        if (os_03.topRun == null) {
            n6 = 2;
            if (os_03.group.groupIndex == 0) {
                if (n3 != 0 && n3 != 1) {
                    return -1;
                }
        if (os_02.topRun.cards[0].rank != 13) {
                    return -1;
                }
            } else if (os_03.group.groupIndex == 3) {
                n6 = 6;
            }
        }
        if ((n3 = os_03.a(os_02, n6, true)) >= 0) {
            int n7 = n6 = n3 == 0 ? n5 : n3;
            if (n2 > 0 && n2 != n6) {
                return -1;
            }
            if (this.d.logLevel <= 5) {
                this.d.log("User move will be permitted " + n3);
            }
            n2 = 0;
            if (n3 > 0) {
                n2 = os_03.topRun != null ? 2 : 0;
                n2 = n2 | (n3 < n5 ? 1 : 0);
            }
            n4 = Move.a(n2, n6, os_02, os_03);
            this.k(n4);
            int n8 = n4;
            if ((n8 >> 24 & 8) == 0) {
                GameState nY2 = new GameState(this.d.initialState, true);
                nY2.stackGroups[Move.e((int)n4)].stacks[Move.f(n4)].a(nY2.stackGroups[Move.c((int)n4)].stacks[Move.d(n4)], n6, null);
                if (this.a(nY2, n4, true) == 2) {
                    this.d.playbackState.moves[this.d.playbackState.depth] = Move.a(24, 0, 0, 0);
                    ++this.d.playbackState.depth;
                }
            }
        }
        return n4;
    }

    @Override
    final int a(Card nT2) {
        CardStack os_02 = nT2.stack;
        if (os_02.group.groupIndex == this.d.bridge.specialSourceGroupIndex) {
            if (this.d.logLevel <= 5) {
                this.d.log("Got double click on feed stack");
            }
            return this.o();
        }
        int n2 = -1;
        if (os_02.group.groupIndex == 3) {
            return -1;
        }
        CardStack[] os_0Array = this.d.initialState.stackGroups[3].stacks;
        int n3 = os_0Array.length;
        int n4 = 0;
        while (n4 < n3) {
            CardStack os_03 = os_0Array[n4];
            n2 = this.a(os_02, os_03, 1, 0);
            if (n2 > 0) {
                break;
            }
            ++n4;
        }
        int n5 = os_02.topRun.cardCount;
        if (n2 <= 0 && os_02.stackIndex < 30) {
            os_0Array = this.d.initialState.stackGroups[0].stacks;
            int n6 = os_0Array.length;
            n4 = 0;
            while (n4 < n6) {
                CardStack os_04 = os_0Array[n4];
                if (os_04 != os_02 && (n2 = this.a(os_02, os_04, n5, 2)) > 0) {
                    break;
                }
                ++n4;
            }
        }
        if (n2 <= 0 && os_02.group.groupIndex == 0) {
            os_0Array = this.d.initialState.stackGroups[0].stacks;
            int n7 = os_0Array.length;
            n4 = 0;
            while (n4 < n7) {
                CardStack os_05 = os_0Array[n4];
                if (os_05 != os_02 && (n2 = this.a(os_02, os_05, -1, 0)) > 0) {
                    break;
                }
                ++n4;
            }
        }
        if (n2 <= 0 && os_02.stackIndex < 30) {
            os_0Array = this.d.initialState.stackGroups[0].stacks;
            int n8 = os_0Array.length;
            n4 = 0;
            while (n4 < n8) {
                CardStack os_06 = os_0Array[n4];
                if (os_06 != os_02 && os_06.topRun == null && (n2 = this.a(os_02, os_06, -1, 1)) > 0) {
                    break;
                }
                ++n4;
            }
        }
        return n2;
    }

    @Override
    final int b(CardStack os_02) {
        int n2 = -1;
        if (os_02.group.groupIndex == this.d.bridge.specialSourceGroupIndex) {
            if (this.d.logLevel <= 5) {
                this.d.log("Trying a deal " + os_02.stackIndex);
            }
            n2 = this.o();
        }
        return n2;
    }

    private int o() {
        int n2;
        CardStack os_02 = this.d.initialState.stackGroups[1].stacks[0];
        CardStack os_03 = this.d.initialState.stackGroups[2].stacks[0];
        int n3 = 0;
        int n4 = 0;
        if (os_02.topRun == null) {
            int n5;
            if (os_03.topRun == null) {
                if (this.d.logLevel <= 3) {
                    this.d.log(" pilestack empty so rejecting deal switch");
                }
                return -1;
            }
            n2 = 0;
            if (this.d.ag) {
                n5 = this.d.playbackMoveIndex - 1;
                while (n5 >= 0) {
                    n4 = this.d.initialState.moves[n5];
                    if ((n4 >> 24 & 8) != 0) {
                        ++n2;
                        --n5;
                        continue;
                    }
                    break;
                }
            } else {
                n5 = this.d.playbackState.depth - 1;
                while (n5 >= 0) {
                    n4 = this.d.playbackState.moves[n5];
                    if ((n4 >> 24 & 8) != 0) {
                        ++n2;
                        --n5;
                        continue;
                    }
                    break;
                }
            }
            if (n2 > (n5 = os_03.runs.size()) / this.d.table.drawCount + 1) {
                if (this.d.logLevel <= 3) {
                    this.d.log("More consecutive deals than cards so reject another deal");
                }
                return -1;
            }
            n4 = n5;
            ++n3;
            n2 = n5;
        } else {
            n2 = os_02.runs.size();
        }
        if (n2 > this.d.table.drawCount) {
            n2 = this.d.table.drawCount;
        }
        n2 = Move.a(8, n3, n4, n2);
        this.k(n2);
        return n2;
    }
}




