package uk.co.ogauthority.pwa.service.pwaapplications.shared.supplementarydocs;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.supplementarydocs.SupplementaryDocumentsForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

@Service
public class SupplementaryDocumentsService implements ApplicationFormSectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SupplementaryDocumentsService.class);
  private final PadFileService padFileService;
  private final SpringValidatorAdapter groupValidator;
  private final PwaApplicationDetailService pwaApplicationDetailService;

  private static final ApplicationDetailFilePurpose FILE_PURPOSE = ApplicationDetailFilePurpose.SUPPLEMENTARY_DOCUMENTS;

  @Autowired
  public SupplementaryDocumentsService(PadFileService padFileService,
                                       SpringValidatorAdapter groupValidator,
                                       PwaApplicationDetailService pwaApplicationDetailService) {
    this.padFileService = padFileService;
    this.groupValidator = groupValidator;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new SupplementaryDocumentsForm();
    mapSavedDataToForm(detail, form);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {

    List<Object> validationHints = new ArrayList<>();

    var castForm = (SupplementaryDocumentsForm) form;

    if (validationType.equals(ValidationType.FULL) && BooleanUtils.isTrue(castForm.getHasFilesToUpload())) {
      validationHints.add(MandatoryUploadValidation.class);
    }

    if (validationType.equals(ValidationType.PARTIAL)) {
      validationHints.add(PartialValidation.class);
    }

    groupValidator.validate(form, bindingResult, validationHints.toArray());

    return bindingResult;

  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    LOGGER.warn("TODO PWA-816: " + this.getClass().getName());
  }

  public void updateDocumentFlag(PwaApplicationDetail detail, SupplementaryDocumentsForm form) {
    pwaApplicationDetailService.setSupplementaryDocumentsFlag(detail, form.getHasFilesToUpload());
  }

  public void mapSavedDataToForm(PwaApplicationDetail detail, SupplementaryDocumentsForm form) {

    form.setHasFilesToUpload(detail.getSupplementaryDocumentsFlag());

    padFileService.mapFilesToForm(form, detail, FILE_PURPOSE);

  }

}
