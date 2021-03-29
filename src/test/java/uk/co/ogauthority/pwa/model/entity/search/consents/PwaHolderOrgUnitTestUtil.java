package uk.co.ogauthority.pwa.model.entity.search.consents;

import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;

public final class PwaHolderOrgUnitTestUtil {

  private PwaHolderOrgUnitTestUtil(){
    throw new UnsupportedOperationException("no util for you!");
  }
  public static PwaHolderOrgUnit createPwaHolderOrgUnit(String compositeId, int pwaId,
                                                        PortalOrganisationUnit organisationUnit) {

    var orgUnit = new PwaHolderOrgUnit();
    orgUnit.setCompositeId(compositeId);
    orgUnit.setPwaId(pwaId);
    orgUnit.setOuId(organisationUnit.getOuId());
    orgUnit.setOrgGrpId(organisationUnit.getPortalOrganisationGroup().getOrgGrpId());
    return orgUnit;

  }
}
