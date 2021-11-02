package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.ConfirmOptionForm;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadConfirmationOfOptionService;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;

@Service
@Profile("test-harness")
class ConfirmOptionsGeneratorService implements TestHarnessAppFormService {

  private final PadConfirmationOfOptionService padConfirmationOfOptionService;

  private static final ApplicationTask LINKED_APP_FORM_TASK = ApplicationTask.CONFIRM_OPTIONS;

  @Autowired
  public ConfirmOptionsGeneratorService(
      PadConfirmationOfOptionService padConfirmationOfOptionService) {
    this.padConfirmationOfOptionService = padConfirmationOfOptionService;
  }


  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return LINKED_APP_FORM_TASK;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {
    var padConfirmationOfOption = padConfirmationOfOptionService.getOrCreatePadConfirmationOfOption(
        appFormServiceParams.getApplicationDetail());
    padConfirmationOfOptionService.mapFormToEntity(createForm(), padConfirmationOfOption);
    padConfirmationOfOptionService.savePadConfirmation(padConfirmationOfOption);
  }


  private ConfirmOptionForm createForm() {
    var form = new ConfirmOptionForm();
    form.setConfirmedOptionType(ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS);
    form.setOptionCompletedDescription("My description of work done");
    return form;
  }




}
