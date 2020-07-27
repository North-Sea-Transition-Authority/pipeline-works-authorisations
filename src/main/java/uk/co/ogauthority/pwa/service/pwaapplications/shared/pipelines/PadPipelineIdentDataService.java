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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentDataRepository;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class PadPipelineIdentDataService {

  private final PadPipelineIdentDataRepository repository;

  @Autowired
  public PadPipelineIdentDataService(PadPipelineIdentDataRepository repository) {
    this.repository = repository;
  }

  @Transactional
  void addIdentData(PadPipelineIdent ident, PipelineIdentDataForm dataForm) {
    var identData = new PadPipelineIdentData(ident);
    saveEntityUsingForm(identData, dataForm);
  }

  @Transactional
  public void updateIdentData(PadPipelineIdent ident, PipelineIdentDataForm dataForm) {
    var identData = getIdentData(ident);
    saveEntityUsingForm(identData, dataForm);
  }

  public Map<PadPipelineIdent, PadPipelineIdentData> getDataFromIdentList(List<PadPipelineIdent> identList) {
    return repository.getAllByPadPipelineIdentIn(identList)
        .stream()
        .collect(StreamUtils.toLinkedHashMap(PadPipelineIdentData::getPadPipelineIdent, data -> data));
  }

  public Optional<PadPipelineIdentData> getOptionalOfIdentData(PadPipelineIdent ident) {
    return repository.getByPadPipelineIdent(ident);
  }

  public PadPipelineIdentData getIdentData(PadPipelineIdent ident) {
    return getOptionalOfIdentData(ident).orElseThrow(() ->
        new PwaEntityNotFoundException("Couldn't find data for ident with id: " + ident.getId()));
  }

  void saveEntityUsingForm(PadPipelineIdentData identData, PipelineIdentDataForm dataForm) {

    identData.setComponentPartsDescription(dataForm.getComponentPartsDescription());
    if (identData.getPadPipelineIdent().getPadPipeline().getPipelineType().getCoreType().equals(PipelineCoreType.SINGLE_CORE)) {
      identData.setExternalDiameter(dataForm.getExternalDiameter());
      identData.setInternalDiameter(dataForm.getInternalDiameter());
      identData.setWallThickness(dataForm.getWallThickness());
      identData.setMaop(dataForm.getMaop());
      identData.setInsulationCoatingType(dataForm.getInsulationCoatingType());
      identData.setProductsToBeConveyed(dataForm.getProductsToBeConveyed());
      identData.setExternalDiameterMultiCore(null);
      identData.setInternalDiameterMultiCore(null);
      identData.setWallThicknessMultiCore(null);
      identData.setMaopMultiCore(null);
      identData.setInsulationCoatingTypeMultiCore(null);
      identData.setProductsToBeConveyedMultiCore(null);

    } else {
      identData.setExternalDiameterMultiCore(dataForm.getExternalDiameterMultiCore());
      identData.setInternalDiameterMultiCore(dataForm.getInternalDiameterMultiCore());
      identData.setWallThicknessMultiCore(dataForm.getWallThicknessMultiCore());
      identData.setMaopMultiCore(dataForm.getMaopMultiCore());
      identData.setInsulationCoatingTypeMultiCore(dataForm.getInsulationCoatingTypeMultiCore());
      identData.setProductsToBeConveyedMultiCore(dataForm.getProductsToBeConveyedMultiCore());
      identData.setExternalDiameter(null);
      identData.setInternalDiameter(null);
      identData.setWallThickness(null);
      identData.setMaop(null);
      identData.setInsulationCoatingType(null);
      identData.setProductsToBeConveyed(null);
    }
    repository.save(identData);
  }

  @Transactional
  public void removeIdentData(PadPipelineIdent ident) {
    var data = getIdentData(ident);
    repository.delete(data);
  }

  public PipelineIdentDataForm getDataFormOfIdent(PadPipelineIdent ident) {
    var form = new PipelineIdentDataForm();
    var identData = getIdentData(ident);

    form.setComponentPartsDescription(identData.getComponentPartsDescription());
    if (identData.getPadPipelineIdent().getPadPipeline().getPipelineType().getCoreType().equals(PipelineCoreType.SINGLE_CORE)) {
      form.setExternalDiameter(identData.getExternalDiameter());
      form.setInternalDiameter(identData.getInternalDiameter());
      form.setWallThickness(identData.getWallThickness());
      form.setMaop(identData.getMaop());
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
}
