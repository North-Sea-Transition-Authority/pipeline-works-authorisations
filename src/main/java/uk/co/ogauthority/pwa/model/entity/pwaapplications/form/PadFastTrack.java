package uk.co.ogauthority.pwa.model.entity.pwaapplications.form;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity(name = "pad_fast_track_information")
public class PadFastTrack {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn("application_detail_id")
  @OneToOne
  private PwaApplicationDetail pwaApplicationDetail;

}
