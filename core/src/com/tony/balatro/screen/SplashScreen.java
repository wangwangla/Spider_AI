//package com.tony.balatro.screen;
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.graphics.Color;
//import com.kw.gdx.BaseGame;
//import com.kw.gdx.screen.BaseScreen;
//import com.tony.balatro.CardGroup;
//import com.tony.balatro.event.Event;
//import com.tony.balatro.event.EventManager;
//import com.tony.balatro.event.EventTrigger;
//
//public class SplashScreen extends PostProcessorScreen {
//    private EventManager eventManager;
//    private CardGroup card;
//    public SplashScreen(BaseGame baseGame,EventManager eventManager) {
//        super(baseGame);
//        this.eventManager = eventManager;
//        initSplashAnimations();
//    }
//
//    private void initSplashAnimations() {
//        // 立即执行：设置着色器和初始状态
//        eventManager.addEvent(new Event.EventBuilder()
//                .trigger(EventTrigger.IMMEDIATE)
//                .func(() -> {
//                    // 这里初始化背景着色器等
//                    return true;
//                })
//                .build());
//
//        // 延迟0.2秒：小丑卡进场
//        eventManager.addEvent(new Event.EventBuilder()
//                .trigger(EventTrigger.AFTER)
//                .delay(0.2f)
//                .blocking(false)
//                .func(() -> {
////                    splashCard = new Card(
////                            Gdx.graphics.getWidth() / 2f - 60,
////                            Gdx.graphics.getHeight() / 2f - 90,
////                            120, 180,
////                            jokerCardTexture
////                    );
//                    return true;
//                })
//                .build());
//        // 延迟1.8秒：卡牌溶解
//        eventManager.addEvent(new Event.EventBuilder()
//                .trigger(EventTrigger.AFTER)
//                .delay(1.8f)
//                .blocking(false)
//                .func(() -> {
////                    if (splashCard != null) {
////                        splashCard.startDissolve(new Color(Color.WHITE), 12);
////                    }
//                    return true;
//                })
//                .build());
//
//        // 创建200张卡片的漩涡效果
//        createVortexAnimation();
//
//        // 延迟2秒后切换到主菜单
//        eventManager.addEvent(new Event.EventBuilder()
//                .trigger(EventTrigger.AFTER)
//                .delay(4.6f)
//                .func(() -> {
//                    // 切换到主菜单
//                    return true;
//                })
//                .build());
//    }
//
//    private void createVortexAnimation() {
//        float tempDelay = 3f;
//        float centerX = Gdx.graphics.getWidth() / 2f;
//        float centerY = Gdx.graphics.getHeight() / 2f;
//
//        for (int i = 1; i <= 200; i++) {
//            final int index = i;
//            final float delay = tempDelay;
//
//            eventManager.addEvent(new Event.EventBuilder()
//                    .trigger(EventTrigger.AFTER)
//                    .delay(delay)
//                    .blocking(false)
//                    .func(() -> {
//                        createVortexCard(index, centerX, centerY);
//                        return true;
//                    })
//                    .build());
//
//            tempDelay += Math.max(1f / index, Math.max(0.2f * (170 - index) / 500, 0.016f));
//        }
//    }
//
//    private void createVortexCard(int index, float centerX, float centerY) {
//        double angle = Math.random() * 2 * Math.PI;
//        float cardSize = (float)((1.5f * (Math.random() + 1)) * (2f - index / 300f));
//
//        float cardX = (float)(centerX + (18 + cardSize) * Math.sin(angle) - 60 * cardSize / 2f);
//        float cardY = (float)(centerY + (18 + cardSize) * Math.cos(angle) - 90 * cardSize / 2f);
//
////        Card card = new Card(cardX, cardY, 120 * cardSize, 180 * cardSize,
////                getRandomCardTexture());
////
////        vortexCards.add(card);
//
//        float speed = Math.max(2f - index * 0.005f, 0.001f);
//        float targetX = centerX - 60 * cardSize / 2f;
//        float targetY = centerY - 90 * cardSize / 2f;
//
//        // 使用ease动画
//        easeCardToCenter(card, targetX, targetY, cardSize, speed);
//    }
//
//    private void easeCardToCenter(CardGroup card, float targetX, float targetY,
//                                  float targetScale, float speed) {
//        // 创建ease事件来移动卡片
//        float duration = 1f * speed;
//
//        // X轴缓动
//        eventManager.addEvent(new Event.EventBuilder()
//                .trigger(EventTrigger.EASE)
//                .delay(duration)
//                .easeType("linear")
//                .func(() -> true)
//                .build());
//
//        // Y轴缓动
//        eventManager.addEvent(new Event.EventBuilder()
//                .trigger(EventTrigger.EASE)
//                .delay(duration)
//                .easeType("linear")
//                .func(() -> true)
//                .build());
//    }
//
//    @Override
//    public void render(float delta) {
//        eventManager.update(delta);
//        super.render(delta);
//    }
//}
