package com.example.testwebsocket;

public class SocketData {

    private int a;
    private double p;
    private double q;
    private int f;
    private int l;
    private long T;
    private boolean m;
    private boolean M;

//    {"a":1140966270,"p":"40675.61000000","q":"0.07498000","f":1325062064,"l":1325062064,"T":1649860147316,"m":true,"M":true}

    public SocketData(int a, double p, double q, int f, int l, long T, boolean m, boolean M) {
        this.a = a;
        this.p = p;
        this.q = q;
        this.f = f;
        this.l = l;
        this.T = T;
        this.m = m;
        this.M = M;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public double getP() {
        return p;
    }

    public void setP(double p) {
        this.p = p;
    }

    public double getQ() {
        return q;
    }

    public void setQ(double q) {
        this.q = q;
    }

    public int getF() {
        return f;
    }

    public void setF(int f) {
        this.f = f;
    }

    public int getL() {
        return l;
    }

    public void setL(int l) {
        this.l = l;
    }

    public long getT() {
        return T;
    }

    public void setT(long t) {
        T = t;
    }

    public boolean isM() {
        return m;
    }

    public void setM(boolean m) {
        this.m = m;
    }
}
