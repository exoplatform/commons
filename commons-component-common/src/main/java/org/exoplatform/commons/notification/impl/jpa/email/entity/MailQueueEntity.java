package org.exoplatform.commons.notification.impl.jpa.email.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.Calendar;

/**
 * Created by exo on 3/27/17.
 */
@Entity(name = "MailQueueEntity")
@ExoEntity
@Table(name = "EMAIL_QUEUE")
@NamedQueries({
    @NamedQuery(name = "commons.getMessagesInQueue", query = "SELECT m FROM MailQueueEntity m")
})
public class MailQueueEntity {
  @Id
  @Column(name = "EMAIL_ID")
  @SequenceGenerator(name="SEQ_NTF_EMAIL_QUEUE", sequenceName="SEQ_NTF_EMAIL_QUEUE")
  @GeneratedValue(strategy= GenerationType.AUTO, generator="SEQ_NTF_EMAIL_QUEUE")
  private long id;

  @Column(name = "EMAIL_CREATION_DATE")
  private Long creationDate;

  @Column(name = "EMAIL_TYPE")
  private String type;

  @Column(name = "EMAIL_FROM")
  private String from;

  @Column(name = "EMAIL_TO")
  private String to;

  @Column(name = "EMAIL_SUBJECT")
  private String subject;

  @Column(name = "EMAIL_BODY")
  private String body;

  @Column(name = "EMAIL_FOOTER")
  private String footer;

  public long getId() {
    return id;
  }

  public Long getCreationDate() {
    return creationDate;
  }

  public MailQueueEntity setCreationDate(Long creationDate) {
    this.creationDate = creationDate;
    return this;
  }

  public String getType() {
    return type;
  }

  public MailQueueEntity setType(String type) {
    this.type = type;
    return this;
  }

  public String getFrom() {
    return from;
  }

  public MailQueueEntity setFrom(String from) {
    this.from = from;
    return this;
  }

  public String getTo() {
    return to;
  }

  public MailQueueEntity setTo(String to) {
    this.to = to;
    return this;
  }

  public String getSubject() {
    return subject;
  }

  public MailQueueEntity setSubject(String subject) {
    this.subject = subject;
    return this;
  }

  public String getBody() {
    return body;
  }

  public MailQueueEntity setBody(String body) {
    this.body = body;
    return this;
  }

  public String getFooter() {
    return footer;
  }

  public MailQueueEntity setFooter(String footer) {
    this.footer = footer;
    return this;
  }

}
