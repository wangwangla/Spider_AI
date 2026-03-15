/*
 * Decompiled with CFR 0.152.
 */
package com.solvitaire.app;

import java.io.File;
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
    SolverContext solverContext;
    private int bucket = 0x100000;
    String e;
    int stackSize;
    int[][] g;
    int[] h;
    int[] i;
    int[] j;
    double[] k;
    int l;
    int m;
    int decksOfCards;
    int o;
    int suitCount;
    private long b;
    private long c;
    private int H;
    private String I;
    transient private boolean state;
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
    Card[] cardArray;
    int cardIndex;
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

    abstract boolean a(GameState var1, int var2);

    BaseSolver(SolverContext solverContext, int searchCreditLimit) {
        super();
        this.solverContext = solverContext;
        this.solverContext.solver = this;
        this.searchCreditLimit = searchCreditLimit;
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

    private int getBucket(int n2) {
        if (n2 > 2) {
            return this.bucket;
        }
        if (n2 == 2) {
            return this.bucket / 2;
        }
        if (n2 == 1) {
            return this.bucket / 8;
        }
        return this.bucket / 64;
    }

    final void initializeBaseState() {
        this.B = false;
        this.solverContext.abortAllReads = false;
        this.solverContext.searchStepCount = 0L;
        if (this.solverContext.solverMode != 3) {
            this.solverContext.files.maxMoves = 999;
        }
        this.cardArray = new Card[this.o];
        int n2 = 0;
        while (n2 < this.o) {
            this.cardArray[n2] = new Card(this.solverContext);
            ++n2;
        }
        this.cardIndex = 0;
        this.u = true;
        this.solverContext.searchCredit = 0;
        this.solverContext.playbackMoveIndex = 0;
        if (this.solverContext.logLevel <= 5) {
            this.solverContext.log("In baseinit, set recursiondepth and playlocation to 0");
        }
        if (this.solverContext.initialState != null) {
            this.solverContext.initialState.reset();
        }
        this.solverContext.searchState = null;
        this.suitCount = 4;
        this.F = 0;
        this.G = 0;
        this.solverContext.foundCompleteSolution = false;
        this.E = false;
        this.H = 200;
        if (this.solverContext.files.maxMoves < 200) {
            this.H = this.solverContext.files.maxMoves + 2;
        }
        switch (this.solverContext.files.b) {
            case 2: {
                if (this.solverContext.files.e <= 1 || this.f()) break;
                this.solverContext.fail("Multiple board challenges are not supported<br>for " + this.getSolverName());
                return;
            }
            case 3: {
                BaseSolver op_02 = this;
                if (op_02.a(op_02.solverContext.initialState, true, 0, 0) >= 0) break;
                this.solverContext.fail("Clearing a specific number of cards<br> is not supported for " + this.getSolverName());
                return;
            }
            case 4: {
                BaseSolver op_03 = this;
                if (op_03.a(op_03.solverContext.initialState, true, 0, 0) >= 0) break;
                this.solverContext.fail("Clearing a specific card<br>is not supported for " + this.getSolverName());
                return;
            }
            case 5: {
                BaseSolver op_04 = this;
                if (op_04.equealData(op_04.solverContext.initialState, true) >= 0) break;
                this.solverContext.fail("Clearing a specific number of stacks<br>is not supported for " + this.getSolverName());
                return;
            }
            case 6: {
                BaseSolver op_05 = this;
                if (op_05.a(op_05.solverContext.initialState, true) >= 0) break;
                this.solverContext.fail("Scoring challenges are not supported for " + this.getSolverName());
            }
        }
    }

    final void solve() {

        block60: {
            this.b = System.currentTimeMillis();
            if (this.solverContext.solverMode != 4) {
                BaseSolver baseSolver = this;
                //堆内存使用情况
                MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
                //最大值 
                long heapMax = memoryUsage.getMax();
                //使用值
                long heapUse = memoryUsage.getUsed();
                // 最大值 > 8x
                if (heapMax > 8000000000L) {
                    baseSolver.bucket = 0x200000;
                    if (baseSolver.solverContext.logLevel <= 5) {
                        baseSolver.solverContext.log("Max heap memory: " + heapMax + " used: " + heapUse + " bucket size: " + baseSolver.bucket);
                    }
                } else if (heapMax > 4000000000L) {
                    baseSolver.bucket = 0x180000;
                    if (baseSolver.solverContext.logLevel <= 5) {
                        baseSolver.solverContext.log("Max heap memory: " + heapMax + " used: " + heapUse + " bucket size: " + baseSolver.bucket);
                    }
                } else if (heapMax > 2000000000L) {
                    baseSolver.bucket = 786432;
                    if (baseSolver.solverContext.logLevel <= 5) {
                        baseSolver.solverContext.log("Max heap memory: " + heapMax + " used: " + heapUse + " bucket size: " + baseSolver.bucket);
                    }
                } else if (heapMax > 1000000000L) {
                    baseSolver.bucket = 393216;
                    if (baseSolver.solverContext.logLevel <= 5) {
                        baseSolver.solverContext.log("Max heap memory: " + heapMax + " used: " + heapUse + " bucket size: " + baseSolver.bucket);
                    }
                } else if (heapMax > 500000000L) {
                    baseSolver.bucket = 196608;
                    if (baseSolver.solverContext.logLevel <= 5) {
                        baseSolver.solverContext.log("Max heap memory: " + heapMax + " used: " + heapUse + " bucket size: " + baseSolver.bucket);
                    }
                } else if (heapMax > 250000000L) {
                    baseSolver.bucket = 98304;
                    if (baseSolver.solverContext.logLevel <= 5) {
                        baseSolver.solverContext.log("Max heap memory: " + heapMax + " used: " + heapUse + " bucket size: " + baseSolver.bucket);
                    }
                } else {
                    baseSolver.solverContext.fail("ERROR<br>System has insufficient available RAM (" + heapMax / 1024000L + " megabytes)<br>Solitaire Solver would run too slowly");
                }
            }
            boolean bl = false;
            if (this.initializeSolver()) {
                String string = "n/a";
                if (this.solverContext.fontStats != null && this.solverContext.fontStats.font != null) {
                    string = this.solverContext.fontStats.font.name;
                }
                if (this.solverContext.logLevel <= 9) {
                    this.solverContext.log("*** " + this.getSolverName() + " initialisation complete, font " + string +
                            ", mode " + SolverContext.INPUT_SOURCE_MODE_NAMES[this.solverContext.inputSourceMode] + "," +
                            SolverContext.SOLVER_MODE_NAMES[this.solverContext.solverMode] + " (Solvitaire version " + "5.1.2" + " on " + new Date() + ")");
                }
                this.C = false;
            } else {
                this.solverContext.log("*** " + this.getSolverName() + " initialisation failed for game mode " + this.solverContext.solverMode);
                this.C = false;
                if (this.solverContext.aN) {
                    bl = true;
                } else {
                    return;
                }
            }
            if (bl) break block60;
            int countIndex = 5;
            this.solverContext.sleepBriefly(100L, "Prevent tight loop");

            this.maxSearchDepth = 298;
            while (this.solverContext.searchCredit > -this.searchCreditLimit) {
                block63: {
                    block62: {
                        if (this.solverContext.solverMode == 4) {
                            this.B = true;
                            break block62;
                        }
                        if (this.solverContext.logLevel <= 4) {
                            this.solverContext.log("In process, entering solve loop");
                        }
                        this.solverContext.ai = true;
                        while (this.solverContext.searchCredit > -this.searchCreditLimit) {
                            countIndex = 0;
                            while (countIndex < 10) {
                                this.R[countIndex] = new HashMap(this.getBucket(countIndex));
                                this.S[countIndex] = new HashMap(this.getBucket(countIndex));
                                ++countIndex;
                            }
                            this.solverContext.complexity = this.solverContext.searchCredit;
                            this.D = -1;
                            this.deepestRecursionDepth = 0;
                            this.deepestRecursionComplexity = 0;
                            if (this.K != null && this.K[0] > 0) {
                                this.K[0] = 0;
                            }
                            this.solverContext.ah = true;
                            this.search(-1, 0);
                            this.solverContext.ah = false;
                            if (this.B || this.D > 0) break;
                            if (this.solverContext.logLevel <= 4) {
                                this.solverContext.log("*** Deepest recursion for credit " + this.solverContext.searchCredit + " was " + this.deepestRecursionDepth + " with complexity " + this.deepestRecursionComplexity);
                            }
                            this.solverContext.searchCredit -= 30;
                        }
                        if (this.B || this.D > 0) break block63;
                        if (this.solverContext.logLevel <= 5) {
                            this.solverContext.log("Credit expired and solve not flagged, do final check");
                        }
                        this.B = this.a(this.solverContext.searchState, 0, true) == 2;
                    }
                }
                countIndex = 0;
                while (countIndex < 10) {
                    this.R[countIndex] = null;
                    this.S[countIndex] = null;
                    ++countIndex;
                }
                System.gc();
                if (this.B) {
                    countIndex = 1;
                    if (countIndex == 0) {

                        this.solverContext.searchCredit = 0;
                        this.B = false;
                        continue;
                    }
                    if (this.solverContext.logLevel <= 5) {
                        this.solverContext.log("Play solution");
                    }
                    this.solverContext.initialState.moveAnnotations = Arrays.copyOf(this.solverContext.bestSolutionState.moveAnnotations, this.solverContext.bestSolutionState.moveAnnotations.length);
                    this.solverContext.bridge.a(this.solverContext.bestSolutionState, null, true, true, false);
                    if (!this.solverContext.Y) {
                        if (this.solverContext.logLevel > 5) break;
                        this.solverContext.log("Solved so exit process loop");
                        break;
                    }
                    if (this.solverContext.logLevel <= 5) {
                        this.solverContext.log("Playback aborted, stay in play loop");
                    }
                    this.solverContext.Y = false;
                    this.solverContext.searchCredit = 0;
                    continue;
                }
                if (!this.solverContext.t) continue;
                if (this.solverContext.logLevel <= 5) {
                    this.solverContext.log("Exited solve loop without solution");
                }
                if (this.D > 0) {
                    this.solverContext.log("The user aborted the solve so go back to user moves");
                    this.D = -1;
                    this.solverContext.searchCredit = 0;
                    continue;
                }
                if (this.solverContext.searchState.depth > 0) {
                    this.solverContext.searchCredit = 0;
                    continue;
                }
            }
            if (!this.solverContext.t) {
                if (this.solverContext.solverMode == 0 || this.solverContext.ad && (this.solverContext.solverMode == 4 || this.solverContext.solverMode == 3)) {
                    if (!this.B && this.solverContext.solverMode == 0) {
                        this.equealData(true);
                        if (!this.solverContext.az) {
                            this.solverContext.fail("Insoluble game. (See option to skip)");
                        }
                    }
                    if (this.solverContext.logLevel <= 5) {
                        this.solverContext.log("***About to skip dialogs (and ads:" + (this.solverContext.solverMode == 0) + ")");
                    }
                    if (this.solverContext.logLevel <= 5) {
                        this.solverContext.log("***Skipped dialogs, and ads");
                    }
                }
            }
        }
        this.solverContext.bestSolutionState.reset();
        if (this.solverContext.logLevel <= 6) {
            this.solverContext.log("*** Exit from process ***");
        }
    }

    private boolean n() {
        if (this.solverContext.searchState.depth > 3 && this.solverContext.searchState.depth == this.K[0] + 1) {
            int n2 = this.solverContext.searchState.depth - 4;
            while (n2 < this.solverContext.searchState.depth && n2 < this.K.length - 1) {
                if (!Move.a(Move.b(this.K[n2 + 1]), this.solverContext.searchState.moves[n2])) {
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
            if (this.solverContext.searchState.depth <= this.K[1]) {
                return false;
            }
            this.K[1] = this.solverContext.searchState.depth;
            this.solverContext.log("Found longer solution");
            this.equealData(9);
            this.dumpState(9, false);
            if (this.solverContext.t) {
                this.solverContext.bridge.a(this.solverContext.searchState, null, false, false, false);
            }
        } else if (this.K[0] == -1) {
            if (this.solverContext.searchState.depth < this.K.length - 1) {
                return false;
            }
            int n2 = 1;
            while (n2 < this.K.length) {
                if (!Move.a(Move.b(this.K[n2]), this.solverContext.searchState.moves[this.solverContext.searchState.depth - this.K.length + n2])) break;
                ++n2;
            }
            if (n2 == this.K.length) {
                this.equealData(9);
                this.solverContext.log("Found segment");
            }
        } else {
            int n3 = 0;
            while (n3 < this.solverContext.searchState.depth && n3 < this.K.length - 1) {
                if (!Move.a(Move.b(this.K[n3 + 1]), this.solverContext.searchState.moves[n3])) break;
                ++n3;
            }
            if (n3 >= this.K[0]) {
                if (n3 > this.K[0]) {
                    BaseSolver op_02 = this;
                    this.solverContext.log("Approaching solution to " + n3 + " of " + (this.K.length - 1) + " dealindex " + this.solverContext.searchState.dealIndex + " score " + op_02.a(op_02.solverContext.searchState, false));
                    this.dumpState(5, false);
                    if (this.solverContext.logLevel <= 5) {
                        this.solverContext.log("State hash " + this.computeStateHash());
                    }
                    this.equealData(9);
                    this.K[0] = n3;
                }
                if (n3 == this.K.length - 1 && n3 == this.solverContext.searchState.depth) {
                    this.solverContext.log("Hit end of known solution");
                    return true;
                }
            } else if (n3 < this.K[0] && this.K[0] < 1000) {
                this.equealData(9);
                this.solverContext.log("@@@ Backing out of solution");
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
            this.solverContext.fail("Logic error: trying to hash invalid card");
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
            this.solverContext.fail("Logic error: trying to hash invalid card");
        }
        l2 = (long)this.L[n2] * (this.W[this.m] + (l2 & Integer.MAX_VALUE));
        if (bl) {
            l2 <<= 1;
        }
        ++this.m;
        return l2;
    }

    final void equealData(int n2) {
        this.a(n2, this.solverContext.searchState, "Work moves");
    }

    final void a(int n2, GameState nY2, String string) {
        if (n2 >= this.solverContext.logLevel) {
            StringBuffer stringBuffer = this.createStateHeader(string, nY2.depth);
            int n3 = 0;
            while (n3 < nY2.depth) {
                stringBuffer.append(Move.a(nY2.moves[n3]));
                stringBuffer.append(",");
                ++n3;
            }
            this.solverContext.log(stringBuffer.toString());
        }
    }

    final void a(int n2, StackGroup ot_02) {
        if (n2 < this.solverContext.logLevel) {
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
            this.solverContext.log(stringBuffer.toString());
            ++n2;
        }
    }

    final boolean loadCheckpointState() {
        Object object;
        int n2;
        boolean bl = true;
        if (this.solverContext.logLevel <= 5) {
            this.solverContext.log("Into loadCheckpoint for game mode " + this.solverContext.solverMode);
        }
        if (this.solverContext.solverMode == 0 || this.solverContext.solverMode == 1 || this.solverContext.solverMode == 6) {
            if (this.solverContext.solverMode == 0) {
                this.solverContext.files.setOutputDirectory(this.e, false);
            }
            n2 = this.solverContext.files.getEndFileIndex();
            if (this.solverContext.solverMode == 1) {
                int n3 = this.solverContext.files.getCurrentFileIndex(false);
                if (this.solverContext.logLevel <= 4) {
                    this.solverContext.log("All cards request, next checkpoint " + n3 + " endFile " + n2);
                }
                if (n3 >= n2) {
                    if (this.solverContext.bk > 0) {
                        this.solverContext.writeTextFile(String.valueOf(this.solverContext.files.outputDirectory) + "difflog.txt", this.I, true);
                    }
                } else {
                    if (n3 == 0) {
                        this.I = "";
                    }
                    String string = null;
                    while (this.solverContext.files.getCurrentFileIndex(false) <= n2) {
                        string = this.solverContext.files.getInputFileName();
                        if (this.solverContext.logLevel <= 4) {
                            this.solverContext.log("Testing for card file " + string);
                        }
                        object = new File(String.valueOf(this.solverContext.files.outputDirectory) + string);
                        if (this.solverContext.solverMode != 1 || ((File)object).exists()) break;

                    }
                    if (this.solverContext.logLevel <= 9) {
                        this.solverContext.log("***Loading history file " + string);
                    }
                }
            } else {
                bl = false;
            }
        } else if (this.solverContext.solverMode == 3) {
            if (this.solverContext.t) {
                bl = true;
                if (this.solverContext.logLevel <= 5) {
                    this.solverContext.log("Capture request in standalone mode so set readcards to true");
                }
            } else {
                if (this.solverContext.logLevel <= 5) {
                    this.solverContext.log("Regular capture so cards come from the screen, readCards=false");
                }
                bl = false;
            }
        } else if (this.solverContext.solverMode == 4) {
            String string = this.solverContext.files.getPlaybackFileName();
            if (string == null) {
                this.solverContext.fail("No solution exists for " + this.solverContext.files.getInputFileName());
            }
            String string2 = this.solverContext.readTextFile(String.valueOf(this.solverContext.files.outputDirectory) + string);
            if (this.solverContext.logLevel <= 5) {
                this.solverContext.log("Playback mode so reading file " + string + " gave solution:" + string2);
            }
            String[] stringArray3 = string2.split(",");
            this.solverContext.bestSolutionState.reset();
            n2 = 0;
            while (n2 < stringArray3.length) {
                int n4;
                if (stringArray3[n2].startsWith("\r") || stringArray3[n2].startsWith("\n")) break;
                stringArray3[n2] = stringArray3[n2].trim();
                if (stringArray3[n2].contains(":")) {
                    String[] stringArray = stringArray3[n2].split(":");
                    stringArray3[n2] = stringArray[0];
                    this.solverContext.bestSolutionState.moveAnnotations[n2] = Integer.parseInt(stringArray[1].trim()) * 10000;
                    boolean bl2 = false;
                    if (this.solverContext.bestSolutionState.moveAnnotations[n2] < 0) {
                        bl2 = true;
                        this.solverContext.bestSolutionState.moveAnnotations[n2] = -this.solverContext.bestSolutionState.moveAnnotations[n2];
                    }
                    if (stringArray.length > 2) {
                        int n5 = n2;
                        this.solverContext.bestSolutionState.moveAnnotations[n5] = this.solverContext.bestSolutionState.moveAnnotations[n5] + Integer.parseInt(stringArray[2].trim()) * 100;
                        if (stringArray.length > 3) {
                            int n6 = n2;
                            this.solverContext.bestSolutionState.moveAnnotations[n6] = this.solverContext.bestSolutionState.moveAnnotations[n6] + Integer.parseInt(stringArray[3].trim());
                        } else {
                            int n7 = n2;
                            this.solverContext.bestSolutionState.moveAnnotations[n7] = this.solverContext.bestSolutionState.moveAnnotations[n7] + 99;
                        }
                    } else {
                        int n8 = n2;
                        this.solverContext.bestSolutionState.moveAnnotations[n8] = this.solverContext.bestSolutionState.moveAnnotations[n8] + 9999;
                    }
                    if (bl2) {
                        this.solverContext.bestSolutionState.moveAnnotations[n2] = -this.solverContext.bestSolutionState.moveAnnotations[n2];
                    }
                } else {
                    this.solverContext.bestSolutionState.moveAnnotations[n2] = 0;
                }
                int n9 = Integer.parseInt(stringArray3[n2]);
                this.solverContext.bestSolutionState.moves[n2] = n4 = Move.b(n9);
                this.solverContext.bestSolutionState.depth = n2 + 1;
                ++n2;
            }
            this.B = true;
        }
        if (bl) {
            String[] stringArray = this.solverContext.readAllLines(String.valueOf(this.solverContext.files.outputDirectory) + this.solverContext.files.getInputFileName());
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
                this.solverContext.fail("Input file has too few lines");
            } else if (!this.loadStateFromLines((String)object, stringArray, n2)) {
                return false;
            }
        }
        return true;
    }

    private void equealData(GameState nY2) {
        StringBuffer stringBuffer = new StringBuffer();
        int n2 = 0;
        while (n2 < nY2.depth) {
            stringBuffer.append(Move.a(nY2.moves[n2]));
            stringBuffer.append(",");
            ++n2;
        }
        if (this.solverContext.files != null) {
            this.solverContext.writeTextFile(String.valueOf(this.solverContext.files.outputDirectory) + this.solverContext.files.getSolutionFileName(), stringBuffer.toString(), true);
        }
    }

    private void a(String string) {
        StringBuffer stringBuffer = new StringBuffer();
        this.appendBoardState(stringBuffer);
        SolverContext.ensureDirectory(string);
        this.solverContext.writeTextFile(String.valueOf(string) + this.solverContext.files.getInputFileName(), stringBuffer.toString(), true);
    }

    final String equealData(boolean bl) {
        String string;
        if (bl) {
            string = String.valueOf(this.solverContext.files.outputDirectory) + "debug" + File.separator;
            this.a(string);
        } else {
            if (this.solverContext.bestSolutionState.depth == 0) {
                this.solverContext.log("No best moves available so just writing current working moves");
                BaseSolver op_02 = this;
                op_02.equealData(op_02.solverContext.searchState);
            } else {
                BaseSolver op_03 = this;
                op_03.equealData(op_03.solverContext.bestSolutionState);
            }
            string = this.solverContext.files.outputDirectory;
            if (!this.solverContext.t && this.solverContext.solverMode != 4) {
                BaseSolver op_04 = this;
                op_04.a(op_04.solverContext.files.outputDirectory);
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

    static String equealData(Card nT2) {
        return String.valueOf(BaseSolver.g(nT2.rank)) + BaseSolver.m(nT2.suit);
    }

    final int a(HashMap hashMap, int n2) {
        int n3;
        Integer n4;
        Integer n5 = n2;
        if (n2 == -1) {
            this.solverContext.fail("ERROR - Card could not be identified");
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
                        if (this.solverContext.logLevel <= 0) {
                            this.solverContext.log("Testing stack " + os_02.stackIndex + " run " + n7 + " entry " + n9 + " card " + ok_02.cards[n9]);
                        }
                        ++n7;
                        int n10 = this.a(hashMap, ok_02.cards[n9].cardId);
                        if (n10 > n6) {
                            Card nT2 = ok_02.cards[n9];
                            Card nT3 = nT2;
                            nT3 = ok_02.cards[n9];
                            this.solverContext.fail("ERROR - Too many " + BaseSolver.f(nT2.cardId) + " of " + BaseSolver.d(nT3.cardId) + "s in the deck");
                        }
                        ++n8;
                    }
                    ++n9;
                }
            }
            n3 += n8;
            ++n5;
        }
        if (this.solverContext.logLevel <= 3) {
            this.solverContext.log("Numcards after stack " + ot_02.name + " is " + n3);
        }
        return n3;
    }

    final boolean a(CardStack os_02, int n2) {
        if (os_02.topRun != null && os_02.topRun.cardCount == 1 && os_02.topRun.cards[0].cardId == 0) {
            BaseSolver op_02 = this;
            int n3 = n2;
            GameState nY2 = op_02.solverContext.searchState;
            BaseSolver op_03 = op_02;
            if (op_02.a(nY2, n3, false) != 0) {
                if (this.solverContext.logLevel <= 4) {
                    this.solverContext.log("Exposing a card would give a solution, so do not read");
                }
                return true;
            }
            if (this.solverContext.abortAllReads) {
                if (this.solverContext.logLevel <= 5) {
                    this.solverContext.log("Abort all reads so returning");
                }
                return false;
            }
            int n4 = this.e(os_02);
            if (this.solverContext.logLevel <= 5) {
                this.solverContext.log("Read card on stack " + os_02.stackIndex + " value " + n4);
            }
            if (!this.B) {
                if (n4 <= 0) {
                    if (this.solverContext.S) {
                        this.solverContext.log("Card read failed due to nomore dialog or user abort, skip read");
                        this.solverContext.S = false;
                        return false;
                    }
                    this.solverContext.fail("ERROR - failed to read unknown card on stack " + os_02.stackIndex);
                } else {
                    if (this.solverContext.logLevel <= 4) {
                        this.solverContext.log("Read unknown card " + n4);
                    }
                    os_02.topRun.a(n4);

                    this.solverContext.solver.a(os_02);
                }
            }
            this.solverContext.S = false;
        }
        return true;
    }

    final int e(CardStack os_02) {
        this.solverContext.bridge.readHighlightColumnIndex = os_02.stackIndex % 10;
        if ((os_02.group.flags & 2) != 0) {
            this.solverContext.bridge.readHighlightRowIndex = os_02.group.groupIndex;
            if (this.solverContext.logLevel <= 4) {
                this.solverContext.log("Horizontal stack " + os_02.stackIndex + " so playrow is " + this.solverContext.bridge.readHighlightRowIndex);
            }
        } else {
            this.solverContext.bridge.readHighlightRowIndex = os_02.runs.size() - 1;
            if (this.solverContext.logLevel <= 4) {
                this.solverContext.log("Vertical stack " + os_02.stackIndex + " so playrow is " + this.solverContext.bridge.readHighlightRowIndex);
            }
        }
        BaseSolver op_02 = this;
        int n3 = this.solverContext.searchState.moves[this.solverContext.searchState.depth];
        GameState nY2 = op_02.solverContext.searchState;
        if (op_02.a(nY2, n3, false) != 0) {
            return -1;
        }
        this.state = true;
        int n4 = this.solverContext.bridge.a(this.solverContext.searchState, os_02, false, true, false);
        this.state = false;
        if (this.B) {
            this.solverContext.log("Solved while reading a card, so return without the card");
            return -1;
        }
        if (this.solverContext.logLevel <= 5) {
            this.solverContext.log("***Completed read of unknown card " + n4 + " on row " + this.solverContext.bridge.readHighlightRowIndex + " stack " + os_02.stackIndex);
        }
        this.equealData(4);
        if (this.solverContext.S) {
            if (this.solverContext.logLevel <= 5) {
                this.solverContext.log("Abort of read card is flagged");
            }
        } else if (n4 == -1) {
            this.solverContext.fail("Unexpected failure to read a card<br>Did a dialog pop up?<br>Did you set the challenge objective right?");
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
        if ((n4 = op_02.solverContext.complexity + n4) <= 0) {
            int n5 = op_02.solverContext.complexity;
            op_02.solverContext.complexity = n4;
            return n5;
        }
        return 999999;
    }

    final int a(int n2, int n3, int n4, double d2) {
        n4 = (int)((double)n2 + d2 * (double)n4);
        if (d2 > 0.0) {
            if (n2 > n3) {
                this.solverContext.fail("Incrementing by " + d2 + " but complexity base " + n2 + " greater than cap " + n3);
            }
            if (n4 > n3) {
                n4 = n3;
            }
        } else if (d2 < 0.0) {
            if (n2 < n3) {
                this.solverContext.fail("Decrementing by " + d2 + " but complexity base " + n2 + " less than cap " + n3);
            }
            if (n4 < n3) {
                n4 = n3;
            }
        }
        return n4;
    }

    final boolean equealData(CardStack os_02, CardStack os_03) {
        int n2 = this.solverContext.searchState.moves[this.solverContext.searchState.depth - 1];
        int n3 = (n2 & 0xF0000) >> 16;
        n2 = this.solverContext.searchState.moves[this.solverContext.searchState.depth - 1];
        int n4 = n2 >> 24 & 0xFFFFFFFE & 0xFFFFFFFD;
        int n5 = os_03.group.groupIndex * 10 + os_03.stackIndex;
        int n6 = os_02.group.groupIndex * 10 + os_02.stackIndex;
        int n7 = this.solverContext.searchState.depth - 2;
        while (n7 >= 0) {
            n2 = this.solverContext.searchState.moves[n7];
            int n8 = n2 >> 24 & 0xFFFFFFFE & 0xFFFFFFFD;
            if ((n8 & 8) != 0) break;
            if ((n8 & 4) == 0) {
                n2 = this.solverContext.searchState.moves[n7];
                int n9 = n2 >> 8 & 0xFF;
                n2 = this.solverContext.searchState.moves[n7];
                int n10 = n2 & 0xFF;
                if (n4 == n8 && ((n2 = this.solverContext.searchState.moves[n7]) & 0xF0000) >> 16 == n3 && n10 == n5) {
                    n8 = 1;
                    int n11 = this.solverContext.searchState.depth - 2;
                    while (n11 > n7) {
                        n2 = this.solverContext.searchState.moves[n11];
                        if ((n2 >> 24 & 4) == 0 && (((n2 = this.solverContext.searchState.moves[n11]) & 0xFF) == n5 || ((n2 = this.solverContext.searchState.moves[n11]) >> 8 & 0xFF) == n5)) {
                            n8 = 0;
                            break;
                        }
                        --n11;
                    }
                    if (n8 != 0) {
                        if (this.solverContext.logLevel < 3) {
                            this.solverContext.log("Move " + Move.a(this.solverContext.searchState.moves[this.solverContext.searchState.depth - 1]) + " is a reversal of " + Move.a(this.solverContext.searchState.moves[n7]));
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
        int n2 = this.solverContext.searchState.depth * 10 / this.H;
        if (n2 >= 10) {
            n2 = 9;
        }
        Long l3 = l2;
        if (this.R[n2].size() > this.bucket) {
            if (this.solverContext.logLevel <= 4) {
                this.solverContext.log(String.format("Discarding %d  hashes in bucket %d, counts %d/%d, %d/%d, %d/%d, %d/%d, %d/%d, %d/%d, %d/%d, %d/%d, %d/%d, %d/%d", this.bucket, n2, this.R[0].size(), this.S[0].size(), this.R[1].size(), this.S[1].size(), this.R[2].size(), this.S[2].size(), this.R[3].size(), this.S[3].size(), this.R[4].size(), this.S[4].size(), this.R[5].size(), this.S[5].size(), this.R[6].size(), this.S[6].size(), this.R[7].size(), this.S[7].size(), this.R[8].size(), this.S[8].size(), this.R[9].size(), this.S[9].size()));
            }
            this.S[n2] = this.R[n2];
            this.R[n2] = new HashMap(this.getBucket(n2));
        }
        this.R[n2].put(l3, this.solverContext.complexity << 16 | this.solverContext.searchState.depth);
    }

    final int equealData(long l2) {
        Long l3;
        Integer n2;
        int n3 = this.solverContext.searchState.depth * 10 / this.H;
        if (n3 >= 10) {
            n3 = 9;
        }
        if ((n2 = (Integer)this.R[n3].get(l3 = Long.valueOf(l2))) != null) {
            n3 = n2;
            int n4 = n3 & 0xFFFF;
            if (this.solverContext.complexity >= (n3 >>= 16) - 50 && (this.solverContext.files.maxMoves == 999 || this.solverContext.searchState.depth >= n4)) {
                if (this.K != null && this.n()) {
                    this.equealData(9);
                    this.solverContext.log("About to reject trial solution as a duplicate, hash = " + l2 + " overriding");
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
            if (this.solverContext.complexity >= (n3 >>= 16) - 50 && (this.solverContext.files.maxMoves == 999 || this.solverContext.searchState.depth >= n5)) {
                if (this.K != null && this.n()) {
                    this.equealData(9);
                    this.solverContext.log("About to reject trial solution as a duplicate, hash = " + l2 + " overriding");
                    return -1;
                }
                return 0;
            }
            return -1;
        }
        return -1;
    }

    final boolean equealData(int n2, boolean bl) {
        return this.a(1, bl, 0);
    }

    final boolean a(int n2, boolean bl, int n3) {
        HashMap hashMap = new HashMap(104);
        int n4 = this.a(hashMap);
        this.decksOfCards = n2;
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
                if (n10 > (this.decksOfCards << 2) / this.suitCount) {
                    n5 = n8 * 100 + n9;
                    n6 = n10;
                    if (this.solverContext.solverMode != 1) {
                        this.solverContext.fail("Incorrect count of " + n6 + " for the " + BaseSolver.h(n5));
                    } else {
                        this.solverContext.log("Incorrect count of " + n6 + " for the " + BaseSolver.h(n5));
                    }
                    n2 = 1;
                }
                ++n9;
            }
            ++n8;
        }
        if (n2 != 0) {
            this.solverContext.log("Suit point calculation: max club = " + this.solverContext.fontStats.e + " min spade = " + this.solverContext.fontStats.f);
            if (this.solverContext.solverMode == 1) {
                this.solverContext.log("*** ERROR, counted " + n4 + " cards, bad card " + n5 + " count of " + n6);
            } else {
                this.solverContext.fail("*** ERROR - counted " + n4 + " cards, bad card " + n5 + " count of " + n6);
            }
        }
        if (bl && n3 == 0 && n7 != this.decksOfCards * 52) {
            n2 = 1;
            if (this.solverContext.solverMode == 1) {
                this.solverContext.log("Needed " + this.decksOfCards * 52 + " cards but only read " + n7);
            } else {
                this.solverContext.fail("Needed " + this.decksOfCards * 52 + " cards but only read " + n7);
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
        if (this.solverContext.logLevel <= 2) {
            this.solverContext.log("Adding " + string + " card " + n2 + " giving " + n3);
        }
        hashMap.put(n4, n3);
    }

    final void getBucket() {
        if (this.solverContext.logLevel <= 3) {
            this.equealData(3);
            this.dumpState(3, false);
        }
        if (this.statusUpdateCounter++ > 10000) {
            this.statusUpdateCounter = 0;
        }
        if (this.K != null) {
            this.o();
        }
        if (this.solverContext.searchState.depth < this.solverContext.U) {
            this.solverContext.U = this.solverContext.searchState.depth;
        }
        if (this.solverContext.searchState.depth > this.deepestRecursionDepth) {
            this.deepestRecursionDepth = this.solverContext.searchState.depth;
            this.deepestRecursionComplexity = this.solverContext.complexity;
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
        if (this.solverContext.files.b == 0) {
            this.solverContext.fail("Need to have gameChallenge set");
        }
        if (this.solverContext.bk > 0 && System.currentTimeMillis() - this.b > 30000L) {
            return 1;
        }
        if (this.B) {
            return 2;
        }
        nY2.solutionLength = nY2.depth;
        if (this.solverContext.foundCompleteSolution && this.solverContext.bestSolutionState.solutionLength < nY2.solutionLength) {
            return 1;
        }
        n2 = 0;
        int n3 = this.a(nY2);
        boolean bl2 = this.a(nY2, n3);
        if (bl2) {
            n2 = 1;
        }
        if (this.solverContext.files.maxMoves < 999 && n3 > this.solverContext.files.maxMoves) {
            return 1;
        }
        if (this.solverContext.files.b == 6) {
            nY2.scoreByDepth[nY2.depth] = n3 = this.a(nY2, false);
            if (this.solverContext.logLevel <= 3) {
                this.solverContext.log("Current score is " + n3);
            }
            if (n3 >= this.solverContext.files.g) {
                n2 = 1;
                if (nY2.solutionLength < this.solverContext.files.maxMoves && (this.F < this.solverContext.files.g || this.solverContext.bestSolutionState.solutionLength > nY2.solutionLength)) {
                    this.F = n3;
                    this.a(nY2, "Can make target with " + this.F + " in " + nY2.solutionLength + " moves", true, true);
                }
            } else {
                boolean bl3;
                boolean bl4 = bl3 = n3 > 0 && (n3 > this.F || this.solverContext.bestSolutionState != null && n3 == this.F && this.solverContext.bestSolutionState.solutionLength > nY2.solutionLength);
                if (this.E) {
                    if (bl2 & bl3) {
                        this.F = n3;
                        this.a(nY2, "Can clear board with " + this.F + " in " + nY2.solutionLength + " moves", true, false);
                    }
                } else {
                    if (this.solverContext.variantId != 4 && this.solverContext.variantId != 5) {
                        bl2 = false;
                    }
                    if (bl3 || bl2) {
                        this.F = n3;
                        this.a(nY2, "Best score currently " + this.F + " in " + nY2.solutionLength + " moves", bl2, false);
                    }
                }
            }
            if (this.a(bl)) {
                this.solverContext.files.l = this.F;
                n2 = 2;
                if (this.solverContext.logLevel <= 5) {
                    this.solverContext.log("Board cleared with score standing at " + this.solverContext.files.l + " vs target of " + this.solverContext.files.g);
                }
                if (this.solverContext.files.g - this.solverContext.files.l <= 0) {
                    this.solverContext.files.a = true;
                }
            }
        } else if (this.solverContext.files.b == 3 || this.solverContext.files.b == 5 || this.solverContext.files.b == 4) {
            String string;
            int n4;
            int n5;
            if (this.solverContext.files.b == 5) {
                n3 = this.equealData(nY2, false);
                n5 = this.solverContext.files.n;
                n4 = this.solverContext.files.i - n5;
                string = "Best solution currently %d stacks in %d moves";
            } else {
                n3 = this.a(nY2, false, this.solverContext.files.d, this.solverContext.files.c);
                if (this.solverContext.files.b == 4) {
                    n5 = 0;
                    n4 = 1;
                    string = "Best solution currently %d card in %d moves";
                } else {
                    n5 = this.solverContext.files.m;
                    n4 = this.solverContext.files.h - n5;
                    string = "Best solution currently %d cards in %d moves";
                }
            }
            if (n3 >= n4) {
                n2 = 1;
                if (nY2.solutionLength < this.solverContext.files.maxMoves && (this.G < n4 || this.solverContext.bestSolutionState.solutionLength > nY2.solutionLength)) {
                    this.G = n3;
                    this.a(nY2, String.format(string, n5 + this.G, nY2.solutionLength), false, true);
                }
            } else {
                boolean bl5;
                boolean bl6 = bl5 = n3 > 0 && (n3 > this.G || n3 == this.G && this.solverContext.bestSolutionState.solutionLength > nY2.solutionLength);
                if (this.E) {
                    if (bl2 & bl5) {
                        this.G = n3;
                        this.a(nY2, String.format(string, n5 + this.G, nY2.solutionLength), true, false);
                        n2 = 1;
                    }
                } else {
                    if (this.solverContext.variantId != 4 && this.solverContext.variantId != 5) {
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
                if (this.solverContext.files.b == 3) {
                    this.solverContext.files.m += this.G;
                    if (this.solverContext.files.m >= this.solverContext.files.h) {
                        this.solverContext.files.a = true;
                    }
                } else if (this.solverContext.files.b == 4) {
                    this.solverContext.files.a = true;
                } else {
                    this.solverContext.files.n += this.G;
                    if (this.solverContext.files.n >= this.solverContext.files.i) {
                        this.solverContext.files.a = true;
                    }
                }
                this.G = 0;
                if (this.solverContext.logLevel <= 5) {
                    this.solverContext.log("Target card/stack count now " + n4);
                }
            }
        } else if (this.solverContext.files.b == 1 || this.solverContext.files.b == 2) {
            if (bl2) {
                n2 = 1;
                if (nY2.solutionLength < this.solverContext.files.maxMoves && (this.solverContext.bestSolutionState.solutionLength == 0 || nY2.solutionLength < this.solverContext.bestSolutionState.solutionLength)) {
                    this.a(nY2, "Best solution currently " + nY2.solutionLength + " moves", true, true);
                }
            }
            if (this.a(bl)) {
                n2 = 2;
                this.solverContext.files.k = this.solverContext.solverMode == 3 ? ++this.solverContext.files.k : 1;
                if (this.solverContext.logLevel <= 5) {
                    this.solverContext.log("Board cleared, accum now " + this.solverContext.files.k);
                }
                if (this.solverContext.files.k >= this.solverContext.files.e) {
                    this.solverContext.files.a = true;
                }
            } else if (!(!bl || this.solverContext.solverMode != 3 && this.solverContext.solverMode != 1 || this.solverContext.variantId != 4 && this.solverContext.variantId != 5)) {
                n2 = 2;
                this.solverContext.bestSolutionState.reset();
            }
        }
        if (n2 == 2) {
            this.B = true;
            if (this.solverContext.logLevel <= 9) {
                this.solverContext.log("Mode " + this.solverContext.solverMode + " (challenge " + this.solverContext.files.b + ") found a solution length " + this.solverContext.bestSolutionState.solutionLength + " in " + (System.currentTimeMillis() - this.b) / 1000L);
            }
            this.a(9, this.solverContext.bestSolutionState, "Solved best moves");
            this.equealData(false);
        }
        return n2;
    }

    private void a(GameState nY2, String string, boolean bl, boolean bl2) {
        if (this.solverContext.logLevel <= 5) {
            this.solverContext.log(string);
            this.dumpState(5, false);
        }
        this.solverContext.bestSolutionState = new GameState(nY2, true);
        if (bl) {
            this.E = true;
        }
        this.c = System.currentTimeMillis();
        if (bl2) {
            this.solverContext.foundCompleteSolution = true;
        }
    }

    private boolean a(boolean bl) {
        if (this.solverContext.bestSolutionState.solutionLength == 0) {
            return false;
        }
        if (bl || this.solverContext.searchStepCount % 1000L == 0L) {
            long l2 = System.currentTimeMillis();
            if (this.solverContext.bridge.lastBridgeUpdateTimeMs > this.c) {
                this.c = this.solverContext.bridge.lastBridgeUpdateTimeMs;
            }
            if (bl || this.solverContext.aG || this.solverContext.aF == 0 || l2 - this.c > (long)this.solverContext.aF) {
                this.solverContext.aG = false;
                if (this.solverContext.foundCompleteSolution || this.E) {
                    if (this.solverContext.logLevel <= 5) {
                        String string = "Test final (forced " + bl + ") best moves";
                        this.solverContext.log("Best solution length " + this.solverContext.bestSolutionState.solutionLength);
                        this.a(5, this.solverContext.bestSolutionState, string);
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

    int equealData(GameState nY2, boolean bl) {
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
        if (this.solverContext.initialState.stackGroups == null) {
            return;
        }
        if (this.solverContext.initialState.stackGroups[0] == null) {
            return;
        }
        int n2 = this.r[0].length;
        int n3 = 0;
        while (n3 < this.stackSize) {
            if (n3 >= this.solverContext.initialState.stackGroups[0].stacks.length) break;
            CardStack os_02 = this.solverContext.initialState.stackGroups[0].stacks[n3];
            int n4 = 0;
            for (Object object : os_02.runs) {
                CardRun ok_02 = (CardRun)object;
                int n5 = 0;
                while (n5 < ok_02.cardCount) {
                    this.r[n3][n4] = ok_02.cards[n5].cardId == 0 ? (!this.B && n3 == this.solverContext.bridge.readHighlightColumnIndex && n4 == this.solverContext.bridge.readHighlightRowIndex ? 3 : 2) : 4;
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

    final int[] equealData(HashMap hashMap, int n2) {
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

    final void printLog(int n2) {
        if (n2 >= this.solverContext.logLevel && this.solverContext.searchState.depth > 0) {
            StringBuffer stringBuffer = new StringBuffer(String.format("Lvl %3d move %8s", this.solverContext.searchState.depth - 1, Move.a(this.solverContext.searchState.moves[this.solverContext.searchState.depth - 1])));
            this.solverContext.log(stringBuffer.toString());
        }
    }

    static int a(StackGroup stackGroup, int n2) {
        if (stackGroup == null) {
            return 0;
        }
        CardStack[] os_0Array = stackGroup.stacks;
        int n3 = stackGroup.stacks.length;
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

    static int equealData(StackGroup stackGroup, int n2) {
        if (stackGroup == null) {
            return 0;
        }
        int[] nArray = new int[4];
        int n3 = 0;
        int n4 = 0;
        CardStack[] os_0Array = stackGroup.stacks;
        int n5 = stackGroup.stacks.length;
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

    final void k(int n2) {
        if (this.solverContext.ag) {
            GameState nY2 = this.state ? this.solverContext.searchState : this.solverContext.bestSolutionState;
            if (this.solverContext.ag && nY2.moves != null && this.equealData(n2, nY2.moves[this.solverContext.playbackMoveIndex])) {
                if (this.solverContext.logLevel <= 5) {
                    this.solverContext.log("This is a matching move so do single step");
                }
                this.solverContext.aX = true;
                this.solverContext.playbackState.depth = SolverBridge.a(nY2.moves, this.solverContext.playbackState.moves, nY2.depth);
                return;
            }
            if (this.solverContext.logLevel <= 5) {
                this.solverContext.log("Non-matching move so discard previous solution");
            }
            this.solverContext.aX = false;
            this.solverContext.playbackState.depth = SolverBridge.a(nY2.moves, this.solverContext.playbackState.moves, this.solverContext.playbackMoveIndex);
            this.solverContext.playbackState.moves[this.solverContext.playbackState.depth] = n2;
            ++this.solverContext.playbackState.depth;
            this.solverContext.bestSolutionState.moves = null;
            this.solverContext.bestSolutionState.reset();
            this.B = false;
            this.solverContext.foundCompleteSolution = false;
            return;
        }
        if (this.solverContext.playbackState.depth < 350) {
            this.solverContext.playbackState.moves[this.solverContext.playbackState.depth] = n2;
            ++this.solverContext.playbackState.depth;
            return;
        }
    }

    final Card equealData(CardStack cardStack, int cardData) {
        if (this.cardIndex == this.o) {
            this.solverContext.fail("Trying to allocate more than " + this.o + " cards");
        }
        if (this.solverContext.logLevel <= 5) {
            this.solverContext.log("@@@ Allocating card #" + this.cardIndex + " value " + cardData);
        }
        Card card = this.cardArray[this.cardIndex++];
        card.dictCardValue(cardData);
        card.stack = cardStack;
        card.primaryUiHandle = null;
        card.secondaryUiHandle = null;
        return card;
    }

    boolean equealData(int n2, int n3) {
        return n2 == n3;
    }
}





