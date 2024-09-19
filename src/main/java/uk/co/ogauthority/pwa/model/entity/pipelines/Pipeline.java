package uk.co.ogauthority.pwa.model.entity.pipelines;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.Objects;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;

@Entity
@Table(name = "pipelines")
public class Pipeline {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "pipeline_id_generator")
  @SequenceGenerator(name = "pipeline_id_generator", sequenceName = "pipeline_id_seq", allocationSize = 1)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pwa_id")
  private MasterPwa masterPwa;

  public Pipeline() {
  }

  public Pipeline(PwaApplication pwaApplication) {
    this.masterPwa = pwaApplication.getMasterPwa();
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public MasterPwa getMasterPwa() {
    return masterPwa;
  }

  public void setMasterPwa(MasterPwa masterPwa) {
    this.masterPwa = masterPwa;
  }

  public PipelineId getPipelineId() {
    return new PipelineId(this.id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Pipeline pipeline = (Pipeline) o;
    return Objects.equals(id, pipeline.id)
        && Objects.equals(masterPwa, pipeline.masterPwa);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, masterPwa);
  }
}
