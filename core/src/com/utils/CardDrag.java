package com.utils;

import com.badlogic.gdx.math.Vector2;

import java.util.List;
import java.util.Vector;

public class CardDrag {
    final int fromCol;
    final List<CardModel> moving;
    private Vector2 touchDownV2;

    public CardDrag(int fromCol, List<CardModel> moving,Vector2 touchDownV2) {
        this.fromCol = fromCol;
        this.moving = moving;
        this.touchDownV2 =  touchDownV2;
    }

    public int getFromCol() {
        return fromCol;
    }

    public List<CardModel> getMoving() {
        return moving;
    }
}