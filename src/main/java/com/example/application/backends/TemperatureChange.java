package com.example.application.backends;

import java.math.BigDecimal;

public class TemperatureChange {
    public short MN; //member number
    public BigDecimal T; //temperature change
    public BigDecimal alpha; //coefficient of thermal expansion

    public TemperatureChange (short MN, BigDecimal T, BigDecimal alpha) {
        this.MN = MN;
        this.T = T;
        this.alpha = alpha;
    }
}
