package uk.co.ogauthority.pwa.features.application.tasks.othertechprops;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;


/* Service providing simplified API for Pipelines Other Properties app form */
@Service
public class PadPipelineOtherPropertiesService implements ApplicationFormSectionService {

  private final PadPipelineOtherPropertiesRepository padPipelineOtherPropertiesRepository;
  private final PipelineOtherPropertiesValidator pipelineOtherPropertiesValidator;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final EntityCopyingService entityCopyingService;

  @Autowired
  public PadPipelineOtherPropertiesService(
      PadPipelineOtherPropertiesRepository padPipelineOtherPropertiesRepository,
      PipelineOtherPropertiesValidator pipelineOtherPropertiesValidator,
      PwaApplicationDetailService pwaApplicationDetailService,
      EntityCopyingService entityCopyingService) {
    this.padPipelineOtherPropertiesRepository = padPipelineOtherPropertiesRepository;
    this.pipelineOtherPropertiesValidator = pipelineOtherPropertiesValidator;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.entityCopyingService = entityCopyingService;
  }

  // Entity/Form Mapping/Retrieval
  public List<PadPipelineOtherProperties> getPipelineOtherPropertyEntities(PwaApplicationDetail pwaApplicationDetail) {
    List<PadPipelineOtherProperties> pipelineOtherPropertiesList =
        padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);

    if (pipelineOtherPropertiesList.isEmpty()) {
      for (OtherPipelineProperty property: OtherPipelineProperty.asList(pwaApplicationDetail.getResourceType())) {
        var padPipelineOtherProperty = new PadPipelineOtherProperties(pwaApplicationDetail, property);
        pipelineOtherPropertiesList.add(padPipelineOtherProperty);
      }
    }

