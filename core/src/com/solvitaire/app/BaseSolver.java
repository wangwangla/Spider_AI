/*
 * Decompiled with CFR 0.152.
 */
package com.solvitaire.app;

import com.solvitaire.app.SolverBridge;
import com.solvitaire.app.Move;
import com.solvitaire.app.Card;
import com.solvitaire.app.GameState;
import com.solvitaire.app.CardRun;
import com.solvitaire.app.SolverContext;
import com.solvitaire.app.CardStack;
import com.solvitaire.app.StackGroup;
import com.solvitaire.app.UiStub;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.LongStream;

/*
 * Renamed from com.solvitaire.app.op
 */
public abstract class BaseSolver {
    SolverContext d;
    private int a = 0x100000;
    String e;
    int f;
    int[][] g;
    int[] h;
    int[] i;
    int[] j;
    double[] k;
    int l;
    int m;
    int n;
    int o;
    int suitCount;
    private long b;
    private long c;
    private int H;
    private String I;
    transient private boolean J;
    private int[] K = null;
    boolean q = false;
    private int[] L = new int[414];
    private int M;
    private int N;
    private int O;
    private int P;
    int[][] r;
    int dealAreaCardCount;
    int stockCardCount;
    boolean u;
    int v;
    int maxSearchDepth = 298;
    private int statusUpdateCounter = 0;
    int searchCreditLimit;
    int[][] y;
    Card[] z;
    int A;
    private HashMap[] R = new HashMap[10];
    private HashMap[] S = new HashMap[10];
    boolean B = false;
    boolean C = true;
    int D = -1;
    private int deepestRecursionDepth;
    private int deepestRecursionComplexity;
    private long[] V;
    private long[] W;
    boolean E;
    int F;
    int G;

    abstract String getSolverName();

    abstract StringBuffer createStateHeader(String var1, int var2);

    abstract boolean initializeSolver();

    abstract void search(int var1, int var2);

    abstract long computeStateHash();

    abstract boolean loadStateFromLines(String var1, String[] var2, int var3);

    abstract void appendBoardState(StringBuffer var1);

    abstract void a(int var1);

    abstract boolean a(CardStack var1, CardStack var2);

    abstract int a(HashMap var1);

    abstract void dumpState(int var1, boolean var2);

    abstract int a(CardStack var1);

    abstract int[] d();

    abstract boolean a(GameState var1, int var2);

    BaseSolver(SolverContext om_02, int n2) {
        super();
        this.d = om_02;
        this.d.solver = this;
        this.searchCreditLimit = n2;
        Random random = new Random(314159265358979323L);
        LongStream longStream = random.longs(150L, 1L, 1000000000000L);
        this.V = longStream.toArray();
        longStream = random.longs(150L, 1L, 1000000000000L);
        this.W = longStream.toArray();
        boolean[] blArray = BaseSolver.n(500);
        int n3 = 3;
        for (int n4 = 1; n4 < 5; ++n4) {
            for (int n5 = 1; n5 < 14; ++n5) {
                while (!blArray[n3]) {
                    ++n3;
                }
                this.L[n4 * 100 + n5] = n3++;
            }
        }
        while (!blArray[n3]) {
            ++n3;
        }
        this.M = n3;
        while (!blArray[n3]) {
            ++n3;
        }
        this.N = n3;
        while (!blArray[n3]) {
            ++n3;
        }
        this.O = n3;
        while (!blArray[n3]) {
            ++n3;
        }
        this.P = n3;
    }

    private int l(int n2) {
        if (n2 > 2) {
            return this.a;
        }
        if (n2 == 2) {
            return this.a / 2;
        }
        if (n2 == 1) {
            return this.a / 8;
        }
        return this.a / 64;
    }

    final void initializeBaseState() {
        this.B = false;
        this.d.abortAllReads = false;
        this.d.searchStepCount = 0L;
        if (this.d.solverMode != 3) {
            this.d.files.maxMoves = 999;
        }
        this.z = new Card[this.o];
        int n2 = 0;
        while (n2 < this.o) {
            this.z[n2] = new Card(this.d);
            ++n2;
        }
        this.A = 0;
        this.u = true;
        this.d.searchCredit = 0;
        this.d.playbackMoveIndex = 0;
        if (this.d.logLevel <= 5) {
            this.d.log("In baseinit, set recursiondepth and playlocation to 0");
        }
        if (this.d.initialState != null) {
            this.d.initialState.reset();
        }
        this.d.searchState = null;
        this.suitCount = 4;
        this.F = 0;
        this.G = 0;
        this.d.foundCompleteSolution = false;
        this.E = false;
        this.H = 200;
        if (this.d.files.maxMoves < 200) {
            this.H = this.d.files.maxMoves + 2;
        }
        switch (this.d.files.b) {
            case 2: {
                if (this.d.files.e <= 1 || this.f()) break;
                this.d.fail("Multiple board challenges are not supported<br>for " + this.getSolverName());
                return;
            }
            case 3: {
                BaseSolver op_02 = this;
                if (op_02.a(op_02.d.initialState, true, 0, 0) >= 0) break;
                this.d.fail("Clearing a specific number of cards<br> is not supported for " + this.getSolverName());
                return;
            }
            case 4: {
                BaseSolver op_03 = this;
                if (op_03.a(op_03.d.initialState, true, 0, 0) >= 0) break;
                this.d.fail("Clearing a specific card<br>is not supported for " + this.getSolverName());
                return;
            }
            case 5: {
                BaseSolver op_04 = this;
                if (op_04.b(op_04.d.initialState, true) >= 0) break;
                this.d.fail("Clearing a specific number of stacks<br>is not supported for " + this.getSolverName());
                return;
            }
            case 6: {
                BaseSolver op_05 = this;
                if (op_05.a(op_05.d.initialState, true) >= 0) break;
                this.d.fail("Scoring challenges are not supported for " + this.getSolverName());
            }
        }
    }

