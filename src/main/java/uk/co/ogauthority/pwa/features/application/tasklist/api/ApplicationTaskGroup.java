package uk.co.ogauthority.pwa.features.application.tasklist.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;

/**
 * Defines groups of applications tasks for the task list page.
 */
public enum ApplicationTaskGroup {
  APPLICATION_USERS(
      "Application users",
      10,
      Map.of(
          PwaResourceType.PETROLEUM, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.APPLICATION_USERS, 10)),
          PwaResourceType.HYDROGEN, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.APPLICATION_USERS, 10)),
          PwaResourceType.CCUS, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.APPLICATION_USERS, 10))
      )),
  CONFIRMATION_OF_WORK(
      "Confirmation of work",
      15,
      Map.of(
          PwaResourceType.PETROLEUM, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.CONFIRM_OPTIONS, 10)),
          PwaResourceType.HYDROGEN, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.CONFIRM_OPTIONS, 10)),
          PwaResourceType.CCUS, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.CONFIRM_OPTIONS, 10))
      )),
  OPTIONS_INFORMATION(
      "Options information",
      25,
      Map.of(
          PwaResourceType.PETROLEUM, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.OPTIONS_TEMPLATE, 10),
              OrderedTaskGroupTask.from(ApplicationTask.SUPPLEMENTARY_DOCUMENTS, 20)),
          PwaResourceType.HYDROGEN, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.OPTIONS_TEMPLATE, 10),
              OrderedTaskGroupTask.from(ApplicationTask.SUPPLEMENTARY_DOCUMENTS, 20)),
          PwaResourceType.CCUS, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.OPTIONS_TEMPLATE, 10),
              OrderedTaskGroupTask.from(ApplicationTask.SUPPLEMENTARY_DOCUMENTS, 20))
      )),
  ADMINISTRATIVE_DETAILS(
      "Administrative details",
      20,
      Map.of(
          PwaResourceType.PETROLEUM, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.FIELD_INFORMATION, 10),
              OrderedTaskGroupTask.from(ApplicationTask.PROJECT_INFORMATION, 20),
              OrderedTaskGroupTask.from(ApplicationTask.FAST_TRACK, 30),
              OrderedTaskGroupTask.from(ApplicationTask.PROJECT_EXTENSION, 35),
              OrderedTaskGroupTask.from(ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING, 40),
              OrderedTaskGroupTask.from(ApplicationTask.HUOO, 50),
              OrderedTaskGroupTask.from(ApplicationTask.PARTNER_LETTERS, 60)),
          PwaResourceType.HYDROGEN, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.FIELD_INFORMATION, 10),
              OrderedTaskGroupTask.from(ApplicationTask.PROJECT_INFORMATION, 20),
              OrderedTaskGroupTask.from(ApplicationTask.FAST_TRACK, 30),
              OrderedTaskGroupTask.from(ApplicationTask.PROJECT_EXTENSION, 35),
              OrderedTaskGroupTask.from(ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING, 40),
              OrderedTaskGroupTask.from(ApplicationTask.HUOO, 50),
              OrderedTaskGroupTask.from(ApplicationTask.PARTNER_LETTERS, 60)),
          PwaResourceType.CCUS, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.CARBON_STORAGE_INFORMATION, 10),
              OrderedTaskGroupTask.from(ApplicationTask.PROJECT_INFORMATION, 20),
              OrderedTaskGroupTask.from(ApplicationTask.FAST_TRACK, 30),
              OrderedTaskGroupTask.from(ApplicationTask.PROJECT_EXTENSION, 35),
              OrderedTaskGroupTask.from(ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING, 40),
              OrderedTaskGroupTask.from(ApplicationTask.HUOO, 50),
              OrderedTaskGroupTask.from(ApplicationTask.PARTNER_LETTERS, 60))
      )),
  LOCATION_DETAILS(
      "Location details",
      30,
      Map.of(
          PwaResourceType.PETROLEUM, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.LOCATION_DETAILS, 10),
              OrderedTaskGroupTask.from(ApplicationTask.CROSSING_AGREEMENTS, 20)),
          PwaResourceType.HYDROGEN, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.LOCATION_DETAILS, 10),
              OrderedTaskGroupTask.from(ApplicationTask.CROSSING_AGREEMENTS, 20)),
          PwaResourceType.CCUS, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.LOCATION_DETAILS, 10),
              OrderedTaskGroupTask.from(ApplicationTask.CROSSING_AGREEMENTS, 20))
      )),
  TECHNICAL_DETAILS(
      "Technical details",
      40,
      Map.of(
          PwaResourceType.PETROLEUM, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.GENERAL_TECH_DETAILS, 10),
              OrderedTaskGroupTask.from(ApplicationTask.FLUID_COMPOSITION, 20),
              OrderedTaskGroupTask.from(ApplicationTask.PIPELINE_OTHER_PROPERTIES, 30),
              OrderedTaskGroupTask.from(ApplicationTask.DESIGN_OP_CONDITIONS, 40)),
          PwaResourceType.HYDROGEN, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.GENERAL_TECH_DETAILS, 10),
              OrderedTaskGroupTask.from(ApplicationTask.FLUID_COMPOSITION, 20),
              OrderedTaskGroupTask.from(ApplicationTask.PIPELINE_OTHER_PROPERTIES, 30),
              OrderedTaskGroupTask.from(ApplicationTask.DESIGN_OP_CONDITIONS, 40)),
          PwaResourceType.CCUS, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.GENERAL_TECH_DETAILS, 10),
              OrderedTaskGroupTask.from(ApplicationTask.FLUID_COMPOSITION, 20),
              OrderedTaskGroupTask.from(ApplicationTask.PIPELINE_OTHER_PROPERTIES, 30),
              OrderedTaskGroupTask.from(ApplicationTask.DESIGN_OP_CONDITIONS, 40))
      )),
  PIPELINES(
      "Pipelines",
      50,
      Map.of(
          PwaResourceType.PETROLEUM, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.PIPELINES, 10),
              OrderedTaskGroupTask.from(ApplicationTask.TECHNICAL_DRAWINGS, 20),
              OrderedTaskGroupTask.from(ApplicationTask.PIPELINES_HUOO, 30),
              OrderedTaskGroupTask.from(ApplicationTask.CAMPAIGN_WORKS, 40)),
          PwaResourceType.HYDROGEN, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.PIPELINES, 10),
              OrderedTaskGroupTask.from(ApplicationTask.TECHNICAL_DRAWINGS, 20),
              OrderedTaskGroupTask.from(ApplicationTask.PIPELINES_HUOO, 30),
              OrderedTaskGroupTask.from(ApplicationTask.CAMPAIGN_WORKS, 40)),
          PwaResourceType.CCUS, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.PIPELINES, 10),
              OrderedTaskGroupTask.from(ApplicationTask.TECHNICAL_DRAWINGS, 20),
              OrderedTaskGroupTask.from(ApplicationTask.PIPELINES_HUOO, 30),
              OrderedTaskGroupTask.from(ApplicationTask.CAMPAIGN_WORKS, 40))
      )),
  DEPOSITS(
      "Deposits",
      60,
      Map.of(
          PwaResourceType.PETROLEUM, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.PERMANENT_DEPOSITS, 10),
              OrderedTaskGroupTask.from(ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS, 20)),
          PwaResourceType.HYDROGEN, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.PERMANENT_DEPOSITS, 10),
              OrderedTaskGroupTask.from(ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS, 20)),
          PwaResourceType.CCUS, List.of(
              OrderedTaskGroupTask.from(ApplicationTask.PERMANENT_DEPOSITS, 10),
              OrderedTaskGroupTask.from(ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS, 20))
      ));


  private final String displayName;
  private final int displayOrder;
  private final Map<PwaResourceType, List<OrderedTaskGroupTask>> tasks;

  ApplicationTaskGroup(String displayName, int displayOrder, Map<PwaResourceType, List<OrderedTaskGroupTask>> tasks) {
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

  public List<OrderedTaskGroupTask> getTasks(PwaResourceType resourceType) {
    return tasks.get(resourceType);
  }

  public Set<ApplicationTask> getApplicationTaskSet(PwaResourceType resourceType) {
    return tasks.get(resourceType)
        .stream()
        .map(OrderedTaskGroupTask::getApplicationTask)
        .collect(Collectors.toSet());
  }

  public static List<ApplicationTaskGroup> asList() {
    return Arrays.stream(ApplicationTaskGroup.values())
        .collect(Collectors.toList());
  }

  public static ApplicationTaskGroup resolveFromName(String displayName) {
    return asList().stream()
        .filter(group -> Objects.equals(group.getDisplayName(), displayName))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(String.format("Couldn't find task group with display name [%s]", displayName)));
  }

}
