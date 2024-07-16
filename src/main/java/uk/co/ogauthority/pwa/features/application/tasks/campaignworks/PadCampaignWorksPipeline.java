package uk.co.ogauthority.pwa.features.application.tasks.campaignworks;


import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity
@Table(name = "pad_campaign_works_pipelines")
public class PadCampaignWorksPipeline implements ChildEntity<Integer, PadCampaignWorkSchedule> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pad_campaign_work_schedule_id")
  private PadCampaignWorkSchedule padCampaignWorkSchedule;

  @ManyToOne
  @JoinColumn(name = "padPipelineId")
  private PadPipeline padPipeline;

  public PadCampaignWorksPipeline() {
  }

  @VisibleForTesting
  public PadCampaignWorksPipeline(
      PadCampaignWorkSchedule padCampaignWorkSchedule,
      PadPipeline padPipeline) {
    this.padCampaignWorkSchedule = padCampaignWorkSchedule;
    this.padPipeline = padPipeline;
  }

  //ChildEntity methods


  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PadCampaignWorkSchedule parentEntity) {
    setPadCampaignWorkSchedule(parentEntity);
  }

  @Override
  public PadCampaignWorkSchedule getParent() {
    return getPadCampaignWorkSchedule();
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadCampaignWorksPipeline that = (PadCampaignWorksPipeline) o;
    return Objects.equals(id, that.id)
        && Objects.equals(padCampaignWorkSchedule, that.padCampaignWorkSchedule)
        && Objects.equals(padPipeline, that.padPipeline);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, padCampaignWorkSchedule, padPipeline);
  }
}
