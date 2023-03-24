package com.rss.newsfeedmanager;

public class Message {
  private String title;
  private String description;
  private String pubDate;

  public String getPubDate() {
    return pubDate;
  }

  public void setPubDate(String pubDate) {
    this.pubDate = pubDate;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
