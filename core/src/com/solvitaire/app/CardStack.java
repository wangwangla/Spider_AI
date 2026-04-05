package com.solvitaire.app;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

final class CardStack {
    final SolverContext context;
    StackGroup group;
    int stackIndex;
    CardRun topRun = null;
    LinkedList<CardRun> runs = new LinkedList<>();
    int foundationSuit;
    boolean alternatingColors;
    protected boolean knownStateLocked;
    private boolean cardsKnown;
    boolean workingCopy = false;
    boolean reservedFlag3 = false;
    long reservedLong1;
    long reservedLong2;
    double xPosition;
    boolean reservedFlag1;
    boolean reservedFlag2;
    StackSnapshot stackSnapshot;

    CardStack(SolverContext context, StackGroup group, int stackIndex, boolean alternatingColors) {
        this.context = context;
        this.group = group;
        this.stackIndex = stackIndex;
        this.foundationSuit = 0;
        this.alternatingColors = alternatingColors;
        this.clear();
    }

    CardStack(StackGroup group, CardStack sourceStack) {
        this.context = sourceStack.context;
        this.stackIndex = sourceStack.stackIndex;
        this.foundationSuit = sourceStack.foundationSuit;
        this.alternatingColors = sourceStack.alternatingColors;
        this.knownStateLocked = sourceStack.knownStateLocked;
        this.clear();
        this.group = group;
        for (CardRun sourceRun : sourceStack.runs) {
            CardRun copiedRun = new CardRun(sourceRun);
            copiedRun.stack = this;
            this.runs.add(copiedRun);
        }
        this.topRun = this.runs.isEmpty() ? null : this.runs.getLast();
        this.cardsKnown = sourceStack.cardsKnown;
        this.xPosition = sourceStack.xPosition;
        this.reservedFlag1 = sourceStack.reservedFlag1;
        this.reservedFlag2 = sourceStack.reservedFlag2;
        this.workingCopy = sourceStack.workingCopy;
        this.reservedFlag3 = sourceStack.reservedFlag3;
        this.reservedLong1 = sourceStack.reservedLong1;
        this.reservedLong2 = sourceStack.reservedLong2;
        this.stackSnapshot = sourceStack.stackSnapshot;
    }

    final Card getTopCard() {
        return this.topRun != null && this.topRun.cardCount != 0 ? this.topRun.cards[this.topRun.cardCount - 1] : null;
    }

    final int getTopCardValue() {
        return this.topRun != null && this.topRun.cardCount != 0 ? this.topRun.cards[this.topRun.cardCount - 1].cardId : -1;
    }

    final int getTopRank() {
        return this.topRun != null && this.topRun.cardCount != 0 ? this.topRun.cards[this.topRun.cardCount - 1].rank : 0;
    }

    final CardRun appendRun(CardRun run) {
        if (run.cardCount == 0) {
            this.context.fail("ERROR adding empty run to stack");
        }
        if (this.topRun == null && this.group != null) {
            --this.group.emptyStackCount;
        }
        this.runs.add(run);
        run.stack = this;
        this.topRun = run;
        return this.topRun;
    }

    final void removeRun(CardRun run) {
        this.runs.remove(run);
        if (this.runs.isEmpty()) {
            this.topRun = null;
            if (this.group != null) {
                ++this.group.emptyStackCount;
            }
            return;
        }
        this.topRun = this.runs.getLast();
    }

    final CardRun popFirstRun() {
        CardRun removedRun = null;
        if (!this.runs.isEmpty()) {
            removedRun = this.runs.removeFirst();
            if (this.runs.isEmpty()) {
                this.topRun = null;
                if (this.group != null) {
                    ++this.group.emptyStackCount;
                }
            } else {
                this.topRun = this.runs.getLast();
            }
        }
        return removedRun;
    }

    final CardRun popTopRun() {
        CardRun removedRun = null;
        if (!this.runs.isEmpty()) {
            removedRun = this.runs.removeLast();
            if (this.runs.isEmpty()) {
                this.topRun = null;
                if (this.group != null) {
                    ++this.group.emptyStackCount;
                }
            } else {
                this.topRun = this.runs.getLast();
            }
        }
        return removedRun;
    }

