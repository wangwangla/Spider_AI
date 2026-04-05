package com.tony.balatro.event;


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class EventManager {
    private Map<String, Queue<Event>> eventQueues;
    private boolean paused;
    private float queueTimer;
    //多久执行一次
    private final float QUEUE_DT = 1f / 60f;

    public EventManager() {
        this.eventQueues = new HashMap<>();
        this.paused = false;
        this.queueTimer = 0;

        // 初始化队列
        eventQueues.put("unlock", new LinkedList<>());
        eventQueues.put("base", new LinkedList<>());
        eventQueues.put("tutorial", new LinkedList<>());
        eventQueues.put("achievement", new LinkedList<>());
        eventQueues.put("other", new LinkedList<>());
    }

    public void addEvent(Event event) {
        addEvent("base", event);
    }

    public void addEvent(String queueName, Event event) {
        //将事件放入道一个队列中
        if (eventQueues.containsKey(queueName)) {
            eventQueues.get(queueName).offer(event);
        }
    }

    public void update(float dt) {
        if (paused) return;
        queueTimer += dt;
        if (queueTimer >= QUEUE_DT) {
            queueTimer -= QUEUE_DT;
            processEventQueues();
        }
    }

    private void processEventQueues() {
        boolean blocked = false;

        for (String queueName : eventQueues.keySet()) {
            Queue<Event> queue = eventQueues.get(queueName);
            Iterator<Event> iterator = queue.iterator();
            while (iterator.hasNext()) {
                Event event = iterator.next();
                // 检查是否被阻止
                if (blocked && event.blockable) {
                    continue;
                }
                event.update(QUEUE_DT);
                if (event.handle()) {
                    iterator.remove();
                    if (event.blocking) {
                        blocked = true;
                    }
                } else if (event.blocking) {
                    blocked = true;
                }
            }
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public int getTotalEventCount() {
        return eventQueues.values().stream()
                .mapToInt(Queue::size)
                .sum();
    }

    public void clear() {
        eventQueues.values().forEach(Queue::clear);
    }
}

