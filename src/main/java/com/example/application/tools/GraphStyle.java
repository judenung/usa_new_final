package com.example.application.tools;

import com.storedobject.chart.LineStyle;
import com.storedobject.chart.PointSymbolType;

public class GraphStyle {

    public LineStyle.Type frameMemberLineStyleType = LineStyle.Type.SOLID;
    public int frameMemberLineWidth = 3;
    public String frameMemberLineColor = "#FFFFFF";
    public PointSymbolType frameMemberPointSymbolType = PointSymbolType.CIRCLE;
    public boolean frameMemberHideSymbol = true;
    public double frameMemberDistantToDashedLine = 0.1;

    public LineStyle.Type frameMemberDashedLineStyleType = LineStyle.Type.DASHED;
    public int frameMemberDashedLineWidth = 1;
    public String frameMemberDashedLineColor = "#000000";
    public PointSymbolType frameMemberDashedLinePointSymbolType = PointSymbolType.CIRCLE;
    public boolean frameMemberDashedLineHideSymbol = true;

    public PointSymbolType[] supportPointSymbolType = {
            PointSymbolType.RECTANGLE,
            PointSymbolType.ARROW,
            PointSymbolType.TRIANGLE,
            PointSymbolType.TRIANGLE,
            PointSymbolType.TRIANGLE};
    public int[][] supportPointSymbolSize = {
            {12, 12},
            {14, 14},
            {16, 16},
            {16, 16},
            {16, 16}};
    public String supportLineColor = "#00FF00";
    public String supportSettlementLineColor = "#808080";

    public String hingeLineColor = "#00FF00";
    public double hingeDistantFromEnd = 0.3;

    public int jointLineWidth = 5;
    public String jointLineColor = "#00FF00";
    public PointSymbolType jointPointSymbolType = PointSymbolType.CIRCLE;
    public boolean jointHideSymbol = false;

    public LineStyle.Type temperatureChangeLineStyleType = LineStyle.Type.SOLID;
    public int temperatureChangeLineWidth = 3;
    public String temperatureChangeLineColor = "#FF0000";
    public double temperatureChangeDistantFromEnd = 0.3;
    public double temperatureChangeArrowHeadSize = 0.2;
    public double temperatureChangeArrowHeadAngle = 45;

    public LineStyle.Type fabricationErrorLineStyleType = LineStyle.Type.SOLID;
    public int fabricationErrorLineWidth = 3;
    public String fabricationErrorLineColor = "#808080";
    public double fabricationErrorDistantFromEnd = 0.4;
    public double fabricationErrorArrowHeadSize = 0.2;
    public double fabricationErrorArrowHeadAngle = 45;

    public LineStyle.Type jointLoadLineStyleType = LineStyle.Type.SOLID;
    public int jointLoadLineWidth = 3;
    public String jointLoadLineColor = "#0096FF";
    public double jointLoadStraightArrowLength = 1.0;
    public double jointLoadStraightArrowHeadSize = 0.3;
    public double jointLoadStraightArrowHeadAngle = 25;
    public double jointLoadRoundArrowStartAngle = -50;
    public double jointLoadRoundArrowEndAngle = 140;
    public double jointLoadRoundArrowRadius = 0.5;
    public int jointLoadRoundArrowPartitionSize = 20;
    public double jointLoadRoundArrowHeadSize = 0.15;
    public double jointLoadRoundArrowHeadInnerAngle = 40;
    public double jointLoadRoundArrowHeadOuterAngle = 20;

    public LineStyle.Type pointLoadLineStyleType = LineStyle.Type.SOLID;
    public int pointLoadLineWidth = 3;
    public String pointLoadLineColor = "#0096FF";
    public double pointLoadStraightArrowLength = 1.0;
    public double pointLoadStraightArrowHeadSize = 0.3;
    public double pointLoadStraightArrowHeadAngle = 25;
    public double pointLoadRoundArrowStartAngle = -50;
    public double pointLoadRoundArrowEndAngle = 140;
    public double pointLoadRoundArrowRadius = 0.5;
    public int pointLoadRoundArrowPartitionSize = 20;
    public double pointLoadRoundArrowHeadSize = 0.15;
    public double pointLoadRoundArrowHeadInnerAngle = 40;
    public double pointLoadRoundArrowHeadOuterAngle = 20;

    public LineStyle.Type linearlyDistributedLoadLineStyleType = LineStyle.Type.SOLID;
    public int linearlyDistributedLoadLineWidth = 3;
    public String linearlyDistributedLoadLineColor = "#0096FF";
    public double linearlyDistributedLoadStraightArrowLength = 1.0;
    public double linearlyDistributedLoadStraightArrowHeadSize = 0.3;
    public double linearlyDistributedLoadStraightArrowHeadAngle = 25;

    public LineStyle.Type reactionLineStyleType = LineStyle.Type.SOLID;
    public int reactionLineWidth = 3;
    public String reactionLineColor = "#FF0000";
    public double reactionStraightArrowLength = 1.0;
    public double reactionStraightArrowHeadSize = 0.3;
    public double reactionStraightArrowHeadAngle = 25;
    public double reactionRoundArrowStartAngle = 130;
    public double reactionRoundArrowEndAngle = 320;
    public double reactionRoundArrowRadius = 0.5;
    public int reactionRoundArrowPartitionSize = 20;
    public double reactionRoundArrowHeadSize = 0.15;
    public double reactionRoundArrowHeadInnerAngle = 40;
    public double reactionRoundArrowHeadOuterAngle = 20;

    public LineStyle.Type internalForceLineStyleType = LineStyle.Type.SOLID;
    public int internalForceLineWidth = 3;
    public String normalLineColor = "#FF69B4";
    public String shearLineColor = "#964B00";
    public String momentLineColor = "#6F2DA8";
    public PointSymbolType internalForcePointSymbolType = PointSymbolType.CIRCLE;
    public double internalForceLineLength = 1.0;
    public double internalForceMinimumValue = 0.1;

    public LineStyle.Type displacementLineStyleType = LineStyle.Type.SOLID;
    public int displacementLineWidth = 3;
    public String displacementLineColor = "#61FFFF";
    public PointSymbolType displacementPointSymbolType = PointSymbolType.CIRCLE;
    public double displacementLineLength = 0.75;

    public String markedColor = "FFA500";
    public String darkModeBackGroundColor = "#233348";

    public void setDarkMode (boolean isDark) {
        if (isDark) {
            frameMemberLineColor = "#FFFFFF";
            frameMemberDashedLineColor = "#FFFFFF";
        }
        else {
            frameMemberLineColor = "#000000";
            frameMemberDashedLineColor = "#000000";
        }
    }
}
