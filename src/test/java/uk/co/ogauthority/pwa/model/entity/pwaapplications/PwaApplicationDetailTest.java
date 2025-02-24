package uk.co.ogauthority.pwa.model.entity.pwaapplications;



import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

class PwaApplicationDetailTest {

  @Test
  void isFirstDraft_whenFirstDraft() {

    var detail = new PwaApplicationDetail();
    detail.setVersionNo(1);
    detail.setStatus(PwaApplicationStatus.DRAFT);

    assertThat(detail.isFirstDraft()).isTrue();

  }

  @Test
  void isFirstDraft_whenFirstVersion_andNotDraft() {

    var detail = new PwaApplicationDetail();
    detail.setVersionNo(1);
    detail.setStatus(PwaApplicationStatus.UPDATE_REQUESTED);

    assertThat(detail.isFirstDraft()).isFalse();

  }

  @Test
  void isFirstDraft_whenNotFirstVersion_andDraft() {

    var detail = new PwaApplicationDetail();
    detail.setVersionNo(2);
    detail.setStatus(PwaApplicationStatus.DRAFT);

    assertThat(detail.isFirstDraft()).isFalse();

  }
}