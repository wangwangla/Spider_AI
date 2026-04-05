package com.wk.postProcessor;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.kw.gdx.BaseBaseGame;
import com.kw.gdx.constant.Constant;
import com.kw.gdx.screen.BaseScreen;
import com.wk.postProcessor.process.CrtPostProcessor;

public class PostProcessorScreen extends BaseScreen {
    private CrtPostProcessor crtPostProcessor;
    private ShaderProgram program;
    protected boolean userCrt;
    public PostProcessorScreen(BaseBaseGame game, ShaderProgram program) {
        super(game);
        this.program = program;
        this.userCrt = false;
    }

    @Override
    public void show() {
        crtPostProcessor = new CrtPostProcessor(game, (int) Constant.GAMEWIDTH, (int) Constant.GAMEHIGHT, program);
        super.show();
    }

    @Override
    public void render(float delta) {
        if (userCrt){
            crtPostProcessor.begin();
            super.render(delta);
            crtPostProcessor.end();
            Gdx.gl.glClearColor(Constant.viewColor.r,Constant.viewColor.g,Constant.viewColor.b,Constant.viewColor.a);
            Gdx.gl.glClear(
                    GL20.GL_COLOR_BUFFER_BIT
                            | GL20.GL_DEPTH_BUFFER_BIT
                            | GL20.GL_STENCIL_BUFFER_BIT);
            crtPostProcessor.render();
        }else {
            super.render(delta);
        }
    }
}

