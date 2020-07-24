package uk.co.ogauthority.pwa.service.devuk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.entity.devuk.PadField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.fields.PwaFieldForm;
import uk.co.ogauthority.pwa.repository.devuk.PadFieldRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;

@RunWith(MockitoJUnitRunner.class)
public class PadFieldServiceTest {

  @Mock
  private PadFieldRepository padFieldRepository;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private PadProjectInformationService padProjectInformationService;

  @Mock
  private DevukFieldService devukFieldService;

  @Captor
  private ArgumentCaptor<List<PadField>> padFieldsArgumentCaptor;

  private PadFieldService padFieldService;
  private PwaApplicationDetail pwaApplicationDetail;

  private PadField existingField;
  private final int DEVUK_FIELD_ID = 1;
  private DevukField devukField;

  @Before
  public void setUp() {

    pwaApplicationDetail = new PwaApplicationDetail();

    devukField = new DevukField();

    existingField = new PadField();
    existingField.setPwaApplicationDetail(pwaApplicationDetail);
    existingField.setDevukField(devukField);

    when(devukFieldService.findById(DEVUK_FIELD_ID)).thenReturn(devukField);

    when(padFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(existingField));

    padFieldService = new PadFieldService(padFieldRepository, pwaApplicationDetailService, padProjectInformationService, devukFieldService);

  }

  @Test
  public void getActiveFieldsForApplicationDetail() {
    var pwaField = new PadField();
    when(padFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(pwaField));
    assertThat(padFieldService.getActiveFieldsForApplicationDetail(pwaApplicationDetail)).containsExactly(pwaField);
  }

  @Test
  public void updateFieldInformation_nothingEntered() {

    padFieldService.updateFieldInformation(pwaApplicationDetail, new PwaFieldForm());

    verifyNoInteractions(padFieldRepository, pwaApplicationDetailService, padProjectInformationService, devukFieldService);

  }

  @Test
  public void updateFieldInformation_linkedToField_noField() {

    var form = new PwaFieldForm();
    form.setLinkedToField(true);

    padFieldService.updateFieldInformation(pwaApplicationDetail, form);

    verify(pwaApplicationDetailService, times(1)).setLinkedToFields(pwaApplicationDetail, true);

    verify(padFieldRepository, times(1)).getAllByPwaApplicationDetail(pwaApplicationDetail);
    verify(padFieldRepository, times(1)).deleteAll(eq(List.of(existingField)));

    verifyNoMoreInteractions(padFieldRepository, pwaApplicationDetailService, padProjectInformationService, devukFieldService);

  }

  @Test
  public void updateFieldInformation_linkedToField_fieldSelected() {

    var form = new PwaFieldForm();
    form.setLinkedToField(true);
    form.setFieldId(DEVUK_FIELD_ID);

    padFieldService.updateFieldInformation(pwaApplicationDetail, form);

    verify(pwaApplicationDetailService, times(1)).setLinkedToFields(pwaApplicationDetail, true);

    verify(padFieldRepository, times(1)).getAllByPwaApplicationDetail(pwaApplicationDetail);
    verify(padFieldRepository, times(1)).deleteAll(eq(List.of(existingField)));

    verify(padFieldRepository, times(1)).saveAll(padFieldsArgumentCaptor.capture());

    var newFields = padFieldsArgumentCaptor.getValue();

    assertThat(newFields.size()).isEqualTo(1);

    var newField = newFields.get(0);

    assertThat(newField.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(newField.getDevukField()).isEqualTo(devukField);
    assertThat(newField.getFieldName()).isNull();

    verifyNoInteractions(padProjectInformationService);

  }

  @Test
  public void updateFieldInformation_notLinkedToField_noDescription() {

    var form = new PwaFieldForm();
    form.setLinkedToField(false);

    padFieldService.updateFieldInformation(pwaApplicationDetail, form);

    verify(pwaApplicationDetailService, times(1)).setLinkedToFields(pwaApplicationDetail, false);

    verify(padFieldRepository, times(1)).getAllByPwaApplicationDetail(pwaApplicationDetail);
    verify(padFieldRepository, times(1)).deleteAll(eq(List.of(existingField)));

    verify(pwaApplicationDetailService, times(1)).setNotLinkedFieldDescription(pwaApplicationDetail, null);

    verify(padProjectInformationService, times(1)).removeFdpQuestionData(pwaApplicationDetail);

    verifyNoMoreInteractions(devukFieldService, padFieldRepository);

  }

  @Test
  public void updateFieldInformation_notLinkedToField_descriptionEntered() {

    var form = new PwaFieldForm();
    form.setLinkedToField(false);
    form.setNoLinkedFieldDescription("description");

    padFieldService.updateFieldInformation(pwaApplicationDetail, form);

    verify(pwaApplicationDetailService, times(1)).setLinkedToFields(pwaApplicationDetail, false);

    verify(padFieldRepository, times(1)).getAllByPwaApplicationDetail(pwaApplicationDetail);
    verify(padFieldRepository, times(1)).deleteAll(eq(List.of(existingField)));

    verify(pwaApplicationDetailService, times(1)).setNotLinkedFieldDescription(pwaApplicationDetail, "description");

    verify(padProjectInformationService, times(1)).removeFdpQuestionData(pwaApplicationDetail);

    verifyNoMoreInteractions(devukFieldService, padFieldRepository);

  }

}