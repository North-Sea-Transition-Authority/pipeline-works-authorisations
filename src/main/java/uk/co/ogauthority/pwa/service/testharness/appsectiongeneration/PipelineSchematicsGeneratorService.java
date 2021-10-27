package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.AdmiraltyChartDocumentForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.service.testharness.filehelper.TestHarnessPadFileService;

@Service
@Profile("test-harness")
class PipelineSchematicsGeneratorService implements TestHarnessAppFormService {

  private final PadTechnicalDrawingService padTechnicalDrawingService;
  private final TestHarnessPadFileService testHarnessPadFileService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.TECHNICAL_DRAWINGS;

  @Autowired
  public PipelineSchematicsGeneratorService(
      PadTechnicalDrawingService padTechnicalDrawingService,
      TestHarnessPadFileService testHarnessPadFileService) {
    this.padTechnicalDrawingService = padTechnicalDrawingService;
    this.testHarnessPadFileService = testHarnessPadFileService;
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

    var generatedFileId = testHarnessPadFileService.generateImageUpload(
        user, pwaApplicationDetail, ApplicationDetailFilePurpose.ADMIRALTY_CHART);
    var admiraltyChartForm = new AdmiraltyChartDocumentForm();
    testHarnessPadFileService.setFileIdOnForm(generatedFileId, admiraltyChartForm.getUploadedFileWithDescriptionForms());

    testHarnessPadFileService.updatePadFiles(
        admiraltyChartForm, user, pwaApplicationDetail, ApplicationDetailFilePurpose.ADMIRALTY_CHART, FileUpdateMode.DELETE_UNLINKED_FILES);
  }

  private PipelineDrawingForm createPipelineDrawingFormAndGenerateUpload(WebUserAccount user, PwaApplicationDetail pwaApplicationDetail) {

    var pipelineIdsToLink = padTechnicalDrawingService.getUnlinkedApplicationPipelineOverviews(pwaApplicationDetail)
        .stream()
        .map(PipelineOverview::getPadPipelineId)
        .collect(Collectors.toList());

    var generatedFileId = testHarnessPadFileService.generateImageUpload(
        user, pwaApplicationDetail, ApplicationDetailFilePurpose.PIPELINE_DRAWINGS);

    var pipelineDrawingForm = new PipelineDrawingForm();
    pipelineDrawingForm.setReference("My drawing reference");
    pipelineDrawingForm.setPadPipelineIds(pipelineIdsToLink);

    testHarnessPadFileService.setFileIdOnForm(generatedFileId, pipelineDrawingForm.getUploadedFileWithDescriptionForms());
    testHarnessPadFileService.updatePadFiles(pipelineDrawingForm, user, pwaApplicationDetail,
        ApplicationDetailFilePurpose.PIPELINE_DRAWINGS, FileUpdateMode.KEEP_UNLINKED_FILES);

    return pipelineDrawingForm;
  }




}
