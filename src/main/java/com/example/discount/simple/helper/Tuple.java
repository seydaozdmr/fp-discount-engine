package com.example.discount.simple.helper;

public class Tuple <T1, T2> {

    private T1 _1;
    private T2 _2;

        public Tuple(T1 t1, T2 t2) {
            this._1 = t1;
            this._2 = t2;
        }

    public T1 get_1() {
        return _1;
    }

    public void set_1(T1 _1) {
        this._1 = _1;
    }

    public T2 get_2() {
        return _2;
    }

    public void set_2(T2 _2) {
        this._2 = _2;
    }
}
