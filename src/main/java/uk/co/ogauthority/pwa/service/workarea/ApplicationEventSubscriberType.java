package uk.co.ogauthority.pwa.service.workarea;

/**
 * Events can occur within the PWA application that qualify applications for the the attention of certain types of user.
 * The enum describes those high level users.
 */
enum ApplicationEventSubscriberType {
  PWA_MANAGER,
  CASE_OFFICER,
  APPLICATION_CONTACT;
  // consultee and as-builts omitted as they have specific workarea requirements seperate from applications as a whole

}
