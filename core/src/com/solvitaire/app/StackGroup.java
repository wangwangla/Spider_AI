/*
 * Decompiled with CFR 0.152.
 */
package com.solvitaire.app;

import java.awt.geom.Point2D;

/*
 * Renamed from com.solvitaire.app.ot
 */
public final class StackGroup {
    private final SolverContext context;
    int groupIndex;
    private int layoutMode;
    String name;
    int stackCount;
    int flags;
    int emptyStackCount;
    CardStack[] stacks;
    Point2D.Double origin;
    double height;
    double[] offsets;

    StackGroup(SolverContext context, String name, int groupIndex, int stackCount, int layoutMode, int flags) {
        this.context = context;
        this.groupIndex = groupIndex;
        this.layoutMode = layoutMode;
        this.name = name;
        this.stackCount = stackCount;
        this.flags = flags;
        this.stacks = new CardStack[stackCount];
        int stackIndex = 0;
        while (stackIndex < stackCount) {
            this.stacks[stackIndex] = new CardStack(this.context, this, stackIndex, (this.flags & 8) != 0);
            ++stackIndex;
        }
        this.emptyStackCount = stackCount;
        this.offsets = new double[6];
    }

    StackGroup(StackGroup sourceGroup, boolean workingCopy) {
        this(sourceGroup.context, sourceGroup.name, sourceGroup.groupIndex, sourceGroup.stackCount, sourceGroup.layoutMode, sourceGroup.flags);
        this.origin = sourceGroup.origin;
        this.height = sourceGroup.height;
        this.offsets = sourceGroup.offsets;
        this.emptyStackCount = sourceGroup.emptyStackCount;
        int stackIndex = 0;
        while (stackIndex < sourceGroup.stacks.length) {
            this.stacks[stackIndex] = new CardStack(this, sourceGroup.stacks[stackIndex]);
            this.stacks[stackIndex].workingCopy = workingCopy;
            ++stackIndex;
        }
    }

    final int countCards() {
        int cardCount = 0;
        int stackIndex = 0;
        while (stackIndex < this.stackCount) {
            cardCount += this.stacks[stackIndex].getCardCount();
            ++stackIndex;
        }
        return cardCount;
    }

    final int addCompletedSuitRun(CardRun completedSuitRun) {
        if ((this.flags & 0x40) == 0) {
            this.context.fail("Cannot add a run to a stackset that is not SpiderSuits");
        }
        if (completedSuitRun.cardCount != 13) {
            this.context.fail("Trying to remove suit run that is not a full suit");
        }
        int stackIndex = 0;
        while (stackIndex < this.stacks.length) {
            if (this.stacks[stackIndex].topRun == null) break;
            ++stackIndex;
        }
        if (stackIndex == 8) {
            this.context.fail("Add of suit stack when no available slots");
        }
        this.stacks[stackIndex].appendRun(completedSuitRun);
        return stackIndex;
    }

    CardRun removeCompletedSuitRun() {
        if ((this.flags & 0x40) == 0) {
            this.context.fail("Cannot remove a run from a stackset that is not SpiderSuits");
        }
        int stackIndex = -1;
        int stackIndexTemp = 0;
        while (stackIndexTemp < this.stacks.length) {
            if (this.stacks[stackIndexTemp].topRun == null) break;
            stackIndex = stackIndexTemp++;
        }
        if (stackIndex < 0) {
            this.context.fail("Remove of suit stack when none available");
        }
        CardRun completedSuitRun = this.stacks[stackIndex].popTopRun();
        return completedSuitRun;
    }
}





