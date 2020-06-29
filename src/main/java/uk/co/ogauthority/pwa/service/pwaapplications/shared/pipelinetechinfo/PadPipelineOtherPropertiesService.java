package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.OtherPipelineProperty;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyAvailabilityOption;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyPhase;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineOtherProperties;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesDataForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo.PadPipelineOtherPropertiesRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.PipelineOtherPropertiesValidator;


/* Service providing simplified API for Pipelines Other Properties app form */
@Service
public class PadPipelineOtherPropertiesService implements ApplicationFormSectionService {

  private final PadPipelineOtherPropertiesRepository padPipelineOtherPropertiesRepository;
  private final PipelineOtherPropertiesValidator pipelineOtherPropertiesValidator;
  private final PwaApplicationDetailService pwaApplicationDetailService;

  @Autowired
  public PadPipelineOtherPropertiesService(
      PadPipelineOtherPropertiesRepository padPipelineOtherPropertiesRepository,
      PipelineOtherPropertiesValidator pipelineOtherPropertiesValidator,
      PwaApplicationDetailService pwaApplicationDetailService) {
    this.padPipelineOtherPropertiesRepository = padPipelineOtherPropertiesRepository;
    this.pipelineOtherPropertiesValidator = pipelineOtherPropertiesValidator;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
  }

  // Entity/Form Mapping/Retrieval
  public List<PadPipelineOtherProperties> getPipelineOtherPropertyEntities(PwaApplicationDetail pwaApplicationDetail) {
    List<PadPipelineOtherProperties> pipelineOtherPropertiesList =
        padPipelineOtherPropertiesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);

    if (pipelineOtherPropertiesList.isEmpty()) {
      for (OtherPipelineProperty property: OtherPipelineProperty.asList()) {
        var padPipelineOtherProperty = new PadPipelineOtherProperties(pwaApplicationDetail, property);
        pipelineOtherPropertiesList.add(padPipelineOtherProperty);
      }
    }

    return pipelineOtherPropertiesList;
  }

  public void mapEntitiesToForm(PipelineOtherPropertiesForm form, List<PadPipelineOtherProperties> entities,
                                PwaApplicationDetail pwaApplicationDetail) {
    var phasesStr = pwaApplicationDetail.getPipelinePhaseProperties();
    var phasesList = phasesStr == null ? new String[0] : phasesStr.split(",");
    for (var phase : phasesList) {
      setPhaseTrueIfExists(form, phase);
      if (PropertyPhase.valueOf(phase).equals(PropertyPhase.OTHER)) {
        form.setOtherPhaseDescription(pwaApplicationDetail.getOtherPhaseDescription());
      }
    }

    for (PadPipelineOtherProperties entity: entities) {
      var pipelineOtherPropertiesDataForm = new PipelineOtherPropertiesDataForm();
      pipelineOtherPropertiesDataForm.setPropertyAvailabilityOption(entity.getAvailabilityOption());
      if (entity.getAvailabilityOption() != null && entity.getAvailabilityOption().equals(PropertyAvailabilityOption.AVAILABLE)) {
        var minMaxInput = new MinMaxInput(entity.getMinValue(), entity.getMaxValue());
        pipelineOtherPropertiesDataForm.setMinMaxInput(minMaxInput);
      }
      form.addPropertyData(entity.getPropertyName(), pipelineOtherPropertiesDataForm);
    }
  }

  private void setPhaseTrueIfExists(PipelineOtherPropertiesForm form, String phaseValue) {
    if (PropertyPhase.valueOf(phaseValue).equals(PropertyPhase.OIL)) {
      form.setOilPresent(true);
    } else if (PropertyPhase.valueOf(phaseValue).equals(PropertyPhase.CONDENSATE)) {
      form.setCondensatePresent(true);
    } else if (PropertyPhase.valueOf(phaseValue).equals(PropertyPhase.GAS)) {
      form.setGasPresent(true);
    } else if (PropertyPhase.valueOf(phaseValue).equals(PropertyPhase.WATER)) {
      form.setWaterPresent(true);
    } else if (PropertyPhase.valueOf(phaseValue).equals(PropertyPhase.OTHER)) {
      form.setOtherPresent(true);
    }
  }

  public void saveEntitiesUsingForm(PipelineOtherPropertiesForm form, List<PadPipelineOtherProperties> entities,
                                    PwaApplicationDetail pwaApplicationDetail) {
    var otherPhaseDescription = form.getOtherPresent() ? form.getOtherPhaseDescription() : "";
    pwaApplicationDetailService.setPhasesPresent(pwaApplicationDetail, createPhasesCsv(form), otherPhaseDescription);

    for (PadPipelineOtherProperties entity: entities) {
      var pipelineOtherPropertiesDataForm = form.getPropertyDataFormMap().get(entity.getPropertyName());
      entity.setAvailabilityOption(pipelineOtherPropertiesDataForm.getPropertyAvailabilityOption());
      if (pipelineOtherPropertiesDataForm.getPropertyAvailabilityOption() != null
          && pipelineOtherPropertiesDataForm.getPropertyAvailabilityOption().equals(PropertyAvailabilityOption.AVAILABLE)) {
        entity.setMinValue(pipelineOtherPropertiesDataForm.getMinMaxInput().getMinValue());
        entity.setMaxValue(pipelineOtherPropertiesDataForm.getMinMaxInput().getMaxValue());
      }
    }
    padPipelineOtherPropertiesRepository.saveAll(entities);
  }

  private String usePhaseIfPresent(PropertyPhase propertyPhase, boolean phasePresent) {
    if (phasePresent) {
      return propertyPhase.name() + ",";
    }
    return  "";
  }

  private String createPhasesCsv(PipelineOtherPropertiesForm form) {
    String phasesCsv = usePhaseIfPresent(PropertyPhase.OIL, form.getOilPresent()) +
        usePhaseIfPresent(PropertyPhase.CONDENSATE, form.getCondensatePresent()) +
        usePhaseIfPresent(PropertyPhase.GAS, form.getGasPresent()) +
        usePhaseIfPresent(PropertyPhase.WATER, form.getWaterPresent()) +
        usePhaseIfPresent(PropertyPhase.OTHER, form.getOtherPresent());
    return StringUtils.removeEnd(phasesCsv, ",");
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
    if (validationType.equals(ValidationType.FULL)) {
      pipelineOtherPropertiesValidator.validate(form, bindingResult);
    }
    return bindingResult;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return true;
  }





}

