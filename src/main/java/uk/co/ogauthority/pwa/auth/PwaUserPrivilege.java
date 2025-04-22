package uk.co.ogauthority.pwa.auth;

import java.util.List;

public enum PwaUserPrivilege {

  // INTERNAL ACCESS
  PWA_ACCESS,

  // TODO: Remove in PWARE-63
  PWA_WORKAREA,
  PWA_ORG_ADMIN,
  PWA_REGULATOR_ADMIN,
  PWA_REG_ORG_MANAGE,

  // EXTERNAL ACCESS
  PIPELINE_VIEW;

  public static List<PwaUserPrivilege> asList() {
    return List.of(PwaUserPrivilege.values());
  }
}
