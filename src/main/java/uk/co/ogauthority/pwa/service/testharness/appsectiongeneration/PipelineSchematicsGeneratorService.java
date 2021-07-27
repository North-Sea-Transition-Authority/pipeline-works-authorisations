package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.service.testharness.filehelper.TestHarnessFileService;

@Service
@Profile("development")
public class PipelineSchematicsGeneratorService {

  private final PadTechnicalDrawingService padTechnicalDrawingService;
  private final TestHarnessFileService testHarnessFileService;

  @Autowired
  public PipelineSchematicsGeneratorService(
      PadTechnicalDrawingService padTechnicalDrawingService,
      TestHarnessFileService testHarnessFileService) {
    this.padTechnicalDrawingService = padTechnicalDrawingService;
    this.testHarnessFileService = testHarnessFileService;
  }



  public void generatePipelineSchematics(WebUserAccount user, PwaApplicationDetail pwaApplicationDetail) {

    testHarnessFileService.generateImageUpload(user, pwaApplicationDetail, ApplicationDetailFilePurpose.ADMIRALTY_CHART);
    var pipelineDrawingForm = createPipelineDrawingFormAndGenerateUpload(user, pwaApplicationDetail);
    padTechnicalDrawingService.addDrawing(pwaApplicationDetail, pipelineDrawingForm);
  }

  private PipelineDrawingForm createPipelineDrawingFormAndGenerateUpload(WebUserAccount user, PwaApplicationDetail pwaApplicationDetail) {

    var pipelineIdsToLink = padTechnicalDrawingService.getUnlinkedApplicationPipelineOverviews(pwaApplicationDetail)
        .stream()
        .map(PipelineOverview::getPadPipelineId)
        .collect(Collectors.toList());

    var generatedFileId = testHarnessFileService.generateImageUpload(
        user, pwaApplicationDetail, ApplicationDetailFilePurpose.PIPELINE_DRAWINGS);

    var pipelineDrawingForm = new PipelineDrawingForm();
    pipelineDrawingForm.setReference("My drawing reference");
    pipelineDrawingForm.setPadPipelineIds(pipelineIdsToLink);

    var uploadFileForm = new UploadFileWithDescriptionForm();
    uploadFileForm.setUploadedFileId(generatedFileId);
    pipelineDrawingForm.getUploadedFileWithDescriptionForms().add(uploadFileForm);

    return pipelineDrawingForm;
  }




}
