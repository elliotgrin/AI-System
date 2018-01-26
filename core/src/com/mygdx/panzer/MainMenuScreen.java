package com.mygdx.panzer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
//import com.sun.corba.se.impl.naming.cosnaming.InternalBindingKey;

import java.io.Console;

/**
 * Created by Влада on 03.12.2017.
 */

public class MainMenuScreen extends ScreenAdapter {
    final int WIDTH = 1920;
    final int HEIGHT = 1080;
    private PanzerProject process;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private FitViewport viewport;
    private Stage stage;
    private Skin skin;
    private TextField velocityField;
    private TextField sensorField;
    private TextField angleField;
    private SelectBox<String> mapField;
    private Texture enableSensorsTexture;
    private Texture disableSensorsTexture;
    private Image enableSensors;
    private Image applyButton;
    private Texture applyButtonTexture;
    private Texture labelTexture;
    private Image label;
    private  BitmapFont font;
    private boolean isEnableSensors = Settings.isDrawsensors();

    public MainMenuScreen(final PanzerProject panz)
    {
        this.process = panz;
        camera = new OrthographicCamera();
        camera.position.set(WIDTH / 2, HEIGHT / 2, 0);
        camera.update();
        viewport = new FitViewport(WIDTH, HEIGHT, camera);
        viewport.apply(true);
        batch = new SpriteBatch();
        this.stage = new Stage(viewport, batch);
        skin = new Skin(Gdx.files.internal("HUD/uiskin.json"));
        skin.getFont("default-font").getData().setScale(2);

        enableSensorsTexture = new Texture("HUD/enabledSensors.png");
        disableSensorsTexture = new Texture("HUD/disabledSensors.png");
        Label drawsensors = new Label("Draw sensors", skin);
        drawsensors.setFontScale(2);
        enableSensors = isEnableSensors? new Image(enableSensorsTexture) : new Image(disableSensorsTexture);
        enableSensors.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (isEnableSensors)
                {
                    enableSensors.setDrawable(new TextureRegionDrawable(new TextureRegion(disableSensorsTexture)));
                    isEnableSensors = false;
                }
                else
                {
                    enableSensors.setDrawable(new TextureRegionDrawable(new TextureRegion(enableSensorsTexture)));
                    isEnableSensors = true;
                }
                return true;
            }
        });

        applyButtonTexture = new Texture("HUD/ApplyButton.png");
        applyButton = new Image(applyButtonTexture);
        applyButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Settings.setMapname(mapField.getSelected());
                Settings.setDrawsensors(isEnableSensors);
                String s = velocityField.getText();
                int value = s.equals("")? Settings.SPEED_LIMIT : Integer.valueOf(velocityField.getText());
                value = value < Settings.SPEED_MIN? Settings.SPEED_MIN : value;
                Settings.setMaxSpeed(value);
                s = sensorField.getText();
                int sensValue = s.equals("")? Settings.SENSORS_LIMIT : Integer.valueOf(sensorField.getText());
                sensValue = sensValue < Settings.SENSORS_MIN? Settings.SENSORS_MIN : sensValue;
                Settings.setSensorRange(sensValue);
                s = angleField.getText();
                int angValue = s.equals("")? Settings.ANGLE_LIMIT : Integer.valueOf(angleField.getText());
                Settings.setStartAngle(angValue);
                process.setProcess();
                return true;
            }
        });


        Label velocity = new Label("Panzer/hours", skin); velocity.setFontScale(2);
        Label sensor = new Label("Max sensors range:", skin); sensor.setFontScale(2);
        Label angle = new Label("Start angle:", skin); angle.setFontScale(2);
        Label mmap = new Label("Select map:", skin); mmap.setFontScale(2);
        velocityField = new TextField(Integer.toString(Settings.getMaxSpeed()), skin);
        velocityField.setMaxLength(3);
        velocityField.setMessageText("MAX = " + String.valueOf(Settings.SPEED_LIMIT));
        velocityField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                String s = textField.getText();
                if (!s.equals(""))
                {
                    float newvalue = Integer.valueOf(textField.getText());
                    if (newvalue > Settings.SPEED_LIMIT)
                        textField.setText(String.valueOf(Settings.SPEED_LIMIT));
                }
            }
        });

        sensorField = new TextField(Integer.toString(Settings.getSensorRange()), skin);
        sensorField.setMaxLength(3);
        sensorField.setMessageText("MAX = " + String.valueOf(Settings.SENSORS_LIMIT));
        sensorField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                String s = textField.getText();
                if (!s.equals(""))
                {
                    int newvalue = Integer.valueOf(textField.getText());
                    if (newvalue > Settings.SENSORS_LIMIT)
                        textField.setText(String.valueOf(Settings.SENSORS_LIMIT));
                }
            }
        });

        angleField = new TextField(Integer.toString(Settings.getStartAngle()), skin);
        angleField.setMaxLength(3);
        angleField.setMessageText("MAX = " + String.valueOf(Settings.ANGLE_LIMIT));
        angleField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                String s = textField.getText();
                if (!s.equals(""))
                {
                    int newvalue = Integer.valueOf(textField.getText());
                    if (newvalue > Settings.ANGLE_LIMIT)
                        textField.setText(String.valueOf(Settings.ANGLE_LIMIT));
                }
            }
        });

        TextField.TextFieldFilter filter = new TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
                if (Character.toString(c).matches("^[0-9]")) {
                    return true;
                }
                return false;
            }
        };
        velocityField.setTextFieldFilter(filter);
        sensorField.setTextFieldFilter(filter);
        angleField.setTextFieldFilter(filter);

        String[] maps = MapManager.getInstance().getMaps();
        mapField = new SelectBox(skin);
        mapField.setItems(maps);
        mapField.setSelected(Settings.getMapname());
        labelTexture = new Texture("HUD/label.png");
        label = new Image(labelTexture);

        float space = 180;
        Table table = new Table();
        table.setPosition(0, 0);
        table.setSize(WIDTH, HEIGHT);

        float wspace = 70;
        float wsize = (WIDTH - wspace *5) / 4f;
        float hsize = HEIGHT - 2*space - labelTexture.getHeight() - applyButton.getHeight() - 20;
        Table inner1 = new Table();
        inner1.setSize(WIDTH, hsize);
        inner1.add().padRight(wspace);
        inner1.add(velocity).size(wsize, hsize*0.3f); inner1.add().padRight(wspace);
        inner1.add(sensor).size(wsize, hsize*0.3f); inner1.add().padRight(wspace);
        inner1.add(angle).size(wsize, hsize*0.3f); inner1.add().padRight(wspace);
        inner1.add(mmap).size(wsize, hsize*0.3f); inner1.add().padRight(wspace);
        inner1.row();
        inner1.add().padRight(wspace);
        inner1.add(velocityField).size(wsize, hsize*0.7f); inner1.add().padRight(wspace);
        inner1.add(sensorField).size(wsize, hsize*0.7f); inner1.add().padRight(wspace);
        inner1.add(angleField).size(wsize, hsize*0.7f); inner1.add().padRight(wspace);
        inner1.add(mapField).size(wsize, hsize*0.7f); inner1.add().padRight(wspace);

        Table inner2 = new Table();
        inner2.setSize(WIDTH, applyButtonTexture.getHeight() + 20);
        inner2.row(); inner2.add().padRight(10);
        inner2.add(enableSensors); inner2.add(drawsensors);
        inner2.add().padRight(WIDTH - 550);
        inner2.add(applyButton); inner2.add().padRight(20);
        inner2.row();
        inner2.add().padBottom(10);

        table.add(label).size(labelTexture.getWidth(), labelTexture.getHeight());
        table.row();
        table.add().padBottom(space);
        table.row();
        table.add(inner1);
        table.row();
        table.add().padBottom(space);
        table.row();
        table.add(inner2);

        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        clearScreen();
        batch.setProjectionMatrix(camera.projection);
        batch.setTransformMatrix(camera.view);
        batch.begin();
        batch.end();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
            viewport.update(width, height);
    }

    private void clearScreen() {
        Gdx.gl.glClearColor((float)135/255, (float)135/255, (float)135/255, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void dispose() {
        applyButtonTexture.dispose();
        enableSensorsTexture.dispose();
        disableSensorsTexture.dispose();
        labelTexture.dispose();
        font.dispose();
        skin.dispose();
    }
}
