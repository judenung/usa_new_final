package com.example.application.tools;

import com.example.application.frontends.*;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

//@VaadinSessionScope
//@PreserveOnRefresh
@SpringComponent
@UIScope
public class MyUI {
    private InputFrame IF;
    private OutputFrame OF;
    private GraphStyle GS;
    private Unit U;

    public BuildView buildView;
    public CrossSectionView crossSectionView;
    public FrameMemberView frameMemberView;
    public SupportView supportView;
    public HingeView hingeView;
    public JointLoadView jointLoadView;
    public PointLoadView pointLoadView;
    public LinearlyDistributedLoadView linearlyDistributedLoadView;
    public TemperatureChangeView temperatureChangeView;
    public FabricationErrorView fabricationErrorView;
    public SupportSettlementView supportSettlementView;
    public SolutionView solutionView;
    public GraphView graphView;

    @Autowired
    public MyUI () {
        this.IF = new InputFrame();
        this.OF = new OutputFrame();
        this.GS = new GraphStyle();
        this.U = new Unit();
    }

    public InputFrame getInputFrame () {
        return IF;
    }

    public OutputFrame getOutputFrame () {
        return OF;
    }

    public GraphStyle getGraphStyle () {
        return GS;
    }

    public Unit getUnit () {
        return U;
    }

    public void refreshAllGrid () {
        if (crossSectionView != null && buildView.tabs.getSelectedIndex() == 0) {
            crossSectionView.refresh(this);
        }
        if (frameMemberView != null && buildView.tabs.getSelectedIndex() == 1) {
            frameMemberView.refresh(this);
        }
        if (supportView != null && buildView.tabs.getSelectedIndex() == 2) {
            supportView.refresh(this);
        }
        if (hingeView != null && buildView.tabs.getSelectedIndex() == 3) {
            hingeView.refresh(this);
        }
        if (jointLoadView != null && buildView.tabs.getSelectedIndex() == 4) {
            jointLoadView.refresh(this);
        }
        if (pointLoadView != null && buildView.tabs.getSelectedIndex() == 5) {
            pointLoadView.refresh(this);
        }
        if (linearlyDistributedLoadView != null && buildView.tabs.getSelectedIndex() == 66) {
            linearlyDistributedLoadView.refresh(this);
        }
        if (temperatureChangeView != null && buildView.tabs.getSelectedIndex() == 7) {
            temperatureChangeView.refresh(this);
        }
        if (fabricationErrorView != null && buildView.tabs.getSelectedIndex() == 8) {
            fabricationErrorView.refresh(this);
        }
        if (supportSettlementView != null && buildView.tabs.getSelectedIndex() == 9) {
            supportSettlementView.refresh(this);
        }
        if (solutionView != null && buildView.tabs.getSelectedIndex() == 10) {
            solutionView.refresh(this);
        }
    }

    public void refreshTabs() {
        if (buildView != null) {
            buildView.refreshTabs();
        }
    }

}


//SAVE_OPTION.getUI().ifPresent(ui -> ui.navigate(""));
//SAVE_OPTION.getUI().ifPresent(ui -> ui.navigate(UI.getCurrentView()));
//SAVE_OPTION.getUI().get().getPage().reload();
