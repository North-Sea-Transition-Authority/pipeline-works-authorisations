package uk.co.ogauthority.pwa.util;

import org.apache.commons.lang3.reflect.FieldUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;

public class PortalOrganisationTestUtils {

  private final static Integer DEFAULT_GROUP_ID = 100;
  public final static String DEFAULT_GROUP_UREF = DEFAULT_GROUP_ID.toString() + "++REGORGGRP";
  public final static Integer DEFAULT_UNIT_ID = 1000;
  private final static String DEFAULT_GROUP_NAME = "ORGANISATION_GROUP";
  private final static String DEFAULT_GROUP_SHORT_NAME = "ORG_GRP";
  private final static String DEFAULT_UNIT_NAME = "ORGANISATION_UNIT";

  public static PortalOrganisationUnit getOrganisationUnit() {

    PortalOrganisationGroup portalOrganisationGroup = generateOrganisationGroup(
        DEFAULT_GROUP_ID,
        DEFAULT_GROUP_NAME,
        DEFAULT_GROUP_SHORT_NAME
    );
    return generateOrganisationUnit(DEFAULT_UNIT_ID, DEFAULT_UNIT_NAME, portalOrganisationGroup);
  }


  public static PortalOrganisationUnit generateOrganisationUnit(int ouId, String name,
                                                                PortalOrganisationGroup portalOrganisationGroup) {
    PortalOrganisationUnit organisationUnit = new PortalOrganisationUnit();
    try {
      FieldUtils.writeField(organisationUnit, "ouId", ouId, true);
      FieldUtils.writeField(organisationUnit, "name", name, true);
      FieldUtils.writeField(organisationUnit, "portalOrganisationGroup", portalOrganisationGroup, true);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    return organisationUnit;
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
