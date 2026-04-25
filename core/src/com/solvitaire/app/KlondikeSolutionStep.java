package com.solvitaire.app;

public final class KlondikeSolutionStep {
    private final int rawMove;
    private final int sourceGroupIndex;
    private final int sourceStackIndex;
    private final int destGroupIndex;
    private final int destStackIndex;
    private final int cardCount;
    private final boolean dealMove;
    private final String description;

    KlondikeSolutionStep(int rawMove, int sourceGroupIndex, int sourceStackIndex,
                         int destGroupIndex, int destStackIndex, int cardCount,
                         boolean dealMove, String description) {
        this.rawMove = rawMove;
        this.sourceGroupIndex = sourceGroupIndex;
        this.sourceStackIndex = sourceStackIndex;
        this.destGroupIndex = destGroupIndex;
        this.destStackIndex = destStackIndex;
        this.cardCount = cardCount;
        this.dealMove = dealMove;
        this.description = description;
    }

    public int getRawMove() { return rawMove; }
    public int getSourceGroupIndex() { return sourceGroupIndex; }
    public int getSourceStackIndex() { return sourceStackIndex; }
    public int getDestGroupIndex() { return destGroupIndex; }
    public int getDestStackIndex() { return destStackIndex; }
    public int getCardCount() { return cardCount; }
    public boolean isDealMove() { return dealMove; }
    public String getDescription() { return description; }

    /** Group 0 = tableau, 1 = feed/stock, 2 = pile/waste, 3 = foundation */
    public boolean isFromWaste() { return sourceGroupIndex == 2; }
    public boolean isToFoundation() { return destGroupIndex == 3; }
    public boolean isFromTableau() { return sourceGroupIndex == 0; }
    public boolean isToTableau() { return destGroupIndex == 0; }
}
