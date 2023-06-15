package com.example.application.backends;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class FrameMember {

    public int ID; //front end ID

    public BigDecimal BCx; //begin x coordinate
    public BigDecimal BCy; //begin y coordinate
    public BigDecimal ECx; //end x coordinate
    public BigDecimal ECy; //end y coordinate
    public BigDecimal L; //length
    public BigDecimal L_15;
    public BigDecimal cos; //cos of the angle measured counterclockwise from the global coordinate system to the local coordinate system
    public BigDecimal sin; //sin of angle measured counterclockwise from the global coordinate system to the local coordinate system

    public BigDecimal A; //area
    public BigDecimal E; //modulus of elasticity
    public BigDecimal I; //moment of inertia

    public short BJN; //begin joint number
    public short EJN; //end joint number
    public boolean[] BJFR = new boolean[]{false,false,false}; //begin joint force release
    public boolean[] EJFR = new boolean[]{false,false,false}; //end joint force release

    public Matrix T = new Matrix((short) 6,(short) 6); //transformation matrix
    public Matrix k = new Matrix((short) 6,(short) 6); //local stiffness matrix
    public Matrix K = new Matrix((short) 6,(short) 6); //global stiffness matrix

    public Matrix Ff = new Matrix((short) 6,(short) 1); //global fixed-end force vector
    public Matrix Qf = new Matrix((short) 6,(short) 1); //local fixed-end force vector
    public Matrix v = new Matrix((short) 6,(short) 1); //global end displacement vector
    public Matrix u = new Matrix((short) 6,(short) 1); //local end displacement vector
    public Matrix F = new Matrix((short) 6,(short) 1); //global end force vector
    public Matrix Q = new Matrix((short) 6,(short) 1); //local end force vector

    public static final int precision = 15;

    public FrameMember (BigDecimal BCx, BigDecimal BCy, BigDecimal ECx, BigDecimal ECy, CrossSection CS) {
        this.BCx = BCx;
        this.BCy = BCy;
        this.ECx = ECx;
        this.ECy = ECy;
        this.A = CS.A;
        this.E = CS.E;
        this.I = CS.I;
        this.L_15 = (ECx.subtract(BCx).pow(2).add(ECy.subtract(BCy).pow(2))).sqrt(new MathContext(precision)).setScale(15, RoundingMode.HALF_UP).stripTrailingZeros();
        this.L = L_15.setScale(2, RoundingMode.FLOOR).stripTrailingZeros();
        this.cos = ECx.subtract(BCx).divide(L_15, precision, RoundingMode.FLOOR).setScale(precision, RoundingMode.FLOOR).stripTrailingZeros();
        this.sin = ECy.subtract(BCy).divide(L_15, precision, RoundingMode.FLOOR).setScale(precision, RoundingMode.FLOOR).stripTrailingZeros();

        BigDecimal o = new BigDecimal("0");
        BigDecimal o1 = new BigDecimal("1");
        T.insert(new BigDecimal[][]
                {{cos,sin,o,o,o,o},
                {sin.negate(),cos,o,o,o,o},
                {o,o,o1,o,o,o},
                {o,o,o,cos,sin,o},
                {o,o,o,sin.negate(),cos,o},
                {o,o,o,o,o,o1}});
    }

    public FrameMember (BigDecimal Cx, BigDecimal Cy, CrossSection CS, BigDecimal cos, BigDecimal sin) {
        this.BCx = Cx;
        this.BCy = Cy;
        this.ECx = Cx;
        this.ECy = Cy;
        this.A = CS.A;
        this.E = CS.E;
        this.I = CS.I;
        this.L_15 = BigDecimal.ONE;
        this.L = BigDecimal.ONE;
        this.cos = cos;
        this.sin = sin;

        BigDecimal o = new BigDecimal("0");
        BigDecimal o1 = new BigDecimal("1");
        T.insert(new BigDecimal[][]
                {{cos,sin,o,o,o,o},
                {sin.negate(),cos,o,o,o,o},
                {o,o,o1,o,o,o},
                {o,o,o,cos,sin,o},
                {o,o,o,sin.negate(),cos,o},
                {o,o,o,o,o,o1}});
    }

    public void calStiffness() {
        BigDecimal L = L_15;

        BigDecimal o = new BigDecimal("0");
        BigDecimal o2 = new BigDecimal("2");
        BigDecimal o3 = new BigDecimal("3");
        BigDecimal o4 = new BigDecimal("4");
        BigDecimal o6 = new BigDecimal("6");
        BigDecimal o12 = new BigDecimal("12");

        if (!BJFR[0] && !BJFR[1] && !BJFR[2] && !EJFR[0] && !EJFR[1] && EJFR[2]) {
            k.insert(new BigDecimal[][]
                    {{A.multiply(L.pow(2)).divide(I, precision,RoundingMode.FLOOR),o,o,A.multiply(L.pow(2)).divide(I, precision,RoundingMode.FLOOR).negate(),o,o},
                    {o,o3,o3.multiply(L),o,o3.negate(),o},
                    {o,o3.multiply(L),o3.multiply(L.pow(2)),o,o3.multiply(L).negate(),o},
                    {A.multiply(L.pow(2)).divide(I, precision,RoundingMode.FLOOR).negate(),o,o,A.multiply(L.pow(2)).divide(I, precision,RoundingMode.FLOOR),o,o},
                    {o,o3.negate(),o3.multiply(L).negate(),o,o3,o},
                    {o,o,o,o,o,o}});
        } //[rigid/free/fixed/pined/roller]-[hinge]

        else if (!BJFR[0] && !BJFR[1] && BJFR[2] && !EJFR[0] && !EJFR[1] && !EJFR[2]) {
            k.insert(new BigDecimal[][]
                    {{A.multiply(L.pow(2)).divide(I, precision,RoundingMode.FLOOR),o,o,A.multiply(L.pow(2)).divide(I, precision,RoundingMode.FLOOR).negate(),o,o},
                    {o,o3,o,o,o3.negate(),o3.multiply(L)},
                    {o,o,o,o,o,o},
                    {A.multiply(L.pow(2)).divide(I, precision,RoundingMode.FLOOR).negate(),o,o,A.multiply(L.pow(2)).divide(I, precision,RoundingMode.FLOOR),o,o},
                    {o,o3.negate(),o,o,o3,o3.multiply(L).negate()},
                    {o,o3.multiply(L),o,o,o3.multiply(L).negate(),o3.multiply(L.pow(2))}});
        } //[hinge]-[rigid/free/fixed/pined/roller]

        else if (!BJFR[0] && !BJFR[1] && BJFR[2] && !EJFR[0] && !EJFR[1] && EJFR[2]) {
            k.insert(new BigDecimal[][]
                    {{A.multiply(L.pow(2)).divide(I, precision,RoundingMode.FLOOR),o,o,A.multiply(L.pow(2)).divide(I, precision,RoundingMode.FLOOR).negate(),o,o},
                    {o,o,o,o,o,o},
                    {o,o,o,o,o,o},
                    {A.multiply(L.pow(2)).divide(I, precision,RoundingMode.FLOOR).negate(),o,o,A.multiply(L.pow(2)).divide(I, precision,RoundingMode.FLOOR),o,o},
                    {o,o,o,o,o,o},
                    {o,o,o,o,o,o}});
        } //[hinge]-[hinge]

        else {
            k.insert(new BigDecimal[][]
                    {{A.multiply(L.pow(2)).divide(I, precision,RoundingMode.FLOOR),o,o,A.multiply(L.pow(2)).divide(I, precision,RoundingMode.FLOOR).negate(),o,o},
                    {o,o12,o6.multiply(L),o,o12.negate(),o6.multiply(L)},
                    {o,o6.multiply(L),o4.multiply(L.pow(2)),o,o6.multiply(L).negate(),o2.multiply(L.pow(2))},
                    {A.multiply(L.pow(2)).divide(I, precision,RoundingMode.FLOOR).negate(),o,o,A.multiply(L.pow(2)).divide(I, precision,RoundingMode.FLOOR),o,o},
                    {o,o12.negate(),o6.multiply(L).negate(),o,o12,o6.multiply(L).negate()},
                    {o,o6.multiply(L),o2.multiply(L.pow(2)),o,o6.multiply(L).negate(),o4.multiply(L.pow(2))}});
        } //[rigid/free/fixed/pined/roller]-[rigid/free/fixed/pined/roller]

        k.insert(k.scalarMultiply(E.multiply(I).divide(L.pow(3), precision,RoundingMode.FLOOR)));
    }

    public void calGlobalStiffness () {
        K.insert(T.transpose().multiply(k).multiply(T));
    }

    public void calFixEndForceInducedByLinearlyDistributedLoad (short LT, BigDecimal W1, BigDecimal W2) {
        BigDecimal L = L_15;

        BigDecimal o2 = new BigDecimal("2");
        BigDecimal o3 = new BigDecimal("3");
        BigDecimal o6 = new BigDecimal("6");
        BigDecimal o7 = new BigDecimal("7");
        BigDecimal o20 = new BigDecimal("20");
        BigDecimal o60 = new BigDecimal("60");

        BigDecimal FAb = L.multiply(o2.multiply(W1).add(W2)).divide(o6, precision,RoundingMode.FLOOR).negate();
        BigDecimal FSb = L.multiply(o7.multiply(W1).add(o3.multiply(W2))).divide(o20, precision,RoundingMode.FLOOR).negate();
        BigDecimal FMb = L.pow(2).multiply(o3.multiply(W1).add(o2.multiply(W2))).divide(o60, precision,RoundingMode.FLOOR).negate();
        BigDecimal FAe = L.multiply(o2.multiply(W2).add(W1)).divide(o6, precision,RoundingMode.FLOOR).negate();
        BigDecimal FSe = L.multiply(o3.multiply(W1).add(o7.multiply(W2))).divide(o20, precision,RoundingMode.FLOOR).negate();
        BigDecimal FMe = L.pow(2).multiply(o2.multiply(W1).add(o3.multiply(W2))).divide(o60, precision,RoundingMode.FLOOR);

        if (LT == 1) {
            Qf.data[0][0] = Qf.data[0][0].add(FAb);
            Qf.data[3][0] = Qf.data[3][0].add(FAe);
        }
        else if (LT == 2) {
            Qf.data[1][0] = Qf.data[1][0].add(FSb);
            Qf.data[2][0] = Qf.data[2][0].add(FMb);
            Qf.data[4][0] = Qf.data[4][0].add(FSe);
            Qf.data[5][0] = Qf.data[5][0].add(FMe);
        }
        else if (LT == 3) {
            Qf.data[0][0] = Qf.data[0][0].add(cos.multiply(FAb));
            Qf.data[1][0] = Qf.data[1][0].add(sin.negate().multiply(FSb));
            Qf.data[2][0] = Qf.data[2][0].add(sin.negate().multiply(FMb));
            Qf.data[3][0] = Qf.data[3][0].add(cos.multiply(FAe));
            Qf.data[4][0] = Qf.data[4][0].add(sin.negate().multiply(FSe));
            Qf.data[5][0] = Qf.data[5][0].add(sin.negate().multiply(FMe));
        }
        else if (LT == 4) {
            Qf.data[0][0] = Qf.data[0][0].add(sin.multiply(FAb));
            Qf.data[1][0] = Qf.data[1][0].add(cos.multiply(FSb));
            Qf.data[2][0] = Qf.data[2][0].add(cos.multiply(FMb));
            Qf.data[3][0] = Qf.data[3][0].add(sin.multiply(FAe));
            Qf.data[4][0] = Qf.data[4][0].add(cos.multiply(FSe));
            Qf.data[5][0] = Qf.data[5][0].add(cos.multiply(FMe));
        }
    }

    public void calFixEndForceInducedByTemperatureChange(BigDecimal T, BigDecimal alpha) {
        Qf.data[0][0] = Qf.data[0][0].add(E.multiply(A).multiply(T).multiply(alpha));
        Qf.data[3][0] = Qf.data[3][0].add(E.multiply(A).multiply(T).multiply(alpha).negate());
    }

    public void calFixEndForceInducedByFabricationError(BigDecimal e) {
        BigDecimal L = L_15;

        Qf.data[0][0] = Qf.data[0][0].add(E.multiply(A).multiply(e).divide(L, precision,RoundingMode.FLOOR));
        Qf.data[3][0] = Qf.data[3][0].add(E.multiply(A).multiply(e).divide(L, precision,RoundingMode.FLOOR).negate());
    }

    public Matrix calFixEndForceInducedBySupportSettlement(SupportSettlement SS, Joint BJ, Joint EJ, short NDF) {
        Matrix vfs = new Matrix((short) 6,(short) 1);
        Matrix Ffs = new Matrix((short) 6,(short) 1);
        Matrix Pfs = new Matrix(NDF, (short) 1);
        if (SS.Cx.compareTo(BCx) == 0 && SS.Cy.compareTo(BCy) == 0) {
            vfs.data[0][0] = SS.settlement[0];
            vfs.data[1][0] = SS.settlement[1];
            vfs.data[2][0] = SS.settlement[2];
        }
        else if (SS.Cx.compareTo(ECx) == 0 && SS.Cy.compareTo(ECy) == 0) {
            vfs.data[3][0] = SS.settlement[0];
            vfs.data[4][0] = SS.settlement[1];
            vfs.data[5][0] = SS.settlement[2];
        }
        Ffs.insert(K.multiply(vfs));
        for (int i = 0; i < 3; i ++) {
            if (BJ.order[i] < NDF) {
                Pfs.data[BJ.order[i]][0] = Pfs.data[BJ.order[i]][0].add(Ffs.data[i][0]);
            }
            else {
                v.data[i][0] = v.data[i][0].add(SS.settlement[i]);
            }
            if (EJ.order[i] < NDF) {
                Pfs.data[EJ.order[i]][0] = Pfs.data[EJ.order[i]][0].add(Ffs.data[i + 3][0]);
            }
            else {
                v.data[i + 3][0] = v.data[i + 3][0].add(SS.settlement[i]);
            }
        }
        return Pfs;
    }

    public void calFixedEndForceAfterMemberRelease() {
        BigDecimal o = new BigDecimal("0");
        BigDecimal o2 = new BigDecimal("2");
        BigDecimal o3 = new BigDecimal("3");

        BigDecimal FAb = Qf.data[0][0];
        BigDecimal FSb = Qf.data[1][0];
        BigDecimal FMb = Qf.data[2][0];
        BigDecimal FAe = Qf.data[3][0];
        BigDecimal FSe = Qf.data[4][0];
        BigDecimal FMe = Qf.data[5][0];

        if (!BJFR[0] && !BJFR[1] && !BJFR[2] && !EJFR[0] && !EJFR[1] && EJFR[2]) {
            Qf.data[1][0] = FSb.subtract(o3.multiply(FMe).divide(o2.multiply(L), precision,RoundingMode.FLOOR));
            Qf.data[2][0] = FMb.subtract(FMe.divide(o2, precision,RoundingMode.FLOOR));
            Qf.data[4][0] = FSe.add(o3.multiply(FMe).divide(o2.multiply(L), precision,RoundingMode.FLOOR));
            Qf.data[5][0] = o;
        } //[rigid/fixed/pined/roller]-[hinge]
        else if (!BJFR[0] && !BJFR[1] && !BJFR[2] && EJFR[0] && EJFR[1] && EJFR[2]) {
            Qf.data[0][0] = FAb.add(FAe);
            Qf.data[1][0] = FSb.add(FSe);
            Qf.data[2][0] = FMb.add(FMe).add(L.multiply(FSe));
            Qf.data[3][0] = o;
            Qf.data[4][0] = o;
            Qf.data[5][0] = o;
        } //[rigid/fixed/pined/roller]-[free]
        else if (!BJFR[0] && !BJFR[1] && BJFR[2] && !EJFR[0] && !EJFR[1] && !EJFR[2]) {
            Qf.data[1][0] = FSb.subtract(o3.multiply(FMb).divide(o2.multiply(L), precision,RoundingMode.FLOOR));
            Qf.data[2][0] = o;
            Qf.data[4][0] = FSe.add(o3.multiply(FMb).divide(o2.multiply(L), precision,RoundingMode.FLOOR));
            Qf.data[5][0] = FMe.subtract(FMb.divide(o2, precision,RoundingMode.FLOOR));
        } //[hinge]-[rigid/fixed/pined/roller]
        else if (!BJFR[0] && !BJFR[1] && BJFR[2] && !EJFR[0] && !EJFR[1] && EJFR[2]) {
            Qf.data[1][0] = FSb.subtract(FMb.add(FMe).divide(L, precision,RoundingMode.FLOOR));
            Qf.data[2][0] = o;
            Qf.data[4][0] = FSe.add(FMb.add(FMe).divide(L, precision,RoundingMode.FLOOR));
            Qf.data[5][0]= o;
        } //[hinge]-[hinge]
        else if (!BJFR[0] && !BJFR[1] && BJFR[2] && EJFR[0] && EJFR[1] && EJFR[2]) {
            Qf.data[0][0] = FAb.add(FAe);
            Qf.data[1][0] = FSb.add(FSe);
            Qf.data[2][0] = o;
            Qf.data[3][0] = o;
            Qf.data[4][0] = o;
            Qf.data[5][0] = o;
        } //[hinge]-[free]
        else if (BJFR[0] && BJFR[1] && BJFR[2] && !EJFR[0] && !EJFR[1] && !EJFR[2]) {
            Qf.data[0][0] = o;
            Qf.data[1][0] = o;
            Qf.data[2][0] = o;
            Qf.data[3][0] = FAb.add(FAe);
            Qf.data[4][0] = FSb.add(FSe);
            Qf.data[5][0] = FMb.add(FMe).subtract(L.multiply(FSb));
        } //[free]-[rigid/fixed/pined/roller]
        else if (BJFR[0] && BJFR[1] && BJFR[2] && !EJFR[0] && !EJFR[1] && EJFR[2]) {
            Qf.data[0][0] = o;
            Qf.data[1][0] = o;
            Qf.data[2][0] = o;
            Qf.data[3][0] = FAb.add(FAe);
            Qf.data[4][0] = FSb.add(FSe);
            Qf.data[5][0] = o;
        } //[free]-[hinge]
        else if (BJFR[0] && BJFR[1] && BJFR[2] && EJFR[0] && EJFR[1] && EJFR[2]) {
            Qf.data[0][0] = o;
            Qf.data[1][0] = o;
            Qf.data[2][0] = o;
            Qf.data[3][0] = o;
            Qf.data[4][0] = o;
            Qf.data[5][0] = o;
        } //[free]-[free]
    }

    public void calGlobalFixEndForce () {
        Ff.insert(T.transpose().multiply(Qf));
    }
}

