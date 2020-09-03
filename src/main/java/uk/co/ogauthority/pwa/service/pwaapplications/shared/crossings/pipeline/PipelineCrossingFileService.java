package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineCrossingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

@Service
public class PipelineCrossingFileService implements ApplicationFormSectionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PipelineCrossingFileService.class);

  private final PadPipelineCrossingRepository padPipelineCrossingRepository;
  private final SpringValidatorAdapter groupValidator;
  private final PadFileService padFileService;

  @Autowired
  public PipelineCrossingFileService(
      PadPipelineCrossingRepository padPipelineCrossingRepository,
      SpringValidatorAdapter groupValidator, PadFileService padFileService) {
    this.padPipelineCrossingRepository = padPipelineCrossingRepository;
    this.groupValidator = groupValidator;
    this.padFileService = padFileService;
  }

  public boolean requiresFullValidation(PwaApplicationDetail pwaApplicationDetail) {
    return padPipelineCrossingRepository.countAllByPwaApplicationDetailAndPipelineFullyOwnedByOrganisation(
        pwaApplicationDetail, false) > 0;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new CrossingDocumentsForm();
    padFileService.mapFilesToForm(form, detail, ApplicationFilePurpose.PIPELINE_CROSSINGS);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    List<Object> hints = new ArrayList<>();
    if (validationType.equals(ValidationType.FULL)) {
      hints.add(FullValidation.class);
      if (requiresFullValidation(pwaApplicationDetail)) {
        hints.add(MandatoryUploadValidation.class);
      }
    } else {
      hints.add(PartialValidation.class);
    }
    groupValidator.validate(form, bindingResult, hints.toArray());
    return bindingResult;
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    LOGGER.warn("TODO PWA-816: " + this.getClass().getName());
  }
}
