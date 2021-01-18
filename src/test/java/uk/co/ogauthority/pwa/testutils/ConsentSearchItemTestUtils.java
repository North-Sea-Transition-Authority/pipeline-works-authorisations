package uk.co.ogauthority.pwa.testutils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnit;

public class ConsentSearchItemTestUtils {

  private ConsentSearchItemTestUtils() {
    throw new AssertionError();
  }

  public static ConsentSearchItem createSearchItem(int pwaId,
                                                   String fieldNameOrOtherReference,
                                                   String holders,
                                                   Instant firstConsentTimestamp) {
    var item = new ConsentSearchItem();
    item.setPwaId(pwaId);
    item.setPwaReference("PWA/" + item.getPwaId());
    item.setFieldNameOrOtherReference(fieldNameOrOtherReference);
    item.setHolderNamesCsv(holders);
    item.setFirstConsentTimestamp(firstConsentTimestamp);
    item.setLatestConsentTimestamp(firstConsentTimestamp.plus(400, ChronoUnit.DAYS));
    item.setLatestConsentReference("C/" + pwaId);
    return item;
  }

  public static PwaHolderOrgUnit createPwaHolderOrgUnit(int rowId, int pwaId, PortalOrganisationUnit organisationUnit) {

    var orgUnit = new PwaHolderOrgUnit();
    orgUnit.setRowId(rowId);
    orgUnit.setPwaId(pwaId);
    orgUnit.setOuId(organisationUnit.getOuId());
    orgUnit.setOrgGrpId(organisationUnit.getPortalOrganisationGroup().getOrgGrpId());
    return orgUnit;

  }

}
