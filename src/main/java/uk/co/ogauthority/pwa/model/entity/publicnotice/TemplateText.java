package uk.co.ogauthority.pwa.model.entity.publicnotice;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.TemplateTextType;

@Entity
@Table(name = "template_text")
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
