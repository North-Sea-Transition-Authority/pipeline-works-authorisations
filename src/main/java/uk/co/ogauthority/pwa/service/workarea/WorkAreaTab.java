package uk.co.ogauthority.pwa.service.workarea;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.auth.RoleGroup;
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
      null,
      WorkAreaTabCategory.FOR_ATTENTION),

  REGULATOR_WAITING_ON_OTHERS(
      "Applications waiting on others",
      "waiting-reg-apps",
      "waitingRegApplications",
      15,
      Set.of(UserType.OGA),
      null,
      WorkAreaTabCategory.BACKGROUND),

  INDUSTRY_OPEN_APPLICATIONS(
      "My open applications",
      "open-ind-apps",
      "openIndApplications",
      20,
      Set.of(UserType.INDUSTRY),
      null,
      WorkAreaTabCategory.FOR_ATTENTION),

  INDUSTRY_SUBMITTED_APPLICATIONS(
      "My submitted applications",
      "submitted-ind-apps",
      "submittedIndApplications",
      30,
      Set.of(UserType.INDUSTRY),
      null,
      WorkAreaTabCategory.BACKGROUND),

  OPEN_CONSULTATIONS(
      "My open consultations",
      "open-consultations",
      "openConsultations",
      40,
      Set.of(UserType.CONSULTEE),
      null,
      WorkAreaTabCategory.FOR_ATTENTION),

  AS_BUILT_NOTIFICATIONS(
      "Outstanding as-built notifications",
      "as-built-notifications",
      "asBuiltNotifications",
      50,
      Set.of(),
      RoleGroup.ASBUILT_WORKAREA,
      WorkAreaTabCategory.FOR_ATTENTION);

  private final String label;
  private final String anchor;
  private final String value;
  private final int displayOrder;
  private final Set<UserType> userTypes;
  private final RoleGroup roleGroup;
  private final WorkAreaTabCategory workAreaTabCategory;

  WorkAreaTab(String label,
              String anchor,
              String value,
              int displayOrder,
              Set<UserType> userTypes,
              RoleGroup roleGroup,
              WorkAreaTabCategory workAreaTabCategory) {
    this.label = label;
    this.anchor = anchor;
    this.value = value;
    this.displayOrder = displayOrder;
    this.userTypes = userTypes;
    this.roleGroup = roleGroup;
    this.workAreaTabCategory = workAreaTabCategory;
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

  public Optional<RoleGroup> getRoleGroup() {
    return Optional.ofNullable(roleGroup);
  }

  public WorkAreaTabCategory getWorkAreaTabCategory() {
    return workAreaTabCategory;
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
