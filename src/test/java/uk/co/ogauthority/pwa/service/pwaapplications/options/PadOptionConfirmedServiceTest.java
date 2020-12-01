package uk.co.ogauthority.pwa.service.pwaapplications.options;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.options.PadConfirmationOfOption;
import uk.co.ogauthority.pwa.repository.pwaapplications.options.PadConfirmationOfOptionRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadOptionConfirmedServiceTest {

  @Mock
  private PadConfirmationOfOptionRepository padConfirmationOfOptionRepository;

  private PadOptionConfirmedService padOptionConfirmedService;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() throws Exception {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    padOptionConfirmedService = new PadOptionConfirmedService(padConfirmationOfOptionRepository);
  }

  @Test
  public void approvedOptionComplete_whenNotOptionsVariation() {
    var notOptions = EnumSet.allOf(PwaApplicationType.class);
    notOptions.remove(PwaApplicationType.OPTIONS_VARIATION);

    for (PwaApplicationType type : notOptions) {
      pwaApplicationDetail.getPwaApplication().setApplicationType(type);

      assertThat(padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail)).isFalse();
      verifyNoInteractions(padConfirmationOfOptionRepository);

    }
  }

  @Test
  public void approvedOptionComplete_whenOptionsVariation_notCompletedApprovedOption() {

    assertThat(padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail)).isFalse();

    verify(padConfirmationOfOptionRepository, times(1))
        .existsByPwaApplicationDetailAndConfirmedOptionType(pwaApplicationDetail,
            ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS);
  }

  @Test
  public void approvedOptionComplete_whenOptionsVariation_CompletedApprovedOption() {

    when(padConfirmationOfOptionRepository.existsByPwaApplicationDetailAndConfirmedOptionType(any(), any()))
        .thenReturn(true);

    assertThat(padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail)).isTrue();

    verify(padConfirmationOfOptionRepository, times(1))
        .existsByPwaApplicationDetailAndConfirmedOptionType(pwaApplicationDetail,
            ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS);
  }

  @Test
  public void optionConfirmationExists_notOptions(){
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    assertThat(padOptionConfirmedService.optionConfirmationExists(detail)).isFalse();

    verifyNoInteractions(padConfirmationOfOptionRepository);
  }

  @Test
  public void optionConfirmationExists_options_confirmationExists(){

    var padConfirmation = new PadConfirmationOfOption(pwaApplicationDetail);

    when(padConfirmationOfOptionRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(Optional.of(padConfirmation));

    assertThat(padOptionConfirmedService.optionConfirmationExists(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void optionConfirmationExists_options_confirmationNotExists(){

     when(padConfirmationOfOptionRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(Optional.empty());

    assertThat(padOptionConfirmedService.optionConfirmationExists(pwaApplicationDetail)).isFalse();
  }
}