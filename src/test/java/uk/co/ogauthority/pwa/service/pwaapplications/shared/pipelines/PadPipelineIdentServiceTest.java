package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineIdentRepository;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
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

    newIdent.prePersist();

    assertThat(newIdent.getPadPipeline()).isEqualTo(pipeline);
    assertThat(newIdent.getFromLocation()).isEqualTo(form.getFromLocation());
    assertThat(FieldUtils.getFieldValue(newIdent, "fromLatitudeDegrees")).isEqualTo(form.getFromCoordinateForm().getLatitudeDegrees());
    assertThat(FieldUtils.getFieldValue(newIdent, "fromLatitudeMinutes")).isEqualTo(form.getFromCoordinateForm().getLatitudeMinutes());
    assertThat(FieldUtils.getFieldValue(newIdent, "fromLatitudeSeconds")).isEqualTo(form.getFromCoordinateForm().getLatitudeSeconds());
    assertThat(FieldUtils.getFieldValue(newIdent, "fromLatitudeDirection")).isEqualTo(form.getFromCoordinateForm().getLatitudeDirection());
    assertThat(FieldUtils.getFieldValue(newIdent, "fromLongitudeDegrees")).isEqualTo(form.getFromCoordinateForm().getLongitudeDegrees());
    assertThat(FieldUtils.getFieldValue(newIdent, "fromLongitudeMinutes")).isEqualTo(form.getFromCoordinateForm().getLongitudeMinutes());
    assertThat(FieldUtils.getFieldValue(newIdent, "fromLongitudeSeconds")).isEqualTo(form.getFromCoordinateForm().getLongitudeSeconds());
    assertThat(FieldUtils.getFieldValue(newIdent, "fromLongitudeDirection")).isEqualTo(form.getFromCoordinateForm().getLongitudeDirection());

    assertThat(FieldUtils.getFieldValue(newIdent, "toLatitudeDegrees")).isEqualTo(form.getToCoordinateForm().getLatitudeDegrees());
    assertThat(FieldUtils.getFieldValue(newIdent, "toLatitudeMinutes")).isEqualTo(form.getToCoordinateForm().getLatitudeMinutes());
    assertThat(FieldUtils.getFieldValue(newIdent, "toLatitudeSeconds")).isEqualTo(form.getToCoordinateForm().getLatitudeSeconds());
    assertThat(FieldUtils.getFieldValue(newIdent, "toLatitudeDirection")).isEqualTo(form.getToCoordinateForm().getLatitudeDirection());
    assertThat(FieldUtils.getFieldValue(newIdent, "toLongitudeDegrees")).isEqualTo(form.getToCoordinateForm().getLongitudeDegrees());
    assertThat(FieldUtils.getFieldValue(newIdent, "toLongitudeMinutes")).isEqualTo(form.getToCoordinateForm().getLongitudeMinutes());
    assertThat(FieldUtils.getFieldValue(newIdent, "toLongitudeSeconds")).isEqualTo(form.getToCoordinateForm().getLongitudeSeconds());
    assertThat(FieldUtils.getFieldValue(newIdent, "toLongitudeDirection")).isEqualTo(form.getToCoordinateForm().getLongitudeDirection());

    assertThat(newIdent.getLength()).isEqualTo(form.getLength());

  }

}
