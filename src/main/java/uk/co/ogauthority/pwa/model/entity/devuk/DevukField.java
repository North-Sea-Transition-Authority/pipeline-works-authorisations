package uk.co.ogauthority.pwa.model.entity.devuk;

import com.google.common.annotations.VisibleForTesting;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.energyportal.model.entity.devuk.DevukFieldId;

@Entity(name = "devuk_fields")
@Immutable
public class DevukField {

  @Id
  private Integer fieldId;
  private String fieldName;
  private Integer status;
  private Integer operatorOuId;

  public DevukField() {
  }

  @VisibleForTesting
  public DevukField(int fieldId, String fieldName, int status) {
    this.fieldId = fieldId;
    this.fieldName = fieldName;
    this.status = status;
  }

  public DevukFieldId getDevukFieldId() {
    return new DevukFieldId(this.fieldId);
  }

  public Integer getFieldId() {
    return fieldId;
  }

  public void setFieldId(Integer fieldId) {
    this.fieldId = fieldId;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Integer getOperatorOuId() {
    return operatorOuId;
  }

  public void setOperatorOuId(Integer operatorOuId) {
    this.operatorOuId = operatorOuId;
  }
}
