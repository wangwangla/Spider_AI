package com.tony.balatro.event;


public class Easing {

    /**
     * 线性插值
     */
    public static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    /**
     * 弹性缓动 (Elastic Out)
     */
    public static float elasticOut(float progress) {
        if (progress == 0) return 0;
        if (progress == 1) return 1;

        float p = 1f - progress;
        return 1f - (float)(Math.pow(2, 10 * p - 10) *
                Math.sin((p * 10 - 10.75) * (2 * Math.PI / 3)));
    }

    /**
     * 二次缓动 (Quad Out)
     */
    public static float quadOut(float progress) {
        return progress * progress;
    }

    /**
     * 立方缓动 (Cubic Out)
     */
    public static float cubicOut(float progress) {
        float p = 1f - progress;
        return 1f - p * p * p;
    }

    /**
     * 圆形缓动 (Circular Out)
     */
    public static float circularOut(float progress) {
        return (float)Math.sqrt(1f - (1f - progress) * (1f - progress));
    }

    /**
     * 获取缓动函数
     */
    public static float ease(float progress, String easeType) {
        switch(easeType.toLowerCase()) {
            case "elastic":
                return elasticOut(progress);
            case "quad":
                return quadOut(progress);
            case "cubic":
                return cubicOut(progress);
            case "circular":
                return circularOut(progress);
            default:
                return progress;
        }
    }
}