package com.tony.balatro.migration;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.Arrays;
import java.util.List;

public class LuaProjectMirror {
    public static final String ROOT_PATH = "original_lua";
    private static LuaProjectSummary cachedSummary;

    private LuaProjectMirror() {
    }

    public static LuaProjectSummary getSummary() {
        if (cachedSummary == null) {
            cachedSummary = scan();
        }
        return cachedSummary;
    }

    public static void refresh() {
        cachedSummary = scan();
    }

    private static LuaProjectSummary scan() {
        FileHandle root = Gdx.files.internal(ROOT_PATH);
        List<String> bootModules = Arrays.asList(
                "main.lua",
                "conf.lua",
                "globals.lua",
                "game.lua",
                "engine/*",
                "functions/*",
                "card*.lua"
        );
        if (!root.exists()) {
            return new LuaProjectSummary(false, ROOT_PATH, 0, 0, 0, 0, 0, bootModules);
        }
        return new LuaProjectSummary(
                true,
                root.path(),
                countBySuffix(root, ".lua"),
                countBySuffix(root.child("localization"), ".lua"),
                countBySuffix(root.child("resources").child("shaders"), ".fs"),
                countBySuffix(root.child("resources").child("sounds"), ".ogg"),
                countBySuffix(root.child("resources").child("textures"), ".png"),
                bootModules
        );
    }

    private static int countBySuffix(FileHandle root, String suffix) {
        if (root == null || !root.exists()) {
            return 0;
        }
        if (!root.isDirectory()) {
            return root.name().endsWith(suffix) ? 1 : 0;
        }
        int count = 0;
        for (FileHandle child : root.list()) {
            count += countBySuffix(child, suffix);
        }
        return count;
    }
}
