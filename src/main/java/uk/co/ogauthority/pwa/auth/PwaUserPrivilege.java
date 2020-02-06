package uk.co.ogauthority.pwa.auth;

public enum PwaUserPrivilege {
  PWA_WORKAREA,
  PWA_ORG_ADMIN,
  PWA_APPLICATION_SUBMIT,
  PWA_APPLICATION_DRAFT, // do we still need this? view/draft/submitter may be controlled by the PWA contacts?
  PWA_REGULATOR_ADMIN,
  PWA_REG_ORG_MANAGE
}
