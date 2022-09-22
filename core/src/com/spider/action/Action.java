package com.spider.action;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.spider.pocker.Pocker;

public class Action {
    protected Pocker poker;
    protected boolean updateGroup;
    public Action() {

    }

    public void setUpdateGroup(boolean updateGroup) {
        this.updateGroup = updateGroup;
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

    public void redoAnimation(){

    }

    public void setGroup(Group cardGroup) {

    }

    @Override
    public String toString() {
        return super.toString();
    }
}