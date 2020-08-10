package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentDataRepository;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineIdentDataServiceTest {

  @Mock
  private PadPipelineIdentDataRepository repository;

  private PadPipelineIdentDataService identDataService;

  @Captor
  private ArgumentCaptor<PadPipelineIdentData> identDataCaptor;

  @Before
  public void setUp() {
    identDataService = new PadPipelineIdentDataService(repository);
  }

  @Test
  public void addIdentData() {

    var ident = new PadPipelineIdent();

    var form = new PipelineIdentDataForm();
    form.setInsulationCoatingType("ins");
    form.setComponentPartsDescription("comp");
    form.setProductsToBeConveyed("prod");
    form.setExternalDiameter(BigDecimal.valueOf(10));
    form.setInternalDiameter(BigDecimal.valueOf(11));
    form.setWallThickness(BigDecimal.valueOf(22.22));
    form.setMaop(BigDecimal.valueOf(500));

    form.setExternalDiameterMultiCore("some text");
    form.setInternalDiameterMultiCore("some text");
    form.setWallThicknessMultiCore("some text");
    form.setMaopMultiCore("some text");
    form.setInsulationCoatingTypeMultiCore("some text");
    form.setProductsToBeConveyedMultiCore("some text");

    var pipeline = new PadPipeline();
    pipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    ident.setPadPipeline(pipeline);

    identDataService.addIdentData(ident, form);

    verify(repository, times(1)).save(identDataCaptor.capture());

    var identData = identDataCaptor.getValue();

    assertThat(identData.getPadPipelineIdent()).isEqualTo(ident);
    assertThat(identData.getComponentPartsDescription()).isEqualTo(form.getComponentPartsDescription());
    assertThat(identData.getInsulationCoatingType()).isEqualTo(form.getInsulationCoatingType());
    assertThat(identData.getProductsToBeConveyed()).isEqualTo(form.getProductsToBeConveyed());
    assertThat(identData.getExternalDiameter()).isEqualTo(form.getExternalDiameter());
    assertThat(identData.getInternalDiameter()).isEqualTo(form.getInternalDiameter());
    assertThat(identData.getWallThickness()).isEqualTo(form.getWallThickness());
    assertThat(identData.getMaop()).isEqualTo(form.getMaop());

  }

  @Test
  public void getDataFromIdentList_valid() {
    var ident = new PadPipelineIdent();
    ident.setIdentNo(1);

    var identData = new PadPipelineIdentData(ident);
    identData.setComponentPartsDescription("parts");

    when(repository.getAllByPadPipelineIdentIn(eq(List.of(ident)))).thenReturn(List.of(identData));
    var result = identDataService.getDataFromIdentList(List.of(ident));
    assertThat(result).containsExactly(
        entry(ident, identData)
    );
  }

  @Test
  public void getDataFromIdentList_emptyList() {
    var ident = new PadPipelineIdent();
    ident.setIdentNo(1);

    var identData = new PadPipelineIdentData(ident);
    identData.setComponentPartsDescription("parts");

    when(repository.getAllByPadPipelineIdentIn(eq(List.of()))).thenReturn(List.of());
    var result = identDataService.getDataFromIdentList(List.of());
    assertThat(result).isEmpty();
  }

  @Test
  public void getIdentData_valid() {
    var ident = new PadPipelineIdent();
    var data = new PadPipelineIdentData();
    when(repository.getByPadPipelineIdent(ident)).thenReturn(Optional.of(data));
    var result = identDataService.getIdentData(ident);
    assertThat(result).isEqualTo(data);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getIdentData_invalid() {
    var ident = new PadPipelineIdent();
    when(repository.getByPadPipelineIdent(ident)).thenReturn(Optional.empty());
    identDataService.getIdentData(ident);
  }

  @Test
  public void removeIdentData_validData() {
    var ident = new PadPipelineIdent();
    var identData = new PadPipelineIdentData();
    when(repository.getByPadPipelineIdent(ident)).thenReturn(Optional.of(identData));
    identDataService.removeIdentData(ident);
    verify(repository, times(1)).delete(identData);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void removeIdentData_noData() {
    var ident = new PadPipelineIdent();
    when(repository.getByPadPipelineIdent(ident)).thenReturn(Optional.empty());
    identDataService.removeIdentData(ident);
  }

  @Test
  public void saveEntityUsingForm_singleCore() {
    var form = new PipelineIdentDataForm();
    form.setInsulationCoatingType("test");
    form.setComponentPartsDescription("test");
    form.setProductsToBeConveyed("test");
    form.setExternalDiameter(BigDecimal.valueOf(999));
    form.setInternalDiameter(BigDecimal.valueOf(999));
    form.setWallThickness(BigDecimal.valueOf(999));
    form.setMaop(BigDecimal.valueOf(999));

    var identData = new PadPipelineIdentData();
    identData.setInsulationCoatingType("ins");
    identData.setComponentPartsDescription("comp");
    identData.setProductsToBeConveyed("prod");
    identData.setExternalDiameter(BigDecimal.valueOf(10));
    identData.setInternalDiameter(BigDecimal.valueOf(11));
    identData.setWallThickness(BigDecimal.valueOf(22.22));
    identData.setMaop(BigDecimal.valueOf(500));

    identData.setExternalDiameterMultiCore("some text");
    identData.setInternalDiameterMultiCore("some text");
    identData.setWallThicknessMultiCore("some text");
    identData.setMaopMultiCore("some text");
    identData.setInsulationCoatingTypeMultiCore("some text");
    identData.setProductsToBeConveyedMultiCore("some text");

    var pipeline = new PadPipeline();
    pipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    var pipelineIdent = new PadPipelineIdent();
    pipelineIdent.setPadPipeline(pipeline);
    identData.setPadPipelineIdent(pipelineIdent);

    identDataService.saveEntityUsingForm(identData, form);

    assertThat(identData.getComponentPartsDescription()).isEqualTo(form.getComponentPartsDescription());
    assertThat(identData.getInsulationCoatingType()).isEqualTo(form.getInsulationCoatingType());
    assertThat(identData.getProductsToBeConveyed()).isEqualTo(form.getProductsToBeConveyed());
    assertThat(identData.getExternalDiameter()).isEqualTo(form.getExternalDiameter());
    assertThat(identData.getInternalDiameter()).isEqualTo(form.getInternalDiameter());
    assertThat(identData.getWallThickness()).isEqualTo(form.getWallThickness());
    assertThat(identData.getMaop()).isEqualTo(form.getMaop());

    assertThat(identData.getExternalDiameterMultiCore()).isNull();
    assertThat(identData.getInternalDiameterMultiCore()).isNull();
    assertThat(identData.getWallThicknessMultiCore()).isNull();
    assertThat(identData.getMaopMultiCore()).isNull();
    assertThat(identData.getInsulationCoatingTypeMultiCore()).isNull();
    assertThat(identData.getProductsToBeConveyedMultiCore()).isNull();

    verify(repository, times(1)).save(identData);
  }

  @Test
  public void saveEntityUsingForm_multicore() {
    var form = new PipelineIdentDataForm();
    form.setComponentPartsDescription("comp");
    form.setExternalDiameterMultiCore("test");
    form.setInternalDiameterMultiCore("test");
    form.setWallThicknessMultiCore("test");
    form.setMaopMultiCore("test");
    form.setInsulationCoatingTypeMultiCore("test");
    form.setProductsToBeConveyedMultiCore("test");

    var identData = new PadPipelineIdentData();
    identData.setInsulationCoatingType("ins");
    identData.setComponentPartsDescription("comp");
    identData.setProductsToBeConveyed("prod");
    identData.setExternalDiameter(BigDecimal.valueOf(10));
    identData.setInternalDiameter(BigDecimal.valueOf(11));
    identData.setWallThickness(BigDecimal.valueOf(22.22));
    identData.setMaop(BigDecimal.valueOf(500));

    identData.setExternalDiameterMultiCore("some text");
    identData.setInternalDiameterMultiCore("some text");
    identData.setWallThicknessMultiCore("some text");
    identData.setMaopMultiCore("some text");
    identData.setInsulationCoatingTypeMultiCore("some text");
    identData.setProductsToBeConveyedMultiCore("some text");

    var pipeline = new PadPipeline();
    pipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER);
    var pipelineIdent = new PadPipelineIdent();
    pipelineIdent.setPadPipeline(pipeline);
    identData.setPadPipelineIdent(pipelineIdent);

    identDataService.saveEntityUsingForm(identData, form);

    assertThat(identData.getComponentPartsDescription()).isEqualTo("comp");
    assertThat(identData.getInsulationCoatingType()).isNull();
    assertThat(identData.getProductsToBeConveyed()).isNull();
    assertThat(identData.getExternalDiameter()).isNull();
    assertThat(identData.getInternalDiameter()).isNull();
    assertThat(identData.getWallThickness()).isNull();
    assertThat(identData.getMaop()).isNull();

    assertThat(identData.getExternalDiameterMultiCore()).isEqualTo(form.getExternalDiameterMultiCore());
    assertThat(identData.getInternalDiameterMultiCore()).isEqualTo(form.getInternalDiameterMultiCore());
    assertThat(identData.getWallThicknessMultiCore()).isEqualTo(form.getWallThicknessMultiCore());
    assertThat(identData.getMaopMultiCore()).isEqualTo(form.getMaopMultiCore());
    assertThat(identData.getInsulationCoatingTypeMultiCore()).isEqualTo(form.getInsulationCoatingTypeMultiCore());
    assertThat(identData.getProductsToBeConveyedMultiCore()).isEqualTo(form.getProductsToBeConveyedMultiCore());

    verify(repository, times(1)).save(identData);
  }

  @Test
  public void updateIdentData_repositoryInteraction() {
    var pipeline = new PadPipeline();
    pipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    var ident = new PadPipelineIdent();
    ident.setPadPipeline(pipeline);
    var identData = new PadPipelineIdentData();
    identData.setPadPipelineIdent(ident);

    var dataForm = new PipelineIdentDataForm();

    when(repository.getByPadPipelineIdent(ident)).thenReturn(Optional.of(identData));

    identDataService.updateIdentData(ident, dataForm);
    verify(repository, times(1)).save(identData);
  }

  @Test
  public void saveAll_serviceInteraction() {
    identDataService.saveAll(List.of());
    verify(repository, times(1)).saveAll(List.of());
  }

  @Test
  public void removeIdentDataForPipeline_serviceInteraction() {
    var pipeline = new PadPipeline();
    var identData = new PadPipelineIdentData();
    when(repository.getAllByPadPipelineIdent_PadPipeline(pipeline))
        .thenReturn(List.of(identData));
    identDataService.removeIdentDataForPipeline(pipeline);
    verify(repository, times(1)).deleteAll(List.of(identData));
  }

}
