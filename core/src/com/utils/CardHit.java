package com.utils;

import com.spider.SpiderScreen;

public class CardHit {
    final int stackIndex;
    final SpiderStack stack;
    final int index;
    final CardModel card;

    public CardHit(int stackIndex, SpiderStack stack, int index, CardModel card) {
        this.stackIndex = stackIndex;
        this.stack = stack;
        this.index = index;
        this.card = card;
    }

    public int getStackIndex() {
        return stackIndex;
    }

    public SpiderStack getStack() {
        return stack;
    }

    public int getIndex() {
        return index;
    }

    public CardModel getCard() {
        return card;
    }
}