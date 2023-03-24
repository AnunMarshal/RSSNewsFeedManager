package com.rss.newsfeedmanager.config;

import com.rss.newsfeedmanager.Security;
import com.rss.newsfeedmanager.handler.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  @Autowired
  private Handler handler;

  @Autowired
  private Security security;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(handler, "/websocket")
      .addInterceptors(new HttpSessionHandshakeInterceptor() {
        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

          String query = request.getURI().getQuery();

          if (!security.authentication(query.replace("token=", ""))) {
            System.out.println("Authentication failed...");
            response.setStatusCode(HttpStatus.valueOf(401));
            return false;
          }

          return super.beforeHandshake(request, response, wsHandler, attributes);
        }
      })
      .setAllowedOrigins("*");
  }
}