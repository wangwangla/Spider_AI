package com.tony.balatro.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureSplitUtil {

    /**
     * @param texture 原始大图
     * @param cols 横向切多少份（N）
     * @param rows 纵向切多少份（M）
     * @return TextureRegion[rows][cols]
     */
    public static TextureRegion[][] split(Texture texture, int cols, int rows) {
        int tileWidth = texture.getWidth() / cols;
        int tileHeight = texture.getHeight() / rows;

        return TextureRegion.split(texture, tileWidth, tileHeight);
    }
}