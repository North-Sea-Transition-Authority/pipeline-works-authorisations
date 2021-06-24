package uk.co.ogauthority.pwa.service.workarea;

/**
 * Open PWA applications are scoped to various user types and require attention at different points.
 * The enum describes types of high level users interested in open applications.
 */
enum WorkAreaUserType {
  PWA_MANAGER,
  CASE_OFFICER,
  APPLICATION_CONTACT;
  // consultee and as-builts omitted as they have specific workarea requirements seperate from applications as a whole
}
