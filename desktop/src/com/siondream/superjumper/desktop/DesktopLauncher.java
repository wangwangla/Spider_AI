package com.siondream.superjumper.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.spider.SpiderGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Spider v1.0";
		config.width = 880;
		config.height = 900;
		config.x = 0;
		config.y = 0;
		new LwjglApplication(new SpiderGame(), config);
	}
}
