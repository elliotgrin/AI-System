package com.mygdx.panzer;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.awt.*;

public class Sensor {

    private String debugTag;
    private ShapeRenderer debugRenderer = new ShapeRenderer();
    private Vector2 sensorBegin = new Vector2();
    private Vector2 sensorEnd = new Vector2();
    private Vector2 intersectPoint = new Vector2();
    private MapManager mapManager = MapManager.getInstance();

    public enum Seeing {
        OBJECT,
        NOTHING
    }

    private int maxRange;
    private int range;
    private float angle;
    private Vector2 position = new Vector2();
    private Seeing seeing = Seeing.NOTHING;

    public Sensor(int maxRange, float angle) {
        this.maxRange = maxRange;
        this.angle = angle;
        this.angle *= Math.PI / 180;
    }

    public void update(float delta) {
        Panzer panzer = mapManager.getPanzer();
        float panzer_angle = panzer.getAngle();
        // Переводим в радианы
        panzer_angle *= Math.PI / 180;
        float sum_angle = panzer_angle + angle;
        position = panzer.getPosition();
        double x = maxRange * Math.cos(sum_angle);
        double y = maxRange * Math.sin(sum_angle);
        sensorBegin = new Vector2(position);
        sensorEnd = new Vector2((float)(x + sensorBegin.x), (float)y + (sensorBegin.y));

        Array<Polygon> polygons = mapManager.getMap().getPolygonPhysObjects();
        float minRange = Float.MAX_VALUE;
        for (Polygon polygon: polygons) {
            float[] vertices = polygon.getTransformedVertices();
            for (int i = 0; i < vertices.length - 3; i+=2) {
                Vector2 p1 = new Vector2(vertices[i], vertices[i+1]);
                Vector2 p2 = new Vector2(vertices[i+2], vertices[i+3]);
                float currentRange = calculateRange(sensorBegin, sensorEnd, p1, p2);
                if (currentRange < minRange) {
                    minRange = currentRange;
                }
            }
            // Последнюю вершину с первой
            Vector2 p1 = new Vector2(vertices[vertices.length - 2], vertices[vertices.length - 1]);
            Vector2 p2 = new Vector2(vertices[0], vertices[1]);
            float currentRange = calculateRange(sensorBegin, sensorEnd, p1, p2);
            if (currentRange < minRange) {
                minRange = currentRange;
            }
        }
        if (minRange < maxRange) {
            seeing = Seeing.OBJECT;
        } else {
            seeing = Seeing.NOTHING;
        }
        range = (int)minRange;
        x = range * Math.cos(sum_angle);
        y = range * Math.sin(sum_angle);
        intersectPoint = new Vector2((float)(x + sensorBegin.x), (float)y + (sensorBegin.y));
        float rangeToSub;
        if (angle == 0) {
            rangeToSub = panzer.getPanzerSize().x / 2;
        } else {
            rangeToSub = (panzer.getPanzerSize().y / 2) / (float)Math.sin(Math.abs(angle));
        }
        range -= rangeToSub;
        debugMessage();
    }

    public void reset(){
        Panzer panzer = mapManager.getPanzer();
        float panzer_angle = panzer.getAngle();
        panzer_angle *= Math.PI / 180;
        float sum_angle = panzer_angle + angle;
        position = panzer.getPosition();
        double x = maxRange * Math.cos(sum_angle);
        double y = maxRange * Math.sin(sum_angle);
        sensorBegin = new Vector2(position);
        sensorEnd = new Vector2((float)(x + sensorBegin.x), (float)y + (sensorBegin.y));
        seeing = Seeing.NOTHING;
    }

    private float calculateRange(Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4) {
        float range = Float.MAX_VALUE;
        Vector2 intersection = new Vector2();
        if (Intersector.intersectLines(p1, p2, p3, p4, intersection)) {
            if (   (intersection.x - p1.x)*(intersection.x - p2.x) <= 0 &&
                    (intersection.x - p3.x)*(intersection.x - p4.x) <= 0 &&
                    (intersection.y - p1.y)*(intersection.y - p2.y) <= 0 &&
                    (intersection.y - p3.y)*(intersection.y - p4.y) <= 0) {
                range = (float) Math.sqrt
                        ((intersection.x - p1.x) * (intersection.x - p1.x) +
                                (intersection.y - p1.y) * (intersection.y - p1.y)
                        );
            }
        }
        return range;
    }

    public void setDebugTag(String debugTag) {
        this.debugTag = debugTag;
    }

    public Vector2 getSensorBegin() {
        return sensorBegin;
    }

    public Vector2 getSensorEnd() {
        return sensorEnd;
    }

    public int getRange() { return range; }

    public Seeing getSeeing() {
        return seeing;
    }

    public Vector2 getIntersectPoint() {
        return intersectPoint;
    }

    private void debugMessage() {
        if (seeing == Seeing.OBJECT) {
            System.out.println("Sensor " + debugTag + " just found object in " + range + " pixels!");
        } else {
            System.out.println("Sensor " + debugTag + " found nothing...");
        }
    }

    public boolean seeingObject() {
        return seeing == Seeing.OBJECT;
    }
}
