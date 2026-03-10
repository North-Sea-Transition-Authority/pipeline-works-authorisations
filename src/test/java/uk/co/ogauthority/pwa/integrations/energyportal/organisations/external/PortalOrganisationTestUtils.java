package uk.co.ogauthority.pwa.integrations.energyportal.organisations.external;

import java.time.LocalDate;
import org.apache.commons.lang3.reflect.FieldUtils;

public class PortalOrganisationTestUtils {

  private static final Integer DEFAULT_GROUP_ID = 100;
  public static final String DEFAULT_GROUP_UREF = DEFAULT_GROUP_ID.toString() + "++REGORGGRP";
  public static final Integer DEFAULT_UNIT_ID = 1000;
  private static final String DEFAULT_GROUP_NAME = "ORGANISATION_GROUP";
  private static final String DEFAULT_GROUP_SHORT_NAME = "ORG_GRP";
  private static final String DEFAULT_UNIT_NAME = "ORGANISATION_UNIT";
  private static final String DEFAULT_REGISTERED_NUMBER = "12345678";


  public static PortalOrganisationGroup getOrganisationGroup() {

    return generateOrganisationGroup(
        DEFAULT_GROUP_ID,
        DEFAULT_GROUP_NAME,
        DEFAULT_GROUP_SHORT_NAME
    );
  }

  public static PortalOrganisationUnit getOrganisationUnitInOrgGroup() {

    PortalOrganisationGroup portalOrganisationGroup = getOrganisationGroup();
    return generateOrganisationUnit(DEFAULT_UNIT_ID, DEFAULT_UNIT_NAME, portalOrganisationGroup);
  }

  public static PortalOrganisationUnit getInactiveOrganisationUnitInOrgGroup() {

    PortalOrganisationGroup portalOrganisationGroup = getOrganisationGroup();
    return genInactiveOrgUnitHelper(DEFAULT_UNIT_ID, DEFAULT_UNIT_NAME, portalOrganisationGroup);
  }


  private static PortalOrganisationUnit genInactiveOrgUnitHelper(int ouId,
                                                               String name,
                                                               PortalOrganisationGroup portalOrganisationGroup) {
    return new PortalOrganisationUnit(
        ouId,
        name,
        DEFAULT_REGISTERED_NUMBER,
        null,
        portalOrganisationGroup,
        LocalDate.of(2000, 1, 1),
        null,
        false,
        false);
  }

  private static PortalOrganisationUnit genActiveOrgUnitHelper(int ouId,
                                                               String name,
                                                               PortalOrganisationGroup portalOrganisationGroup) {
    return new PortalOrganisationUnit(
        ouId,
        name,
        DEFAULT_REGISTERED_NUMBER,
        null,
        portalOrganisationGroup,
        LocalDate.of(2000, 1, 1),
        null,
        false,
        true);
  }

  public static PortalOrganisationUnit generateOrganisationUnit(int ouId, String name) {
    return genActiveOrgUnitHelper(
        ouId,
        name,
        null
    );


  }


  public static PortalOrganisationUnit generateOrganisationUnit(int ouId, String name,
                                                                PortalOrganisationGroup portalOrganisationGroup) {
    PortalOrganisationUnit organisationUnit = genActiveOrgUnitHelper(ouId, name, portalOrganisationGroup);
    return organisationUnit;
  }

  public static PortalOrganisationUnit generateOrganisationUnit(
      int ouId,
      String name,
      String registeredNumber,
      String foreignRegisteredNumber
  ) {
    return new PortalOrganisationUnit(
        ouId,
        name,
        registeredNumber,
        foreignRegisteredNumber,
        null,
        LocalDate.of(2000, 1, 1),
        null,
        false,
        true
    );
  }

  public static PortalOrganisationSearchUnit generateOrganisationSearchUnit(int ouId,
                                                                            String name,
                                                                            String companyReference,
                                                                            PortalOrganisationGroup portalOrganisationGroup) {
    return new PortalOrganisationSearchUnit(ouId,
        name + " (" + companyReference + ")",
        portalOrganisationGroup.getOrgGrpId(),
        true);
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

  public static PortalOrganisationUnitDetail generateOrganisationUnitDetail(PortalOrganisationUnit orgUnit, String address, String registeredNumber) {

    var detail = new PortalOrganisationUnitDetail();

    try {
      FieldUtils.writeField(detail, "ouId", orgUnit.getOuId(), true);
      FieldUtils.writeField(detail, "organisationUnit", orgUnit, true);
      FieldUtils.writeField(detail, "legalAddress", address, true);
      FieldUtils.writeField(detail, "registeredNumber", registeredNumber, true);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    return detail;

  }
}
