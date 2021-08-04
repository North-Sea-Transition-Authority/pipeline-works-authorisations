package uk.co.ogauthority.pwa.service.testharness.appprocessinggeneration;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskRequirement;

/**
 *  Service to generate data for all app processing tasks required.
 */
@Service
@Profile("test-harness")
public class AppProcessingTaskGeneratorService {


  private final Map<PwaAppProcessingTask, TestHarnessAppProcessingService> processingTaskAndGeneratorServiceMap;
  private static final Logger LOGGER = LoggerFactory.getLogger(AppProcessingTaskGeneratorService.class);


  @Autowired
  public AppProcessingTaskGeneratorService(
      List<TestHarnessAppProcessingService> processingAppTaskServices) {
    this.processingTaskAndGeneratorServiceMap = processingAppTaskServices.stream()
        .collect(Collectors.toMap(TestHarnessAppProcessingService::getLinkedAppProcessingTask, Function.identity()));
  }



  public void generateAppProcessingTasks(TestHarnessAppProcessingProperties appProcessingProps,
                                         Collection<PwaAppProcessingTask> appProcessingTasks) {

    appProcessingTasks.stream().sorted(Comparator.comparing(PwaAppProcessingTask::getDisplayOrder))
        .forEach(task -> {
          if (task.getTaskRequirement().equals(TaskRequirement.REQUIRED)) {
            LOGGER.info("Generating app processing task {}", task.getTaskName());
            processingTaskAndGeneratorServiceMap.get(task).generateAppProcessingTaskData(appProcessingProps);
          }
        });
  }







}
