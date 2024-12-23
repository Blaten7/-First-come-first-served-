package com.sparta.orderservice.scheduler;

import com.sparta.orderservice.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class OrderScheduler {

    private final OrderRepository orderRepository;

    public OrderScheduler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // 사용자가 주문시 1시간마다 체크하여 24시간이 경과 되면 배송 준비중 -> 배송중으로 상태값 변경
    @Scheduled(fixedRate = 3600000) // 1시간(3600000ms)마다 실행
    public void updateOrderStatus() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thresholdTime = now.minusHours(24);

        orderRepository.updateOrderStatusIfReady(thresholdTime);
    }

    // "배송중" → "배송 완료" 상태 변경
    @Scheduled(fixedRate = 3600000) // 1시간 간격 실행
    public void updateOrderStatusToCompleted() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thresholdTime = now.minusHours(24);

        orderRepository.updateOrderStatusToCompleted(thresholdTime);
    }
}
