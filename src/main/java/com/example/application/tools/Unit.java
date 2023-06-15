package com.example.application.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Unit {

    private static final String[] length = {"m", "ft"};
    private static final String[] area = {"mm\u00B2", "in\u00B2"};
    private static final String[] momentOfInertia = {"mm\u2074", "in\u2074",};
    private static final String[] modulusOfElasticity = {"GPa", "ksi"};
    private static final String[] force = {"kN", "kips"};
    private static final String[] moment = {"kN\u00B7m", "kips-ft"};
    private static final String[] smallLength = {"mm", "in"};
    private static final String[] angle = {"deg", "deg"};
    private static final String[] temp = {"°C", "°C"};
    private static final String[][] gridSizes = {
            {
                    "10 m × 10 m",
                    "20 m × 20 m",
                    "30 m × 30 m",
                    "40 m × 40 m",
                    "50 m × 50 m",
                    "60 m × 60 m",
                    "70 m × 70 m",
                    "80 m × 80 m",
                    "90 m × 90 m",
                    "100 m × 100 m"},
            {
                    "10 ft × 10 ft",
                    "20 ft × 20 ft",
                    "30 ft × 30 ft",
                    "40 ft × 40 ft",
                    "50 ft × 50 ft",
                    "60 ft × 60 ft",
                    "70 ft × 70 ft",
                    "80 ft × 80 ft",
                    "90 ft × 90 ft",
                    "100 ft × 100 ft"
            }};

    public String m;
    public String mm2;
    public String mm4;
    public String GPa;
    public String kN;
    public String kNm;
    public String mm;
    public String deg;
    public String C;
    public String[] mxm;

    public int unitIndex;

    public void setUnit (int index) {
        if (index < length.length) {
            m = length[index];
            mm2 = area[index];
            mm4 = momentOfInertia[index];
            GPa = modulusOfElasticity[index];
            kN = force[index];
            kNm = moment[index];
            mm = smallLength[index];
            deg = angle[index];
            C = temp[index];
            mxm = gridSizes[index];
            unitIndex = index;
        }
    }

    public Unit () {
        setUnit(0);
    }

    public BigDecimal convertA1 (BigDecimal BD) {
        if (unitIndex == 0) {
            return BD.divide(new BigDecimal("1000"), 15, RoundingMode.CEILING);
        }
        else {
            return BD.divide(new BigDecimal("144"), 15, RoundingMode.CEILING);
        }
    }

    public BigDecimal convertA2 (BigDecimal BD) {
        if (unitIndex == 0) {
            return BD.multiply(new BigDecimal("1000"));
        }
        else {
            return BD.multiply(new BigDecimal("144"));
        }
    }

    public BigDecimal convertE1 (BigDecimal BD) {
        if (unitIndex == 0) {
            return BD.multiply(new BigDecimal("1000000"));
        }
        else {
            return BD.multiply(new BigDecimal("144"));
        }
    }

    public BigDecimal convertE2 (BigDecimal BD) {
        if (unitIndex == 0) {
            return BD.divide(new BigDecimal("1000000"), 15, RoundingMode.CEILING);
        }
        else {
            return BD.divide(new BigDecimal("144"), 15, RoundingMode.CEILING);
        }
    }

    public BigDecimal convertI1 (BigDecimal BD) {
        if (unitIndex == 0) {
            return BD.divide(new BigDecimal("1000000"), 15, RoundingMode.CEILING);
        }
        else {
            return BD.divide(new BigDecimal("20736"), 15, RoundingMode.CEILING);
        }
    }

    public BigDecimal convertI2 (BigDecimal BD) {
        if (unitIndex == 0) {
            return BD.multiply(new BigDecimal("1000000"));
        }
        else {
            return BD.multiply(new BigDecimal("20736"));
        }
    }

    public BigDecimal convertSL1 (BigDecimal BD) {
        if (unitIndex == 0) {
            return BD.divide(new BigDecimal("1000"), 15, RoundingMode.CEILING);
        }
        else {
            return BD.divide(new BigDecimal("12"), 15, RoundingMode.CEILING);
        }
    }

    public BigDecimal convertSL2 (BigDecimal BD) {
        if (unitIndex == 0) {
            return BD.multiply(new BigDecimal("1000"));
        }
        else {
            return BD.multiply(new BigDecimal("12"));
        }
    }

    public BigDecimal convertOC1 (BigDecimal BD) {
        return BD.divide(new BigDecimal("1000000"), 15, RoundingMode.CEILING);
    }

    public BigDecimal convertOC2 (BigDecimal BD) {
            return BD.multiply(new BigDecimal("1000000"));
    }

    public BigDecimal convertRad1 (BigDecimal BD) {
        return BD.multiply(new BigDecimal("180")).divide(new BigDecimal(Math.PI), 15, RoundingMode.CEILING);
    }

    public BigDecimal convertRad2 (BigDecimal BD) {
        return BD.multiply(new BigDecimal(Math.PI)).divide(new BigDecimal("180"), 15, RoundingMode.CEILING);
    }

    public double round (double d) {
        return new BigDecimal(d + "").setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().doubleValue();
    }

    public double round2 (double d) {
        return new BigDecimal(d + "").setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().doubleValue();
    }

    public BigDecimal round (BigDecimal BD) {
        return BD.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    public BigDecimal round2 (BigDecimal BD) {
        return BD.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros();
    }

}
