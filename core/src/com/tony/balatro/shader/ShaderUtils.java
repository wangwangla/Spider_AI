package com.tony.balatro.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ArrayMap;

public class ShaderUtils {
    private static ArrayMap<ShaderType,ShaderProgram> shaderProgramArrayMap = new ArrayMap<>();
    public static ShaderProgram getShaderProgram(ShaderType type){
        ShaderProgram shaderProgram = shaderProgramArrayMap.get(type);
        if (shaderProgram == null){
            shaderProgram
                    = new ShaderProgram(
                        Gdx.files.internal(type.getVertFilePath()),
                        Gdx.files.internal(type.getFragFilePath()));
            shaderProgramArrayMap.put(type,shaderProgram);
        }
        return shaderProgram;
    }
}