    final void prependRun(CardRun run) {
        if (this.runs.isEmpty()) {
            this.appendRun(run);
            if (this.group != null) {
                --this.group.emptyStackCount;
            }
        } else {
            this.runs.offerFirst(run);
        }
        run.stack = this;
        this.topRun = this.runs.getLast();
    }

    final void clear() {
        this.runs = new LinkedList<>();
        this.topRun = null;
        if (this.group != null) {
            this.group.emptyStackCount = this.group.stacks.length;
        }
        if (!this.knownStateLocked) {
            this.cardsKnown = true;
        }
        this.workingCopy = false;
        this.reservedFlag2 = false;
    }

    final void lockKnownState() {
        this.knownStateLocked = true;
        this.cardsKnown = false;
    }

    final boolean hasKnownCards() {
        return this.cardsKnown;
    }

    final void markCardsKnown() {
        if (!this.knownStateLocked) {
            this.cardsKnown = true;
        }
    }

    final void markCardsUnknown() {
        this.cardsKnown = false;
    }

    final int evaluateJoinFrom(CardStack sourceStack, int moveMode, boolean exactMatchOnly) {
        int joinCount = -1;
        if (sourceStack.topRun == null) {
            return -1;
        }
        if (moveMode == 2 || moveMode == 6) {
            if (this.topRun != null) {
                return joinCount;
            }
            if (moveMode != 6) {
                return sourceStack.topRun.cardCount;
            }
            if (sourceStack.topRun.cardCount != 1) {
                return 1;
            }
            return 0;
        }
        if (moveMode == 1) {
            int directJoinCount = this.evaluateJoin(this.topRun, sourceStack.topRun, false, exactMatchOnly);
            if (directJoinCount > 0) {
                joinCount = directJoinCount;
            }
            return joinCount;
        }
        if (moveMode == 3) {
            if (sourceStack.runs.size() != 1) {
                return joinCount;
            }
            int directJoinCount = this.evaluateJoin(this.topRun, sourceStack.topRun, false, exactMatchOnly);
            if (directJoinCount > 0) {
                return directJoinCount;
            }
            if (this.evaluateJoin(this.topRun, sourceStack.topRun, true, exactMatchOnly) != 0) {
                return joinCount;
            }
        } else {
            int splitJoinCount = this.evaluateJoin(this.topRun, sourceStack.topRun, true, exactMatchOnly);
            if (exactMatchOnly) {
                return splitJoinCount;
            }
            if (splitJoinCount < 0) {
                return joinCount;
            }
            if (splitJoinCount > 0) {
                this.context.fail("Mismatched join caused split");
                return joinCount;
            }
            int sourceRunCount = sourceStack.runs.size();
            if (sourceRunCount < 2) {
                return -1;
            }
            joinCount = 0;
            CardRun previousRun = sourceStack.runs.get(sourceRunCount - 2);
            Card previousTopCard = previousRun.cards[previousRun.cardCount - 1];
            Card firstSourceCard = sourceStack.topRun.cards[0];
            if (previousTopCard.rank == firstSourceCard.rank + 1) {
                joinCount = 1;
            }
            if (joinCount != 0 && moveMode == 4 || joinCount == 0 && moveMode == 5) {
                return -1;
            }
        }
        return 0;
    }

