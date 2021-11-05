package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration;

import org.springframework.context.annotation.Profile;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;

@Profile("test-harness")
public interface TestHarnessAppFormService {

  void generateAppFormData(TestHarnessAppFormServiceParams testHarnessAppFormServiceParams);

  ApplicationTask getLinkedAppFormTask();

}
