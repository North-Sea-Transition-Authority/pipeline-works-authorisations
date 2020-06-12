package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
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
    identData.setExternalDiameter(dataForm.getExternalDiameter());
    identData.setInternalDiameter(dataForm.getInternalDiameter());
    identData.setWallThickness(dataForm.getWallThickness());
    identData.setMaop(dataForm.getMaop());
    identData.setInsulationCoatingType(dataForm.getInsulationCoatingType());
    identData.setProductsToBeConveyed(dataForm.getProductsToBeConveyed());

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
    form.setExternalDiameter(identData.getExternalDiameter());
    form.setInternalDiameter(identData.getInternalDiameter());
    form.setWallThickness(identData.getWallThickness());
    form.setMaop(identData.getMaop());
    form.setInsulationCoatingType(identData.getInsulationCoatingType());
    form.setProductsToBeConveyed(identData.getProductsToBeConveyed());
    return form;
  }
}