    final int evaluateJoin(CardRun destinationRun, CardRun sourceRun, boolean allowSplit, boolean allowPartialJoin) {
        int joinCount = -1;
        Card sourceTopCard = sourceRun.cards[sourceRun.cardCount - 1];
        if (sourceTopCard == null) {
            return -1;
        }
        if (this.foundationSuit != 0) {
                if (destinationRun == null) {
                    if (this.foundationSuit > 0) {
                    if (sourceTopCard.cardId == this.foundationSuit * 100 + 1) {
                        joinCount = 1;
                    }
                } else if (sourceTopCard.rank == 1) {
                    joinCount = 1;
                }
            } else if (sourceTopCard.cardId == destinationRun.cards[destinationRun.cardCount - 1].cardId + 1) {
                joinCount = 1;
            }
        } else if (destinationRun != null) {
            Card destinationTopCard = destinationRun.cards[destinationRun.cardCount - 1];
            if (sourceRun.cardCount > 0) {
                if (!this.alternatingColors) {
                    if (!allowSplit) {
                        joinCount = destinationRun.a(destinationTopCard, sourceTopCard, sourceRun.cardCount, true);
                        if (!allowPartialJoin && joinCount + destinationRun.cardCount <= sourceRun.cardCount) {
                            joinCount = -1;
                        }
                    } else if ((joinCount = destinationRun.a(destinationTopCard, sourceTopCard, sourceRun.cardCount, false)) != sourceRun.cardCount) {
                        if (!allowPartialJoin) {
                            joinCount = -1;
                        }
                    } else {
                        joinCount = 0;
                    }
                } else if ((joinCount = destinationRun.a(destinationTopCard, sourceTopCard, sourceRun.cardCount, false)) > 0
                        && !(joinCount % 2 == 0 ^ CardRun.a(destinationTopCard, sourceTopCard))) {
                    joinCount = -1;
                }
            }
        }
        return joinCount;
    }

    final int moveCardsFrom(CardStack sourceStack, int cardCount, StackGroup completedSuitGroup) {
        if (cardCount > 0) {
            if (this.topRun == null) {
                CardRun movedRun = new CardRun();
                cardCount = movedRun.a(sourceStack.topRun, cardCount);
                this.appendRun(movedRun);
            } else {
                if (this.context.logLevel <= 2) {
                    this.context.log("Joining card " + this.topRun.cards[this.topRun.cardCount - 1] + " with card " + sourceStack.topRun.cards[0]);
                }
                cardCount = this.topRun.a(sourceStack.topRun, cardCount);
            }
            if (this.topRun.cardCount == 13 && completedSuitGroup != null) {
                cardCount += 100 * this.topRun.cards[0].suit;
                CardRun completedSuitRun = this.popTopRun();
                completedSuitGroup.addCompletedSuitRun(completedSuitRun);
            }
        } else {
            this.appendRun(sourceStack.topRun);
        }
        if (cardCount > 0 && cardCount % 20 < sourceStack.topRun.cardCount) {
            sourceStack.topRun.cardCount -= cardCount % 20;
        } else {
            sourceStack.removeRun(sourceStack.topRun);
        }
        return cardCount;
    }

    final void undoMoveCardsFrom(CardStack sourceStack, int cardCount, StackGroup completedSuitGroup) {
        if (cardCount > 0 && cardCount != 20) {
            if (cardCount > 100) {
                int removedSuit = cardCount / 100;
                cardCount %= 100;
                CardRun completedSuitRun = completedSuitGroup.removeCompletedSuitRun();
                if (completedSuitRun.cards[0].suit != removedSuit) {
                    this.context.fail("Logic error - move says remove suit " + removedSuit + " but suit stack is " + completedSuitRun.cards[0].suit);
                }
                this.appendRun(completedSuitRun);
            }
            if (cardCount > 20) {
                cardCount -= 20;
                for (int cardIndex = 0; cardIndex < cardCount; ++cardIndex) {
                    sourceStack.topRun.cards[sourceStack.topRun.cardCount + cardIndex] = this.topRun.cards[this.topRun.cardCount - cardCount + cardIndex];
                }
                sourceStack.topRun.cardCount += cardCount;
            } else {
                CardRun restoredRun = new CardRun();
                for (int cardIndex = 0; cardIndex < cardCount; ++cardIndex) {
                    restoredRun.cards[cardIndex] = this.topRun.cards[this.topRun.cardCount - cardCount + cardIndex];
                }
                restoredRun.cardCount = cardCount;
                sourceStack.appendRun(restoredRun);
            }
            this.topRun.cardCount -= cardCount;
            if (this.topRun.cardCount != 0) {
                return;
            }
        } else if (cardCount == 20) {
            for (int cardIndex = 0; cardIndex < this.topRun.cardCount; ++cardIndex) {
                sourceStack.topRun.cards[sourceStack.topRun.cardCount++] = this.topRun.cards[cardIndex];
            }
        } else {
            sourceStack.appendRun(this.topRun);
        }
        this.removeRun(this.topRun);
    }

