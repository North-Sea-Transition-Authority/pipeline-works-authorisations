package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity
@Table(name = "pad_pipeline-other-properties")
public class PadPipelineOtherProperties {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "application_detail_id")
  @OneToOne
  private PwaApplicationDetail pwaApplicationDetail;


}
