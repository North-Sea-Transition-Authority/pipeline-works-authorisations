package uk.co.ogauthority.pwa.model.entity.pwaapplications.search;

import java.time.Instant;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.converters.SemiColonSeperatedListConverter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@Entity
@Table(name = "pad_search_items")
public class ApplicationDetailSearchItem {

  @Id
  private int pwaApplicationDetailId;

  private int pwaApplicationId;

  private int pwaId;

  private int pwaDetailId;

  private String pwaReference;

  private String padReference;

  @Enumerated(EnumType.STRING)
  private PwaApplicationType applicationType;

  @Convert(converter = SemiColonSeperatedListConverter.class)
  @Column(name = "pad_field_name_list")
  private List<String> padFields;

  private String padProjectName;

  @Column(name = "pad_proposed_start_timestamp")
  private Instant padProposedStart;

  @Enumerated(EnumType.STRING)
  private PwaApplicationStatus padStatus;

  private Instant padCreatedTimestamp;

  private Instant padSubmittedTimestamp;

  private Instant padApprovedTimestamp;

  private Instant padStatusTimestamp;

  private boolean tipFlag;


  public int getPwaApplicationDetailId() {
    return pwaApplicationDetailId;
  }

  public void setPwaApplicationDetailId(int pwaApplicationDetailId) {
    this.pwaApplicationDetailId = pwaApplicationDetailId;
  }

  public int getPwaApplicationId() {
    return pwaApplicationId;
  }

  public void setPwaApplicationId(int pwaApplicationId) {
    this.pwaApplicationId = pwaApplicationId;
  }

  public int getPwaId() {
    return pwaId;
  }

  public void setPwaId(int pwaId) {
    this.pwaId = pwaId;
  }

  public int getPwaDetailId() {
    return pwaDetailId;
  }

  public void setPwaDetailId(int pwaDetailId) {
    this.pwaDetailId = pwaDetailId;
  }

  public String getPwaReference() {
    return pwaReference;
  }

  public void setPwaReference(String pwaReference) {
    this.pwaReference = pwaReference;
  }

  public String getPadReference() {
    return padReference;
  }

  public void setPadReference(String padReference) {
    this.padReference = padReference;
  }

  public PwaApplicationType getApplicationType() {
    return applicationType;
  }

  public void setApplicationType(PwaApplicationType applicationType) {
    this.applicationType = applicationType;
  }

  public PwaApplicationStatus getPadStatus() {
    return padStatus;
  }

  public void setPadStatus(PwaApplicationStatus padStatus) {
    this.padStatus = padStatus;
  }

  public Instant getPadCreatedTimestamp() {
    return padCreatedTimestamp;
  }

  public void setPadCreatedTimestamp(Instant padCreatedTimestamp) {
    this.padCreatedTimestamp = padCreatedTimestamp;
  }

  public Instant getPadSubmittedTimestamp() {
    return padSubmittedTimestamp;
  }

  public void setPadSubmittedTimestamp(Instant padSubmittedTimestamp) {
    this.padSubmittedTimestamp = padSubmittedTimestamp;
  }

  public Instant getPadApprovedTimestamp() {
    return padApprovedTimestamp;
  }

  public void setPadApprovedTimestamp(Instant padApprovedTimestamp) {
    this.padApprovedTimestamp = padApprovedTimestamp;
  }

  public boolean isTipFlag() {
    return tipFlag;
  }

  public void setTipFlag(boolean tipFlag) {
    this.tipFlag = tipFlag;
  }

  public List<String> getPadFields() {
    return padFields;
  }

  public void setPadFields(List<String> padFields) {
    this.padFields = padFields;
  }

  public String getPadProjectName() {
    return padProjectName;
  }

  public void setPadProjectName(String padProjectName) {
    this.padProjectName = padProjectName;
  }

  public Instant getPadProposedStart() {
    return padProposedStart;
  }

  public void setPadProposedStart(Instant padProposedStart) {
    this.padProposedStart = padProposedStart;
  }

  public Instant getPadStatusTimestamp() {
    return padStatusTimestamp;
  }

  public void setPadStatusTimestamp(Instant padStatusTimestamp) {
    this.padStatusTimestamp = padStatusTimestamp;
  }
}
