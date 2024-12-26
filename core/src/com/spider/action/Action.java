package com.spider.action;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.spider.pocker.Pocker;

public class Action {
    protected Pocker poker;
    public Action() {

    }

    public boolean doAction(Pocker inpoker) {
        return false;
    }

    public boolean redo(Pocker inpoker) {
        return false;
    }

    public void startAnimation(){

    }

    public void redoAnimation(){

    }
}