package uk.co.ogauthority.pwa.service.pwaapplications.options;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.options.OptionsTemplateForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.FileUploadUtils;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

@Service
public class OptionsTemplateService implements ApplicationFormSectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OptionsTemplateService.class);
  private final PadFileService padFileService;
  private final SpringValidatorAdapter groupValidator;

  private static final ApplicationDetailFilePurpose FILE_PURPOSE = ApplicationDetailFilePurpose.OPTIONS_TEMPLATE;

  @Autowired
  public OptionsTemplateService(PadFileService padFileService,
                                SpringValidatorAdapter groupValidator) {
    this.padFileService = padFileService;
    this.groupValidator = groupValidator;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new OptionsTemplateForm();
    padFileService.mapFilesToForm(form, detail, FILE_PURPOSE);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {

    List<Object> validationHints = new ArrayList<>();

    if (validationType.equals(ValidationType.FULL)) {
      validationHints.add(MandatoryUploadValidation.class);
    } else {
      validationHints.add(PartialValidation.class);
    }

    groupValidator.validate(form, bindingResult, validationHints.toArray());

    FileUploadUtils.validateMaxFileLimit(
        (UploadMultipleFilesWithDescriptionForm) form,
        bindingResult,
        1,
        "Provide a single template upload");

    return bindingResult;

  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    padFileService.copyPadFilesToPwaApplicationDetail(
        fromDetail,
        toDetail,
        FILE_PURPOSE,
        ApplicationFileLinkStatus.FULL
    );
  }

}
