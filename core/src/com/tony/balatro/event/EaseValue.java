package com.tony.balatro.event;

public class EaseValue {

    /**
     * 缓动数值变化
     * @param obj 目标对象
     * @param property 属性名（必须是public float）
     * @param delta 要改变的量
     * @param delay 缓动时长
     * @param easeType 缓动类型
     */
    public static void ease(EventManager eventManager,
                            Object obj,
                            String property,
                            float delta,
                            float delay,
                            String easeType) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(property);
            field.setAccessible(true);
            float startValue = field.getFloat(obj);
            float endValue = startValue + delta;

            Event event = new Event.EventBuilder()
                    .trigger(EventTrigger.EASE)
                    .delay(delay)
                    .ease(obj, property, endValue, easeType)
                    .func(() -> true)
                    .build();

            event.easeFrom = startValue;
            eventManager.addEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
