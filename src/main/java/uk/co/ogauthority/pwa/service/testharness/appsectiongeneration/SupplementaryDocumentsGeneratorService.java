package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.supplementarydocs.SupplementaryDocumentsForm;
import uk.co.ogauthority.pwa.features.application.tasks.supplementarydocs.SupplementaryDocumentsService;

@Service
@Profile("test-harness")
class SupplementaryDocumentsGeneratorService implements TestHarnessAppFormService {

  private final SupplementaryDocumentsService supplementaryDocumentsService;

  private static final ApplicationTask LINKED_APP_FORM_TASK = ApplicationTask.SUPPLEMENTARY_DOCUMENTS;

  @Autowired
  public SupplementaryDocumentsGeneratorService(
      SupplementaryDocumentsService supplementaryDocumentsService) {
    this.supplementaryDocumentsService = supplementaryDocumentsService;
  }


  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return LINKED_APP_FORM_TASK;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {
    supplementaryDocumentsService.updateDocumentFlag(appFormServiceParams.getApplicationDetail(), createForm());
  }


  private SupplementaryDocumentsForm createForm() {
    var form = new SupplementaryDocumentsForm();
    form.setHasFilesToUpload(false);
    return form;
  }




}
