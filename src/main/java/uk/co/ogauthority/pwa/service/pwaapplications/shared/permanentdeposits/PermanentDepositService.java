package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositOverview;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadDepositPipelineRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPermanentDepositRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositdrawings.DepositDrawingsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.appdetailreconciliation.PadPipelineReconcilerService;
import uk.co.ogauthority.pwa.util.CleanupUtils;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;
import uk.co.ogauthority.pwa.validators.PermanentDepositsValidator;


/* Service providing simplified API for Permanent Deposit app form */
@Service
public class PermanentDepositService implements ApplicationFormSectionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PermanentDepositService.class);

  private final PadPermanentDepositRepository permanentDepositRepository;
  private final PermanentDepositEntityMappingService permanentDepositEntityMappingService;
  private final PermanentDepositsValidator permanentDepositsValidator;
  private final SpringValidatorAdapter groupValidator;
  private final PadPipelineRepository padPipelineRepository;
  private final PadDepositPipelineRepository padDepositPipelineRepository;
  private final PadProjectInformationRepository padProjectInformationRepository;
  private final DepositDrawingsService depositDrawingsService;
  private final EntityCopyingService entityCopyingService;
  private final PadPipelineReconcilerService padPipelineReconcilerService;

  @Autowired
  public PermanentDepositService(
      PadPermanentDepositRepository permanentDepositRepository,
      @Lazy DepositDrawingsService depositDrawingsService,
      PermanentDepositEntityMappingService permanentDepositEntityMappingService,
      PermanentDepositsValidator permanentDepositsValidator,
      SpringValidatorAdapter groupValidator,
      PadPipelineRepository padPipelineRepository,
      PadDepositPipelineRepository padDepositPipelineRepository,
      PadProjectInformationRepository padProjectInformationRepository,
      EntityCopyingService entityCopyingService,
      PadPipelineReconcilerService padPipelineReconcilerService) {
    this.permanentDepositRepository = permanentDepositRepository;
    this.depositDrawingsService = depositDrawingsService;
    this.permanentDepositEntityMappingService = permanentDepositEntityMappingService;
    this.permanentDepositsValidator = permanentDepositsValidator;
    this.groupValidator = groupValidator;
    this.padPipelineRepository = padPipelineRepository;
    this.padDepositPipelineRepository = padDepositPipelineRepository;
    this.padProjectInformationRepository = padProjectInformationRepository;
    this.entityCopyingService = entityCopyingService;
    this.padPipelineReconcilerService = padPipelineReconcilerService;
  }


  /**
   * Map stored data to form.
   *
   * @param padPermanentDeposit stored data
   * @param form                form to map to
   */
  public void mapEntityToForm(PadPermanentDeposit padPermanentDeposit,
                              PermanentDepositsForm form) {
    permanentDepositEntityMappingService.mapDepositInformationDataToForm(padPermanentDeposit, form);
    var depositsForPipelines = padDepositPipelineRepository.findAllByPadPermanentDeposit(padPermanentDeposit);
    var pipelineIds = depositsForPipelines.stream().map(
        depositsForPipeline -> String.valueOf(depositsForPipeline.getPadPipeline().getId()))
        .collect(Collectors.toSet());
    form.setSelectedPipelines(pipelineIds);
  }


  public PadPermanentDeposit mapEntityToFormById(Integer entityID, PermanentDepositsForm form) {
    var permanentDeposit = permanentDepositRepository.findById(entityID)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find permanent deposit with ID: %s", entityID)));
    mapEntityToForm(permanentDeposit, form);
    return permanentDeposit;
  }

  public PermanentDepositOverview createViewFromEntity(PadPermanentDeposit padPermanentDeposit) {
    return permanentDepositEntityMappingService.createPermanentDepositOverview(padPermanentDeposit);
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
    permanentDepositInformation = permanentDepositRepository.save(permanentDepositInformation);
    var existingDepositPipelines = padDepositPipelineRepository.findAllByPadPermanentDeposit(permanentDepositInformation);
    padDepositPipelineRepository.deleteAll(existingDepositPipelines);
    for (String padPipelineId : form.getSelectedPipelines()) {
      if (padPipelineId != "") {
        var padPipeline = padPipelineRepository.findById(Integer.valueOf(padPipelineId))
            .orElseThrow(() -> new PwaEntityNotFoundException(
                String.format("Couldn't find PadPipeline with ID: %s", padPipelineId)));
        var depositsForPipelines = new PadDepositPipeline(permanentDepositInformation, padPipeline);
        padDepositPipelineRepository.save(depositsForPipelines);
      }
    }
  }

  @Transactional
  public void removeDeposit(Integer depositId) {
    var permanentDeposit = permanentDepositRepository.findById(depositId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find permanent deposit with ID: %s", depositId)));

    depositDrawingsService.removeDepositFromDrawing(permanentDeposit);

    padDepositPipelineRepository.deleteAll(
        padDepositPipelineRepository.findAllByPadPermanentDeposit(permanentDeposit));
    permanentDepositRepository.delete(permanentDeposit);
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
        permanentDepositRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail);

    for (PadPermanentDeposit padPermanentDeposit : padPermanentDeposits) {
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


  public Optional<PadPermanentDeposit> getDepositById(int id) {
    return permanentDepositRepository.findById(id);
  }

  public List<PadPermanentDeposit> getPermanentDeposits(PwaApplicationDetail pwaApplicationDetail) {
    return permanentDepositRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail);
  }

  public List<PermanentDepositOverview> getPermanentDepositViews(PwaApplicationDetail pwaApplicationDetail) {
    return permanentDepositRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail)
        .stream()
        .map(permanentDepositEntityMappingService::createPermanentDepositOverview)
        .collect(Collectors.toList());
  }

  public PermanentDepositOverview createViewFromDepositId(Integer depositId) {
    var permanentDeposit = permanentDepositRepository.findById(depositId)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format("Couldn't find permanent deposit with ID: %s", depositId)));
    return permanentDepositEntityMappingService.createPermanentDepositOverview(permanentDeposit);

  }

  public Map<String, String> getEditUrlsForDeposits(PwaApplicationDetail pwaApplicationDetail) {
    Map<String, String> depositUrls = new HashMap<>();
    var permanentDeposits = permanentDepositRepository.findByPwaApplicationDetailOrderByReferenceAsc(
        pwaApplicationDetail);

    for (PadPermanentDeposit permanentDeposit : permanentDeposits) {
      depositUrls.put(permanentDeposit.getId().toString(),
          ReverseRouter.route(on(PermanentDepositController.class)
              .renderEditPermanentDeposits(
                  pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),
                  permanentDeposit.getId(), null, null)));
    }
    return depositUrls;
  }

  public Map<String, String> getRemoveUrlsForDeposits(PwaApplicationDetail pwaApplicationDetail) {
    Map<String, String> depositUrls = new HashMap<>();
    var permanentDeposits = permanentDepositRepository.findByPwaApplicationDetailOrderByReferenceAsc(
        pwaApplicationDetail);

    for (PadPermanentDeposit permanentDeposit : permanentDeposits) {
      depositUrls.put(permanentDeposit.getId().toString(),
          ReverseRouter.route(on(PermanentDepositController.class)
              .renderRemovePermanentDeposits(
                  pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),
                  permanentDeposit.getId(), null, null)));
    }
    return depositUrls;
  }


  public boolean isDepositReferenceUnique(String depositRef, Integer padDepositId,
                                          PwaApplicationDetail pwaApplicationDetail) {
    var existingDeposits = permanentDepositRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(
        pwaApplicationDetail, depositRef);
    return existingDeposits.isEmpty() || (existingDeposits.get().getId() != null && existingDeposits.get().getId().equals(
        padDepositId));
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return !pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION)
        && permanentDepositsAreToBeMadeOnApp(pwaApplicationDetail);

  }

  public boolean permanentDepositsAreToBeMadeOnApp(PwaApplicationDetail pwaApplicationDetail) {
    var projectInformation = padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail);
    if (projectInformation.isPresent()) {
      return BooleanUtils.isTrue(projectInformation.get().getPermanentDepositsMade())
          || pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.DEPOSIT_CONSENT);
    }
    return false;
  }

  public boolean hasPermanentDepositBeenMade(PwaApplicationDetail pwaApplicationDetail) {
    return permanentDepositRepository.countByPwaApplicationDetail(pwaApplicationDetail) > 0;
  }

  @VisibleForTesting
  public void removePadPipelineDepositLinks(PadPipeline padPipeline) {
    var depositPipelineLinks = padDepositPipelineRepository.getAllByPadPipeline(padPipeline);
    padDepositPipelineRepository.deleteAll(depositPipelineLinks);
  }

  @Transactional
  public void removePadPipelineFromDeposits(PadPipeline padPipeline) {

    this.removePadPipelineDepositLinks(padPipeline);

    var pwaApplicationDetail = padPipeline.getPwaApplicationDetail();
    var deposits = permanentDepositRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
    Map<PadPermanentDeposit, List<PadDepositPipeline>> depositMap =
        padDepositPipelineRepository.getAllByPadPipeline_PwaApplicationDetail(pwaApplicationDetail)
            .stream()
            .collect(Collectors.groupingBy(PadDepositPipeline::getPadPermanentDeposit));

    var depositsToRemove = CleanupUtils.getUnlinkedKeys(deposits, depositMap,
        (key, value) -> key.getId().equals(value.getId()));

    if (!depositsToRemove.isEmpty()) {
      depositDrawingsService.removeDepositsFromDrawings(depositsToRemove);
      permanentDepositRepository.deleteAll(depositsToRemove);
    }
  }

  @Override
  public void cleanupData(PwaApplicationDetail detail) {

    var updatedDepositsList = getPermanentDeposits(detail).stream()
        .filter(deposit -> deposit.getMaterialType() != null)
        .peek(deposit -> {

          switch (deposit.getMaterialType()) {

            case CONCRETE_MATTRESSES:
              deposit.setMaterialSize(null);
              cleanupOtherMaterial(deposit);
              cleanupGroutBags(deposit);
              break;
            case ROCK:
              cleanupMattresses(deposit);
              cleanupGroutBags(deposit);
              cleanupOtherMaterial(deposit);
              break;
            case GROUT_BAGS:
              cleanupMattresses(deposit);
              cleanupOtherMaterial(deposit);
              break;
            case OTHER:
              cleanupMattresses(deposit);
              cleanupGroutBags(deposit);
              break;
            default:
              break;
          }

        })
        .collect(Collectors.toList());

    permanentDepositRepository.saveAll(updatedDepositsList);

  }

  @Transactional
  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {


    // 1. copy deposits
    var copiedPermanentDepositEntityIds = entityCopyingService.duplicateEntitiesAndSetParent(
        () -> permanentDepositRepository.getAllByPwaApplicationDetail(fromDetail),
        toDetail,
        PadPermanentDeposit.class
    );

    // 2. copy deposit pipeline links
    var copiedPermanentDepositPipelineEntityIds = entityCopyingService.duplicateEntitiesAndSetParentFromCopiedEntities(
        () -> padDepositPipelineRepository.getAllByPadPermanentDeposit_PwaApplicationDetail(fromDetail),
        copiedPermanentDepositEntityIds,
        PadDepositPipeline.class
    );

    // 3. update deposit pipeline links to match pipelines to the new versions duplicates
    var reconciledPadPipelines = padPipelineReconcilerService.reconcileApplicationDetailPadPipelines(
        fromDetail,
        toDetail
    );

    var toDetailPermanentDepositPipelines = padDepositPipelineRepository.getAllByPadPermanentDeposit_PwaApplicationDetail(toDetail);

    toDetailPermanentDepositPipelines.forEach(padDepositPipeline -> {
      padDepositPipeline.setPadPipeline(
          reconciledPadPipelines.findByPipelineIdOrError(
            padDepositPipeline.getPadPipeline().getPipelineId()
          ).getReconciledPadPipeline()
      );
    });

  }

  private void cleanupGroutBags(PadPermanentDeposit deposit) {
    deposit.setGroutBagsBioDegradable(null);
    deposit.setBagsNotUsedDescription(null);
  }

  private void cleanupMattresses(PadPermanentDeposit deposit) {
    deposit.setConcreteMattressDepth(null);
    deposit.setConcreteMattressLength(null);
    deposit.setConcreteMattressWidth(null);
  }

  private void cleanupOtherMaterial(PadPermanentDeposit deposit) {
    deposit.setOtherMaterialType(null);
  }

}

