package uk.co.ogauthority.pwa.model.entity.publicnotice;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.TemplateTextType;

@Entity
public class TemplateText {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Enumerated(EnumType.STRING)
  private TemplateTextType textType;

  private String text;

  private Instant endTimestamp;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public TemplateTextType getTextType() {
    return textType;
  }

  public void setTextType(TemplateTextType textType) {
    this.textType = textType;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Instant getEndTimestamp() {
    return endTimestamp;
  }

  public void setEndTimestamp(Instant endTimestamp) {
    this.endTimestamp = endTimestamp;
  }
}
