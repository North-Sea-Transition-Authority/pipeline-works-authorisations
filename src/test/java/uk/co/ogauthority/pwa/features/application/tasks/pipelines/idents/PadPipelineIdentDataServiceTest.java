package uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;
import uk.co.ogauthority.pwa.util.validation.PipelineValidationUtils;

@ExtendWith(MockitoExtension.class)
class PadPipelineIdentDataServiceTest {

  @Mock
  private PadPipelineIdentDataRepository repository;

  private PadPipelineIdentDataService identDataService;

  @Captor
  private ArgumentCaptor<PadPipelineIdentData> identDataCaptor;

  @BeforeEach
  void setUp() {
    identDataService = new PadPipelineIdentDataService(repository);
  }

  @Test
  void addIdentData() {

    var ident = new PadPipelineIdent();

    var form = new PipelineIdentDataForm();
    form.setInsulationCoatingType("ins");
    form.setComponentPartsDescription("comp");
    form.setProductsToBeConveyed("prod");
    form.setExternalDiameter(DecimalInput.from(10));
    form.setInternalDiameter(DecimalInput.from(11));
    form.setWallThickness(DecimalInput.from(22.22));
    form.setMaop(DecimalInput.from(500));

    form.setExternalDiameterMultiCore("some text");
    form.setInternalDiameterMultiCore("some text");
    form.setWallThicknessMultiCore("some text");
    form.setMaopMultiCore("some text");
    form.setInsulationCoatingTypeMultiCore("some text");
    form.setProductsToBeConveyedMultiCore("some text");

    var pipeline = new PadPipeline();
    pipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    ident.setPadPipeline(pipeline);

    identDataService.addIdentData(ident, false, form);

    verify(repository, times(1)).save(identDataCaptor.capture());

    var identData = identDataCaptor.getValue();

    assertThat(identData.getPadPipelineIdent()).isEqualTo(ident);
    assertThat(identData.getComponentPartsDesc()).isEqualTo(form.getComponentPartsDescription());
    assertThat(identData.getInsulationCoatingType()).isEqualTo(form.getInsulationCoatingType());
    assertThat(identData.getProductsToBeConveyed()).isEqualTo(form.getProductsToBeConveyed());
    assertThat(identData.getExternalDiameter()).isEqualTo(form.getExternalDiameter().createBigDecimalOrNull());
    assertThat(identData.getInternalDiameter()).isEqualTo(form.getInternalDiameter().createBigDecimalOrNull());
    assertThat(identData.getWallThickness()).isEqualTo(form.getWallThickness().createBigDecimalOrNull());
    assertThat(identData.getMaop()).isEqualTo(form.getMaop().createBigDecimalOrNull());

  }

  @Test
  void getDataFromIdentList_valid() {
    var ident = new PadPipelineIdent();
    ident.setIdentNo(1);

    var identData = new PadPipelineIdentData(ident);
    identData.setComponentPartsDesc("parts");

    when(repository.getAllByPadPipelineIdentIn(eq(List.of(ident)))).thenReturn(List.of(identData));
    var result = identDataService.getDataFromIdentList(List.of(ident));
    assertThat(result).containsExactly(
        entry(ident, identData)
    );
  }

  @Test
  void getDataFromIdentList_emptyList() {
    var ident = new PadPipelineIdent();
    ident.setIdentNo(1);

    var identData = new PadPipelineIdentData(ident);
    identData.setComponentPartsDesc("parts");

    when(repository.getAllByPadPipelineIdentIn(eq(List.of()))).thenReturn(List.of());
    var result = identDataService.getDataFromIdentList(List.of());
    assertThat(result).isEmpty();
  }

  @Test
  void getIdentData_valid() {
    var ident = new PadPipelineIdent();
    var data = new PadPipelineIdentData();
    when(repository.getByPadPipelineIdent(ident)).thenReturn(Optional.of(data));
    var result = identDataService.getIdentData(ident);
    assertThat(result).isEqualTo(data);
  }

  @Test
  void getIdentData_invalid() {
    var ident = new PadPipelineIdent();
    when(repository.getByPadPipelineIdent(ident)).thenReturn(Optional.empty());
    assertThrows(PwaEntityNotFoundException.class, () ->
      identDataService.getIdentData(ident));
  }

  @Test
  void removeIdentData_validData() {
    var ident = new PadPipelineIdent();
    var identData = new PadPipelineIdentData();
    when(repository.getByPadPipelineIdent(ident)).thenReturn(Optional.of(identData));
    identDataService.removeIdentData(ident);
    verify(repository, times(1)).delete(identData);
  }

  @Test
  void removeIdentData_noData() {
    var ident = new PadPipelineIdent();
    when(repository.getByPadPipelineIdent(ident)).thenReturn(Optional.empty());
    assertThrows(PwaEntityNotFoundException.class, () ->
      identDataService.removeIdentData(ident));
  }

  @Test
  void saveEntityUsingForm_singleCore_isDefiningStructure_singleAndMultiCoreFieldsSetAccordingly() throws IllegalAccessException {

    var form = new PipelineIdentDataForm();
    form.setProductsToBeConveyed("text");

    var identData = PadPipelineTestUtil.createPadPipeline(
        PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL), PipelineType.PRODUCTION_FLOWLINE);

    identDataService.saveEntityUsingForm(identData, true, form);
    assertThat(identData.getProductsToBeConveyed()).isEqualTo(form.getProductsToBeConveyed());

