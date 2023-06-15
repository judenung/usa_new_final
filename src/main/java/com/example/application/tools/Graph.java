package com.example.application.tools;

import com.example.application.backends.*;
import com.storedobject.chart.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

public class Graph extends SOChart {
    private ArrayList<LineChart> lines;
    private ArrayList<LineChart> markedLines;
    private XAxis xAxis;
    private YAxis yAxis;
    private RectangularCoordinate rc;

    private InputFrame IF;
    private OutputFrame OF;
    private GraphStyle GS;
    private Unit U;

    public Graph(@Autowired MyUI UI) {
        lines = new ArrayList<>();
        markedLines = new ArrayList<>();
        xAxis = new XAxis(DataType.NUMBER);
        yAxis = new YAxis(DataType.NUMBER);
        rc = new RectangularCoordinate(xAxis, yAxis);

        IF = UI.getInputFrame();
        OF = UI.getOutputFrame();
        GS = UI.getGraphStyle();
        U = UI.getUnit();

        setCoordinateVisible();

        if (IF.showFrameMember) {
            frameMembersToLines();
            jointToLines();
        }
        if (IF.showLoad) {
            jointLoadsToLines();
            pointLoadsToLines();
            linearlyDistributedLoadToLines();
        }
        if (OF.showReaction && OF.reactions != null) {
            reactionToLines();
        }
        if (IF.showSupport) {
            supportsToLines();
            hingeToLines();
        }
        if (IF.showExternalEffect) {
            supportSettlementsToLines();
            temperatureChangeToLines();
            fabricationErrorToLines();
        }
        if (OF.showNormal) {
            normalToLines();
        }
        if (OF.showShear) {
            shearToLines();
        }
        if (OF.showMoment || OF.isReverseMoment) {
            momentToLines();
        }
        if (OF.showDisplacement) {
            displacementToLines();
        }

        setAxisStyle();
        addToolBox();
        plot();
    }

    private double[] rotatePoint (double Xo, double Yo, double X, double Y, double Theta, boolean isCounterClockwise) {
        double cosTheta;
        double sinTheta;
        if (isCounterClockwise) {
            cosTheta = Math.cos(Math.toRadians(Theta));
            sinTheta = Math.sin(Math.toRadians(Theta));
        }
        else {
            cosTheta = Math.cos(Math.toRadians(360 - Theta));
            sinTheta = Math.sin(Math.toRadians(360 - Theta));
        }

        double Xr = (X - Xo) * cosTheta - (Y - Yo) * sinTheta + Xo;
        double Yr = (X - Xo) * sinTheta + (Y - Yo) * cosTheta + Yo;

        return new double[]{Xr, Yr};
    }

    private LineChart createLine (LineChart line, LineStyle.Type lineType, int lineWidth, String lineColor) {

        LineStyle s = new LineStyle();
        s.setType(lineType);
        s.setWidth(lineWidth);
        line.setLineStyle(s);
        line.setColors(new Color(lineColor));

        Animation a = new Animation();
        a.setDisabled(true);
        line.setAnimation(a);

        return line;
    }

    private LineChart createPointSymbol (LineChart line, PointSymbolType symbolType, int symbolWidth, int symbolHeight, boolean hideSymbol) {

        PointSymbol p = new PointSymbol();
        p.setType(symbolType);
        p.setSize(symbolWidth, symbolHeight);
        if (hideSymbol) {p.hide();}
        line.setPointSymbol(p);

        return line;
    }

    private LineChart[] createStraightArrow (double X, double Y, double cosAlpha, double sinAlpha, double arrowLength, double arrowHeadSize, double arrowHeadAngle, double Theta,
                                            LineStyle.Type lineType, int lineWidth, String lineColor, String tooltipText) {

        Data Dx1 = new Data();
        Data Dy1 = new Data();
        Data Dx2 = new Data();
        Data Dy2 = new Data();
        Data Dx3 = new Data();
        Data Dy3 = new Data();
        
        Dx1.add(X);
        Dy1.add(Y);
        Dx2.add(X);
        Dy2.add(Y);
        Dx3.add(X);
        Dy3.add(Y);

        double Xe1 = X;
        double Ye1 = Y + arrowLength;
        double Xe2 = X - arrowLength * arrowHeadSize * Math.sin(Math.toRadians(arrowHeadAngle));
        double Ye2 = Y + arrowLength * arrowHeadSize * Math.cos(Math.toRadians(arrowHeadAngle));
        double Xe3 = 2 * X - Xe2;
        double Ye3 = Ye2;

        double rotationAngle;
        if (sinAlpha > 0 && cosAlpha == 0) {
            rotationAngle = Theta + 90;
        }
        else if (sinAlpha < 0 && cosAlpha == 0) {
            rotationAngle = Theta + 270;
        }
        else {
            if (cosAlpha < 0) {
                rotationAngle = Theta + Math.toDegrees(Math.atan(sinAlpha / cosAlpha)) + 180;
            }
            else {
                rotationAngle = Theta + Math.toDegrees(Math.atan(sinAlpha / cosAlpha));
            }
        }

        Dx1.add(rotatePoint(X, Y, Xe1, Ye1, rotationAngle, true)[0]);
        Dy1.add(rotatePoint(X, Y, Xe1, Ye1, rotationAngle, true)[1]);
        Dx2.add(rotatePoint(X, Y, Xe2, Ye2, rotationAngle, true)[0]);
        Dy2.add(rotatePoint(X, Y, Xe2, Ye2, rotationAngle, true)[1]);
        Dx3.add(rotatePoint(X, Y, Xe3, Ye3, rotationAngle, true)[0]);
        Dy3.add(rotatePoint(X, Y, Xe3, Ye3, rotationAngle, true)[1]);

        LineChart lc1 = new LineChart(Dx1, Dy1);
        LineChart lc2 = new LineChart(Dx2, Dy2);
        LineChart lc3 = new LineChart(Dx3, Dy3);
        LineChart[] lcs = new LineChart[]{lc1, lc2, lc3};

        boolean[] hideSymbol = {false, true, true};

        for (int i = 0; i < 3; i ++) {
            lcs[i] = createLine(lcs[i], lineType, lineWidth, lineColor);
            lcs[i] = createPointSymbol(lcs[i], PointSymbolType.CIRCLE, lineWidth, lineWidth, hideSymbol[i]);
        }

        lcs[0] = setArrowTooltip(lcs[0], X, Y, tooltipText);

        return lcs;
    }

