package uk.co.ogauthority.pwa.model.entity.pwaapplications.search;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;

/**
 * Basic mapped view which reports the max_version number associated with each important application detail status.
 */
@Entity
@Table(name = "pad_status_versions")
@Immutable
public class PwaApplicationStatusCategoryLookup {

  @Id
  private Integer pwaApplicationId;
  private Integer lastSubmittedVersion;
  private Integer maxDraftVersion;
  private Integer maxInitialSubReviewVers;
  private Integer maxCaseOfficerReviewVers;


  public Integer getPwaApplicationId() {
    return pwaApplicationId;
  }

  public void setPwaApplicationId(Integer pwaApplicationId) {
    this.pwaApplicationId = pwaApplicationId;
  }

  public Integer getLastSubmittedVersion() {
    return lastSubmittedVersion;
  }

  public void setLastSubmittedVersion(Integer lastSubmittedVersion) {
    this.lastSubmittedVersion = lastSubmittedVersion;
  }

  public Integer getMaxDraftVersion() {
    return maxDraftVersion;
  }

  public void setMaxDraftVersion(Integer maxDraftVersion) {
    this.maxDraftVersion = maxDraftVersion;
  }

  public Integer getMaxInitialSubReviewVers() {
    return maxInitialSubReviewVers;
  }

  public void setMaxInitialSubReviewVers(Integer maxInitialSubReviewVers) {
    this.maxInitialSubReviewVers = maxInitialSubReviewVers;
  }

  public Integer getMaxCaseOfficerReviewVers() {
    return maxCaseOfficerReviewVers;
  }

  public void setMaxCaseOfficerReviewVers(Integer maxCaseOfficerReviewVers) {
    this.maxCaseOfficerReviewVers = maxCaseOfficerReviewVers;
  }
}
