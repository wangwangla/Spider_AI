package com.spider.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.spider.log.NLog;

public class AssetUtil {
    private AssetManager assetManager;
    public AssetUtil(){
        NLog.e("init assetUtil ");
        assetManager = new AssetManager();
    }

    public Texture loadTexture(String file){
        NLog.e("loading texture file %s",file);
        if (!assetManager.isLoaded(file)) {
            NLog.e("%s is not load !",file);
            assetManager.load(file, Texture.class);
            assetManager.finishLoading();
        }
        return assetManager.get(file);
    }
}