    private LineChart[] createRoundArrow (double X, double Y, double arrowStartAngle, double arrowEndAngle, int arrowPartitionSize, double arrowRadius, double arrowHeadSize, double arrowHeadInnerAngle, double arrowHeadOuterAngle,
                                         boolean isPositive, LineStyle.Type lineType, int lineWidth, String lineColor, String tooltipText) {

        Data Dx1 = new Data();
        Data Dy1 = new Data();
        Data Dx2 = new Data();
        Data Dy2 = new Data();

        Dx1.add(rotatePoint(X, Y, X + arrowRadius, Y, isPositive ? arrowEndAngle : arrowStartAngle, true)[0]);
        Dy1.add(rotatePoint(X, Y, X + arrowRadius, Y, isPositive ? arrowEndAngle : arrowStartAngle, true)[1]);
        Dx2.add(rotatePoint(X, Y, X + arrowRadius, Y, isPositive ? arrowEndAngle : arrowStartAngle, true)[0]);
        Dy2.add(rotatePoint(X, Y, X + arrowRadius, Y, isPositive ? arrowEndAngle : arrowStartAngle, true)[1]);

        double arcLength = 2 * Math.PI * arrowRadius * (arrowEndAngle - arrowStartAngle) / 360;

        double Xe1;
        double Ye1;
        double Xe2;
        double Ye2;

        if (isPositive) {
            Xe1 = X + arrowRadius - arcLength * arrowHeadSize * Math.sin(Math.toRadians(arrowHeadInnerAngle));
            Ye1 = Y - arcLength * arrowHeadSize * Math.cos(Math.toRadians(arrowHeadInnerAngle));
            Xe2 = X + arrowRadius + arcLength * arrowHeadSize * Math.sin(Math.toRadians(arrowHeadOuterAngle));
            Ye2 = Y - arcLength * arrowHeadSize * Math.cos(Math.toRadians(arrowHeadOuterAngle));
        }
        else {
            Xe1 = X + arrowRadius - arcLength * arrowHeadSize * Math.sin(Math.toRadians(arrowHeadInnerAngle));
            Ye1 = Y + arcLength * arrowHeadSize * Math.cos(Math.toRadians(arrowHeadInnerAngle));
            Xe2 = X + arrowRadius + arcLength * arrowHeadSize * Math.sin(Math.toRadians(arrowHeadOuterAngle));
            Ye2 = Y + arcLength * arrowHeadSize * Math.cos(Math.toRadians(arrowHeadOuterAngle));
        }

        Dx1.add(rotatePoint(X, Y, Xe1, Ye1, isPositive ? arrowEndAngle : arrowStartAngle, true)[0]);
        Dy1.add(rotatePoint(X, Y, Xe1, Ye1, isPositive ? arrowEndAngle : arrowStartAngle, true)[1]);
        Dx2.add(rotatePoint(X, Y, Xe2, Ye2, isPositive ? arrowEndAngle : arrowStartAngle, true)[0]);
        Dy2.add(rotatePoint(X, Y, Xe2, Ye2, isPositive ? arrowEndAngle : arrowStartAngle, true)[1]);


        LineChart[] lcs = new LineChart[arrowPartitionSize + 2];

        LineChart lc1 = new LineChart(Dx1, Dy1);
        LineChart lc2 = new LineChart(Dx2, Dy2);

        lc1 = createLine(lc1, lineType, lineWidth, lineColor);
        lc1 = createPointSymbol(lc1, PointSymbolType.CIRCLE, lineWidth, lineWidth, true);
        lc2 = createLine(lc2, lineType, lineWidth, lineColor);
        lc2 = createPointSymbol(lc2, PointSymbolType.CIRCLE, lineWidth, lineWidth, true);

        lcs[arrowPartitionSize] = lc1;
        lcs[arrowPartitionSize + 1] = lc2;


        for (int i = 0; i < arrowPartitionSize; i++) {
            Data Dx3 = new Data();
            Data Dy3 = new Data();

            Dx3.add(rotatePoint(X, Y, X + arrowRadius, Y, arrowStartAngle + (arrowEndAngle - arrowStartAngle) * i / arrowPartitionSize, true)[0]);
            Dx3.add(rotatePoint(X, Y, X + arrowRadius, Y, arrowStartAngle + (arrowEndAngle - arrowStartAngle) * (i + 1) / arrowPartitionSize, true)[0]);
            Dy3.add(rotatePoint(X, Y, X + arrowRadius, Y, arrowStartAngle + (arrowEndAngle - arrowStartAngle) * i / arrowPartitionSize, true)[1]);
            Dy3.add(rotatePoint(X, Y, X + arrowRadius, Y, arrowStartAngle + (arrowEndAngle - arrowStartAngle) * (i + 1) / arrowPartitionSize, true)[1]);

            LineChart lci = new LineChart(Dx3, Dy3);
            lci = createLine(lci, lineType, lineWidth, lineColor);

            if (i == 0 || i == arrowPartitionSize -1) {
                lci = createPointSymbol(lci, PointSymbolType.CIRCLE, lineWidth, lineWidth, false);
                lci = setArrowTooltip(lci, X, Y, tooltipText);
            }
            else {
                lci = createPointSymbol(lci, PointSymbolType.CIRCLE, lineWidth, lineWidth, true);
            }

            lcs[i] = lci;
        }

        return lcs;
    }

    private LineChart[] createArrowHead (double X, double Y, double cosAlpha, double sinAlpha, double distantFromEnd, double arrowHeadSize, double arrowHeadAngle, double Theta,
                                         LineStyle.Type lineType, int lineWidth, String lineColor, String tooltipText, boolean isOutward) {
        Data Dx1 = new Data();
        Data Dy1 = new Data();
        Data Dx2 = new Data();
        Data Dy2 = new Data();

        double rotationAngle;
        if (sinAlpha > 0 && cosAlpha == 0) {
            rotationAngle = Theta + 90;
        }
        else if (sinAlpha < 0 && cosAlpha == 0) {
            rotationAngle = Theta + 270;
        }
        else {
            if (cosAlpha < 0) {
                rotationAngle = Theta + Math.toDegrees(Math.atan(sinAlpha / cosAlpha)) + 180;
            }
            else {
                rotationAngle = Theta + Math.toDegrees(Math.atan(sinAlpha / cosAlpha));
            }
        }

        Dx1.add(rotatePoint(X, Y, X + distantFromEnd, Y, rotationAngle, true)[0]);
        Dy1.add(rotatePoint(X, Y, X + distantFromEnd, Y, rotationAngle, true)[1]);
        Dx2.add(rotatePoint(X, Y, X + distantFromEnd, Y, rotationAngle, true)[0]);
        Dy2.add(rotatePoint(X, Y, X + distantFromEnd, Y, rotationAngle, true)[1]);

        double Xe1, Xe2, Ye1, Ye2;

        if (isOutward) {
            Xe1 = X + distantFromEnd + arrowHeadSize * Math.cos(Math.toRadians(arrowHeadAngle));
            Xe2 = X + distantFromEnd + arrowHeadSize * Math.cos(Math.toRadians(arrowHeadAngle));
        }
        else {
            Xe1 = X + distantFromEnd - arrowHeadSize * Math.cos(Math.toRadians(arrowHeadAngle));
            Xe2 = X + distantFromEnd - arrowHeadSize * Math.cos(Math.toRadians(arrowHeadAngle));
        }
        Ye1 = Y + arrowHeadSize * Math.sin(Math.toRadians(arrowHeadAngle));
        Ye2 = Y - arrowHeadSize * Math.sin(Math.toRadians(arrowHeadAngle));

        Dx1.add(rotatePoint(X, Y, Xe1, Ye1, rotationAngle, true)[0]);
        Dy1.add(rotatePoint(X, Y, Xe1, Ye1, rotationAngle, true)[1]);
        Dx2.add(rotatePoint(X, Y, Xe2, Ye2, rotationAngle, true)[0]);
        Dy2.add(rotatePoint(X, Y, Xe2, Ye2, rotationAngle, true)[1]);

        LineChart lc1 = new LineChart(Dx1, Dy1);
        LineChart lc2 = new LineChart(Dx2, Dy2);
        LineChart[] lcs = new LineChart[]{lc1, lc2};

        for (int i = 0; i < 2; i ++) {
            lcs[i] = createLine(lcs[i], lineType, lineWidth, lineColor);
            lcs[i] = createPointSymbol(lcs[i], PointSymbolType.CIRCLE, lineWidth, lineWidth, false);
            lcs[i] = setArrowHeadTooltip(lcs[i], tooltipText);
        }

        return lcs;
    }

    private LineChart setJointTooltip(LineChart lc, double X, double Y) {
        lc.getTooltip(true)
                .append("X = " + X + " " + U.m)
                .newline()
                .append("Y = " + Y + " " + U.m);

        return lc;
    }

    private LineChart setSupportTooltip (LineChart lc, double X, double Y, String supportType) {
        lc.getTooltip(true)
                .append(supportType + " Support")
                .newline()
                .append("X = " + X + " " + U.m)
                .newline()
                .append("Y = " + Y + " " + U.m);

        return lc;
    }

    private LineChart setSupportTooltip (LineChart lc, double A, double X, double Y, String supportType) {
        lc.getTooltip(true)
                .append(supportType + " Support")
                .newline()
                .append("Incline Angle = " + A + " " + U.deg)
                .newline()
                .append("X = " + X + " " + U.m)
                .newline()
                .append("Y = " + Y + " " + U.m);

        return lc;
    }