    final CardRun getPreviousRun() {
        int runCount = this.runs.size();
        return runCount < 2 ? null : this.runs.get(runCount - 2);
    }

    final Rectangle2D.Double getScreenBounds() {
        Rectangle2D.Double screenBounds = this.getLocalBounds();
        screenBounds.x += this.context.table.screenOrigin.a();
        screenBounds.y += this.context.table.screenOrigin.b();
        return screenBounds;
    }

    final Rectangle getScreenRectangle() {
        Rectangle2D.Double screenBounds = this.getScreenBounds();
        return new Rectangle((int)screenBounds.x, (int)screenBounds.y, (int)screenBounds.width, (int)screenBounds.height);
    }

    final Rectangle getLocalRectangle() {
        Rectangle2D.Double localBounds = this.getLocalBounds();
        return new Rectangle((int)localBounds.x, (int)localBounds.y, (int)localBounds.width, (int)localBounds.height);
    }

    final Rectangle2D.Double getLocalBounds() {
        if (this.group.height < (double)(this.context.table.cardHeight / 2)) {
            this.context.fail("Error, stack " + this.group.name + " height is less than half the card Height");
        }
        if (this.group.origin == null) {
            this.context.fail("StackSet " + this.group.name + " origin has not been set yet");
        }
        Rectangle2D.Double localBounds = new Rectangle2D.Double();
        localBounds.x = this.xPosition;
        localBounds.y = this.group.origin.y;
        if ((this.group.flags & 16) != 0) {
            localBounds.width = this.context.table.cardWidth * 1.45;
            localBounds.height = this.group.height;
            if (this.context.logLevel <= 3) {
                this.context.log("Triplet stack " + this + " bounds " + SolverContext.describe(localBounds));
            }
        } else if ((this.group.flags & 64) != 0) {
            localBounds.width = this.context.table.cardWidth;
            localBounds.height = this.group.height;
            if (this.context.logLevel <= 3) {
                this.context.log("Spider suits stack " + this + " bounds " + SolverContext.describe(localBounds));
            }
        } else {
            localBounds.width = this.context.table.cardWidth;
            localBounds.height = (double)((int)this.group.height);
            if ((this.group.flags & 2) != 0) {
                if (this.context.logLevel <= 3) {
                    this.context.log("Horizontal stack " + this + " bounds " + SolverContext.describe(localBounds));
                }
            } else if (this.context.logLevel <= 3) {
                this.context.log("Standard stack " + this + " bounds " + SolverContext.describe(localBounds) + " cards: " + this.stackSnapshot);
            }
        }
        return localBounds;
    }

    static void swapRuns(CardStack firstStack, CardStack secondStack) {
        LinkedList<CardRun> swappedRuns = firstStack.runs;
        firstStack.runs = secondStack.runs;
        secondStack.runs = swappedRuns;
        firstStack.topRun = firstStack.runs.isEmpty() ? null : firstStack.runs.getLast();
        secondStack.topRun = secondStack.runs.isEmpty() ? null : secondStack.runs.getLast();
    }

    // Legacy wrappers keep the rest of the solver compiling while call sites are renamed.
    final Card a() {
        return this.getTopCard();
    }

    final int b() {
        return this.getTopCardValue();
    }

    final int c() {
        return this.getTopRank();
    }

    final CardRun a(CardRun run) {
        return this.appendRun(run);
    }

    final void b(CardRun run) {
        this.removeRun(run);
    }

    final CardRun d() {
        return this.popFirstRun();
    }

    final CardRun e() {
        return this.popTopRun();
    }

    final void c(CardRun run) {
        this.prependRun(run);
    }

