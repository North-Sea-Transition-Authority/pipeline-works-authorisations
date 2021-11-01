package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineCrossingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.util.FileUploadUtils;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;

@Service
public class PipelineCrossingFileService implements ApplicationFormSectionService {

  private final PadPipelineCrossingRepository padPipelineCrossingRepository;
  private final PadFileService padFileService;

  @Autowired
  public PipelineCrossingFileService(PadPipelineCrossingRepository padPipelineCrossingRepository,
                                     PadFileService padFileService) {
    this.padPipelineCrossingRepository = padPipelineCrossingRepository;
    this.padFileService = padFileService;
  }

  public boolean requiresFullValidation(PwaApplicationDetail pwaApplicationDetail) {
    return padPipelineCrossingRepository.countAllByPwaApplicationDetailAndPipelineFullyOwnedByOrganisation(
        pwaApplicationDetail, false) > 0;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new CrossingDocumentsForm();
    padFileService.mapFilesToForm(form, detail, ApplicationDetailFilePurpose.PIPELINE_CROSSINGS);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {

    List<Object> hints = new ArrayList<>();
    if (validationType.equals(ValidationType.FULL) && requiresFullValidation(pwaApplicationDetail)) {
      hints.add(MandatoryUploadValidation.class);
    }

    FileUploadUtils.validateFiles((CrossingDocumentsForm) form, bindingResult, hints);

    return bindingResult;
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    // files copied in PadPipelineCrossingService
  }
}