    private LineChart setSupportSettlementTooltip (LineChart lc, double X, double Y, String supportType, BigDecimal Sx, BigDecimal Sy, BigDecimal Sz) {
        lc.getTooltip(true)
                .append(supportType + " Support (With Settlement)")
                .newline();
        if (Sx.compareTo(BigDecimal.ZERO) != 0) {
            lc.getTooltip(true)
                    .append("Sx = " + U.round(U.convertSL2(Sx)).toPlainString() + " " + U.mm)
                    .newline();
        }
        if (Sy.compareTo(BigDecimal.ZERO) != 0) {
            lc.getTooltip(true)
                    .append("Sy = " + U.round(U.convertSL2(Sy)).toPlainString() + " " + U.mm)
                    .newline();
        }
        if (Sz.compareTo(BigDecimal.ZERO) != 0) {
            lc.getTooltip(true)
                    .append("Sz = " + U.round(U.convertRad2(Sz)).toPlainString() + " " + U.deg)
                    .newline();
        }
        lc.getTooltip(true)
                .append("X = " + X + " " + U.m)
                .newline()
                .append("Y = " + Y + " " + U.m);

        return lc;
    }

    private LineChart setArrowTooltip (LineChart lc, double X, double Y, String text) {
        lc.getTooltip(true)
                .append(text)
                .newline()
                .append("X = " + X + " " + U.m)
                .newline()
                .append("Y = " + Y + " " + U.m);

        return lc;
    }

    private LineChart setArrowHeadTooltip (LineChart lc, String text) {
        lc.getTooltip(true).append(text);

        return lc;
    }

    private LineChart setDisplacementTooltip (LineChart lc, BigDecimal dx, BigDecimal dy, BigDecimal dz, double X, double Y) {
        lc.getTooltip(true)
                .append("dx = " + U.round(U.convertSL2(dx)).toPlainString() + " " + U.mm)
                .newline()
                .append("dy = " + U.round(U.convertSL2(dy)).toPlainString() + " " + U.mm)
                .newline()
                .append("dz = " + U.round(U.convertRad2(dz)).toPlainString() + " " + U.deg)
                .newline()
                .append("X = " + X + " " + U.m)
                .newline()
                .append("Y = " + Y + " " + U.m);

        return lc;
    }

    private void setCoordinateVisible () {
        Data Dx = new Data();
        Data Dy = new Data();
        Dx.add(0);
        Dx.add(0);
        Dy.add(0);
        Dy.add(0);
        LineChart lc = new LineChart(Dx, Dy);

        this.lines.add(createPointSymbol(lc, PointSymbolType.NONE, 0, 0, true));
    }

    private void frameMembersToLines () {

        LineStyle.Type lineType = GS.frameMemberLineStyleType;
        int lineWidth = GS.frameMemberLineWidth;
        PointSymbolType symbolType = GS.frameMemberPointSymbolType;
        boolean hideSymbol = GS.frameMemberHideSymbol;
        double distantToDashedLine = GS.frameMemberDistantToDashedLine;

        for (FrameMember fm : IF.frameMembers) {

            if (IF.undisplayedFrameMembers.contains(fm.ID - 1)) {
                continue;
            }

            String lineColor = IF.markedFrameMembers.contains(IF.frameMembers.indexOf(fm)) ? GS.markedColor : GS.frameMemberLineColor;
            String dashedLineColor = IF.markedFrameMembers.contains(IF.frameMembers.indexOf(fm)) ? GS.markedColor : GS.frameMemberDashedLineColor;

            Data Dx = new Data();
            Data Dy = new Data();

            Dx.add(fm.BCx.doubleValue());
            Dx.add(fm.ECx.doubleValue());
            Dy.add(fm.BCy.doubleValue());
            Dy.add(fm.ECy.doubleValue());

            LineChart lc = new LineChart(Dx, Dy);
            lc = createLine(lc, lineType, lineWidth, lineColor);
            lc = createPointSymbol(lc, symbolType, lineWidth, lineWidth, hideSymbol);

            LineChart lc_dashed = frameMembersToDashedLines(dashedLineColor, distantToDashedLine, fm.BCx.doubleValue(), fm.BCy.doubleValue(),
                    fm.ECx.doubleValue(), fm.ECy.doubleValue(), fm.cos.doubleValue(), fm.sin.doubleValue());

            if (!IF.markedFrameMembers.contains(IF.frameMembers.indexOf(fm))) {
                this.lines.add(lc);
                this.lines.add(lc_dashed);
            }
            else {
                this.markedLines.add(lc);
                this.markedLines.add(lc_dashed);
            }
        }
    }

    private LineChart frameMembersToDashedLines (String lineColor, double distant, double BCx, double BCy, double ECx, double ECy, double cosAlpha, double sinAlpha) {

        LineStyle.Type lineType = GS.frameMemberDashedLineStyleType;
        int lineWidth = GS.frameMemberDashedLineWidth;
        PointSymbolType symbolType = GS.frameMemberDashedLinePointSymbolType;
        boolean hideSymbol = GS.frameMemberDashedLineHideSymbol;

        Data Dx = new Data();
        Data Dy = new Data();

        double rotationAngle;
        if (sinAlpha > 0 && cosAlpha == 0) {
            rotationAngle = 90;
        }
        else if (sinAlpha < 0 && cosAlpha == 0) {
            rotationAngle = 270;
        }
        else {
            if (cosAlpha < 0) {
                rotationAngle = Math.toDegrees(Math.atan(sinAlpha / cosAlpha)) + 180;
            }
            else {
                rotationAngle = Math.toDegrees(Math.atan(sinAlpha / cosAlpha));
            }
        }

        double x1 = rotatePoint(BCx, BCy, BCx, BCy - distant, rotationAngle, true)[0];
        double y1 = rotatePoint(BCx, BCy, BCx, BCy - distant, rotationAngle, true)[1];
        double x2 = ECx - BCx + x1;
        double y2 = ECy - BCy + y1;

        Dx.add(x1);
        Dx.add(x2);
        Dy.add(y1);
        Dy.add(y2);

        LineChart lc = new LineChart(Dx, Dy);
        lc = createLine(lc, lineType, lineWidth, lineColor);
        lc = createPointSymbol(lc, symbolType, lineWidth, lineWidth, hideSymbol);

        return lc;
    }

    private void jointToLines () {

        PointSymbolType symbolType = GS.jointPointSymbolType;
        boolean hideSymbol = GS.jointHideSymbol;

        IF.setJoints();

        for (Joint j : IF.joints) {

            String lineColor = IF.markedJoints.contains(IF.joints.indexOf(j)) ? GS.markedColor : GS.jointLineColor;
            int lineWidth = IF.markedJoints.contains(IF.joints.indexOf(j)) ? GS.jointLineWidth + 2 : GS.jointLineWidth;

            Data Dx = new Data();
            Data Dy = new Data();

            Dx.add(j.Cx.doubleValue());
            Dx.add(j.Cx.doubleValue());
            Dy.add(j.Cy.doubleValue());
            Dy.add(j.Cy.doubleValue());

            LineChart lc = new LineChart(Dx, Dy);
            lc = createLine(lc, LineStyle.Type.SOLID, lineWidth, lineColor);
            lc = createPointSymbol(lc, symbolType, lineWidth, lineWidth, hideSymbol);
            lc = setJointTooltip(lc, j.Cx.doubleValue(), j.Cy.doubleValue());

            if (!IF.markedJoints.contains(IF.joints.indexOf(j))) {
                this.lines.add(lc);
            }
            else {
                this.markedLines.add(lc);
            }
        }
    }

    private  void supportsToLines () {

        PointSymbolType[] symbolTypes = GS.supportPointSymbolType;
        int[][] symbolSize = GS.supportPointSymbolSize;
        String[] supportTypes = {"Fixed", "Pinned", "Incline Roller", "Roller", "vertical Roller"};

        for (Support sp : IF.supports) {

            String lineColor = IF.markedSupports.contains(IF.supports.indexOf(sp)) ? GS.markedColor : GS.supportLineColor;

            Data Dx = new Data();
            Data Dy = new Data();

            Dx.add(sp.Cx.doubleValue());
            Dx.add(sp.Cx.doubleValue());
            Dy.add(sp.Cy.doubleValue());
            Dy.add(sp.Cy.doubleValue());

            LineChart lc = new LineChart(Dx, Dy);

            lc = createLine(lc, LineStyle.Type.SOLID, 1, lineColor);
            lc = createPointSymbol(lc, symbolTypes[sp.ST - 1], symbolSize[sp.ST - 1][0], symbolSize[sp.ST - 1][1], false);
            if (sp.ST == 3) {
                lc = setSupportTooltip(lc, sp.angle.doubleValue(), sp.Cx.doubleValue(), sp.Cy.doubleValue(), supportTypes[sp.ST - 1]);
            }
            else {
                lc = setSupportTooltip(lc, sp.Cx.doubleValue(), sp.Cy.doubleValue(), supportTypes[sp.ST - 1]);
            }
            

            if (!IF.markedSupports.contains(IF.supports.indexOf(sp))) {
                this.lines.add(lc);
            }
            else {
                this.markedLines.add(lc);
            }
        }
    }

