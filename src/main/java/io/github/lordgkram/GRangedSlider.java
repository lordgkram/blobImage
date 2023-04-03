package io.github.lordgkram;

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

public class GRangedSlider extends JPanel {

    private JSlider minS;
    private JSlider maxS;

    private int minV;
    private int maxV;

    private ArrayList<ChangeListener> cl;
    
    public GRangedSlider(int min, int max) {
        cl = new ArrayList<>();
        minS = new JSlider(min, max, min);
        maxS = new JSlider(min, max, max);
        this.minV = min;
        this.maxV = max;
        minS.addChangeListener((e) -> {
            int i = minS.getValue();
            int a = maxS.getValue();
            if(i > a) {
                maxS.setValue(i);
                maxV = i;
            }
            minV = i;
            cl.forEach(c -> c.stateChanged(null));
        });
        maxS.addChangeListener((e) -> {
            int i = minS.getValue();
            int a = maxS.getValue();
            if(a < i) {
                minS.setValue(a);
                minV = a;
            }
            maxV = a;
            cl.forEach(c -> c.stateChanged(null));
        });
        setLayout(new GridLayout(2, 1));
        add(minS);
        add(maxS);
    }

    public int getMinValue() {
        return minV;
    }

    public int getMaxValue() {
        return maxV;
    }

    @Override
    public void setEnabled(boolean v) {
        minS.setEnabled(v);
        maxS.setEnabled(v);
    }

    public void addChangeListener(ChangeListener l) {
        cl.add(l);
    }
 
}