    final void solve() {
        Object object;
        block60: {
            this.b = System.currentTimeMillis();
            if (this.d.solverMode != 4) {
                int n2 = 0;
                n2 = 5;
                object = this;
                MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
                long l2 = memoryUsage.getMax();
                long l3 = memoryUsage.getUsed();
                if (l2 > 8000000000L) {
                    ((BaseSolver)object).a = 0x200000;
                    if (((BaseSolver)object).d.logLevel <= 5) {
                        ((BaseSolver)object).d.log("Max heap memory: " + l2 + " used: " + l3 + " bucket size: " + ((BaseSolver)object).a);
                    }
                } else if (l2 > 4000000000L) {
                    ((BaseSolver)object).a = 0x180000;
                    if (((BaseSolver)object).d.logLevel <= 5) {
                        ((BaseSolver)object).d.log("Max heap memory: " + l2 + " used: " + l3 + " bucket size: " + ((BaseSolver)object).a);
                    }
                } else if (l2 > 2000000000L) {
                    ((BaseSolver)object).a = 786432;
                    if (((BaseSolver)object).d.logLevel <= 5) {
                        ((BaseSolver)object).d.log("Max heap memory: " + l2 + " used: " + l3 + " bucket size: " + ((BaseSolver)object).a);
                    }
                } else if (l2 > 1000000000L) {
                    ((BaseSolver)object).a = 393216;
                    if (((BaseSolver)object).d.logLevel <= 5) {
                        ((BaseSolver)object).d.log("Max heap memory: " + l2 + " used: " + l3 + " bucket size: " + ((BaseSolver)object).a);
                    }
                } else if (l2 > 500000000L) {
                    ((BaseSolver)object).a = 196608;
                    if (((BaseSolver)object).d.logLevel <= 5) {
                        ((BaseSolver)object).d.log("Max heap memory: " + l2 + " used: " + l3 + " bucket size: " + ((BaseSolver)object).a);
                    }
                } else if (l2 > 250000000L) {
                    ((BaseSolver)object).a = 98304;
                    if (((BaseSolver)object).d.logLevel <= 5) {
                        ((BaseSolver)object).d.log("Max heap memory: " + l2 + " used: " + l3 + " bucket size: " + ((BaseSolver)object).a);
                    }
                } else {
                    ((BaseSolver)object).d.fail("ERROR<br>System has insufficient available RAM (" + l2 / 1024000L + " megabytes)<br>Solitaire Solver would run too slowly");
                }
            }
            boolean bl = false;
            if (this.initializeSolver()) {
                String string = "n/a";
                if (this.d.fontStats != null && this.d.fontStats.font != null) {
                    string = this.d.fontStats.font.name;
                }
                if (this.d.logLevel <= 9) {
                    object = this.d;
                    object = this.d;
                    this.d.log("*** " + this.getSolverName() + " initialisation complete, font " + string + ", mode " + SolverContext.INPUT_SOURCE_MODE_NAMES[((SolverContext)object).inputSourceMode] + "," + SolverContext.SOLVER_MODE_NAMES[((SolverContext)object).solverMode] + " (Solvitaire version " + "5.1.2" + " on " + new Date() + ")");
                }
                this.C = false;
            } else {
                this.d.log("*** " + this.getSolverName() + " initialisation failed for game mode " + this.d.solverMode);
                this.C = false;
                if (this.d.aN) {
                    bl = true;
                    this.d.a(17, true);
                    this.d.b(90, 300);
                    this.d.a(17, false);
                } else {
                    return;
                }
            }
            if (bl) break block60;
            int n3 = 5;
            UiStub oz_02 = this.d.ui;
            oz_02.showStatus(n3, null);
            this.d.sleepBriefly(100L, "Prevent tight loop");
            this.d.k();
            this.maxSearchDepth = 298;
            if (this.d.t && this.d.solverMode != 1 && this.d.solverMode != 4) {
                this.d.ui.showScreen(3);
                if (this.d.inputSourceMode == 2) {
                    this.d.stats.a(SolverContext.VARIANT_NAMES[this.d.variantId], this.d.bg, this.d.bf);
                    this.d.stats.b();
                }
            }
            while (this.d.searchCredit > -this.searchCreditLimit) {
                block63: {
                    block62: {
                        this.d.h();
                        this.d.b("processPause", true);
                        if (this.d.solverMode == 4) {
                            this.B = true;
                            break block62;
                        }
                        if (this.d.logLevel <= 4) {
                            this.d.log("In process, entering solve loop");
                        }
                        this.d.ai = true;
                        while (this.d.searchCredit > -this.searchCreditLimit) {
                            n3 = 0;
                            while (n3 < 10) {
                                this.R[n3] = new HashMap(this.l(n3));
                                this.S[n3] = new HashMap(this.l(n3));
                                ++n3;
                            }
                            this.d.complexity = this.d.searchCredit;
                            this.D = -1;
                            this.deepestRecursionDepth = 0;
                            this.deepestRecursionComplexity = 0;
                            if (this.K != null && this.K[0] > 0) {
                                this.K[0] = 0;
                            }
                            this.d.ah = true;
                            this.search(-1, 0);
                            this.d.ah = false;
                            if (this.B || this.D > 0) break;
                            if (this.d.logLevel <= 4) {
                                this.d.log("*** Deepest recursion for credit " + this.d.searchCredit + " was " + this.deepestRecursionDepth + " with complexity " + this.deepestRecursionComplexity);
                            }
                            this.d.searchCredit -= 30;
                        }
                        if (this.B || this.D > 0) break block63;
                        if (this.d.logLevel <= 5) {
                            this.d.log("Credit expired and solve not flagged, do final check");
                        }
                        this.B = this.a(this.d.searchState, 0, true) == 2;
                    }
                }
                n3 = 0;
                while (n3 < 10) {
                    this.R[n3] = null;
                    this.S[n3] = null;
                    ++n3;
                }
                System.gc();
                if (this.B) {
                    n3 = 1;
                    if (this.d.t && !this.d.stats.keepCurrentStats && this.d.inputSourceMode == 2 && !this.d.ad) {
                        n3 = this.d.ui.confirm("Statistics", "<html>I have found a solution.  Do you want to see it?<br>The statistics for this game will be discarded if you do.<br>OK?</html>") ? 1 : 0;
                    }
                    if (n3 == 0) {
                        this.d.ui.showScreen(15);
                        this.d.b(8);
                        this.d.searchCredit = 0;
                        this.B = false;
                        continue;
                    }
                    this.d.stats.keepCurrentStats = true;
                    if (this.d.logLevel <= 5) {
                        this.d.log("Play solution");
                    }
                    this.d.initialState.moveAnnotations = Arrays.copyOf(this.d.bestSolutionState.moveAnnotations, this.d.bestSolutionState.moveAnnotations.length);
                    this.d.bridge.a(this.d.bestSolutionState, null, true, true, false);
                    if (!this.d.Y) {
                        if (this.d.logLevel > 5) break;
                        this.d.log("Solved so exit process loop");
                        break;
                    }
                    if (this.d.logLevel <= 5) {
                        this.d.log("Playback aborted, stay in play loop");
                    }
                    this.d.Y = false;
                    this.d.ui.showScreen(15);
                    this.d.b(8);
                    this.d.searchCredit = 0;
                    continue;
                }
                if (!this.d.t) continue;
                if (this.d.logLevel <= 5) {
                    this.d.log("Exited solve loop without solution");
                }
                if (this.D > 0) {
                    this.d.log("The user aborted the solve so go back to user moves");
                    this.D = -1;
                    this.d.ui.showScreen(15);
                    this.d.b(8);
                    this.d.searchCredit = 0;
                    continue;
                }
                if (this.d.searchState.depth > 0) {
                    this.d.ui.showScreen(16);
                    this.d.searchCredit = 0;
                    continue;
                }
                this.d.ui.showScreen(17);
                this.d.updateStatus("failedSolve");
            }
            if (!this.d.t) {
                if (this.d.solverMode == 0 || this.d.ad && (this.d.solverMode == 4 || this.d.solverMode == 3)) {
                    if (!this.B && this.d.solverMode == 0) {
                        this.b(true);
                        if (!this.d.az) {
                            this.d.fail("Insoluble game. (See option to skip)");
                        } else {
                            this.d.table.p();
                        }
                    }
                    if (this.d.logLevel <= 5) {
                        this.d.log("***About to skip dialogs (and ads:" + (this.d.solverMode == 0) + ")");
                    }
                    this.d.table.a(this.d.solverMode == 0, false);
                    this.d.ui.dialog.reset();
                    if (this.d.logLevel <= 5) {
                        this.d.log("***Skipped dialogs, and ads");
                    }
                }
            } else if (this.B) {
                this.d.n();
            }
        }
        if (this.d.aN && !this.d.t) {
            this.d.b(118, 500);
            this.d.a(16, true);
            this.d.b(9, 300);
            this.d.b(9, 300);
            int n4 = 0;
            while (n4 < this.d.aO % 15) {
                int n5 = 39;
                object = this.d;
                ((SolverContext)object).b(39, 120);
                ++n4;
            }
            ++this.d.aO;
            this.d.b(32, 400);
            this.d.b(8, 700);
            this.d.a(16, false);
        }
        this.d.bestSolutionState.reset();
        if (this.d.logLevel <= 6) {
            this.d.log("*** Exit from process ***");
        }
    }

