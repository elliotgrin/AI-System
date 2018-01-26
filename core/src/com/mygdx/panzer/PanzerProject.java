package com.mygdx.panzer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.physics.box2d.Box2D;

public class PanzerProject extends Game {
    private ProcessScreen.ProcessState processState;
    public ProcessScreen proc;


	@Override
	public void create () {
		Box2D.init();
        proc = new ProcessScreen(this);
        setScreen(new MainMenuScreen(this));
        //setScreen(proc);
	}

    public ProcessScreen.ProcessState getProcessState() {
        return processState;
    }

    public void setProcessState(ProcessScreen.ProcessState processState) {
        this.processState = processState;
    }

    public void setProcess()
    {
        setScreen(proc);
    }
}
