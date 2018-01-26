package com.mygdx.panzer;

import com.badlogic.gdx.utils.Array;

/* Работа с этим классом, как и с FuzzyFunction, тоже будет вестись путем создания абстрактных классов
* Пример:
  rule1 = new Rule(){
        public Array<FuzzyFunction> apply(Array<Double> distances){
            double ruleValue = Math.min( lowDistanceRule(distances[0]), midDistanceRule(distances[1]),
                        midDistanceRule(distances[2]) );

            FuzzyFunction leftFun = Rule.topBound(ruleValue, moveFuns[1]);
            FuzzyFunction rightFun = leftFun;

            return(leftFun, rightFun);
         }
  }

  */
public abstract class Rule {

    /*возвращает массив из 2 функций, первая для левой гуменицы, вторая для правой
    */
    public abstract Array<FuzzyFunction> apply(Array<Float> distances, RuleSet.Direction direction);

    /* Ограничивает функцию сверху, возвращает новую функцию */
    public static FuzzyFunction topBound(final float bound, final FuzzyFunction fuzzyFun){
        if (bound == 0)
            return zeroFunction;
        return new FuzzyFunction(){
            @Override
            public float fun(final float x){
                float value = fuzzyFun.fun(x);
                if (value > bound){
                    return bound;
                }
                else
                    return value;
            }
        };
    }

    static public FuzzyFunction zeroFunction = new FuzzyFunction(){
            @Override
            public float fun(float x){
                return 0;
            }
        };
}