    private boolean n() {
        if (this.d.searchState.depth > 3 && this.d.searchState.depth == this.K[0] + 1) {
            int n2 = this.d.searchState.depth - 4;
            while (n2 < this.d.searchState.depth && n2 < this.K.length - 1) {
                if (!Move.a(Move.b(this.K[n2 + 1]), this.d.searchState.moves[n2])) {
                    return false;
                }
                ++n2;
            }
            return true;
        }
        return false;
    }

    private boolean o() {
        if (this.K[0] == -2) {
            if (this.d.searchState.depth <= this.K[1]) {
                return false;
            }
            this.K[1] = this.d.searchState.depth;
            this.d.log("Found longer solution");
            this.b(9);
            this.dumpState(9, false);
            if (this.d.t) {
                this.d.bridge.a(this.d.searchState, null, false, false, false);
            }
        } else if (this.K[0] == -1) {
            if (this.d.searchState.depth < this.K.length - 1) {
                return false;
            }
            int n2 = 1;
            while (n2 < this.K.length) {
                if (!Move.a(Move.b(this.K[n2]), this.d.searchState.moves[this.d.searchState.depth - this.K.length + n2])) break;
                ++n2;
            }
            if (n2 == this.K.length) {
                this.b(9);
                this.d.log("Found segment");
            }
        } else {
            int n3 = 0;
            while (n3 < this.d.searchState.depth && n3 < this.K.length - 1) {
                if (!Move.a(Move.b(this.K[n3 + 1]), this.d.searchState.moves[n3])) break;
                ++n3;
            }
            if (n3 >= this.K[0]) {
                if (n3 > this.K[0]) {
                    BaseSolver op_02 = this;
                    this.d.log("Approaching solution to " + n3 + " of " + (this.K.length - 1) + " dealindex " + this.d.searchState.dealIndex + " score " + op_02.a(op_02.d.searchState, false));
                    this.dumpState(5, false);
                    if (this.d.logLevel <= 5) {
                        this.d.log("State hash " + this.computeStateHash());
                    }
                    this.b(9);
                    this.K[0] = n3;
                }
                if (n3 == this.K.length - 1 && n3 == this.d.searchState.depth) {
                    this.d.log("Hit end of known solution");
                    return true;
                }
            } else if (n3 < this.K[0] && this.K[0] < 1000) {
                this.b(9);
                this.d.log("@@@ Backing out of solution");
                this.K[0] = this.K[0] + 1000;
            }
        }
        return false;
    }

    final long a(CardStack os_02, long l2, boolean bl, boolean bl2) {
        if (bl) {
            this.m = 0;
            l2 = 0L;
        }
        if (os_02.runs.size() == 0) {
            l2 += (long)this.N;
        } else {
            for (Object object : os_02.runs) {
                CardRun ok_02 = (CardRun)object;
                int n2 = 0;
                while (n2 < ok_02.cardCount) {
                    int n3 = ok_02.cards[n2].cardId;
                    if (!os_02.hasKnownCards()) {
                        n3 = this.P;
                    }
                    if (n3 == 0) {
                        n3 = this.O;
                    }
                    l2 = bl ? (l2 += this.a(n3, l2, ok_02.faceDown)) : (l2 += this.a(n3, os_02.stackIndex, l2, ok_02.faceDown));
                    ++n2;
                }
            }
        }
        if (!bl) {
            l2 += (long)this.M * this.V[this.m];
            ++this.m;
        }
        return l2;
    }

    final long a(int n2, int n3, long l2, boolean bl) {
        if (n2 <= 0) {
            this.d.fail("Logic error: trying to hash invalid card");
        }
        l2 = (long)this.L[n2] * (this.V[this.m] + (l2 & Integer.MAX_VALUE)) * (long)(n3 + 1);
        if (bl) {
            l2 <<= 1;
        }
        ++this.m;
        return l2;
    }

    private long a(int n2, long l2, boolean bl) {
        if (n2 <= 0) {
            this.d.fail("Logic error: trying to hash invalid card");
        }
        l2 = (long)this.L[n2] * (this.W[this.m] + (l2 & Integer.MAX_VALUE));
        if (bl) {
            l2 <<= 1;
        }
        ++this.m;
        return l2;
    }

    final void b(int n2) {
        this.a(n2, this.d.searchState, "Work moves");
    }

    final void a(int n2, GameState nY2, String string) {
        if (n2 >= this.d.logLevel) {
            StringBuffer stringBuffer = this.createStateHeader(string, nY2.depth);
            int n3 = 0;
            while (n3 < nY2.depth) {
                stringBuffer.append(Move.a(nY2.moves[n3]));
                stringBuffer.append(",");
                ++n3;
            }
            this.d.log(stringBuffer.toString());
        }
    }

    final void a(int n2, StackGroup ot_02) {
        if (n2 < this.d.logLevel) {
            return;
        }
        n2 = 0;
        while (n2 < ot_02.stacks.length) {
            StringBuffer stringBuffer = new StringBuffer(String.valueOf(ot_02.name) + " stack " + n2 + ": ");
            CardStack os_02 = ot_02.stacks[n2];
            for (Object object : os_02.runs) {
                CardRun ok_02 = (CardRun)object;
                int n3 = 0;
                while (n3 < ok_02.cardCount) {
                    stringBuffer.append("" + ok_02.cards[n3]);
                    if (n3 < ok_02.cardCount - 1) {
                        stringBuffer.append("+");
                    }
                    ++n3;
                }
                stringBuffer.append(" ");
            }
            this.d.log(stringBuffer.toString());
            ++n2;
        }
    }

