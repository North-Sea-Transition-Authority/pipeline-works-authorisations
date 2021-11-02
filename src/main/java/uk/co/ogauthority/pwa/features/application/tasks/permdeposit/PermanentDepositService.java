package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PhysicalPipelineState;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadOptionConfirmedService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.controller.PermanentDepositController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineAndIdentViewFactory;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.entitycopier.CopiedEntityIdTuple;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult;


/* Service providing simplified API for Permanent Deposit app form */
@Service
public class PermanentDepositService implements ApplicationFormSectionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PermanentDepositService.class);

  private final PadPermanentDepositRepository permanentDepositRepository;
  private final PermanentDepositEntityMappingService permanentDepositEntityMappingService;
  private final PermanentDepositsValidator permanentDepositsValidator;
  private final SpringValidatorAdapter groupValidator;
  private final PadDepositPipelineRepository padDepositPipelineRepository;
  private final PadProjectInformationService padProjectInformationService;
  private final DepositDrawingsService depositDrawingsService;
  private final EntityCopyingService entityCopyingService;
  private final PipelineAndIdentViewFactory pipelineAndIdentViewFactory;
  private final PadFileService padFileService;
  private final PipelineDetailService pipelineDetailService;
  private final PadOptionConfirmedService padOptionConfirmedService;

  @Autowired
  public PermanentDepositService(
      PadPermanentDepositRepository permanentDepositRepository,
      @Lazy DepositDrawingsService depositDrawingsService,
      PermanentDepositEntityMappingService permanentDepositEntityMappingService,
      PermanentDepositsValidator permanentDepositsValidator,
      SpringValidatorAdapter groupValidator,
      PadDepositPipelineRepository padDepositPipelineRepository,
      PadProjectInformationService padProjectInformationService,
      EntityCopyingService entityCopyingService,
      PipelineAndIdentViewFactory pipelineAndIdentViewFactory,
      PadFileService padFileService,
      PipelineDetailService pipelineDetailService,
      PadOptionConfirmedService padOptionConfirmedService) {
    this.permanentDepositRepository = permanentDepositRepository;
    this.depositDrawingsService = depositDrawingsService;
    this.permanentDepositEntityMappingService = permanentDepositEntityMappingService;
    this.permanentDepositsValidator = permanentDepositsValidator;
    this.groupValidator = groupValidator;
    this.padProjectInformationService = padProjectInformationService;
    this.padDepositPipelineRepository = padDepositPipelineRepository;
    this.entityCopyingService = entityCopyingService;
    this.pipelineAndIdentViewFactory = pipelineAndIdentViewFactory;
    this.padFileService = padFileService;
    this.pipelineDetailService = pipelineDetailService;
    this.padOptionConfirmedService = padOptionConfirmedService;
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
        depositsForPipeline -> String.valueOf(depositsForPipeline.getPipeline().getId()))
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
    var pipelineIdAndOverviewMap = pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(
        padPermanentDeposit.getPwaApplicationDetail(),
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ALL_CURRENT_STATUS_PIPELINES);
    return permanentDepositEntityMappingService.createPermanentDepositOverview(padPermanentDeposit,
        pipelineIdAndOverviewMap);
  }

  /**
   * From the form extract form data which should be persisted.
   */
  @Transactional
  public void saveEntityUsingForm(PwaApplicationDetail detail,
                                  PermanentDepositsForm form,
                                  WebUserAccount user) {
    var permanentDeposit = new PadPermanentDeposit();
    permanentDeposit.setPwaApplicationDetail(detail);
    permanentDepositEntityMappingService.setEntityValuesUsingForm(permanentDeposit, form);
    permanentDeposit = permanentDepositRepository.save(permanentDeposit);

    var existingDepositPipelines = padDepositPipelineRepository.findAllByPadPermanentDeposit(permanentDeposit);
    padDepositPipelineRepository.deleteAll(existingDepositPipelines);

    if (form.getSelectedPipelines() != null) {
      for (String pipelineId : form.getSelectedPipelines()) {
        if (!pipelineId.equals("")) {
          var pipeline = new Pipeline();
          pipeline.setMasterPwa(detail.getMasterPwa());
          pipeline.setId(Integer.parseInt(pipelineId));
          var depositsForPipelines = new PadDepositPipeline(permanentDeposit, pipeline);
          padDepositPipelineRepository.save(depositsForPipelines);
        }
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
    return getDepositSummaryScreenValidationResult(detail).isSectionComplete();
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {

    var projectInfo = padProjectInformationService.getPadProjectInformationData(pwaApplicationDetail);
    var existingDeposits = permanentDepositRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
    var validationHints = new PermanentDepositsValidationHints(
        pwaApplicationDetail, projectInfo.getProposedStartTimestamp(), existingDeposits);
    permanentDepositsValidator.validate(form, bindingResult, validationHints);

    return bindingResult;
  }

  public SummaryScreenValidationResult getDepositSummaryScreenValidationResult(PwaApplicationDetail pwaApplicationDetail) {
    List<PadPermanentDeposit> padPermanentDeposits =
        permanentDepositRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail);

    Map<String, String> invalidDepositIdToDescriptorMap = new LinkedHashMap<>();

    for (PadPermanentDeposit padPermanentDeposit : padPermanentDeposits) {
      var depositForm = new PermanentDepositsForm();
      mapEntityToForm(padPermanentDeposit, depositForm);

      BindingResult bindingResult = new BeanPropertyBindingResult(depositForm, "form");
      validate(depositForm, bindingResult, ValidationType.FULL, pwaApplicationDetail);
      if (bindingResult.hasErrors()) {
        invalidDepositIdToDescriptorMap.put(
            String.valueOf(padPermanentDeposit.getId()), padPermanentDeposit.getReference());
      }
    }

    var sectionComplete = invalidDepositIdToDescriptorMap.isEmpty() && !padPermanentDeposits.isEmpty();
    String sectionIncompleteError = !sectionComplete
        ? "Ensure that at least one deposit has been added and that they are all valid." : null;

    return new SummaryScreenValidationResult(invalidDepositIdToDescriptorMap, "deposit", "must have all sections completed without errors",
        sectionComplete,
        sectionIncompleteError);
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
        .map(deposit -> {
          var pipelineIdAndOverviewMap = pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwaByPipelineIds(
              pwaApplicationDetail, getPipelineIdsForDeposit(deposit));
          return permanentDepositEntityMappingService.createPermanentDepositOverview(deposit, pipelineIdAndOverviewMap);
        })
        .collect(Collectors.toList());
  }


  public Map<PadPermanentDeposit, List<PadDepositPipeline>> getDepositForDepositPipelinesMap(PwaApplicationDetail pwaApplicationDetail) {
    return padDepositPipelineRepository.getAllByPadPermanentDeposit_PwaApplicationDetail(pwaApplicationDetail)
        .stream()
        .collect(Collectors.groupingBy(PadDepositPipeline::getPadPermanentDeposit));
  }

  public List<PadPermanentDeposit> getAllDepositsWithPipelinesFromOtherApps(PwaApplicationDetail pwaApplicationDetail) {
    return permanentDepositRepository.getAllByPwaApplicationDetailAndDepositIsForPipelinesOnOtherApp(pwaApplicationDetail, true);
  }

  private List<PipelineId> getPipelineIdsForDeposit(PadPermanentDeposit deposit) {
    return padDepositPipelineRepository.findAllByPadPermanentDeposit(deposit)
        .stream()
        .map(link -> link.getPipeline().getPipelineId())
        .collect(Collectors.toList());
  }

  public Map<String, String> getPipelinesMapForDeposits(PwaApplicationDetail pwaApplicationDetail) {
    Map<String, String> pipelinesIdAndNameMap = new LinkedHashMap<>();
    pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(
        pwaApplicationDetail,
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ALL_CURRENT_STATUS_PIPELINES
    )
        .entrySet().stream()
        .filter(entry -> entry.getValue().getPipelineStatus().getPhysicalPipelineState() == PhysicalPipelineState.ON_SEABED)
        .forEach(entry ->
            pipelinesIdAndNameMap.put(String.valueOf(entry.getKey().getPipelineIdAsInt()), entry.getValue().getPipelineName()));
    return pipelinesIdAndNameMap;
  }

  public PermanentDepositOverview createViewFromDepositId(Integer depositId) {
    var permanentDeposit = permanentDepositRepository.findById(depositId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find permanent deposit with ID: %s", depositId)));
    var pipelineIdAndOverviewMap = pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwaByPipelineIds(
        permanentDeposit.getPwaApplicationDetail(), getPipelineIdsForDeposit(permanentDeposit));
    return permanentDepositEntityMappingService.createPermanentDepositOverview(permanentDeposit,
        pipelineIdAndOverviewMap);

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

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    var isNotOptionsOrIsOptionsAndOptionCompleted =
        !pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION)
            || padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail);

    return isNotOptionsOrIsOptionsAndOptionCompleted
        && permanentDepositsAreToBeMadeOnApp(pwaApplicationDetail);

  }

  public boolean permanentDepositsAreToBeMadeOnApp(PwaApplicationDetail pwaApplicationDetail) {
    if (pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.DEPOSIT_CONSENT)) {
      return true;
    }

    return padProjectInformationService.getPermanentDepositsOnApplication(pwaApplicationDetail);

  }

  public boolean hasPermanentDepositBeenMade(PwaApplicationDetail pwaApplicationDetail) {
    return permanentDepositRepository.countByPwaApplicationDetail(pwaApplicationDetail) > 0;
  }


  @Transactional
  public void removePadPipelineFromDeposits(PadPipeline padPipeline) {

    var isPipelineConsented = pipelineDetailService.isPipelineConsented(padPipeline.getPipeline());

    if (!isPipelineConsented) {
      List<PadPermanentDeposit> depositsToRemove = new ArrayList<>();
      var depositsLinksForPipelineAndApp = padDepositPipelineRepository.getAllByPadPermanentDeposit_PwaApplicationDetailAndPipeline(
          padPipeline.getPwaApplicationDetail(), padPipeline.getPipeline());

      for (var depositsLinkForPipelineAndApp : depositsLinksForPipelineAndApp) {
        var deposit = depositsLinkForPipelineAndApp.getPadPermanentDeposit();
        var totalPipelinesLinkedToDeposit = padDepositPipelineRepository.countAllByPadPermanentDeposit(deposit);
        if (totalPipelinesLinkedToDeposit == 1 && !deposit.getDepositIsForPipelinesOnOtherApp()) {
          depositsToRemove.add(deposit);
        }
      }

      padDepositPipelineRepository.deleteAll(depositsLinksForPipelineAndApp);
      if (!depositsToRemove.isEmpty()) {
        depositDrawingsService.removeDepositsFromDrawings(depositsToRemove);
        permanentDepositRepository.deleteAll(depositsToRemove);
      }
    }

  }


  @Override
  public void cleanupData(PwaApplicationDetail detail) {

    var deposits = getPermanentDeposits(detail);

    for (var deposit : deposits) {
      if (deposit.getMaterialType() != null) {
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
      }

      cleanupPipelines(deposit);
    }

    permanentDepositRepository.saveAll(deposits);
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

    // 3. duplicate all deposits drawing
    var copiedDepositDrawingEntityIds = entityCopyingService.duplicateEntitiesAndSetParent(
        () -> depositDrawingsService.getAllDepositDrawingsForDetail(fromDetail),
        toDetail,
        PadDepositDrawing.class
    );

    //4. duplicate all drawing files and point duplicated drawings at new versions
    var copiedDrawingPadFileEntityIds = padFileService.copyPadFilesToPwaApplicationDetail(
        fromDetail, toDetail, ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS, ApplicationFileLinkStatus.FULL
    );

    var toDetailDrawingPadFileLookup = padFileService.getAllByPwaApplicationDetailAndPurpose(
        toDetail,
        ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS
    ).stream()
        .collect(Collectors.toMap(PadFile::getId, padFile -> padFile));

    var duplicatedPadFileMap = copiedDrawingPadFileEntityIds.stream()
        .collect(Collectors.toMap(
            CopiedEntityIdTuple::getOriginalEntityId,
            padFileCopiedEntityIdTuple -> toDetailDrawingPadFileLookup.get(
                padFileCopiedEntityIdTuple.getDuplicateEntityId()))
        );

    // use map of original pad file id to duplicated pad file id to set correct padFile link on deposit drawing.
    var toDetailDepositDrawings = depositDrawingsService.getAllDepositDrawingsForDetail(toDetail);
    toDetailDepositDrawings.forEach(padDepositDrawing -> {
      padDepositDrawing.setFile(duplicatedPadFileMap.get(padDepositDrawing.getFile().getId()));
    });
    depositDrawingsService.saveDepositDrawings(toDetailDepositDrawings);

    //6. duplicate all drawing links and repoint at duplicated perm deposits
    var copiedDrawingLinkEntityIds = entityCopyingService.duplicateEntitiesAndSetParentFromCopiedEntities(
        () -> depositDrawingsService.getAllDepositDrawingLinksByDetailPermanentDeposits(fromDetail),
        copiedPermanentDepositEntityIds,
        PadDepositDrawingLink.class
    );

    // id of duplicated drawing to duplicated drawing entity
    var toDetailDepositDrawingLookup = toDetailDepositDrawings.stream()
        .collect(Collectors.toMap(PadDepositDrawing::getId, depositDrawing -> depositDrawing));

    var originalDrawingIdToDuplicatedDrawingIdLookup = copiedDepositDrawingEntityIds.stream()
        .collect(Collectors.toMap(
            CopiedEntityIdTuple::getOriginalEntityId,
            CopiedEntityIdTuple::getDuplicateEntityId));

    var toDetailDrawingLinks = depositDrawingsService.getAllDepositDrawingLinksByDetailPermanentDeposits(toDetail);

    toDetailDrawingLinks.forEach(padDepositDrawingLink -> {
      var duplicatedDepositDrawing = toDetailDepositDrawingLookup.get(
          originalDrawingIdToDuplicatedDrawingIdLookup.get(padDepositDrawingLink.getPadDepositDrawing().getId())
      );
      padDepositDrawingLink.setPadDepositDrawing(duplicatedDepositDrawing);
    });

    depositDrawingsService.saveDepositDrawingLinks(toDetailDrawingLinks);

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

  private void cleanupPipelines(PadPermanentDeposit deposit) {
    if (deposit.getDepositForConsentedPipeline() == null || !deposit.getDepositForConsentedPipeline()) {
      var depositPipelineLinks = padDepositPipelineRepository.findAllByPadPermanentDeposit(deposit);
      padDepositPipelineRepository.deleteAll(depositPipelineLinks);
    }
    if (deposit.getDepositIsForPipelinesOnOtherApp() == null || !deposit.getDepositIsForPipelinesOnOtherApp()) {
      deposit.setAppRefAndPipelineNum(null);
    }
  }

}

