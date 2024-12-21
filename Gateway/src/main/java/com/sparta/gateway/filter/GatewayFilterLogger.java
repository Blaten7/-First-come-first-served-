//package com.sparta.gateway.filter;
//
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
//import org.springframework.stereotype.Component;
//import java.util.List;
//
//@Component
//public class GatewayFilterLogger {
//
//    @Autowired
//    private List<GatewayFilterFactory> gatewayFilterFactories;
//
//    @PostConstruct
//    public void logAvailableFilters() {
//        gatewayFilterFactories.forEach(factory -> System.out.println("Available GatewayFilterFactory: " + factory.getClass().getSimpleName()));
//    }
//}
