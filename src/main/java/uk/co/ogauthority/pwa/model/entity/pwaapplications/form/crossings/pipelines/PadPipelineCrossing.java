package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity(name = "pad_pipeline_crossings")
public class PadPipelineCrossing {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_detail_id")
  private PwaApplicationDetail pwaApplicationDetail;

  private String pipelineCrossed;

  @Column(name = "pipeline_fully_owned_by_org")
  private Boolean pipelineFullyOwnedByOrganisation;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public String getPipelineCrossed() {
    return pipelineCrossed;
  }

  public void setPipelineCrossed(String pipelineCrossed) {
    this.pipelineCrossed = pipelineCrossed;
  }

  public Boolean getPipelineFullyOwnedByOrganisation() {
    return pipelineFullyOwnedByOrganisation;
  }

  public void setPipelineFullyOwnedByOrganisation(Boolean pipelineFullyOwnedByOrganisation) {
    this.pipelineFullyOwnedByOrganisation = pipelineFullyOwnedByOrganisation;
  }
}
