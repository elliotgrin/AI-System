package com.mygdx.panzer.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.panzer.PanzerProject;

public class DesktopLauncher {

	public static void main (String[] arg) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "PanzerProject";
		cfg.width = 960;
		cfg.height = 540;
		new LwjglApplication(new PanzerProject(), cfg);
	}
}
