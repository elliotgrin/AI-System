package com.mygdx.panzer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Влада on 05.12.2017.
 */

public class Panzer {

    private static final float FRAME_TIME = 0.3f;
    private static final float POWER_RATIO_BOUND = 10f;
    private static final float TURN_RULE_FREQ = 0.12f;
    private static final float DEFAULT_RULE_FREQ = 0.00001f;
    private static final int SENSOR_COUNT = 3;

    private Vector2 position = new Vector2(0,0);
    private float frameTime;
    private float angle = 0;
    private Animation animation;
    private float ruleFreq = 0;
    private Polygon physBody;
    public Sprite panzerSprite;
    private TextureRegion panzerImage;
    private Vector2 panzerSize;
    private Array<Sensor> sensors = new Array<>();
    private float timePassed = 0;
    private RuleSet ruleSet;
    private Vector2 lastRulePower = new Vector2(0, 0);

    public Panzer(float startAngle) {
        this.angle = startAngle;
        String texturePath = "maps/truck.png";
        Array<GridPoint2> points = new Array<>();
        points.add(new GridPoint2(0, 0));
        points.add(new GridPoint2(0, 1));
        points.add(new GridPoint2(0, 2));
        animation = loadAnimation(texturePath, points, FRAME_TIME);
        panzerImage = (TextureRegion) animation.getKeyFrame(frameTime);
        panzerSize = new Vector2(panzerImage.getRegionWidth(), panzerImage.getRegionHeight());
        panzerSprite = new Sprite(panzerImage);
        Settings.setFinishPos(new Vector2(Settings.WORLD_WIDTH - panzerSize.x, Settings.WORLD_HEIGHT - panzerSize.y));

        physBody = new Polygon();
        panzerSprite.setRotation(startAngle);
        Rectangle p = panzerSprite.getBoundingRectangle();
        Settings.setStartPos(new Vector2(p.getWidth() / 2, p.getHeight() / 2));
        for (int i = 0, sensorAngle = 45; i < SENSOR_COUNT; ++i, sensorAngle-=45) {
            float rangeToSub;
            if (sensorAngle == 0) {
                rangeToSub = panzerSize.x / 2;
            } else {
                rangeToSub = (panzerSize.y / 2) / (float)Math.sin(Math.abs(sensorAngle));
            }
            Sensor sensor = new Sensor(Settings.getSensorRange() + (int)rangeToSub, sensorAngle);
            sensor.setDebugTag("SENSOR" + i);
            sensors.add(sensor);
        }
        float[] vertices = { 0, 0, 0, panzerSize.y, panzerSize.x, panzerSize.y, panzerSize.x, 0};
        physBody.setVertices(vertices);
        physBody.setPosition(Settings.getStartPos().x - panzerSize.x/2, Settings.getStartPos().y - panzerSize.y/2);
        physBody.setOrigin(panzerSize.x / 2,panzerSize.y / 2);
        physBody.setRotation(startAngle);
        ruleSet = new RuleSet(RuleSet.getRules(), this);
    }

    private Animation loadAnimation(String textureName, Array<GridPoint2> points, float frameDuration) {
        Texture texture = new Texture(textureName);
        Vector2 size = new Vector2(128, 64);
        TextureRegion[][] textureFrames = TextureRegion.split(texture, (int) size.x, (int) size.y);

        Array<TextureRegion> animationKeyFrames = new Array<>(points.size);

        for (GridPoint2 point : points) {
            animationKeyFrames.add(textureFrames[point.x][point.y]);
        }
        return new Animation(frameDuration, animationKeyFrames, Animation.PlayMode.LOOP);
    }

    public void setPosition(int x, int y) {
        position.x = x;
        position.y = y;
    }

    public void updatePosition(float delta) {
        timePassed += delta;
        // Через константый промежуток времени применяем новое правило
        if (timePassed >= ruleFreq) {
            timePassed = 0;
            lastRulePower = ruleSet.apply(sensors);
        }

        //System.out.println("left power is " + leftMove);
        //System.out.println("right power is " + rightMove);


        //System.out.println("левое значение " + leftMove + " правое значение " + rightMove);

        if (timePassed == 0) {
            float powerRatio = lastRulePower.x > lastRulePower.y
                    ? lastRulePower.x / lastRulePower.y
                    : lastRulePower.y / lastRulePower.x;

            if (powerRatio > POWER_RATIO_BOUND) {
                ruleFreq = TURN_RULE_FREQ;
            } else {
                ruleFreq = DEFAULT_RULE_FREQ;
            }
        }
        // Скорость - пиксели в секунду
        calculateMotion(
                lastRulePower.x * (delta / 1) * Settings.getMaxSpeed(),
                lastRulePower.y * (delta / 1) * Settings.getMaxSpeed());
        for (Sensor sensor: sensors) {
            sensor.update(delta);
        }
        angle = (angle + 360) % 360;
        //System.out.println("current pos: " + position.x + " " + position.y);
    }

    // Поворачивает и передвигает танк, основываясь на передвижении (не мощности) левой и правой гусеницы.
    private void calculateMotion(float leftMovement, float rightMovement) {
        float weightDiff = leftMovement - rightMovement;
        if (weightDiff == 0) {
            moveStraight(leftMovement);
            return;
        }
        boolean isRightRotation = weightDiff > 0;
        float sectorLength = Math.abs(weightDiff);
        float straightMovement = Math.min(leftMovement, rightMovement);
        moveStraight(straightMovement);
        double rotationAngle = (180 * sectorLength) / (panzerSize.y * Math.PI);
        angle = isRightRotation ? angle - (float)rotationAngle : angle + (float)rotationAngle;
        physBody.setOrigin(panzerSize.x / 2,panzerSize.y / 2);
        physBody.setRotation(angle);
        float movementRemainder = (float) Math.sin(rotationAngle * (Math.PI / 180)) * (panzerSize.y / 2);
        moveStraight(movementRemainder);
    }

    private void moveStraight(float distance){
        // Переводим в радианы
        double curr_angle = angle * (Math.PI / 180);
        double x = distance * Math.cos(curr_angle);
        double y = distance * Math.sin(curr_angle);
        Vector2 new_position = new Vector2((float)(x + position.x), (float)y + (position.y));
        position.x = new_position.x;
        position.y = new_position.y;
        physBody.setPosition(position.x - panzerSize.x/2, position.y - panzerSize.y/2);
    }
    public void reset() {
        angle = Settings.getStartAngle();
        setPosition((int)Settings.getStartPos().x, (int)Settings.getStartPos().y);
        physBody.setPosition(Settings.getStartPos().x - panzerSize.x/2, Settings.getStartPos().y - panzerSize.y/2);
        physBody.setOrigin(panzerSize.x / 2,panzerSize.y / 2);
        physBody.setRotation(angle);
    }


    public void draw(Batch batch, float delta){
        frameTime = (frameTime + delta) % (FRAME_TIME * 100);
        panzerImage = (TextureRegion) animation.getKeyFrame(frameTime);
        panzerSprite = new Sprite(panzerImage);
        panzerSprite.setRotation(angle);
        panzerSprite.setCenter(position.x, position.y);
        batch.begin();
        panzerSprite.draw(batch);
        batch.end();
    }

    public void dispose() {
        panzerImage.getTexture().dispose();
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getAngle() {
        return angle;
    }

    public Array<Sensor> getSensors() {
        return sensors;
    }

    public Polygon getPhysBody() {
        return physBody;
    }

    public Vector2 getPanzerSize() {
        return panzerSize;
    }
}