package com.tony.balatro;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.kw.gdx.constant.Constant;
import com.tony.balatro.shader.ShaderType;
import com.tony.balatro.shader.ShaderUtils;

public class GameBgAnimation extends Group {
    private ShaderProgram program;
    private float time = 4f;
    private float flash = 0f;
    public GameBgAnimation(){
        program = ShaderUtils.getShaderProgram(ShaderType.whirlpool);
        setSize(Constant.GAMEWIDTH,Constant.GAMEHIGHT);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setShader(program);
        program.setUniformf("time", time);
        program.setUniformf("vort_speed", 4.0f);
        program.setUniformf("mid_flash", flash);
        program.setUniformf("vort_offset", 0.0f);
        program.setUniformf("colour_1", new Color(0.4f, 0.7f, 1f, 1f));
        program.setUniformf("colour_2", new Color(0.8f, 0.2f, 1f, 1f));
        super.draw(batch, parentAlpha);
        batch.setShader(null);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;


    }
}
