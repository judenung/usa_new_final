package com.example.application.tools;

import com.example.application.backends.*;

import java.math.BigDecimal;
import java.util.ArrayList;

public class OutputFrame {

    public Matrix reactions;
    public ArrayList<Matrix> forcesAndDisplacements;
    public boolean isStable;

    public boolean showReaction;
    public boolean showNormal;
    public boolean showShear;
    public boolean showMoment;
    public boolean showDisplacement;

    public boolean isReverseMoment;

    public OutputFrame () {
        forcesAndDisplacements = new ArrayList<>();
        hideAll();
        isReverseMoment = false;
    }

    public void solve(InputFrame IF) {
        Frame F = new Frame(IF.MS);

        for (CrossSection cs : IF.crossSections) {
            F.addCrossSection(cs);
        }
        for (FrameMember fm : IF.frameMembers) {
            F.addFrameMember(fm);
        }
        for (Support s : IF.supports) {
            F.addSupport(s);
        }
        for (Hinge h : IF.hinges) {
            F.addHinge(h);
        }
        for (JointLoad jl : IF.jointLoads) {
            F.addJointLoad(jl);
        }
        for (PointLoad pl : IF.pointLoads) {
            F.addPointLoad(pl);
        }
        for (LinearlyDistributedLoad ldl : IF.linearlyDistributedLoads) {
            F.addLinearlyDistributedLoad(ldl);
        }
        for (TemperatureChange tc : IF.temperatureChanges) {
            F.addTemperatureChange(tc);
        }
        for (FabricationError fe : IF.fabricationErrors) {
            F.addFabricationError(fe);
        }
        for (SupportSettlement ss : IF.supportSettlements) {
            F.addSupportSettlement(ss);
        }

        F.solve();
        extractReaction(F);
        extractForcesAndDisplacements(F);
        isStable = F.isStable;
    }

    private void extractReaction (Frame F) {
        reactions = new Matrix(F.NR, (short) 4);
        reactions.insert(F.R);
    }

    private void extractForcesAndDisplacements (Frame F) {
        forcesAndDisplacements.clear();
        for (FrameMember fm : F.mainFrameMembers) {
            int startIndex = F.subFrameMembersStartingIndex.get(F.mainFrameMembers.indexOf(fm));
            int sfmAmount = F.subFrameMembersStartingIndex.get(F.mainFrameMembers.indexOf(fm) + 1) - startIndex;
            Matrix fm_forcesAndDisplacements = new Matrix((short) (sfmAmount * 2), (short) 8);

            for (int i = startIndex; i < startIndex + sfmAmount; i++) {
                FrameMember sfm = F.subFrameMembers.get(i);

                BigDecimal x1, x2;
                BigDecimal y1, y2;
                BigDecimal N1, N2;
                BigDecimal S1, S2;
                BigDecimal M1, M2;
                BigDecimal dx1, dx2;
                BigDecimal dy1, dy2;
                BigDecimal dz1, dz2;

                x1 = sfm.BCx;
                y1 = sfm.BCy;
                N1 = sfm.Q.data[0][0].negate();
                S1 = sfm.Q.data[1][0];
                M1 = sfm.Q.data[2][0].negate();
                dx1 = sfm.v.data[0][0];
                dy1 = sfm.v.data[1][0];
                dz1 = sfm.v.data[2][0];

                x2 = sfm.ECx;
                y2 = sfm.ECy;
                N2 = sfm.Q.data[3][0];
                S2 = sfm.Q.data[4][0].negate();
                M2 = sfm.Q.data[5][0];
                dx2 = sfm.v.data[3][0];
                dy2 = sfm.v.data[4][0];
                dz2 = sfm.v.data[5][0];

                fm_forcesAndDisplacements.data[2 * (i - startIndex)][0] = x1;
                fm_forcesAndDisplacements.data[2 * (i - startIndex)][1] = y1;
                fm_forcesAndDisplacements.data[2 * (i - startIndex)][2] = N1;
                fm_forcesAndDisplacements.data[2 * (i - startIndex)][3] = S1;
                fm_forcesAndDisplacements.data[2 * (i - startIndex)][4] = M1;
                fm_forcesAndDisplacements.data[2 * (i - startIndex)][5] = dx1;
                fm_forcesAndDisplacements.data[2 * (i - startIndex)][6] = dy1;
                fm_forcesAndDisplacements.data[2 * (i - startIndex)][7] = dz1;

                fm_forcesAndDisplacements.data[2 * (i - startIndex) + 1][0] = x2;
                fm_forcesAndDisplacements.data[2 * (i - startIndex) + 1][1] = y2;
                fm_forcesAndDisplacements.data[2 * (i - startIndex) + 1][2] = N2;
                fm_forcesAndDisplacements.data[2 * (i - startIndex) + 1][3] = S2;
                fm_forcesAndDisplacements.data[2 * (i - startIndex) + 1][4] = M2;
                fm_forcesAndDisplacements.data[2 * (i - startIndex) + 1][5] = dx2;
                fm_forcesAndDisplacements.data[2 * (i - startIndex) + 1][6] = dy2;
                fm_forcesAndDisplacements.data[2 * (i - startIndex) + 1][7] = dz2;
            }
            forcesAndDisplacements.add(fm_forcesAndDisplacements);
        }
    }

    public void showAll () {
        showReaction = true;
        showNormal = true;
        showShear = true;
        showMoment = true;
        showDisplacement = true;
    }

    public void hideAll () {
        showReaction = false;
        showNormal = false;
        showShear = false;
        showMoment = false;
        showDisplacement = false;
        isReverseMoment = false;
        clearAll();
    }

    public void clearAll () {
        reactions = null;
        forcesAndDisplacements.clear();
    }
}
