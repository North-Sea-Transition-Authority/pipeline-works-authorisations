package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartDocumentForm;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PipelineDrawingForm;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementTestHarnessService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
@Profile("test-harness")
class PipelineSchematicsGeneratorService implements TestHarnessAppFormService {

  private final PadTechnicalDrawingService padTechnicalDrawingService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.TECHNICAL_DRAWINGS;
  private final PadFileManagementTestHarnessService padFileManagementTestHarnessService;

  @Autowired
  public PipelineSchematicsGeneratorService(
      PadTechnicalDrawingService padTechnicalDrawingService,
      PadFileManagementTestHarnessService padFileManagementTestHarnessService) {
    this.padTechnicalDrawingService = padTechnicalDrawingService;
    this.padFileManagementTestHarnessService = padFileManagementTestHarnessService;
  }

  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    var user = appFormServiceParams.getUser();
    var detail = appFormServiceParams.getApplicationDetail();

    createAdmiraltyChartDocumentFormAndGenerateUpload(user, detail);

    var pipelineDrawingForm = createPipelineDrawingFormAndGenerateUpload(user, detail);
    padTechnicalDrawingService.addDrawing(detail, pipelineDrawingForm);
  }

  private void createAdmiraltyChartDocumentFormAndGenerateUpload(WebUserAccount user, PwaApplicationDetail pwaApplicationDetail) {
    var admiraltyChartForm = new AdmiraltyChartDocumentForm();
    padFileManagementTestHarnessService.uploadFileAndMapToForm(admiraltyChartForm, pwaApplicationDetail, FileDocumentType.ADMIRALTY_CHART);
  }

  private PipelineDrawingForm createPipelineDrawingFormAndGenerateUpload(WebUserAccount user, PwaApplicationDetail pwaApplicationDetail) {

    var pipelineIdsToLink = padTechnicalDrawingService.getUnlinkedApplicationPipelineOverviews(pwaApplicationDetail)
        .stream()
        .map(PipelineOverview::getPadPipelineId)
        .collect(Collectors.toList());

    var pipelineDrawingForm = new PipelineDrawingForm();
    pipelineDrawingForm.setReference("My drawing reference");
    pipelineDrawingForm.setPadPipelineIds(pipelineIdsToLink);

    padFileManagementTestHarnessService.uploadFileAndMapToFormWithLegacyPadFileLink(
        pipelineDrawingForm,
        pwaApplicationDetail,
        FileDocumentType.PIPELINE_DRAWINGS,
        ApplicationDetailFilePurpose.PIPELINE_DRAWINGS
    );

    return pipelineDrawingForm;
  }
}
