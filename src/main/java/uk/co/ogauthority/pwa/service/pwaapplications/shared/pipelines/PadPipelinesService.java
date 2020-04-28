package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.PipelinesController;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.CoordinateUtils;

@Service
public class PadPipelinesService implements ApplicationFormSectionService {

  private final PadPipelineRepository padPipelineRepository;

  @Autowired
  public PadPipelinesService(PadPipelineRepository padPipelineRepository) {
    this.padPipelineRepository = padPipelineRepository;
  }

  public List<PipelineOverview> getPipelineOverviews(PwaApplicationDetail detail) {

    return padPipelineRepository.getAllByPwaApplicationDetail(detail).stream()
        .map(this::getPipelineOverview)
        .collect(Collectors.toList());

  }

  private PipelineOverview getPipelineOverview(PadPipeline pipeline) {
    return new PipelineOverview(
        pipeline.getFromLocation(),
        pipeline.getFromCoordinates(),
        pipeline.getToLocation(),
        pipeline.getToCoordinates(),
        pipeline.getPipelineRef(),
        pipeline.getPipelineType(),
        pipeline.getComponentPartsDescription(),
        pipeline.getLength(),
        pipeline.getProductsToBeConveyed(),
        List.of(
            new TaskListEntry("Header information", getEditPipelineHeaderUrl(
                pipeline.getPwaApplicationDetail().getMasterPwaApplicationId(),
                pipeline.getPwaApplicationDetail().getPwaApplicationType(),
                pipeline.getId()), true),
            new TaskListEntry("Idents", "#", false)
        ));
  }

  private String getEditPipelineHeaderUrl(int applicationId, PwaApplicationType applicationType, int pipelineId) {
    return ReverseRouter.route(on(PipelinesController.class).renderEditPipeline(
        applicationId,
        applicationType,
        null,
        pipelineId,
        null
    ));
  }

  @Transactional
  public void addPipeline(PwaApplicationDetail pwaApplicationDetail, PipelineHeaderForm form) {

    var newPipeline = new PadPipeline(pwaApplicationDetail);
    Long numberOfPipesForDetail = padPipelineRepository.countAllByPwaApplicationDetail(pwaApplicationDetail);
    newPipeline.setPipelineRef("TEMP-" + (numberOfPipesForDetail.intValue() + 1));

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
    form.setToLocation(pipeline.getToLocation());

    form.setFromLatDeg(pipeline.getFromCoordinates().getLatitude().getDegrees());
    form.setFromLatMin(pipeline.getFromCoordinates().getLatitude().getMinutes());
    form.setFromLatSec(pipeline.getFromCoordinates().getLatitude().getSeconds());
    form.setFromLatDirection(pipeline.getFromCoordinates().getLatitude().getDirection());
    form.setFromLongDeg(pipeline.getFromCoordinates().getLongitude().getDegrees());
    form.setFromLongMin(pipeline.getFromCoordinates().getLongitude().getMinutes());
    form.setFromLongSec(pipeline.getFromCoordinates().getLongitude().getSeconds());
    form.setFromLongDirection(pipeline.getFromCoordinates().getLongitude().getDirection());

    form.setToLatDeg(pipeline.getToCoordinates().getLatitude().getDegrees());
    form.setToLatMin(pipeline.getToCoordinates().getLatitude().getMinutes());
    form.setToLatSec(pipeline.getToCoordinates().getLatitude().getSeconds());
    form.setToLatDirection(pipeline.getToCoordinates().getLatitude().getDirection());
    form.setToLongDeg(pipeline.getToCoordinates().getLongitude().getDegrees());
    form.setToLongMin(pipeline.getToCoordinates().getLongitude().getMinutes());
    form.setToLongSec(pipeline.getToCoordinates().getLongitude().getSeconds());
    form.setToLongDirection(pipeline.getToCoordinates().getLongitude().getDirection());

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
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format("Couldn't find PadPipeline with ID: %s", padPipelineId)));
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return false;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    return null;
  }

}
