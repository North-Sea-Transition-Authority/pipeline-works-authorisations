package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.permanentdeposits.PermanentDepositController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadDepositPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositsOverview;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadDepositPipelineRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPermanentDepositRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;
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

  private final PadPermanentDepositRepository permanentDepositInformationRepository;
  private final PermanentDepositEntityMappingService permanentDepositEntityMappingService;
  private final PermanentDepositsValidator permanentDepositsValidator;
  private final SpringValidatorAdapter groupValidator;
  private final PadPipelineRepository padPipelineRepository;
  private final PadDepositPipelineRepository padDepositPipelineRepository;
  private final PadProjectInformationRepository padProjectInformationRepository;

  @Autowired
  public PermanentDepositService(
      PadPermanentDepositRepository permanentDepositInformationRepository,
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
    var depositsForPipelines = padDepositPipelineRepository.findAllByPermanentDepositInfoId(padPermanentDeposit.getId());
    var pipelineIds = depositsForPipelines.stream().map(
        depositsForPipeline -> String.valueOf(depositsForPipeline.getPadPipelineId().getId()))
        .collect(Collectors.toSet());
    form.setSelectedPipelines(pipelineIds);
  }


  public PadPermanentDeposit mapEntityToFormById(Integer entityID, PermanentDepositsForm form) {
    var permanentDeposit = permanentDepositInformationRepository.findById(entityID)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format("Couldn't find permanent deposit with ID: %s", entityID)));
    mapEntityToForm(permanentDeposit, form);
    return permanentDeposit;
  }

  public void mapEntityToView(PadPermanentDeposit padPermanentDeposit,
                              PermanentDepositsOverview view) {
    permanentDepositEntityMappingService.mapDepositInformationDataToView(padPermanentDeposit, view);
    var depositsForPipelines = padDepositPipelineRepository.findAllByPermanentDepositInfoId(padPermanentDeposit.getId());
    var pipelineIds = depositsForPipelines.stream().map(
        depositsForPipeline -> String.valueOf(depositsForPipeline.getPadPipelineId().getId()))
        .collect(Collectors.toSet());
    view.setPipelineRefs(pipelineIds);
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
            .orElseThrow(() -> new PwaEntityNotFoundException(String.format("Couldn't find PadPipeline with ID: %s", padPipelineId)));
        var depositsForPipelines = new PadDepositPipeline(permanentDepositInformation, padPipeline);
        padDepositPipelineRepository.save(depositsForPipelines);
      }
    }
  }

  @Transactional
  public void removeDeposit(Integer depositId) {
    var permanentDeposit = permanentDepositInformationRepository.findById(depositId)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format("Couldn't find permanent deposit with ID: %s", depositId)));

    padDepositPipelineRepository.deleteAll(
        padDepositPipelineRepository.findAllByPermanentDepositInfoId(permanentDeposit.getId()));
    permanentDepositInformationRepository.delete(permanentDeposit);
  }




  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return validateDepositOverview(detail);
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
      permanentDepositsValidator.validate(form, bindingResult, this, pwaApplicationDetail);
    }

    return bindingResult;
  }

  public boolean validateDepositOverview(PwaApplicationDetail pwaApplicationDetail) {
    List<PadPermanentDeposit> padPermanentDeposits =
        permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail);

    for (PadPermanentDeposit padPermanentDeposit: padPermanentDeposits) {
      var depositForm = new PermanentDepositsForm();
      mapEntityToForm(padPermanentDeposit, depositForm);

      BindingResult bindingResult = new BeanPropertyBindingResult(depositForm, "form");
      validate(depositForm, bindingResult, ValidationType.FULL, pwaApplicationDetail);
      if (bindingResult.hasErrors()) {
        return false;
      }
    }

    return padPermanentDeposits.size() > 0;
  }


  public List<PadPermanentDeposit> getPermanentDeposits(PwaApplicationDetail pwaApplicationDetail) {
    return permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail);
  }

  public List<PermanentDepositsOverview> getPermanentDepositViews(PwaApplicationDetail pwaApplicationDetail) {
    List<PermanentDepositsOverview> views = new ArrayList<>();

    var permanentDeposits = permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail);
    for (PadPermanentDeposit permanentDeposit: permanentDeposits) {
      PermanentDepositsOverview view = new PermanentDepositsOverview();
      mapEntityToView(permanentDeposit, view);
      view.setPipelineRefs(getPipeLineRefs(permanentDeposit));
      views.add(view);
    }
    return views;
  }

  public void populatePermanentDepositView(Integer depositId, PermanentDepositsOverview view) {
    var permanentDeposit = permanentDepositInformationRepository.findById(depositId)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format("Couldn't find permanent deposit with ID: %s", depositId)));
    permanentDepositEntityMappingService.mapDepositInformationDataToView(permanentDeposit, view);
    view.setPipelineRefs(getPipeLineRefs(permanentDeposit));
  }

  private Set<String> getPipeLineRefs(PadPermanentDeposit permanentDeposit) {
    var depositsForPipelines = padDepositPipelineRepository.findAllByPermanentDepositInfoId(permanentDeposit.getId());
    return depositsForPipelines.stream()
        .map(depositsForPipeline -> depositsForPipeline.getPadPipelineId().getPipelineRef())
        .collect(Collectors.toSet());
  }

  public Map<String, String> getEditUrlsForDeposits(PwaApplicationDetail pwaApplicationDetail) {
    Map<String, String>  depositUrls = new HashMap<>();
    var permanentDeposits = permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail);

    for (PadPermanentDeposit permanentDeposit: permanentDeposits) {
      depositUrls.put(permanentDeposit.getId().toString(),
          ReverseRouter.route(on(PermanentDepositController.class)
              .renderEditPermanentDeposits(
                  pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),
                  permanentDeposit.getId(), null, null)));
    }
    return depositUrls;
  }

  public Map<String, String> getRemoveUrlsForDeposits(PwaApplicationDetail pwaApplicationDetail) {
    Map<String, String>  depositUrls = new HashMap<>();
    var permanentDeposits = permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail);

    for (PadPermanentDeposit permanentDeposit: permanentDeposits) {
      depositUrls.put(permanentDeposit.getId().toString(),
          ReverseRouter.route(on(PermanentDepositController.class)
              .renderRemovePermanentDeposits(
                  pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),
                  permanentDeposit.getId(), null, null)));
    }
    return depositUrls;
  }



  public boolean isDepositReferenceUnique(String depositRef, Integer padDepositId, PwaApplicationDetail pwaApplicationDetail) {
    var existingDeposits = permanentDepositInformationRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(
        pwaApplicationDetail, depositRef);
    return existingDeposits.isEmpty() || (existingDeposits.get().getId() != null && existingDeposits.get().getId().equals(padDepositId));
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return isPermanentDepositMade(pwaApplicationDetail);
  }

  public boolean isPermanentDepositMade(PwaApplicationDetail pwaApplicationDetail) {
    var projectInformation = padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail);
    if (projectInformation.isPresent()) {
      return BooleanUtils.isTrue(projectInformation.get().getPermanentDepositsMade())
          || pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.DEPOSIT_CONSENT);
    }
    return false;
  }


}

