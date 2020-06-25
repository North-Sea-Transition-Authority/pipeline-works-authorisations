package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.Chemical;
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.FluidCompositionOption;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadFluidCompositionInfo;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.FluidCompositionDataForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.FluidCompositionForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo.PadFluidCompositionInfoRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.FluidCompositionValidator;

/* Service providing simplified API for Technical Information Fluid Composition app form */
@Service
public class PadFluidCompositionInfoService implements ApplicationFormSectionService {

  private final PadFluidCompositionInfoRepository padFluidCompositionInfoRepository;
  private final FluidCompositionValidator fluidCompositionValidator;

  @Autowired
  public PadFluidCompositionInfoService(
      PadFluidCompositionInfoRepository padFluidCompositionInfoRepository,
      FluidCompositionValidator fluidCompositionValidator) {
    this.padFluidCompositionInfoRepository = padFluidCompositionInfoRepository;
    this.fluidCompositionValidator = fluidCompositionValidator;
  }


  // Entity/Form  Retrieval/Mapping
  public List<PadFluidCompositionInfo> getPadFluidCompositionInfoEntities(PwaApplicationDetail pwaApplicationDetail) {
    List<PadFluidCompositionInfo> padFluidCompositionInfoList =
        padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);

    if (padFluidCompositionInfoList.isEmpty()) {
      for (Chemical chemical: Chemical.asList()) {
        var padFluidCompositionInfo = new PadFluidCompositionInfo(pwaApplicationDetail, chemical);
        padFluidCompositionInfoList.add(padFluidCompositionInfo);
      }
    }
    return padFluidCompositionInfoList;
  }

  public void mapEntitiesToForm(FluidCompositionForm form, List<PadFluidCompositionInfo> entities) {
    for (PadFluidCompositionInfo entity: entities) {
      var fluidCompositionDataForm = new FluidCompositionDataForm();
      fluidCompositionDataForm.setFluidCompositionOption(entity.getFluidCompositionOption());
      if (entity.getFluidCompositionOption() != null && entity.getFluidCompositionOption().equals(FluidCompositionOption.HIGHER_AMOUNT)) {
        fluidCompositionDataForm.setMoleValue(entity.getMoleValue());
      }
      form.addChemicalData(entity.getChemicalName(), fluidCompositionDataForm);
    }
  }

  public void saveEntitiesUsingForm(FluidCompositionForm form, List<PadFluidCompositionInfo> entities) {
    for (PadFluidCompositionInfo entity : entities) {
      var fluidCompositionDataForm = form.getChemicalDataFormMap().get(entity.getChemicalName());
      entity.setFluidCompositionOption(fluidCompositionDataForm.getFluidCompositionOption());
      if (fluidCompositionDataForm.getFluidCompositionOption() != null
          && fluidCompositionDataForm.getFluidCompositionOption().equals(FluidCompositionOption.HIGHER_AMOUNT)) {
        entity.setMoleValue(fluidCompositionDataForm.getMoleValue());
      }
    }
    padFluidCompositionInfoRepository.saveAll(entities);
  }




  // Validation / Checking
  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var fluidCompositions = getPadFluidCompositionInfoEntities(detail);

    var fluidCompositionForm = new FluidCompositionForm();
    mapEntitiesToForm(fluidCompositionForm, fluidCompositions);
    BindingResult bindingResult = new BeanPropertyBindingResult(fluidCompositionForm, "form");
    bindingResult = validate(fluidCompositionForm, bindingResult, ValidationType.FULL, detail);

    return !bindingResult.hasErrors();
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult,
                                ValidationType validationType, PwaApplicationDetail pwaApplicationDetail) {
    if (validationType.equals(ValidationType.FULL)) {
      fluidCompositionValidator.validate(form, bindingResult, pwaApplicationDetail);
    }
    return bindingResult;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return true;
  }





}

