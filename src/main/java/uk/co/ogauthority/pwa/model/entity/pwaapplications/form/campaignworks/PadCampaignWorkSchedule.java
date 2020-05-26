package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks;

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
@Table(name = "pad_campaign_works_schedule")
public class PadCampaignWorkSchedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pad_id")
  private PwaApplicationDetail pwaApplicationDetail;

  private LocalDate workFromDate;
  private LocalDate workToDate;


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
