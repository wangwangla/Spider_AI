package com.utils;

import java.util.List;

public class CardDrag {
    final int fromCol;
    final List<CardModel> moving;

    public CardDrag(int fromCol, List<CardModel> moving) {
        this.fromCol = fromCol;
        this.moving = moving;
    }

    public int getFromCol() {
        return fromCol;
    }

    public List<CardModel> getMoving() {
        return moving;
    }
}