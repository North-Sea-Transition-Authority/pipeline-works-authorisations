package uk.co.ogauthority.pwa.model.entity.asbuilt;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.model.entity.converters.SemiColonSeparatedListConverter;

@Entity
@Immutable
@Table(name = "as_built_notif_workarea_view")
public class AsBuiltNotificationWorkareaView {

  @Id
  private Integer ngId;

  private String ngReference;

  private LocalDate deadlineDate;

  @Column(name = "completion_timestamp")
  private Instant projectCompletionDateTimestamp;

  @Enumerated(EnumType.STRING)
  private AsBuiltNotificationGroupStatus status;

  private String projectName;

  private Integer pwaId;

  @Column(name = "pwa_reference")
  private String masterPwaReference;

  private Integer consentId;

  @Convert(converter = SemiColonSeparatedListConverter.class)
  @Column(name = "pwa_holder_name_list")
  private List<String> pwaHolderNameList;


  public AsBuiltNotificationWorkareaView() {
  }

  AsBuiltNotificationWorkareaView(Integer ngId, String ngReference, LocalDate deadlineDate, Instant projectCompletionDateTimestamp,
                                         AsBuiltNotificationGroupStatus status, String projectName,
                                         Integer pwaId, String masterPwaReference, Integer consentId,
                                         List<String> pwaHolderNameList) {
    this.ngId = ngId;
    this.ngReference = ngReference;
    this.deadlineDate = deadlineDate;
    this.projectCompletionDateTimestamp = projectCompletionDateTimestamp;
    this.status = status;
    this.projectName = projectName;
    this.pwaId = pwaId;
    this.masterPwaReference = masterPwaReference;
    this.consentId = consentId;
    this.pwaHolderNameList = pwaHolderNameList;
  }

  public Integer getNgId() {
    return ngId;
  }

  public void setNgId(Integer ngId) {
    this.ngId = ngId;
  }

  public String getNgReference() {
    return ngReference;
  }

  public void setNgReference(String ngReference) {
    this.ngReference = ngReference;
  }

  public LocalDate getDeadlineDate() {
    return deadlineDate;
  }

  public void setDeadlineDate(LocalDate deadlineDate) {
    this.deadlineDate = deadlineDate;
  }

  public Instant getProjectCompletionDateTimestamp() {
    return projectCompletionDateTimestamp;
  }

  public void setProjectCompletionDateTimestamp(Instant projectCompletionDateTimestamp) {
    this.projectCompletionDateTimestamp = projectCompletionDateTimestamp;
  }

  public AsBuiltNotificationGroupStatus getStatus() {
    return status;
  }

  public void setStatus(AsBuiltNotificationGroupStatus status) {
    this.status = status;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public Integer getPwaId() {
    return pwaId;
  }

  public void setPwaId(Integer pwaId) {
    this.pwaId = pwaId;
  }

  public String getMasterPwaReference() {
    return masterPwaReference;
  }

  public void setMasterPwaReference(String masterPwaReference) {
    this.masterPwaReference = masterPwaReference;
  }

  public Integer getConsentId() {
    return consentId;
  }

  public void setConsentId(Integer consentId) {
    this.consentId = consentId;
  }

  public List<String> getPwaHolderNameList() {
    return pwaHolderNameList;
  }

  public void setPwaHolderNameList(List<String> pwaHolderNameList) {
    this.pwaHolderNameList = pwaHolderNameList;
  }

}
