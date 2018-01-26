package com.mygdx.panzer;

import com.badlogic.gdx.utils.Array;

//в этом классе будет происходить инициализация всех наших функций
public class FunctionSet {

    public static Array<FuzzyFunction> getSensorFuns(float maxRange){
        Array<FuzzyFunction> funs = new Array<>();

        //контрольные значения дистанций
        final float d1 = 60;
        final float d2 = 95;

        //final double d3 = 60;
        //final double d4 = 80;
        //double d5 = 50;

        //близко
        funs.add(new FuzzyFunction() {
            @Override
            public float fun(float x) {
                if (x < d1){
                    return 1;
                }
                else if (x < d2)
                    return 1 - (x - d1)/(d2 - d1);
                else
                    return 0;
            }
        });
        //средне
        funs.add(new FuzzyFunction() {
            @Override
            public float fun(float x) {
                if (x < d1)
                    return 0;
                else if (x < d2)
                    return (x - d1)/(d2 - d1);
                else
                    return 1;
            }
        });
        /*//далеко
        funs.add(new FuzzyFunction() {
            @Override
            public double fun(double x) {
                if (x < d3)
                    return 0;
                else if (x < d4)
                    return (x - d3)/(d4 - d3);
                else return 1;
            }
        });*/

        return funs;
    }

    public static Array<FuzzyFunction> getTrackFuns(float maxSpeed){
        Array<FuzzyFunction> funs = new Array<>();

        final float speed1 = 40;
        final float speed2 = 80;
        final float speed3 = 100;
        final float speed4 = 180;
        final float speed5 = 250;
        final float speed6 = 400;
        final float speed7 = 600;

        // 0
        funs.add(new FuzzyFunction() {
            @Override
            public float fun(float x) {
                float k = x/speed1;
                if ( x < speed1 )
                    return 1 - k;
                else
                    return 0;
            }
        });

        // 1
        funs.add(new FuzzyFunction() {
            @Override
            public float fun(float x) {
                if (x < speed1)
                    return x/speed1;
                else if (x < speed2)
                    return 1;
                else if (x < speed3)
                    return 1 - (x - speed2)/(speed3 - speed2);
                else
                    return 0;
            }
        });

        // 2
        funs.add(new FuzzyFunction() {
            @Override
            public float fun(float x) {
                if (x < speed2)
                    return 0;
                else if (x < speed3)
                    return (x - speed2)/(speed3 - speed2);
                else if (x < speed4)
                    return 1;
                else
                    return 0;
            }
        });

        // 3
        funs.add(new FuzzyFunction() {
            @Override
            public float fun(float x) {
                if (x < speed3)
                    return 0;
                else if (x < speed4)
                    return (x - speed3)/(speed4 - speed3);
                else if (x < speed5)
                    return 1;
                else
                    return 0;
            }
        });

        // 4
        funs.add(new FuzzyFunction() {
            @Override
            public float fun(float x) {
                if (x < speed4)
                    return 0;
                else if (x < speed5)
                    return (x - speed4)/(speed5 - speed4);
                else if (x < speed6)
                    return 1;
                else
                    return 0;
            }
        });

        // 5
        funs.add(new FuzzyFunction() {
            @Override
            public float fun(float x) {
                if (x < speed5)
                    return 0;
                else if (x < speed6)
                    return (x - speed5)/(speed6 - speed5);
                else if (x < speed7)
                    return 1;
                else
                    return 0;
            }
        });

        return funs;
    }
}
