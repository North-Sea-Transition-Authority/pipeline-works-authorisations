package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.util.FieldUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.form.fds.ErrorItem;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.CoordinatePairTestUtil;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentRepository;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.pipelinedatautils.PipelineIdentViewCollectorService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.tasklist.PadPipelineTaskListServiceTestUtil;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.CoordinateUtils;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineIdentServiceTest {

  private final static PipelineId PIPELINE_ID = new PipelineId(10);
  private static final int PAD_PIPELINE_1_ID = 1;

  @Mock
  private PadPipelineIdentRepository padPipelineIdentRepository;

  @Mock
  private PadPipelineIdentDataService padPipelineIdentDataService;

  private PadPipelineIdentService padPipelineIdentService;

  @Mock
  private PadPipelinePersisterService padPipelinePersisterService;

  @Mock
  private PipelineIdentFormValidator pipelineIdentFormValidator;

  // not a mock to allow full testing with labda params
  private PipelineIdentViewCollectorService pipelineIdentViewCollectorService;

  @Captor
  private ArgumentCaptor<PadPipelineIdent> identCaptor;

  @Captor
  private ArgumentCaptor<PadPipelineIdentData> identDataCaptor;

  private Pipeline pipeline;
  private PadPipeline padPipeline;
  private PadPipelineIdent ident;
  private PadPipelineIdentData identData;

  private PwaApplicationDetail detail;

  private final static String IDENT_FROM_LOCATION_MISMATCH_ERROR_MSG =
      "The from structure and coordinates of the first ident must match the from structure and coordinates in the pipeline header";

  private final static String IDENT_TO_LOCATION_MISMATCH_ERROR_MSG =
      "The to structure and coordinates of the last ident must match the to structure and coordinates in the pipeline header";

  @Before
  public void setUp() {

    pipelineIdentViewCollectorService = new PipelineIdentViewCollectorService();

    padPipelineIdentService = new PadPipelineIdentService(
        padPipelineIdentRepository,
        padPipelineIdentDataService,
        padPipelinePersisterService,
        pipelineIdentFormValidator,
        pipelineIdentViewCollectorService);
    pipeline = new Pipeline();
    pipeline.setId(PIPELINE_ID.asInt());
    padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    padPipeline.setPipelineType(PipelineType.CABLE);
    ident = makeIdent(1, "from", "to");
    ident.setPadPipeline(padPipeline);
    identData = makeIdentData(ident);
    padPipeline.setLength(ident.getLength());
    padPipeline.setFromLocation(ident.getFromLocation());
    padPipeline.setFromCoordinates(ident.getFromCoordinates());
    padPipeline.setToLocation(ident.getToLocation());
    padPipeline.setToCoordinates(ident.getToCoordinates());

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

  }

  @Test
  public void addIdent() throws IllegalAccessException {

    padPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    padPipeline.setPipelineInBundle(false);
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
    form.setDefiningStructure(false);

    var dataForm = new PipelineIdentDataForm();
    dataForm.setExternalDiameter(BigDecimal.valueOf(12.1));
    dataForm.setInternalDiameter(BigDecimal.valueOf(12.1));
    dataForm.setWallThickness(BigDecimal.valueOf(12.1));
    dataForm.setMaop(BigDecimal.valueOf(12.1));
    dataForm.setProductsToBeConveyed("prod");
    dataForm.setComponentPartsDescription("component");
    dataForm.setInsulationCoatingType("ins");
    form.setDataForm(dataForm);

    padPipelineIdentService.addIdent(padPipeline, form);

    verify(padPipelineIdentRepository, times(1)).save(identCaptor.capture());
    var newIdent = identCaptor.getValue();

    verify(padPipelineIdentDataService, times(1)).addIdentData(newIdent, form.getDataForm());
    verify(padPipelineIdentDataService, never()).updateIdentData(any(), any());

    assertThat(newIdent.getPadPipeline()).isEqualTo(padPipeline);
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
    assertThat(newIdent.getIsDefiningStructure()).isEqualTo(form.getDefiningStructure());

  }

  @Test(expected= PwaEntityNotFoundException.class)
  public void getIdentViewsFromOverview_whenNoPadPipelineIdentOnOverview(){

   var pipelineOverview = mock(PipelineOverview.class);
   when(pipelineOverview.getPadPipelineId()).thenReturn(null);
    padPipelineIdentService.getIdentViewsFromOverview(pipelineOverview);

  }

  @Test
  public void getIdentViewsFromOverview_whenIdentFoundMapsCorrectly(){
    var pipelineOverview = mock(PipelineOverview.class);
    when(padPipelineIdentRepository.getAllByPadPipeline_IdIn(any())).thenReturn(List.of(ident));
    when(padPipelineIdentDataService.getDataFromIdentList(eq(List.of(ident)))).thenReturn(Map.of(ident, identData));

    List<IdentView> identViews =  padPipelineIdentService.getIdentViewsFromOverview(pipelineOverview);

    var view = identViews.get(0);
    assertIdentViewMatchesIdent(view, ident, identData);

  }

  @Test
  public void getIdentViews() {
    var pipeline = new PadPipeline();
    when(padPipelineIdentRepository.getAllByPadPipeline(pipeline)).thenReturn(List.of(ident));
    when(padPipelineIdentDataService.getDataFromIdentList(eq(List.of(ident)))).thenReturn(Map.of(ident, identData));
    List<IdentView> identViews = padPipelineIdentService.getIdentViews(pipeline);

    var view = identViews.get(0);
    assertIdentViewMatchesIdent(view, ident, identData);

  }

  private void assertIdentViewMatchesIdent(IdentView view, PadPipelineIdent ident,PadPipelineIdentData identData){
    assertThat(view.getFromCoordinates()).isEqualTo(ident.getFromCoordinates());
    assertThat(view.getToCoordinates()).isEqualTo(ident.getToCoordinates());
    assertThat(view.getFromLocation()).isEqualTo(ident.getFromLocation());
    assertThat(view.getToLocation()).isEqualTo(ident.getToLocation());
    assertThat(view.getIdentNumber()).isEqualTo(ident.getIdentNo());
    assertThat(view.getLength()).isEqualTo(ident.getLength());
    assertThat(view.getComponentPartsDescription()).isEqualTo(identData.getComponentPartsDesc());
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
    var idents = List.of(identAStart, identAEnd, identB);
    when(padPipelineIdentRepository.getAllByPadPipeline(pipeline)).thenReturn(idents);
    when(padPipelineIdentDataService.getDataFromIdentList(eq(idents)))
        .thenReturn(new LinkedHashMap<>() {{
          put(identAStart, identDataAStart);
          put(identAEnd, identDataAEnd);
          put(identB, identDataB);
        }});
    ConnectedPipelineIdentSummaryView summaryView = padPipelineIdentService.getConnectedPipelineIdentSummaryView(pipeline);
    assertThat(summaryView.getConnectedPipelineIdents().size()).isEqualTo(2);
    assertThat(summaryView.getConnectedPipelineIdents().get(0).getIdentViews())
        .extracting(IdentView::getIdentNumber)
        .containsExactly(1, 2);
    assertThat(summaryView.getConnectedPipelineIdents().get(1).getIdentViews())
        .extracting(IdentView::getIdentNumber)
        .containsExactly(3);

    var expectedLengthDisplay = idents.stream()
        .map(PadPipelineIdent::getLength)
        .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP).toPlainString() + "m";
    assertThat(summaryView.getTotalIdentLength()).isEqualTo(expectedLengthDisplay);
  }

  @Test
  public void getConnectedPipelineIdentSummaryView_noIdents() {

    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline)).thenReturn(List.of());
    when(padPipelineIdentDataService.getDataFromIdentList(eq(List.of())))
        .thenReturn(new LinkedHashMap<>());
    ConnectedPipelineIdentSummaryView summaryView = padPipelineIdentService.getConnectedPipelineIdentSummaryView(padPipeline);
    assertThat(summaryView.getConnectedPipelineIdents()).isEmpty();
    assertThat(summaryView.getTotalIdentLength()).isEqualTo("0m");
  }

  private PadPipelineIdent makeIdent(int id, String fromLocation, String toLocation) {
    var coordinatePair = new CoordinatePair(
        new LatitudeCoordinate(1, 1, BigDecimal.ONE, LatitudeDirection.NORTH),
        new LongitudeCoordinate(1, 1, BigDecimal.ONE, LongitudeDirection.EAST)
    );
    var padPipeline = new PadPipeline();
    padPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    var ident = new PadPipelineIdent();
    ident.setPadPipeline(padPipeline);
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
    identData.setComponentPartsDesc("desc");
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

    padPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    padPipeline.setPipelineInBundle(false);

    var ident = new PadPipelineIdent();
    ident.setPadPipeline(padPipeline);
    ident.setIdentNo(1);

    var ident2 = new PadPipelineIdent();
    ident2.setPadPipeline(padPipeline);
    ident2.setIdentNo(2);

    var ident3 = new PadPipelineIdent();
    ident3.setPadPipeline(padPipeline);
    ident3.setIdentNo(3);

    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline)).thenReturn(List.of(ident, ident3));

    padPipelineIdentService.removeIdent(ident2);

    verify(padPipelineIdentDataService, times(1)).removeIdentData(ident2);

    var captor = ArgumentCaptor.forClass(Iterable.class);
    verify(padPipelineIdentRepository, times(1)).saveAll(captor.capture());

    assertThat((List<PadPipelineIdent>) captor.getValue()).extracting(PadPipelineIdent::getIdentNo)
        .containsExactly(1, 2);
  }

  @Test
  public void updateIdent() {
    var form = new PipelineIdentForm();
    form.setFromLocation("from");
    form.setToLocation("to");
    form.setLength(BigDecimal.ONE);
    form.setDefiningStructure(false);

    form.setFromCoordinateForm(new CoordinateForm());
    form.setToCoordinateForm(new CoordinateForm());
    CoordinateUtils.mapCoordinatePairToForm(new CoordinatePair(
        new LatitudeCoordinate(1, 1, BigDecimal.ZERO, LatitudeDirection.NORTH),
        new LongitudeCoordinate(1, 1, BigDecimal.ZERO, LongitudeDirection.EAST)
    ), form.getFromCoordinateForm());
    CoordinateUtils.mapCoordinatePairToForm(new CoordinatePair(
        new LatitudeCoordinate(1, 1, BigDecimal.ZERO, LatitudeDirection.NORTH),
        new LongitudeCoordinate(1, 1, BigDecimal.ZERO, LongitudeDirection.EAST)
    ), form.getToCoordinateForm());
    form.setDataForm(new PipelineIdentDataForm());

    var identData = new PadPipelineIdentData();
    when(padPipelineIdentDataService.getOptionalOfIdentData(ident)).thenReturn(Optional.of(identData));

    padPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    padPipeline.setPipelineInBundle(false);
    ident.setPadPipeline(padPipeline);

    padPipelineIdentService.updateIdent(ident, form);
    verify(padPipelineIdentRepository, times(1)).save(ident);
    verify(padPipelineIdentDataService, times(1)).updateIdentData(ident, form.getDataForm());
    verify(padPipelineIdentDataService, never()).addIdentData(any(), any());

    assertThat(ident.getFromCoordinates()).isEqualTo(
        CoordinateUtils.coordinatePairFromForm(form.getFromCoordinateForm()));
    assertThat(ident.getFromCoordinates()).isEqualTo(
        CoordinateUtils.coordinatePairFromForm(form.getFromCoordinateForm()));
    assertThat(ident.getFromLocation()).isEqualTo(form.getFromLocation());
    assertThat(ident.getToLocation()).isEqualTo(form.getToLocation());
    assertThat(ident.getLength()).isEqualTo(form.getLength());
    assertThat(ident.getIsDefiningStructure()).isEqualTo(form.getDefiningStructure());
  }

  @Test
  public void mapEntityToForm_notDefiningStructure() {
    var form = new PipelineIdentForm();
    var ident = new PadPipelineIdent();

    ident.setFromLocation("from");
    ident.setToLocation("to");
    ident.setLength(BigDecimal.ONE);
    ident.setDefiningStructure(false);
    ident.setFromCoordinates(new CoordinatePair(
        new LatitudeCoordinate(1, 1, BigDecimal.ZERO, LatitudeDirection.NORTH),
        new LongitudeCoordinate(1, 1, BigDecimal.ZERO, LongitudeDirection.EAST)
    ));
    ident.setToCoordinates(new CoordinatePair(
        new LatitudeCoordinate(1, 1, BigDecimal.ZERO, LatitudeDirection.NORTH),
        new LongitudeCoordinate(1, 1, BigDecimal.ZERO, LongitudeDirection.EAST)
    ));
    form.setDataForm(new PipelineIdentDataForm());

    padPipelineIdentService.mapEntityToForm(ident, form);

    var coordinateFromForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(ident.getFromCoordinates(), coordinateFromForm);
    assertThat(form.getFromCoordinateForm()).isEqualTo(coordinateFromForm);

    var coordinateToForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(ident.getToCoordinates(), coordinateToForm);
    assertThat(form.getFromCoordinateForm()).isEqualTo(coordinateFromForm);

    assertThat(form.getFromLocation()).isEqualTo(ident.getFromLocation());
    assertThat(form.getToLocation()).isEqualTo(ident.getToLocation());
    assertThat(form.getLength()).isEqualTo(ident.getLength());
    assertThat(form.getDefiningStructure()).isEqualTo(ident.getIsDefiningStructure());
  }


  @Test
  public void mapEntityToForm_definingStructure() {
    var form = new PipelineIdentForm();
    var ident = new PadPipelineIdent();

    ident.setLength(BigDecimal.ONE);
    ident.setDefiningStructure(true);
    form.setDataForm(new PipelineIdentDataForm());

    padPipelineIdentService.mapEntityToForm(ident, form);

    assertThat(form.getLengthOptional()).isEqualTo(ident.getLength());
    assertThat(form.getDefiningStructure()).isEqualTo(ident.getIsDefiningStructure());
  }

  @Test
  public void validateSection_valid() {
    padPipeline.setPipelineType(PipelineType.CABLE);
    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline)).thenReturn(List.of(ident));
    var valid = padPipelineIdentService.isSectionValid(padPipeline);
    assertThat(valid).isTrue();
  }

  @Test
  public void validateSection_invalid() {
    ident.setId(1);
    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline)).thenReturn(List.of(ident));
    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("", ""));
      return null;
    }).when(pipelineIdentFormValidator).validate(any(), any(), any());
    var valid = padPipelineIdentService.isSectionValid(padPipeline);
    assertThat(valid).isFalse();
  }

  @Test
  public void getIdentByIdentNumber_serviceInteraction() {
    when(padPipelineIdentRepository.getByPadPipelineAndAndIdentNo(padPipeline, 1)).thenReturn(Optional.of(ident));
    var result = padPipelineIdentService.getIdentByIdentNumber(padPipeline, 1);
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(ident);
  }

  @Test
  public void addIdentAtPosition_serviceInteractionAndCheckData() throws IllegalAccessException {

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
    form.setDefiningStructure(false);

    var dataForm = new PipelineIdentDataForm();
    dataForm.setExternalDiameter(BigDecimal.valueOf(12.1));
    dataForm.setInternalDiameter(BigDecimal.valueOf(12.1));
    dataForm.setWallThickness(BigDecimal.valueOf(12.1));
    dataForm.setMaop(BigDecimal.valueOf(12.1));
    dataForm.setProductsToBeConveyed("prod");
    dataForm.setComponentPartsDescription("component");
    dataForm.setInsulationCoatingType("ins");
    form.setDataForm(dataForm);

    padPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    padPipeline.setPipelineInBundle(false);

    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline)).thenReturn(List.of(ident));

    padPipelineIdentService.addIdentAtPosition(padPipeline, form, 1);

    assertThat(ident.getIdentNo()).isEqualTo(2);


    var saveCaptor = ArgumentCaptor.forClass(PadPipelineIdent.class);
    verify(padPipelineIdentRepository, times(1)).save(saveCaptor.capture());
    verify(padPipelineIdentRepository, times(1)).saveAll(List.of(ident));
    verify(padPipelineIdentDataService, times(1)).addIdentData(saveCaptor.getValue(), form.getDataForm());
    verify(padPipelineIdentDataService, never()).updateIdentData(any(), any());

    var newIdent = saveCaptor.getValue();

    assertThat(newIdent.getIdentNo()).isEqualTo(1);
    assertThat(newIdent.getPadPipeline()).isEqualTo(padPipeline);
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
    assertThat(newIdent.getIsDefiningStructure()).isEqualTo(form.getDefiningStructure());
  }

  @Test
  public void saveAll_serviceInteraction() {
    padPipelineIdentService.saveAll(List.of());
    verify(padPipelineIdentRepository, times(1)).saveAll(List.of());
  }

  @Test
  public void removeAllIdents_serviceInteraction() {
    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline))
        .thenReturn(List.of(ident));
    padPipelineIdentService.removeAllIdents(padPipeline);
    verify(padPipelineIdentDataService, times(1)).removeIdentDataForPipeline(padPipeline);
    verify(padPipelineIdentRepository, times(1)).deleteAll(List.of(ident));
  }

  @Test
  public void getAllIdents_serviceInteraction() {
    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline))
        .thenReturn(List.of(ident));
    var result = padPipelineIdentService.getAllIdents(padPipeline);
    assertThat(result).isEqualTo(List.of(ident));
  }


  @Test
  public void  getSummaryScreenValidationResult_identsStartAndToLocationDoesNotMatchHeader_invalid() {

    //create padPipeline and idents and overview
    var padPipeline = PadPipelineTaskListServiceTestUtil.createPadPipeline(detail, pipeline);
    padPipeline.setId(PAD_PIPELINE_1_ID);
    var fromIdent = PadPipelineTaskListServiceTestUtil.createIdentWithUnMatchingHeaderFromLocation(padPipeline);
    fromIdent.setId(1);
    fromIdent.setLength(padPipeline.getLength());
    var toIdent = PadPipelineTaskListServiceTestUtil.createIdentWithUnMatchingHeaderToLocation(padPipeline);
    toIdent.setId(2);
    toIdent.setLength(BigDecimal.ZERO);

    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline)).thenReturn(List.of(fromIdent, toIdent));

    //assert error messages match
    var validationResult = padPipelineIdentService.getSummaryScreenValidationResult(padPipeline);

    assertThat(validationResult.isSectionComplete()).isFalse();
    assertThat(validationResult.getErrorItems())
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(1, "ident-1" + fromIdent.getId(), " " + IDENT_FROM_LOCATION_MISMATCH_ERROR_MSG),
            tuple(2, "ident-1" + toIdent.getId(), " " + IDENT_TO_LOCATION_MISMATCH_ERROR_MSG));
    assertThat(validationResult.getIdPrefix()).isEqualTo("ident-");
    assertThat(validationResult.getInvalidObjectIds()).containsExactly(String.valueOf(fromIdent.getId()), String.valueOf(toIdent.getId()));
  }

  @Test
  public void  getSummaryScreenValidationResult_identStartLocationMatch_identToLocationDoesNotMatch_invalid() {

    //create padPipeline and idents and overview
    var padPipeline = PadPipelineTaskListServiceTestUtil.createPadPipeline(detail, pipeline);
    padPipeline.setId(PAD_PIPELINE_1_ID);
    var fromIdent = PadPipelineTaskListServiceTestUtil.createIdentWithMatchingHeaderFromLocation(padPipeline);
    fromIdent.setId(1);
    fromIdent.setLength(padPipeline.getLength());
    var toIdent = PadPipelineTaskListServiceTestUtil.createIdentWithUnMatchingHeaderToLocation(padPipeline);
    toIdent.setId(2);
    toIdent.setLength(BigDecimal.ZERO);

    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline)).thenReturn(List.of(fromIdent, toIdent));


    //assert error messages match
    var validationResult = padPipelineIdentService.getSummaryScreenValidationResult(padPipeline);

    assertThat(validationResult.isSectionComplete()).isFalse();
    assertThat(validationResult.getErrorItems())
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(1, "ident-1" + toIdent.getId(), " " + IDENT_TO_LOCATION_MISMATCH_ERROR_MSG));
    assertThat(validationResult.getIdPrefix()).isEqualTo("ident-");
    assertThat(validationResult.getInvalidObjectIds()).containsExactly(String.valueOf(toIdent.getId()));
  }

  @Test
  public void  getSummaryScreenValidationResult_identsStartAndEndLocationMatchHeader_noCoordinates_valid() {

    //create padPipeline and idents and overview
    var padPipeline = PadPipelineTaskListServiceTestUtil.createPadPipeline(detail, pipeline);
    padPipeline.setId(PAD_PIPELINE_1_ID);
    var fromIdent = PadPipelineTaskListServiceTestUtil.createIdentWithMatchingHeaderFromLocation(padPipeline);
    fromIdent.setFromCoordinates(CoordinatePairTestUtil.getNullCoordinate());
    fromIdent.setId(1);
    fromIdent.setLength(padPipeline.getLength());
    var toIdent = PadPipelineTaskListServiceTestUtil.createIdentWithMatchingHeaderToLocation(padPipeline);
    toIdent.setFromCoordinates(CoordinatePairTestUtil.getNullCoordinate());
    toIdent.setId(2);
    toIdent.setLength(BigDecimal.ZERO);

    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline)).thenReturn(List.of(fromIdent, toIdent));


    //assert error messages match
    var validationResult = padPipelineIdentService.getSummaryScreenValidationResult(padPipeline);

    assertThat(validationResult.isSectionComplete()).isTrue();
    assertThat(validationResult.getErrorItems()).isEmpty();
    assertThat(validationResult.getSectionIncompleteError()).isNull();
    assertThat(validationResult.getIdPrefix()).isEqualTo("ident-");
    assertThat(validationResult.getInvalidObjectIds()).isEmpty();
  }

  @Test
  public void  getSummaryScreenValidationResult_identsStartAndEndLocationMatchHeader_coordsExistAndDoesNotMatchHeader_invalid() {

    //create padPipeline and idents and overview
    var padPipeline = PadPipelineTaskListServiceTestUtil.createPadPipeline(detail, pipeline);
    padPipeline.setId(PAD_PIPELINE_1_ID);
    var fromIdent = PadPipelineTaskListServiceTestUtil.createIdentWithMatchingHeaderFromLocation(padPipeline);
    fromIdent.setFromCoordinates(PadPipelineTaskListServiceTestUtil.createCoordinatePairUnMatchingHeaderFromLocation(padPipeline));
    fromIdent.setId(1);
    fromIdent.setLength(padPipeline.getLength());
    var toIdent = PadPipelineTaskListServiceTestUtil.createIdentWithMatchingHeaderToLocation(padPipeline);
    toIdent.setToCoordinates(PadPipelineTaskListServiceTestUtil.createCoordinatePairUnMatchingHeaderToLocation(padPipeline));
    toIdent.setId(2);
    toIdent.setLength(BigDecimal.ZERO);

    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline)).thenReturn(List.of(fromIdent, toIdent));

    //assert error messages match
    var validationResult = padPipelineIdentService.getSummaryScreenValidationResult(padPipeline);

    assertThat(validationResult.isSectionComplete()).isFalse();
    assertThat(validationResult.getErrorItems())
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(1, "ident-1" + fromIdent.getId(), " " + IDENT_FROM_LOCATION_MISMATCH_ERROR_MSG),
            tuple(2, "ident-1" + toIdent.getId(), " " + IDENT_TO_LOCATION_MISMATCH_ERROR_MSG));
    assertThat(validationResult.getIdPrefix()).isEqualTo("ident-");
    assertThat(validationResult.getInvalidObjectIds()).containsExactly(String.valueOf(fromIdent.getId()), String.valueOf(toIdent.getId()));
  }

  @Test
  public void  getSummaryScreenValidationResult_identsStartAndEndLocationMatchHeader_fromCoordsExistAndDoesNotMatch_toExistsAndMatches_invalid() {

    //create padPipeline and idents and overview
    var padPipeline = PadPipelineTaskListServiceTestUtil.createPadPipeline(detail, pipeline);
    padPipeline.setId(PAD_PIPELINE_1_ID);
    var fromIdent = PadPipelineTaskListServiceTestUtil.createIdentWithMatchingHeaderFromLocation(padPipeline);
    fromIdent.setFromCoordinates(PadPipelineTaskListServiceTestUtil.createCoordinatePairUnMatchingHeaderFromLocation(padPipeline));
    fromIdent.setId(1);
    fromIdent.setLength(padPipeline.getLength());
    var toIdent = PadPipelineTaskListServiceTestUtil.createIdentWithMatchingHeaderToLocation(padPipeline);
    toIdent.setToCoordinates(padPipeline.getToCoordinates());
    toIdent.setId(2);
    toIdent.setLength(BigDecimal.ZERO);

    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline)).thenReturn(List.of(fromIdent, toIdent));

    //assert error messages match
    var validationResult = padPipelineIdentService.getSummaryScreenValidationResult(padPipeline);

    assertThat(validationResult.isSectionComplete()).isFalse();
    assertThat(validationResult.getErrorItems())
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(1, "ident-1" + fromIdent.getId(), " " + IDENT_FROM_LOCATION_MISMATCH_ERROR_MSG));
    assertThat(validationResult.getIdPrefix()).isEqualTo("ident-");
    assertThat(validationResult.getInvalidObjectIds()).containsExactly(String.valueOf(fromIdent.getId()));
  }

  @Test
  public void  getSummaryScreenValidationResult_identsStartAndEndLocationMatchHeader_fromCoordsExistAndMatches_toExistsAndDoesNotMatch_invalid() {

    //create padPipeline and idents and overview
    var padPipeline = PadPipelineTaskListServiceTestUtil.createPadPipeline(detail, pipeline);
    padPipeline.setId(PAD_PIPELINE_1_ID);
    var fromIdent = PadPipelineTaskListServiceTestUtil.createIdentWithMatchingHeaderFromLocation(padPipeline);
    fromIdent.setLength(padPipeline.getLength());
    fromIdent.setFromCoordinates(padPipeline.getFromCoordinates());
    fromIdent.setId(1);
    var toIdent = PadPipelineTaskListServiceTestUtil.createIdentWithMatchingHeaderToLocation(padPipeline);
    toIdent.setToCoordinates(PadPipelineTaskListServiceTestUtil.createCoordinatePairUnMatchingHeaderToLocation(padPipeline));
    toIdent.setId(2);
    toIdent.setLength(BigDecimal.ZERO);

    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline)).thenReturn(List.of(fromIdent, toIdent));

    //assert error messages match
    var validationResult = padPipelineIdentService.getSummaryScreenValidationResult(padPipeline);

    assertThat(validationResult.isSectionComplete()).isFalse();
    assertThat(validationResult.getErrorItems())
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(1, "ident-1" + toIdent.getId(), " " + IDENT_TO_LOCATION_MISMATCH_ERROR_MSG));
    assertThat(validationResult.getIdPrefix()).isEqualTo("ident-");
    assertThat(validationResult.getInvalidObjectIds()).containsExactly(String.valueOf(toIdent.getId()));
  }

  @Test
  public void  getSummaryScreenValidationResult_identsStartAndEndLocationMatchHeader_coordinatesExistAndMatchHeader_valid() {

    //create padPipeline and idents and overview
    var padPipeline = PadPipelineTaskListServiceTestUtil.createPadPipeline(detail, pipeline);
    padPipeline.setId(PAD_PIPELINE_1_ID);
    var fromIdent = PadPipelineTaskListServiceTestUtil.createIdentWithMatchingHeaderFromLocation(padPipeline);
    fromIdent.setLength(padPipeline.getLength());
    fromIdent.setFromCoordinates(padPipeline.getFromCoordinates());
    fromIdent.setId(1);
    var toIdent = PadPipelineTaskListServiceTestUtil.createIdentWithMatchingHeaderToLocation(padPipeline);
    toIdent.setToCoordinates(padPipeline.getToCoordinates());
    toIdent.setId(2);
    toIdent.setLength(BigDecimal.ZERO);

    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline)).thenReturn(List.of(fromIdent, toIdent));

    //assert error messages match
    var validationResult = padPipelineIdentService.getSummaryScreenValidationResult(padPipeline);

    assertThat(validationResult.isSectionComplete()).isTrue();
    assertThat(validationResult.getErrorItems()).isEmpty();
    assertThat(validationResult.getSectionIncompleteError()).isNull();
    assertThat(validationResult.getIdPrefix()).isEqualTo("ident-");
    assertThat(validationResult.getInvalidObjectIds()).isEmpty();
  }

  @Test
  public void getSummaryScreenValidationResult_identsValidAndLengthMatchesPadPipeline_sectionComplete() {
    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline))
        .thenReturn(List.of(ident));
    var result = padPipelineIdentService.getSummaryScreenValidationResult(padPipeline);
    assertThat(result.getErrorItems()).isEmpty();
    assertThat(result.isSectionComplete()).isTrue();
  }

  @Test
  public void getSummaryScreenValidationResult_identLengthDoesNotMatchPadPipelineLength_sectionIncomplete() {
    padPipeline.setLength(BigDecimal.valueOf(51));
    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline))
        .thenReturn(List.of(ident));
    var result = padPipelineIdentService.getSummaryScreenValidationResult(padPipeline);
    assertThat(result.isSectionComplete()).isFalse();
  }

  @Test
  public void getSummaryScreenValidationResult_identInvalid_sectionIncomplete() {
    ident.setId(1);
    ident.setIdentNo(1);
    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("", ""));
      return null;
    }).when(pipelineIdentFormValidator).validate(any(), any(), any());
    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline))
        .thenReturn(List.of(ident));
    var result = padPipelineIdentService.getSummaryScreenValidationResult(padPipeline);
    assertThat(result.isSectionComplete()).isFalse();
  }

  @Test
  public void isIdentValid_true() {
    var result = padPipelineIdentService.isIdentValid(padPipeline, ident);
    assertThat(result).isTrue();
  }

  @Test
  public void isIdentValid_failedValidation() {
    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("", ""));
      return null;
    }).when(pipelineIdentFormValidator).validate(any(), any(), any());
    var result = padPipelineIdentService.isIdentValid(padPipeline, ident);
    assertThat(result).isFalse();
  }

  @Test
  public void isSectionValid_true() {
    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline))
        .thenReturn(List.of(ident));
    var result = padPipelineIdentService.isSectionValid(padPipeline);
    assertThat(result).isTrue();
  }

  @Test
  public void isSectionValid_noIdents() {
    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline))
        .thenReturn(List.of());
    var result = padPipelineIdentService.isSectionValid(padPipeline);
    assertThat(result).isFalse();
  }

  @Test
  public void isSectionValid_invalidIdent() {
    ident.setId(1);
    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline))
        .thenReturn(List.of(ident));
    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("", ""));
      return null;
    }).when(pipelineIdentFormValidator).validate(any(), any(), any());
    var result = padPipelineIdentService.isSectionValid(padPipeline);
    assertThat(result).isFalse();
  }

  @Test
  public void getApplicationIdentViewsForPipelines_whenOnePipelineIdProvided_andIdentFound() {
    when(padPipelineIdentRepository.getAllByPadPipeline_Pipeline_IdIn(Set.of(PIPELINE_ID.asInt())))
        .thenReturn(List.of(ident));

    when(padPipelineIdentDataService.getAllPadPipelineIdentDataForIdents(List.of(ident)))
        .thenReturn(List.of(identData));

    var result = padPipelineIdentService.getApplicationIdentViewsForPipelines(Set.of(PIPELINE_ID));

    assertThat(result).containsOnlyKeys(PIPELINE_ID);
    assertThat(result.get(PIPELINE_ID)).hasSize(1);
    assertIdentViewMatchesIdent(result.get(PIPELINE_ID).get(0), ident, identData);
  }

  @Test
  public void getApplicationIdentViewsForPipelines_whenMutliplePipelineIdsProvided_andIdentsFound() {
    var pipelineId2 = new PipelineId(2);
    var pipeline2 = new Pipeline();
    pipeline2.setId(pipelineId2.asInt());
    var padPipeline2 = new PadPipeline();
    padPipeline2.setPipeline(pipeline2);
    padPipeline2.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    var pipeline2Ident1 = makeIdent(1, "from1", "to1");
    pipeline2Ident1.setPadPipeline(padPipeline2);
    var pipeline2Ident2 = makeIdent(2, "from2", "to2");
    pipeline2Ident2.setPadPipeline(padPipeline2);

    var pipeline2Ident1Data = makeIdentData(pipeline2Ident1);
    var pipeline2Ident2Data = makeIdentData(pipeline2Ident2);


    var foundIdentList = List.of(ident, pipeline2Ident1, pipeline2Ident2);
    when(padPipelineIdentRepository.getAllByPadPipeline_Pipeline_IdIn(
        Set.of(PIPELINE_ID.asInt(), pipelineId2.asInt())))
        .thenReturn(foundIdentList);

    when(padPipelineIdentDataService.getAllPadPipelineIdentDataForIdents(foundIdentList))
        .thenReturn(List.of(identData, pipeline2Ident1Data, pipeline2Ident2Data));

    var result = padPipelineIdentService.getApplicationIdentViewsForPipelines(Set.of(PIPELINE_ID, pipelineId2));

    assertThat(result).containsOnlyKeys(PIPELINE_ID, pipelineId2);
    assertThat(result.get(PIPELINE_ID)).hasSize(1);
    assertIdentViewMatchesIdent(result.get(PIPELINE_ID).get(0), ident, identData);

    assertThat(result.get(pipelineId2)).hasSize(2);
    assertIdentViewMatchesIdent(result.get(pipelineId2).get(0), pipeline2Ident1, pipeline2Ident1Data);
    assertIdentViewMatchesIdent(result.get(pipelineId2).get(1), pipeline2Ident2, pipeline2Ident2Data);

  }

  @Test
  public void getApplicationIdentViewsForPipelines_whenMutliplePipelineIdsProvided_andNoIdentsFound() {

    var result = padPipelineIdentService.getApplicationIdentViewsForPipelines(
        Set.of(new PipelineId(99), new PipelineId(100)));

    assertThat(result).isEmpty();

  }


}
