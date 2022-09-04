package com.spider.action;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.spider.card.Card;
import com.spider.pocker.Pocker;

public class Action {
    protected Pocker poker;
    public Action() {

    }

    public boolean doAction(Pocker inpoker, Group group) {
        return false;
    }

    public boolean doAction(Pocker inpoker) {
        return false;
    }

    public boolean redo(Pocker inpoker) {
        return false;
    }

    public void startAnimation(){

    }

    public void endAnimation(){

    }

    public void redoAnimation(){}

    public void setGroup(Group cardGroup) {

    }

    @Override
    public String toString() {
        return super.toString();
    }
}