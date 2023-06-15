package com.example.application.backends;

import java.math.BigDecimal;

public class SupportSettlement {
    public BigDecimal Cx; //x coordinate
    public BigDecimal Cy; //y coordinate
    public BigDecimal[] settlement = new BigDecimal[3]; //settlement in the X and Y-axis and rotation in the Z-axis

    public SupportSettlement (BigDecimal Cx, BigDecimal Cy, BigDecimal Sx, BigDecimal Sy, BigDecimal Sz) {
        this.Cx = Cx;
        this.Cy = Cy;
        this.settlement[0] = Sx;
        this.settlement[1] = Sy;
        this.settlement[2] = Sz;
    }

}