    private void hingeToLines () {

        double distantFromEnd = GS.hingeDistantFromEnd * IF.PH.doubleValue() / 10;

        for (Hinge h : IF.hinges) {

            String lineColor = IF.markedHinges.contains(IF.hinges.indexOf(h)) ? GS.markedColor : GS.hingeLineColor;

            for (int index : h.MN) {
                Data Dx = new Data();
                Data Dy = new Data();

                FrameMember fm = IF.frameMembers.get(index);

                double rotationAngle;
                if (fm.sin.doubleValue() > 0 && fm.cos.doubleValue() == 0) {
                    rotationAngle = 90;
                }
                else if (fm.sin.doubleValue() < 0 && fm.cos.doubleValue() == 0) {
                    rotationAngle = 270;
                }
                else {
                    if (fm.cos.doubleValue() < 0) {
                        rotationAngle = Math.toDegrees(Math.atan(fm.sin.doubleValue() / fm.cos.doubleValue())) + 180;
                    }
                    else {
                        rotationAngle = Math.toDegrees(Math.atan(fm.sin.doubleValue() / fm.cos.doubleValue()));
                    }
                }

                LineChart lc = new LineChart(Dx, Dy);

                if (h.Cx.compareTo(fm.BCx) == 0 && h.Cy.compareTo(fm.BCy) == 0) {
                    double[] C = rotatePoint(fm.BCx.doubleValue(), fm.BCy.doubleValue(), fm.BCx.doubleValue() + distantFromEnd, fm.BCy.doubleValue(), rotationAngle, true);
                    Dx.add(C[0]);
                    Dx.add(C[0]);
                    Dy.add(C[1]);
                    Dy.add(C[1]);}
                else if (h.Cx.compareTo(fm.ECx) == 0 && h.Cy.compareTo(fm.ECy) == 0) {
                    double[] C = rotatePoint(fm.ECx.doubleValue(), fm.ECy.doubleValue(), fm.ECx.doubleValue() + distantFromEnd, fm.ECy.doubleValue(), rotationAngle + 180, true);
                    Dx.add(C[0]);
                    Dx.add(C[0]);
                    Dy.add(C[1]);
                    Dy.add(C[1]);
                }

                lc = createLine(lc, LineStyle.Type.SOLID, 5, lineColor);
                lc = setArrowTooltip(lc, h.Cx.doubleValue(), h.Cy.doubleValue(), "Hinge");

                if (!IF.markedHinges.contains(IF.hinges.indexOf(h))) {
                    this.lines.add(lc);
                }
                else {
                    this.markedLines.add(lc);
                }
            }
        }
    }

    private void supportSettlementsToLines () {

        PointSymbolType[] symbolTypes = GS.supportPointSymbolType;
        int[][] symbolSize = GS.supportPointSymbolSize;
        String[] supportTypes = {"Fixed", "Pinned", "Roller", "Vertical Roller", "Hinge"};

        for (SupportSettlement ss : IF.supportSettlements) {

            String lineColor = IF.markedSupportsSettlements.contains(IF.supportSettlements.indexOf(ss)) ? GS.markedColor : GS.supportSettlementLineColor;

            int ST = 0;
            for (Support sp : IF.supports) {
                if (ss.Cx.compareTo(sp.Cx) == 0 && ss.Cy.compareTo(sp.Cy) == 0) {
                    ST = sp.ST;
                    break;
                }
            }
            if (ST == 0) {
                return;
            }

            Data Dx = new Data();
            Data Dy = new Data();

            Dx.add(ss.Cx.doubleValue());
            Dx.add(ss.Cx.doubleValue());
            Dy.add(ss.Cy.doubleValue());
            Dy.add(ss.Cy.doubleValue());

            LineChart lc = new LineChart(Dx, Dy);

            lc = createLine(lc, LineStyle.Type.SOLID, 1, lineColor);
            lc = createPointSymbol(lc, symbolTypes[ST - 1], symbolSize[ST - 1][0], symbolSize[ST - 1][1], false);
            lc = setSupportSettlementTooltip(lc, ss.Cx.doubleValue(), ss.Cy.doubleValue(), supportTypes[ST - 1],
                    ss.settlement[0], ss.settlement[1], ss.settlement[2]);

            if (!IF.markedSupportsSettlements.contains(IF.supportSettlements.indexOf(ss))) {
                this.lines.add(lc);
            }
            else {
                this.markedLines.add(lc);
            }
        }
    }

    private void temperatureChangeToLines () {

        LineStyle.Type lineType = GS.temperatureChangeLineStyleType;
        int lineWidth = GS.temperatureChangeLineWidth;
        double distantFromEnd = GS.temperatureChangeDistantFromEnd * IF.PH.doubleValue() / 10;
        double arrowHeadSize = GS.temperatureChangeArrowHeadSize * IF.PH.doubleValue() / 10;
        double arrowHeadAngle = GS.temperatureChangeArrowHeadAngle;

        for (TemperatureChange tc : IF.temperatureChanges) {

            String lineColor = IF.markedTemperatureChanges.contains(IF.temperatureChanges.indexOf(tc)) ? GS.markedColor : GS.temperatureChangeLineColor;
            FrameMember fm = IF.frameMembers.get(tc.MN);

            boolean isOutward = tc.T.multiply(tc.alpha).compareTo(BigDecimal.ZERO) > 0;
            String text = "Temperature Change\nΔT = " + tc.T.stripTrailingZeros().toPlainString() + " " + U.C + "\nα = " + U.round(U.convertOC2(tc.alpha)).toPlainString() + "×10\u207B\u2076/" + U.C;

            if (!IF.markedTemperatureChanges.contains(IF.temperatureChanges.indexOf(tc))) {
                Collections.addAll(this.lines, createArrowHead(fm.BCx.doubleValue(), fm.BCy.doubleValue(), fm.cos.doubleValue(), fm.sin.doubleValue(),
                        distantFromEnd, arrowHeadSize, arrowHeadAngle, 0, lineType, lineWidth, lineColor, text, isOutward));

                Collections.addAll(this.lines, createArrowHead(fm.ECx.doubleValue(), fm.ECy.doubleValue(), fm.cos.doubleValue(), fm.sin.doubleValue(),
                        distantFromEnd, arrowHeadSize, arrowHeadAngle, 180, lineType, lineWidth, lineColor, text, isOutward));
            }
            else {
                Collections.addAll(this.markedLines, createArrowHead(fm.BCx.doubleValue(), fm.BCy.doubleValue(), fm.cos.doubleValue(), fm.sin.doubleValue(),
                        distantFromEnd, arrowHeadSize, arrowHeadAngle, 0, lineType, lineWidth, lineColor, text, isOutward));

                Collections.addAll(this.markedLines, createArrowHead(fm.ECx.doubleValue(), fm.ECy.doubleValue(), fm.cos.doubleValue(), fm.sin.doubleValue(),
                        distantFromEnd, arrowHeadSize, arrowHeadAngle, 180, lineType, lineWidth, lineColor, text, isOutward));
            }
        }
    }