    final boolean loadCheckpointState() {
        Object object;
        int n2;
        boolean bl = true;
        if (this.d.logLevel <= 5) {
            this.d.log("Into loadCheckpoint for game mode " + this.d.solverMode);
        }
        if (this.d.solverMode == 0 || this.d.solverMode == 1 || this.d.solverMode == 6) {
            if (this.d.solverMode == 0) {
                this.d.files.setOutputDirectory(this.e, false);
            }
            n2 = this.d.files.getEndFileIndex();
            if (this.d.solverMode == 1) {
                int n3 = this.d.files.getCurrentFileIndex(false);
                if (this.d.logLevel <= 4) {
                    this.d.log("All cards request, next checkpoint " + n3 + " endFile " + n2);
                }
                if (n3 >= n2) {
                    this.d.ui.showScreen(18);
                    if (this.d.bk > 0) {
                        this.d.writeTextFile(String.valueOf(this.d.files.outputDirectory) + "difflog.txt", this.I, true);
                    }
                    this.d.b(14);
                    this.d.updateStatus("Run complete");
                } else {
                    if (n3 == 0) {
                        this.I = "";
                    }
                    String string = null;
                    while (this.d.files.getCurrentFileIndex(false) <= n2) {
                        string = this.d.files.getInputFileName();
                        if (this.d.logLevel <= 4) {
                            this.d.log("Testing for card file " + string);
                        }
                        object = new File(String.valueOf(this.d.files.outputDirectory) + string);
                        if (this.d.solverMode != 1 || ((File)object).exists()) break;
                        this.d.files.setCurrentFileIndex(this.d.files.getCurrentFileIndex(false) + 1, true);
                    }
                    if (this.d.files.getCurrentFileIndex(false) > n2) {
                        this.d.ui.showScreen(18);
                        this.d.b(14);
                        this.d.updateStatus("Run complete");
                    }
                    this.d.ui.statusLabel.setText("File: " + string);
                    this.d.board.repaintBoard();
                    this.d.ui.repaintSurface.repaint();
                    if (this.d.logLevel <= 9) {
                        this.d.log("***Loading history file " + string);
                    }
                }
            } else {
                this.d.files.setEndFileIndex(n2 + 1);
                bl = false;
            }
        } else if (this.d.solverMode == 3) {
            if (this.d.t) {
                bl = true;
                if (this.d.logLevel <= 5) {
                    this.d.log("Capture request in standalone mode so set readcards to true");
                }
            } else {
                if (this.d.logLevel <= 5) {
                    this.d.log("Regular capture so cards come from the screen, readCards=false");
                }
                bl = false;
            }
        } else if (this.d.solverMode == 4) {
            String string = this.d.files.getPlaybackFileName();
            if (string == null) {
                this.d.fail("No solution exists for " + this.d.files.getInputFileName());
            }
            String string2 = this.d.readTextFile(String.valueOf(this.d.files.outputDirectory) + string);
            if (this.d.logLevel <= 5) {
                this.d.log("Playback mode so reading file " + string + " gave solution:" + string2);
            }
            String[] stringArray3 = string2.split(",");
            this.d.bestSolutionState.reset();
            n2 = 0;
            while (n2 < stringArray3.length) {
                int n4;
                if (stringArray3[n2].startsWith("\r") || stringArray3[n2].startsWith("\n")) break;
                stringArray3[n2] = stringArray3[n2].trim();
                if (stringArray3[n2].contains(":")) {
                    String[] stringArray = stringArray3[n2].split(":");
                    stringArray3[n2] = stringArray[0];
                    this.d.bestSolutionState.moveAnnotations[n2] = Integer.parseInt(stringArray[1].trim()) * 10000;
                    boolean bl2 = false;
                    if (this.d.bestSolutionState.moveAnnotations[n2] < 0) {
                        bl2 = true;
                        this.d.bestSolutionState.moveAnnotations[n2] = -this.d.bestSolutionState.moveAnnotations[n2];
                    }
                    if (stringArray.length > 2) {
                        int n5 = n2;
                        this.d.bestSolutionState.moveAnnotations[n5] = this.d.bestSolutionState.moveAnnotations[n5] + Integer.parseInt(stringArray[2].trim()) * 100;
                        if (stringArray.length > 3) {
                            int n6 = n2;
                            this.d.bestSolutionState.moveAnnotations[n6] = this.d.bestSolutionState.moveAnnotations[n6] + Integer.parseInt(stringArray[3].trim());
                        } else {
                            int n7 = n2;
                            this.d.bestSolutionState.moveAnnotations[n7] = this.d.bestSolutionState.moveAnnotations[n7] + 99;
                        }
                    } else {
                        int n8 = n2;
                        this.d.bestSolutionState.moveAnnotations[n8] = this.d.bestSolutionState.moveAnnotations[n8] + 9999;
                    }
                    if (bl2) {
                        this.d.bestSolutionState.moveAnnotations[n2] = -this.d.bestSolutionState.moveAnnotations[n2];
                    }
                } else {
                    this.d.bestSolutionState.moveAnnotations[n2] = 0;
                }
                int n9 = Integer.parseInt(stringArray3[n2]);
                this.d.bestSolutionState.moves[n2] = n4 = Move.b(n9);
                this.d.bestSolutionState.depth = n2 + 1;
                ++n2;
            }
            this.B = true;
        }
        if (bl) {
            String[] stringArray = this.d.readAllLines(String.valueOf(this.d.files.outputDirectory) + this.d.files.getInputFileName());
            if (stringArray == null) {
                return false;
            }
            String[] stringArray2 = stringArray[0].split(",");
            object = stringArray2.length < 2 ? null : stringArray2[1];
            n2 = 0;
            while (n2 < stringArray.length) {
                if (stringArray[n2] == null) break;
                ++n2;
            }
            if (n2 < 6) {
                this.d.fail("Input file has too few lines");
            } else if (!this.loadStateFromLines((String)object, stringArray, n2)) {
                return false;
            }
            if (this.d.t && this.d.board != null && this.d.board.repaintStub != null) {
                this.d.board.repaintStub.repaint();
            }
        }
        return true;
    }

    private void b(GameState nY2) {
        StringBuffer stringBuffer = new StringBuffer();
        int n2 = 0;
        while (n2 < nY2.depth) {
            stringBuffer.append(Move.a(nY2.moves[n2]));
            stringBuffer.append(",");
            ++n2;
        }
        if (this.d.files != null) {
            this.d.writeTextFile(String.valueOf(this.d.files.outputDirectory) + this.d.files.getSolutionFileName(), stringBuffer.toString(), true);
        }
    }

    private void a(String string) {
        StringBuffer stringBuffer = new StringBuffer();
        this.appendBoardState(stringBuffer);
        SolverContext.ensureDirectory(string);
        this.d.writeTextFile(String.valueOf(string) + this.d.files.getInputFileName(), stringBuffer.toString(), true);
    }

    final String b(boolean bl) {
        String string;
        if (bl) {
            string = String.valueOf(this.d.files.outputDirectory) + "debug" + File.separator;
            this.a(string);
        } else {
            if (this.d.bestSolutionState.depth == 0) {
                this.d.log("No best moves available so just writing current working moves");
                BaseSolver op_02 = this;
                op_02.b(op_02.d.searchState);
            } else {
                BaseSolver op_03 = this;
                op_03.b(op_03.d.bestSolutionState);
            }
            string = this.d.files.outputDirectory;
            if (!this.d.t && this.d.solverMode != 4) {
                BaseSolver op_04 = this;
                op_04.a(op_04.d.files.outputDirectory);
            }
        }
        return string;
    }

    static boolean c(int n2) {
        return n2 == 1 || n2 == 4;
    }

    static String d(int n2) {
        switch (n2 /= 100) {
            case 1: {
                return "Spade";
            }
            case 2: {
                return "Heart";
            }
            case 3: {
                return "Diamond";
            }
            case 4: {
                return "Club";
            }
        }
        return "Unknown";
    }

    private static String m(int n2) {
        switch (n2) {
            case 1: {
                return "s";
            }
            case 2: {
                return "h";
            }
            case 3: {
                return "d";
            }
            case 4: {
                return "c";
            }
        }
        return "?";
    }

    static String e(int n2) {
        switch (n2) {
            case 1: {
                return "\u2660";
            }
            case 2: {
                return "\u2665";
            }
            case 3: {
                return "\u2666";
            }
            case 4: {
                return "\u2663";
            }
        }
        return "?";
    }

    static String f(int n2) {
        switch (n2 %= 100) {
            case 1: {
                return "Ace";
            }
            case 2: {
                return "Two";
            }
            case 3: {
                return "Three";
            }
            case 4: {
                return "Four";
            }
            case 5: {
                return "Five";
            }
            case 6: {
                return "Six";
            }
            case 7: {
                return "Seven";
            }
            case 8: {
                return "Eight";
            }
            case 9: {
                return "Nine";
            }
            case 10: {
                return "Ten";
            }
            case 11: {
                return "Jack";
            }
            case 12: {
                return "Queen";
            }
            case 13: {
                return "King";
            }
        }
        return "empty/invalid";
    }

    static String g(int n2) {
        if (n2 > 1 && n2 < 10) {
            return "" + n2;
        }
        switch (n2) {
            case 1: {
                return "A";
            }
            case 10: {
                return "10";
            }
            case 11: {
                return "J";
            }
            case 12: {
                return "Q";
            }
            case 13: {
                return "K";
            }
        }
        return "?";
    }

    static String h(int n2) {
        return String.valueOf(BaseSolver.f(n2)) + " of " + BaseSolver.d(n2) + "s";
    }

    static String i(int n2) {
        return String.valueOf(BaseSolver.g(n2 % 100)) + BaseSolver.m(n2 / 100);
    }

    static String b(Card nT2) {
        return String.valueOf(BaseSolver.g(nT2.rank)) + BaseSolver.m(nT2.suit);
    }

