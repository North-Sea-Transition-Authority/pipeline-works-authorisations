package uk.co.ogauthority.pwa.model.entity.search.consents;

import com.google.common.annotations.VisibleForTesting;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "vw_pwa_consents")
@Immutable
public class PwaConsentView {

  @Id
  private String rowId;

  private Integer pwaId;

  private String consentReference;

  public PwaConsentView() {
  }

  @VisibleForTesting
  public PwaConsentView(String rowId, Integer pwaId, String consentReference) {
    this.rowId = rowId;
    this.pwaId = pwaId;
    this.consentReference = consentReference;
  }

  public String getRowId() {
    return rowId;
  }

  public void setRowId(String rowId) {
    this.rowId = rowId;
  }

  public Integer getPwaId() {
    return pwaId;
  }

  public void setPwaId(Integer pwaId) {
    this.pwaId = pwaId;
  }

  public String getConsentReference() {
    return consentReference;
  }

  public void setConsentReference(String consentReference) {
    this.consentReference = consentReference;
  }

}
