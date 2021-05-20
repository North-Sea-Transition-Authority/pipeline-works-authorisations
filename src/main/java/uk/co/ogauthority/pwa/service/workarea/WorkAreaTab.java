package uk.co.ogauthority.pwa.service.workarea;

import java.util.Set;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;
import uk.co.ogauthority.pwa.service.enums.users.UserType;

/**
 * Differentiates between tabs requested on work area.
 */
public enum WorkAreaTab {

  REGULATOR_REQUIRES_ATTENTION(
      "Applications for my attention",
      "attention-reg-apps",
      "attentionRegApplications",
      10,
      Set.of(UserType.OGA),
      Set.of()),

  REGULATOR_WAITING_ON_OTHERS(
      "Applications waiting on others",
      "waiting-reg-apps",
      "waitingRegApplications",
      15,
      Set.of(UserType.OGA),
      Set.of()),

  INDUSTRY_OPEN_APPLICATIONS(
      "My open applications",
      "open-ind-apps",
      "openIndApplications",
      20,
      Set.of(UserType.INDUSTRY),
      Set.of()),

  INDUSTRY_SUBMITTED_APPLICATIONS(
      "My submitted applications",
      "submitted-ind-apps",
      "submittedIndApplications",
      30,
      Set.of(UserType.INDUSTRY),
      Set.of()),

  OPEN_CONSULTATIONS(
      "My open consultations",
      "open-consultations",
      "openConsultations",
      40,
      Set.of(UserType.CONSULTEE),
      Set.of()),

  AS_BUILT_NOTIFICATIONS(
      "Outstanding as-built notifications",
      "as-built-notifications",
      "asBuiltNotifications",
      50,
      Set.of(),
      Set.of(PwaUserPrivilege.PWA_ASBUILT_WORKAREA));

  private final String label;
  private final String anchor;
  private final String value;
  private final int displayOrder;
  private final Set<UserType> userTypes;
  private final Set<PwaUserPrivilege> pwaUserPrivileges;

  WorkAreaTab(String label,
              String anchor,
              String value,
              int displayOrder,
              Set<UserType> userTypes, Set<PwaUserPrivilege> pwaUserPrivileges) {
    this.label = label;
    this.anchor = anchor;
    this.value = value;
    this.displayOrder = displayOrder;
    this.userTypes = userTypes;
    this.pwaUserPrivileges = pwaUserPrivileges;
  }

  public String getLabel() {
    return label;
  }

  public String getAnchor() {
    return anchor;
  }

  public String getValue() {
    return value;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public Set<UserType> getUserTypes() {
    return userTypes;
  }

  public Set<PwaUserPrivilege> getPwaUserPrivileges() {
    return pwaUserPrivileges;
  }

  public static Stream<WorkAreaTab> stream() {
    return Stream.of(WorkAreaTab.values());
  }

  public static WorkAreaTab fromValue(String value) {
    return WorkAreaTab.stream()
        .filter(tab -> tab.getValue().equals(value))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(String.format("Couldn't find WorkAreaTab with value: %s", value)));
  }
}
