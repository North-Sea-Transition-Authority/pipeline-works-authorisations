package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

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
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;

/* Service providing simplified API for Technical Information Fluid Composition app form */
@Service
public class PadFluidCompositionInfoService implements ApplicationFormSectionService {

  private final PadFluidCompositionInfoRepository padFluidCompositionInfoRepository;
  private final FluidCompositionValidator fluidCompositionValidator;
  private final EntityCopyingService entityCopyingService;

  @Autowired
  public PadFluidCompositionInfoService(
      PadFluidCompositionInfoRepository padFluidCompositionInfoRepository,
      FluidCompositionValidator fluidCompositionValidator,
      EntityCopyingService entityCopyingService) {
    this.padFluidCompositionInfoRepository = padFluidCompositionInfoRepository;
    this.fluidCompositionValidator = fluidCompositionValidator;
    this.entityCopyingService = entityCopyingService;
  }


  // Entity/Form  Retrieval/Mapping
  public List<PadFluidCompositionInfo> getPadFluidCompositionInfoEntities(PwaApplicationDetail pwaApplicationDetail) {
    List<PadFluidCompositionInfo> padFluidCompositionInfoList =
        padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);

    if (padFluidCompositionInfoList.isEmpty()) {
      for (Chemical chemical : Chemical.asList()) {
        var padFluidCompositionInfo = new PadFluidCompositionInfo(pwaApplicationDetail, chemical);
        padFluidCompositionInfoList.add(padFluidCompositionInfo);
      }
    }
    return padFluidCompositionInfoList;
  }

  public void mapEntitiesToForm(FluidCompositionForm form, List<PadFluidCompositionInfo> entities) {
    for (PadFluidCompositionInfo entity : entities) {
      var fluidCompositionDataForm = new FluidCompositionDataForm();
      fluidCompositionDataForm.setFluidCompositionOption(entity.getFluidCompositionOption());
      if (entity.getFluidCompositionOption() != null && entity.getFluidCompositionOption().equals(
          FluidCompositionOption.HIGHER_AMOUNT)) {
        fluidCompositionDataForm.setMoleValue(new DecimalInput(entity.getMoleValue()));
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
        entity.setMoleValue(fluidCompositionDataForm.getMoleValue().createBigDecimalOrNull());
      }
    }
    padFluidCompositionInfoRepository.saveAll(entities);
  }

  public FluidCompositionView getFluidCompositionView(PwaApplicationDetail pwaApplicationDetail) {

    Map<Chemical, FluidCompositionDataForm> chemicalDataMap = new LinkedHashMap<>();
    var fluidCompositionView = new FluidCompositionView(chemicalDataMap);

    for (PadFluidCompositionInfo entity : getPadFluidCompositionInfoEntities(pwaApplicationDetail)) {
      var fluidCompositionDataForm = new FluidCompositionDataForm();
      fluidCompositionDataForm.setFluidCompositionOption(entity.getFluidCompositionOption());
      if (entity.getFluidCompositionOption() != null && entity.getFluidCompositionOption().equals(
          FluidCompositionOption.HIGHER_AMOUNT)) {
        fluidCompositionDataForm.setMoleValue(new DecimalInput(entity.getMoleValue()));
      }
      chemicalDataMap.put(entity.getChemicalName(), fluidCompositionDataForm);
    }

    return fluidCompositionView;
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
  public void cleanupData(PwaApplicationDetail detail) {

    // null out mole value for all non-higher amount entries
    var fluidCompositionEntitiesToClear = getPadFluidCompositionInfoEntities(detail).stream()
        .filter(fluidCompositionInfo ->
            !Objects.equals(fluidCompositionInfo.getFluidCompositionOption(), FluidCompositionOption.HIGHER_AMOUNT))
        .collect(Collectors.toList());

    fluidCompositionEntitiesToClear.forEach(fluidCompositionInfo -> fluidCompositionInfo.setMoleValue(null));

    padFluidCompositionInfoRepository.saveAll(fluidCompositionEntitiesToClear);

  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    entityCopyingService.duplicateEntitiesAndSetParent(
        () -> padFluidCompositionInfoRepository.getAllByPwaApplicationDetail(fromDetail),
        toDetail,
        PadFluidCompositionInfo.class
    );
  }
}

