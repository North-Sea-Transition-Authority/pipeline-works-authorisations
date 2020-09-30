package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import org.apache.commons.lang3.RandomUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.enums.BlockLocation;
import uk.co.ogauthority.pwa.model.entity.licence.PearsLicence;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.CrossedBlockOwner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCrossedBlock;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCrossedBlockOwner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCrossedBlockOwner_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCrossedBlock_;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

public class PadCrossedBlockTestUtil {

  private PadCrossedBlockTestUtil() {
    // no instantiation
  }

  public static PadCrossedBlock createUnlicensedPadCrossedBlock(PwaApplicationDetail pwaApplicationDetail, CrossedBlockOwner crossedBlockOwner){

    var cb  = new PadCrossedBlock();
    cb.setPwaApplicationDetail(pwaApplicationDetail);
    cb.setBlockOwner(crossedBlockOwner);
    cb.setCreatedInstant(Instant.now());
    cb.setLocation(BlockLocation.OFFSHORE);
    cb.setQuadrantNumber("1");
    cb.setBlockNumber("2");
    cb.setSuffix("a");
    cb.setBlockReference("1/2a");

    ObjectTestUtils.assertAllFieldsNotNull(cb, PadCrossedBlock.class,
        Set.of(PadCrossedBlock_.ID, PadCrossedBlock_.LICENCE));

    return cb;

  }

  public static PadCrossedBlock createLicensedPadCrossedBlock(PwaApplicationDetail pwaApplicationDetail,
                                                              CrossedBlockOwner crossedBlockOwner,
                                                              PearsLicence pearsLicence){

    var cb  = createUnlicensedPadCrossedBlock(pwaApplicationDetail, crossedBlockOwner);
    cb.setLicence(pearsLicence);

    ObjectTestUtils.assertAllFieldsNotNull(cb, PadCrossedBlock.class,
        Set.of(PadCrossedBlock_.ID));

    return cb;

  }

  public static PadCrossedBlockOwner createManualPadCrossedBlockOwner(PadCrossedBlock padCrossedBlock){

    var cbo  = new PadCrossedBlockOwner();
    cbo.setOwnerName(Arrays.toString(RandomUtils.nextBytes(10)));
    cbo.setPadCrossedBlock(padCrossedBlock);

    ObjectTestUtils.assertAllFieldsNotNull(
        cbo,
        PadCrossedBlockOwner.class,
        Set.of(PadCrossedBlockOwner_.ID, PadCrossedBlockOwner_.OWNER_OU_ID));

    return cbo;

  }

  public static PadCrossedBlockOwner createPortalOrgPadCrossedBlockOwner(PadCrossedBlock padCrossedBlock, PortalOrganisationUnit portalOrganisationUnit){

    var cbo  = new PadCrossedBlockOwner();
    cbo.setOwnerOuId(portalOrganisationUnit.getOuId());
    cbo.setPadCrossedBlock(padCrossedBlock);

    ObjectTestUtils.assertAllFieldsNotNull(
        cbo,
        PadCrossedBlockOwner.class,
        Set.of(PadCrossedBlockOwner_.ID, PadCrossedBlockOwner_.OWNER_NAME));

    return cbo;

  }
}
