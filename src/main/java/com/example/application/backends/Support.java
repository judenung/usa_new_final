package com.example.application.backends;

import java.math.BigDecimal;

public class Support {
    public BigDecimal Cx; //x coordinate
    public BigDecimal Cy; //y coordinate
    public short ST; //support type
    public BigDecimal angle; //rotational angle for incline roller

    public Support(BigDecimal Cx, BigDecimal Cy, short ST) {
        this.Cx = Cx;
        this.Cy = Cy;
        this.ST = ST;
    }
}

    /* [Support Type]
    1: fixed (prevent displacement in x,y,z-axis)
    2: pined (prevent displacement in x,y-axis)
    3: incline roller (prevent displacement perpendicular to the incline plane)
    */