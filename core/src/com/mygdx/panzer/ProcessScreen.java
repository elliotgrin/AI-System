package com.mygdx.panzer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Влада on 03.12.2017.
 */

public class ProcessScreen extends ScreenAdapter {

    public enum ProcessState {
        RUN,
        PAUSE,
        FINISHED
    }

    private PanzerProject game;
    private SpriteBatch batch;
    private MapManager mapManager;

    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private FitViewport viewport;

    private PanzerHUD hud;
    public Panzer panzer;

    public ProcessScreen(PanzerProject game) {
        this.game = game;
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.position.set(Settings.WORLD_WIDTH / 2, Settings.WORLD_HEIGHT / 2, 0);
        camera.update();
        viewport = new FitViewport(Settings.WORLD_WIDTH, Settings.WORLD_HEIGHT, camera);
        viewport.apply(true);
        batch = new SpriteBatch();
        game.setProcessState(ProcessState.PAUSE);
    }

    @Override
    public void show() {
        mapManager = MapManager.getInstance();
        Map currentMap = new Map("maps/" + Settings.getMapname() + ".tmx");
        mapManager.setMap(currentMap);
        panzer = new Panzer(Settings.getStartAngle());
        MapManager.getInstance().setPanzer(panzer);
        mapRenderer = new OrthogonalTiledMapRenderer(mapManager.getMap().getTiledMap(), batch);
        mapRenderer.setView(camera);
        hud = new PanzerHUD(game, camera, viewport, batch);
	}

    @Override
    public void render(float delta) {
        switch (game.getProcessState()) {
            case RUN:
                panzer.updatePosition(delta);
                break;
            case PAUSE:
                panzer.reset();
                for (Sensor sensor: panzer.getSensors()) {
                    sensor.reset();
                }
                break;
            default:
                break;
        }
        clearScreen();
        batch.setProjectionMatrix(camera.projection);
        batch.setTransformMatrix(camera.view);
        mapRenderer.render();
        if (Settings.isDrawsensors()) {
            drawSensors();
            drawDebug();
        }
        if (hud.isPanzInFinish())
            game.setProcessState(ProcessState.FINISHED);
        panzer.draw(batch, delta);
        hud.render(delta);
    }


    // рендерим прямоугольники физических обьектов
    private void drawDebug() {
        Polygon panzer = mapManager.getPanzer().getPhysBody();
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Array<Polygon> polygonPhysObjects = mapManager.getMap().getPolygonPhysObjects();

        for (Polygon polygon : polygonPhysObjects) {
            shapeRenderer.polygon(polygon.getTransformedVertices());
        }

        shapeRenderer.polygon(panzer.getTransformedVertices());
        shapeRenderer.end();
    }

    private void drawSensors()
    {
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Sensor sensor: panzer.getSensors()) {
            Vector2 endPoint = null;
            if (sensor.seeingObject()) {
                endPoint = new Vector2(sensor.getIntersectPoint());
            } else {
                endPoint = new Vector2(sensor.getSensorEnd());
            }
            shapeRenderer.line(sensor.getSensorBegin(), endPoint);
        }
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, Color.BLACK.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void dispose () {
        batch.dispose();
        panzer.dispose();
        mapManager.getMap().dispose();
        hud.dispose();
    }
}

