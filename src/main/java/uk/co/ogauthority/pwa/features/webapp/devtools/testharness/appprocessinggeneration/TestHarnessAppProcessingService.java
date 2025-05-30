package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appprocessinggeneration;

import org.springframework.context.annotation.Profile;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;

@Profile("test-harness")
public interface TestHarnessAppProcessingService {

  void generateAppProcessingTaskData(TestHarnessAppProcessingProperties testHarnessAppProcessingProperties);

  PwaAppProcessingTask getLinkedAppProcessingTask();

}
