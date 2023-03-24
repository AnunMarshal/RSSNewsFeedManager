package com.rss.newsfeedmanager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class Security {

  @Value("${ws.username1}")
  private String userName1;

  @Value("${ws.password1}")
  private String password1;

  @Value("${ws.username2}")
  private String userName2;

  @Value("${ws.password2}")
  private String password2;

  public boolean authentication(String encodedPassword) throws UnsupportedEncodingException {
    String value1 = userName1 + ":" + password1;
    String base641 = "Basic " + Base64.getEncoder()
      .encodeToString(value1.getBytes(StandardCharsets.UTF_8.toString()));
    System.out.println("Base64 Password1 is         : " + base641);
    String value2 = userName2 + ":" + password2;
    String base642 = "Basic " + Base64.getEncoder()
      .encodeToString(value2.getBytes(StandardCharsets.UTF_8.toString()));
    System.out.println("Base64 Password2 is         : " + base642);
    System.out.println("Encoded Password is : " + encodedPassword);
    return base641.equals(encodedPassword) || base642.equals(encodedPassword);
  }
}
