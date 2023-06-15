package com.example.application.tools;

import com.example.application.backends.*;
import com.vaadin.flow.component.textfield.BigDecimalField;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class InputFrame {
    public BigDecimal min_MS;
    public BigDecimal MS;
    public BigDecimal PH;
    public BigDecimal PW;

    public ArrayList<CrossSection> crossSections = new ArrayList<>();
    public ArrayList<FrameMember> frameMembers = new ArrayList<>();
    public ArrayList<Support> supports = new ArrayList<>();
    public ArrayList<Hinge> hinges = new ArrayList<>();

    public ArrayList<PointLoad> pointLoads = new ArrayList<>();
    public ArrayList<JointLoad> jointLoads = new ArrayList<>();
    public ArrayList<LinearlyDistributedLoad> linearlyDistributedLoads = new ArrayList<>();

    public ArrayList<TemperatureChange> temperatureChanges = new ArrayList<>();
    public ArrayList<FabricationError> fabricationErrors = new ArrayList<>();
    public ArrayList<SupportSettlement> supportSettlements = new ArrayList<>();

    public ArrayList<Joint> joints = new ArrayList<>();

    public ArrayList<Integer> markedFrameMembers = new ArrayList<>();
    public ArrayList<Integer> markedJoints = new ArrayList<>();
    public ArrayList<Integer> markedSupports = new ArrayList<>();
    public ArrayList<Integer> markedHinges = new ArrayList<>();
    public ArrayList<Integer> markedJointLoads = new ArrayList<>();
    public ArrayList<Integer> markedPointLoads = new ArrayList<>();
    public ArrayList<Integer> markedLinearlyDistributedLoads = new ArrayList<>();
    public ArrayList<Integer> markedTemperatureChanges = new ArrayList<>();
    public ArrayList<Integer> markedFabricationErrors = new ArrayList<>();
    public ArrayList<Integer> markedSupportsSettlements = new ArrayList<>();

    public ArrayList<Integer> undisplayedFrameMembers = new ArrayList<>();

    public boolean showFrameMember;
    public boolean showSupport;
    public boolean showLoad;
    public boolean showExternalEffect;

    public boolean isDefaultCS;
    public static final CrossSection defaultCrossSection = new CrossSection(new BigDecimal("0.01"), new BigDecimal("300000000"), new BigDecimal("0.01"));

    public static final int crossSectionMaxItem = 30;
    public static final int frameMemberMaxItem = 30;
    public static final int supportMaxItem = 30;
    public static final int hingeMaxItem = 30;
    public static final int jointLoadMaxItem = 30;
    public static final int pointLoadMaxItem = 30;
    public static final int linearlyDistributedLoadMaxItem = 30;
    public static final int temperatureChangeMaxItem = 30;
    public static final int fabricationErrorMaxItem = 30;
    public static final int supportSettlementMaxItem = 30;

    public InputFrame () {
        min_MS = new BigDecimal("0.1");
        MS = new BigDecimal("1");
        PH = new BigDecimal("10");
        PW = new BigDecimal("10");
        showAll();
        this.isDefaultCS = true;
    }

    public void clearAll () {
        crossSections.clear();
        frameMembers.clear();
        supports.clear();
        hinges.clear();
        pointLoads.clear();
        jointLoads.clear();
        linearlyDistributedLoads.clear();
        temperatureChanges.clear();
        fabricationErrors.clear();
        supportSettlements.clear();
        joints.clear();
        unmarkedAll();
        showAll();
    }

    public void showAll () {
        showFrameMember = true;
        showSupport = true;
        showLoad = true;
        showExternalEffect = true;
        undisplayedFrameMembers.clear();
    }

    public void hideAll() {
        showFrameMember = false;
        showSupport = false;
        showLoad = false;
        showExternalEffect = false;
    }

    public void unmarkedAll () {
        markedFrameMembers.clear();
        markedJoints.clear();
        markedSupports.clear();
        markedHinges.clear();
        markedJointLoads.clear();
        markedPointLoads.clear();
        markedLinearlyDistributedLoads.clear();
        markedTemperatureChanges.clear();
        markedFabricationErrors.clear();
        markedSupportsSettlements.clear();
    }

    public void removeOutOfBoundFrameMember () {
        for (FrameMember fm : frameMembers) {
            if (fm.BCx.compareTo(PW) > 0 || fm.ECx.compareTo(PW) > 0 || fm.BCy.compareTo(PH) > 0 || fm.ECy.compareTo(PH) > 0) {
                removeComponentAttachToFrameMember(fm);
            }
        }
        frameMembers.removeIf(fm -> fm.BCx.compareTo(PW) > 0 || fm.ECx.compareTo(PW) > 0 || fm.BCy.compareTo(PH) > 0 || fm.ECy.compareTo(PH) > 0);
        setIDAfterRemoveComponentAttachToFrameMember();
        removeComponentAttachToJoint();
    }

    public void addCrossSection (BigDecimal A, BigDecimal E, BigDecimal I) {
        crossSections.add(new CrossSection(A, E, I));
    }

    public void addCrossSection (int index, BigDecimal A, BigDecimal E, BigDecimal I) {
        crossSections.add(index, new CrossSection(A, E, I));
    }

    public void removeCrossSection (CrossSection CS) {
        for (FrameMember fm : frameMembers) {
            if (fm.A.compareTo(CS.A) == 0 && fm.E.compareTo(CS.E) == 0 && fm.I.compareTo(CS.I) == 0) {
                removeComponentAttachToFrameMember(fm);
            }
        }
        frameMembers.removeIf(fm -> fm.A.compareTo(CS.A) == 0 && fm.E.compareTo(CS.E) == 0 && fm.I.compareTo(CS.I) == 0);
        crossSections.remove(CS);
        setIDAfterRemoveComponentAttachToFrameMember();
        removeComponentAttachToJoint();
    }

    public void addFrameMember (BigDecimal BCx, BigDecimal BCy, BigDecimal ECx, BigDecimal ECy, int CSN) {
        frameMembers.add(new FrameMember(BCx, BCy, ECx, ECy, crossSections.get(CSN)));
        setJoints();
    }

    public void addFrameMember (BigDecimal BCx, BigDecimal BCy, BigDecimal ECx, BigDecimal ECy, CrossSection CS) {
        frameMembers.add(new FrameMember(BCx, BCy, ECx, ECy, CS));
        setJoints();
    }

    public void addFrameMember (int index, BigDecimal BCx, BigDecimal BCy, BigDecimal ECx, BigDecimal ECy, int CSN) {
        frameMembers.add(index, new FrameMember(BCx, BCy, ECx, ECy, crossSections.get(CSN)));
        setJoints();
    }

    public void addFrameMember (int index, BigDecimal BCx, BigDecimal BCy, BigDecimal ECx, BigDecimal ECy, CrossSection CS) {
        frameMembers.add(index, new FrameMember(BCx, BCy, ECx, ECy, CS));
        setJoints();
    }

    public void removeFrameMember (FrameMember FM) {
        removeComponentAttachToFrameMember(FM);
        frameMembers.remove(FM);
        setIDAfterRemoveComponentAttachToFrameMember();
        removeComponentAttachToJoint();
    }

    public void addSupport (BigDecimal Cx, BigDecimal Cy, int ST) {
        supports.add(new Support(Cx, Cy, (short) ST));
    }

    public void addSupport (int index, BigDecimal Cx, BigDecimal Cy, int ST) {
        supports.add(index, new Support(Cx, Cy, (short) ST));
    }

    public void removeSupport (Support S) {
        supportSettlements.removeIf(ss -> (ss.Cx.compareTo(S.Cx) == 0 && ss.Cy.compareTo(S.Cy) == 0));
        supports.remove(S);
    }

    public void addHinge (BigDecimal Cx, BigDecimal Cy) {
        hinges.add(new Hinge(Cx, Cy));
    }

    public void addHinge (int index, BigDecimal Cx, BigDecimal Cy) {
        hinges.add(index, new Hinge(Cx, Cy));
    }

    public void removeHinge (Hinge H) {
        hinges.remove(H);
    }

    public void addJointLoad (BigDecimal Cx, BigDecimal Cy, BigDecimal Fx, BigDecimal Fy, BigDecimal M) {
        jointLoads.add(new JointLoad(Cx, Cy, Fx, Fy, M));
    }

    public void addJointLoad (int index, BigDecimal Cx, BigDecimal Cy, BigDecimal Fx, BigDecimal Fy, BigDecimal M) {
        jointLoads.add(index, new JointLoad(Cx, Cy, Fx, Fy, M));
    }

    public void removeJointLoad (JointLoad JL) {
        jointLoads.remove(JL);
    }

    public void addPointLoad (int MN, int LT, BigDecimal d, BigDecimal Fx, BigDecimal Fy, BigDecimal M) {
        pointLoads.add(new PointLoad((short) MN, (short) LT, d, Fx, Fy, M));
    }

    public void addPointLoad (int index, int MN, int LT, BigDecimal d, BigDecimal Fx, BigDecimal Fy, BigDecimal M) {
        pointLoads.add(index, new PointLoad((short) MN, (short) LT, d, Fx, Fy, M));
    }

    public void removePointLoad (PointLoad PL) {
        pointLoads.remove(PL);
    }

    public void addLinearlyDistributedLoad (int MN, int LT, BigDecimal W1, BigDecimal W2, BigDecimal a, BigDecimal b) {
        linearlyDistributedLoads.add(new LinearlyDistributedLoad((short) MN, (short) LT, W1, W2, a, b));
    }

    public void addLinearlyDistributedLoad (int index, int MN, int LT, BigDecimal W1, BigDecimal W2, BigDecimal a, BigDecimal b) {
        linearlyDistributedLoads.add(index, new LinearlyDistributedLoad((short) MN, (short) LT, W1, W2, a, b));
    }

    public void removeLinearlyDistributedLoad (LinearlyDistributedLoad LDL) {
        linearlyDistributedLoads.remove(LDL);
    }

    public void addTemperatureChange(int MN, BigDecimal T, BigDecimal alpha) {
        temperatureChanges.add(new TemperatureChange((short) MN, T, alpha));
    }

    public void addTemperatureChange(int index,  int MN, BigDecimal T, BigDecimal alpha) {
        temperatureChanges.add(index, new TemperatureChange((short) MN, T, alpha));
    }

    public void removeTemperatureChange (TemperatureChange TC) {
        temperatureChanges.remove(TC);
    }

    public void addFabricationError (int MN, BigDecimal e) {
        fabricationErrors.add(new FabricationError((short) MN, e));
    }

    public void addFabricationError (int index, int MN, BigDecimal e) {
        fabricationErrors.add(index, new FabricationError((short) MN, e));
    }

    public void removeFabricationError (FabricationError FE) {
        fabricationErrors.remove(FE);
    }

    public void addSupportSettlement (BigDecimal Cx, BigDecimal Cy, BigDecimal Sx, BigDecimal Sy, BigDecimal Sz) {
        supportSettlements.add(new SupportSettlement(Cx, Cy, Sx, Sy, Sz));
    }

    public void addSupportSettlement (int index, BigDecimal Cx, BigDecimal Cy, BigDecimal Sx, BigDecimal Sy, BigDecimal Sz) {
        supportSettlements.add(index, new SupportSettlement(Cx, Cy, Sx, Sy, Sz));
    }

    public void removeSupportSettlement (SupportSettlement SS) {
        supportSettlements.remove(SS);
    }

    public void setJoints() {
        joints.clear();

        ArrayList<BigDecimal> Cxs = new ArrayList<>();
        ArrayList<BigDecimal> Cys = new ArrayList<>();
        BigDecimal cx;
        BigDecimal cy;

        for (FrameMember fm : frameMembers) {

            if (undisplayedFrameMembers.contains(fm.ID - 1)) {
                continue;
            }

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
            joints.add(j);
        }
    }

    private void removeComponentAttachToFrameMember (FrameMember fm) {

        for (Hinge h : hinges) {
            h.MN.removeIf(mn -> (mn + 1) == fm.ID);
        }
        hinges.removeIf(h -> h.MN.isEmpty());

        pointLoads.removeIf(pl -> (pl.MN + 1) == fm.ID);
        linearlyDistributedLoads.removeIf(ldl -> (ldl.MN + 1) == fm.ID);
        temperatureChanges.removeIf(tc -> (tc.MN + 1) == fm.ID);
        fabricationErrors.removeIf(fe -> (fe.MN + 1) == fm.ID);
    }

    private void setIDAfterRemoveComponentAttachToFrameMember () {
        for (FrameMember fm : frameMembers) {
            if (frameMembers.indexOf(fm) + 1 != fm.ID) {
                for (PointLoad pl : pointLoads) {
                    if (pl.MN + 1 == fm.ID) {
                        pl.MN = (short) frameMembers.indexOf(fm);
                    }
                }
                for (LinearlyDistributedLoad ldl : linearlyDistributedLoads) {
                    if (ldl.MN + 1 == fm.ID) {
                        ldl.MN = (short) frameMembers.indexOf(fm);
                    }
                }
                for (TemperatureChange tc : temperatureChanges) {
                    if (tc.MN + 1 == fm.ID) {
                        tc.MN = (short) frameMembers.indexOf(fm);
                    }
                }
                for (FabricationError fe : fabricationErrors) {
                    if (fe.MN + 1 == fm.ID) {
                        fe.MN = (short) frameMembers.indexOf(fm);
                    }
                }
                fm.ID = frameMembers.indexOf(fm) + 1;
            }
        }
    }

    private void removeComponentAttachToJoint () {
        setJoints();

        ArrayList<Support> supportsToRemove = new ArrayList<>();
        for (Support s : supports) {
            boolean remove = true;
            for (Joint j : joints) {
                if (s.Cx.compareTo(j.Cx) == 0 && s.Cy.compareTo(j.Cy) == 0) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                supportsToRemove.add(s);
            }
        }

        for (Support s : supportsToRemove) {
            removeSupport(s);
        }

        ArrayList<JointLoad> jointLoadsToRemove = new ArrayList<>();
        for (JointLoad jl : jointLoads) {
            boolean remove = true;
            for (Joint j : joints) {
                if (jl.Cx.compareTo(j.Cx) == 0 && jl.Cy.compareTo(j.Cy) == 0) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                jointLoadsToRemove.add(jl);
            }
        }

        for (JointLoad jl : jointLoadsToRemove) {
            removeJointLoad(jl);
        }
    }

    public static void trim (BigDecimalField bdf, int digit) {
        bdf.addValueChangeListener(event -> {
            if (bdf.getValue() != null ) {
                bdf.setValue(bdf.getValue().setScale(digit, RoundingMode.FLOOR).stripTrailingZeros());
                if (bdf.getValue().compareTo(BigDecimal.ZERO) == 0) {
                    bdf.clear();
                }
            }
        });
    }

    public static void trimMin (BigDecimalField bdf, int MinValue, int digit) {
        bdf.addValueChangeListener(event -> {
            if (bdf.getValue() != null) {
                if (bdf.getValue().compareTo(new BigDecimal(MinValue + "")) < 0) {
                    bdf.setValue(new BigDecimal(MinValue + ""));
                }
                bdf.setValue(bdf.getValue().setScale(digit, RoundingMode.FLOOR).stripTrailingZeros());
                if (bdf.getValue().compareTo(BigDecimal.ZERO) == 0) {
                    bdf.clear();
                }
            }
        });
    }

    public static void trimMax (BigDecimalField bdf, int MaxValue, int digit) {
        bdf.addValueChangeListener(event -> {
            if (bdf.getValue() != null ) {
                if (bdf.getValue().compareTo(new BigDecimal(MaxValue + "")) > 0) {
                    bdf.setValue(new BigDecimal(MaxValue + ""));
                }
                bdf.setValue(bdf.getValue().setScale(digit, RoundingMode.FLOOR).stripTrailingZeros());
                if (bdf.getValue().compareTo(BigDecimal.ZERO) == 0) {
                    bdf.clear();
                }
            }
        });
    }

    public static void trimMinMax (BigDecimalField bdf, int MinValue, int MaxValue, int digit) {
        bdf.addValueChangeListener(event -> {
            if (bdf.getValue() != null) {
                if (bdf.getValue().compareTo(new BigDecimal(MinValue + "")) < 0) {
                    bdf.setValue(new BigDecimal(MinValue + ""));
                }
                if (bdf.getValue().compareTo(new BigDecimal(MaxValue + "")) > 0) {
                    bdf.setValue(new BigDecimal(MaxValue + ""));
                }
                bdf.setValue(bdf.getValue().setScale(digit, RoundingMode.FLOOR).stripTrailingZeros());
                if (bdf.getValue().compareTo(BigDecimal.ZERO) == 0) {
                    bdf.clear();
                }
            }
        });
    }


    public static void trimAngle (BigDecimalField bdf, int maxAngle, int digit) {
        bdf.addValueChangeListener(event -> {
            if (bdf.getValue() != null ) {
                bdf.setValue(bdf.getValue().remainder(new BigDecimal(maxAngle + "")).setScale(digit, RoundingMode.FLOOR).stripTrailingZeros());
                if (bdf.getValue().compareTo(BigDecimal.ZERO) == 0) {
                    bdf.clear();
                }
            }
        });
    }
}