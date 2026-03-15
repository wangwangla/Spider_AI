package com.solvitaire.app;

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

    final boolean hasKnownCards() {
        return this.cardsKnown;
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

    static void swapRuns(CardStack firstStack, CardStack secondStack) {
        LinkedList<CardRun> swappedRuns = firstStack.runs;
        firstStack.runs = secondStack.runs;
        secondStack.runs = swappedRuns;
        firstStack.topRun = firstStack.runs.isEmpty() ? null : firstStack.runs.getLast();
        secondStack.topRun = secondStack.runs.isEmpty() ? null : secondStack.runs.getLast();
    }

    final int b() {
        return this.getTopCardValue();
    }

    final CardRun a(CardRun run) {
        return this.appendRun(run);
    }

    final void b(CardRun run) {
        this.removeRun(run);
    }

    final void c(CardRun run) {
        this.prependRun(run);
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

    static void a(CardStack firstStack, CardStack secondStack) {
        CardStack.swapRuns(firstStack, secondStack);
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

    final void setTopRunFlagged(boolean flagged) {
        if (this.topRun != null) {
            this.topRun.faceDown = flagged;
        }
    }

    public final String toString() {
        return String.valueOf(this.workingCopy ? "Work" : "") + this.group.name + ":" + this.stackIndex % 10;
    }
}



