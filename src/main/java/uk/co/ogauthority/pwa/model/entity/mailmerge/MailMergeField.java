package uk.co.ogauthority.pwa.model.entity.mailmerge;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldType;

@Entity
@Table(name = "mail_merge_fields")
public class MailMergeField {

  @Id
  @Enumerated(EnumType.STRING)
  @Column(name = "mnem")
  private MailMergeFieldMnem mnem;

  @Enumerated(EnumType.STRING)
  private MailMergeFieldType type;

  private String text;

  public MailMergeFieldMnem getMnem() {
    return mnem;
  }

  public void setMnem(MailMergeFieldMnem mnem) {
    this.mnem = mnem;
  }

  public MailMergeFieldType getType() {
    return type;
  }

  public void setType(MailMergeFieldType type) {
    this.type = type;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getDisplayString() {
    return type.getOpeningDelimiter() + mnem.name() + type.getClosingDelimiter();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MailMergeField that = (MailMergeField) o;
    return mnem == that.mnem && type == that.type && Objects.equals(text, that.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mnem, type, text);
  }

}
