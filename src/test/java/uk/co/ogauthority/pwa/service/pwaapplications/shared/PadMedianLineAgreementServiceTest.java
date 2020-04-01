package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadMedianLineAgreement;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.MedianLineAgreementsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadMedianLineAgreementRepository;

@RunWith(MockitoJUnitRunner.class)
public class PadMedianLineAgreementServiceTest {

  @Mock
  private PadMedianLineAgreementRepository padMedianLineAgreementRepository;

  private PadMedianLineAgreementService padMedianLineAgreementService;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() throws Exception {
    padMedianLineAgreementService = new PadMedianLineAgreementService(padMedianLineAgreementRepository);
    pwaApplicationDetail = new PwaApplicationDetail();
  }

  @Test
  public void getMedianLineAgreementForDraft_WithExisting() {
    var agreement = new PadMedianLineAgreement();
    when(padMedianLineAgreementRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(agreement));
    var result = padMedianLineAgreementService.getMedianLineAgreementForDraft(pwaApplicationDetail);
    assertThat(result).isEqualTo(agreement);
  }

  @Test
  public void getMedianLineAgreementForDraft_NoneExisting() {
    when(padMedianLineAgreementRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.empty());
    var result = padMedianLineAgreementService.getMedianLineAgreementForDraft(pwaApplicationDetail);
    assertThat(result.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
  }

  @Test
  public void save() {
    var agreement = new PadMedianLineAgreement();
    padMedianLineAgreementService.save(agreement);
    verify(padMedianLineAgreementRepository, times(1)).save(agreement);
  }

  @Test
  public void mapEntityToForm_Nulls() {
    var form = new MedianLineAgreementsForm();
    var entity = new PadMedianLineAgreement();
    padMedianLineAgreementService.mapEntityToForm(entity, form);
    assertThat(form.getNegotiatorNameIfOngoing()).isNull();
    assertThat(form.getNegotiatorEmailIfOngoing()).isNull();
    assertThat(form.getNegotiatorNameIfCompleted()).isNull();
    assertThat(form.getNegotiatorEmailIfCompleted()).isNull();
    assertThat(form.getAgreementStatus()).isNull();
  }

  @Test
  public void mapEntityToForm_NotCrossed() {
    var form = new MedianLineAgreementsForm();
    var entity = new PadMedianLineAgreement();
    entity.setAgreementStatus(MedianLineStatus.NOT_CROSSED);
    entity.setNegotiatorName("NOT CROSSED");
    entity.setNegotiatorEmail("not@crossed");
    padMedianLineAgreementService.mapEntityToForm(entity, form);
    assertThat(form.getNegotiatorNameIfOngoing()).isNull();
    assertThat(form.getNegotiatorEmailIfOngoing()).isNull();
    assertThat(form.getNegotiatorNameIfCompleted()).isNull();
    assertThat(form.getNegotiatorEmailIfCompleted()).isNull();
    assertThat(form.getAgreementStatus()).isEqualTo(entity.getAgreementStatus());
  }

  @Test
  public void mapEntityToForm_Ongoing() {
    var form = new MedianLineAgreementsForm();
    var entity = new PadMedianLineAgreement();
    entity.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_ONGOING);
    entity.setNegotiatorName("ONGOING");
    entity.setNegotiatorEmail("on@going");
    padMedianLineAgreementService.mapEntityToForm(entity, form);
    assertThat(form.getNegotiatorNameIfOngoing()).isEqualTo(entity.getNegotiatorName());
    assertThat(form.getNegotiatorEmailIfOngoing()).isEqualTo(entity.getNegotiatorEmail());
    assertThat(form.getNegotiatorNameIfCompleted()).isNull();
    assertThat(form.getNegotiatorEmailIfCompleted()).isNull();
    assertThat(form.getAgreementStatus()).isEqualTo(entity.getAgreementStatus());
  }

  @Test
  public void mapEntityToForm_Completed() {
    var form = new MedianLineAgreementsForm();
    var entity = new PadMedianLineAgreement();
    entity.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    entity.setNegotiatorName("COMPLETED");
    entity.setNegotiatorEmail("completed@test");
    padMedianLineAgreementService.mapEntityToForm(entity, form);
    assertThat(form.getNegotiatorNameIfOngoing()).isNull();
    assertThat(form.getNegotiatorEmailIfOngoing()).isNull();
    assertThat(form.getNegotiatorNameIfCompleted()).isEqualTo(entity.getNegotiatorName());
    assertThat(form.getNegotiatorEmailIfCompleted()).isEqualTo(entity.getNegotiatorEmail());
    assertThat(form.getAgreementStatus()).isEqualTo(entity.getAgreementStatus());
  }

  @Test
  public void saveEntityUsingForm_Nulls() {
    var form = new MedianLineAgreementsForm();
    var entity = new PadMedianLineAgreement();
    padMedianLineAgreementService.saveEntityUsingForm(entity, form);
    assertThat(entity.getAgreementStatus()).isNull();
    assertThat(entity.getNegotiatorName()).isNull();
    assertThat(entity.getNegotiatorEmail()).isNull();
  }

  @Test
  public void saveEntityUsingForm_NotCrossed() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NOT_CROSSED);
    form.setNegotiatorNameIfOngoing("Ongoing name");
    form.setNegotiatorEmailIfOngoing("Ongoing email");
    form.setNegotiatorNameIfCompleted("Completed name");
    form.setNegotiatorEmailIfCompleted("Completed email");
    var entity = new PadMedianLineAgreement();
    padMedianLineAgreementService.saveEntityUsingForm(entity, form);
    assertThat(entity.getAgreementStatus()).isEqualTo(form.getAgreementStatus());
    assertThat(entity.getNegotiatorName()).isNull();
    assertThat(entity.getNegotiatorEmail()).isNull();
  }

  @Test
  public void saveEntityUsingForm_Ongoing() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_ONGOING);
    form.setNegotiatorNameIfOngoing("Ongoing name");
    form.setNegotiatorEmailIfOngoing("Ongoing email");
    form.setNegotiatorNameIfCompleted("Completed name");
    form.setNegotiatorEmailIfCompleted("Completed email");
    var entity = new PadMedianLineAgreement();
    padMedianLineAgreementService.saveEntityUsingForm(entity, form);
    assertThat(entity.getAgreementStatus()).isEqualTo(form.getAgreementStatus());
    assertThat(entity.getNegotiatorName()).isEqualTo(form.getNegotiatorNameIfOngoing());
    assertThat(entity.getNegotiatorEmail()).isEqualTo(form.getNegotiatorEmailIfOngoing());
  }

  @Test
  public void saveEntityUsingForm_Completed() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    form.setNegotiatorNameIfOngoing("Ongoing name");
    form.setNegotiatorEmailIfOngoing("Ongoing email");
    form.setNegotiatorNameIfCompleted("Completed name");
    form.setNegotiatorEmailIfCompleted("Completed email");
    var entity = new PadMedianLineAgreement();
    padMedianLineAgreementService.saveEntityUsingForm(entity, form);
    assertThat(entity.getAgreementStatus()).isEqualTo(form.getAgreementStatus());
    assertThat(entity.getNegotiatorName()).isEqualTo(form.getNegotiatorNameIfCompleted());
    assertThat(entity.getNegotiatorEmail()).isEqualTo(form.getNegotiatorEmailIfCompleted());
  }
}