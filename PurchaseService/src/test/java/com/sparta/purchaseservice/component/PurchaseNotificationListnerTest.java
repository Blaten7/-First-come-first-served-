package com.sparta.purchaseservice.component;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseNotificationListnerTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RTopic topic;

    @InjectMocks
    private PurchaseNotificationListner purchaseNotificationListner;

    @Test
    @DisplayName("Redis 메시지 수신 및 처리 테스트")
    void messageReceiveTest() {
        // given
        when(redissonClient.getTopic("payment:notifications")).thenReturn(topic);

        // Capture the listener
        ArgumentCaptor<MessageListener<String>> listenerCaptor =
                ArgumentCaptor.forClass(MessageListener.class);

        // when
        purchaseNotificationListner.subscribeToNotifications();

        // then
        verify(topic).addListener(eq(String.class), listenerCaptor.capture());

        // Simulate message reception
        MessageListener<String> listener = listenerCaptor.getValue();

        // Test with System.out capture
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            listener.onMessage("channel", "테스트 메시지");
            assertTrue(outputStream.toString().contains("결제 알림 수신: 테스트 메시지"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    @DisplayName("Redis 토픽 구독 테스트")
    void subscribeToNotificationsTest() {
        // given
        when(redissonClient.getTopic("payment:notifications")).thenReturn(topic);

        // when
        purchaseNotificationListner.subscribeToNotifications();

        // then
        verify(redissonClient).getTopic("payment:notifications");
        verify(topic).addListener(eq(String.class), any());
    }

    @Test
    @DisplayName("PostConstruct 어노테이션 확인")
    void postConstructAnnotationTest() throws NoSuchMethodException {
        // given
        Method method = PurchaseNotificationListner.class
                .getMethod("subscribeToNotifications");

        // when
        PostConstruct annotation = method.getAnnotation(PostConstruct.class);

        // then
        assertNotNull(annotation);
    }
}