    assertThat(identData.getExternalDiameter()).isNull();
    assertThat(identData.getInternalDiameter()).isNull();
    assertThat(identData.getWallThickness()).isNull();
    assertThat(identData.getMaop()).isNull();
    assertThat(identData.getInsulationCoatingType()).isNull();

    assertThat(identData.getExternalDiameterMultiCore()).isNull();
    assertThat(identData.getInternalDiameterMultiCore()).isNull();
    assertThat(identData.getWallThicknessMultiCore()).isNull();
    assertThat(identData.getMaopMultiCore()).isNull();
    assertThat(identData.getInsulationCoatingTypeMultiCore()).isNull();
    assertThat(identData.getProductsToBeConveyedMultiCore()).isNull();
  }

  @Test
  void saveEntityUsingForm_multiCore_isDefiningStructure_singleAndMultiCoreFieldsSetAccordingly() throws IllegalAccessException {

    var form = new PipelineIdentDataForm();
    form.setProductsToBeConveyedMultiCore("text");

    var identData = PadPipelineTestUtil.createPadPipeline(
        PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL), PipelineType.SERVICES_UMBILICAL);

    identDataService.saveEntityUsingForm(identData, true, form);
    assertThat(identData.getProductsToBeConveyedMultiCore()).isEqualTo(form.getProductsToBeConveyedMultiCore());

    assertThat(identData.getExternalDiameter()).isNull();
    assertThat(identData.getInternalDiameter()).isNull();
    assertThat(identData.getWallThickness()).isNull();
    assertThat(identData.getMaop()).isNull();
    assertThat(identData.getInsulationCoatingType()).isNull();
    assertThat(identData.getProductsToBeConveyed()).isNull();

    assertThat(identData.getExternalDiameterMultiCore()).isNull();
    assertThat(identData.getInternalDiameterMultiCore()).isNull();
    assertThat(identData.getWallThicknessMultiCore()).isNull();
    assertThat(identData.getMaopMultiCore()).isNull();
    assertThat(identData.getInsulationCoatingTypeMultiCore()).isNull();
  }

  @Test
  void saveEntityUsingForm_singleCore() {
    var form = new PipelineIdentDataForm();
    form.setInsulationCoatingType("test");
    form.setComponentPartsDescription("test");
    form.setProductsToBeConveyed("test");
    form.setExternalDiameter(DecimalInput.from(999));
    form.setInternalDiameter(DecimalInput.from(999));
    form.setWallThickness(DecimalInput.from(999));
    form.setMaop(DecimalInput.from(999));

    var identData = new PadPipelineIdentData();
    identData.setInsulationCoatingType("ins");
    identData.setComponentPartsDesc("comp");
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

    identDataService.saveEntityUsingForm(identData, false, form);

    assertThat(identData.getComponentPartsDesc()).isEqualTo(form.getComponentPartsDescription());
    assertThat(identData.getInsulationCoatingType()).isEqualTo(form.getInsulationCoatingType());
    assertThat(identData.getProductsToBeConveyed()).isEqualTo(form.getProductsToBeConveyed());
    assertThat(identData.getExternalDiameter()).isEqualTo(form.getExternalDiameter().createBigDecimalOrNull());
    assertThat(identData.getInternalDiameter()).isEqualTo(form.getInternalDiameter().createBigDecimalOrNull());
    assertThat(identData.getWallThickness()).isEqualTo(form.getWallThickness().createBigDecimalOrNull());
    assertThat(identData.getMaop()).isEqualTo(form.getMaop().createBigDecimalOrNull());

    assertThat(identData.getExternalDiameterMultiCore()).isNull();
    assertThat(identData.getInternalDiameterMultiCore()).isNull();
    assertThat(identData.getWallThicknessMultiCore()).isNull();
    assertThat(identData.getMaopMultiCore()).isNull();
    assertThat(identData.getInsulationCoatingTypeMultiCore()).isNull();
    assertThat(identData.getProductsToBeConveyedMultiCore()).isNull();

    verify(repository, times(1)).save(identData);
  }

  @Test
  void saveEntityUsingForm_multicore() {
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
    identData.setComponentPartsDesc("comp");
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
    pipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER_MULTI_CORE);
    var pipelineIdent = new PadPipelineIdent();
    pipelineIdent.setPadPipeline(pipeline);
    identData.setPadPipelineIdent(pipelineIdent);

    identDataService.saveEntityUsingForm(identData, false, form);

    assertThat(identData.getComponentPartsDesc()).isEqualTo("comp");
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
  void updateIdentData_verifyRepositoryInteraction() {
    var pipeline = new PadPipeline();
    pipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    var ident = new PadPipelineIdent();
    ident.setPadPipeline(pipeline);
    var identData = new PadPipelineIdentData();
    identData.setPadPipelineIdent(ident);

    var dataForm = PipelineValidationUtils.createEmptyPipelineIdentDataForm();

    when(repository.getByPadPipelineIdent(ident)).thenReturn(Optional.of(identData));

    identDataService.updateIdentData(ident, false, dataForm);
    verify(repository, times(1)).save(identData);
  }

  @Test
  void saveAll_serviceInteraction() {
    identDataService.saveAll(List.of());
    verify(repository, times(1)).saveAll(List.of());
  }

  @Test
  void removeIdentDataForPipeline_serviceInteraction() {
    var pipeline = new PadPipeline();
    var identData = new PadPipelineIdentData();
    when(repository.getAllByPadPipelineIdent_PadPipeline(pipeline))
        .thenReturn(List.of(identData));
    identDataService.removeIdentDataForPipeline(pipeline);
    verify(repository, times(1)).deleteAll(List.of(identData));
  }

}
