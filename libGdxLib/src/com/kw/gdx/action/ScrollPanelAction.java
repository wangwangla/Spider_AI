package com.kw.gdx.action;

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.kw.gdx.scrollpanel.ScrollPane;

public class ScrollPanelAction extends TemporalAction {
    private float start;
    private float end;
    private ScrollPane pane;
    private boolean isV;
    private Runnable runnable;
    private boolean liji;

    public void setLiji(boolean liji) {
        this.liji = liji;
    }

    public ScrollPanelAction(ScrollPane pane){
        this.pane = pane;
    }

    public void setV(boolean v) {
        isV = v;
    }

    public void setStart(float start) {
        this.start = start;
    }

    public void setEnd(float end) {
        this.end = end;
    }

    @Override
    protected void update(float v) {
        float v1 = end - start;
        float vv = v1 * v;
        System.out.println(v);
        if (isV) {
            pane.setScrollY(start + vv);
        }else {
            pane.setScrollX(start + vv);
        }
        if (liji){
            pane.updateVisualScroll();
        }
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    protected void end() {
        super.end();
        if (runnable != null) {
            runnable.run();
        }
    }
}