    return pipelineOtherPropertiesList;
  }

  public void mapEntitiesToForm(PipelineOtherPropertiesForm form, List<PadPipelineOtherProperties> entities,
                                PwaApplicationDetail pwaApplicationDetail) {
    var phasesPresent = pwaApplicationDetail.getPipelinePhaseProperties();
    if (phasesPresent != null && !phasesPresent.isEmpty()) {
      for (var phase: phasesPresent) {
        form.getPhasesSelection().put(phase, "true");
      }
      form.setPhase(phasesPresent.iterator().next());

      if (phasesPresent.contains(PropertyPhase.OTHER)) {
        form.setOtherPhaseDescription(pwaApplicationDetail.getOtherPhaseDescription());
      }
    }

    for (PadPipelineOtherProperties entity: entities) {
      var pipelineOtherPropertiesDataForm = new PipelineOtherPropertiesDataForm();
      pipelineOtherPropertiesDataForm.setPropertyAvailabilityOption(entity.getAvailabilityOption());
      if (entity.getAvailabilityOption() != null && entity.getAvailabilityOption().equals(PropertyAvailabilityOption.AVAILABLE)) {
        var minValue = entity.getMinValue() == null ? null : String.valueOf(entity.getMinValue());
        var maxValue = entity.getMaxValue() == null ? null : String.valueOf(entity.getMaxValue());
        var minMaxInput = new MinMaxInput(minValue, maxValue);
        pipelineOtherPropertiesDataForm.setMinMaxInput(minMaxInput);
      }
      form.addPropertyData(entity.getPropertyName(), pipelineOtherPropertiesDataForm);
    }
  }

  public void saveEntitiesUsingForm(PipelineOtherPropertiesForm form, List<PadPipelineOtherProperties> entities,
                                    PwaApplicationDetail pwaApplicationDetail) {
    var propertyPhases = new HashSet<PropertyPhase>();
    propertyPhases.addAll(form.getPhasesSelection().keySet());
    propertyPhases.add(form.getPhase());

    var otherPhaseDescription = propertyPhases.contains(PropertyPhase.OTHER) ? form.getOtherPhaseDescription() : null;

    pwaApplicationDetailService.setPhasesPresent(pwaApplicationDetail, propertyPhases, otherPhaseDescription);

    for (PadPipelineOtherProperties entity: entities) {
      var pipelineOtherPropertiesDataForm = form.getPropertyDataFormMap().get(entity.getPropertyName());

      if (pipelineOtherPropertiesDataForm != null
          && pipelineOtherPropertiesDataForm.getPropertyAvailabilityOption() != null
          && pipelineOtherPropertiesDataForm.getPropertyAvailabilityOption().equals(PropertyAvailabilityOption.AVAILABLE)) {
        entity.setAvailabilityOption(pipelineOtherPropertiesDataForm.getPropertyAvailabilityOption());
        entity.setMinValue(pipelineOtherPropertiesDataForm.getMinMaxInput().createMinOrNull());
        entity.setMaxValue(pipelineOtherPropertiesDataForm.getMinMaxInput().createMaxOrNull());
      }
    }
    padPipelineOtherPropertiesRepository.saveAll(entities);
  }


  public OtherPropertiesView getOtherPropertiesView(PwaApplicationDetail pwaApplicationDetail) {

    var entities = getPipelineOtherPropertyEntities(pwaApplicationDetail);
    Map<OtherPipelineProperty, OtherPropertiesValueView> propertyValueMap = new LinkedHashMap<>();

    for (PadPipelineOtherProperties entity: entities) {
      String minValue = null;
      String maxValue = null;
      if (PropertyAvailabilityOption.AVAILABLE.equals(entity.getAvailabilityOption())) {
        minValue = entity.getMinValue() == null ? null : String.valueOf(entity.getMinValue());
        maxValue = entity.getMaxValue() == null ? null : String.valueOf(entity.getMaxValue());
      }
      var otherPropertiesValueView = new OtherPropertiesValueView(entity.getAvailabilityOption(), minValue, maxValue);
      propertyValueMap.put(entity.getPropertyName(), otherPropertiesValueView);
    }

    return new OtherPropertiesView(
        propertyValueMap, pwaApplicationDetail.getPipelinePhaseProperties(), pwaApplicationDetail.getOtherPhaseDescription());
  }




  // Validation / Checking
  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var otherPropertyEntities = getPipelineOtherPropertyEntities(detail);

    var otherPropertiesForm = new PipelineOtherPropertiesForm();
    mapEntitiesToForm(otherPropertiesForm, otherPropertyEntities, detail);
    BindingResult bindingResult = new BeanPropertyBindingResult(otherPropertiesForm, "form");
    bindingResult = validate(otherPropertiesForm, bindingResult, ValidationType.FULL, detail);

    return !bindingResult.hasErrors();
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult,
                                ValidationType validationType, PwaApplicationDetail pwaApplicationDetail) {
    pipelineOtherPropertiesValidator.validate(form, bindingResult, validationType, pwaApplicationDetail.getResourceType());
    if (bindingResult.hasErrors()) {
      var otherPropertiesForm = (PipelineOtherPropertiesForm) form;
      for (var phaseEntry: otherPropertiesForm.getPhasesSelection().entrySet()) {
        phaseEntry.setValue("true");
      }
    }
    return bindingResult;
  }

  @Override
  public void cleanupData(PwaApplicationDetail detail) {

    // null out min/max values of any properties that aren't present/available
    var otherPropertiesToClear = getPipelineOtherPropertyEntities(detail).stream()
        .filter(otherProperty ->
            !Objects.equals(otherProperty.getAvailabilityOption(), PropertyAvailabilityOption.AVAILABLE))
        .collect(Collectors.toList());

    otherPropertiesToClear.forEach(otherProperty -> {
      otherProperty.setMinValue(null);
      otherProperty.setMaxValue(null);
    });

    padPipelineOtherPropertiesRepository.saveAll(otherPropertiesToClear);

  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    // "phasesPresent" stored on app detail, so dont need to copy specifically here.
    entityCopyingService.duplicateEntitiesAndSetParent(
        () -> padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(fromDetail),
        toDetail,
        PadPipelineOtherProperties.class
    );
  }

}

