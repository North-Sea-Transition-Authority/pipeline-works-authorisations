package uk.co.ogauthority.pwa.service.fields;

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
import uk.co.ogauthority.pwa.model.entity.fields.DevukField;
import uk.co.ogauthority.pwa.model.entity.fields.PwaApplicationDetailField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.fields.PwaFieldRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationFieldServiceTest {

  @Mock
  private PwaFieldRepository pwaFieldRepository;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  private PwaApplicationFieldService pwaApplicationFieldService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    pwaApplicationFieldService = new PwaApplicationFieldService(pwaFieldRepository,
        pwaApplicationDetailService);
    pwaApplicationDetail = new PwaApplicationDetail();
  }

  @Test
  public void getActiveFieldsForApplicationDetail() {
    var pwaField = new PwaApplicationDetailField();
    when(pwaFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(pwaField));
    assertThat(pwaApplicationFieldService.getActiveFieldsForApplicationDetail(pwaApplicationDetail)).containsExactly(pwaField);
  }

  @Test
  public void addField() {
    var devukField = new DevukField();
    var pwaField = pwaApplicationFieldService.addField(pwaApplicationDetail, devukField);
    assertThat(pwaField.getDevukField()).isEqualTo(devukField);
    verify(pwaFieldRepository, times(1)).save(pwaField);
  }

  @Test
  public void endAllFields() {
    var devukFieldA = new DevukField(1, "field", 500);
    var devukFieldB = new DevukField(2, "field", 500);

    var pwaFieldA = new PwaApplicationDetailField();
    pwaFieldA.setId(1);
    pwaFieldA.setDevukField(devukFieldA);
    var pwaFieldB = new PwaApplicationDetailField();
    pwaFieldB.setId(2);
    pwaFieldB.setDevukField(devukFieldB);

    when(pwaFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(pwaFieldA, pwaFieldB));

    when(pwaFieldRepository.findByPwaApplicationDetailAndDevukField(pwaApplicationDetail, devukFieldA))
        .thenReturn(pwaFieldA);
    when(pwaFieldRepository.findByPwaApplicationDetailAndDevukField(pwaApplicationDetail, devukFieldB))
        .thenReturn(pwaFieldB);

    pwaApplicationFieldService.removeAllFields(pwaApplicationDetail);
    verify(pwaFieldRepository, times(1)).delete(pwaFieldA);
    verify(pwaFieldRepository, times(1)).delete(pwaFieldB);
  }

  @Test
  public void setFields() {
    var devukFieldA = new DevukField(1, "Field A", 500);
    var devukFieldB = new DevukField(2, "Field B", 700);
    var pwaFieldA = new PwaApplicationDetailField();
    var pwaFieldB = new PwaApplicationDetailField();
    pwaFieldA.setDevukField(devukFieldA);
    pwaFieldB.setDevukField(devukFieldB);

    when(pwaFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(pwaFieldA, pwaFieldB));

    when(pwaFieldRepository.findByPwaApplicationDetailAndDevukField(pwaApplicationDetail, devukFieldA)).thenReturn(pwaFieldA);
    when(pwaFieldRepository.findByPwaApplicationDetailAndDevukField(pwaApplicationDetail, devukFieldB)).thenReturn(pwaFieldB);

    pwaApplicationFieldService.setFields(pwaApplicationDetail, List.of(devukFieldA, devukFieldB));
  }

  @Test
  public void removeField() {
    var devukField = new DevukField();
    var pwaField = new PwaApplicationDetailField();
    pwaField.setDevukField(devukField);
    when(pwaFieldRepository.findByPwaApplicationDetailAndDevukField(pwaApplicationDetail, devukField))
        .thenReturn(pwaField);
    var removed = pwaApplicationFieldService.removeField(pwaApplicationDetail, devukField);
    verify(pwaFieldRepository, times(1)).delete(pwaField);
    assertThat(removed).isEqualTo(pwaField);
  }
}