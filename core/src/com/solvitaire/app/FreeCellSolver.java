/*
 * Decompiled with CFR 0.152.
 */
package com.solvitaire.app;

import com.solvitaire.app.FreeCellBridge;
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

/*
 * Renamed from com.solvitaire.app.nz
 */
final class FreeCellSolver
extends BaseSolver {
    static private int MAX_TABLEAU_HEIGHT = 10;
    private int moveToAcesPenalty = 0;
    private int fromSpacePenalty = -5;
    private int moveToSpacePenalty = 40;
    private int kingToSpacePenalty = 20;
    private int moveToWorkAreaPenalty = 40;
    private int fromWorkAreaPenalty = -10;
    private int alternatingJoinPenalty = -4;
    private int exposeAcePenalty = -12;
    private int splitMatchPenalty = 30;
    private int splitMatchesAcePenalty = -10;
    private int maxMoveTargetMoveToAcesPenalty = -3;
    private int maxMoveTargetAlternatingJoinPenalty = -2;
    private int maxMoveTargetExposeAcePenalty = -4;
    private int removeAntagonistSuitPenalty = 43;
    private int removeTargetSuitPenalty = -9;
    private int removeOtherSuitPenalty = 6;
    private int splitGivesSuitPenalty = -2;
    private int splitDoesNotGiveSuitPenalty = 10;
    private int joinOverTargetSuitPenalty = 3;
    private int challengeExposeAcePenalty = -7;
    private int challengeAlternatingJoinPenalty = -1;
    private int challengeKingToSpacePenalty = 20;
    private int challengeToSpacePenalty = 40;
    private int moveToAcesAttempts = 0;
    private int toSpaceAttempts = 0;
    private int fromSpaceAttempts = 0;
    private int moveToWorkAreaAttempts = 0;
    private int fromWorkAreaAttempts = 0;
    private int exposeAceAttempts = 0;
    private int alternatingJoinAttempts = 0;
    private int splitMatchAttempts = 0;
    static private String[] moveModeNames = new String[]{"?", "toAces", "fromSpace", "toSpace", "fromWork", "toWork", "matching", "toAcesAuto", "expose", "matchWithSplit", "toSpaceKing"};
    final static int[] targetFoundationIndexBySuit;
    static private int[] antagonistFoundationIndexBySuit;
    private FreeCellBridge freeCellBridge;
    int[] b;
    int[] c;

    static {
        int[] nArray = new int[5];
        nArray[0] = -1;
        nArray[1] = 3;
        nArray[3] = 2;
        nArray[4] = 1;
        targetFoundationIndexBySuit = nArray;
        int[] nArray2 = new int[5];
        nArray2[0] = -1;
        nArray2[1] = 1;
        nArray2[2] = 2;
        nArray2[4] = 3;
        antagonistFoundationIndexBySuit = nArray2;
    }

    FreeCellSolver(SolverContext om_02) {
        super(om_02, 2000);
        this.n = 1;
        this.o = 52;
        this.f = 8;
        this.q = true;
    }

    @Override
    final String getSolverName() {
        return "FreeCell";
    }

    @Override
    final boolean initializeSolver() {
        this.initializeBaseState();
        this.e = String.valueOf(this.d.workspaceRoot) + "freecell" + File.separator;
        this.g = new int[50][this.f];
        this.b = new int[4];
        this.c = new int[4];
        this.r = new int[this.f][MAX_TABLEAU_HEIGHT];
        int[][] cfr_ignored_0 = new int[0][1];
        this.h = new int[this.f];
        this.i = new int[this.f];
        if (this.C) {
            if (this.d.logLevel <= 4) {
                this.d.log("Running FreeCell");
            }
            this.freeCellBridge = new FreeCellBridge(this);
            this.d.bridge = this.freeCellBridge;
            if (this.d.logLevel <= 3) {
                this.d.log("Auto constructed");
            }
            this.d.bridge.d();
        }
        if (!this.d.bridge.e()) {
            return false;
        }
        if (!this.d.t) {
            this.freeCellBridge.i();
        }
        this.d.searchState = new GameState(this.d.initialState, true);
        this.F = 0;
        this.G = 0;
        if (this.d.files.maxMoves < 999) {
            if (this.d.logLevel <= 5) {
                this.d.log("Using modified search for max move target");
            }
            this.moveToAcesPenalty = this.maxMoveTargetMoveToAcesPenalty;
            this.exposeAcePenalty = this.maxMoveTargetExposeAcePenalty;
            this.alternatingJoinPenalty = this.maxMoveTargetAlternatingJoinPenalty;
        }
        if (this.d.files.b == 4) {
            this.exposeAcePenalty = this.challengeExposeAcePenalty;
            this.alternatingJoinPenalty = this.challengeAlternatingJoinPenalty;
            this.kingToSpacePenalty = this.challengeKingToSpacePenalty;
            this.moveToSpacePenalty = this.challengeToSpacePenalty;
        }
        return true;
    }

    @Override
    final void dumpState(int n2, boolean bl) {
        if (this.d.logLevel <= n2) {
            this.b(n2);
            this.a(n2, this.d.searchState.stackGroups[2]);
            this.a(n2, this.d.searchState.stackGroups[1]);
            this.a(n2, this.d.searchState.stackGroups[0]);
        }
    }

    @Override
    final void search(int n2, int n3) {
        this.l();
        if (this.d.searchStepCount++ % 100000L == 0L) {
            this.b(4);
        }
        if (!this.B) {
            FreeCellSolver nz_02 = this;
            int n4 = n2;
            GameState nY2 = nz_02.d.searchState;
            FreeCellSolver nz_03 = nz_02;
            n3 = nz_02.a(nY2, n4, false);
            if (n3 == 2) {
                if (this.d.logLevel <= 4) {
                    this.d.log("Solved state solved so backout 999");
                }
                this.D = 999;
            } else if (n3 == 1) {
                return;
            }
        }
        if (this.d.searchState.depth > this.maxSearchDepth) {
            return;
        }
        if (this.D < 0 && this.d.searchState.depth == 0 && this.c(1, n2) && this.D < 0) {
            this.D = 0;
        }
        n3 = this.d.complexity;
        if (this.D < 0 && !this.c(7, n2)) {
            if (this.D < 0) {
                this.d.complexity += this.moveToAcesPenalty;
                if (this.d.complexity <= 0) {
                    ++this.moveToAcesAttempts;
                    if (this.d.logLevel <= 2) {
                        this.d.log("Depth " + this.d.searchState.depth + " try moving aces");
                    }
                    this.c(1, n2);
                    --this.moveToAcesAttempts;
                }
                this.d.complexity = n3;
            }
            if (this.D < 0) {
                this.d.complexity += this.fromWorkAreaPenalty;
                if (this.d.complexity < 0) {
                    ++this.fromWorkAreaAttempts;
                    if (this.d.logLevel <= 2) {
                        this.d.log("Depth " + this.d.searchState.depth + " try moving from work area");
                    }
                    this.c(4, n2);
                    --this.fromWorkAreaAttempts;
                }
                this.d.complexity = n3;
            }
            if (this.D < 0) {
                this.d.complexity += this.fromSpacePenalty;
                if (this.d.complexity < 0) {
                    ++this.fromSpaceAttempts;
                    this.c(2, n2);
                    --this.fromSpaceAttempts;
                }
                this.d.complexity = n3;
            }
            if (this.D < 0) {
                this.d.complexity += this.exposeAcePenalty;
                if (this.d.complexity < 0) {
                    ++this.alternatingJoinAttempts;
                    if (this.d.logLevel <= 2) {
                        this.d.log("Depth " + this.d.searchState.depth + " try exposing board ace");
                    }
                    this.c(8, n2);
                    --this.alternatingJoinAttempts;
                }
                this.d.complexity = n3;
            }
            if (this.D < 0) {
                this.d.complexity += this.alternatingJoinPenalty;
                if (this.d.complexity < 0) {
                    ++this.exposeAceAttempts;
                    if (this.d.logLevel <= 2) {
                        this.d.log("Depth " + this.d.searchState.depth + " try alternating joins");
                    }
                    this.c(6, n2);
                    --this.exposeAceAttempts;
                }
                this.d.complexity = n3;
            }
            if (this.D < 0) {
                this.d.complexity += this.kingToSpacePenalty;
                if (this.d.complexity < 0) {
                    ++this.toSpaceAttempts;
                    if (this.d.logLevel <= 2) {
                        this.d.log("Depth " + this.d.searchState.depth + " try moving to a space");
                    }
                    this.c(10, n2);
                    --this.toSpaceAttempts;
                }
                this.d.complexity = n3;
            }
            if (this.D < 0) {
                this.d.complexity += this.moveToSpacePenalty;
                if (this.d.complexity < 0) {
                    ++this.toSpaceAttempts;
                    if (this.d.logLevel <= 2) {
                        this.d.log("Depth " + this.d.searchState.depth + " try moving to a space");
                    }
                    this.c(3, n2);
                    --this.toSpaceAttempts;
                }
                this.d.complexity = n3;
            }
            if (this.D < 0) {
                this.d.complexity += this.moveToWorkAreaPenalty;
                if (this.d.complexity < 0) {
                    ++this.moveToWorkAreaAttempts;
                    if (this.d.logLevel <= 2) {
                        this.d.log("Depth " + this.d.searchState.depth + " try moving to work area");
                    }
                    this.c(5, n2);
                    --this.moveToWorkAreaAttempts;
                }
                this.d.complexity = n3;
            }
            if (this.D < 0) {
                this.d.complexity += this.splitMatchPenalty;
                if (this.d.complexity < 0) {
                    ++this.splitMatchAttempts;
                    if (this.d.logLevel <= 2) {
                        this.d.log("Depth " + this.d.searchState.depth + " try a match with a split");
                    }
                    this.c(9, n2);
                    --this.splitMatchAttempts;
                }
                this.d.complexity = n3;
            }
        }
        if (this.D >= 0) {
            --this.D;
            if (this.d.logLevel <= 1) {
                this.d.log("Backout now " + this.D);
            }
        }
    }

    @Override
    final long computeStateHash() {
        long l2 = 0L;
        int n2 = 0;
        while (n2 < this.f) {
            FreeCellSolver nz_02 = this;
            l2 += nz_02.a(nz_02.d.searchState.stackGroups[0].stacks[n2], l2, true, false);
            ++n2;
        }
        this.m = 0;
        n2 = 0;
        while (n2 < 4) {
            FreeCellSolver nz_03 = this;
            l2 += nz_03.a(nz_03.d.searchState.stackGroups[2].stacks[n2], l2, false, false);
            ++n2;
        }
        n2 = 0;
        while (n2 < 4) {
            FreeCellSolver nz_04 = this;
            l2 += nz_04.a(nz_04.d.searchState.stackGroups[1].stacks[n2], l2, false, false);
            ++n2;
        }
        return l2;
    }

    private static boolean c(CardStack os_02) {
        boolean bl = false;
        java.util.Iterator iterator = os_02.runs.iterator();
        while (iterator.hasNext()) {
            CardRun ok_02 = (CardRun)iterator.next();
            if (ok_02.cardCount != 1 || ok_02.cards[0].rank != 1) continue;
            bl = true;
            break;
        }
        return bl;
    }

    private boolean d(CardStack os_02) {
        int n2 = this.d.searchState.stackGroups[2].stacks[targetFoundationIndexBySuit[this.d.files.d]].b() + 1;
        CardRun ok_02 = null;
        CardRun ok_03;
        for (java.util.Iterator iterator = os_02.runs.iterator(); iterator.hasNext() && (ok_03 = (CardRun)iterator.next()) != os_02.topRun; ok_02 = ok_03) {
        }
        return ok_02 != null && ok_02.cards[ok_02.cardCount - 1].cardId == n2;
    }

    private boolean c(int n2, int n3) {
        boolean bl;
        block81: {
            bl = false;
            if (this.d.logLevel <= 3) {
                this.d.log("Entered dojoins for mode " + moveModeNames[n2] + " complexity " + this.d.complexity);
            }
            if (n2 == 4) {
                CardStack[] os_0Array = this.d.searchState.stackGroups[1].stacks;
                int n4 = this.d.searchState.stackGroups[1].stacks.length;
                int n5 = 0;
                while (n5 < n4) {
                    CardStack os_02 = os_0Array[n5];
                    if (this.D <= 0) {
                        if (os_02.topRun != null) {
                            CardStack[] os_0Array2 = this.d.searchState.stackGroups[0].stacks;
                            int n6 = this.d.searchState.stackGroups[0].stacks.length;
                            int n7 = 0;
                            while (n7 < n6) {
                                CardStack os_03 = os_0Array2[n7];
                                if (this.D > 0) break;
                                if (os_03.topRun != null || os_02.topRun.cards[0].rank == 13) {
                                    this.a(os_03, os_02, n2, n3);
                                }
                                ++n7;
                            }
                        }
                        ++n5;
                        continue;
                    }
                    break;
                }
            } else if (n2 == 3 || n2 == 10) {
                CardStack[] os_0Array = this.d.searchState.stackGroups[0].stacks;
                int n8 = this.d.searchState.stackGroups[0].stacks.length;
                int n9 = 0;
                while (n9 < n8) {
                    CardStack os_04 = os_0Array[n9];
                    if (os_04.topRun == null) {
                        CardStack[] os_0Array3 = this.d.searchState.stackGroups[0].stacks;
                        int n10 = this.d.searchState.stackGroups[0].stacks.length;
                        int n11 = 0;
                        while (n11 < n10) {
                            CardStack os_05 = os_0Array3[n11];
                            if (this.D <= 0) {
                                if (os_05.topRun != null) {
                                    boolean bl2;
                                    boolean bl3 = bl2 = os_05.topRun.cards[0].rank == 13;
                                    if (!(n2 != 10 ? bl2 : !bl2) && os_05.runs.size() != 1) {
                                        this.a(os_04, os_05, n2, n3);
                                    }
                                }
                                ++n11;
                                continue;
                            }
                            break block81;
                        }
                        break;
                    }
                    ++n9;
                }
            } else if (n2 == 2 || n2 == 8 || n2 == 6 || n2 == 9) {
                CardStack[] os_0Array = this.d.searchState.stackGroups[0].stacks;
                int n12 = this.d.searchState.stackGroups[0].stacks.length;
                int n13 = 0;
                while (n13 < n12) {
                    CardStack os_06 = os_0Array[n13];
                    if (this.D > 0) break;
                    if (os_06.topRun != null) {
                        boolean bl4 = true;
                        if (this.d.files.b == 4) {
                            if (n2 == 8) {
                                if (!this.d(os_06)) {
                                    bl4 = false;
                                }
                            } else if (n2 == 6 && this.d(os_06)) {
                                bl4 = false;
                            }
                        } else if (n2 == 8 && !FreeCellSolver.c(os_06)) {
                            bl4 = false;
                        }
                        if (bl4) {
                            CardStack[] os_0Array4 = this.d.searchState.stackGroups[0].stacks;
                            int n14 = this.d.searchState.stackGroups[0].stacks.length;
                            int n15 = 0;
                            while (n15 < n14) {
                                CardStack os_07 = os_0Array4[n15];
                                if (this.D > 0) break;
                                if (this.d.files.b == 4 || !(n2 != 8 ? n2 == 6 && FreeCellSolver.c(os_06) && !FreeCellSolver.c(os_07) : FreeCellSolver.c(os_07))) {
                                    this.a(os_07, os_06, n2, n3);
                                }
                                ++n15;
                            }
                        }
                    }
                    ++n13;
                }
            } else if (n2 == 5) {
                CardStack os_08;
                if (this.d.searchState.stackGroups[1].stacks[0].topRun == null) {
                    os_08 = this.d.searchState.stackGroups[1].stacks[0];
                } else if (this.d.searchState.stackGroups[1].stacks[1].topRun == null) {
                    os_08 = this.d.searchState.stackGroups[1].stacks[1];
                } else if (this.d.searchState.stackGroups[1].stacks[2].topRun == null) {
                    os_08 = this.d.searchState.stackGroups[1].stacks[2];
                } else if (this.d.searchState.stackGroups[1].stacks[3].topRun == null) {
                    os_08 = this.d.searchState.stackGroups[1].stacks[3];
                } else {
                    return false;
                }
                if (this.d.logLevel <= 2) {
                    this.d.log("Selected workArea " + os_08.stackIndex);
                }
                CardStack[] os_0Array = this.d.searchState.stackGroups[0].stacks;
                int n16 = this.d.searchState.stackGroups[0].stacks.length;
                int n17 = 0;
                while (n17 < n16) {
                    CardStack os_09 = os_0Array[n17];
                    if (os_09.runs.size() == 1 && os_09.topRun.cardCount == 1) {
                        this.d.complexity += this.fromSpacePenalty;
                    }
                    if (this.D <= 0) {
                        this.a(os_08, os_09, n2, n3);
                        ++n17;
                        continue;
                    }
                    break;
                }
            } else if (n2 == 7) {
                int n18;
                CardStack os_010;
                if (this.d.files.maxMoves < 999 && (this.d.files.b == 4 || this.d.files.b == 3)) {
                    return false;
                }
                int n19 = 13;
                int n20 = 13;
                CardStack[] os_0Array = this.d.searchState.stackGroups[2].stacks;
                int n21 = this.d.searchState.stackGroups[2].stacks.length;
                int n22 = 0;
                while (n22 < n21) {
                    os_010 = os_0Array[n22];
                    int n23 = os_010.foundationSuit;
                    n18 = os_010.getTopRank();
                    if (FreeCellSolver.c(n23)) {
                        if (n18 < n19) {
                            n19 = n18;
                        }
                    } else if (n18 < n20) {
                        n20 = n18;
                    }
                    ++n22;
                }
                if (this.d.logLevel <= 3) {
                    this.d.log("Lowest black on aces is " + n19 + " lowest red is " + n20);
                }
                os_0Array = this.d.searchState.stackGroups[2].stacks;
                n21 = this.d.searchState.stackGroups[2].stacks.length;
                n22 = 0;
                while (n22 < n21) {
                    os_010 = os_0Array[n22];
                    int n24 = os_010.foundationSuit;
                    n18 = os_010.getTopRank();
                    int n25 = 0;
                    if (n18 < 2) {
                        n25 = 1;
                    } else if (FreeCellSolver.c(n24)) {
                        if (n18 <= n20) {
                            n25 = 1;
                        }
                    } else if (n18 <= n19) {
                        n25 = 1;
                    }
                    if (n25 != 0) {
                        if (this.d.logLevel <= 3) {
                            this.d.log("Try and move card up to " + FreeCellSolver.f(n18) + " of " + FreeCellSolver.d(n24 * 100));
                        }
                        CardStack[] os_0Array5 = this.d.searchState.stackGroups[0].stacks;
                        n25 = this.d.searchState.stackGroups[0].stacks.length;
                        n18 = 0;
                        while (n18 < n25) {
                            CardStack os_011 = os_0Array5[n18];
                            bl = this.a(os_010, os_011, n2, n3);
                            if (bl) {
                                if (this.d.logLevel > 3) break;
                                this.d.log("Automatic ace move from stack " + os_011.stackIndex + " was productive");
                                break;
                            }
                            ++n18;
                        }
                        if (bl) break;
                        os_0Array5 = this.d.searchState.stackGroups[1].stacks;
                        n25 = this.d.searchState.stackGroups[1].stacks.length;
                        n18 = 0;
                        while (n18 < n25) {
                            CardStack os_012 = os_0Array5[n18];
                            bl = this.a(os_010, os_012, n2, n3);
                            if (bl) {
                                if (this.d.logLevel > 3) break;
                                this.d.log("Automatic ace move from work " + os_012.stackIndex + " was productive");
                                break;
                            }
                            ++n18;
                        }
                        if (bl) break;
                    }
                    ++n22;
                }
            } else {
                CardStack[] os_0Array = this.d.searchState.stackGroups[2].stacks;
                int n26 = this.d.searchState.stackGroups[2].stacks.length;
                int n27 = 0;
                while (n27 < n26) {
                    CardStack os_013 = os_0Array[n27];
                    if (this.D > 0) break;
                    if (this.d.logLevel <= 2) {
                        this.d.log("Try and move card run to ace of " + FreeCellSolver.d(os_013.foundationSuit * 100));
                    }
                    if (this.d.files.b == 4 && os_013.topRun != null && os_013.getTopRank() > 2) {
                        int n28 = os_013.stackIndex % 10;
                        if (n28 == antagonistFoundationIndexBySuit[this.d.files.d]) {
                            this.d.complexity += this.removeAntagonistSuitPenalty;
                            if (this.d.logLevel <= 3) {
                                this.d.log("Adjusted complexity by removeAntag to " + this.d.complexity);
                            }
                        } else if (n28 == targetFoundationIndexBySuit[this.d.files.d]) {
                            this.d.complexity += this.removeTargetSuitPenalty;
                            if (this.d.logLevel <= 3) {
                                this.d.log("Adjusted complexity by removeTargetSuit to " + this.d.complexity);
                            }
                        } else {
                            this.d.complexity += this.removeOtherSuitPenalty;
                            if (this.d.logLevel <= 3) {
                                this.d.log("Adjusted complexity by removeOtherSuit to " + this.d.complexity);
                            }
                        }
                    }
                    CardStack[] os_0Array6 = this.d.searchState.stackGroups[0].stacks;
                    int n29 = this.d.searchState.stackGroups[0].stacks.length;
                    int n30 = 0;
                    while (n30 < n29) {
                        CardStack os_014 = os_0Array6[n30];
                        if (this.D > 0 || (bl = this.a(os_013, os_014, n2, n3))) break;
                        ++n30;
                    }
                    if (bl) break;
                    os_0Array6 = this.d.searchState.stackGroups[1].stacks;
                    n29 = this.d.searchState.stackGroups[1].stacks.length;
                    n30 = 0;
                    while (n30 < n29) {
                        CardStack os_015 = os_0Array6[n30];
                        if (this.D > 0 || (bl = this.a(os_013, os_015, n2, n3))) break;
                        ++n30;
                    }
                    if (!bl) {
                        ++n27;
                        continue;
                    }
                    break;
                }
            }
        }
        return bl;
    }

    private boolean a(CardStack os_02, CardStack os_03, int n2, int n3) {
        if (os_02 == os_03) {
            return false;
        }
        if (os_03.topRun == null) {
            return false;
        }
        int n4 = os_03.topRun.cardCount;
        if (n3 > 0) {
            int n5 = n3 % 100;
            n3 = n3 / 100 % 100;
            if (os_02.stackIndex == n3 && os_03.stackIndex == n5) {
                return false;
            }
        }
        boolean bl = false;
        n3 = this.d.complexity;
        int n6;
        switch (n2) {
            case 1:
            case 7: {
                n6 = 1;
                break;
            }
            case 2: {
                n6 = 3;
                break;
            }
            case 3: {
                n6 = 2;
                break;
            }
            case 4: {
                n6 = 1;
                if (os_02.topRun == null) {
                    n6 = 2;
                }
                break;
            }
            case 5: {
                n6 = 6;
                break;
            }
            case 6:
            case 9: {
                n6 = 1;
                break;
            }
            case 8: {
                n6 = 1;
                if (os_02.topRun == null) {
                    n6 = 2;
                }
                break;
            }
            case 10: {
                n6 = 2;
                break;
            }
            default: {
                n6 = -1;
            }
        }
        int n7 = os_02.a(os_03, n6, false);
        if (n2 == 9 && n7 > 0 && n7 < os_03.topRun.cardCount) {
            Card nT2 = os_03.topRun.cards[os_03.topRun.cardCount - n7 - 1];
            if (this.d.files.b == 4) {
        if (nT2.suit == this.d.files.d) {
                    this.d.complexity += this.splitGivesSuitPenalty;
                    if (this.d.logLevel <= 3) {
                        this.d.log("Adjusted complexity by splitGivesSuit to " + this.d.complexity);
                    }
                } else {
                    this.d.complexity += this.splitDoesNotGiveSuitPenalty;
                    if (this.d.logLevel <= 3) {
                        this.d.log("Adjusted complexity by splitDoesNotGiveSuit to " + this.d.complexity);
                    }
                }
            } else if (this.d.searchState.stackGroups[2].stacks[0].getTopCardValue() + 1 == nT2.cardId || this.d.searchState.stackGroups[2].stacks[1].getTopCardValue() + 1 == nT2.cardId || this.d.searchState.stackGroups[2].stacks[2].getTopCardValue() + 1 == nT2.cardId || this.d.searchState.stackGroups[2].stacks[3].getTopCardValue() + 1 == nT2.cardId) {
                this.d.complexity += this.splitMatchesAcePenalty;
                if (this.d.logLevel <= 3) {
                    this.d.log("Adjusted complexity by splitMatchesAce to " + this.d.complexity);
                }
            }
        }
        if (this.d.files.b == 4 && (n2 == 6 || n2 == 9 || n2 == 4 || n2 == 2) && os_02.group.groupIndex == 0 && os_02.topRun != null && os_02.topRun.cards[os_02.topRun.cardCount - 1].suit == this.d.files.d) {
            this.d.complexity += this.joinOverTargetSuitPenalty;
            if (this.d.logLevel <= 3) {
                this.d.log("Adjusted complexity by joinOverSuit to " + this.d.complexity);
            }
        }
        if (n7 >= 0) {
            int n8;
            if ((n8 = n7 == 0 ? n4 : n7) > 1 && (n2 == 6 || n2 == 9 || n2 == 8 || n2 == 3 || n2 == 10 || n2 == 2)) {
                int n9 = this.d.searchState.stackGroups[1].emptyStackCount;
                int n10 = this.d.searchState.stackGroups[0].emptyStackCount;
                if (n6 == 2) {
                    --n10;
                }
                int n11 = (1 << n10) * (n9 + 1);
                if (this.d.logLevel <= 2) {
                    this.d.log("Workarea spaces " + n9 + " stack spaces " + n10 + " allow length " + n11);
                }
                if (n8 > n11) {
                    if (this.d.logLevel <= 2) {
                        this.d.log("Move of " + n7 + " denied because workarea spaces " + n9 + " and stack spaces " + n10);
                    }
                    n7 = -1;
                }
            }
            if (n7 >= 0 && (n8 == n4 || n2 != 6 && n2 != 8 && n2 != 2)) {
                int n12 = os_02.topRun != null ? 2 : 0;
                if ((n7 = os_02.a(os_03, n7, null)) >= 0) {
                    if (this.d.logLevel <= 2) {
                        this.d.log("Completed join with split of " + n7);
                    }
                    if (n8 != n4) {
                        n12 |= 1;
                    }
                    if (n2 == 7) {
                        n12 |= 16;
                    }
                    int n13 = Move.a(n12, n8, os_03, os_02);
                    this.d.searchState.moves[this.d.searchState.depth] = n13;
                    ++this.d.searchState.depth;
                    long l2 = this.computeStateHash();
                    if (n2 == 7 || !this.b(os_02, os_03)) {
                        if (n2 != 7) {
                            this.D = this.b(l2);
                        }
                        if (this.D < 0) {
                            this.a(l2);
                            bl = true;
                            n2 = 0;
                            Card nT3 = os_03.getTopCard();
                            CardStack os_04 = null;
                            if (nT3 != null && nT3.cardId == 0) {
                                n2 = 1;
                                os_04 = os_03;
                            } else {
                                CardRun ok_02 = (CardRun)os_03.runs.peekFirst();
                                if (ok_02 != null && ok_02.cards[0].cardId == 0 && os_03.getCardCount() < 12) {
                                    n2 = 1;
                                }
                            }
                            if (n2 != 0) {
                                if (this.d.logLevel <= 5) {
                                    this.d.log("Invoking play() due to unknown cards, stack " + os_03.stackIndex + " lastCard " + nT3 + " peek " + os_03.runs.peekFirst());
                                }
                                this.d.bridge.a(this.d.searchState, os_04, false, true, false);
                                this.d.sleepBriefly(1000L, "Wait for auto to complete");
                                this.d.bridge.c(os_03);
                                this.d.playbackMoveIndex = this.d.bridge.a(this.d.initialState.moves);
                                this.freeCellBridge.a(os_03, this.d.V);
                            }
                            this.search(n13, 0);
                        }
                        if (this.D >= 0) {
                            --this.D;
                        }
                    }
                    --this.d.searchState.depth;
                    os_02.b(os_03, n7, null);
                }
            }
        }
        this.d.complexity = n3;
        return bl;
    }

    @Override
    final int a(CardStack os_02) {
        HashMap hashMap = new HashMap(52);
        return this.a(hashMap, this.d.initialState.stackGroups[0], 1) + this.a(hashMap, this.d.initialState.stackGroups[1], 1) + this.a(hashMap, this.d.initialState.stackGroups[2], 1);
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
        if (nY2.stackGroups[2] == null) {
            return 0;
        }
        int n4 = 0;
        CardStack[] os_0Array = nY2.stackGroups[2].stacks;
        int n5 = os_0Array.length;
        for (int i = 0; i < n5; ++i) {
            CardStack os_02 = os_0Array[i];
            if (os_02.topRun == null || os_02.topRun.cardCount < n3 || n2 != 0 && os_02.topRun.cards[os_02.topRun.cardCount - 1].suit != n2) continue;
            ++n4;
        }
        return n4;
    }

    @Override
    final int a(GameState nY2) {
        if (nY2 == null) {
            return 999;
        }
        if (nY2.stackGroups[2] == null) {
            return 999;
        }
        if (this.d.files.maxMoves == 999) {
            boolean bl = true;
            CardStack[] os_0Array = nY2.stackGroups[0].stacks;
            int n2 = os_0Array.length;
            for (int i = 0; i < n2; ++i) {
                if (os_0Array[i].runs.size() <= 1) continue;
                bl = false;
                break;
            }
            if (bl) {
                if (this.d.logLevel <= 5) {
                    this.d.log("Freecell completed because stacks sequenced, depth " + nY2.depth);
                }
                return nY2.depth;
            }
        }
        if (this.d.files.b != 4) {
            if (this.d.files.b == 3) {
                int n3 = FreeCellSolver.b(nY2.stackGroups[2], this.d.files.h);
                int n4 = this.d.files.c * this.d.files.h - n3;
                return nY2.depth + n4;
            }
            return nY2.depth + 52 - nY2.stackGroups[2].countCards();
        }
        int n5 = FreeCellSolver.a(nY2.stackGroups[2], this.d.files.d);
        int n6 = 0;
        int n7 = this.d.files.d * 100 + n5 + 1;
        int n8 = this.d.files.d * 100 + this.d.files.c;
        CardStack[] os_0Array2 = nY2.stackGroups[0].stacks;
        int n9 = os_0Array2.length;
        for (int i = 0; i < n9; ++i) {
            for (Object object : os_0Array2[i].runs) {
                CardRun ok_02 = (CardRun)object;
                for (int j = 0; j < ok_02.cardCount; ++j) {
                    if (ok_02.cards[j].cardId < n7 || ok_02.cards[j].cardId > n8) continue;
                    ++n6;
                    if (j >= ok_02.cardCount - 1) continue;
                    ++n6;
                }
            }
        }
        CardStack[] os_0Array3 = nY2.stackGroups[1].stacks;
        int n10 = os_0Array3.length;
        for (int i = 0; i < n10; ++i) {
            int n11 = os_0Array3[i].b();
            if (n11 < n7 || n11 > n8) continue;
            ++n6;
        }
        return nY2.depth + n6;
    }

    @Override
    final boolean a(GameState nY2, int n2) {
        if (nY2 == null) {
            return false;
        }
        if (nY2.stackGroups[2] == null) {
            return false;
        }
        n2 = 1;
        CardStack[] os_0Array = nY2.stackGroups[0].stacks;
        int n3 = os_0Array.length;
        for (int i = 0; i < n3; ++i) {
            if (os_0Array[i].runs.size() <= 1) continue;
            n2 = 0;
            break;
        }
        return n2 != 0;
    }

    @Override
    final boolean loadStateFromLines(String string, String[] stringArray, int n2) {
        StackGroup ot_02 = this.d.initialState.stackGroups[0];
        int n3 = 7;
        boolean bl = false;
        int[] nArray = new int[]{7, 7, 7, 7, 6, 6, 6, 6};
        if (string == null) {
            if (n2 != 8) {
                this.d.invalidInput("FreeCell input file must have 7 rows of cards", false);
            }
        } else {
            bl = true;
            n3 = 0;
            String[] stringArray2 = string.split(":");
            try {
                for (int i = 0; i < 8; ++i) {
                    int n4 = Integer.parseInt(stringArray2[i]);
                    nArray[i] = n4;
                    if (n4 <= n3) continue;
                    n3 = n4;
                }
            }
            catch (NumberFormatException numberFormatException) {
                this.d.invalidInput("Error parsing Freecell options for partially completed deck", false);
            }
        }
        try {
            for (int i = 0; i < n3; ++i) {
                String[] stringArray3 = stringArray[i + 1].split(",");
                for (int j = 0; j < stringArray3.length; ++j) {
                    if (nArray[j] <= i) continue;
                    CardStack os_02 = ot_02.stacks[j];
                    String string2 = stringArray3[j];
                    int n5 = this.d.parseCardCode(string2);
                    this.g[i][j] = n5;
                    if (this.d.logLevel <= 2) {
                        this.d.log("Loading card " + n5 + " into stack " + j + " level " + i);
                    }
                    CardRun ok_02 = os_02.topRun;
                    CardRun ok_03 = new CardRun(this.b(os_02, n5));
                    if (ok_02 != null) {
                        int n6 = os_02.a(ok_02, ok_03, false, false);
                        if (n6 > 0) {
                            ok_02.a(ok_03, n6);
                        } else {
                            os_02.a(ok_03);
                        }
                    } else {
                        os_02.a(ok_03);
                    }
                }
            }
            if (bl) {
                String[] stringArray4 = stringArray[n3 + 1].split(",");
                for (int i = 0; i < 4; ++i) {
                    String string3 = stringArray4[i];
                    int n7 = this.d.parseCardCode(string3);
                    this.c[i] = n7;
                    if (n7 <= 0) continue;
                    CardStack os_03 = this.d.initialState.stackGroups[1].stacks[i];
                    os_03.a(new CardRun(this.b(os_03, n7)));
                }
                String[] stringArray5 = stringArray[n3 + 2].split(",");
                for (int i = 0; i < 4; ++i) {
                    String string4 = stringArray5[i];
                    int n8 = this.d.parseCardCode(string4);
                    this.b[i] = n8;
                    if (n8 <= 0) continue;
                    int n9 = n8 / 100;
                    int n10 = n8 % 100;
                    CardStack os_04 = this.d.initialState.stackGroups[2].stacks[i];
                    CardRun ok_04 = new CardRun(this.b(os_04, n9 * 100 + 1));
                    os_04.a(ok_04);
                    for (int j = 2; j <= n10; ++j) {
                        ok_04.a(new CardRun(this.b(os_04, n9 * 100 + j)), 1);
                    }
                }
            }
        }
        catch (Exception exception) {
            this.d.invalidInput("Error interpreting the card data.  Probably unexpected number of cards somewhere in the file.", false);
        }
        if (this.a((CardStack)null) != 52) {
            this.d.invalidInput("ERROR - Did not read 52 cards from the file", false);
        }
        return true;
    }

    @Override
    final void appendBoardState(StringBuffer stringBuffer) {
        int n2 = 0;
        boolean bl = false;
        stringBuffer.append(SolverContext.VARIANT_NAMES[this.d.variantId]);
        int n3 = 0;
        while (n3 < 4) {
            if (this.h[n3] > n2) {
                n2 = this.h[n3];
            }
            if (this.h[n3] != 7) {
                bl = true;
            }
            ++n3;
        }
        n3 = 4;
        while (n3 < 8) {
            if (this.h[n3] > n2) {
                n2 = this.h[n3];
            }
            if (this.h[n3] != 6) {
                bl = true;
            }
            ++n3;
        }
        n3 = 0;
        while (n3 < 4) {
            if (this.b[n3] != 0) {
                bl = true;
            }
            ++n3;
        }
        n3 = 0;
        while (n3 < 4) {
            if (this.c[n3] != 0) {
                bl = true;
            }
            ++n3;
        }
        if (bl) {
            stringBuffer.append(",");
            n3 = 0;
            while (n3 < 8) {
                stringBuffer.append("" + this.h[n3]);
                if (n3 != 7) {
                    stringBuffer.append(":");
                }
                ++n3;
            }
        }
        stringBuffer.append("\n# Stacks:\n");
        n3 = 0;
        while (n3 < n2) {
            int n4;
            int n5 = this.f;
            if (n3 == n2 - 1) {
                n4 = this.f - 1;
                while (n4 >= 0) {
                    if (this.g[n3][n4] != 0) {
                        n5 = n4 + 1;
                        break;
                    }
                    --n4;
                }
            }
            n4 = 0;
            while (n4 < n5) {
                stringBuffer.append(FreeCellSolver.i(this.g[n3][n4]));
                stringBuffer.append(",");
                ++n4;
            }
            stringBuffer.append("\n");
            ++n3;
        }
        if (bl) {
            stringBuffer.append("# Work:\n");
            n3 = 0;
            while (n3 < 4) {
                stringBuffer.append(FreeCellSolver.i(this.c[n3]));
                stringBuffer.append(",");
                ++n3;
            }
            stringBuffer.append("\n# Aces:\n");
            n3 = 0;
            while (n3 < 4) {
                stringBuffer.append(FreeCellSolver.i(this.b[n3]));
                stringBuffer.append(",");
                ++n3;
            }
        }
    }

    @Override
    final StringBuffer createStateHeader(String string, int n2) {
        return new StringBuffer(String.valueOf(string) + "[" + n2 + ":" + this.moveToAcesAttempts + "," + this.toSpaceAttempts + "," + this.fromSpaceAttempts + "," + this.moveToWorkAreaAttempts + "," + this.fromWorkAreaAttempts + "," + this.exposeAceAttempts + "," + this.alternatingJoinAttempts + "," + this.splitMatchAttempts + "," + 0 + "]: ");
    }

    @Override
    final void a(int n2) {
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
        while (n4 < 4) {
            if (this.c[n4] > 0) {
                ++n3;
                this.a(hashMap, this.c[n4], "workarea");
                if (this.d.logLevel <= 3) {
                    this.d.log("Add workarea card " + n4 + " to check:" + this.c[n4]);
                }
            }
            if (this.b[n4] > 0) {
                n2 = this.b[n4] % 100;
                int n5 = this.b[n4] / 100 * 100;
                n3 += n2;
                int n6 = 0;
                while (n6 < n2) {
                    int n7 = n5 + n6 + 1;
                    this.a(hashMap, n7, "aces");
                    if (this.d.logLevel <= 3) {
                        this.d.log("Add ace card " + n7 + " to check");
                    }
                    ++n6;
                }
            }
            ++n4;
        }
        return n3;
    }

    @Override
    final int[] d() {
        String string = "Unknown cards not expected in Freecell";
        SolverContext om_02 = this.d;
        om_02.invalidInput(string, false);
        return null;
    }

    @Override
    final int a(CardStack os_02, CardStack os_03, int n2) {
        int n3 = 0;
        if (os_03.topRun == null) {
            n3 = 1;
        }
        return this.b(os_02, os_03, n2, n3);
    }

    private int b(CardStack os_02, CardStack os_03, int n2, int n3) {
        if (os_02.group.groupIndex == 2) {
            return -1;
        }
        int n4 = -1;
        int n5 = os_02.topRun.cardCount;
        int n6 = os_03.group.groupIndex == 1 ? 6 : (os_03.group.groupIndex == 0 && os_03.topRun == null ? 2 : (os_02.runs.size() == 1 ? 3 : 1));
        if (n3 == 2 ? os_03.topRun == null : n3 == 1 && os_03.topRun != null) {
            return -1;
        }
        n3 = -1;
        if (os_03.group.groupIndex == 0 && os_03.topRun == null && n2 > 0) {
            if (n2 == n5) {
                n3 = 0;
            } else if (n2 > 0 && n2 < n5) {
                n3 = n2;
            }
        } else {
            n3 = os_03.a(os_02, n6, true);
        }
        if (n3 >= 0) {
            int n7 = n4 = n3 == 0 ? n5 : n3;
            if (n2 > 0 && n2 != n4) {
                return -1;
            }
            if (n4 > 1) {
                n2 = this.d.initialState.stackGroups[1].emptyStackCount;
                int n8 = this.d.initialState.stackGroups[0].emptyStackCount;
                if (n6 == 2) {
                    --n8;
                }
                n6 = (1 << n8) * (n2 + 1);
                if (this.d.logLevel <= 2) {
                    this.d.log("Workarea spaces " + n2 + " stack spaces " + n8 + " allow length " + n6);
                }
                if (n4 > n6) {
                    if (this.d.logLevel <= 2) {
                        this.d.log("Move of " + n3 + " denied because workarea spaces " + n2 + " and stack spaces " + n8);
                    }
                    return -1;
                }
            }
            int n9 = n2 = os_03.topRun != null ? 2 : 0;
            if (n4 < n5) {
                n2 |= 1;
            }
            n4 = Move.a(n2, n4, os_02, os_03);
            this.k(n4);
        }
        if (this.d.logLevel <= 5) {
            this.d.log(String.format("trymove returning %08x", n4));
        }
        return n4;
    }

    @Override
    final int a(Card nT2) {
        CardStack os_02 = nT2.stack;
        if (os_02.group.groupIndex == 2) {
            return -1;
        }
        int n2 = -1;
        for (CardStack os_03 : this.d.initialState.stackGroups[2].stacks) {
            if ((n2 = this.b(os_02, os_03, 1, 0)) > 0) {
                break;
            }
        }
        int n3 = os_02.topRun.cardCount;
        if (n2 <= 0) {
            CardStack[] os_0Array = this.d.initialState.stackGroups[0].stacks;
            int n4 = os_0Array.length;
            CardStack os_04;
            for (int i = 0; i < n4 && ((os_04 = os_0Array[i]) == os_02 || (n2 = this.b(os_02, os_04, n3, 2)) <= 0); ++i) {
            }
        }
        if (n2 <= 0) {
            CardStack[] os_0Array = this.d.initialState.stackGroups[0].stacks;
            int n5 = os_0Array.length;
            CardStack os_05;
            for (int i = 0; i < n5 && ((os_05 = os_0Array[i]) == os_02 || (n2 = this.b(os_02, os_05, -1, 0)) <= 0); ++i) {
            }
        }
        if (n2 <= 0) {
            CardStack[] os_0Array = this.d.initialState.stackGroups[0].stacks;
            int n6 = os_0Array.length;
            CardStack os_06;
            for (int i = 0; i < n6 && ((os_06 = os_0Array[i]) == os_02 || (n2 = this.b(os_02, os_06, -1, 1)) <= 0); ++i) {
            }
        }
        if (n2 <= 0) {
            for (CardStack os_07 : this.d.initialState.stackGroups[1].stacks) {
                if ((n2 = this.b(os_02, os_07, 1, 0)) > 0) {
                    break;
                }
            }
        }
        return n2;
    }

    @Override
    final int e() {
        int n2;
        int n3;
        CardStack os_02;
        StackGroup ot_02 = this.d.initialState.stackGroups[0];
        StackGroup ot_03 = this.d.initialState.stackGroups[1];
        StackGroup ot_04 = this.d.initialState.stackGroups[2];
        int n4 = -1;
        if (this.d.files.maxMoves < 999 && (this.d.files.b == 4 || this.d.files.b == 3)) {
            return -1;
        }
        int n5 = 13;
        int n6 = 13;
        CardStack[] os_0Array = ot_04.stacks;
        int n7 = ot_04.stacks.length;
        int n8 = 0;
        while (n8 < n7) {
            os_02 = os_0Array[n8];
            n3 = os_02.foundationSuit;
            n2 = os_02.getTopRank();
            if (FreeCellSolver.c(n3)) {
                if (n2 < n5) {
                    n5 = n2;
                }
            } else if (n2 < n6) {
                n6 = n2;
            }
            ++n8;
        }
        if (this.d.logLevel <= 3) {
            this.d.log("Lowest black on aces is " + n5 + " lowest red is " + n6);
        }
        os_0Array = ot_04.stacks;
        n7 = ot_04.stacks.length;
        n8 = 0;
        while (n8 < n7) {
            os_02 = os_0Array[n8];
            n3 = os_02.foundationSuit;
            n2 = os_02.getTopCardValue();
            int n9 = n2 % 100;
            ++n2;
            int n10 = 0;
            if (n9 < 2) {
                n10 = 1;
            } else if (FreeCellSolver.c(n3)) {
                if (n9 <= n6) {
                    n10 = 1;
                }
            } else if (n9 <= n5) {
                n10 = 1;
            }
            if (n10 != 0) {
                if (this.d.logLevel <= 3) {
                    this.d.log("Try and move card up to " + FreeCellSolver.f(n9) + " of " + FreeCellSolver.d(n3 * 100));
                }
                CardStack[] os_0Array2 = ot_02.stacks;
                n10 = ot_02.stacks.length;
                n3 = 0;
                while (n3 < n10) {
                    CardStack os_03 = os_0Array2[n3];
                    if (os_03.topRun != null) {
                        Card nT2 = os_03.topRun.cards[os_03.topRun.cardCount - 1];
                        if (nT2.suit == os_02.foundationSuit && (n2 == 1 && nT2.rank == 1 || os_03.topRun.cards[os_03.topRun.cardCount - 1].cardId == n2)) {
                            n4 = FreeCellSolver.c(os_03, os_02);
                            if (this.d.logLevel > 3) break;
                            this.d.log("Automatic ace move from stack " + os_03.stackIndex + " was productive");
                            break;
                        }
                    }
                    ++n3;
                }
                if (n4 > 0) break;
                os_0Array2 = ot_03.stacks;
                n10 = ot_03.stacks.length;
                n3 = 0;
                while (n3 < n10) {
                    CardStack os_04 = os_0Array2[n3];
                    if (os_04.topRun != null && os_04.topRun.cards[os_04.topRun.cardCount - 1].cardId == n2) {
                        n4 = FreeCellSolver.c(os_04, os_02);
                        if (this.d.logLevel > 3) break;
                        this.d.log("Automatic ace move from work " + os_04.stackIndex + " was productive");
                        break;
                    }
                    ++n3;
                }
                if (n4 > 0) break;
            }
            ++n8;
        }
        if (n4 > 0) {
            this.k(n4);
        }
        return n4;
    }

    private static int c(CardStack os_02, CardStack os_03) {
        int n2;
        int n3 = n2 = os_03.topRun != null ? 18 : 16;
        if (os_02.topRun.cardCount > 1) {
            n2 |= 1;
        }
        return Move.a(n2, 1, os_02, os_03);
    }
}




