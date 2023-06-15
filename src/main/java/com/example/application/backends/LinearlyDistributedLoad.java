package com.example.application.backends;

import java.math.BigDecimal;

public class LinearlyDistributedLoad {
    public short MN; //member number
    public short LT; //load type

    public BigDecimal[] load = new BigDecimal[4]; //begin load value, end load value, begin location, end location

    public LinearlyDistributedLoad(short MN, short LT, BigDecimal W1, BigDecimal W2, BigDecimal a, BigDecimal b) {
        this.MN = MN;
        this.LT = LT;
        this.load[0] = W1;
        this.load[1] = W2;
        this.load[2] = a;
        this.load[3] = b;
    }
}

    /*
    1: linearly distributed horizontal loads with respect to the local coordinate system start and end at distant a and b
       from start and end joints (in the local coordinate system)
    2: linearly distributed vertical loads with respect to the local coordinate system start and end at distant a and b
       from start and end joints (in the local coordinate system)
    3: linearly distributed horizontal loads with respect to the global coordinate system start and end at distant a and b
       from start and end joints (in the local coordinate system)
    4: linearly distributed vertical loads with respect to the global coordinate system start and end at distant a and b
       from start and end joints (in the local coordinate system)
     */