    private void fabricationErrorToLines () {

        LineStyle.Type lineType = GS.fabricationErrorLineStyleType;
        int lineWidth = GS.fabricationErrorLineWidth;
        double distantFromEnd = GS.fabricationErrorDistantFromEnd * IF.PH.doubleValue() / 10;
        double arrowHeadSize = GS.fabricationErrorArrowHeadSize * IF.PH.doubleValue() / 10;
        double arrowHeadAngle = GS.fabricationErrorArrowHeadAngle;

        for (FabricationError fe : IF.fabricationErrors) {

            String lineColor = IF.markedFabricationErrors.contains(IF.fabricationErrors.indexOf(fe)) ? GS.markedColor : GS.fabricationErrorLineColor;
            FrameMember fm = IF.frameMembers.get(fe.MN);

            boolean isOutward = fe.e.compareTo(BigDecimal.ZERO) > 0;
            String text = "Fabrication Error\ne = " + U.round(U.convertSL2(fe.e)).stripTrailingZeros().toPlainString() + " " + U.mm;

            if (!IF.markedFabricationErrors.contains(IF.fabricationErrors.indexOf(fe))) {
                Collections.addAll(this.lines, createArrowHead(fm.BCx.doubleValue(), fm.BCy.doubleValue(), fm.cos.doubleValue(), fm.sin.doubleValue(),
                        distantFromEnd, arrowHeadSize, arrowHeadAngle, 0, lineType, lineWidth, lineColor, text, isOutward));

                Collections.addAll(this.lines, createArrowHead(fm.ECx.doubleValue(), fm.ECy.doubleValue(), fm.cos.doubleValue(), fm.sin.doubleValue(),
                        distantFromEnd, arrowHeadSize, arrowHeadAngle, 180, lineType, lineWidth, lineColor, text, isOutward));
            }
            else {
                Collections.addAll(this.markedLines, createArrowHead(fm.BCx.doubleValue(), fm.BCy.doubleValue(), fm.cos.doubleValue(), fm.sin.doubleValue(),
                        distantFromEnd, arrowHeadSize, arrowHeadAngle, 0, lineType, lineWidth, lineColor, text, isOutward));

                Collections.addAll(this.markedLines, createArrowHead(fm.ECx.doubleValue(), fm.ECy.doubleValue(), fm.cos.doubleValue(), fm.sin.doubleValue(),
                        distantFromEnd, arrowHeadSize, arrowHeadAngle, 180, lineType, lineWidth, lineColor, text, isOutward));
            }
        }
    }

    private void jointLoadsToLines () {

        LineStyle.Type lineType = GS.jointLoadLineStyleType;
        int lineWidth = GS.jointLoadLineWidth;
        double straightArrowLength = GS.jointLoadStraightArrowLength * IF.PH.doubleValue() / 10;
        double straightArrowHeadSize = GS.jointLoadStraightArrowHeadSize;
        double straightArrowHeadAngle = GS.jointLoadStraightArrowHeadAngle;
        double roundArrowStartAngle = GS.jointLoadRoundArrowStartAngle;
        double roundArrowEndAngle = GS.jointLoadRoundArrowEndAngle;
        double roundArrowRadius = GS.jointLoadRoundArrowRadius * IF.PH.doubleValue() / 10;
        int roundArrowPartitionSize = GS.jointLoadRoundArrowPartitionSize;
        double roundArrowHeadSize = GS.jointLoadRoundArrowHeadSize;
        double roundArrowHeadInnerAngle = GS.jointLoadRoundArrowHeadInnerAngle;
        double roundArrowHeadOuterAngle = GS.jointLoadRoundArrowHeadOuterAngle;

        for (JointLoad jl : IF.jointLoads) {

            String lineColor = IF.markedJointLoads.contains(IF.jointLoads.indexOf(jl)) ? GS.markedColor : GS.jointLoadLineColor;

            if (!IF.markedJointLoads.contains(IF.jointLoads.indexOf(jl))) {
                if (jl.load[0].compareTo(BigDecimal.ZERO) != 0) {
                    double rotationAngle = jl.load[0].compareTo(BigDecimal.ZERO) > 0 ? 90 : 270;

                    Collections.addAll(this.lines, createStraightArrow(jl.Cx.doubleValue(), jl.Cy.doubleValue(), 1, 0,
                            straightArrowLength, straightArrowHeadSize, straightArrowHeadAngle, rotationAngle, lineType, lineWidth, lineColor,
                            "Fx = " + jl.load[0].stripTrailingZeros().toPlainString() + " " + U.kN));
                }
                if (jl.load[1].compareTo(BigDecimal.ZERO) != 0) {
                    double rotationAngle = jl.load[1].compareTo(BigDecimal.ZERO) > 0 ? 180 : 0;

                    Collections.addAll(this.lines, createStraightArrow(jl.Cx.doubleValue(), jl.Cy.doubleValue(), 1, 0,
                            straightArrowLength, straightArrowHeadSize, straightArrowHeadAngle, rotationAngle, lineType, lineWidth, lineColor,
                            "Fy = " + jl.load[1].stripTrailingZeros().toPlainString() + " " + U.kN));
                }
                if (jl.load[2].compareTo(BigDecimal.ZERO) != 0) {
                    Collections.addAll(this.lines, createRoundArrow(jl.Cx.doubleValue(), jl.Cy.doubleValue(), roundArrowStartAngle, roundArrowEndAngle,
                            roundArrowPartitionSize, roundArrowRadius, roundArrowHeadSize, roundArrowHeadInnerAngle, roundArrowHeadOuterAngle, jl.load[2].compareTo(BigDecimal.ZERO) > 0,
                            lineType, lineWidth, lineColor, "Mz = " + jl.load[2].stripTrailingZeros().toPlainString() + " " + U.kNm));
                }
            }
            else {
                if (jl.load[0].compareTo(BigDecimal.ZERO) != 0) {
                    double rotationAngle = jl.load[0].compareTo(BigDecimal.ZERO) > 0 ? 90 : 270;

                    Collections.addAll(this.markedLines, createStraightArrow(jl.Cx.doubleValue(), jl.Cy.doubleValue(), 1, 0,
                            straightArrowLength, straightArrowHeadSize, straightArrowHeadAngle, rotationAngle, lineType, lineWidth, lineColor,
                            "Fx = " + jl.load[0].stripTrailingZeros().toPlainString() + " " + U.kN));
                }
                if (jl.load[1].compareTo(BigDecimal.ZERO) != 0) {
                    double rotationAngle = jl.load[1].compareTo(BigDecimal.ZERO) > 0 ? 180 : 0;

                    Collections.addAll(this.markedLines, createStraightArrow(jl.Cx.doubleValue(), jl.Cy.doubleValue(), 1, 0,
                            straightArrowLength, straightArrowHeadSize, straightArrowHeadAngle, rotationAngle, lineType, lineWidth, lineColor,
                            "Fy = " + jl.load[1].stripTrailingZeros().toPlainString() + " " + U.kN));
                }
                if (jl.load[2].compareTo(BigDecimal.ZERO) != 0) {
                    Collections.addAll(this.markedLines, createRoundArrow(jl.Cx.doubleValue(), jl.Cy.doubleValue(), roundArrowStartAngle, roundArrowEndAngle,
                            roundArrowPartitionSize, roundArrowRadius, roundArrowHeadSize, roundArrowHeadInnerAngle, roundArrowHeadOuterAngle, jl.load[2].compareTo(BigDecimal.ZERO) > 0,
                            lineType, lineWidth, lineColor, "Mz = " + jl.load[2].stripTrailingZeros().toPlainString() + " " + U.kNm));
                }
            }
        }
    }

