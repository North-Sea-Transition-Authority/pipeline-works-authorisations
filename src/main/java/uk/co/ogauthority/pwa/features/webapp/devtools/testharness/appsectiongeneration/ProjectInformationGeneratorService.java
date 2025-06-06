package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PermanentDepositMade;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.ProjectInformationForm;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.ProjectInformationQuestion;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementTestHarnessService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
@Profile("test-harness")
class ProjectInformationGeneratorService implements TestHarnessAppFormService {

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.PROJECT_INFORMATION;
  private final PadProjectInformationService padProjectInformationService;
  private final PadFileManagementTestHarnessService padFileManagementTestHarnessService;

  @Autowired
  public ProjectInformationGeneratorService(
      PadProjectInformationService padProjectInformationService,
      PadFileManagementTestHarnessService padFileManagementTestHarnessService
  ) {
    this.padProjectInformationService = padProjectInformationService;
    this.padFileManagementTestHarnessService = padFileManagementTestHarnessService;
  }


  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    var form = createForm(appFormServiceParams.getUser(), appFormServiceParams.getApplicationDetail());
    var entity = padProjectInformationService.getPadProjectInformationData(appFormServiceParams.getApplicationDetail());
    padProjectInformationService.saveEntityUsingForm(entity, form, appFormServiceParams.getUser());
  }


  private ProjectInformationForm createForm(WebUserAccount user, PwaApplicationDetail pwaApplicationDetail) {

    var form = new ProjectInformationForm();

    var requiredQuestions = padProjectInformationService.getRequiredQuestions(
        pwaApplicationDetail.getPwaApplicationType(),
        pwaApplicationDetail.getResourceType());

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_NAME)) {
      form.setProjectName("My test project name");
    }

    var proposedStartDate = LocalDate.now();
    if (requiredQuestions.contains(ProjectInformationQuestion.PROPOSED_START_DATE)) {
      form.setProposedStartDay(proposedStartDate.getDayOfMonth());
      form.setProposedStartMonth(proposedStartDate.getMonthValue());
      form.setProposedStartYear(proposedStartDate.getYear());
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_OVERVIEW)) {
      form.setProjectOverview("My test project overview");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT)) {
      form.setMethodOfPipelineDeployment("My method of pipeline deployment");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.MOBILISATION_DATE)) {
      form.setMobilisationDay(LocalDate.now().getDayOfMonth());
      form.setMobilisationMonth(LocalDate.now().getMonthValue());
      form.setMobilisationYear(LocalDate.now().getYear());
    }

    var earliestCompletionDate = proposedStartDate.plusDays(1);
    if (requiredQuestions.contains(ProjectInformationQuestion.EARLIEST_COMPLETION_DATE)) {
      form.setEarliestCompletionDay(earliestCompletionDate.getDayOfMonth());
      form.setEarliestCompletionMonth(earliestCompletionDate.getMonthValue());
      form.setEarliestCompletionYear(earliestCompletionDate.getYear());
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.LATEST_COMPLETION_DATE)) {
      var latestCompletionDate = earliestCompletionDate.plusDays(1);
      form.setLatestCompletionDay(latestCompletionDate.getDayOfMonth());
      form.setLatestCompletionMonth(latestCompletionDate.getMonthValue());
      form.setLatestCompletionYear(latestCompletionDate.getYear());
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED)) {
      form.setLicenceTransferPlanned(false);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.USING_CAMPAIGN_APPROACH)) {
      form.setUsingCampaignApproach(false);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE)) {
      form.setPermanentDepositsMadeType(PermanentDepositMade.NONE);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.TEMPORARY_DEPOSITS_BEING_MADE)) {
      form.setTemporaryDepositsMade(false);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN)) {
      form.setFdpOptionSelected(true);
      form.setFdpConfirmationFlag(true);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM)) {
      padFileManagementTestHarnessService.uploadFileAndMapToForm(form, pwaApplicationDetail, FileDocumentType.PROJECT_INFORMATION);
    }

    return form;
  }





}
