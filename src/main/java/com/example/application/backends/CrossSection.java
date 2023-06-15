package com.example.application.backends;

import java.math.BigDecimal;

public class CrossSection {
    public BigDecimal A; //area
    public BigDecimal E; //modulus of elasticity
    public BigDecimal I; //moment of inertia

    public CrossSection (BigDecimal A, BigDecimal E, BigDecimal I) {
        this.A = A;
        this.E = E;
        this.I = I;
    }
}
