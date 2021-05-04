package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineHeaderFormContext;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.ModifyPipelineForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineBundlePairDto;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineMappingService;
import uk.co.ogauthority.pwa.util.CoordinateUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class PadPipelineService {

  private final PadPipelineRepository padPipelineRepository;
  private final PipelineService pipelineService;
  private final PipelineDetailService pipelineDetailService;
  private final PadPipelinePersisterService padPipelinePersisterService;
  private final PipelineHeaderFormValidator pipelineHeaderFormValidator;
  private final PipelineMappingService pipelineMappingService;

  private static final Set<PipelineStatus> DATA_REQUIRED_STATUSES = Set.of(PipelineStatus.IN_SERVICE, PipelineStatus.OUT_OF_USE_ON_SEABED);

  @Autowired
  public PadPipelineService(PadPipelineRepository padPipelineRepository,
                            PipelineService pipelineService,
                            PipelineDetailService pipelineDetailService,
                            PadPipelinePersisterService padPipelinePersisterService,
                            PipelineHeaderFormValidator pipelineHeaderFormValidator,
                            PipelineMappingService pipelineMappingService) {
    this.padPipelineRepository = padPipelineRepository;
    this.pipelineService = pipelineService;
    this.pipelineDetailService = pipelineDetailService;
    this.padPipelinePersisterService = padPipelinePersisterService;
    this.pipelineHeaderFormValidator = pipelineHeaderFormValidator;
    this.pipelineMappingService = pipelineMappingService;
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

  public boolean canShowAlreadyExistsOnSeabedQuestions(PadPipeline padPipeline, PwaApplicationType pwaApplicationType) {
    return (PwaApplicationType.CAT_2_VARIATION.equals(pwaApplicationType)
        || PwaApplicationType.DECOMMISSIONING.equals(pwaApplicationType))
        && PipelineHeaderFormContext.NON_CONSENTED_PIPELINE.equals(getPipelineHeaderFormContext(padPipeline));
  }

  public boolean canShowAlreadyExistsOnSeabedQuestions(PwaApplicationType pwaApplicationType) {
    return canShowAlreadyExistsOnSeabedQuestions(null, pwaApplicationType);
  }

  public PipelineHeaderFormContext getPipelineHeaderFormContext(PadPipeline padPipeline) {
    if (padPipeline == null || !pipelineDetailService.isPipelineConsented(padPipeline.getPipeline())) {
      return PipelineHeaderFormContext.NON_CONSENTED_PIPELINE;
    }
    return PipelineHeaderFormContext.CONSENTED_PIPELINE;
  }

  @Transactional
  public PadPipeline addPipeline(PwaApplicationDetail pwaApplicationDetail, PipelineHeaderForm form) {

    var newPipeline = pipelineService.createApplicationPipeline(pwaApplicationDetail.getPwaApplication());

    var newPadPipeline = new PadPipeline(pwaApplicationDetail);
    newPadPipeline.setPipeline(newPipeline);
    newPadPipeline.setPipelineStatus(PipelineStatus.IN_SERVICE);

    // N.B. this temporary reference format is intended. Applicants need a reference for a pipeline that they can use in their
    // schematic drawings, mention in text etc while filling in the application. PL numbers are only assigned after submission.
    Integer maxTemporaryNumber = padPipelineRepository.getMaxTemporaryNumberByPwaApplicationDetail(
        pwaApplicationDetail);

    newPadPipeline.setTemporaryNumber(maxTemporaryNumber + 1);
    newPadPipeline.setPipelineRef("TEMPORARY " + newPadPipeline.getTemporaryNumber());

    saveEntityUsingForm(newPadPipeline, form);

    return newPadPipeline;
  }

  public void saveEntityUsingForm(PadPipeline padPipeline, PipelineHeaderForm form) {

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
    }
    padPipeline.setPipelineDesignLife(form.getPipelineDesignLife());
    padPipeline.setPipelineInBundle(form.getPipelineInBundle());
    if (BooleanUtils.isTrue(form.getPipelineInBundle())) {
      padPipeline.setBundleName(form.getBundleName());
    } else {
      padPipeline.setBundleName(null);
    }

    padPipeline.setPipelineStatusReason(form.getWhyNotReturnedToShore());

    if (PipelineHeaderFormContext.NON_CONSENTED_PIPELINE.equals(getPipelineHeaderFormContext(padPipeline))) {
      padPipeline.setAlreadyExistsOnSeabed(form.getAlreadyExistsOnSeabed());
      if (BooleanUtils.isTrue(form.getAlreadyExistsOnSeabed())) {
        padPipeline.setPipelineInUse(form.getPipelineInUse());
      } else {
        padPipeline.setPipelineInUse(null);
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

    if (PipelineHeaderFormContext.NON_CONSENTED_PIPELINE.equals(getPipelineHeaderFormContext(padPipeline))) {
      form.setAlreadyExistsOnSeabed(padPipeline.getAlreadyExistsOnSeabed());
      form.setPipelineInUse(padPipeline.getPipelineInUse());
    }

    form.setFootnote(padPipeline.getFootnote());
  }

  @Transactional
  public void updatePipeline(PadPipeline pipeline, PipelineHeaderForm form) {
    saveEntityUsingForm(pipeline, form);
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
          padPipeline.getPipelineStatus(), canShowAlreadyExistsOnSeabedQuestions(padPipeline, pwaApplicationType));
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


}
