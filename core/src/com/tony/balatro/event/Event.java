package com.tony.balatro.event;

import java.util.function.Supplier;

public class Event {
    /**
     * 触发器
    * */
    public EventTrigger trigger;
    /**
     * 打断
    * */
    public boolean blocking;
    public boolean blockable;
    public boolean complete;
    public float delay;
    public float elapsed;
    public String timerName;
    // Ease参数
    public Object refTable;
    public String refValue;
    public Float easeFrom;
    public Float easeTo;
    public String easeType; // "lerp", "elastic", "quad"

    // 回调函数
    private Supplier<Boolean> func;

    public Event(EventBuilder builder) {
        this.trigger = builder.trigger;
        this.blocking = builder.blocking;
        this.blockable = builder.blockable;
        this.complete = false;
        this.delay = builder.delay;
        this.elapsed = 0;
        this.timerName = builder.timerName;
        this.func = builder.func;

        if (trigger == EventTrigger.EASE) {
            this.refTable = builder.refTable;
            this.refValue = builder.refValue;
            this.easeTo = builder.easeTo;
            this.easeType = builder.easeType != null ? builder.easeType : "lerp";
        }
    }

    public void update(float dt) {
        elapsed += dt;
    }

    public boolean handle() {
        //完成
        if (complete) return true;

        switch(trigger) {
            case IMMEDIATE:
                complete = func.get();
                return true;

            case AFTER:
                if (elapsed >= delay) {
                    complete = func.get();
                    return true;
                }
                return false;

            case EASE:
                float progress = Math.min(elapsed / delay, 1f);
                float easeValue = calculateEaseValue(progress);

                // 通过反射设置值
                try {
                    java.lang.reflect.Field field = refTable.getClass().getDeclaredField(refValue);
                    field.setAccessible(true);
                    field.setFloat(refTable, easeFrom + (easeTo - easeFrom) * easeValue);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (progress >= 1f) {
                    complete = true;
                    return true;
                }
                return false;

            case CONDITION:
                complete = func.get();
                return complete;

            case BEFORE:
                boolean funcResult = func.get();
                if (elapsed >= delay) {
                    return true;
                }
                return funcResult;
        }
        return false;
    }

    private float calculateEaseValue(float progress) {
        switch(easeType) {
            case "lerp":
                return progress;
            case "elastic":
                // 弹性缓动
                float p = 1f - progress;
                return 1f - (float)(Math.pow(2, 10 * p - 10) *
                        Math.sin((p * 10 - 10.75) * (2 * Math.PI / 3)));
            case "quad":
                // 二次缓动
                return progress * progress;
            default:
                return progress;
        }
    }

    public static class EventBuilder {
        public EventTrigger trigger = EventTrigger.IMMEDIATE;
        public boolean blocking = true;
        public boolean blockable = true;
        public float delay = 0;
        public String timerName = "TOTAL";
        public Supplier<Boolean> func = () -> true;

        // Ease参数
        public Object refTable;
        public String refValue;
        public Float easeTo;
        public String easeType;

        public EventBuilder trigger(EventTrigger trigger) {
            this.trigger = trigger;
            return this;
        }

        public EventBuilder blocking(boolean blocking) {
            this.blocking = blocking;
            return this;
        }

        public EventBuilder blockable(boolean blockable) {
            this.blockable = blockable;
            return this;
        }

        public EventBuilder delay(float delay) {
            this.delay = delay;
            return this;
        }

        public EventBuilder timerName(String timerName) {
            this.timerName = timerName;
            return this;
        }

        public EventBuilder func(Supplier<Boolean> func) {
            this.func = func;
            return this;
        }

        public EventBuilder ease(Object refTable, String refValue, Float easeTo, String easeType) {
            this.trigger = EventTrigger.EASE;
            this.refTable = refTable;
            this.refValue = refValue;
            this.easeTo = easeTo;
            this.easeType = easeType;
            return this;
        }

        public Event build() {
            return new Event(this);
        }

        public EventBuilder easeType(String linear) {
            return null;
        }
    }
}