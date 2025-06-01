package uk.co.ogauthority.pwa.testutils;

import org.apache.commons.lang3.reflect.FieldUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationSearchUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;

/**
 * Util class to ease testing of Teams service and associated services which use teams package objects.
 */
public class TeamTestingUtils {

  public static PortalOrganisationUnit createOrgUnit() {
    var portalOrganisationGroup = generateOrganisationGroup(100, "ORGANISATION_GROUP", "ORG_GRP");
    return generateOrganisationUnit(1000, "ORGANISATION_UNIT", portalOrganisationGroup);
  }

  public static PortalOrganisationSearchUnit createOrgSearchUnit() {
    var portalOrganisationGroup = generateOrganisationGroup(100, "ORGANISATION_GROUP", "ORG_GRP");
    return generateOrganisationSearchUnit(1000, "ORGANISATION_UNIT", "XX001", portalOrganisationGroup);
  }

  private static PortalOrganisationUnit generateOrganisationUnit(int ouId, String name,
                                                                PortalOrganisationGroup portalOrganisationGroup) {
    return PortalOrganisationTestUtils.generateOrganisationUnit(
        ouId, name, portalOrganisationGroup);
  }

  private static PortalOrganisationSearchUnit generateOrganisationSearchUnit(int ouId,
                                                                             String name,
                                                                             String companyNumber,
                                                                             PortalOrganisationGroup portalOrganisationGroup) {
    return PortalOrganisationTestUtils.generateOrganisationSearchUnit(ouId,
        name,
        companyNumber,
        portalOrganisationGroup);
  }

  public static PortalOrganisationGroup generateOrganisationGroup(int orgGrpId, String name, String shortName) {

    PortalOrganisationGroup portalOrganisationGroup = new PortalOrganisationGroup();
    try {
      FieldUtils.writeField(portalOrganisationGroup, "orgGrpId", orgGrpId, true);
      FieldUtils.writeField(portalOrganisationGroup, "name", name, true);
      FieldUtils.writeField(portalOrganisationGroup, "shortName", shortName, true);
      FieldUtils.writeField(portalOrganisationGroup, "urefValue", String.valueOf(orgGrpId) + "++REGORGGRP", true);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    return portalOrganisationGroup;
  }

}