    final int a(HashMap hashMap, int n2) {
        int n3;
        Integer n4;
        Integer n5 = n2;
        if (n2 == -1) {
            this.d.fail("ERROR - Card could not be identified");
        }
        if ((n4 = (Integer)hashMap.get(n5)) == null) {
            n3 = 1;
        } else {
            n3 = n4;
            ++n3;
        }
        hashMap.put(n5, n3);
        return n3;
    }

    final int a(HashMap hashMap, StackGroup ot_02, int n2) {
        int n3 = 0;
        CardStack[] os_0Array = ot_02.stacks;
        int n4 = ot_02.stacks.length;
        int n5 = 0;
        while (n5 < n4) {
            CardStack os_02 = os_0Array[n5];
            int n6 = n2;
            int n7 = 0;
            int n8 = 0;
            for (Object object : os_02.runs) {
                CardRun ok_02 = (CardRun)object;
                int n9 = 0;
                while (n9 < ok_02.cardCount) {
                    if (ok_02.cards[n9].cardId != 0) {
                        if (this.d.logLevel <= 0) {
                            this.d.log("Testing stack " + os_02.stackIndex + " run " + n7 + " entry " + n9 + " card " + ok_02.cards[n9]);
                        }
                        ++n7;
                        int n10 = this.a(hashMap, ok_02.cards[n9].cardId);
                        if (n10 > n6) {
                            this.d.fontStats.reset();
                            Card nT2 = ok_02.cards[n9];
                            Card nT3 = nT2;
                            nT3 = ok_02.cards[n9];
                            this.d.fail("ERROR - Too many " + BaseSolver.f(nT2.cardId) + " of " + BaseSolver.d(nT3.cardId) + "s in the deck");
                        }
                        ++n8;
                    }
                    ++n9;
                }
            }
            n3 += n8;
            ++n5;
        }
        if (this.d.logLevel <= 3) {
            this.d.log("Numcards after stack " + ot_02.name + " is " + n3);
        }
        return n3;
    }

    final boolean a(CardStack os_02, int n2) {
        if (os_02.topRun != null && os_02.topRun.cardCount == 1 && os_02.topRun.cards[0].cardId == 0) {
            BaseSolver op_02 = this;
            int n3 = n2;
            GameState nY2 = op_02.d.searchState;
            BaseSolver op_03 = op_02;
            if (op_02.a(nY2, n3, false) != 0) {
                if (this.d.logLevel <= 4) {
                    this.d.log("Exposing a card would give a solution, so do not read");
                }
                return true;
            }
            if (this.d.abortAllReads) {
                if (this.d.logLevel <= 5) {
                    this.d.log("Abort all reads so returning");
                }
                return false;
            }
            int n4 = this.e(os_02);
            if (this.d.logLevel <= 5) {
                this.d.log("Read card on stack " + os_02.stackIndex + " value " + n4);
            }
            if (!this.B) {
                if (n4 <= 0) {
                    if (this.d.S) {
                        this.d.log("Card read failed due to nomore dialog or user abort, skip read");
                        this.d.S = false;
                        return false;
                    }
                    this.d.fail("ERROR - failed to read unknown card on stack " + os_02.stackIndex);
                } else {
                    if (this.d.logLevel <= 4) {
                        this.d.log("Read unknown card " + n4);
                    }
                    os_02.topRun.a(n4);
                    if (!this.d.t) {
                        this.d.bridge.a(os_02, this.d.V);
                    }
                    this.d.solver.a(os_02);
                }
                int n5 = 5;
                UiStub oz_02 = this.d.ui;
                oz_02.showStatus(n5, null);
            }
            this.d.S = false;
        }
        return true;
    }

    final int e(CardStack os_02) {
        this.d.bridge.readHighlightColumnIndex = os_02.stackIndex % 10;
        if ((os_02.group.flags & 2) != 0) {
            this.d.bridge.readHighlightRowIndex = os_02.group.groupIndex;
            if (this.d.logLevel <= 4) {
                this.d.log("Horizontal stack " + os_02.stackIndex + " so playrow is " + this.d.bridge.readHighlightRowIndex);
            }
        } else {
            this.d.bridge.readHighlightRowIndex = os_02.runs.size() - 1;
            if (this.d.logLevel <= 4) {
                this.d.log("Vertical stack " + os_02.stackIndex + " so playrow is " + this.d.bridge.readHighlightRowIndex);
            }
        }
        int n2 = 4;
        Object object = this.d.ui;
        ((UiStub)object).showStatus(n2, null);
        BaseSolver op_02 = this;
        int n3 = this.d.searchState.moves[this.d.searchState.depth];
        GameState nY2 = op_02.d.searchState;
        object = op_02;
        if (op_02.a(nY2, n3, false) != 0) {
            return -1;
        }
        this.J = true;
        int n4 = this.d.bridge.a(this.d.searchState, os_02, false, true, false);
        this.J = false;
        if (this.B) {
            this.d.log("Solved while reading a card, so return without the card");
            return -1;
        }
        if (this.d.logLevel <= 5) {
            this.d.log("***Completed read of unknown card " + n4 + " on row " + this.d.bridge.readHighlightRowIndex + " stack " + os_02.stackIndex);
        }
        this.b(4);
        if (this.d.S) {
            if (this.d.logLevel <= 5) {
                this.d.log("Abort of read card is flagged");
            }
        } else if (n4 == -1) {
            this.d.fail("Unexpected failure to read a card<br>Did a dialog pop up?<br>Did you set the challenge objective right?");
        }
        return n4;
    }

    final int j(int n2) {
        int n3 = n2;
        double d2 = 0.0;
        n2 = 0;
        n2 = 0;
        int n4 = n3;
        BaseSolver op_02 = this;
        n4 = op_02.a(n4, 0, 0, 0.0);
        if ((n4 = op_02.d.complexity + n4) <= 0) {
            int n5 = op_02.d.complexity;
            op_02.d.complexity = n4;
            return n5;
        }
        return 999999;
    }

    final int a(int n2, int n3, int n4, double d2) {
        n4 = (int)((double)n2 + d2 * (double)n4);
        if (d2 > 0.0) {
            if (n2 > n3) {
                this.d.fail("Incrementing by " + d2 + " but complexity base " + n2 + " greater than cap " + n3);
            }
            if (n4 > n3) {
                n4 = n3;
            }
        } else if (d2 < 0.0) {
            if (n2 < n3) {
                this.d.fail("Decrementing by " + d2 + " but complexity base " + n2 + " less than cap " + n3);
            }
            if (n4 < n3) {
                n4 = n3;
            }
        }
        return n4;
    }

    final boolean b(CardStack os_02, CardStack os_03) {
        int n2 = this.d.searchState.moves[this.d.searchState.depth - 1];
        int n3 = (n2 & 0xF0000) >> 16;
        n2 = this.d.searchState.moves[this.d.searchState.depth - 1];
        int n4 = n2 >> 24 & 0xFFFFFFFE & 0xFFFFFFFD;
        int n5 = os_03.group.groupIndex * 10 + os_03.stackIndex;
        int n6 = os_02.group.groupIndex * 10 + os_02.stackIndex;
        int n7 = this.d.searchState.depth - 2;
        while (n7 >= 0) {
            n2 = this.d.searchState.moves[n7];
            int n8 = n2 >> 24 & 0xFFFFFFFE & 0xFFFFFFFD;
            if ((n8 & 8) != 0) break;
            if ((n8 & 4) == 0) {
                n2 = this.d.searchState.moves[n7];
                int n9 = n2 >> 8 & 0xFF;
                n2 = this.d.searchState.moves[n7];
                int n10 = n2 & 0xFF;
                if (n4 == n8 && ((n2 = this.d.searchState.moves[n7]) & 0xF0000) >> 16 == n3 && n10 == n5) {
                    n8 = 1;
                    int n11 = this.d.searchState.depth - 2;
                    while (n11 > n7) {
                        n2 = this.d.searchState.moves[n11];
                        if ((n2 >> 24 & 4) == 0 && (((n2 = this.d.searchState.moves[n11]) & 0xFF) == n5 || ((n2 = this.d.searchState.moves[n11]) >> 8 & 0xFF) == n5)) {
                            n8 = 0;
                            break;
                        }
                        --n11;
                    }
                    if (n8 != 0) {
                        if (this.d.logLevel < 3) {
                            this.d.log("Move " + Move.a(this.d.searchState.moves[this.d.searchState.depth - 1]) + " is a reversal of " + Move.a(this.d.searchState.moves[n7]));
                        }
                        return true;
                    }
                }
                if (n10 == n6 || n10 == n5 || n9 == n6 || n9 == n5) break;
            }
            --n7;
        }
        return this.a(os_02, os_03);
    }

