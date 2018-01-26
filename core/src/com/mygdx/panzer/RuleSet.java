package com.mygdx.panzer;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class RuleSet {
    private Array<Rule> rules;
    Panzer panzer;
    private Direction direction;

    public enum Direction {
        LEFT,
        RIGHT
    }

    public RuleSet(Array<Rule> rules, Panzer panzer){
        this.rules = rules;
        this.panzer = panzer;
        direction = Direction.LEFT;

        System.out.println(Settings.getSensorRange());
    }

    // Вектор значений - 2 значения, нагрузка на левую и правую функцию
    Vector2 apply(Array<Sensor> sensors){
        Array<Float> distances = new Array<>();

        float dist1 = !sensors.get(0).seeingObject() ? Float.POSITIVE_INFINITY : sensors.get(0).getRange();
        float dist2 = !sensors.get(1).seeingObject() ? Float.POSITIVE_INFINITY : sensors.get(1).getRange();
        float dist3 = !sensors.get(2).seeingObject() ? Float.POSITIVE_INFINITY : sensors.get(2).getRange();

        distances.add(dist1);
        distances.add(dist2);
        distances.add(dist3);

        updateDirection();

        Array<FuzzyFunction> leftTrackFuns = new Array<>();
        Array<FuzzyFunction> rightTrackFuns = new Array<>();
        int i = 0;
        for (Rule r: rules){
            i+=1;
            Array<FuzzyFunction> ruleRes = r.apply(distances, direction);

            if (ruleRes.get(0) != Rule.zeroFunction)
                System.out.println("Rule " + i + " is apply");

            leftTrackFuns.add(ruleRes.get(0));
            rightTrackFuns.add(ruleRes.get(1));
        }

        FuzzyFunction leftTrackSummaryFun = combine(leftTrackFuns);
        FuzzyFunction rightTrackSummaryFun = combine(rightTrackFuns);

        float leftPower = massCentre(leftTrackSummaryFun,0, 200, 1000);
        float rightPower = massCentre(rightTrackSummaryFun, 0, 200, 1000);

        return new Vector2(leftPower, rightPower);
    }

    private void updateDirection(){

        Panzer panzer = MapManager.getInstance().getPanzer();
        float panzer_angle = panzer.getAngle();
        // Переводим в радианы
        panzer_angle *= Math.PI / 180;

        double x2 = 10* Math.cos(panzer_angle);
        double y2 = 10* Math.sin(panzer_angle);
        Vector2 lineBegin = new Vector2(panzer.getPosition());
        Vector2 lineEnd = new Vector2((float)(x2 + lineBegin.x), (float)y2 + (lineBegin.y));

        int rawDirection = Intersector.pointLineSide(lineBegin, lineEnd, Settings.getFinishPos());

        if (rawDirection >= 0){
            direction = Direction.LEFT;
        }
        else
            direction = Direction.RIGHT;

        //System.out.println(direction);
    }

    private static FuzzyFunction combine(final Array<FuzzyFunction> funs){
        return new FuzzyFunction(){
            @Override
            public float fun(float x){
                float max = -1;
                for (FuzzyFunction f: funs){
                    if (f.fun(x) > max)
                        max = f.fun(x);
                }

                return max;
            }
        };
    }

    //превращает функции f(x) в x*f(x), нужно для вычисления интеграла
    private static FuzzyFunction modify(final FuzzyFunction f){
        return new FuzzyFunction() {
            @Override
            public float fun(float x) {
                return x * f.fun(x);
            }
        };
    }

    private static float massCentre(FuzzyFunction f, float start, float end, int n){
        float divider = calcIntegral(modify(f), start, end, n);
        float denominator = calcIntegral(f, start, end, n);
        if (denominator == 0)
            return 0;
        return divider/denominator;
    }

    private static float calcIntegral(FuzzyFunction f, float start, float end, int n){
        float step = (end - start)/ n;
        float area = 0;
        float x = start + step/2;

        for (int i = 0; i < n; ++i){
            area += f.fun(x) * step;
            x += step;
        }

        //System.out.println("Integral Value is " + area);
        return area;
    }

    //здесь будет находиться инициализация всех правил
    public static Array<Rule> getRules(){
        Array<Rule> rules = new Array<>();
        final Array<FuzzyFunction> trackFuns = FunctionSet.getTrackFuns(Settings.getMaxSpeed());
        final Array<FuzzyFunction> sensorFuns = FunctionSet.getSensorFuns(Settings.getSensorRange());

        //1) 1,2,3 = mid -> td = 1, -td = 2
        rules.add( new Rule() {
            @Override
            public Array<FuzzyFunction> apply(Array<Float> distances, Direction direction) {
                float v1 = sensorFuns.get(1).fun(distances.get(0));
                float v2 = sensorFuns.get(1).fun(distances.get(1));
                float v3 = sensorFuns.get(1).fun(distances.get(2));

                float ruleValue = Math.min(v1, Math.min(v2, v3));
                FuzzyFunction first = topBound(ruleValue, trackFuns.get(1));
                FuzzyFunction second = topBound(ruleValue, trackFuns.get(2));
                Array<FuzzyFunction> powers = new Array<>();

                if (direction == Direction.LEFT)
                {
                    powers.add(first);
                    powers.add(second);
                }
                else{
                    powers.add(second);
                    powers.add(first);
                }

                return powers;
            }
        } );
        //2) 1-low, 2,3 - mid, td==L  -> L=2, R=1
        //                               else L = 2, R = 1
        rules.add( new Rule(){
            @Override
            public Array<FuzzyFunction> apply(Array<Float> distances, Direction direction) {
                float v1 = sensorFuns.get(0).fun(distances.get(0));
                float v2 = sensorFuns.get(1).fun(distances.get(1));
                float v3 = sensorFuns.get(1).fun(distances.get(2));

                float ruleValue = Math.min(v1, Math.min(v2, v3));

                Array<FuzzyFunction> powers = new Array<>();

                if (direction == Direction.LEFT)
                {
                    powers.add(topBound(ruleValue, trackFuns.get(2)));
                    powers.add(topBound(ruleValue, trackFuns.get(1)));
                }
                else{
                    powers.add(topBound(ruleValue, trackFuns.get(2)));
                    powers.add(topBound(ruleValue, trackFuns.get(1)));
                }

                return powers;
            }
        });

        //3) 1-mid, 2 - mid, 3 - low, td==R  -> L=1, R=2
        //                                  else L = 1, R = 2
        rules.add( new Rule(){
            @Override
            public Array<FuzzyFunction> apply(Array<Float> distances, Direction direction) {
                float v1 = sensorFuns.get(1).fun(distances.get(0));
                float v2 = sensorFuns.get(1).fun(distances.get(1));
                float v3 = sensorFuns.get(0).fun(distances.get(2));

                float ruleValue = Math.min(v1, Math.min(v2, v3));

                Array<FuzzyFunction> powers = new Array<>();

                if (direction == Direction.LEFT)
                {
                    powers.add(topBound(ruleValue, trackFuns.get(1)));
                    powers.add(topBound(ruleValue, trackFuns.get(2)));
                }
                else{
                    powers.add(topBound(ruleValue, trackFuns.get(1)));
                    powers.add(topBound(ruleValue, trackFuns.get(2)));
                }

                return powers;
            }
        });

        //4 1- mid, 2- low, 3 - mid -> TD = 0, -TD = 2
        rules.add( new Rule() {
            @Override
            public Array<FuzzyFunction> apply(Array<Float> distances, Direction direction) {
                float v1 = sensorFuns.get(1).fun(distances.get(0));
                float v2 = sensorFuns.get(0).fun(distances.get(1));
                float v3 = sensorFuns.get(1).fun(distances.get(2));

                float ruleValue = Math.min(v1, Math.min(v2, v3));

                Array<FuzzyFunction> powers = new Array<>();

                if (direction == Direction.LEFT)
                {
                    powers.add(topBound(ruleValue, trackFuns.get(0)));
                    powers.add(topBound(ruleValue, trackFuns.get(2)));
                }
                else{
                    powers.add(topBound(ruleValue, trackFuns.get(2)));
                    powers.add(topBound(ruleValue, trackFuns.get(0)));
                }

                return powers;
            }
        } );

        //5 invert) 1- low, 2- mid, 3 - low -> TD = 2, -TD = 2
        rules.add( new Rule() {
            @Override
            public Array<FuzzyFunction> apply(Array<Float> distances, Direction direction) {
                float v1 = sensorFuns.get(0).fun(distances.get(0));
                float v2 = sensorFuns.get(1).fun(distances.get(1));
                float v3 = sensorFuns.get(0).fun(distances.get(2));

                float ruleValue = Math.min(v1, Math.min(v2, v3));

                Array<FuzzyFunction> powers = new Array<>();

                powers.add(topBound(ruleValue, trackFuns.get(2)));
                powers.add(topBound(ruleValue, trackFuns.get(2)));

                return powers;
            }
        } );


        //6) 1 - low, 2 - low, 3 - mid -> L= 2, R= 0
        rules.add( new Rule() {
            @Override
            public Array<FuzzyFunction> apply(Array<Float> distances, Direction direction) {
                float v1 = sensorFuns.get(0).fun(distances.get(0));
                float v2 = sensorFuns.get(0).fun(distances.get(1));
                float v3 = sensorFuns.get(1).fun(distances.get(2));

                float ruleValue = Math.min(v1, Math.min(v2, v3));

                Array<FuzzyFunction> powers = new Array<>();

                powers.add(topBound(ruleValue, trackFuns.get(3)));
                powers.add(topBound(ruleValue, trackFuns.get(0)));

                return powers;
            }
        } );

        //7) 1 - mid, 2 - low, 3 - low -> L= 0, R= 2
        rules.add( new Rule() {
            @Override
            public Array<FuzzyFunction> apply(Array<Float> distances, Direction direction) {
                float v1 = sensorFuns.get(1).fun(distances.get(0));
                float v2 = sensorFuns.get(0).fun(distances.get(1));
                float v3 = sensorFuns.get(0).fun(distances.get(2));

                float ruleValue = Math.min(v1, Math.min(v2, v3));

                Array<FuzzyFunction> powers = new Array<>();

                powers.add(topBound(ruleValue, trackFuns.get(0)));
                powers.add(topBound(ruleValue, trackFuns.get(3)));

                return powers;
            }
        } );

        //8) 1,2,3 - low, TD = 0, -TD = 1
        rules.add( new Rule() {
            @Override
            public Array<FuzzyFunction> apply(Array<Float> distances, Direction direction) {
                float v1 = sensorFuns.get(0).fun(distances.get(0));
                float v2 = sensorFuns.get(0).fun(distances.get(1));
                float v3 = sensorFuns.get(0).fun(distances.get(2));

                float ruleValue = Math.min(v1, Math.min(v2, v3));

                Array<FuzzyFunction> powers = new Array<>();
                if (direction == Direction.LEFT){
                    powers.add(topBound(ruleValue, trackFuns.get(0)));
                    powers.add(topBound(ruleValue, trackFuns.get(1)));
                }
                else{
                    powers.add(topBound(ruleValue, trackFuns.get(0)));
                    powers.add(topBound(ruleValue, trackFuns.get(1)));
                }

                return powers;
            }
        } );

        return rules;
    }
}
