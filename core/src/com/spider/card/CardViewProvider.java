package com.spider.card;

import com.spider.model.CardModel;

/**
 * Provides the UI actor that represents a data card.
 */
public interface CardViewProvider {
    Card viewOf(CardModel model);
}
