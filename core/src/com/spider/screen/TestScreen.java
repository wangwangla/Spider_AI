package com.spider.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.spider.SpiderGame;
import com.spider.card.Card;
import com.spider.constant.Constant;
import com.spider.log.NLog;
import com.spider.manager.GameManager;
import com.spider.pocker.Pocker;

import java.util.HashSet;

public class TestScreen extends ScreenAdapter {

    @Override
    public void show() {
        super.show();
        Pocker pocker = new Pocker();
        Array<Card> array = new Array();
        array.add(new Card(1,1,false));
        pocker.getDesk().add(array);
        Pocker pocker1 = pocker.copyInstance();
        if (pocker1 == pocker) {
            System.out.println("xxxxxxxxxxxxxxxxxxxx");
        }
        HashSet<Pocker> pockerHashSet = new HashSet<>();
        pockerHashSet.add(pocker);
        if (pockerHashSet.contains(pocker1)) {
            System.out.println("=========");
        }
    }
}
