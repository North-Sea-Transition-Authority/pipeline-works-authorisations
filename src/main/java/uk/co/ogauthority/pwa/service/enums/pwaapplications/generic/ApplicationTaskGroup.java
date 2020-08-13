package uk.co.ogauthority.pwa.service.enums.pwaapplications.generic;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * defines groups of applications tasks for the task list page.
 */
public enum ApplicationTaskGroup {
  APPLICATION_USERS(
      "Application users",
      10,
      List.of(
          ApplicationTask.APPLICATION_USERS
      )),
  ADMINISTRATIVE_DETAILS(
      "Administrative details",
      20,
      List.of(
          ApplicationTask.FIELD_INFORMATION,
          ApplicationTask.PROJECT_INFORMATION,
          ApplicationTask.FAST_TRACK,
          ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING,
          ApplicationTask.HUOO,
          ApplicationTask.PARTNER_LETTERS
      )),
  LOCATION_DETAILS(
      "Location details",
      30,
      List.of(
          ApplicationTask.LOCATION_DETAILS,
          ApplicationTask.CROSSING_AGREEMENTS
      )),
  TECHNICAL_DETAILS(
      "Technical details",
      40,
      List.of(
          ApplicationTask.GENERAL_TECH_DETAILS,
          ApplicationTask.FLUID_COMPOSITION,
          ApplicationTask.PIPELINE_OTHER_PROPERTIES,
          ApplicationTask.DESIGN_OP_CONDITIONS
      )),
  PIPELINES(
      "Pipelines",
      50,
      List.of(
          ApplicationTask.PIPELINES,
          ApplicationTask.TECHNICAL_DRAWINGS,
          ApplicationTask.PIPELINES_HUOO,
          ApplicationTask.CAMPAIGN_WORKS
      )),
  DEPOSITS(
      "Deposits",
      60,
      List.of(
          ApplicationTask.PERMANENT_DEPOSITS,
          ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS
      ));


  private final String displayName;
  private final int displayOrder;
  private final List<ApplicationTask> tasks;

  ApplicationTaskGroup(String displayName, int displayOrder, List<ApplicationTask> tasks) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
    this.tasks = tasks;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public List<ApplicationTask> getTasksInDisplayOrder() {
    return tasks;
  }

  public Set<ApplicationTask> getTasksAsSet() {
    return EnumSet.copyOf(tasks);
  }

  public static List<ApplicationTaskGroup> asList() {
    return Arrays.stream(ApplicationTaskGroup.values())
        .collect(Collectors.toList());
  }

}
