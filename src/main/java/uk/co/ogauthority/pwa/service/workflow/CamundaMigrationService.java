package uk.co.ogauthority.pwa.service.workflow;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ResourceDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;

/**
 * Used to migrate running Camunda process instances from their current version to the latest version of their process definition.
 */
@Service
public class CamundaMigrationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CamundaMigrationService.class);
  private final RepositoryService repositoryService;
  private final RuntimeService runtimeService;

  @Autowired
  public CamundaMigrationService(RepositoryService repositoryService,
                                 RuntimeService runtimeService) {
    this.repositoryService = repositoryService;
    this.runtimeService = runtimeService;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void autoMigrateProcessInstancesToLatestVersion() {
    LOGGER.info("Starting workflow migrations");
    Arrays.stream(WorkflowType.values()).forEach(this::migrateProcessInstancesForWorkflowToLatestVersion);
  }

  private void migrateProcessInstancesForWorkflowToLatestVersion(WorkflowType workflowType) {

    LOGGER.info("Starting workflow migration for {}", workflowType.getProcessDefinitionKey());

    // get the latest version of the process definition
    ProcessDefinition latestVersion = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey(workflowType.getProcessDefinitionKey())
        .latestVersion()
        .singleResult();

    // populate map of (old, not latest) proc def ids to their definition objects
    Map<String, ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey(workflowType.getProcessDefinitionKey())
        .list()
        .stream()
        .filter(def -> !Objects.equals(def.getId(), latestVersion.getId()))
        .collect(Collectors.toMap(ResourceDefinition::getId, def -> def));

    // get a map of old process definition ids to a list of process instances running those process definitions
    Map<String, List<ProcessInstance>> oldProcessDefinitionsAndTheirProcessInstances = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey(workflowType.getProcessDefinitionKey())
        .active()
        .list()
        .stream()
        // only care about ones not running latest version
        .filter(instance -> !Objects.equals(instance.getProcessDefinitionId(), latestVersion.getId()))
        .collect(Collectors.groupingBy(ProcessInstance::getProcessDefinitionId));

    // for each of the old process definitions and their instances, migrate them to latest version
    oldProcessDefinitionsAndTheirProcessInstances.forEach((processDefinitionId, processInstances) -> {

      ProcessDefinition def = processDefinitions.get(processDefinitionId);

      MigrationPlan plan = runtimeService.createMigrationPlan(def.getId(), latestVersion.getId())
          .mapEqualActivities()
          .updateEventTriggers() // this means that any timer events will be reset, be wary of this if using timer events
          .build();

      List<String> processInstanceIds = processInstances.stream().map(ProcessInstance::getId).collect(
          Collectors.toList());

      try {
        runtimeService.newMigration(plan)
            .processInstanceIds(processInstanceIds)
            .execute();

        LOGGER.info("Workflow migration completed for {} \n" +
            "Process instance IDs: {} \n" +
            "from process definition ID: {} to latest version with ID: {}",
            workflowType.getProcessDefinitionKey(),
            String.join(", ", processInstanceIds),
            processDefinitionId,
            latestVersion.getId());

      } catch (Exception e) {
        LOGGER.error("Workflow migration failed for {} workflow. \n" +
            "Process instance IDs: {}" +
            "from process definition ID: {} to latest version with ID: {} \n" +
            "Exception was: {} {}",
            workflowType.getProcessDefinitionKey(),
            String.join(", ", processInstanceIds),
            processDefinitionId,
            latestVersion.getId(),
            e.getClass(),
            e.getLocalizedMessage());
      }
    });
  }
}