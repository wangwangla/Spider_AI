package com.tony.balatro.event;


import com.badlogic.gdx.graphics.Color;

public class ColorEasing {
    public static void easeColor(EventManager eventManager,
                                 Color color,
                                 Color targetColor,
                                 float duration) {
        easeColorChannel(eventManager, color, targetColor, duration, ColorChannel.RED);
        easeColorChannel(eventManager, color, targetColor, duration, ColorChannel.GREEN);
        easeColorChannel(eventManager, color, targetColor, duration, ColorChannel.BLUE);
        easeColorChannel(eventManager, color, targetColor, duration, ColorChannel.ALPHA);
    }

    private static void easeColorChannel(EventManager eventManager,
                                         Color color,
                                         Color targetColor,
                                         float duration,
                                         ColorChannel channel) {
        float startValue = 0;
        float endValue = 0;
        String fieldName = "";

        switch(channel) {
            case RED:
                startValue = color.r;
                endValue = targetColor.r;
                fieldName = "r";
                break;
            case GREEN:
                startValue = color.g;
                endValue = targetColor.g;
                fieldName = "g";
                break;
            case BLUE:
                startValue = color.b;
                endValue = targetColor.b;
                fieldName = "b";
                break;
            case ALPHA:
                startValue = color.a;
                endValue = targetColor.a;
                fieldName = "a";
                break;
        }

        final float delta = endValue - startValue;
        final String fFieldName = fieldName;
        final float fStart = startValue;
        final float fEnd = endValue;

        Event event = new Event.EventBuilder()
                .trigger(EventTrigger.EASE)
                .delay(duration)
                .easeType("lerp")
                .func(() -> {
                    try {
                        java.lang.reflect.Field field = Color.class.getDeclaredField(fFieldName);
                        field.setAccessible(true);
                        field.setFloat(color, fEnd);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                })
                .build();

        eventManager.addEvent(event);
    }

    enum ColorChannel {
        RED, GREEN, BLUE, ALPHA
    }
}