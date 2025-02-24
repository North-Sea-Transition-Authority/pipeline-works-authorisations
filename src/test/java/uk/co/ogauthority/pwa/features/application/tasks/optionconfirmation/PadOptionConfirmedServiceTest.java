package uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PadOptionConfirmedServiceTest {

  @Mock
  private PadConfirmationOfOptionRepository padConfirmationOfOptionRepository;

  private PadOptionConfirmedService padOptionConfirmedService;

  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() throws Exception {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    padOptionConfirmedService = new PadOptionConfirmedService(padConfirmationOfOptionRepository);
  }

  @Test
  void approvedOptionComplete_whenNotOptionsVariation() {
    var notOptions = EnumSet.allOf(PwaApplicationType.class);
    notOptions.remove(PwaApplicationType.OPTIONS_VARIATION);

    for (PwaApplicationType type : notOptions) {
      pwaApplicationDetail.getPwaApplication().setApplicationType(type);

      assertThat(padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail)).isFalse();
      verifyNoInteractions(padConfirmationOfOptionRepository);

    }
  }

  @Test
  void approvedOptionComplete_whenOptionsVariation_notCompletedApprovedOption() {

    assertThat(padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail)).isFalse();

    verify(padConfirmationOfOptionRepository, times(1))
        .existsByPwaApplicationDetailAndConfirmedOptionType(pwaApplicationDetail,
            ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS);
  }

  @Test
  void approvedOptionComplete_whenOptionsVariation_CompletedApprovedOption() {

    when(padConfirmationOfOptionRepository.existsByPwaApplicationDetailAndConfirmedOptionType(any(), any()))
        .thenReturn(true);

    assertThat(padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail)).isTrue();

    verify(padConfirmationOfOptionRepository, times(1))
        .existsByPwaApplicationDetailAndConfirmedOptionType(pwaApplicationDetail,
            ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS);
  }

}