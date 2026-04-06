package com.utils;

import com.actor.CardActor;

import java.util.ArrayList;
import java.util.List;

public class SpiderStack {
    final List<CardModel> cards = new ArrayList<>();

    public List<CardModel> getCards() {
        return cards;
    }

    public int findCardIndex(CardActor cardActor) {
        for (int i = 0; i < cards.size(); i++) {
            CardModel cardModel = cards.get(i);
            if (cardModel == cardActor.getCard()){
                return i;
            }
        }
        return -1;
    }
}
