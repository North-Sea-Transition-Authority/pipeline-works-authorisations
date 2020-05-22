package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadDepositPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.DepositsForPipelinesRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PermanentDepositInformationRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;
import uk.co.ogauthority.pwa.validators.PermanentDepositsValidator;

/* Service providing simplified API for Permanent Deposit app form */
@Service
public class PermanentDepositService implements ApplicationFormSectionService {

  private final PermanentDepositInformationRepository permanentDepositInformationRepository;
  private final PermanentDepositEntityMappingService permanentDepositEntityMappingService;
  private final PermanentDepositsValidator permanentDepositsValidator;
  private final SpringValidatorAdapter groupValidator;
  private final PadPipelineRepository padPipelineRepository;
  private final DepositsForPipelinesRepository depositsForPipelinesRepository;
  private final PadProjectInformationRepository padProjectInformationRepository;

  @Autowired
  public PermanentDepositService(
      PermanentDepositInformationRepository permanentDepositInformationRepository,
      PermanentDepositEntityMappingService permanentDepositEntityMappingService,
      PermanentDepositsValidator permanentDepositsValidator,
      SpringValidatorAdapter groupValidator,
      PadPipelineRepository padPipelineRepository,
      DepositsForPipelinesRepository depositsForPipelinesRepository,
      PadProjectInformationRepository padProjectInformationRepository) {
    this.permanentDepositInformationRepository = permanentDepositInformationRepository;
    this.permanentDepositEntityMappingService = permanentDepositEntityMappingService;
    this.permanentDepositsValidator = permanentDepositsValidator;
    this.groupValidator = groupValidator;
    this.padPipelineRepository = padPipelineRepository;
    this.depositsForPipelinesRepository = depositsForPipelinesRepository;
    this.padProjectInformationRepository = padProjectInformationRepository;
  }


  /**
   * Map stored data to form.
   *
   * @param padPermanentDeposit     stored data
   * @param form                      form to map to
   */
  public void mapEntityToForm(PadPermanentDeposit padPermanentDeposit,
                              PermanentDepositsForm form) {
    permanentDepositEntityMappingService.mapDepositInformationDataToForm(padPermanentDeposit, form);
  }


  /**
   * From the form extract form data which should be persisted.
   */
  @Transactional
  public void saveEntityUsingForm(PwaApplicationDetail detail,
                                  PermanentDepositsForm form,
                                  WebUserAccount user) {
    var permanentDepositInformation = new PadPermanentDeposit();
    permanentDepositInformation.setPwaApplicationDetail(detail);
    permanentDepositEntityMappingService.setEntityValuesUsingForm(permanentDepositInformation, form);
    permanentDepositInformation = permanentDepositInformationRepository.save(permanentDepositInformation);
    for (String padPipelineId : form.getSelectedPipelines()) {
      var padPipeline = padPipelineRepository.findById(Integer.valueOf(padPipelineId))
          .orElseThrow(() -> new PwaEntityNotFoundException(String.format("Couldn't find PadPipeline with ID: %s", padPipelineId)));
      var depositsForPipelines = new PadDepositPipeline(permanentDepositInformation, padPipeline);
      depositsForPipelinesRepository.save(depositsForPipelines);
    }
  }


  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var permanentDeposits = permanentDepositInformationRepository.findByPwaApplicationDetail(detail);
    if (permanentDeposits.size() > 0) {
      PadPermanentDeposit padPermanentDeposit = permanentDeposits.get(0);
      var permanentDepositsForm = new PermanentDepositsForm();
      mapEntityToForm(padPermanentDeposit, permanentDepositsForm);
      BindingResult bindingResult = new BeanPropertyBindingResult(permanentDepositsForm, "form");
      permanentDepositsValidator.validate(permanentDepositsForm, bindingResult);

      return !bindingResult.hasErrors();
    }
    return false;
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    if (validationType.equals(ValidationType.PARTIAL)) {
      groupValidator.validate(form, bindingResult, PartialValidation.class);
    } else {
      groupValidator.validate(form, bindingResult, FullValidation.class);
      permanentDepositsValidator.validate(form, bindingResult);
    }

    return bindingResult;

  }


  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return isPermanentDepositMade(pwaApplicationDetail);
  }

  public boolean isPermanentDepositMade(PwaApplicationDetail pwaApplicationDetail) {
    var projectInformation = padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PadProjectInformation());
    return BooleanUtils.isTrue(projectInformation.getPermanentDepositsMade())
        || pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.DEPOSIT_CONSENT);
  }


}