    private void pointLoadsToLines () {

        LineStyle.Type lineType = GS.pointLoadLineStyleType;
        int lineWidth = GS.pointLoadLineWidth;
        double straightArrowLength = GS.pointLoadStraightArrowLength * IF.PH.doubleValue() / 10;
        double straightArrowHeadSize = GS.pointLoadStraightArrowHeadSize;
        double straightArrowHeadAngle = GS.pointLoadStraightArrowHeadAngle;
        double roundArrowStartAngle = GS.pointLoadRoundArrowStartAngle;
        double roundArrowEndAngle = GS.pointLoadRoundArrowEndAngle;
        double roundArrowRadius = GS.pointLoadRoundArrowRadius * IF.PH.doubleValue() / 10;
        int roundArrowPartitionSize = GS.pointLoadRoundArrowPartitionSize;
        double roundArrowHeadSize = GS.pointLoadRoundArrowHeadSize;
        double roundArrowHeadInnerAngle = GS.pointLoadRoundArrowHeadInnerAngle;
        double roundArrowHeadOuterAngle = GS.pointLoadRoundArrowHeadOuterAngle;

        for (PointLoad pl : IF.pointLoads) {

            String lineColor = IF.markedPointLoads.contains(IF.pointLoads.indexOf(pl)) ? GS.markedColor : GS.pointLoadLineColor;

            FrameMember fm = IF.frameMembers.get(pl.MN);
            double Cx = U.round(fm.BCx.doubleValue() + pl.d.doubleValue() * fm.cos.doubleValue());
            double Cy = U.round(fm.BCy.doubleValue() + pl.d.doubleValue() * fm.sin.doubleValue());
            double cos = pl.LT == 1 ? fm.cos.doubleValue() : 1;
            double sin = pl.LT == 1 ? fm.sin.doubleValue() : 0;
            String type = pl.LT == 1 ? " (Local)" : " (Global)";

            if (!IF.markedPointLoads.contains(IF.pointLoads.indexOf(pl))) {
                if (pl.load[0].compareTo(BigDecimal.ZERO) != 0) {
                    double rotationAngle = pl.load[0].compareTo(BigDecimal.ZERO) > 0 ? 90 : 270;

                    Collections.addAll(this.lines, createStraightArrow(Cx, Cy, cos, sin, straightArrowLength, straightArrowHeadSize, straightArrowHeadAngle,
                            rotationAngle, lineType, lineWidth, lineColor, "Fx = " + pl.load[0].stripTrailingZeros().toPlainString() + " " + U.kN + type));
                }
                if (pl.load[1].compareTo(BigDecimal.ZERO) != 0) {
                    double rotationAngle = pl.load[1].compareTo(BigDecimal.ZERO) > 0 ? 180 : 0;

                    Collections.addAll(this.lines, createStraightArrow(Cx, Cy, cos, sin, straightArrowLength, straightArrowHeadSize, straightArrowHeadAngle,
                            rotationAngle, lineType, lineWidth, lineColor, "Fy = " + pl.load[1].stripTrailingZeros().toPlainString() + " " + U.kN + type));
                }
                if (pl.load[2].compareTo(BigDecimal.ZERO) != 0) {
                    Collections.addAll(this.lines, createRoundArrow(Cx, Cy, roundArrowStartAngle, roundArrowEndAngle,
                            roundArrowPartitionSize, roundArrowRadius, roundArrowHeadSize, roundArrowHeadInnerAngle, roundArrowHeadOuterAngle, pl.load[2].compareTo(BigDecimal.ZERO) > 0,
                            lineType, lineWidth, lineColor, "Mz = " + pl.load[2].stripTrailingZeros().toPlainString() + " " + U.kNm + type));
                }
            }
            else {
                if (pl.load[0].compareTo(BigDecimal.ZERO) != 0) {
                    double rotationAngle = pl.load[0].compareTo(BigDecimal.ZERO) > 0 ? 90 : 270;

                    Collections.addAll(this.markedLines, createStraightArrow(Cx, Cy, cos, sin, straightArrowLength, straightArrowHeadSize, straightArrowHeadAngle,
                            rotationAngle, lineType, lineWidth, lineColor, "Fx = " + pl.load[0].stripTrailingZeros().toPlainString() + " " + U.kN + type));
                }
                if (pl.load[1].compareTo(BigDecimal.ZERO) != 0) {
                    double rotationAngle = pl.load[1].compareTo(BigDecimal.ZERO) > 0 ? 180 : 0;

                    Collections.addAll(this.markedLines, createStraightArrow(Cx, Cy, cos, sin, straightArrowLength, straightArrowHeadSize, straightArrowHeadAngle,
                            rotationAngle, lineType, lineWidth, lineColor, "Fy = " + pl.load[1].stripTrailingZeros().toPlainString() + " " + U.kN + type));
                }
                if (pl.load[2].compareTo(BigDecimal.ZERO) != 0) {
                    Collections.addAll(this.markedLines, createRoundArrow(Cx, Cy, roundArrowStartAngle, roundArrowEndAngle,
                            roundArrowPartitionSize, roundArrowRadius, roundArrowHeadSize, roundArrowHeadInnerAngle, roundArrowHeadOuterAngle, pl.load[2].compareTo(BigDecimal.ZERO) > 0,
                            lineType, lineWidth, lineColor, "Mz = " + pl.load[2].stripTrailingZeros().toPlainString() + " " + U.kNm + type));
                }
            }
        }
    }

    private void linearlyDistributedLoadToLines () {

        LineStyle.Type lineType = GS.linearlyDistributedLoadLineStyleType;
        int lineWidth = GS.linearlyDistributedLoadLineWidth;
        double maxArrowLength = GS.linearlyDistributedLoadStraightArrowLength * IF.PH.doubleValue() / 10;
        double arrowHeadSize = GS.linearlyDistributedLoadStraightArrowHeadSize;
        double arrowHeadAngle = GS.linearlyDistributedLoadStraightArrowHeadAngle;

        for (LinearlyDistributedLoad ldl : IF.linearlyDistributedLoads) {

            String lineColor = IF.markedLinearlyDistributedLoads.contains(IF.linearlyDistributedLoads.indexOf(ldl)) ? GS.markedColor : GS.linearlyDistributedLoadLineColor;
            FrameMember fm = IF.frameMembers.get(ldl.MN);

            double maxForce = Math.max(ldl.load[0].abs().doubleValue(), ldl.load[1].abs().doubleValue());
            double loadLength = fm.L.doubleValue() - ldl.load[2].doubleValue() - ldl.load[3].doubleValue();
            double cos = ldl.LT == 1 || ldl.LT == 2 ? fm.cos.doubleValue() : 1;
            double sin = ldl.LT == 1 || ldl.LT == 2 ? fm.sin.doubleValue() : 0;
            double rotationAngle1 = ldl.LT == 1 || ldl.LT == 3 ? 90 : 180;
            String type = ldl.LT == 1 || ldl.LT == 3 ?  "Fx = " : "Fy = ";
            String orientation = ldl.LT == 1 || ldl.LT == 2 ? " (Local)" : " (Global)";

            double x = 0;
            double y = 0;

            double floorLoadLengthScale = Math.floor(loadLength / IF.PH.doubleValue() * 10) > 0 ? Math.floor(loadLength / IF.PH.doubleValue() * 10) : 1;

            for (int i = 0; i <= floorLoadLengthScale; i++) {
                double Cx = U.round(fm.BCx.doubleValue() + (ldl.load[2].doubleValue() + (loadLength) / (floorLoadLengthScale) * i) * fm.cos.doubleValue());
                double Cy = U.round(fm.BCy.doubleValue() + (ldl.load[2].doubleValue() + (loadLength) / (floorLoadLengthScale) * i) * fm.sin.doubleValue());
                double fd = U.round(ldl.load[0].doubleValue() + (ldl.load[1].doubleValue() - ldl.load[0].doubleValue()) / (floorLoadLengthScale) * i);
                double rotationAngle2 = fd > 0 ? rotationAngle1 : rotationAngle1 + 180;

                if (!IF.markedLinearlyDistributedLoads.contains(IF.linearlyDistributedLoads.indexOf(ldl))) {
                    Collections.addAll(this.lines, createStraightArrow(Cx, Cy, cos, sin, Math.abs(fd) / maxForce * maxArrowLength,
                            arrowHeadSize, arrowHeadAngle, rotationAngle2, lineType, lineWidth, lineColor, type + fd + " " + U.kN + "/" + U.m + orientation));
                }
                else {
                    Collections.addAll(this.markedLines, createStraightArrow(Cx, Cy, cos, sin, Math.abs(fd) / maxForce * maxArrowLength,
                            arrowHeadSize, arrowHeadAngle, rotationAngle2, lineType, lineWidth, lineColor, type + fd + " " + U.kN + "/" + U.m + orientation));
                }

                double Xe = - (Math.abs(fd) / maxForce * maxArrowLength) * sin + Cx;
                double Ye = (Math.abs(fd) / maxForce * maxArrowLength) * cos + Cy;

                if (i == 0) {
                    x = rotatePoint(Cx, Cy, Xe, Ye, rotationAngle2, true)[0];
                    y = rotatePoint(Cx, Cy, Xe, Ye, rotationAngle2, true)[1];
                }
                else {
                    Data Dx = new Data();
                    Data Dy = new Data();
                    Dx.add(x);
                    Dy.add(y);
                    x = rotatePoint(Cx, Cy, Xe, Ye, rotationAngle2, true)[0];
                    y = rotatePoint(Cx, Cy, Xe, Ye, rotationAngle2, true)[1];
                    Dx.add(x);
                    Dy.add(y);

                    LineChart lc = new LineChart(Dx, Dy);
                    lc = createPointSymbol(lc, PointSymbolType.CIRCLE, 0, 0, true);
                    if (!IF.markedLinearlyDistributedLoads.contains(IF.linearlyDistributedLoads.indexOf(ldl))) {
                        this.lines.add(createLine(lc, lineType, lineWidth, lineColor));
                    }
                    else {
                        this.markedLines.add(createLine(lc, lineType, lineWidth, lineColor));
                    }
                }
            }
        }
    }

