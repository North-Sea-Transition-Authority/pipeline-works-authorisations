package uk.co.ogauthority.pwa.service.workarea;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>Various occurrences with the lifecycle of a PWA application qualify the application for the attention of different
 * event subscriber types in the work area.
 * This enum captures each of these events and determines the relevant subscriber types who need to see the application.</p>
 *
 * <p>For example, a PWA manager mostly want to fire and forget their actions of applications, but case officers and applicants
 * would want to keep track of applications where other people/processes are blocking progress but not actually
 * pay attention to them until theres something for them to do.</p>
 *
 * <p>Please see the view definition "workarea_app_lifecycle_events" for the details about how these events get genertated.</p>
 */
public enum ApplicationLifecycleEvent {

  UPDATE_REQUIRED(
      EnumSet.of(ApplicationEventSubscriberType.APPLICATION_CONTACT)
  ),

  PAYMENT_REQUIRED(
      EnumSet.of(ApplicationEventSubscriberType.APPLICATION_CONTACT)
  ),

  INITIAL_REVIEW_REQUIRED(
      EnumSet.of(ApplicationEventSubscriberType.PWA_MANAGER)
  ),

  APPLICATION_NOT_BEING_WORKED_ON(
      EnumSet.of(ApplicationEventSubscriberType.CASE_OFFICER)
  ),

  PUBLIC_NOTICE_WAITING_ON_CASE_OFFICER(
      EnumSet.of(ApplicationEventSubscriberType.CASE_OFFICER)
  ),

  PUBLIC_NOTICE_WAITING_ON_APP_CONTACT(
      EnumSet.of(ApplicationEventSubscriberType.APPLICATION_CONTACT)
  ),

  PUBLIC_NOTICE_WAITING_ON_PWA_MANAGER(
      EnumSet.of(ApplicationEventSubscriberType.PWA_MANAGER)
  ),

  CONSENT_DOCUMENT_REQUIRES_PWA_MANAGER_SIGN_OFF(
      EnumSet.of(ApplicationEventSubscriberType.PWA_MANAGER)
  );

  private final Set<ApplicationEventSubscriberType> forAttentionSubscriberTypes;

  ApplicationLifecycleEvent(Set<ApplicationEventSubscriberType> eventSubscriberTypes) {
    this.forAttentionSubscriberTypes = eventSubscriberTypes;

  }

  public Set<ApplicationEventSubscriberType> getForAttentionSubscriberTypes() {
    return Collections.unmodifiableSet(forAttentionSubscriberTypes);
  }

  public static Set<ApplicationLifecycleEvent> getForAttentionEventsWhereSubscriberIs(
      ApplicationEventSubscriberType applicationEventSubscriberType) {
    return Arrays.stream(ApplicationLifecycleEvent.values())
        .filter(o -> o.forAttentionSubscriberTypes.contains(applicationEventSubscriberType))
        .collect(Collectors.toUnmodifiableSet());
  }

}