    final void a(long l2) {
        int n2 = this.d.searchState.depth * 10 / this.H;
        if (n2 >= 10) {
            n2 = 9;
        }
        Long l3 = l2;
        if (this.R[n2].size() > this.a) {
            if (this.d.logLevel <= 4) {
                this.d.log(String.format("Discarding %d  hashes in bucket %d, counts %d/%d, %d/%d, %d/%d, %d/%d, %d/%d, %d/%d, %d/%d, %d/%d, %d/%d, %d/%d", this.a, n2, this.R[0].size(), this.S[0].size(), this.R[1].size(), this.S[1].size(), this.R[2].size(), this.S[2].size(), this.R[3].size(), this.S[3].size(), this.R[4].size(), this.S[4].size(), this.R[5].size(), this.S[5].size(), this.R[6].size(), this.S[6].size(), this.R[7].size(), this.S[7].size(), this.R[8].size(), this.S[8].size(), this.R[9].size(), this.S[9].size()));
            }
            this.S[n2] = this.R[n2];
            this.R[n2] = new HashMap(this.l(n2));
        }
        this.R[n2].put(l3, this.d.complexity << 16 | this.d.searchState.depth);
    }

    final int b(long l2) {
        Long l3;
        Integer n2;
        int n3 = this.d.searchState.depth * 10 / this.H;
        if (n3 >= 10) {
            n3 = 9;
        }
        if ((n2 = (Integer)this.R[n3].get(l3 = Long.valueOf(l2))) != null) {
            n3 = n2;
            int n4 = n3 & 0xFFFF;
            if (this.d.complexity >= (n3 >>= 16) - 50 && (this.d.files.maxMoves == 999 || this.d.searchState.depth >= n4)) {
                if (this.K != null && this.n()) {
                    this.b(9);
                    this.d.log("About to reject trial solution as a duplicate, hash = " + l2 + " overriding");
                    return -1;
                }
                return 0;
            }
            return -1;
        }
        n2 = (Integer)this.S[n3].get(l3);
        if (n2 != null) {
            n3 = n2;
            int n5 = n3 & 0xFFFF;
            if (this.d.complexity >= (n3 >>= 16) - 50 && (this.d.files.maxMoves == 999 || this.d.searchState.depth >= n5)) {
                if (this.K != null && this.n()) {
                    this.b(9);
                    this.d.log("About to reject trial solution as a duplicate, hash = " + l2 + " overriding");
                    return -1;
                }
                return 0;
            }
            return -1;
        }
        return -1;
    }

    final boolean b(int n2, boolean bl) {
        return this.a(1, bl, 0);
    }

    final boolean a(int n2, boolean bl, int n3) {
        HashMap hashMap = new HashMap(104);
        int n4 = this.a(hashMap);
        this.n = n2;
        n2 = 0;
        int n5 = 0;
        int n6 = 0;
        int n7 = 0;
        int n8 = 1;
        while (n8 < 5) {
            int n9 = 1;
            while (n9 < 14) {
                int n10 = 0;
                Integer n11 = (Integer)hashMap.get(n8 * 100 + n9);
                if (n11 != null) {
                    n10 = n11;
                }
                n7 += n10;
                if (n10 > (this.n << 2) / this.suitCount) {
                    n5 = n8 * 100 + n9;
                    n6 = n10;
                    if (this.d.solverMode != 1) {
                        this.d.fontStats.reset();
                        this.d.fail("Incorrect count of " + n6 + " for the " + BaseSolver.h(n5));
                    } else {
                        this.d.log("Incorrect count of " + n6 + " for the " + BaseSolver.h(n5));
                    }
                    n2 = 1;
                }
                ++n9;
            }
            ++n8;
        }
        if (n2 != 0) {
            this.d.log("Suit point calculation: max club = " + this.d.fontStats.e + " min spade = " + this.d.fontStats.f);
            if (this.d.solverMode == 1) {
                this.d.log("*** ERROR, counted " + n4 + " cards, bad card " + n5 + " count of " + n6);
            } else {
                this.d.fail("*** ERROR - counted " + n4 + " cards, bad card " + n5 + " count of " + n6);
            }
        }
        if (bl && n3 == 0 && n7 != this.n * 52) {
            n2 = 1;
            if (this.d.solverMode == 1) {
                this.d.log("Needed " + this.n * 52 + " cards but only read " + n7);
            } else {
                this.d.fail("Needed " + this.n * 52 + " cards but only read " + n7);
            }
        }
        return n2 == 0;
    }

    void a(HashMap hashMap, int n2, String string) {
        int n3;
        Integer n4 = n2;
        Integer n5 = (Integer)hashMap.get(n4);
        if (n5 == null) {
            n3 = 1;
        } else {
            n3 = n5;
            ++n3;
        }
        if (this.d.logLevel <= 2) {
            this.d.log("Adding " + string + " card " + n2 + " giving " + n3);
        }
        hashMap.put(n4, n3);
    }

    final void l() {
        if (this.d.logLevel <= 3) {
            this.b(3);
            this.dumpState(3, false);
        }
        if (this.statusUpdateCounter++ > 10000) {
            if (this.d.aI && this.d.t) {
                this.d.b(8);
            }
            this.d.updateStatus("Solving");
            this.statusUpdateCounter = 0;
        }
        if (this.K != null) {
            this.o();
        }
        if (this.d.searchState.depth < this.d.U) {
            this.d.U = this.d.searchState.depth;
        }
        if (this.d.searchState.depth > this.deepestRecursionDepth) {
            this.deepestRecursionDepth = this.d.searchState.depth;
            this.deepestRecursionComplexity = this.d.complexity;
        }
    }

    /*
     * Handled impossible loop by duplicating code
     * Enabled aggressive block sorting
     */
    private static boolean[] n(int n2) {
        boolean[] blArray;
        block5: {
            int n3;
            int n4;
            block4: {
                blArray = new boolean[501];
                Arrays.fill(blArray, true);
                n4 = 2;
                if (!true) break block4;
                n3 = ++n4;
                if (n3 * n3 > 500) break block5;
            }
            do {
                if (blArray[n4]) {
                    int n5 = n4 << 1;
                    while (n5 <= 500) {
                        blArray[n5] = false;
                        n5 += n4;
                    }
                }
                n3 = ++n4;
            } while (n3 * n3 <= 500);
        }
        return blArray;
    }

    static boolean a(Card nT2, int n2, int n3) {
        if (n2 > 0) {
            return nT2.cardId == n2 * 100 + n3;
        }
        return nT2.rank == n3;
    }

