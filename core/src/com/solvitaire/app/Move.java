/*
 * Decompiled with CFR 0.152.
 */
package com.solvitaire.app;

import com.solvitaire.app.SolverBridge;
import com.solvitaire.app.SolverContext;
import com.solvitaire.app.CardStack;
import com.solvitaire.app.StackGroup;
import java.util.Vector;

public final class Move {
    int rawMove;
    int moveTypeFlags;
    StackGroup destinationGroup;
    StackGroup sourceGroup;
    CardStack destinationStack;
    int destinationStackIndex;
    CardStack sourceStack;
    int sourceStackIndex;
    int movedCardCount;
    int suppliedFlags;
    int specialDestinationCode;
    int specialSourceCode;
    int specialCardCount;
    boolean specialMove;
    boolean autoMove;
    boolean splitMove;

    Move(SolverContext context, int rawMove, int suppliedFlags) {
        this.suppliedFlags = suppliedFlags;
        this.rawMove = rawMove;
        int moveBits = rawMove;
        this.moveTypeFlags = moveBits >> 24;
        this.specialMove = (this.moveTypeFlags & 8) != 0;
        this.autoMove = (this.moveTypeFlags & 0x10) != 0;
        suppliedFlags = Move.e(rawMove);
        int sourceGroupIndex = Move.c(rawMove);
        this.destinationStackIndex = Move.f(rawMove);
        this.sourceStackIndex = Move.d(rawMove);
        moveBits = rawMove;
        this.movedCardCount = (moveBits & 0xF0000) >> 16;
        if (this.specialMove) {
            moveBits = rawMove;
            this.specialDestinationCode = moveBits & 0xFF;
            moveBits = rawMove;
            this.specialSourceCode = moveBits >> 8 & 0xFF;
            moveBits = rawMove;
            this.specialCardCount = moveBits >> 16 & 0xFF;
            if (context.bridge.specialDestinationGroupIndex >= 0) {
                this.destinationGroup = context.initialState.stackGroups[context.bridge.specialDestinationGroupIndex];
                this.destinationStack = this.destinationGroup.stacks[0];
            }
            if (context.bridge.specialSourceGroupIndex >= 0) {
                this.sourceGroup = context.initialState.stackGroups[context.bridge.specialSourceGroupIndex];
                this.sourceStack = this.sourceGroup.stacks[0];
                return;
            }
        } else {
            this.destinationGroup = context.initialState.stackGroups[suppliedFlags];
            this.sourceGroup = context.initialState.stackGroups[sourceGroupIndex];
            this.destinationStack = this.destinationGroup == null ? null : this.destinationGroup.stacks[this.destinationStackIndex];
            this.sourceStack = this.sourceGroup == null ? null : this.sourceGroup.stacks[this.sourceStackIndex];
            this.splitMove = (this.moveTypeFlags & 1) != 0;
        }
    }

    public final String toString() {
        return this.movedCardCount + " cards, source " + this.sourceStack + " dest " + this.destinationStack + " auto:" + this.autoMove + " split:" + this.splitMove;
    }

    static String a(int n2) {
        int n3 = n2;
        if ((n3 >>= 24) == 0) {
            Object[] objectArray = new Object[3];
            n3 = n2;
            objectArray[0] = (n3 & 0xF0000) >> 16;
            n3 = n2;
            objectArray[1] = n3 >> 8 & 0xFF;
            n3 = n2;
            objectArray[2] = n3 & 0xFF;
            return String.format("%d%02d%02d", objectArray);
        }
        Object[] objectArray = new Object[4];
        n3 = n2;
        objectArray[0] = n3 >> 24;
        n3 = n2;
        objectArray[1] = (n3 & 0xF0000) >> 16;
        n3 = n2;
        objectArray[2] = n3 >> 8 & 0xFF;
        n3 = n2;
        objectArray[3] = n3 & 0xFF;
        return String.format("%d%02d%02d%02d", objectArray);
    }

    static int b(int n2) {
        int n3 = n2 / 1000000;
        int n4 = n2 / 10000 % 100;
        int n5 = n2 / 100 % 100;
        n2 %= 100;
        if (n4 > 13) {
            n3 |= 1;
            n4 %= 20;
        }
        return n3 << 24 | n4 << 16 | n5 << 8 | n2;
    }

    static int c(int n2) {
        return (n2 >> 8 & 0xFF) / 10;
    }

    static int d(int n2) {
        return (n2 >> 8 & 0xFF) % 10;
    }

    static int e(int n2) {
        return (n2 & 0xFF) / 10;
    }

    static int f(int n2) {
        return (n2 & 0xFF) % 10;
    }

    static int a(int n2, int n3, CardStack os_02, CardStack os_03, boolean bl) {
        int n4 = 0;
        if (bl) {
            n4 = 2;
        }
        if (n2 > 0) {
            if (n2 > 100) {
                n4 |= n2 / 100 << 4;
            }
            if (n2 % 100 > 20) {
                n4 |= 1;
            }
            n3 = n2 % 20;
        }
        n2 = os_02 == null ? 0 : os_02.group.groupIndex * 10 + os_02.stackIndex;
        int n5 = os_03 == null ? 0 : os_03.group.groupIndex * 10 + os_03.stackIndex;
        return n4 << 24 | n3 << 16 | n2 << 8 | n5;
    }

    static int a(int n2, int n3, CardStack os_02, CardStack os_03) {
        if (n3 > 13) {
            n2 |= 1;
            n3 %= 20;
        }
        int n4 = os_02 == null ? 0 : os_02.group.groupIndex * 10 + os_02.stackIndex;
        int n5 = os_03 == null ? 0 : os_03.group.groupIndex * 10 + os_03.stackIndex;
        return n2 << 24 | n3 << 16 | n4 << 8 | n5;
    }

    static int a(int n2, int n3, int n4, int n5) {
        return n2 << 24 | n3 << 16 | n4 << 8 | n5;
    }

    static boolean a(int n2, int n3) {
        return ((n2 ^ n3) & 0xFFFFFF) == 0 && (n2 &= 0x8000000) == (n3 &= 0x8000000);
    }

    static String[] a(SolverBridge e_02, int[] nArray, int n2, int n3, boolean bl) {
        Vector<String> vector = new Vector<String>();
        if (bl) {
            --n2;
            while (n2 >= n3) {
                int n4;
                int n5 = n4 = nArray[n2];
                n5 = n4 >> 24;
                if ((n5 & 4) == 0) {
                    vector.add(String.format(" %3d.\t Undo %s", n2, e_02.a(n4, n5)));
                }
                --n2;
            }
        } else {
            n2 = 0;
            while (n2 < n3) {
                int n6 = nArray[n2];
                int n7 = n6 >> 24;
                if ((n7 & 4) == 0) {
                    vector.add(String.format(" %3d.\t %s", n2, e_02.a(n6, n7)));
                }
                ++n2;
            }
        }
        return vector.toArray(new String[1]);
    }
}





