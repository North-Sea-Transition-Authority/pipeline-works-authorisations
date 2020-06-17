package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "pad_bundle_links")
public class PadBundleLink {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pp_id")
  private PadPipeline pipeline;

  @ManyToOne
  @JoinColumn(name = "pb_id")
  private PadBundle bundle;

  public PadBundleLink() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PadPipeline getPipeline() {
    return pipeline;
  }

  public void setPipeline(PadPipeline pipeline) {
    this.pipeline = pipeline;
  }

  public PadBundle getBundle() {
    return bundle;
  }

  public void setBundle(PadBundle bundle) {
    this.bundle = bundle;
  }
}
