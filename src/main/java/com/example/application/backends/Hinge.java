package com.example.application.backends;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Hinge {
    public BigDecimal Cx; //x coordinate
    public BigDecimal Cy; //y coordinate

    public ArrayList<Short> MN = new ArrayList<>(); //member number

    public Hinge (BigDecimal Cx, BigDecimal Cy) {
        this.Cx = Cx;
        this.Cy = Cy;
    }
}
