package com.sparta.purchaseservice.component;

import jakarta.annotation.PostConstruct;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PurchaseNotificationListner {

    @Autowired
    private RedissonClient redissonClient;

    @PostConstruct
    public void subscribeToNotifications() {
        RTopic topic = redissonClient.getTopic("payment:notifications");
        topic.addListener(String.class, (channel, message) -> {
            System.out.println("결제 알림 수신: " + message);
        });
    }
}
