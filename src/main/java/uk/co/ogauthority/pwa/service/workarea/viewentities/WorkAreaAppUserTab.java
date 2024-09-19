package uk.co.ogauthority.pwa.service.workarea.viewentities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTabCategory;

@Entity
@Table(name = "workarea_app_user_tabs")
@Immutable
public class WorkAreaAppUserTab {

  @Id
  private Integer pwaApplicationId;

  @OneToOne
  @JoinColumn(name =  "pwa_application_id")
  private WorkAreaApplicationDetailSearchItem workAreaApplicationDetailSearchItem;

  @Enumerated(EnumType.STRING)
  private WorkAreaTabCategory appUserWorkareaCategory;

  @Enumerated(EnumType.STRING)
  private WorkAreaTabCategory caseOfficerWorkareaCategory;

  @Enumerated(EnumType.STRING)
  private WorkAreaTabCategory pwaManagerWorkareaCategory;


  public Integer getPwaApplicationId() {
    return pwaApplicationId;
  }

  public void setPwaApplicationId(Integer pwaApplicationId) {
    this.pwaApplicationId = pwaApplicationId;
  }

  public WorkAreaTabCategory getAppUserWorkareaCategory() {
    return appUserWorkareaCategory;
  }

  public void setAppUserWorkareaCategory(WorkAreaTabCategory appUserWorkareaCategory) {
    this.appUserWorkareaCategory = appUserWorkareaCategory;
  }

  public WorkAreaTabCategory getCaseOfficerWorkareaCategory() {
    return caseOfficerWorkareaCategory;
  }

  public void setCaseOfficerWorkareaCategory(
      WorkAreaTabCategory caseOfficerWorkareaCategory) {
    this.caseOfficerWorkareaCategory = caseOfficerWorkareaCategory;
  }

  public WorkAreaTabCategory getPwaManagerWorkareaCategory() {
    return pwaManagerWorkareaCategory;
  }

  public void setPwaManagerWorkareaCategory(
      WorkAreaTabCategory pwaManagerWorkareaCategory) {
    this.pwaManagerWorkareaCategory = pwaManagerWorkareaCategory;
  }

  public WorkAreaApplicationDetailSearchItem getWorkAreaApplicationDetailSearchItem() {
    return workAreaApplicationDetailSearchItem;
  }

  public void setWorkAreaApplicationDetailSearchItem(
      WorkAreaApplicationDetailSearchItem workAreaApplicationDetailSearchItem) {
    this.workAreaApplicationDetailSearchItem = workAreaApplicationDetailSearchItem;
  }
}
