package com.example.application.backends;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Frame {
    public BigDecimal MS; //mesh size
    public short NDF; //number degree of freedom
    public short NR; //number of restrain

    public Matrix S; //structural stiffness matrix
    public Matrix P; //joint load vector
    public Matrix Pf; //fixed-end load vector
    public Matrix d; //joint displacement vector
    public Matrix R; //reaction force vector

    public ArrayList<FrameMember> mainFrameMembers = new ArrayList<>();
    public ArrayList<Joint> mainJoints = new ArrayList<>();

    public ArrayList<LinearlyDistributedLoad> mainLinearlyDistributedLoads = new ArrayList<>();
    public ArrayList<PointLoad> pointLoads = new ArrayList<>();
    public ArrayList<JointLoad> jointLoads = new ArrayList<>();
    public ArrayList<Support> supports = new ArrayList<>();
    public ArrayList<CrossSection> crossSections = new ArrayList<>();

    public ArrayList<TemperatureChange> temperatureChanges = new ArrayList<>();
    public ArrayList<FabricationError> fabricationErrors = new ArrayList<>();
    public ArrayList<SupportSettlement> supportSettlements = new ArrayList<>();
    public ArrayList<Hinge> hinges = new ArrayList<>();

    public ArrayList<FrameMember> subFrameMembers = new ArrayList<>();
    public ArrayList<Joint> subJoints = new ArrayList<>();
    public ArrayList<LinearlyDistributedLoad> subLinearlyDistributedLoads = new ArrayList<>();
    public ArrayList<Short> subFrameMembersStartingIndex = new ArrayList<>();

    public static final int precision = 15;
    public boolean isStable = true;

    public Frame (double MS) {
        this.MS = new BigDecimal(MS + "");
    }

    public Frame (BigDecimal MS) {
        this.MS = MS;
    }

    public void addCrossSection (CrossSection CS) {
        crossSections.add(CS);
    }

    public void addFrameMember (FrameMember FM) {
        mainFrameMembers.add(FM);
    }

    public void addSupport (Support S) {
        supports.add(S);
    }

    public void addHinge (Hinge H) {
        hinges.add(H);
    }

    public void addJointLoad (JointLoad JL) {
        jointLoads.add(JL);
    }

    public void addPointLoad (PointLoad PL) {
        pointLoads.add(PL);
    }

    public void addLinearlyDistributedLoad (LinearlyDistributedLoad LDL) {
        mainLinearlyDistributedLoads.add(LDL);
    }

    public void addTemperatureChange (TemperatureChange TC) {
        temperatureChanges.add(TC);
    }

    public void addFabricationError (FabricationError FE) {
        fabricationErrors.add(FE);
    }

    public void addSupportSettlement (SupportSettlement SS) {
        supportSettlements.add(SS);
    }

    private void setMainJoints () {
        ArrayList<BigDecimal> Cxs = new ArrayList<>();
        ArrayList<BigDecimal> Cys = new ArrayList<>();
        BigDecimal cx;
        BigDecimal cy;

        for (FrameMember fm : mainFrameMembers) {
            for (short i = 0; i < 2; i++) {
                if (i == 0) {
                    cx = fm.BCx;
                    cy = fm.BCy;
                }
                else {
                    cx = fm.ECx;
                    cy = fm.ECy;
                }

                boolean add = true;

                for (short j = 0; j < Cxs.size(); j++) {
                    if (Cxs.get(j).compareTo(cx) == 0 && Cys.get(j).compareTo(cy) == 0) {
                        add = false;
                        break;
                    }
                }

                if (add) {
                    short k = 0;
                    for (;k < Cxs.size(); k++) {
                        if ((cy.compareTo(Cys.get(k)) < 0) || (cy.compareTo(Cys.get(k)) == 0 && cx.compareTo(Cxs.get(k)) < 0)) {
                            break;
                        }
                    }
                    Cxs.add(k, cx);
                    Cys.add(k, cy);
                }
            }
        }

        for (short l = 0; l < Cxs.size(); l++) {
            Joint j = new Joint(Cxs.get(l), Cys.get(l));
            mainJoints.add(j);
        }
    }

    private void linkMainFrameMembersAndMainJoints () {
        for (short i = 0; i < mainJoints.size(); i++) {
            for (short j = 0; j < mainFrameMembers.size(); j++) {
                if (mainFrameMembers.get(j).BCx.compareTo(mainJoints.get(i).Cx) == 0 && mainFrameMembers.get(j).BCy.compareTo(mainJoints.get(i).Cy) == 0) {
                    mainFrameMembers.get(j).BJN = i;
                    mainJoints.get(i).MN.add(j);
                }
                if (mainFrameMembers.get(j).ECx.compareTo(mainJoints.get(i).Cx) == 0&& mainFrameMembers.get(j).ECy.compareTo(mainJoints.get(i).Cy) == 0) {
                    mainFrameMembers.get(j).EJN = i;
                    mainJoints.get(i).MN.add(j);
                }
            }
        }

        for (Joint j : mainJoints) {
            for (Support sp : supports) {
                if (sp.ST != 3) {
                    if (sp.Cx.compareTo(j.Cx) == 0 && sp.Cy.compareTo(j.Cy) == 0) {
                        j.setJoinType(sp.ST);
                        break;
                    }
                }
            }
        }
    }

    private void setSubJointsAndSubFrameMembers () {
        for (Joint j: mainJoints) {
            subJoints.add(new Joint(j.Cx, j.Cy));
            subJoints.get(subJoints.size() - 1).setJoinType(j.JT);
        }
        subFrameMembersStartingIndex.add((short) 0);

        for (FrameMember fm : mainFrameMembers) {
            ArrayList<Joint> intermediateJoints = new ArrayList<>();
            intermediateJoints.add(new Joint(fm.BCx, fm.BCy));
            intermediateJoints.add(new Joint(fm.ECx, fm.ECy));

            for (PointLoad pl : pointLoads) {
                BigDecimal Cx;
                BigDecimal Cy;
                if (pl.MN == mainFrameMembers.indexOf(fm)) {
                    if (pl.d.compareTo(fm.L) == 0) {
                        Cx = fm.ECx;
                        Cy = fm.ECy;
                    }
                    else {
                        Cx = fm.BCx.add(pl.d.multiply(fm.cos)).setScale(precision, RoundingMode.FLOOR).stripTrailingZeros();
                        Cy = fm.BCy.add(pl.d.multiply(fm.sin)).setScale(precision, RoundingMode.FLOOR).stripTrailingZeros();
                    }
                    if (pl.LT == 1) {
                        jointLoads.add(new JointLoad(Cx, Cy, pl.load[0].multiply(fm.cos).subtract(pl.load[1].multiply(fm.sin)), pl.load[0].multiply(fm.sin).add(pl.load[1].multiply(fm.cos)), pl.load[2]));
                    }
                    else {
                        jointLoads.add(new JointLoad(Cx, Cy, pl.load[0], pl.load[1], pl.load[2]));
                    }
                    boolean add = true;
                    for (Joint ij : intermediateJoints) {
                        if (ij.Cx.compareTo(Cx) == 0 && ij.Cy.compareTo(Cy) == 0) {
                            add = false;
                            break;
                        }
                    }
                    if (add) {
                        short i = 0;
                        for (; i < intermediateJoints.size(); i++) {
                            if (fm.cos.compareTo(BigDecimal.ZERO) == 0) {
                                if (Cy.compareTo(intermediateJoints.get(i).Cy) < 0) {
                                    break;
                                }
                            }
                            else {
                                if (Cx.divide(fm.cos, precision, RoundingMode.FLOOR).compareTo(intermediateJoints.get(i).Cx.divide(fm.cos, precision,RoundingMode.FLOOR)) < 0) {
                                    break;
                                }
                            }
                        }
                        intermediateJoints.add(i, new Joint(Cx, Cy));
                    }
                }
            }

            ArrayList<LinearlyDistributedLoad> splitLinearlyDistributedLoads = new ArrayList<>();
            for (LinearlyDistributedLoad ldl : mainLinearlyDistributedLoads) {
                if (ldl.load[0].multiply(ldl.load[1]).compareTo(BigDecimal.ZERO) < 0) {
                    BigDecimal fmL = mainFrameMembers.get(ldl.MN).L;
                    BigDecimal d = ldl.load[0].multiply(fmL.subtract(ldl.load[2]).subtract((ldl.load[3])))
                            .divide(ldl.load[0].subtract(ldl.load[1]), 2, RoundingMode.FLOOR);

                    splitLinearlyDistributedLoads.add(new LinearlyDistributedLoad(ldl.MN, ldl.LT, ldl.load[0], BigDecimal.ZERO, ldl.load[2], fmL.subtract(d.add(ldl.load[2]))));
                    splitLinearlyDistributedLoads.add(new LinearlyDistributedLoad(ldl.MN, ldl.LT, BigDecimal.ZERO, ldl.load[1], ldl.load[2].add(d), ldl.load[3]));
                }
            }
            mainLinearlyDistributedLoads.removeIf(ldl -> ldl.load[0].multiply(ldl.load[1]).compareTo(BigDecimal.ZERO) < 0);
            mainLinearlyDistributedLoads.addAll(splitLinearlyDistributedLoads);

            for (LinearlyDistributedLoad ldl : mainLinearlyDistributedLoads) {
                BigDecimal Cx;
                BigDecimal Cy;
                if (ldl.MN == mainFrameMembers.indexOf(fm)) {
                    for (short i = 0; i < 2; i++) {
                        if (i == 0) {
                            Cx = fm.BCx.add(ldl.load[2].multiply(fm.cos)).setScale(precision, RoundingMode.FLOOR).stripTrailingZeros();
                            Cy = fm.BCy.add(ldl.load[2].multiply(fm.sin)).setScale(precision, RoundingMode.FLOOR).stripTrailingZeros();
                        }
                        else {
                            if (ldl.load[3].compareTo(BigDecimal.ZERO) == 0) {
                                Cx = fm.ECx;
                                Cy = fm.ECy;
                            }
                            else {
                                Cx = fm.BCx.add(fm.L.subtract(ldl.load[3]).multiply(fm.cos)).setScale(precision, RoundingMode.FLOOR).stripTrailingZeros();
                                Cy = fm.BCy.add(fm.L.subtract(ldl.load[3]).multiply(fm.sin)).setScale(precision, RoundingMode.FLOOR).stripTrailingZeros();
                            }
                        }
                        boolean add = true;
                        for (Joint ij : intermediateJoints) {
                            if (ij.Cx.compareTo(Cx) == 0 && ij.Cy.compareTo(Cy) == 0) {
                                add = false;
                                break;
                            }
                        }
                        if (add) {
                            short j = 0;
                            for (; j < intermediateJoints.size(); j++) {
                                if (Cx.divide(fm.cos, precision, RoundingMode.FLOOR).compareTo(intermediateJoints.get(j).Cx.divide(fm.cos, precision,RoundingMode.FLOOR)) < 0) {
                                    break;
                                }
                            }
                            intermediateJoints.add(j, new Joint(Cx, Cy));
                        }
                    }
                }
            }

            short count = subFrameMembersStartingIndex.get(subFrameMembersStartingIndex.size() - 1);

            for (short i = 1; i < intermediateJoints.size(); i++) {
                BigDecimal L;
                if (fm.cos.compareTo(BigDecimal.ZERO) == 0) {
                    L = intermediateJoints.get(i).Cy.subtract(intermediateJoints.get(i - 1).Cy).abs()
                            .setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
                }
                else {
                    L = (intermediateJoints.get(i).Cx.subtract(intermediateJoints.get(i - 1).Cx)).divide(fm.cos, precision,RoundingMode.CEILING).abs()
                            .setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
                }
                short PS = L.divide(MS,0,RoundingMode.CEILING).shortValueExact();
                short PS2 = L.divide(MS,0,RoundingMode.FLOOR).shortValueExact();

                BigDecimal PS3 = L.divide(MS, 0, RoundingMode.FLOOR);
                BigDecimal PS4 = L.divide(MS, precision, RoundingMode.CEILING);
                boolean isTooClose = isWithinTolerance(PS3, PS4, new BigDecimal("0.1"));
                if (PS != PS2 && isTooClose && PS != 1) {
                    PS--;
                }

                count += PS;

                BigDecimal BCx = intermediateJoints.get(i - 1).Cx;
                BigDecimal BCy = intermediateJoints.get(i - 1).Cy;
                BigDecimal ECx;
                BigDecimal ECy;

                if (PS != PS2) {
                    ECx = BCx.add(BigDecimal.valueOf(PS - 1).multiply(MS).multiply(fm.cos));
                    ECy = BCy.add(BigDecimal.valueOf(PS - 1).multiply(MS).multiply(fm.sin));

                    for (short j = 0; j < PS - 1; j++) {
                        BigDecimal iBCx = j == 0 ? BCx : subFrameMembers.get(subFrameMembers.size() - 1).ECx;
                        BigDecimal iBCy = j == 0 ? BCy : subFrameMembers.get(subFrameMembers.size() - 1).ECy;
                        BigDecimal iECx = BCx.add(ECx.subtract(BCx).multiply(BigDecimal.valueOf(j + 1)).divide(BigDecimal.valueOf(PS - 1), precision,RoundingMode.FLOOR))
                                .setScale(precision, RoundingMode.FLOOR).stripTrailingZeros();
                        BigDecimal iECy = BCy.add(ECy.subtract(BCy).multiply(BigDecimal.valueOf(j + 1)).divide(BigDecimal.valueOf(PS - 1), precision,RoundingMode.FLOOR))
                                .setScale(precision, RoundingMode.FLOOR).stripTrailingZeros();

                        FrameMember sfm = new FrameMember(iBCx, iBCy, iECx, iECy, new CrossSection(fm.A, fm.E, fm.I));
                        if (j == 0 && i == 1) {
                            sfm.BJN = fm.BJN;
                            subJoints.get(fm.BJN).MN.add((short) subFrameMembers.size()); /////
                        }
                        else {
                            sfm.BJN = (short) (subJoints.size() - 1);
                        }
                        sfm.EJN = (short) subJoints.size();
                        Joint sj = new Joint(iECx, iECy);
                        sj.setJoinType((short) 0);
                        sj.MN.add((short) subFrameMembers.size());
                        sj.MN.add((short) (subFrameMembers.size() + 1));
                        subJoints.add(sj);
                        subFrameMembers.add(sfm);
                    }
                    FrameMember sfm = new FrameMember(ECx, ECy, intermediateJoints.get(i).Cx, intermediateJoints.get(i).Cy, new CrossSection(fm.A, fm.E, fm.I));
                    if (PS == 1 && i == 1) {
                        sfm.BJN = fm.BJN;
                        subJoints.get(fm.BJN).MN.add((short) subFrameMembers.size()); /////
                    }
                    else {
                        sfm.BJN = (short) (subJoints.size() - 1);
                    }
                    if (i == intermediateJoints.size() - 1) {
                        sfm.EJN = fm.EJN;
                        subJoints.get(fm.EJN).MN.add((short) subFrameMembers.size()); /////
                    }
                    else {
                        sfm.EJN = (short) subJoints.size();
                        Joint sj = new Joint(intermediateJoints.get(i).Cx, intermediateJoints.get(i).Cy);
                        sj.setJoinType((short) 0);
                        sj.MN.add((short) subFrameMembers.size());
                        sj.MN.add((short) (subFrameMembers.size() + 1));
                        subJoints.add(sj);
                    }
                    subFrameMembers.add(sfm);
                }
                else {
                    ECx = intermediateJoints.get(i).Cx;
                    ECy = intermediateJoints.get(i).Cy;

                    for (short j = 0; j < PS; j++) {
                        BigDecimal iBCx = j == 0 ? BCx : subFrameMembers.get(subFrameMembers.size() - 1).ECx;
                        BigDecimal iBCy = j == 0 ? BCy : subFrameMembers.get(subFrameMembers.size() - 1).ECy;
                        BigDecimal iECx = BCx.add(ECx.subtract(BCx).multiply(BigDecimal.valueOf(j + 1)).divide(BigDecimal.valueOf(PS), precision,RoundingMode.FLOOR))
                                .setScale(precision, RoundingMode.FLOOR).stripTrailingZeros();
                        BigDecimal iECy = BCy.add(ECy.subtract(BCy).multiply(BigDecimal.valueOf(j + 1)).divide(BigDecimal.valueOf(PS), precision,RoundingMode.FLOOR))
                                .setScale(precision, RoundingMode.FLOOR).stripTrailingZeros();

                        FrameMember sfm = new FrameMember(iBCx, iBCy, iECx, iECy, new CrossSection(fm.A, fm.E, fm.I));
                        if (j == 0 && i == 1) {
                            sfm.BJN = fm.BJN;
                            subJoints.get(fm.BJN).MN.add((short) subFrameMembers.size()); /////
                        }
                        else {
                            sfm.BJN = (short) (subJoints.size() - 1);
                        }
                        if (j == PS - 1 && i == intermediateJoints.size() - 1) {
                            sfm.EJN = fm.EJN;
                            subJoints.get(fm.EJN).MN.add((short) subFrameMembers.size()); /////
                        }
                        else {
                            sfm.EJN = (short) subJoints.size();
                            Joint sj = new Joint(iECx, iECy);
                            sj.setJoinType((short) 0);
                            sj.MN.add((short) subFrameMembers.size());
                            sj.MN.add((short) (subFrameMembers.size() + 1));
                            subJoints.add(sj);
                        }
                        subFrameMembers.add(sfm);
                    }
                }
            }
            subFrameMembersStartingIndex.add(count);
        }
    }

    private void setInclineRollers () {
        for (Support sp : supports) {
            if (sp.ST == 3) {
                CrossSection cs = new CrossSection(new BigDecimal("10000000"), new BigDecimal("1"), new BigDecimal("1"));
                BigDecimal cos = new BigDecimal(Math.cos(Math.toRadians(sp.angle.doubleValue() - 90)) + "");
                BigDecimal sin = new BigDecimal(Math.sin(Math.toRadians(sp.angle.doubleValue() - 90)) + "");
                FrameMember sfm = new FrameMember(sp.Cx, sp.Cy, cs, cos, sin);

                for (Joint sj : subJoints) {
                    if (sj.Cx.compareTo(sp.Cx) == 0 && sj.Cy.compareTo(sp.Cy) == 0) {
                        sj.MN.add((short) subFrameMembers.size());
                        sfm.BJN = (short) subJoints.indexOf(sj);
                        break;
                    }
                }

                Joint j = new Joint(sp.Cx, sp.Cy);
                j.setJoinType((short) 3);
                j.MN.add((short) subFrameMembers.size());
                subJoints.add(j);
                sfm.EJN = (short) subJoints.indexOf(j);

                sfm.BJFR = new boolean[] {false, false, true};
                sfm.EJFR = new boolean[] {false, false, true};

                subFrameMembers.add(sfm);
            }
        }
    }

    private void calNumberDegreeOfFreedoms () {
        for (Joint sj : subJoints) {
            for (short i = 0; i < 3; i++) {
                if (sj.DR[i]) {
                    NR ++;
                }
                else {
                    NDF ++;
                }
            }
        }
    }

    private void setSubJointOrder() {
        short index1 = 0;
        short index2 = NDF;
        for (Joint subJoint : subJoints) {
            for (short i = 0; i < 3; i++) {
                if (!subJoint.DR[i]) {
                    subJoint.order[i] = index1;
                    index1++;
                }
                else {
                    subJoint.order[i] = index2;
                    index2++;
                }
            }
        }
    }

    private void convertMainLinearlyDistributedLoadsToSubLinearlyDistributedLoads () {
        for (LinearlyDistributedLoad ldl : mainLinearlyDistributedLoads) {
            short BMN = -1; //begin member number
            short EMN = -1; //end member number
            FrameMember fm = mainFrameMembers.get(ldl.MN);

            for (short i = subFrameMembersStartingIndex.get(ldl.MN); i < subFrameMembersStartingIndex.get(ldl.MN + 1); i++) {

                BigDecimal BCx = fm.BCx.add(ldl.load[2].multiply(fm.cos)).setScale(precision, RoundingMode.FLOOR).stripTrailingZeros();
                BigDecimal BCy = fm.BCy.add(ldl.load[2].multiply(fm.sin)).setScale(precision, RoundingMode.FLOOR).stripTrailingZeros();
                BigDecimal ECx = ldl.load[3].compareTo(BigDecimal.ZERO) == 0 ? fm.ECx : fm.BCx.add(fm.L.subtract(ldl.load[3]).multiply(fm.cos)).setScale(precision, RoundingMode.FLOOR).stripTrailingZeros();
                BigDecimal ECy = ldl.load[3].compareTo(BigDecimal.ZERO) == 0 ? fm.ECy : fm.BCy.add(fm.L.subtract(ldl.load[3]).multiply(fm.sin)).setScale(precision, RoundingMode.FLOOR).stripTrailingZeros();


                if (BCx.compareTo(subFrameMembers.get(i).BCx) == 0 && BCy.compareTo(subFrameMembers.get(i).BCy) == 0) {
                    BMN = i;
                }
                if (ECx.compareTo(subFrameMembers.get(i).ECx) == 0 && ECy.compareTo(subFrameMembers.get(i).ECy) == 0) {
                    EMN = i;
                    break;
                }
            }
            BigDecimal s = ldl.load[1].subtract(ldl.load[0]).divide(fm.L.subtract(ldl.load[2]).subtract(ldl.load[3]), precision,RoundingMode.FLOOR)
                    .setScale(precision, RoundingMode.FLOOR).stripTrailingZeros(); //slope
            BigDecimal x = new BigDecimal("0"); //distant covered

            for (short j = BMN; j <= EMN; j++) {
                BigDecimal W1 = ldl.load[0].add(s.multiply(x));
                BigDecimal W2;
                if (j == EMN) {
                    W2 = ldl.load[1];
                }
                else {
                    W2 = W1.add(s.multiply(subFrameMembers.get(j).L_15));
                }

                LinearlyDistributedLoad sml = new LinearlyDistributedLoad(j, ldl.LT, W1, W2, null, null);
                subLinearlyDistributedLoads.add(sml);
                x = x.add(subFrameMembers.get(j).L_15);
            }
        }
    }

    private void calFixEndLoadVector () {
        convertMainLinearlyDistributedLoadsToSubLinearlyDistributedLoads();
        for (LinearlyDistributedLoad sml : subLinearlyDistributedLoads) {
            subFrameMembers.get(sml.MN).calFixEndForceInducedByLinearlyDistributedLoad(sml.LT, sml.load[0], sml.load[1]);
        }
        for (TemperatureChange tp : temperatureChanges) {
            for (short i = subFrameMembersStartingIndex.get(tp.MN); i < subFrameMembersStartingIndex.get(tp.MN + 1); i++) {
                subFrameMembers.get(i).calFixEndForceInducedByTemperatureChange(tp.T, tp.alpha);
            }
        }
        for (FabricationError fe : fabricationErrors) {
            short i = subFrameMembersStartingIndex.get(fe.MN);
            subFrameMembers.get(i).calFixEndForceInducedByFabricationError(fe.e);
        }

        for (SupportSettlement ss : supportSettlements) {
            for (Joint j : subJoints) {
                if (ss.Cx.compareTo(j.Cx) == 0 && ss.Cy.compareTo(j.Cy) == 0) {
                    for (short index : j.MN) {
                        Joint BJ = subJoints.get(subFrameMembers.get(index).BJN);
                        Joint EJ = subJoints.get(subFrameMembers.get(index).EJN);
                        Pf.insert(Pf.add(subFrameMembers.get(index).calFixEndForceInducedBySupportSettlement(ss, BJ, EJ, NDF)));
                    }
                }
            }
        }

        for (FrameMember sfm: subFrameMembers) {
            sfm.calFixedEndForceAfterMemberRelease();
            sfm.calGlobalFixEndForce();

            for (short j = 0; j < 3; j++) {
                if (subJoints.get(sfm.BJN).order[j] < NDF) {
                    Pf.data[subJoints.get(sfm.BJN).order[j]][0] = Pf.data[subJoints.get(sfm.BJN).order[j]][0].add(sfm.Ff.data[j][0]);
                }
                if (subJoints.get(sfm.EJN).order[j] < NDF) {
                    Pf.data[subJoints.get(sfm.EJN).order[j]][0] = Pf.data[subJoints.get(sfm.EJN).order[j]][0].add(sfm.Ff.data[j + 3][0]);
                }
            }
        }
    }

    private void calJointLoadVector () {
        for (JointLoad jl : jointLoads) {
            for (Joint j : subJoints) {
                if (j.Cx.compareTo(jl.Cx) == 0 && j.Cy.compareTo(jl.Cy) == 0) {
                    for (short i = 0; i < 3; i++) {
                        if (j.order[i] < NDF) {
                            P.data[j.order[i]][0] = P.data[j.order[i]][0].add(jl.load[i]);
                        }
                    }
                }
            }
        }
    }

    private void calSubFrameMembersStiffness () {
        for (Hinge h : hinges) {
            for (int index : h.MN) {
                FrameMember fm = mainFrameMembers.get(index);
                if (h.Cx.compareTo(fm.BCx) == 0 && h.Cy.compareTo(fm.BCy) == 0) {
                    subFrameMembers.get(subFrameMembersStartingIndex.get(index)).BJFR[0] = false;
                    subFrameMembers.get(subFrameMembersStartingIndex.get(index)).BJFR[1] = false;
                    subFrameMembers.get(subFrameMembersStartingIndex.get(index)).BJFR[2] = true;
                }
                else if (h.Cx.compareTo(fm.ECx) == 0 && h.Cy.compareTo(fm.ECy) == 0) {
                    subFrameMembers.get(subFrameMembersStartingIndex.get(index + 1) - 1).EJFR[0] = false;
                    subFrameMembers.get(subFrameMembersStartingIndex.get(index + 1) - 1).EJFR[1] = false;
                    subFrameMembers.get(subFrameMembersStartingIndex.get(index + 1) - 1).EJFR[2] = true;
                }
            }
        }
        for (FrameMember sfm : subFrameMembers) {
            sfm.calStiffness();
            sfm.calGlobalStiffness();
        }
    }

    private void calStructuralStiffness () {
        for (FrameMember sfm : subFrameMembers) {
            short[] o = {
                    subJoints.get(sfm.BJN).order[0],
                    subJoints.get(sfm.BJN).order[1],
                    subJoints.get(sfm.BJN).order[2],
                    subJoints.get(sfm.EJN).order[0],
                    subJoints.get(sfm.EJN).order[1],
                    subJoints.get(sfm.EJN).order[2]
            };
            for (short i = 0; i < 6; i++) {
                for (short j = 0; j < 6; j++) {
                    if (o[i] < NDF && o[j] < NDF) {
                        S.data[o[i]][o[j]] = S.data[o[i]][o[j]].add(sfm.K.data[i][j]);
                    }
                }
            }
        }
    }

    private void calJointDisplacementVector () {
        d.insert(S.appendCol(P.subtract(Pf)).eliminateGaussJordan().getCol(NDF));
    }

    public boolean isWithinTolerance(BigDecimal BD1, BigDecimal BD2, BigDecimal tolerance) {
        final BigDecimal diff = BD1.abs().subtract(BD2.abs()).abs();
        return diff.compareTo(tolerance) <= 0;
    }

    private void checkIfStable () {
        Matrix O = S.multiply(d).subtract(P.subtract(Pf));
        for (int i = 0; i < O.row; i ++) {
            if (!isWithinTolerance(O.data[i][0], BigDecimal.ZERO, new BigDecimal("0.1"))) {
                isStable = false;
                break;
            }
        }
    }

    private void calFrameMembersEndDisplacements () {
        for (FrameMember sfm: subFrameMembers) {
            for (short j = 0; j < 3; j++) {
                if (subJoints.get(sfm.BJN).order[j] < NDF) {
                    sfm.v.data[j][0] = sfm.v.data[j][0].add(d.data[subJoints.get(sfm.BJN).order[j]][0]);
                }
                if (subJoints.get(sfm.EJN).order[j] < NDF) {
                    sfm.v.data[j + 3][0] = sfm.v.data[j + 3][0].add(d.data[subJoints.get(sfm.EJN).order[j]][0]);
                }
            }
            sfm.u.insert(sfm.T.multiply(sfm.v));
        }
    }

    private void calFrameMembersEndForces () {
        for (FrameMember sfm: subFrameMembers) {
            sfm.Q.insert(sfm.k.multiply(sfm.u).add(sfm.Qf));
            sfm.F.insert(sfm.T.transpose().multiply(sfm.Q));
        }
    }

    private void setReactionTypeAndCoordinate () {
        for (Joint j : subJoints) {
            for (int i = 0; i < 3; i++) {
                if (j.order[i] >= NDF) {
                    R.data[j.order[i]- NDF][0] = j.Cx;
                    R.data[j.order[i]- NDF][1] = j.Cy;
                    R.data[j.order[i]- NDF][2] = BigDecimal.valueOf(i + 1);
                }
            }
        }
    }

    private void calReactionForceVector () {
        setReactionTypeAndCoordinate();

        for (FrameMember sfm: subFrameMembers) {
            for (short i = 0; i < 3; i++) {
                if (subJoints.get(sfm.BJN).order[i] >= NDF) {
                    R.data[subJoints.get(sfm.BJN).order[i] - NDF][3] = R.data[subJoints.get(sfm.BJN).order[i] - NDF][3].add(sfm.F.data[i][0]);
                }
                if (subJoints.get(sfm.EJN).order[i] >= NDF) {
                    R.data[subJoints.get(sfm.EJN).order[i] - NDF][3] = R.data[subJoints.get(sfm.EJN).order[i] - NDF][3].add(sfm.F.data[i + 3][0]);
                }
            }
        }

        for (JointLoad jl : jointLoads) {
            for (Joint j : subJoints) {
                if (j.Cx.compareTo(jl.Cx) == 0 && j.Cy.compareTo(jl.Cy) == 0) {
                    for (short i = 0; i < 3; i++) {
                        if (j.order[i] >= NDF) {
                            R.data[j.order[i] - NDF][3] = R.data[j.order[i] - NDF][3].add(jl.load[i].negate());
                        }
                    }
                }
            }
        }
    }

    public void solve () {
        setMainJoints();
        linkMainFrameMembersAndMainJoints();
        setSubJointsAndSubFrameMembers();
        setInclineRollers();

        calNumberDegreeOfFreedoms();

        S = new Matrix(NDF,NDF);
        P = new Matrix(NDF,(short) 1);
        Pf = new Matrix(NDF,(short) 1);
        d = new Matrix(NDF,(short) 1);
        R = new Matrix(NR, (short) 4);

        setSubJointOrder();

        calSubFrameMembersStiffness();
        calStructuralStiffness();
        calJointLoadVector();
        calFixEndLoadVector();

        calJointDisplacementVector();
        checkIfStable();
        calFrameMembersEndDisplacements();
        calFrameMembersEndForces();
        calReactionForceVector();
    }

}
