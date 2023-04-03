package io.github.lordgkram.blobimg;

import java.awt.GridLayout;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

public class GausBrushAxisPannel extends JPanel {

    private int gausSize = 5;
    private double gausAmp = 1;
    private double gausWidth = Math.sqrt(.5);
    private boolean gausClamp = false;
    private float gaus[];
    private List<ChangeListener> changeListeners;

    public GausBrushAxisPannel() {
        super(new GridLayout(4, 2));
        changeListeners = new ArrayList<>();

        // size
        JSpinner gausSizeSpinner = new JSpinner(new SpinnerNumberModel(gausSize, 1, 50, 1));
        gausSizeSpinner.addChangeListener((ev) -> {
            int val = (int) gausSizeSpinner.getValue();
            this.gausSize = val;
            updateGaus();
        });
        gausSizeSpinner.addMouseWheelListener((mwe) -> {
            if(mwe.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                int val = (int) mwe.getPreciseWheelRotation();
                int cabs = Math.abs(val);
                for(int i = 0; i < cabs; i++) {
                    Object next = val < 0 ? gausSizeSpinner.getModel().getNextValue() : gausSizeSpinner.getModel().getPreviousValue();
                    if(next != null) gausSizeSpinner.getModel().setValue(next);
                }
            }
        });

        // amp
        JSpinner gausAmpSpinner = new JSpinner(new SpinnerNumberModel(gausAmp, 0.25, 50, 0.1));
        gausAmpSpinner.addChangeListener((ev) -> {
            double val = (double) gausAmpSpinner.getValue();
            gausAmp = val;
            updateGaus();
        });
        gausAmpSpinner.addMouseWheelListener((mwe) -> {
            if(mwe.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                int val = (int) mwe.getPreciseWheelRotation();
                int cabs = Math.abs(val);
                for(int i = 0; i < cabs; i++) {
                    Object next = val < 0 ? gausAmpSpinner.getModel().getNextValue() : gausAmpSpinner.getModel().getPreviousValue();
                    if(next != null) gausAmpSpinner.getModel().setValue(next);
                }
            }
        });

        // width
        JSpinner gausWidthSpinner = new JSpinner(new SpinnerNumberModel(gausWidth, 0.01, 50, 0.1));
        gausWidthSpinner.addChangeListener((ev) -> {
            double val = (double) gausWidthSpinner.getValue();
            gausWidth = val;
            updateGaus();
        });
        gausWidthSpinner.addMouseWheelListener((mwe) -> {
            if(mwe.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                int val = (int) mwe.getPreciseWheelRotation();
                int cabs = Math.abs(val);
                for(int i = 0; i < cabs; i++) {
                    Object next = val < 0 ? gausWidthSpinner.getModel().getNextValue() : gausWidthSpinner.getModel().getPreviousValue();
                    if(next != null) gausWidthSpinner.getModel().setValue(next);
                }
            }
        });

        // clamp
        JCheckBox gausClampBox = new JCheckBox("Pinsel Einklemmen");
        gausClampBox.addActionListener((ev) -> {
            gausClamp = gausClampBox.isSelected();
            updateGaus();
        });

        add(new JLabel("Größe"));
        add(gausSizeSpinner);

        add(new JLabel("Breite"));
        add(gausWidthSpinner);

        add(new JLabel("Höhe"));
        add(gausAmpSpinner);

        add(gausClampBox);
    }

    private void updateGaus() {
        gaus = new float[gausSize];
        double max = 0;
        for(int i = 0; i < gausSize; i++) {
            gaus[i] = (float) (gausAmp * Math.exp(-(i*i/(2*gausWidth*gausWidth))));
            if(gausClamp) gaus[i] = Math.min(gaus[i], 1);
            max += gaus[i];
        }
        for(int i = 0; i < gausSize; i++) {
            gaus[i] /= max;
        }
        for(ChangeListener changeListener : changeListeners) changeListener.stateChanged(null);
    }

    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }

    public float[] getGaus() {
        return gaus;
    }
    
}
