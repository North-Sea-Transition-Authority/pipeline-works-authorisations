package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.PipelineIdentsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.PipelinesController;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineTaskListItem;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskInfo;
import uk.co.ogauthority.pwa.util.CoordinateUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class PadPipelineService implements ApplicationFormSectionService {

  private final PadPipelineRepository padPipelineRepository;

  @Autowired
  public PadPipelineService(PadPipelineRepository padPipelineRepository) {
    this.padPipelineRepository = padPipelineRepository;
  }

  public List<PadPipeline> getPipelines(PwaApplicationDetail detail) {
    return padPipelineRepository.getAllByPwaApplicationDetail(detail);
  }

  public PipelineOverview getPipelineOverview(PadPipeline padPipeline) {

    return padPipelineRepository.findPipelineAsSummaryDtoByPadPipeline(padPipeline)
        .map(PadPipelineOverview::from)
        .orElseThrow(() -> new PwaEntityNotFoundException("Pipeline Summary not found. Pad pipeline id: " + padPipeline.getId()));
  }

  public List<PipelineOverview> getPipelineOverviews(PwaApplicationDetail detail) {

    return padPipelineRepository.findAllPipelinesAsSummaryDtoByPwaApplicationDetail(detail).stream()
        .map(PadPipelineOverview::from)
        .sorted(Comparator.comparing(PipelineOverview::getPipelineNumber))
        .collect(Collectors.toList());

  }

  public List<PadPipelineTaskListItem> getPipelineTaskListItems(PwaApplicationDetail detail) {

    return getPipelineOverviews(detail)
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

  private String getEditPipelineHeaderUrl(int applicationId, PwaApplicationType applicationType, int pipelineId) {
    return ReverseRouter.route(on(PipelinesController.class).renderEditPipeline(
        applicationId,
        applicationType,
        pipelineId,
        null,
        null
    ));
  }

  private String getPipelineIdentOverviewUrl(int applicationId, PwaApplicationType applicationType, int pipelineId) {
    return ReverseRouter.route(on(PipelineIdentsController.class).renderIdentOverview(
        applicationId,
        applicationType,
        pipelineId,
        null
    ));
  }

  @Transactional
  public void addPipeline(PwaApplicationDetail pwaApplicationDetail, PipelineHeaderForm form) {

    var newPipeline = new PadPipeline(pwaApplicationDetail);

    // N.B. this temporary reference format is intended. Applicants need a reference for a pipeline that they can use in their
    // schematic drawings, mention in text etc while filling in the application. PL numbers are only assigned after submission.
    Long numberOfPipesForDetail = padPipelineRepository.countAllByPwaApplicationDetail(pwaApplicationDetail);
    newPipeline.setPipelineRef("TEMPORARY_" + (numberOfPipesForDetail.intValue() + 1));

    saveEntityUsingForm(newPipeline, form);

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

  public List<PadPipeline> getByIdList(PwaApplicationDetail detail, List<Integer> pipelineIds) {
    return padPipelineRepository.getAllByPwaApplicationDetailAndIdIn(detail, ListUtils.emptyIfNull(pipelineIds));
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
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

  public long totalPipelineContainedInApplication(PwaApplicationDetail pwaApplicationDetail) {
    return padPipelineRepository.countAllByPwaApplicationDetail(pwaApplicationDetail);
  }

}
