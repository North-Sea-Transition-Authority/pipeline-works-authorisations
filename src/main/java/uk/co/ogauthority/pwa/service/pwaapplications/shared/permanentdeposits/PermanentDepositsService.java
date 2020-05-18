package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;


import java.util.Comparator;
import java.util.Map;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.DepositsForPipelines;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PermanentDepositInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.DepositsForPipelinesRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PermanentDepositInformationRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;
import uk.co.ogauthority.pwa.validators.PermanentDepositsValidator;

/* Service providing simplified API for Permanent Deposit app form */
@Service
public class PermanentDepositsService implements ApplicationFormSectionService {

  private final PermanentDepositInformationRepository permanentDepositInformationRepository;
  private final PermanentDepositsEntityMappingService permanentDepositsEntityMappingService;
  private final PermanentDepositsValidator permanentDepositsValidator;
  private final SpringValidatorAdapter groupValidator;
  private final PadPipelineRepository padPipelineRepository;
  private final DepositsForPipelinesRepository depositsForPipelinesRepository;
  private final PadProjectInformationRepository padProjectInformationRepository;

  @Autowired
  public PermanentDepositsService(
      PermanentDepositInformationRepository permanentDepositInformationRepository,
      PermanentDepositsEntityMappingService permanentDepositsEntityMappingService,
      PermanentDepositsValidator permanentDepositsValidator,
      SpringValidatorAdapter groupValidator,
      PadPipelineRepository padPipelineRepository,
      DepositsForPipelinesRepository depositsForPipelinesRepository,
      PadProjectInformationRepository padProjectInformationRepository) {
    this.permanentDepositInformationRepository = permanentDepositInformationRepository;
    this.permanentDepositsEntityMappingService = permanentDepositsEntityMappingService;
    this.permanentDepositsValidator = permanentDepositsValidator;
    this.groupValidator = groupValidator;
    this.padPipelineRepository = padPipelineRepository;
    this.depositsForPipelinesRepository = depositsForPipelinesRepository;
    this.padProjectInformationRepository = padProjectInformationRepository;
  }

  public PermanentDepositInformation getPermanentDepositData(PwaApplicationDetail pwaApplicationDetail) {
    var permanentDeposits = permanentDepositInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail);
    if (permanentDeposits.size() > 0) {
      return permanentDeposits.get(0);
    }
    var permanentDepositInformation = new PermanentDepositInformation();
    permanentDepositInformation.setPwaApplicationDetail(pwaApplicationDetail);
    return permanentDepositInformation;
  }



  /**
   * Map stored data to form including uploaded files depending on requested link status.
   *
   * @param permanentDepositInformation     stored data
   * @param form                      form to map to
   */
  public void mapEntityToForm(PermanentDepositInformation permanentDepositInformation,
                              PermanentDepositsForm form) {
    permanentDepositsEntityMappingService.mapDepositInformationDataToForm(permanentDepositInformation, form);
  }


  /**
   * From the form extract form data and file data which should be persisted.
   */
  @Transactional
  public void saveEntityUsingForm(PermanentDepositInformation permanentDepositInformation,
                                  PermanentDepositsForm form,
                                  WebUserAccount user) {
    permanentDepositsEntityMappingService.setEntityValuesUsingForm(permanentDepositInformation, form);
    permanentDepositInformation = permanentDepositInformationRepository.save(permanentDepositInformation);
    for (String padPipelineId : form.getSelectedPipelines().split(",")) {
      var depositsForPipelines = new DepositsForPipelines(permanentDepositInformation.getId(), Integer.parseInt(padPipelineId));
      depositsForPipelinesRepository.save(depositsForPipelines);
    }
  }


  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    PermanentDepositInformation permanentDepositInformation = getPermanentDepositData(detail);
    var permanentDepositsForm = new PermanentDepositsForm();
    mapEntityToForm(permanentDepositInformation, permanentDepositsForm);
    BindingResult bindingResult = new BeanPropertyBindingResult(permanentDepositInformation, "form");
    permanentDepositsValidator.validate(permanentDepositsForm, bindingResult);

    return !bindingResult.hasErrors();
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
      permanentDepositsValidator.validate(form, bindingResult,
          padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail).get());
    }

    return bindingResult;

  }


  public Map<String, String> getPipelines(PwaApplicationDetail pwaApplicationDetail) {
    return padPipelineRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)
        .stream()
        .sorted(Comparator.comparing(PadPipeline::getId))
        .collect(
            StreamUtils.toLinkedHashMap(padPipeline -> padPipeline.getId().toString(), PadPipeline::getFromLocation));
  }

  public boolean isPermanentDepositMade(PwaApplicationDetail pwaApplicationDetail) {
    var projectInformation = padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PadProjectInformation());
    return BooleanUtils.isTrue(projectInformation.getPermanentDepositsMade())
        || pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.DEPOSIT_CONSENT);
  }


}
