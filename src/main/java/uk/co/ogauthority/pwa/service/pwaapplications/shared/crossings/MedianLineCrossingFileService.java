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
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadMedianLineAgreementRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

@Service
public class MedianLineCrossingFileService implements ApplicationFormSectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MedianLineCrossingFileService.class);

  private final PadMedianLineAgreementRepository padMedianLineAgreementRepository;
  private final SpringValidatorAdapter groupValidator;
  private final PadFileService padFileService;

  @Autowired
  public MedianLineCrossingFileService(
      PadMedianLineAgreementRepository padMedianLineAgreementRepository,
      SpringValidatorAdapter groupValidator, PadFileService padFileService) {
    this.padMedianLineAgreementRepository = padMedianLineAgreementRepository;
    this.groupValidator = groupValidator;
    this.padFileService = padFileService;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new CrossingDocumentsForm();
    padFileService.mapFilesToForm(form, detail, ApplicationFilePurpose.MEDIAN_LINE_CROSSING);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  public boolean requiresMandatoryValidation(PwaApplicationDetail pwaApplicationDetail) {
    var optionalAgreement = padMedianLineAgreementRepository.findByPwaApplicationDetail(pwaApplicationDetail);
    if (optionalAgreement.isEmpty()) {
      return false;
    } else {
      return optionalAgreement.get().getAgreementStatus() != MedianLineStatus.NOT_CROSSED;
    }
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    List<Object> hints = new ArrayList<>();
    if (validationType.equals(ValidationType.FULL)) {
      hints.add(FullValidation.class);
      if (requiresMandatoryValidation(pwaApplicationDetail)) {
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
