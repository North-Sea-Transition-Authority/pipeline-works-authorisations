package uk.co.ogauthority.pwa.model.entity.pipelines;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
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

}
