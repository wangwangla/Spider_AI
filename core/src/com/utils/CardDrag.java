package com.utils;

import com.actor.CardActor;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class CardDrag {
    private int fromCol;
    private List<CardModel> moving;
    private List<CardActor> movingActor;
    private Vector2 touchDownV2;


    public CardDrag(int fromCol, List<CardModel> moving, ArrayList<CardActor> cardActors, Vector2 vector2) {
        this.fromCol = fromCol;
        this.moving = moving;
        this.movingActor = cardActors;
        this.touchDownV2 =  vector2;
    }

    public void setFromCol(int fromCol) {
        this.fromCol = fromCol;
    }

    public void setMoving(List<CardModel> moving) {
        this.moving = moving;
    }

    public List<CardActor> getMovingActor() {
        return movingActor;
    }

    public void setMovingActor(List<CardActor> movingActor) {
        this.movingActor = movingActor;
    }

    public Vector2 getTouchDownV2() {
        return touchDownV2;
    }

    public void setTouchDownV2(Vector2 touchDownV2) {
        this.touchDownV2 = touchDownV2;
    }

    public int getFromCol() {
        return fromCol;
    }

    public List<CardModel> getMoving() {
        return moving;
    }
}