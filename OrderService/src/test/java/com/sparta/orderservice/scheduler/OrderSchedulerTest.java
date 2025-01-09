package com.sparta.orderservice.scheduler;

import com.sparta.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSchedulerTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderScheduler orderScheduler;

    @BeforeEach
    void setUp() {
        orderScheduler = new OrderScheduler(orderRepository);
    }

    @Test
    @DisplayName("24시간이 경과된 '배송 준비중' 주문을 '배송중' 상태로 변경")
    void updateOrderStatus_ShouldUpdateStatusToInDelivery() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expectedThresholdTime = now.minusHours(24);

        // when
        orderScheduler.updateOrderStatus();

        // then
        verify(orderRepository, times(1))
                .updateOrderStatusIfReady(argThat(thresholdTime ->
                        thresholdTime.getHour() == expectedThresholdTime.getHour() &&
                                thresholdTime.getMinute() == expectedThresholdTime.getMinute()
                ));
    }

    @Test
    @DisplayName("24시간이 경과된 '배송중' 주문을 '배송 완료' 상태로 변경")
    void updateOrderStatusToCompleted_ShouldUpdateStatusToDelivered() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expectedThresholdTime = now.minusHours(24);

        // when
        orderScheduler.updateOrderStatusToCompleted();

        // then
        verify(orderRepository, times(1))
                .updateOrderStatusToCompleted(argThat(thresholdTime ->
                        thresholdTime.getHour() == expectedThresholdTime.getHour() &&
                                thresholdTime.getMinute() == expectedThresholdTime.getMinute()
                ));
    }

    @Test
    @DisplayName("스케줄러가 정시에 실행되는지 확인")
    void schedulerTiming() {
        // OrderScheduler 클래스의 @Scheduled 어노테이션 확인
        Scheduled scheduleAnnotation = OrderScheduler.class
                .getDeclaredMethods()[0]
                .getAnnotation(Scheduled.class);

        // fixedRate가 1시간(3600000ms)으로 설정되어 있는지 확인
        assert scheduleAnnotation != null;
        assert scheduleAnnotation.fixedRate() == 3600000;
    }
}