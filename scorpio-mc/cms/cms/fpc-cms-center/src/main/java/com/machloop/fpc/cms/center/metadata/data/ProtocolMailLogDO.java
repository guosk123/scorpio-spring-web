package com.machloop.fpc.cms.center.metadata.data;

import java.util.List;

public class ProtocolMailLogDO extends AbstractLogRecordDO {

  private String protocol;
  private String messageId;
  private String from;
  private String to;
  private String subject;
  private String date;
  private String cc;
  private String bcc;
  private String attachment;
  private String content;
  private List<String> urlList;
  private String decrypted;

  @Override
  public String toString() {
    return "ProtocolMailLogDO [protocol=" + protocol + ", messageId=" + messageId + ", from=" + from
        + ", to=" + to + ", subject=" + subject + ", date=" + date + ", cc=" + cc + ", bcc=" + bcc
        + ", attachment=" + attachment + ", content=" + content + ", urlList=" + urlList
        + ", decrypted=" + decrypted + "]";
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getCc() {
    return cc;
  }

  public void setCc(String cc) {
    this.cc = cc;
  }

  public String getBcc() {
    return bcc;
  }

  public void setBcc(String bcc) {
    this.bcc = bcc;
  }

  public String getAttachment() {
    return attachment;
  }

  public void setAttachment(String attachment) {
    this.attachment = attachment;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public List<String> getUrlList() {
    return urlList;
  }

  public void setUrlList(List<String> urlList) {
    this.urlList = urlList;
  }

  public String getDecrypted() {
    return decrypted;
  }

  public void setDecrypted(String decrypted) {
    this.decrypted = decrypted;
  }

}
