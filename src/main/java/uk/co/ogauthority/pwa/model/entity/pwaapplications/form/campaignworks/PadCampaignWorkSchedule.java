package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks;

import com.google.common.annotations.VisibleForTesting;
import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity
@Table(name = "pad_campaign_work_schedule")
public class PadCampaignWorkSchedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pad_id")
  private PwaApplicationDetail pwaApplicationDetail;

  private LocalDate workFromDate;
  private LocalDate workToDate;

  public PadCampaignWorkSchedule(){
  }

  @VisibleForTesting
  public PadCampaignWorkSchedule(
      PwaApplicationDetail pwaApplicationDetail, int id) {
    this.id = id;
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public LocalDate getWorkFromDate() {
    return workFromDate;
  }

  public void setWorkFromDate(LocalDate workFromDate) {
    this.workFromDate = workFromDate;
  }

  public LocalDate getWorkToDate() {
    return workToDate;
  }

  public void setWorkToDate(LocalDate workToDate) {
    this.workToDate = workToDate;
  }
}
