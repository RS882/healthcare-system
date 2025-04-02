//package com.healthcare.api_gateway.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilter;
//import org.springframework.web.server.WebFilterChain;
//
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public WebFilter corsFilter() {
//        return (ServerWebExchange exchange, WebFilterChain chain) -> {
//            var request = exchange.getRequest();
//            var response = exchange.getResponse();
//
//            var headers = response.getHeaders();
//            var origin = request.getHeaders().getOrigin();
//
//            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin != null ? origin : "*");
//            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
//            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
//            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
//
//            if (request.getMethod() == HttpMethod.OPTIONS) {
//                response.setStatusCode(HttpStatus.OK);
//                return response.setComplete();
//            }
//
//            return chain.filter(exchange);
//        };
//    }
//}
//