    private void reactionToLines () {
        Matrix R = OF.reactions;
        for (int i = 0; i < R.row; i++) {

            LineStyle.Type lineType = GS.reactionLineStyleType;
            int lineWidth = GS.reactionLineWidth;
            String lineColor = GS.reactionLineColor;
            double straightArrowLength = GS.reactionStraightArrowLength * IF.PH.doubleValue() / 10;
            double straightArrowHeadSize = GS.reactionStraightArrowHeadSize;
            double straightArrowHeadAngle = GS.reactionStraightArrowHeadAngle;
            double roundArrowStartAngle = GS.reactionRoundArrowStartAngle;
            double roundArrowEndAngle = GS.reactionRoundArrowEndAngle;
            double roundArrowRadius = GS.reactionRoundArrowRadius * IF.PH.doubleValue() / 10;
            int roundArrowPartitionSize = GS.reactionRoundArrowPartitionSize;
            double roundArrowHeadSize = GS.reactionRoundArrowHeadSize;
            double roundArrowHeadInnerAngle = GS.reactionRoundArrowHeadInnerAngle;
            double roundArrowHeadOuterAngle = GS.reactionRoundArrowHeadOuterAngle;

            if (R.data[i][2].intValue() == 1 && U.round(R.data[i][3]).doubleValue() != 0) {
                double rotationAngle = R.data[i][3].compareTo(BigDecimal.ZERO) > 0 ? 90 : 270;

                Collections.addAll(this.lines, createStraightArrow(U.round(R.data[i][0]).doubleValue(), U.round(R.data[i][1]).doubleValue(), 1, 0,
                        straightArrowLength, straightArrowHeadSize, straightArrowHeadAngle, rotationAngle, lineType, lineWidth, lineColor,
                        "Reaction Force\nRx = " + U.round2(R.data[i][3]).toPlainString() + " " + U.kN));
            }
            if (R.data[i][2].intValue() == 2 && U.round(R.data[i][3]).doubleValue() != 0) {
                double rotationAngle = R.data[i][3].compareTo(BigDecimal.ZERO) > 0 ? 180 : 0;

                Collections.addAll(this.lines, createStraightArrow(U.round(R.data[i][0]).doubleValue(), U.round(R.data[i][1]).doubleValue(), 1, 0,
                        straightArrowLength, straightArrowHeadSize, straightArrowHeadAngle, rotationAngle, lineType, lineWidth, lineColor,
                        "Reaction Force\nRy = " + U.round2(R.data[i][3]).toPlainString() + " " + U.kN));
            }
            if (R.data[i][2].intValue() == 3 && U.round(R.data[i][3]).doubleValue() != 0) {

                Collections.addAll(this.lines, createRoundArrow(U.round(R.data[i][0]).doubleValue(), U.round(R.data[i][1]).doubleValue(), roundArrowStartAngle, roundArrowEndAngle,
                        roundArrowPartitionSize, roundArrowRadius, roundArrowHeadSize, roundArrowHeadInnerAngle, roundArrowHeadOuterAngle, R.data[i][3].compareTo(BigDecimal.ZERO) > 0,
                        lineType, lineWidth, lineColor, "Reaction Force\nRz = " + U.round2(R.data[i][3]).toPlainString() + " " + U.kNm));
            }
        }
    }


    private void internalForcesToLines (String lineColor, int LT) {

        LineStyle.Type lineType = GS.internalForceLineStyleType;
        int lineWidth = GS.internalForceLineWidth;
        PointSymbolType symbolType = GS.internalForcePointSymbolType;
        double maxLineLength = GS.internalForceLineLength * IF.PH.doubleValue() / 10;

        boolean hideSymbol1 = false;
        boolean hideSymbol2 = true;
        String[] type = {"Normal Force\nN = ", "Shear Force\nV = ", "Bending Moment\nM = "};
        String[] unit = {" " + U.kN, " " + U.kN, " " + U.kNm};

        int forceIndex = LT + 1;
        double maxForce = 0;

        for (Matrix M : OF.forcesAndDisplacements) {
            for (int i = 0; i < M.row; i++) {
                if (M.data[i][forceIndex].abs().doubleValue() > maxForce){
                    maxForce = M.data[i][forceIndex].abs().doubleValue();
                }
            }
        }

        if (maxForce < GS.internalForceMinimumValue) {
            maxForce = 0;
        }

        for (FrameMember fm : IF.frameMembers) {

            if (IF.undisplayedFrameMembers.contains(fm.ID - 1)) {
                continue;
            }

            double cos = fm.cos.doubleValue();
            double sin = fm.sin.doubleValue();
            double rotationAngle;
            if (sin > 0 && cos == 0) {
                rotationAngle = 90;
            }
            else if (sin < 0 && cos == 0) {
                rotationAngle = 270;
            }
            else {
                if (cos< 0) {
                    rotationAngle = Math.toDegrees(Math.atan(sin / cos)) + 180;
                }
                else {
                    rotationAngle =  Math.toDegrees(Math.atan(sin / cos));
                }
            }

            Matrix M = OF.forcesAndDisplacements.get(fm.ID - 1);

            for (int i = 0; i < M.row; i += 2) {

                Data Dx1 = new Data();
                Data Dx2 = new Data();
                Data Dx3 = new Data();
                Data Dy1 = new Data();
                Data Dy2 = new Data();
                Data Dy3 = new Data();

                double x1, x2, x3, x4, y1, y2, y3, y4, F12, F34, l12, l34;

                x1 = U.round(M.data[i][0]).doubleValue();
                y1 = U.round(M.data[i][1]).doubleValue();
                F12 = U.round2(M.data[i][forceIndex]).doubleValue();
                l12 = maxForce == 0 ? 0 : Math.abs(F12) / maxForce * maxLineLength;

                x3 = U.round(M.data[i + 1][0]).doubleValue();
                y3 = U.round(M.data[i + 1][1]).doubleValue();
                F34 = U.round2(M.data[i + 1][forceIndex]).doubleValue();
                l34 = maxForce == 0 ? 0 : Math.abs(F34) / maxForce * maxLineLength;

                if (OF.isReverseMoment && LT == 3) {
                    x2 = U.round(rotatePoint(x1, y1, x1, y1 + l12, F12 > 0 ? rotationAngle + 180: rotationAngle, true)[0]);
                    y2 = U.round(rotatePoint(x1, y1, x1, y1 + l12, F12 > 0 ? rotationAngle + 180: rotationAngle, true)[1]);

                    x4 = U.round(rotatePoint(x3, y3, x3, y3 + l34, F34 > 0 ? rotationAngle + 180: rotationAngle, true)[0]);
                    y4 = U.round(rotatePoint(x3, y3, x3, y3 + l34, F34 > 0 ? rotationAngle + 180: rotationAngle, true)[1]);
                }
                else {
                    x2 = U.round(rotatePoint(x1, y1, x1, y1 + l12, F12 > 0 ? rotationAngle : rotationAngle + 180, true)[0]);
                    y2 = U.round(rotatePoint(x1, y1, x1, y1 + l12, F12 > 0 ? rotationAngle : rotationAngle + 180, true)[1]);

                    x4 = U.round(rotatePoint(x3, y3, x3, y3 + l34, F34 > 0 ? rotationAngle : rotationAngle + 180, true)[0]);
                    y4 = U.round(rotatePoint(x3, y3, x3, y3 + l34, F34 > 0 ? rotationAngle : rotationAngle + 180, true)[1]);
                }

                Dx1.add(x1);
                Dx1.add(x2);
                Dy1.add(y1);
                Dy1.add(y2);

                Dx2.add(x3);
                Dx2.add(x4);
                Dy2.add(y3);
                Dy2.add(y4);

                Dx3.add(x2);
                Dx3.add(x4);
                Dy3.add(y2);
                Dy3.add(y4);

                LineChart lc1 = new LineChart(Dx1, Dy1);
                LineChart lc2 = new LineChart(Dx2, Dy2);
                LineChart lc3 = new LineChart(Dx3, Dy3);

                lc1 = createLine(lc1, lineType, lineWidth, lineColor);
                lc1 = createPointSymbol(lc1, symbolType, lineWidth, lineWidth, hideSymbol1);
                lc1 = setArrowTooltip(lc1, x1, y1, type[LT - 1] + F12 + unit[LT - 1]);

                lc2 = createLine(lc2, lineType, lineWidth, lineColor);
                lc2 = createPointSymbol(lc2, symbolType, lineWidth, lineWidth, hideSymbol1);
                lc2 = setArrowTooltip(lc2, x3, y3, type[LT - 1] + F34 + unit[LT - 1]);

                lc3 = createLine(lc3, lineType, lineWidth, lineColor);
                lc3 = createPointSymbol(lc3, symbolType, lineWidth, lineWidth, hideSymbol2);

                this.lines.add(lc1);
                this.lines.add(lc2);
                this.lines.add(lc3);
            }
        }
    }

