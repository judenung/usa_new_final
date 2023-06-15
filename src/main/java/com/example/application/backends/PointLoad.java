package com.example.application.backends;

import java.math.BigDecimal;

public class PointLoad {
    public short MN; //member number
    public short LT; //load type

    public BigDecimal d; //distant from start joints
    public BigDecimal[] load = new BigDecimal[3]; //load in the x-axis, y-axis, couple moment

    public PointLoad (short MN, short LT, BigDecimal d, BigDecimal Fx, BigDecimal Fy, BigDecimal M) {
        this.MN = MN;
        this.LT = LT;
        this.d = d;
        this.load[0] = Fx;
        this.load[1] = Fy;
        this.load[2] = M;
    }
}

    /*
    1: point loads with respect to the local coordinate system at distant d from start joint (in the local coordinate system)
    2: point loads with respect to the global coordinate system at distant d from start joint (in the local coordinate system)
     */