package com.wk.postProcessor.process;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.kw.gdx.BaseBaseGame;
import com.kw.gdx.constant.Constant;

public class CrtPostProcessor {
    private FrameBuffer fbo;
    private ShaderProgram shader;
    private int width;
    private int height;
    private Stage postProcessStage;
    private Image postImage;
    public CrtPostProcessor(BaseBaseGame game, int width, int height, ShaderProgram shader) {
        this.width = width;
        this.height = height;
        ShaderProgram.pedantic = false;
        this.shader = shader;
        fbo = new FrameBuffer(
                Pixmap.Format.RGBA8888,
                width,
                height,
                false
        );
        Texture colorBufferTexture = fbo.getColorBufferTexture();
        this.postProcessStage = new Stage(game.getStageViewport(), game.getBatch());
        this.postImage = new Image(colorBufferTexture){
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.setShader(shader);


                shader.setUniformf("time", 0);
                shader.setUniformf("distortion_fac", 1.1f, 1.1f);
                shader.setUniformf("scale_fac", 1.0f, 1.0f);
                shader.setUniformf("feather_fac", 0.15f);
                shader.setUniformf("noise_fac", 0.15f);
                shader.setUniformf("bloom_fac", 0.8f);
                shader.setUniformf("crt_intensity", 0.4f);
                shader.setUniformf("glitch_intensity", 0.4f);
                shader.setUniformf("scanlines", 900.0f);

                shader.setUniformf(
                        "u_resolution",
                        Gdx.graphics.getWidth(),
                        Gdx.graphics.getHeight()
                );


                super.draw(batch, parentAlpha);
                batch.setShader(null);
            }
        };
        postProcessStage.addActor(postImage);
        postImage.setPosition(Constant.WIDTH/2f,Constant.HIGHT/2f, Align.center);
        postImage.setDebug(true);
    }

    public void begin() {
        fbo.begin();
    }

    /** 结束后处理（把 FBO 用 CRT shader 画到屏幕） */
    public void end() {
        fbo.end();
        Texture tex = fbo.getColorBufferTexture();
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        shader.setUniformf("u_resolution", width, height);
    }

    /** 屏幕尺寸变化时调用 */
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        if (fbo != null) fbo.dispose();
        fbo = new FrameBuffer(
                Pixmap.Format.RGBA8888,
                width,
                height,
                false
        );
    }

    public void render() {
        postProcessStage.act();
        postProcessStage.draw();
    }

    public void dispose() {
        shader.dispose();
        fbo.dispose();
    }

}