    final int a(GameState nY2, int n2, boolean bl) {
        if (this.d.files.b == 0) {
            this.d.fail("Need to have gameChallenge set");
        }
        if (this.d.bk > 0 && System.currentTimeMillis() - this.b > 30000L) {
            return 1;
        }
        if (this.B) {
            return 2;
        }
        nY2.solutionLength = nY2.depth;
        if (this.d.foundCompleteSolution && this.d.bestSolutionState.solutionLength < nY2.solutionLength) {
            return 1;
        }
        n2 = 0;
        int n3 = this.a(nY2);
        boolean bl2 = this.a(nY2, n3);
        if (bl2) {
            n2 = 1;
        }
        if (this.d.files.maxMoves < 999 && n3 > this.d.files.maxMoves) {
            return 1;
        }
        if (this.d.files.b == 6) {
            nY2.scoreByDepth[nY2.depth] = n3 = this.a(nY2, false);
            if (this.d.logLevel <= 3) {
                this.d.log("Current score is " + n3);
            }
            if (n3 >= this.d.files.g) {
                n2 = 1;
                if (nY2.solutionLength < this.d.files.maxMoves && (this.F < this.d.files.g || this.d.bestSolutionState.solutionLength > nY2.solutionLength)) {
                    this.F = n3;
                    this.a(nY2, "Can make target with " + this.F + " in " + nY2.solutionLength + " moves", true, true);
                }
            } else {
                boolean bl3;
                boolean bl4 = bl3 = n3 > 0 && (n3 > this.F || this.d.bestSolutionState != null && n3 == this.F && this.d.bestSolutionState.solutionLength > nY2.solutionLength);
                if (this.E) {
                    if (bl2 & bl3) {
                        this.F = n3;
                        this.a(nY2, "Can clear board with " + this.F + " in " + nY2.solutionLength + " moves", true, false);
                    }
                } else {
                    if (this.d.variantId != 4 && this.d.variantId != 5) {
                        bl2 = false;
                    }
                    if (bl3 || bl2) {
                        this.F = n3;
                        this.a(nY2, "Best score currently " + this.F + " in " + nY2.solutionLength + " moves", bl2, false);
                    }
                }
            }
            if (this.a(bl)) {
                this.d.files.l = this.F;
                n2 = 2;
                if (this.d.logLevel <= 5) {
                    this.d.log("Board cleared with score standing at " + this.d.files.l + " vs target of " + this.d.files.g);
                }
                this.d.ui.showStatus(5, "Best score is now " + this.d.files.l + " of " + this.d.files.g + " in " + this.d.bestSolutionState.solutionLength + " moves");
                if (this.d.files.g - this.d.files.l <= 0) {
                    this.d.files.a = true;
                }
            }
        } else if (this.d.files.b == 3 || this.d.files.b == 5 || this.d.files.b == 4) {
            String string;
            int n4;
            int n5;
            if (this.d.files.b == 5) {
                n3 = this.b(nY2, false);
                n5 = this.d.files.n;
                n4 = this.d.files.i - n5;
                string = "Best solution currently %d stacks in %d moves";
            } else {
                n3 = this.a(nY2, false, this.d.files.d, this.d.files.c);
                if (this.d.files.b == 4) {
                    n5 = 0;
                    n4 = 1;
                    string = "Best solution currently %d card in %d moves";
                } else {
                    n5 = this.d.files.m;
                    n4 = this.d.files.h - n5;
                    string = "Best solution currently %d cards in %d moves";
                }
            }
            if (n3 >= n4) {
                n2 = 1;
                if (nY2.solutionLength < this.d.files.maxMoves && (this.G < n4 || this.d.bestSolutionState.solutionLength > nY2.solutionLength)) {
                    this.G = n3;
                    this.a(nY2, String.format(string, n5 + this.G, nY2.solutionLength), false, true);
                }
            } else {
                boolean bl5;
                boolean bl6 = bl5 = n3 > 0 && (n3 > this.G || n3 == this.G && this.d.bestSolutionState.solutionLength > nY2.solutionLength);
                if (this.E) {
                    if (bl2 & bl5) {
                        this.G = n3;
                        this.a(nY2, String.format(string, n5 + this.G, nY2.solutionLength), true, false);
                        n2 = 1;
                    }
                } else {
                    if (this.d.variantId != 4 && this.d.variantId != 5) {
                        bl2 = false;
                    }
                    if (bl5 || bl2) {
                        this.G = n3;
                        this.a(nY2, String.format(string, n5 + this.G, nY2.solutionLength), bl2, false);
                        if (bl2) {
                            n2 = 1;
                        }
                    }
                }
            }
            if (this.a(bl)) {
                n2 = 2;
                if (this.d.files.b == 3) {
                    this.d.files.m += this.G;
                    this.d.ui.showStatus(5, "Best solution clears " + this.d.files.m + " of " + this.d.files.h + " cards in " + this.d.bestSolutionState.solutionLength + " moves");
                    if (this.d.files.m >= this.d.files.h) {
                        this.d.files.a = true;
                    }
                } else if (this.d.files.b == 4) {
                    this.d.ui.showStatus(5, "Best solution clears the card in " + this.d.bestSolutionState.solutionLength + " moves");
                    this.d.files.a = true;
                } else {
                    this.d.files.n += this.G;
                    this.d.ui.showStatus(5, "Best solution clears " + this.d.files.n + " of " + this.d.files.i + " stacks in " + this.d.bestSolutionState.solutionLength + " moves");
                    if (this.d.files.n >= this.d.files.i) {
                        this.d.files.a = true;
                    }
                }
                this.G = 0;
                if (this.d.logLevel <= 5) {
                    this.d.log("Target card/stack count now " + n4);
                }
            }
        } else if (this.d.files.b == 1 || this.d.files.b == 2) {
            if (bl2) {
                n2 = 1;
                if (nY2.solutionLength < this.d.files.maxMoves && (this.d.bestSolutionState.solutionLength == 0 || nY2.solutionLength < this.d.bestSolutionState.solutionLength)) {
                    this.a(nY2, "Best solution currently " + nY2.solutionLength + " moves", true, true);
                }
            }
            if (this.a(bl)) {
                n2 = 2;
                this.d.files.k = this.d.solverMode == 3 ? ++this.d.files.k : 1;
                if (this.d.logLevel <= 5) {
                    this.d.log("Board cleared, accum now " + this.d.files.k);
                }
                if (this.d.files.e == 1) {
                    this.d.ui.showStatus(5, "Best solution takes " + this.d.bestSolutionState.solutionLength + " moves");
                } else {
                    this.d.ui.showStatus(5, "Best solution gives " + this.d.files.k + " of " + this.d.files.e + " boards in " + this.d.bestSolutionState.solutionLength + " moves");
                }
                if (this.d.files.k >= this.d.files.e) {
                    this.d.files.a = true;
                }
            } else if (!(!bl || this.d.solverMode != 3 && this.d.solverMode != 1 || this.d.variantId != 4 && this.d.variantId != 5)) {
                n2 = 2;
                this.d.bestSolutionState.reset();
            }
        }
        if (n2 == 2) {
            this.B = true;
            if (this.d.logLevel <= 9) {
                this.d.log("Mode " + this.d.solverMode + " (challenge " + this.d.files.b + ") found a solution length " + this.d.bestSolutionState.solutionLength + " in " + (System.currentTimeMillis() - this.b) / 1000L);
            }
            this.a(9, this.d.bestSolutionState, "Solved best moves");
            this.b(false);
        }
        return n2;
    }

    private void a(GameState nY2, String string, boolean bl, boolean bl2) {
        if (this.d.logLevel <= 5) {
            this.d.log(string);
            this.dumpState(5, false);
        }
        this.d.bestSolutionState = new GameState(nY2, true);
        if (bl) {
            this.E = true;
        }
        this.c = System.currentTimeMillis();
        this.d.ui.showStatus(5, string);
        if (bl2) {
            this.d.foundCompleteSolution = true;
        }
    }

