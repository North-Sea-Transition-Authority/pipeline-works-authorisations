package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;

@Service
@Profile("test-harness")
class AppUsersGeneratorService implements TestHarnessAppFormService {

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.APPLICATION_USERS;
  private static final Logger LOGGER = LoggerFactory.getLogger(AppUsersGeneratorService.class);


  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {
    LOGGER.info("Application user already set on application detail with id: {}", appFormServiceParams.getApplicationDetail().getId());
  }


}
