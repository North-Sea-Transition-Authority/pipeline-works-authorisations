package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PhysicalPipelineState;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineBundlePairDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineMaterial;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.importconsented.ModifyPipelineForm;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinateUtils;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineMappingService;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class PadPipelineService {

  private final PadPipelineRepository padPipelineRepository;
  private final PipelineService pipelineService;
  private final PipelineDetailService pipelineDetailService;
  private final PadPipelinePersisterService padPipelinePersisterService;
  private final PipelineHeaderFormValidator pipelineHeaderFormValidator;
  private final PipelineMappingService pipelineMappingService;
  private final PipelineHeaderService pipelineHeaderService;

  private static final Set<PipelineStatus> DATA_REQUIRED_STATUSES = PipelineStatus.getStatusesWithState(PhysicalPipelineState.ON_SEABED);
  private static final Set<PipelineStatus> INACTIVE_STATUSES = EnumSet.complementOf(EnumSet.copyOf(
      PipelineStatus.getStatusesWithState(PhysicalPipelineState.ON_SEABED)));

  @Autowired
  public PadPipelineService(PadPipelineRepository padPipelineRepository,
                            PipelineService pipelineService,
                            PipelineDetailService pipelineDetailService,
                            PadPipelinePersisterService padPipelinePersisterService,
                            PipelineHeaderFormValidator pipelineHeaderFormValidator,
                            PipelineMappingService pipelineMappingService,
                            PipelineHeaderService pipelineHeaderService) {
    this.padPipelineRepository = padPipelineRepository;
    this.pipelineService = pipelineService;
    this.pipelineDetailService = pipelineDetailService;
    this.padPipelinePersisterService = padPipelinePersisterService;
    this.pipelineHeaderFormValidator = pipelineHeaderFormValidator;
    this.pipelineMappingService = pipelineMappingService;
    this.pipelineHeaderService = pipelineHeaderService;
  }

  public List<PadPipeline> getPipelines(PwaApplicationDetail detail) {
    return padPipelineRepository.getAllByPwaApplicationDetail(detail);
  }

  public Set<PipelineId> getMasterPipelineIds(PwaApplicationDetail detail) {
    return padPipelineRepository.getMasterPipelineIdsOnApplication(detail);
  }

  public PipelineOverview getPipelineOverview(PadPipeline padPipeline) {

    return padPipelineRepository.findPipelineAsSummaryDtoByPadPipeline(padPipeline)
        .map(padPipelineSummaryDto -> PadPipelineOverview.from(
            padPipelineSummaryDto,
            false
        ))
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Pipeline Summary not found. Pad pipeline id: " + padPipeline.getId()));
  }

  public List<PadPipeline> findSubmittedOrDraftPipelinesWithPipelineNumber(String pipelineNumber) {
    return padPipelineRepository.findApplicationsWherePipelineNumberExistsOnDraftOrLastSubmittedVersion(pipelineNumber);
  }

  public List<PipelineOverview> getApplicationPipelineOverviews(PwaApplicationDetail detail) {

    return padPipelineRepository.findAllPipelinesAsSummaryDtoByPwaApplicationDetail(detail).stream()
        .map(padPipelineSummaryDto -> PadPipelineOverview.from(
            padPipelineSummaryDto,
            false
        ))
        .sorted(Comparator.comparing(PipelineOverview::getPipelineNumber))
        .collect(Collectors.toList());

  }

  public boolean isValidationRequiredByStatus(PipelineStatus pipelineStatus) {
    return DATA_REQUIRED_STATUSES.contains(pipelineStatus);
  }

  @Transactional
  public PadPipeline addPipeline(PwaApplicationDetail pwaApplicationDetail,
                                 PipelineHeaderForm form,
                                 Set<PipelineHeaderQuestion> requiredQuestions) {

    var newPipeline = pipelineService.createApplicationPipeline(pwaApplicationDetail.getPwaApplication());

    var newPadPipeline = new PadPipeline(pwaApplicationDetail);
    newPadPipeline.setPipeline(newPipeline);

    // N.B. this temporary reference format is intended. Applicants need a reference for a pipeline that they can use in their
    // schematic drawings, mention in text etc while filling in the application. PL numbers are only assigned after submission.
    Integer maxTemporaryNumber = padPipelineRepository.getMaxTemporaryNumberByPwaApplicationDetail(
        pwaApplicationDetail);

    newPadPipeline.setTemporaryNumber(maxTemporaryNumber + 1);
    newPadPipeline.setPipelineRef("TEMPORARY " + newPadPipeline.getTemporaryNumber());
    newPadPipeline.setPipelineStatus(PipelineStatus.IN_SERVICE);

    saveEntityUsingForm(newPadPipeline, form, requiredQuestions);

    return newPadPipeline;

  }

  public void saveEntityUsingForm(PadPipeline padPipeline,
                                  PipelineHeaderForm form,
                                  Set<PipelineHeaderQuestion> requiredQuestions) {

    padPipeline.setPipelineType(form.getPipelineType());

    padPipeline.setFromLocation(form.getFromLocation());
    padPipeline.setFromCoordinates(CoordinateUtils.coordinatePairFromForm(form.getFromCoordinateForm()));
    padPipeline.setToLocation(form.getToLocation());
    padPipeline.setToCoordinates(CoordinateUtils.coordinatePairFromForm(form.getToCoordinateForm()));

    padPipeline.setComponentPartsDescription(form.getComponentPartsDescription());
    padPipeline.setLength(form.getLength());
    padPipeline.setProductsToBeConveyed(form.getProductsToBeConveyed());
    padPipeline.setTrenchedBuriedBackfilled(form.getTrenchedBuriedBackfilled());

    if (form.getTrenchedBuriedBackfilled()) {
      padPipeline.setTrenchingMethodsDescription(form.getTrenchingMethods());
    }

    padPipeline.setPipelineFlexibility(form.getPipelineFlexibility());

    padPipeline.setPipelineMaterial(form.getPipelineMaterial());
    if (form.getPipelineMaterial().equals(PipelineMaterial.OTHER)) {
      padPipeline.setOtherPipelineMaterialUsed(form.getOtherPipelineMaterialUsed());
    } else {
      padPipeline.setOtherPipelineMaterialUsed(null);
    }

    padPipeline.setPipelineDesignLife(form.getPipelineDesignLife());
    padPipeline.setPipelineInBundle(form.getPipelineInBundle());
    if (BooleanUtils.isTrue(form.getPipelineInBundle())) {
      padPipeline.setBundleName(form.getBundleName());
    } else {
      padPipeline.setBundleName(null);
    }

    padPipeline.setPipelineStatusReason(form.getWhyNotReturnedToShore());

    if (requiredQuestions.contains(PipelineHeaderQuestion.ALREADY_EXISTS_ON_SEABED)) {

      padPipeline.setAlreadyExistsOnSeabed(form.getAlreadyExistsOnSeabed());

      boolean alreadyExists = BooleanUtils.isTrue(padPipeline.getAlreadyExistsOnSeabed());

      if (alreadyExists) {
        padPipeline.setPipelineInUse(form.getPipelineInUse());
      } else {
        padPipeline.setPipelineInUse(null);
      }

      boolean inUse = BooleanUtils.isTrue(padPipeline.getPipelineInUse());

      if (alreadyExists && !inUse) {
        padPipeline.setPipelineStatus(PipelineStatus.OUT_OF_USE_ON_SEABED);
      } else {
        padPipeline.setPipelineStatus(PipelineStatus.IN_SERVICE);
      }

    }

    padPipeline.setFootnote(form.getFootnote());

    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(padPipeline);

  }

  public void mapEntityToForm(PipelineHeaderForm form, PadPipeline padPipeline) {

    form.setPipelineType(padPipeline.getPipelineType());

    form.setFromLocation(padPipeline.getFromLocation());
    form.setFromCoordinateForm(new CoordinateForm());
    CoordinateUtils.mapCoordinatePairToForm(padPipeline.getFromCoordinates(), form.getFromCoordinateForm());

    form.setToLocation(padPipeline.getToLocation());
    form.setToCoordinateForm(new CoordinateForm());
    CoordinateUtils.mapCoordinatePairToForm(padPipeline.getToCoordinates(), form.getToCoordinateForm());

    form.setProductsToBeConveyed(padPipeline.getProductsToBeConveyed());
    form.setLength(padPipeline.getLength());
    form.setComponentPartsDescription(padPipeline.getComponentPartsDescription());

    form.setTrenchedBuriedBackfilled(padPipeline.getTrenchedBuriedBackfilled());

    Optional.ofNullable(form.getTrenchedBuriedBackfilled())
        .filter(tru -> tru)
        .ifPresent(t -> form.setTrenchingMethods(padPipeline.getTrenchingMethodsDescription()));

    form.setPipelineFlexibility(padPipeline.getPipelineFlexibility());
    form.setPipelineMaterial(padPipeline.getPipelineMaterial());
    form.setOtherPipelineMaterialUsed(padPipeline.getOtherPipelineMaterialUsed());
    form.setPipelineDesignLife(padPipeline.getPipelineDesignLife());

    form.setPipelineInBundle(padPipeline.getPipelineInBundle());
    form.setBundleName(padPipeline.getBundleName());

    if (padPipeline.getPipelineStatus().equals(PipelineStatus.OUT_OF_USE_ON_SEABED)) {
      form.setWhyNotReturnedToShore(padPipeline.getPipelineStatusReason());
    }

    form.setAlreadyExistsOnSeabed(padPipeline.getAlreadyExistsOnSeabed());
    form.setPipelineInUse(padPipeline.getPipelineInUse());

    form.setFootnote(padPipeline.getFootnote());
  }

  @Transactional
  public void updatePipeline(PadPipeline pipeline,
                             PipelineHeaderForm form,
                             Set<PipelineHeaderQuestion> requiredQuestions) {
    saveEntityUsingForm(pipeline, form, requiredQuestions);
  }

  public PadPipeline getById(Integer padPipelineId) {
    return padPipelineRepository.findById(padPipelineId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find PadPipeline with ID: %s", padPipelineId)));
  }

  public Optional<PadPipeline> findByPwaApplicationDetailAndPipelineId(PwaApplicationDetail pwaApplicationDetail,
                                                                       PipelineId pipelineId) {
    return padPipelineRepository.findByPwaApplicationDetailAndPipeline_Id(pwaApplicationDetail, pipelineId.asInt());
  }


  public List<PadPipeline> getByIdList(PwaApplicationDetail detail, List<Integer> padPipelineIds) {
    var padPipelineIdList = ListUtils.emptyIfNull(padPipelineIds);
    if (padPipelineIdList.size() > 0) {
      return padPipelineRepository.getAllByPwaApplicationDetailAndIdIn(detail, padPipelineIdList);
    }
    return List.of();
  }

  @VisibleForTesting
  public List<PipelineBundlePairDto> getPipelineBundleNamesByDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padPipelineRepository.getBundleNamesByPwaApplicationDetail(pwaApplicationDetail);
  }

  public Set<String> getAvailableBundleNamesForApplication(PwaApplicationDetail detail) {
    var applicationBundlePairDtos = getPipelineBundleNamesByDetail(detail);
    var consentedBundlePairDtos = pipelineDetailService.getSimilarPipelineBundleNamesByDetail(detail);

    Set<String> availableConsentedBundleNames = consentedBundlePairDtos.stream()
        .filter(consentBundlePair -> applicationBundlePairDtos.stream()
            .noneMatch(appBundlePair -> appBundlePair.getPipelineId().equals(consentBundlePair.getPipelineId())))
        .map(PipelineBundlePairDto::getBundleName)
        .collect(Collectors.toUnmodifiableSet());

    Set<String> availableApplicationBundleNames = applicationBundlePairDtos.stream()
        .map(PipelineBundlePairDto::getBundleName)
        .collect(Collectors.toUnmodifiableSet());

    List<String> filteredBundleNames = new ArrayList<>();
    filteredBundleNames.addAll(availableApplicationBundleNames);
    filteredBundleNames.addAll(availableConsentedBundleNames);

    return Set.copyOf(filteredBundleNames);

  }


  public Map<String, String> getPipelineReferenceMap(PwaApplicationDetail pwaApplicationDetail) {
    return padPipelineRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)
        .stream()
        .sorted(Comparator.comparing(PadPipeline::getId))
        .collect(
            StreamUtils.toLinkedHashMap(padPipeline -> String.valueOf(padPipeline.getId()),
                PadPipeline::getPipelineRef));
  }

  public long getTotalPipelinesContainedInApplication(PwaApplicationDetail pwaApplicationDetail) {
    return padPipelineRepository.countAllByPwaApplicationDetail(pwaApplicationDetail);
  }

  @Transactional
  public PadPipeline copyDataToNewPadPipeline(PwaApplicationDetail detail, PipelineDetail pipelineDetail,
                                              ModifyPipelineForm form) {
    var newPadPipeline = new PadPipeline(detail);
    newPadPipeline.setPipeline(pipelineDetail.getPipeline());
    pipelineMappingService.mapPipelineEntities(newPadPipeline, pipelineDetail);

    newPadPipeline.setPipelineStatus(form.getPipelineStatus());
    if (newPadPipeline.getPipelineStatus() == PipelineStatus.OUT_OF_USE_ON_SEABED) {
      newPadPipeline.setPipelineStatusReason(form.getPipelineStatusReason());

    } else if (newPadPipeline.getPipelineStatus() == PipelineStatus.TRANSFERRED) {
      newPadPipeline.setPipelineTransferAgreed(form.getTransferAgreed());
    }

    padPipelineRepository.save(newPadPipeline);
    return newPadPipeline;
  }

  public boolean isPadPipelineValid(PadPipeline padPipeline, PwaApplicationType pwaApplicationType) {

    if (isValidationRequiredByStatus(padPipeline.getPipelineStatus())) {

      var form = new PipelineHeaderForm();

      mapEntityToForm(form, padPipeline);

      var bindingResult = new BeanPropertyBindingResult(form, "form");

      var validationHints = new PipelineHeaderValidationHints(
          pipelineHeaderService.getRequiredQuestions(padPipeline, pwaApplicationType)
      );

      pipelineHeaderFormValidator.validate(form, bindingResult, validationHints);

      return !bindingResult.hasErrors();

    }

    return true;

  }

  public Map<PadPipelineId, PadPipeline> getPadPipelineMapForOverviews(PwaApplicationDetail detail,
                                                                Collection<PipelineOverview> overviews) {
    List<Integer> pipelineIds = overviews.stream()
        .map(padPipelineOverview -> (PadPipelineOverview) padPipelineOverview)
        .map(PadPipelineOverview::getPadPipelineId)
        .collect(Collectors.toList());

    return getByIdList(detail, pipelineIds).stream()
        .collect(StreamUtils.toLinkedHashMap(
            padPipeline -> new PadPipelineId(padPipeline.getId()),
            padPipeline -> padPipeline
        ));
  }

  public Set<PipelineStatus> getPadPipelineInactiveStatuses() {
    return INACTIVE_STATUSES;
  }

  public List<PadPipeline> getPadPipelineByPipelines(List<Pipeline> pipelines) {
    return padPipelineRepository.findAllByPipelineIn(pipelines);
  }
}
