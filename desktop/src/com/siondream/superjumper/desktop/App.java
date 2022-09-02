package com.siondream.superjumper.desktop;

import com.badlogic.gdx.utils.Array;
import com.spider.card.Card;
import com.spider.pocker.Pocker;

import java.util.Random;

public class App {
    public static void main(String[] args) {
        Pocker pocker = new Pocker();
        Pocker pocker1 = new Pocker();
        Array<Card> array = new Array<Card>();
        array.add(new Card(1,1));
        pocker.getDesk().add(array);
        Array<Card> array1 = new Array<Card>();
        array1.add(new Card(7,1));
        pocker1.getDesk().add(array);
        if (pocker.equals(pocker1)) {
            System.out.println("-----------------------");
        }
    }
}
