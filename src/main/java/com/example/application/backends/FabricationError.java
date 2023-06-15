package com.example.application.backends;

import java.math.BigDecimal;

public class FabricationError {
    public short MN; //member number
    public BigDecimal e; //error length

    public FabricationError (short MN, BigDecimal e) {
        this.MN = MN;
        this.e = e;
    }
}
