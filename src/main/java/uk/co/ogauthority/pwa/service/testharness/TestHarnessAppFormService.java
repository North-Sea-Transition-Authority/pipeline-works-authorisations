package uk.co.ogauthority.pwa.service.testharness;

import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;

public interface TestHarnessAppFormService {

  void generateAppFormData(TestHarnessAppFormServiceParams testHarnessAppFormServiceParams);

  ApplicationTask getLinkedAppFormTask();

}
