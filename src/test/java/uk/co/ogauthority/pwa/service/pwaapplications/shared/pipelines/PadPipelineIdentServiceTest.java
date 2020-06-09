package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.util.FieldUtils;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentRepository;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.CoordinateUtils;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineIdentServiceTest {

  @Mock
  private PadPipelineIdentRepository repository;

  @Mock
  private PadPipelineIdentDataService identDataService;

  private PadPipelineIdentService identService;

  @Captor
  private ArgumentCaptor<PadPipelineIdent> identCaptor;

  @Captor
  private ArgumentCaptor<PadPipelineIdentData> identDataCaptor;

  @Before
  public void setUp() {
    identService = new PadPipelineIdentService(repository, identDataService);
  }

  @Test
  public void addIdent() throws IllegalAccessException {

    var pipeline = new PadPipeline();
    var form = new PipelineIdentForm();

    form.setFromLocation("from");
    var fromCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(55, 55, BigDecimal.valueOf(55.55), LatitudeDirection.NORTH),
            new LongitudeCoordinate(12, 12, BigDecimal.valueOf(12), LongitudeDirection.EAST)
        ), fromCoordinateForm
    );
    form.setFromCoordinateForm(fromCoordinateForm);

    form.setToLocation("to");
    var toCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(46, 46, BigDecimal.valueOf(46), LatitudeDirection.SOUTH),
            new LongitudeCoordinate(6, 6, BigDecimal.valueOf(6.66), LongitudeDirection.WEST)
        ), toCoordinateForm
    );
    form.setToCoordinateForm(toCoordinateForm);

    form.setLength(BigDecimal.valueOf(65.5));

    var dataForm = new PipelineIdentDataForm();
    dataForm.setExternalDiameter(BigDecimal.valueOf(12.1));
    dataForm.setInternalDiameter(BigDecimal.valueOf(12.1));
    dataForm.setWallThickness(BigDecimal.valueOf(12.1));
    dataForm.setMaop(BigDecimal.valueOf(12.1));
    dataForm.setProductsToBeConveyed("prod");
    dataForm.setComponentPartsDescription("component");
    dataForm.setInsulationCoatingType("ins");
    form.setDataForm(dataForm);

    identService.addIdent(pipeline, form);

    verify(repository, times(1)).save(identCaptor.capture());
    var newIdent = identCaptor.getValue();

    verify(identDataService, times(1)).addIdentData(newIdent, form.getDataForm());

    assertThat(newIdent.getPadPipeline()).isEqualTo(pipeline);
    assertThat(newIdent.getFromLocation()).isEqualTo(form.getFromLocation());
    assertThat(FieldUtils.getFieldValue(newIdent, "fromLatitudeDegrees")).isEqualTo(
        form.getFromCoordinateForm().getLatitudeDegrees());
    assertThat(FieldUtils.getFieldValue(newIdent, "fromLatitudeMinutes")).isEqualTo(
        form.getFromCoordinateForm().getLatitudeMinutes());
    assertThat(FieldUtils.getFieldValue(newIdent, "fromLatitudeSeconds")).isEqualTo(
        form.getFromCoordinateForm().getLatitudeSeconds());
    assertThat(FieldUtils.getFieldValue(newIdent, "fromLatitudeDirection")).isEqualTo(
        form.getFromCoordinateForm().getLatitudeDirection());
    assertThat(FieldUtils.getFieldValue(newIdent, "fromLongitudeDegrees")).isEqualTo(
        form.getFromCoordinateForm().getLongitudeDegrees());
    assertThat(FieldUtils.getFieldValue(newIdent, "fromLongitudeMinutes")).isEqualTo(
        form.getFromCoordinateForm().getLongitudeMinutes());
    assertThat(FieldUtils.getFieldValue(newIdent, "fromLongitudeSeconds")).isEqualTo(
        form.getFromCoordinateForm().getLongitudeSeconds());
    assertThat(FieldUtils.getFieldValue(newIdent, "fromLongitudeDirection")).isEqualTo(
        form.getFromCoordinateForm().getLongitudeDirection());

    assertThat(FieldUtils.getFieldValue(newIdent, "toLatitudeDegrees")).isEqualTo(
        form.getToCoordinateForm().getLatitudeDegrees());
    assertThat(FieldUtils.getFieldValue(newIdent, "toLatitudeMinutes")).isEqualTo(
        form.getToCoordinateForm().getLatitudeMinutes());
    assertThat(FieldUtils.getFieldValue(newIdent, "toLatitudeSeconds")).isEqualTo(
        form.getToCoordinateForm().getLatitudeSeconds());
    assertThat(FieldUtils.getFieldValue(newIdent, "toLatitudeDirection")).isEqualTo(
        form.getToCoordinateForm().getLatitudeDirection());
    assertThat(FieldUtils.getFieldValue(newIdent, "toLongitudeDegrees")).isEqualTo(
        form.getToCoordinateForm().getLongitudeDegrees());
    assertThat(FieldUtils.getFieldValue(newIdent, "toLongitudeMinutes")).isEqualTo(
        form.getToCoordinateForm().getLongitudeMinutes());
    assertThat(FieldUtils.getFieldValue(newIdent, "toLongitudeSeconds")).isEqualTo(
        form.getToCoordinateForm().getLongitudeSeconds());
    assertThat(FieldUtils.getFieldValue(newIdent, "toLongitudeDirection")).isEqualTo(
        form.getToCoordinateForm().getLongitudeDirection());

    assertThat(newIdent.getLength()).isEqualTo(form.getLength());

  }

  @Test
  public void getIdentViews() {

    var coordinatePair = new CoordinatePair(
        new LatitudeCoordinate(1, 1, BigDecimal.ONE, LatitudeDirection.NORTH),
        new LongitudeCoordinate(1, 1, BigDecimal.ONE, LongitudeDirection.EAST)
    );

    var ident = makeIdent(1, "from", "to");
    var identData = makeIdentData(ident);

    var pipeline = new PadPipeline();
    when(repository.getAllByPadPipeline(pipeline)).thenReturn(List.of(ident));
    when(identDataService.getDataFromIdentList(eq(List.of(ident)))).thenReturn(Map.of(ident, identData));
    List<IdentView> identViews = identService.getIdentViews(pipeline);

    var view = identViews.get(0);

    assertThat(view.getFromCoordinates()).isEqualTo(ident.getFromCoordinates());
    assertThat(view.getToCoordinates()).isEqualTo(ident.getToCoordinates());
    assertThat(view.getFromLocation()).isEqualTo(ident.getFromLocation());
    assertThat(view.getToLocation()).isEqualTo(ident.getToLocation());
    assertThat(view.getIdentNumber()).isEqualTo(ident.getIdentNo());
    assertThat(view.getLength()).isEqualTo(ident.getLength());
    assertThat(view.getComponentPartsDescription()).isEqualTo(identData.getComponentPartsDescription());
    assertThat(view.getExternalDiameter()).isEqualTo(identData.getExternalDiameter());
    assertThat(view.getInsulationCoatingType()).isEqualTo(identData.getInsulationCoatingType());
    assertThat(view.getMaop()).isEqualTo(identData.getMaop());
    assertThat(view.getProductsToBeConveyed()).isEqualTo(identData.getProductsToBeConveyed());
    assertThat(view.getInternalDiameter()).isEqualTo(identData.getInternalDiameter());
    assertThat(view.getWallThickness()).isEqualTo(identData.getWallThickness());
  }

  @Test
  public void getConnectedPipelineIdentSummaryView_valid() {

    var identAStart = makeIdent(1, "from", "to");
    var identAEnd = makeIdent(2, "to", "other");
    var identDataAStart = makeIdentData(identAStart);
    var identDataAEnd = makeIdentData(identAEnd);

    var identB = makeIdent(3, "from2", "to2");
    var identDataB = makeIdentData(identB);


    var pipeline = new PadPipeline();
    when(repository.getAllByPadPipeline(pipeline)).thenReturn(List.of(identAStart, identAEnd, identB));
    when(identDataService.getDataFromIdentList(eq(List.of(identAStart, identAEnd, identB))))
        .thenReturn(new LinkedHashMap<>() {{
          put(identAStart, identDataAStart);
          put(identAEnd, identDataAEnd);
          put(identB, identDataB);
        }});
    ConnectedPipelineIdentSummaryView summaryView = identService.getConnectedPipelineIdentSummaryView(pipeline);
    assertThat(summaryView.getConnectedPipelineIdents().size()).isEqualTo(2);
    assertThat(summaryView.getConnectedPipelineIdents().get(0).getIdentViews())
        .extracting(IdentView::getIdentNumber)
        .containsExactly(1, 2);
    assertThat(summaryView.getConnectedPipelineIdents().get(1).getIdentViews())
        .extracting(IdentView::getIdentNumber)
        .containsExactly(3);
  }

  @Test
  public void getConnectedPipelineIdentSummaryView_noIdents() {

    var pipeline = new PadPipeline();
    when(repository.getAllByPadPipeline(pipeline)).thenReturn(List.of());
    when(identDataService.getDataFromIdentList(eq(List.of())))
        .thenReturn(new LinkedHashMap<>());
    ConnectedPipelineIdentSummaryView summaryView = identService.getConnectedPipelineIdentSummaryView(pipeline);
    assertThat(summaryView.getConnectedPipelineIdents()).isEmpty();
  }

  private PadPipelineIdent makeIdent(int id, String fromLocation, String toLocation) {
    var coordinatePair = new CoordinatePair(
        new LatitudeCoordinate(1, 1, BigDecimal.ONE, LatitudeDirection.NORTH),
        new LongitudeCoordinate(1, 1, BigDecimal.ONE, LongitudeDirection.EAST)
    );
    var ident = new PadPipelineIdent();
    ident.setLength(new BigDecimal(32));
    ident.setFromCoordinates(coordinatePair);
    ident.setToCoordinates(coordinatePair);
    ident.setFromLocation(fromLocation);
    ident.setToLocation(toLocation);
    ident.setIdentNo(id);
    return ident;
  }

  private PadPipelineIdentData makeIdentData(PadPipelineIdent ident) {
    var identData = new PadPipelineIdentData(ident);
    identData.setComponentPartsDescription("desc");
    identData.setExternalDiameter(BigDecimal.ONE);
    identData.setInsulationCoatingType("type");
    identData.setMaop(BigDecimal.ONE);
    identData.setProductsToBeConveyed("products");
    identData.setInternalDiameter(BigDecimal.TEN);
    identData.setWallThickness(BigDecimal.ONE);
    return identData;
  }

  @Test
  public void removeIdent() {

    var pipeline = new PadPipeline();

    var ident = new PadPipelineIdent();
    ident.setPadPipeline(pipeline);
    ident.setIdentNo(1);

    var ident2 = new PadPipelineIdent();
    ident2.setPadPipeline(pipeline);
    ident2.setIdentNo(2);

    var ident3 = new PadPipelineIdent();
    ident3.setPadPipeline(pipeline);
    ident3.setIdentNo(3);

    when(repository.getAllByPadPipeline(pipeline)).thenReturn(List.of(ident, ident3));

    identService.removeIdent(ident2);

    verify(identDataService, times(1)).removeIdentData(ident2);

    var captor = ArgumentCaptor.forClass(Iterable.class);
    verify(repository, times(1)).saveAll(captor.capture());

    assertThat((List<PadPipelineIdent>) captor.getValue()).extracting(PadPipelineIdent::getIdentNo)
        .containsExactly(1, 2);
  }

  @Test
  public void validateSection_valid() {
    var padPipeline = new PadPipeline();
    when(repository.countAllByPadPipeline(padPipeline)).thenReturn(1L);
    var bindingResult = identService.validateSection(padPipeline);
    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  public void validateSection_invalid() {
    var padPipeline = new PadPipeline();
    when(repository.countAllByPadPipeline(padPipeline)).thenReturn(0L);
    var bindingResult = identService.validateSection(padPipeline);
    assertThat(bindingResult.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .containsExactly("idents" + FieldValidationErrorCodes.REQUIRED.getCode());
  }

}