    final void f() {
        this.clear();
    }

    final void g() {
        this.lockKnownState();
    }

    final boolean h() {
        return this.hasKnownCards();
    }

    final void i() {
        this.markCardsKnown();
    }

    final void j() {
        this.markCardsUnknown();
    }

    final int a(CardStack sourceStack, int moveMode, boolean exactMatchOnly) {
        return this.evaluateJoinFrom(sourceStack, moveMode, exactMatchOnly);
    }

    final int a(CardRun destinationRun, CardRun sourceRun, boolean allowSplit, boolean allowPartialJoin) {
        return this.evaluateJoin(destinationRun, sourceRun, allowSplit, allowPartialJoin);
    }

    final int a(CardStack sourceStack, int cardCount, StackGroup completedSuitGroup) {
        return this.moveCardsFrom(sourceStack, cardCount, completedSuitGroup);
    }

    final void b(CardStack sourceStack, int cardCount, StackGroup completedSuitGroup) {
        this.undoMoveCardsFrom(sourceStack, cardCount, completedSuitGroup);
    }

    final CardRun k() {
        return this.getPreviousRun();
    }

    final Rectangle2D.Double l() {
        return this.getScreenBounds();
    }

    final Rectangle m() {
        return this.getScreenRectangle();
    }

    final Rectangle n() {
        return this.getLocalRectangle();
    }

    final Rectangle2D.Double o() {
        return this.getLocalBounds();
    }

    static void a(CardStack firstStack, CardStack secondStack) {
        CardStack.swapRuns(firstStack, secondStack);
    }

    final int p() {
        return this.getCheckedCardCount();
    }

    final int q() {
        return this.getCardCount();
    }

    final Card a(int index) {
        return this.getCardAt(index);
    }

    final boolean b(int cardValue) {
        return this.containsCardValue(cardValue);
    }

    final void a(boolean flagged) {
        this.setTopRunFlagged(flagged);
    }

    final int r() {
        return this.countFlaggedRuns();
    }

    final int getCheckedCardCount() {
        if (this.workingCopy) {
            this.context.a();
            this.context.fail("Cannot get the card count of a work stack");
        }
        if (!this.cardsKnown) {
            return 0;
        }
        int cardCount = 0;
        for (CardRun run : this.runs) {
            cardCount += run.cardCount;
        }
        return cardCount;
    }

    final int getCardCount() {
        if (!this.cardsKnown) {
            return 0;
        }
        int cardCount = 0;
        for (CardRun run : this.runs) {
            cardCount += run.cardCount;
        }
        return cardCount;
    }

    final Card getCardAt(int index) {
        Card foundCard = null;
        int remainingIndex = index;
        for (CardRun run : this.runs) {
            if (remainingIndex - run.cardCount < 0) {
                if (remainingIndex < 0) {
                    return null;
                }
                foundCard = run.cards[remainingIndex];
                foundCard.parentRun = run;
                foundCard.runIndex = remainingIndex;
                run.cards[remainingIndex].faceDown = run.faceDown;
                break;
            }
            remainingIndex -= run.cardCount;
        }
        return foundCard;
    }

    final boolean containsCardValue(int cardValue) {
        for (CardRun run : this.runs) {
            for (int cardIndex = 0; cardIndex < run.cardCount; ++cardIndex) {
                if (run.cards[cardIndex].cardId == cardValue) {
                    return true;
                }
            }
        }
        return false;
    }

    final void setTopRunFlagged(boolean flagged) {
        if (this.topRun != null) {
            this.topRun.faceDown = flagged;
        }
    }

    final int countFlaggedRuns() {
        if (this.workingCopy) {
            this.context.fail("Cannot get the card count of a work stack");
        }
        int flaggedRunCount = 0;
        for (CardRun run : this.runs) {
            if (run.faceDown) {
                ++flaggedRunCount;
            }
        }
        return flaggedRunCount;
    }

    public final String toString() {
        return String.valueOf(this.workingCopy ? "Work" : "") + this.group.name + ":" + this.stackIndex % 10;
    }
}



