/*
 * Decompiled with CFR 0.152.
 */
package com.solvitaire.app;

import com.solvitaire.app.CardRun;
import com.solvitaire.app.SolverContext;
import com.solvitaire.app.CardStack;
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
        int n6 = 0;
        while (n6 < stackCount) {
            this.stacks[n6] = new CardStack(this.context, this, n6, (this.flags & 8) != 0);
            ++n6;
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
        int n2 = 0;
        while (n2 < sourceGroup.stacks.length) {
            this.stacks[n2] = new CardStack(this, sourceGroup.stacks[n2]);
            this.stacks[n2].workingCopy = workingCopy;
            ++n2;
        }
    }

    final void setOrigin(double x, double y) {
        this.origin = new Point2D.Double(x, y);
        this.stacks[0].xPosition = this.origin.x;
    }

    final int countCards() {
        int n2 = 0;
        int n3 = 0;
        while (n3 < this.stackCount) {
            n2 += this.stacks[n3].getCardCount();
            ++n3;
        }
        return n2;
    }

    final int addCompletedSuitRun(CardRun completedSuitRun) {
        if ((this.flags & 0x40) == 0) {
            this.context.fail("Cannot add a run to a stackset that is not SpiderSuits");
        }
        if (completedSuitRun.cardCount != 13) {
            this.context.fail("Trying to remove suit run that is not a full suit");
        }
        int n2 = 0;
        while (n2 < this.stacks.length) {
            if (this.stacks[n2].topRun == null) break;
            ++n2;
        }
        if (n2 == 8) {
            this.context.fail("Add of suit stack when no available slots");
        }
        this.stacks[n2].appendRun(completedSuitRun);
        return n2;
    }

    final CardRun removeCompletedSuitRun() {
        if ((this.flags & 0x40) == 0) {
            this.context.fail("Cannot remove a run from a stackset that is not SpiderSuits");
        }
        int n2 = -1;
        int n3 = 0;
        while (n3 < this.stacks.length) {
            if (this.stacks[n3].topRun == null) break;
            n2 = n3++;
        }
        if (n2 < 0) {
            this.context.fail("Remove of suit stack when none available");
        }
        CardRun completedSuitRun = this.stacks[n2].popTopRun();
        return completedSuitRun;
    }

    final int a() {
        return this.countCards();
    }

    final int a(CardRun completedSuitRun) {
        return this.addCompletedSuitRun(completedSuitRun);
    }

    final CardRun b() {
        return this.removeCompletedSuitRun();
    }
}





