package com.siondream.superjumper.desktop;

import com.badlogic.gdx.utils.Array;
import com.spider.card.Card;
import com.spider.pocker.Pocker;

import java.util.HashSet;
import java.util.Random;

public class App {
    public static void main(String[] args) {

        HashSet<Pocker> hashSet = new HashSet<Pocker>();

        Pocker pocker = new Pocker();
        Array<Card> array = new Array<Card>();
        array.add(new Card(1,1));
        pocker.getDesk().add(array);

        Pocker pocker1 = new Pocker();
        Array<Card> array1 = new Array<Card>();
        array1.add(new Card(2,1));


        pocker1.getDesk().add(array1);


        hashSet.add(pocker);


    }
}
