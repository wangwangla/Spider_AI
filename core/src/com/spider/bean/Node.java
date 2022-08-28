package com.spider.bean;

import com.spider.action.Action;
import com.spider.pocker.Pocker;

public class Node {
    private int value;
    private Pocker poker;
    private Action action;

    public Node(int getValue, Pocker newPoker, Action action) {
        this.value = getValue;
        this.poker = newPoker;
        this.action = action;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Pocker getPoker() {
        return poker;
    }

    public void setPoker(Pocker poker) {
        this.poker = poker;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
