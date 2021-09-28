package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentDataRepository;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;

@Service
public class PadPipelineIdentDataService {

  private final PadPipelineIdentDataRepository repository;

  @Autowired
  public PadPipelineIdentDataService(PadPipelineIdentDataRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public void addIdentData(PadPipelineIdent ident,
                           boolean definingStructure,
                           PipelineIdentDataForm dataForm) {
    var identData = new PadPipelineIdentData(ident);
    saveEntityUsingForm(identData, definingStructure, dataForm);
  }

  @Transactional
  public void updateIdentData(PadPipelineIdent ident,
                              boolean definingStructure,
                              PipelineIdentDataForm dataForm) {
    var identData = getIdentData(ident);
    saveEntityUsingForm(identData, definingStructure, dataForm);
  }

  public Map<PadPipelineIdent, PadPipelineIdentData> getDataFromIdentList(List<PadPipelineIdent> identList) {
    return repository.getAllByPadPipelineIdentIn(identList)
        .stream()
        .collect(StreamUtils.toLinkedHashMap(PadPipelineIdentData::getPadPipelineIdent, data -> data));
  }

  public List<PadPipelineIdentData> getAllPadPipelineIdentDataForIdents(List<PadPipelineIdent> identList) {
    return repository.getAllByPadPipelineIdentIn(identList);
  }

  public Optional<PadPipelineIdentData> getOptionalOfIdentData(PadPipelineIdent ident) {
    return repository.getByPadPipelineIdent(ident);
  }

  public PadPipelineIdentData getIdentData(PadPipelineIdent ident) {
    return getOptionalOfIdentData(ident).orElseThrow(() ->
        new PwaEntityNotFoundException("Couldn't find data for ident with id: " + ident.getId()));
  }

  void saveEntityUsingForm(PadPipelineIdentData identData,
                           boolean definingStructure,
                           PipelineIdentDataForm dataForm) {

    var coreType = identData.getPadPipelineIdent().getPadPipeline().getCoreType();
    identData.setComponentPartsDesc(dataForm.getComponentPartsDescription());

    if (definingStructure) {
      setIdentDataMultiCoreFieldsAsNull(identData);
      setIdentDataSingleCoreFieldsAsNull(identData);
      if (coreType.equals(PipelineCoreType.SINGLE_CORE)) {
        identData.setProductsToBeConveyed(dataForm.getProductsToBeConveyed());
      } else {
        identData.setProductsToBeConveyedMultiCore(dataForm.getProductsToBeConveyedMultiCore());
      }

    } else if (coreType.equals(PipelineCoreType.SINGLE_CORE)) {
      identData.setExternalDiameter(dataForm.getExternalDiameter().createBigDecimalOrNull());
      identData.setInternalDiameter(dataForm.getInternalDiameter().createBigDecimalOrNull());
      identData.setWallThickness(dataForm.getWallThickness().createBigDecimalOrNull());
      identData.setMaop(dataForm.getMaop().createBigDecimalOrNull());
      identData.setInsulationCoatingType(dataForm.getInsulationCoatingType());
      identData.setProductsToBeConveyed(dataForm.getProductsToBeConveyed());
      setIdentDataMultiCoreFieldsAsNull(identData);

    } else {
      identData.setExternalDiameterMultiCore(dataForm.getExternalDiameterMultiCore());
      identData.setInternalDiameterMultiCore(dataForm.getInternalDiameterMultiCore());
      identData.setWallThicknessMultiCore(dataForm.getWallThicknessMultiCore());
      identData.setMaopMultiCore(dataForm.getMaopMultiCore());
      identData.setInsulationCoatingTypeMultiCore(dataForm.getInsulationCoatingTypeMultiCore());
      identData.setProductsToBeConveyedMultiCore(dataForm.getProductsToBeConveyedMultiCore());
      setIdentDataSingleCoreFieldsAsNull(identData);
    }
    repository.save(identData);
  }

  private void setIdentDataMultiCoreFieldsAsNull(PadPipelineIdentData identData) {
    identData.setExternalDiameterMultiCore(null);
    identData.setInternalDiameterMultiCore(null);
    identData.setWallThicknessMultiCore(null);
    identData.setMaopMultiCore(null);
    identData.setInsulationCoatingTypeMultiCore(null);
    identData.setProductsToBeConveyedMultiCore(null);
  }

  private void setIdentDataSingleCoreFieldsAsNull(PadPipelineIdentData identData) {
    identData.setExternalDiameter(null);
    identData.setInternalDiameter(null);
    identData.setWallThickness(null);
    identData.setMaop(null);
    identData.setInsulationCoatingType(null);
    identData.setProductsToBeConveyed(null);
  }

  @Transactional
  public void removeIdentData(PadPipelineIdent ident) {
    var data = getIdentData(ident);
    repository.delete(data);
  }

  @Transactional
  public void removeIdentDataForPipeline(PadPipeline padPipeline) {
    var data = repository.getAllByPadPipelineIdent_PadPipeline(padPipeline);
    repository.deleteAll(data);
  }

  public PipelineIdentDataForm getDataFormOfIdent(PadPipelineIdent ident) {
    var form = new PipelineIdentDataForm();
    var identData = getIdentData(ident);

    form.setComponentPartsDescription(identData.getComponentPartsDesc());
    if (identData.getPadPipelineIdent().getPadPipeline().getPipelineType().getCoreType().equals(
        PipelineCoreType.SINGLE_CORE)) {
      form.setExternalDiameter(new DecimalInput(identData.getExternalDiameter()));
      form.setInternalDiameter(new DecimalInput(identData.getInternalDiameter()));
      form.setWallThickness(new DecimalInput(identData.getWallThickness()));
      form.setMaop(new DecimalInput(identData.getMaop()));
      form.setInsulationCoatingType(identData.getInsulationCoatingType());
      form.setProductsToBeConveyed(identData.getProductsToBeConveyed());

    } else {
      form.setExternalDiameterMultiCore(identData.getExternalDiameterMultiCore());
      form.setInternalDiameterMultiCore(identData.getInternalDiameterMultiCore());
      form.setWallThicknessMultiCore(identData.getWallThicknessMultiCore());
      form.setMaopMultiCore(identData.getMaopMultiCore());
      form.setInsulationCoatingTypeMultiCore(identData.getInsulationCoatingTypeMultiCore());
      form.setProductsToBeConveyedMultiCore(identData.getProductsToBeConveyedMultiCore());
    }

    return form;
  }

  public void saveAll(Collection<PadPipelineIdentData> identData) {
    repository.saveAll(identData);
  }

  public List<PadPipelineIdentData> getAllPipelineIdentDataForPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    return repository.getAllPadPipelineIdentDataByPwaApplicationDetail(pwaApplicationDetail);
  }

  public List<PadPipelineIdentData> getAllPipelineIdentDataForPadPipelines(Collection<PadPipeline> padPipelines) {
    return repository.getAllByPadPipelineIdent_PadPipelineIn(padPipelines);
  }

}
