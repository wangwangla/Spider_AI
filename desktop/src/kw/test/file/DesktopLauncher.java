package kw.test.file;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.solvitaire.gdx.FreeCellBaseGame;

public class DesktopLauncher {
    public static void main(String[] args) {
        ReadFileConfig readFileConfig = new ReadFileConfig();
        Bean value = readFileConfig.getValue();
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = value.getName();
        config.x = 1000;
        config.y = 0;
        config.height = (int)(720);
        config.width = (int) (1280);
        config.stencil = 8;
        new LwjglApplication(new FreeCellBaseGame(),config);
    }
}