    private void normalToLines () {
        internalForcesToLines(GS.normalLineColor, 1);
    }

    private void shearToLines () {
        internalForcesToLines(GS.shearLineColor, 2);
    }

    private void momentToLines () {
        internalForcesToLines(GS.momentLineColor, 3);
    }

    private void displacementToLines () {

        LineStyle.Type lineType = GS.displacementLineStyleType;
        int lineWidth = GS.displacementLineWidth;
        String lineColor = GS.displacementLineColor;
        PointSymbolType symbolType = GS.displacementPointSymbolType;
        double maxLineLength = GS.displacementLineLength * IF.PH.doubleValue() / 10;

        boolean hideSymbol1 = false;
        boolean hideSymbol2 = true;

        double maxDisplacement = 0;

        for (Matrix M : OF.forcesAndDisplacements) {
            for (int i = 0; i < M.row; i++) {
                if (M.data[i][5].abs().doubleValue() > maxDisplacement){
                    maxDisplacement = M.data[i][5].abs().doubleValue();
                }
                if (M.data[i][6].abs().doubleValue() > maxDisplacement){
                    maxDisplacement = M.data[i][6].abs().doubleValue();
                }
            }
        }

        for (FrameMember fm : IF.frameMembers) {

            if (IF.undisplayedFrameMembers.contains(fm.ID - 1)) {
                continue;
            }

            Matrix M = OF.forcesAndDisplacements.get(fm.ID - 1);

            for (int i = 0; i < M.row; i += 2) {

                Data Dx1 = new Data();
                Data Dx2 = new Data();
                Data Dx3 = new Data();
                Data Dy1 = new Data();
                Data Dy2 = new Data();
                Data Dy3 = new Data();

                double x1 = U.round(M.data[i][0]).doubleValue();
                double y1 = U.round(M.data[i][1]).doubleValue();
                double x3 = U.round(M.data[i + 1][0]).doubleValue();
                double y3 = U.round(M.data[i + 1][1]).doubleValue();
                double x2, x4, y2, y4;

                if (maxDisplacement != 0) {
                    x2 = U.round2(M.data[i][0].doubleValue() + M.data[i][5].doubleValue() / maxDisplacement * maxLineLength);
                    y2 = U.round2(M.data[i][1].doubleValue() + M.data[i][6].doubleValue() / maxDisplacement * maxLineLength);
                    x4 = U.round2(M.data[i + 1][0].doubleValue() + M.data[i + 1][5].doubleValue() / maxDisplacement * maxLineLength);
                    y4 = U.round2(M.data[i + 1][1].doubleValue() + M.data[i + 1][6].doubleValue() / maxDisplacement * maxLineLength);
                }
                else {
                    x2 = U.round2(M.data[i][0].doubleValue());
                    y2 = U.round2(M.data[i][1].doubleValue());
                    x4 = U.round2(M.data[i + 1][0].doubleValue());
                    y4 = U.round2(M.data[i + 1][1].doubleValue());
                }

                Dx1.add(x2);
                Dx1.add(x2);
                Dy1.add(y2);
                Dy1.add(y2);

                Dx2.add(x4);
                Dx2.add(x4);
                Dy2.add(y4);
                Dy2.add(y4);

                Dx3.add(x2);
                Dx3.add(x4);
                Dy3.add(y2);
                Dy3.add(y4);

                LineChart lc1 = new LineChart(Dx1, Dy1);
                LineChart lc2 = new LineChart(Dx2, Dy2);
                LineChart lc3 = new LineChart(Dx3, Dy3);

                lc1 = createLine(lc1, lineType, lineWidth, lineColor);
                lc1 = createPointSymbol(lc1, symbolType, lineWidth, lineWidth, hideSymbol1);

                lc2 = createLine(lc2, lineType, lineWidth, lineColor);
                lc2 = createPointSymbol(lc2, symbolType, lineWidth, lineWidth, hideSymbol1);

                lc3 = createLine(lc3, lineType, lineWidth, lineColor);
                lc3 = createPointSymbol(lc3, symbolType, lineWidth, lineWidth, hideSymbol2);

                if (!IF.isDefaultCS) {
                    lc1 = setDisplacementTooltip(lc1, M.data[i][5], M.data[i][6], M.data[i][7], x1, y1);
                    lc2 = setDisplacementTooltip(lc2, M.data[i + 1][5], M.data[i + 1][6], M.data[i + 1][7], x3, y3);
                }
                else {
                    lc1 = setArrowTooltip(lc1, x1, y1, "Cannot display the value of joint\ndisplacements when the default\ncross-section is used.");
                    lc2 = setArrowTooltip(lc2, x3, y3, "Cannot display the value of joint\ndisplacements when the default\ncross-section is used.");
                }

                this.lines.add(lc1);
                this.lines.add(lc2);
                this.lines.add(lc3);
            }
        }
    }

    private void setAxisStyle () {
        xAxis.setMax(IF.PW.multiply(new BigDecimal("1.1")).stripTrailingZeros().doubleValue());
        xAxis.setMin(IF.PW.multiply(new BigDecimal("0.1")).negate().stripTrailingZeros().doubleValue());
        xAxis.setDivisions(10);
        yAxis.setMax(IF.PH.multiply(new BigDecimal("1.1")).stripTrailingZeros().doubleValue());
        yAxis.setMin(IF.PH.multiply(new BigDecimal("0.1")).negate().stripTrailingZeros().doubleValue());
        yAxis.setDivisions(10);
    }

    private void addToolBox () {
//        Toolbox tb = new Toolbox();
//        Toolbox.Download dl = new Toolbox.Download();
//        Position p = new Position();
//        p.alignTop();
//        p.justifyCenter();
//        tb.setPosition(p);
//        tb.showVertically();
//        tb.addButton(dl);
//        this.add(tb);

        DataZoom zoomX = new DataZoom(rc, xAxis);
        DataZoom zoomY = new DataZoom(rc, yAxis);

//        Position px = new Position();
//        px.alignTop();
//        px.justifyCenter();
//        zoomX.setPosition(px);
//        Position py = new Position();
//        py.alignCenter();
//        py.justifyRight();
//        zoomY.setPosition(py);

        this.add(zoomX, zoomY);
    }

    private void plot () {
        for (LineChart lc : lines) {
            lc.plotOn(this.rc);
            this.add(lc);
        }
        for (LineChart lc : markedLines) {
            lc.plotOn(this.rc);
            this.add(lc);
        }
        this.disableDefaultLegend();
    }
}
