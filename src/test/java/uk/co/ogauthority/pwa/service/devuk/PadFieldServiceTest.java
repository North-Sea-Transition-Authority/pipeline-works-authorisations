package uk.co.ogauthority.pwa.service.devuk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.entity.devuk.PadField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.devuk.PadFieldRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@RunWith(MockitoJUnitRunner.class)
public class PadFieldServiceTest {

  @Mock
  private PadFieldRepository padFieldRepository;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  private PadFieldService padFieldService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    padFieldService = new PadFieldService(padFieldRepository,
        pwaApplicationDetailService);
    pwaApplicationDetail = new PwaApplicationDetail();
  }

  @Test
  public void getActiveFieldsForApplicationDetail() {
    var pwaField = new PadField();
    when(padFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(pwaField));
    assertThat(padFieldService.getActiveFieldsForApplicationDetail(pwaApplicationDetail)).containsExactly(pwaField);
  }

  @Test
  public void addField() {
    var devukField = new DevukField();
    var pwaField = padFieldService.addField(pwaApplicationDetail, devukField);
    assertThat(pwaField.getDevukField()).isEqualTo(devukField);
    verify(padFieldRepository, times(1)).save(pwaField);
  }

  @Test
  public void endAllFields() {
    var devukFieldA = new DevukField(1, "field", 500);
    var devukFieldB = new DevukField(2, "field", 500);

    var pwaFieldA = new PadField();
    pwaFieldA.setId(1);
    pwaFieldA.setDevukField(devukFieldA);
    var pwaFieldB = new PadField();
    pwaFieldB.setId(2);
    pwaFieldB.setDevukField(devukFieldB);

    when(padFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(pwaFieldA, pwaFieldB));

    when(padFieldRepository.findByPwaApplicationDetailAndDevukField(pwaApplicationDetail, devukFieldA))
        .thenReturn(pwaFieldA);
    when(padFieldRepository.findByPwaApplicationDetailAndDevukField(pwaApplicationDetail, devukFieldB))
        .thenReturn(pwaFieldB);

    padFieldService.removeAllFields(pwaApplicationDetail);
    verify(padFieldRepository, times(1)).delete(pwaFieldA);
    verify(padFieldRepository, times(1)).delete(pwaFieldB);
  }

  @Test
  public void setFields() {
    var devukFieldA = new DevukField(1, "Field A", 500);
    var devukFieldB = new DevukField(2, "Field B", 700);
    var pwaFieldA = new PadField();
    var pwaFieldB = new PadField();
    pwaFieldA.setDevukField(devukFieldA);
    pwaFieldB.setDevukField(devukFieldB);

    when(padFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(pwaFieldA, pwaFieldB));

    when(padFieldRepository.findByPwaApplicationDetailAndDevukField(pwaApplicationDetail, devukFieldA)).thenReturn(pwaFieldA);
    when(padFieldRepository.findByPwaApplicationDetailAndDevukField(pwaApplicationDetail, devukFieldB)).thenReturn(pwaFieldB);

    padFieldService.setFields(pwaApplicationDetail, List.of(devukFieldA, devukFieldB));
  }

  @Test
  public void removeField() {
    var devukField = new DevukField();
    var pwaField = new PadField();
    pwaField.setDevukField(devukField);
    when(padFieldRepository.findByPwaApplicationDetailAndDevukField(pwaApplicationDetail, devukField))
        .thenReturn(pwaField);
    var removed = padFieldService.removeField(pwaApplicationDetail, devukField);
    verify(padFieldRepository, times(1)).delete(pwaField);
    assertThat(removed).isEqualTo(pwaField);
  }
}