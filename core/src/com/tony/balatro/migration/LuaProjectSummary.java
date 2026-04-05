package com.tony.balatro.migration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LuaProjectSummary {
    private final boolean available;
    private final String rootPath;
    private final int luaFileCount;
    private final int localizationCount;
    private final int shaderCount;
    private final int soundCount;
    private final int textureCount;
    private final List<String> bootModules;

    public LuaProjectSummary(boolean available,
                             String rootPath,
                             int luaFileCount,
                             int localizationCount,
                             int shaderCount,
                             int soundCount,
                             int textureCount,
                             List<String> bootModules) {
        this.available = available;
        this.rootPath = rootPath;
        this.luaFileCount = luaFileCount;
        this.localizationCount = localizationCount;
        this.shaderCount = shaderCount;
        this.soundCount = soundCount;
        this.textureCount = textureCount;
        this.bootModules = Collections.unmodifiableList(new ArrayList<String>(bootModules));
    }

    public boolean isAvailable() {
        return available;
    }

    public String getRootPath() {
        return rootPath;
    }

    public int getLuaFileCount() {
        return luaFileCount;
    }

    public int getLocalizationCount() {
        return localizationCount;
    }

    public int getShaderCount() {
        return shaderCount;
    }

    public int getSoundCount() {
        return soundCount;
    }

    public int getTextureCount() {
        return textureCount;
    }

    public List<String> getBootModules() {
        return bootModules;
    }

    public String toStatusLine() {
        if (!available) {
            return "Lua mirror missing: " + rootPath;
        }
        return "Lua mirror ready: "
                + luaFileCount + " scripts, "
                + localizationCount + " locales, "
                + shaderCount + " shaders, "
                + soundCount + " sounds, "
                + textureCount + " textures";
    }

    public String toEntryLine() {
        if (!available) {
            return "Expected extracted source under " + rootPath;
        }
        return "Boot chain: " + joinModules(bootModules);
    }

    private String joinModules(List<String> modules) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < modules.size(); i++) {
            if (i > 0) {
                builder.append(" -> ");
            }
            builder.append(modules.get(i));
        }
        return builder.toString();
    }
}