    private boolean a(boolean bl) {
        if (this.d.bestSolutionState.solutionLength == 0) {
            return false;
        }
        if (bl || this.d.searchStepCount % 1000L == 0L) {
            long l2 = System.currentTimeMillis();
            if (this.d.bridge.lastBridgeUpdateTimeMs > this.c) {
                this.c = this.d.bridge.lastBridgeUpdateTimeMs;
            }
            if (bl || this.d.aG || this.d.aF == 0 || l2 - this.c > (long)this.d.aF) {
                this.d.aG = false;
                if (this.d.foundCompleteSolution || this.E) {
                    if (this.d.logLevel <= 5) {
                        String string = "Test final (forced " + bl + ") best moves";
                        this.d.log("Best solution length " + this.d.bestSolutionState.solutionLength);
                        this.a(5, this.d.bestSolutionState, string);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    boolean f() {
        return false;
    }

    int b(GameState nY2, boolean bl) {
        return -1;
    }

    int a(GameState nY2, boolean bl, int n2, int n3) {
        return -1;
    }

    int a(GameState nY2, boolean bl) {
        return -1;
    }

    int a(GameState nY2) {
        return nY2.depth + 1;
    }

    void g() {
        if (this.d.initialState.stackGroups == null) {
            return;
        }
        if (this.d.initialState.stackGroups[0] == null) {
            return;
        }
        int n2 = this.r[0].length;
        int n3 = 0;
        while (n3 < this.f) {
            if (n3 >= this.d.initialState.stackGroups[0].stacks.length) break;
            CardStack os_02 = this.d.initialState.stackGroups[0].stacks[n3];
            int n4 = 0;
            for (Object object : os_02.runs) {
                CardRun ok_02 = (CardRun)object;
                int n5 = 0;
                while (n5 < ok_02.cardCount) {
                    this.r[n3][n4] = ok_02.cards[n5].cardId == 0 ? (!this.B && n3 == this.d.bridge.readHighlightColumnIndex && n4 == this.d.bridge.readHighlightRowIndex ? 3 : 2) : 4;
                    if (++n4 > n2 - 1) break;
                    ++n5;
                }
                if (n4 > n2 - 1) break;
            }
            while (n4 < n2) {
                this.r[n3][n4] = 0;
                ++n4;
            }
            ++n3;
        }
    }

    final int[] b(HashMap hashMap, int n2) {
        int[] nArray = new int[52];
        int n3 = 1;
        while (n3 < this.suitCount + 1) {
            int n4 = 1;
            while (n4 < 14) {
                Integer n5 = n3 * 100 + n4;
                n5 = (Integer)hashMap.get(n5);
                int n6 = 4 * n2 / this.suitCount;
                if (n5 != null) {
                    n6 -= n5.intValue();
                }
                nArray[(n3 - 1) * 13 + (n4 - 1)] = n6;
                ++n4;
            }
            ++n3;
        }
        return nArray;
    }

    final void b(int n2, StackGroup ot_02) {
        if (n2 >= this.d.logLevel && this.d.searchState.depth > 0) {
            StringBuffer stringBuffer = new StringBuffer(String.format("Lvl %3d move %8s", this.d.searchState.depth - 1, Move.a(this.d.searchState.moves[this.d.searchState.depth - 1])));
            this.d.table.a(stringBuffer, ot_02);
            this.d.log(stringBuffer.toString());
        }
    }

    static int a(StackGroup ot_02, int n2) {
        if (ot_02 == null) {
            return 0;
        }
        CardStack[] os_0Array = ot_02.stacks;
        int n3 = ot_02.stacks.length;
        int n4 = 0;
        while (n4 < n3) {
            CardStack os_02 = os_0Array[n4];
            if (os_02.topRun != null && os_02.topRun.cards[os_02.topRun.cardCount - 1].suit == n2) {
                return os_02.topRun.cardCount;
            }
            ++n4;
        }
        return 0;
    }

    static int b(StackGroup ot_02, int n2) {
        if (ot_02 == null) {
            return 0;
        }
        int[] nArray = new int[4];
        int n3 = 0;
        int n4 = 0;
        CardStack[] os_0Array = ot_02.stacks;
        int n5 = ot_02.stacks.length;
        int n6 = 0;
        while (n6 < n5) {
            CardStack os_02 = os_0Array[n6];
            if (os_02.topRun != null) {
                nArray[n3] = os_02.topRun.cardCount;
            }
            ++n3;
            ++n6;
        }
        Arrays.sort(nArray);
        n3 = 3;
        while (n3 > 3 - n2) {
            n4 += nArray[n3];
            --n3;
        }
        return n4;
    }

    int b(CardStack os_02) {
        return -1;
    }

    int a(CardStack os_02, CardStack os_03, int n2) {
        return -1;
    }

    int a(Card nT2) {
        return -1;
    }

    int h() {
        if (this.d.logLevel <= 5) {
            this.d.log("Default click on space, drop selected");
        }
        if (this.d.board != null) {
            this.d.board.clickBackground();
        }
        return -1;
    }

    int e() {
        return -1;
    }

    final int m() {
        int n2 = -1;
        this.d.playbackState.depth = SolverBridge.a(this.d.initialState.moves, this.d.playbackState.moves, this.d.playbackMoveIndex);
        if (this.d.playbackState.depth > 0) {
            --this.d.playbackState.depth;
            n2 = this.d.playbackState.moves[this.d.playbackState.depth];
        }
        if (this.d.ag) {
            if (this.d.logLevel <= 5) {
                this.d.log("Undo in playback always matches solution so use single step");
            }
            this.d.aX = true;
        } else {
            if (this.d.logLevel <= 5) {
                this.d.log("Undo not in playback does not match any solution so not single step");
            }
            this.d.aX = false;
        }
        return n2;
    }

    final void k(int n2) {
        if (this.d.ag) {
            GameState nY2;
            GameState nY3 = nY2 = this.J ? this.d.searchState : this.d.bestSolutionState;
            if (this.d.ag && nY2.moves != null && this.b(n2, nY2.moves[this.d.playbackMoveIndex])) {
                if (this.d.logLevel <= 5) {
                    this.d.log("This is a matching move so do single step");
                }
                this.d.aX = true;
                this.d.playbackState.depth = SolverBridge.a(nY2.moves, this.d.playbackState.moves, nY2.depth);
                return;
            }
            if (this.d.logLevel <= 5) {
                this.d.log("Non-matching move so discard previous solution");
            }
            this.d.aX = false;
            this.d.playbackState.depth = SolverBridge.a(nY2.moves, this.d.playbackState.moves, this.d.playbackMoveIndex);
            this.d.playbackState.moves[this.d.playbackState.depth] = n2;
            ++this.d.playbackState.depth;
            this.d.bestSolutionState.moves = null;
            this.d.bestSolutionState.reset();
            this.B = false;
            this.d.foundCompleteSolution = false;
            return;
        }
        if (this.d.playbackState.depth < 350) {
            this.d.playbackState.moves[this.d.playbackState.depth] = n2;
            ++this.d.playbackState.depth;
            return;
        }
        this.d.ui.showError("Too many moves", "You must solve the problem in under 350 moves");
    }

    final Card b(CardStack os_02, int n2) {
        if (this.A == this.o) {
            this.d.fontStats.reset();
            this.d.fail("Trying to allocate more than " + this.o + " cards");
        }
        if (this.d.logLevel <= 5) {
            this.d.log("@@@ Allocating card #" + this.A + " value " + n2);
        }
        Card nT2 = this.z[this.A++];
        nT2.a(n2);
        nT2.stack = os_02;
        nT2.primaryUiHandle = null;
        nT2.secondaryUiHandle = null;
        return nT2;
    }

    boolean b(int n2, int n3) {
        return n2 == n3;
    }
}





