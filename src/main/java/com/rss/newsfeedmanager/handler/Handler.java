package com.rss.newsfeedmanager.handler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.rss.newsfeedmanager.Item;
import com.rss.newsfeedmanager.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.HtmlUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@EnableScheduling
@Component
public class Handler extends TextWebSocketHandler {

  List<WebSocketSession> webSocketSessions = Collections.synchronizedList(new ArrayList<>());

  @Value("${ws.endpoint}")
  private String api;

  @Value("${ws.date}")
  private String date;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    super.afterConnectionEstablished(session);
    System.out.println("Connection established <<< " + session);
    webSocketSessions.add(session);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    super.afterConnectionClosed(session, status);
    System.out.println("Connection closed <<< " + session);
    webSocketSessions.remove(session);
  }

  @Override
  public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
    String request = message.getPayload().toString();
    System.out.println("Server received: {}" + request);

    String response = String.format("response from server to '%s'", HtmlUtils.htmlEscape(request));
    System.out.println("Server sends: {}" + response);
    session.sendMessage(message);
  }

  @Scheduled(fixedRate = 10000)
  void sendPeriodicMessages() throws IOException {
    for (WebSocketSession session : webSocketSessions) {
      if (session.isOpen()) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> apiRes = restTemplate.getForEntity(api, String.class);
        XmlMapper xmlMapper = new XmlMapper();
        JsonNode node = xmlMapper.readTree(apiRes.getBody().getBytes(StandardCharsets.UTF_8));
        ObjectMapper jsonMapper = new ObjectMapper();
        String json = jsonMapper.writeValueAsString(node.get("channel"));
        Item item = jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(json, Item.class);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssz");
        AtomicReference<Boolean> condition = new AtomicReference<>(false);
        item.getItem().stream().filter(e -> OffsetDateTime.parse(e.getPubDate(), df).isAfter(OffsetDateTime.parse(date, df))).forEach((val -> {
          System.out.println("Server sends: {}" + val.getTitle() + " <<< " + val.getDescription() + " <<< " + val.getPubDate());
          condition.set(true);
          try {
            session.sendMessage(generateMessage(val));
          } catch (IOException e) {
            e.printStackTrace();
          }
        }));
        if (condition.get()) {
          date = item.getItem().get(0).getPubDate();
        }
      }
    }
  }

  private TextMessage generateMessage(Message msg) {
    return new TextMessage(msg.getTitle() + " <<< " + msg.getDescription() + " <<< " + msg.getPubDate());
  }
}