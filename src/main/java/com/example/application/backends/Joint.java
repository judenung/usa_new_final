package com.example.application.backends;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Joint {
    public BigDecimal Cx; //x coordinate
    public BigDecimal Cy; //y coordinate
    public short JT; //joint type

    public ArrayList<Short> MN = new ArrayList<>(); //member number
    public short[] order = new short[3]; //order of joint displacement label
    public boolean[] DR; //displacement restrain [x-axis movement, y-axis movement, z-axis rotation]

    public Joint (BigDecimal Cx, BigDecimal Cy) {
        this.Cx = Cx;
        this.Cy = Cy;
        this.setJoinType((short) 0);
    }

    public void setJoinType (short JT) {
        this.JT = JT;
        if (JT == 0) {
            DR = new boolean[]{false, false, false};
        }
        else if (JT == 1) {
            DR = new boolean[]{true, true, true};
        }
        else if (JT == 2) {
            DR = new boolean[]{true, true, false};
        }
        else if (JT == 3) {
            DR = new boolean[]{true, true, false};
        }
        else if (JT == 4) {
            DR = new boolean[]{false, true, false};
        }
        else if (JT == 5) {
            DR = new boolean[]{true, false, false};
        }
    }
}

    /* [Joint Type]
    0: rigid / free joint (allow displacement in x,y,z-axis, doesn't release force)
    1: fixed joint (prevent displacement in x,y,z-axis, doesn't release force)
    2: pined joint (prevent displacement in x,y-axis, doesn't release force)
    3: incline roller
    4: roller
    5: vertical roller
     */
