package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PermanentDepositController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadDepositPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadDepositPipelineRepository;
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
  private final PadDepositPipelineRepository padDepositPipelineRepository;
  private final PadProjectInformationRepository padProjectInformationRepository;

  @Autowired
  public PermanentDepositService(
      PermanentDepositInformationRepository permanentDepositInformationRepository,
      PermanentDepositEntityMappingService permanentDepositEntityMappingService,
      PermanentDepositsValidator permanentDepositsValidator,
      SpringValidatorAdapter groupValidator,
      PadPipelineRepository padPipelineRepository,
      PadDepositPipelineRepository padDepositPipelineRepository,
      PadProjectInformationRepository padProjectInformationRepository) {
    this.permanentDepositInformationRepository = permanentDepositInformationRepository;
    this.permanentDepositEntityMappingService = permanentDepositEntityMappingService;
    this.permanentDepositsValidator = permanentDepositsValidator;
    this.groupValidator = groupValidator;
    this.padPipelineRepository = padPipelineRepository;
    this.padDepositPipelineRepository = padDepositPipelineRepository;
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
    var existingDepositPipelines = padDepositPipelineRepository.findAllByPermanentDepositInfoId(permanentDepositInformation.getId());
    padDepositPipelineRepository.deleteAll(existingDepositPipelines);
    for (String padPipelineId : form.getSelectedPipelines()) {
      if (padPipelineId != "") {
        var padPipeline = padPipelineRepository.findById(Integer.valueOf(padPipelineId))
            .orElseThrow(() -> new PwaEntityNotFoundException("Permanent deposit information could not be found"));
        var depositsForPipelines = new PadDepositPipeline(permanentDepositInformation, padPipeline);
        padDepositPipelineRepository.save(depositsForPipelines);
      }
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

  public boolean isPermanentDepositMade(PwaApplicationDetail pwaApplicationDetail) {
    var projectInformation = padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PadProjectInformation());
    return BooleanUtils.isTrue(projectInformation.getPermanentDepositsMade())
        || pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.DEPOSIT_CONSENT);
  }


  public List<PermanentDepositsForm> getPermanentDepositForm(PwaApplicationDetail pwaApplicationDetail) {
    List<PermanentDepositsForm> forms = new ArrayList<>();

    var permanentDeposits = permanentDepositInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail);
    for (PadPermanentDeposit permanentDeposit: permanentDeposits) {
      PermanentDepositsForm form = new PermanentDepositsForm();
      mapEntityToForm(permanentDeposit, form);

      var depositsForPipelines = padDepositPipelineRepository.findAllByPermanentDepositInfoId(permanentDeposit.getId());
      var pipelineRefs = depositsForPipelines.stream().map(
          depositsForPipeline -> String.valueOf(depositsForPipeline.getPadPipelineId().getPipelineRef()))
          .collect(Collectors.toSet());

      form.setSelectedPipelines(pipelineRefs);
      forms.add(form);
    }

    return forms;
  }

  public PadPermanentDeposit mapEntityToFormById(Integer entityID, PermanentDepositsForm form) {
    var permanentDeposit = permanentDepositInformationRepository.findById(entityID)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format("Couldn't find permanent deposit with ID: %s", entityID)));
    mapEntityToForm(permanentDeposit, form);
    return permanentDeposit;
  }


  public Map<String, String> getEditUrlsForDeposits(PwaApplicationDetail pwaApplicationDetail) {
    Map<String, String>  depositUrls = new HashMap<>();
    var permanentDeposits = permanentDepositInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail);

    for (PadPermanentDeposit permanentDeposit: permanentDeposits) {
      depositUrls.put(permanentDeposit.getId().toString(),
          ReverseRouter.route(on(PermanentDepositController.class)
              .renderEditPermanentDeposits(
                  pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),
                  permanentDeposit.getId(), null, null)));
    }
    return depositUrls;
  }


}

