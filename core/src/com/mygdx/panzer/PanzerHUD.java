package com.mygdx.panzer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
/**
 * Created by Влада on 05.12.2017.
 */

public class PanzerHUD {
    private enum conditions {NOTHING, START, FINISH};
    private conditions condition = conditions.NOTHING;
    private MapManager mapManager;

    private PanzerProject game;
    private Stage stage;
    private Batch batch;
    private Camera camera;

    private Image toMenu;
    private Image playButton;
    private Image editStartButton;
    private Image editFinishButton;

    private Texture playButtonTexture;
    private Texture stopButtonTexture;
    private Texture menuButtonTexture;
    private Texture editStartButtonTexture;
    private Texture editFinishButtonTexture;
    private Texture finishTexture;
    private Texture pressedStartButtonTexture;
    private Texture pressedFinishButtonTexture;

    public PanzerHUD(final PanzerProject game, OrthographicCamera camera, FitViewport vp, Batch batch) {
        mapManager = MapManager.getInstance();
        this.game = game;
        this.camera = camera;
        final Viewport nvp = vp;
        this.batch = batch;
        this.stage = new Stage(vp, batch);

        playButtonTexture = new Texture("HUD/playButton.png");
        stopButtonTexture = new Texture("HUD/stopButton.png");
        menuButtonTexture = new Texture("HUD/toMenuButton.png");
        editFinishButtonTexture = new Texture("HUD/FinishButton.png");
        editStartButtonTexture = new Texture("HUD/StartButton.png");
        finishTexture = new Texture("maps/finish.png");
        pressedFinishButtonTexture = new Texture("HUD/pressedFinishButton.png");
        pressedStartButtonTexture = new Texture("HUD/pressedStartButton.png");

        playButton = new Image(playButtonTexture);
        playButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("play : clicked");
                switchToOpposite();
                return true;
            }
        });

        toMenu = new Image(menuButtonTexture);
        toMenu.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    game.setScreen(new MainMenuScreen(game));
                    return true;
                }
            });

        editStartButton = new Image(editStartButtonTexture);
        editStartButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    System.out.println("editstart : clicked");
                    switch (condition)
                    {
                        case NOTHING:
                            editStartButton.setDrawable(new TextureRegionDrawable(new TextureRegion(pressedStartButtonTexture)));
                            condition = conditions.START;
                        default:
                            break;
                    }
                    return true;
                }
            });

        editFinishButton = new Image(editFinishButtonTexture);
        editFinishButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    editFinishButton.setDrawable(new TextureRegionDrawable(new TextureRegion(pressedFinishButtonTexture)));
                    System.out.println("editfinish : clicked");
                    switch (condition)
                    {
                        case NOTHING:
                            condition = conditions.FINISH;
                        default:
                            break;
                    }
                    return true;
                }
            });

        Table table = new Table();
        table.setPosition(0, 0);
        table.setSize(Settings.WORLD_WIDTH, Settings.WORLD_HEIGHT);

        table.add().padRight(Settings.WORLD_WIDTH - 500);
        table.add(editStartButton); table.add().padRight(32);
        table.add(editFinishButton); table.add().padRight(32);
        table.add(toMenu);
        table.row();
        table.add().padBottom(Settings.WORLD_HEIGHT - 150);
        table.row();
        table.add().padRight(Settings.WORLD_WIDTH - 500);
        table.add().padRight(playButtonTexture.getWidth()); table.add().padRight(32);
        table.add().padRight(playButtonTexture.getWidth()); table.add().padRight(32);
        table.add(playButton);

        stage.addActor(table);
       InputMultiplexer multiplexer = new InputMultiplexer();
       multiplexer.addProcessor(stage);
       multiplexer.addProcessor(new InputAdapter(){
            public boolean touchDown(int x,int y,int pointer,int button){
                Vector2 realCoord = new Vector2(x, y);
                nvp.unproject(realCoord);
                System.out.println("adapter : touched");
                                switch (condition) {
                    case START:
                        editStartButton.setDrawable(new TextureRegionDrawable(new TextureRegion(editStartButtonTexture)));
                        if (!panzInPolygons(realCoord.x,realCoord.y))
                            Settings.setStartPos(realCoord);
                        break;
                    case FINISH:
                        editFinishButton.setDrawable(new TextureRegionDrawable(new TextureRegion(editFinishButtonTexture)));
                        if (!finishInPolygons(realCoord.x,realCoord.y))
                            Settings.setFinishPos(realCoord);
                        break;
                    case NOTHING:
                        break;
                }
                condition = conditions.NOTHING;
                return true;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);
    }

    public boolean isPanzInFinish()
    {
        Rectangle finish = new Rectangle(Settings.getFinishPos().x - finishTexture.getWidth() / 2,
                Settings.getFinishPos().y - finishTexture.getHeight() / 2,
                finishTexture.getWidth(), finishTexture.getHeight());
        float[] vertices = {finish.x, finish.y, finish.x + finish.width, finish.y,
                finish.x + finish.width, finish.y + finish.height, finish.x, finish.y + finish.height};
        Polygon finishp = new Polygon(vertices);
        Polygon panz = mapManager.getPanzer().getPhysBody();

        return Intersector.overlapConvexPolygons(finishp, panz);
    }
    private boolean panzInPolygons(float x, float y) {
        Vector2 panzerSize = mapManager.getPanzer().getPanzerSize();
        float[] vertices = { 0, 0, 0, panzerSize.y, panzerSize.x, panzerSize.y, panzerSize.x, 0};
        Polygon npp = new Polygon(vertices);
        npp.setOrigin(panzerSize.x / 2,panzerSize.y / 2);
        npp.setRotation(mapManager.getPanzer().getAngle());
        npp.setPosition(x - panzerSize.x/2, y - panzerSize.y/2);

        for (Polygon p:mapManager.getMap().getPolygonPhysObjects()){
            if (Intersector.overlapConvexPolygons(p, npp))
                return true;
        }
        return false;
    }

    private boolean finishInPolygons(float x, float y)
    {
        Rectangle newfinish = new Rectangle(x - finishTexture.getWidth() / 2, y - finishTexture.getHeight() / 2,
                                                finishTexture.getWidth(), finishTexture.getHeight());
        float[] vertices = {newfinish.x, newfinish.y, newfinish.x + newfinish.width, newfinish.y,
                newfinish.x + newfinish.width, newfinish.y + newfinish.height, newfinish.x, newfinish.y + newfinish.height};
        Polygon npp = new Polygon(vertices);
        for (Polygon p:mapManager.getMap().getPolygonPhysObjects()){
            if (Intersector.overlapConvexPolygons(p, npp))
                return true;
        }
        return false;
        }

    public void render(float delta) {
        Vector2 finishpos = Settings.getFinishPos();
        batch.begin();
        batch.draw(finishTexture, finishpos.x - finishTexture.getWidth() / 2, finishpos.y - finishTexture.getHeight() / 2);
        batch.end();
        stage.act(delta);
        stage.draw();
    }

    private void switchToOpposite() {
        switch (game.getProcessState())
        {
            case FINISHED:
            case RUN:
                toMenu.setVisible(true);
                editStartButton.setVisible(true);
                editFinishButton.setVisible(true);
                game.setProcessState(ProcessScreen.ProcessState.PAUSE);
                playButton.setDrawable(new TextureRegionDrawable(new TextureRegion(playButtonTexture)));
                playButton.setSize(stopButtonTexture.getWidth(), stopButtonTexture.getHeight());
                break;
            case PAUSE:
                toMenu.setVisible(false);
                editStartButton.setVisible(false);
                editFinishButton.setVisible(false);
                game.setProcessState(ProcessScreen.ProcessState.RUN);
                playButton.setDrawable(new TextureRegionDrawable(new TextureRegion(stopButtonTexture)));
                playButton.setSize(stopButtonTexture.getWidth(), stopButtonTexture.getHeight());
                break;
            //case FINISHED:
            //    break;
            default:
                break;
        }
    }

    public void dispose()
    {
        playButtonTexture.dispose();
        stopButtonTexture.dispose();
        menuButtonTexture.dispose();
        editStartButtonTexture.dispose();
        editFinishButtonTexture.dispose();
        finishTexture.dispose();
        pressedStartButtonTexture.dispose();
        pressedFinishButtonTexture.dispose();
    }
}