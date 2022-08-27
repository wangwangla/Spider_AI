package com.spider.action;

import com.badlogic.gdx.utils.Array;
import com.spider.card.Card;
import com.spider.pocker.Pocker;

public class Action {
    protected Pocker poker;

    public Action() {

    }

    public boolean Do(Pocker inpoker) {
        return false;
    }

    public boolean Redo(Pocker inpoker) {
        return false;
    }

    public String GetCommand() {
        return "";
    }

    public void startAnimation(boolean bOnAnimation,boolean bStopAnimation){

    }

}