package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;

@Entity(name = "pad_pipeline_crossing_owners")
public class PadPipelineCrossingOwner {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "ppc_id")
  private PadPipelineCrossing padPipelineCrossing;

  @ManyToOne
  @JoinColumn(name = "ou_id")
  private PortalOrganisationUnit organisationUnit;

}
