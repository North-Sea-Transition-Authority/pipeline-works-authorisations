package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import org.springframework.context.annotation.Profile;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;

@Profile("test-harness")
public interface TestHarnessAppFormService {

  void generateAppFormData(TestHarnessAppFormServiceParams testHarnessAppFormServiceParams);

  ApplicationTask getLinkedAppFormTask();

}
