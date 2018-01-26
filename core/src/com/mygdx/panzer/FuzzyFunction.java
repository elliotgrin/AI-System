package com.mygdx.panzer;

/*
этот класс не будет иметь конкретных классов наследников в дереве проекта,
работа с ним будет вестись путем создания анонимных классов.
Реализация функции будет определяться при обьявлении анонимного класса
Пример:
FuzzyFunction fuzzyFun1 = new FuzzyFunction(){
            public double fun(double x){
                if (x < 10)
                    return 10/x;
                else if (x > 20)
                    return 1 - 10/(x - 20);
                else
                    return 1;
            }
        };
 */
public abstract class FuzzyFunction {
    abstract public float fun(float x);
}
