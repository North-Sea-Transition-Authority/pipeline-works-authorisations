package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

@Entity
@Table(name="pad_campaign_works_pipelines")
public class PadCampaignWorksPipeline {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name="pad_campaign_work_schedule_id")
  private PadCampaignWorkSchedule padCampaignWorkSchedule;

  @ManyToOne
  @JoinColumn(name = "padPipelineId")
  private PadPipeline padPipeline;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PadCampaignWorkSchedule getPadCampaignWorkSchedule() {
    return padCampaignWorkSchedule;
  }

  public void setPadCampaignWorkSchedule(
      PadCampaignWorkSchedule padCampaignWorkSchedule) {
    this.padCampaignWorkSchedule = padCampaignWorkSchedule;
  }

  public PadPipeline getPadPipeline() {
    return padPipeline;
  }

  public void setPadPipeline(PadPipeline padPipeline) {
    this.padPipeline = padPipeline;
  }
}
