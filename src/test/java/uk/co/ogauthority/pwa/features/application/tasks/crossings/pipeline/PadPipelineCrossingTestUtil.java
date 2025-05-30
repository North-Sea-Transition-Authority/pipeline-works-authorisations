package uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline;

import java.util.Set;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

public class PadPipelineCrossingTestUtil {

  private PadPipelineCrossingTestUtil() {
    // no instantiation
  }

  public static PadPipelineCrossing createPadPipelineCrossing(PwaApplicationDetail pwaApplicationDetail) {
    var pipelineCrossing = new PadPipelineCrossing();
    pipelineCrossing.setPwaApplicationDetail(pwaApplicationDetail);
    pipelineCrossing.setPipelineCrossed("CROSSED PIPELINE");
    pipelineCrossing.setPipelineFullyOwnedByOrganisation(true);

    ObjectTestUtils.assertAllFieldsNotNull(
        pipelineCrossing,
        PadPipelineCrossing.class,
        Set.of(PadPipelineCrossing_.ID)
    );

    return pipelineCrossing;
  }

  public static PadPipelineCrossingOwner createPortalOrgPadPipelineCrossingOwner(PadPipelineCrossing padPipelineCrossing,
                                                                                 PortalOrganisationUnit portalOrganisationUnit) {
    var pipelineCrossingOwner = new PadPipelineCrossingOwner();
    pipelineCrossingOwner.setPadPipelineCrossing(padPipelineCrossing);
    pipelineCrossingOwner.setOrganisationUnit(portalOrganisationUnit);

    ObjectTestUtils.assertAllFieldsNotNull(
        pipelineCrossingOwner,
        PadPipelineCrossingOwner.class,
        Set.of(PadPipelineCrossingOwner_.ID, PadPipelineCrossingOwner_.MANUAL_ORGANISATION_ENTRY)
    );

    return pipelineCrossingOwner;
  }

  public static PadPipelineCrossingOwner createManualOrgPadPipelineCrossingOwner(PadPipelineCrossing padPipelineCrossing) {
    var pipelineCrossingOwner = new PadPipelineCrossingOwner();
    pipelineCrossingOwner.setPadPipelineCrossing(padPipelineCrossing);
    pipelineCrossingOwner.setManualOrganisationEntry("Some manual entry");

    ObjectTestUtils.assertAllFieldsNotNull(
        pipelineCrossingOwner,
        PadPipelineCrossingOwner.class,
        Set.of(PadPipelineCrossingOwner_.ID, PadPipelineCrossingOwner_.ORGANISATION_UNIT)
    );

    return pipelineCrossingOwner;
  }
}
