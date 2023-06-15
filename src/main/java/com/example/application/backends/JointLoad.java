package com.example.application.backends;

import java.math.BigDecimal;

public class JointLoad {
    public BigDecimal Cx; //x coordinate
    public BigDecimal Cy; //y coordinate
    public BigDecimal[] load = new BigDecimal[3]; //load in the x-axis, y-axis, couple moment

    public JointLoad (BigDecimal Cx, BigDecimal Cy, BigDecimal Fx, BigDecimal Fy, BigDecimal M) {
        this.Cx = Cx;
        this.Cy = Cy;
        this.load[0] = Fx;
        this.load[1] = Fy;
        this.load[2] = M;
    }
}
