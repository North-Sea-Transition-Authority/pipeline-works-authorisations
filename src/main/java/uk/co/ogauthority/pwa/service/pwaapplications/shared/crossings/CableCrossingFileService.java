package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

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
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadCableCrossingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

@Service
public class CableCrossingFileService implements ApplicationFormSectionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(CableCrossingFileService.class);

  private final PadCableCrossingRepository padCableCrossingRepository;
  private final SpringValidatorAdapter groupValidator;
  private final PadFileService padFileService;

  @Autowired
  public CableCrossingFileService(
      PadCableCrossingRepository padCableCrossingRepository,
      SpringValidatorAdapter groupValidator, PadFileService padFileService) {
    this.padCableCrossingRepository = padCableCrossingRepository;
    this.groupValidator = groupValidator;
    this.padFileService = padFileService;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new CrossingDocumentsForm();
    padFileService.mapFilesToForm(form, detail, ApplicationFilePurpose.CABLE_CROSSINGS);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  private boolean requiresFullValidation(PwaApplicationDetail pwaApplicationDetail) {
    return padCableCrossingRepository.countAllByPwaApplicationDetail(pwaApplicationDetail) > 0;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
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
