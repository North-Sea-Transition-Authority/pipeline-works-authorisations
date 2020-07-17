package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.PipelineIdentsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.PipelinesController;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineTaskListItem;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineBundlePairDto;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskInfo;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailService;
import uk.co.ogauthority.pwa.util.CoordinateUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class PadPipelineService implements ApplicationFormSectionService {

  private final PadPipelineRepository padPipelineRepository;
  private final PipelineService pipelineService;
  private final PipelineDetailService pipelineDetailService;
  private final PipelineIdentFormValidator pipelineIdentFormValidator;
  private final PadPipelineIdentService padPipelineIdentService;


  @Autowired
  public PadPipelineService(PadPipelineRepository padPipelineRepository,
                            PipelineService pipelineService,
                            PipelineDetailService pipelineDetailService,
                            PadPipelineIdentService padPipelineIdentService,
                            PipelineIdentFormValidator pipelineIdentFormValidator) {
    this.padPipelineRepository = padPipelineRepository;
    this.pipelineService = pipelineService;
    this.pipelineDetailService = pipelineDetailService;
    this.padPipelineIdentService = padPipelineIdentService;
    this.pipelineIdentFormValidator = pipelineIdentFormValidator;
  }

  public List<PadPipeline> getPipelines(PwaApplicationDetail detail) {
    return padPipelineRepository.getAllByPwaApplicationDetail(detail);
  }

  public List<Integer> getMasterPipelineIds(PwaApplicationDetail detail) {
    return padPipelineRepository.getMasterPipelineIdsOnApplication(detail);
  }

  public PipelineOverview getPipelineOverview(PadPipeline padPipeline) {

    return padPipelineRepository.findPipelineAsSummaryDtoByPadPipeline(padPipeline)
        .map(PadPipelineOverview::from)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Pipeline Summary not found. Pad pipeline id: " + padPipeline.getId()));
  }

  public List<PipelineOverview> getApplicationPipelineOverviews(PwaApplicationDetail detail) {

    return padPipelineRepository.findAllPipelinesAsSummaryDtoByPwaApplicationDetail(detail).stream()
        .map(PadPipelineOverview::from)
        .sorted(Comparator.comparing(PipelineOverview::getPipelineNumber))
        .collect(Collectors.toList());

  }

  public List<PadPipelineTaskListItem> getPipelineTaskListItems(PwaApplicationDetail detail) {

    return getApplicationPipelineOverviews(detail)
        .stream()
        .map(pipelineOverview -> new PadPipelineTaskListItem(
                pipelineOverview,
                createTaskListEntries(detail, pipelineOverview)
            )
        )
        .collect(Collectors.toList());

  }

  private List<TaskListEntry> createTaskListEntries(PwaApplicationDetail pwaApplicationDetail,
                                                    PipelineOverview pipelineOverview) {
    var editPipelineHeaderUrl = getEditPipelineHeaderUrl(
        pwaApplicationDetail.getMasterPwaApplicationId(),
        pwaApplicationDetail.getPwaApplicationType(),
        pipelineOverview.getPadPipelineId());

    var identTaskUrl = getPipelineIdentOverviewUrl(
        pwaApplicationDetail.getMasterPwaApplicationId(),
        pwaApplicationDetail.getPwaApplicationType(),
        pipelineOverview.getPadPipelineId()
    );

    return List.of(
        new TaskListEntry(
            "Header information",
            editPipelineHeaderUrl,
            true
        ),
        new TaskListEntry(
            "Idents",
            identTaskUrl,
            false,
            List.of(new TaskInfo("IDENT", pipelineOverview.getNumberOfIdents()))
        )
    );

  }

  private String getEditPipelineHeaderUrl(int applicationId, PwaApplicationType applicationType, int padPipelineId) {
    return ReverseRouter.route(on(PipelinesController.class).renderEditPipeline(
        applicationId,
        applicationType,
        padPipelineId,
        null,
        null
    ));
  }

  private String getPipelineIdentOverviewUrl(int applicationId, PwaApplicationType applicationType, int padPipelineId) {
    return ReverseRouter.route(on(PipelineIdentsController.class).renderIdentOverview(
        applicationId,
        applicationType,
        padPipelineId,
        null
    ));
  }

  @Transactional
  public PadPipeline addPipeline(PwaApplicationDetail pwaApplicationDetail, PipelineHeaderForm form) {

    var newPipeline = pipelineService.createApplicationPipeline(pwaApplicationDetail.getPwaApplication());

    var newPadPipeline = new PadPipeline(pwaApplicationDetail);
    newPadPipeline.setPipeline(newPipeline);

    // N.B. this temporary reference format is intended. Applicants need a reference for a pipeline that they can use in their
    // schematic drawings, mention in text etc while filling in the application. PL numbers are only assigned after submission.
    Long numberOfPipesForDetail = padPipelineRepository.countAllByPwaApplicationDetail(pwaApplicationDetail);
    // TODO PWA-341 this could cause duplicate pipeline numbers e.g
    // 1. Add new pipeline "TEMP 1"
    // 2. Add new pipeline "TEMP 2"
    // 3. Remove "TEMP 1"
    // 4. Add new pipeline "TEMP 2"!

    newPadPipeline.setPipelineRef("TEMPORARY " + (numberOfPipesForDetail.intValue() + 1));

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


    padPipelineRepository.save(padPipeline);

  }


  public void mapEntityToForm(PipelineHeaderForm form, PadPipeline pipeline) {

    form.setPipelineType(pipeline.getPipelineType());

    form.setFromLocation(pipeline.getFromLocation());
    form.setFromCoordinateForm(new CoordinateForm());
    CoordinateUtils.mapCoordinatePairToForm(pipeline.getFromCoordinates(), form.getFromCoordinateForm());

    form.setToLocation(pipeline.getToLocation());
    form.setToCoordinateForm(new CoordinateForm());
    CoordinateUtils.mapCoordinatePairToForm(pipeline.getToCoordinates(), form.getToCoordinateForm());

    form.setProductsToBeConveyed(pipeline.getProductsToBeConveyed());
    form.setLength(pipeline.getLength());
    form.setComponentPartsDescription(pipeline.getComponentPartsDescription());

    form.setTrenchedBuriedBackfilled(pipeline.getTrenchedBuriedBackfilled());

    Optional.ofNullable(form.getTrenchedBuriedBackfilled())
        .filter(tru -> tru)
        .ifPresent(t -> form.setTrenchingMethods(pipeline.getTrenchingMethodsDescription()));

    form.setPipelineFlexibility(pipeline.getPipelineFlexibility());
    form.setPipelineMaterial(pipeline.getPipelineMaterial());
    form.setOtherPipelineMaterialUsed(pipeline.getOtherPipelineMaterialUsed());
    form.setPipelineDesignLife(pipeline.getPipelineDesignLife());

    form.setPipelineInBundle(pipeline.getPipelineInBundle());
    form.setBundleName(pipeline.getBundleName());

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

  public List<PadPipeline> getByIdList(PwaApplicationDetail detail, List<Integer> padPipelineIds) {
    var padPipelineIdList = ListUtils.emptyIfNull(padPipelineIds);
    if (padPipelineIdList.size() > 0) {
      return padPipelineRepository.getAllByPwaApplicationDetailAndIdIn(detail, padPipelineIdList);
    }
    return List.of();
  }

  public List<PadPipeline> getPadPipelinesByMasterAndIds(MasterPwa master, List<Integer> padPipelineIds) {
    var padPipelineIdList = ListUtils.emptyIfNull(padPipelineIds);
    if (padPipelineIdList.size() > 0) {
      return padPipelineRepository.getPadPipelineByMasterPwaAndPipelineIds(master,
          padPipelineIdList);
    }
    return List.of();
  }

  public PadPipeline getPadPipelinesByMasterAndId(MasterPwa master, Integer padPipelineId) {
    return padPipelineRepository.getPadPipelineByMasterPwaAndPipelineId(master, padPipelineId);
  }

  public List<PadPipeline> getPadPipelinesByPadPipelineIds(Collection<Integer> padPipelineIds) {
    return IterableUtils.toList(padPipelineRepository.findAllById(padPipelineIds));
  }

  public Long getCountOfPipelinesByIdList(PwaApplicationDetail detail, List<Integer> pipelineIds) {
    return padPipelineRepository.countAllByPwaApplicationDetailAndIdIn(detail, pipelineIds);
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

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    for (var pipeline : getPipelines(detail)) {
      for (var ident : padPipelineIdentService.getIdentsByPipeline(pipeline)) {
        var identForm = new PipelineIdentForm();
        padPipelineIdentService.mapEntityToForm(ident, identForm);
        BindingResult bindingResult = new BeanPropertyBindingResult(identForm, "form");
        pipelineIdentFormValidator.validate(identForm, bindingResult, detail, pipeline.getCoreType());
        if (bindingResult.hasErrors()) {
          return false;
        }
      }
    }

    return padPipelineRepository.countAllByPwaApplicationDetail(detail) > 0L
        && padPipelineRepository.countAllWithNoIdentsByPwaApplicationDetail(detail) == 0L;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    throw new AssertionError("Doesn't make sense to implement this.");
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

  public List<PadPipelineSummaryDto> getAllPadPipelineSummaryDtosForApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    return padPipelineRepository.findAllPipelinesAsSummaryDtoByPwaApplicationDetail(
        pwaApplicationDetail);
  }


  /**
   * Get a lookup of by pipeline id to pipeline number.
   * If a pipeline has been imported in the application, map pipelineId to the application's pipeline number,
   * else use the consented model pipeline number.
   */
  public Map<PipelineId, String> getApplicationOrConsentedPipelineNumberLookup(
      PwaApplicationDetail pwaApplicationDetail) {
    Map<PipelineId, PipelineDetail> pipelineDetailsLookup = pipelineService.getActivePipelineDetailsForApplicationMasterPwa(
        pwaApplicationDetail.getPwaApplication()
    ).stream()
        .collect(Collectors.toMap(PipelineId::from, p -> p));

    Map<PipelineId, PadPipeline> padPipelinesLookup = padPipelineRepository.getAllByPwaApplicationDetail(
        pwaApplicationDetail
    ).stream()
        .collect(Collectors.toMap(PipelineId::from, p -> p));

    var allPipelineIds = Sets.union(
        pipelineDetailsLookup.keySet(),
        padPipelinesLookup.keySet()
    );

    return allPipelineIds
        .stream()
        .map(pipelineId -> {
          // this will blow up if we fail to find a reference from the application detail or the consented model for the pipelineId
          if (padPipelinesLookup.containsKey(pipelineId)) {
            return new ImmutablePair<PipelineId, String>(
                pipelineId,
                padPipelinesLookup.get(pipelineId).getPipelineRef());
          }

          return new ImmutablePair<PipelineId, String>(
              pipelineId,
              pipelineDetailsLookup.get(pipelineId).getPipelineNumber()
          );

        })
        .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
  }

  @Transactional
  public PadPipeline copyDataToNewPadPipeline(PwaApplicationDetail detail, PadPipeline padPipelineToCopyFrom) {
    var newPadPipeline = new PadPipeline(detail);
    newPadPipeline.setBundleName(padPipelineToCopyFrom.getBundleName());
    newPadPipeline.setPipelineRef(padPipelineToCopyFrom.getPipelineRef());
    newPadPipeline.setPipeline(padPipelineToCopyFrom.getPipeline());
    newPadPipeline.setComponentPartsDescription(padPipelineToCopyFrom.getComponentPartsDescription());
    newPadPipeline.setFromCoordinates(padPipelineToCopyFrom.getFromCoordinates());
    newPadPipeline.setFromLocation(padPipelineToCopyFrom.getFromLocation());
    newPadPipeline.setLength(padPipelineToCopyFrom.getLength());
    newPadPipeline.setOtherPipelineMaterialUsed(padPipelineToCopyFrom.getOtherPipelineMaterialUsed());
    newPadPipeline.setPipelineDesignLife(padPipelineToCopyFrom.getPipelineDesignLife());
    newPadPipeline.setPipelineFlexibility(padPipelineToCopyFrom.getPipelineFlexibility());
    newPadPipeline.setPipelineInBundle(padPipelineToCopyFrom.getPipelineInBundle());
    newPadPipeline.setPipelineMaterial(padPipelineToCopyFrom.getPipelineMaterial());
    newPadPipeline.setPipelineType(padPipelineToCopyFrom.getPipelineType());
    newPadPipeline.setProductsToBeConveyed(padPipelineToCopyFrom.getProductsToBeConveyed());
    newPadPipeline.setToCoordinates(padPipelineToCopyFrom.getToCoordinates());
    newPadPipeline.setToLocation(padPipelineToCopyFrom.getToLocation());
    newPadPipeline.setTrenchedBuriedBackfilled(padPipelineToCopyFrom.getTrenchedBuriedBackfilled());
    newPadPipeline.setTrenchingMethodsDescription(padPipelineToCopyFrom.getTrenchingMethodsDescription());
    padPipelineRepository.save(newPadPipeline);
    return newPadPipeline;
  }

  @Override
  public void cleanupData(PwaApplicationDetail detail) {

    var updatedPipelinesList = getPipelines(detail).stream()
        .peek(padPipeline -> {

          if (!padPipeline.getTrenchedBuriedBackfilled()) {
            padPipeline.setTrenchingMethodsDescription(null);
          }

          if (!padPipeline.getPipelineMaterial().equals(PipelineMaterial.OTHER)) {
            padPipeline.setOtherPipelineMaterialUsed(null);
          }

        })
        .collect(Collectors.toList());

    padPipelineRepository.saveAll(updatedPipelinesList);

  }
}
