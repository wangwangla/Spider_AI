package com.kw.gdx.utils;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
public class FontUtils {
    public static Label labelToLabel4(Label monlabel){
        Label label4 = new Label("",new Label.LabelStyle(){{
            font = monlabel.getStyle().font;
        }});
        monlabel.getParent().addActor(label4);
        monlabel.remove();
        label4.setPosition(monlabel.getX(Align.center),monlabel.getY(Align.center),Align.center);
        label4.setAlignment(Align.center);
        label4.setName(monlabel.getName());
        label4.setModkerning(5);
        return label4;
    }
}
