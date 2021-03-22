package uk.co.ogauthority.pwa.model.entity.pwaapplications;



import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

public class PwaApplicationDetailTest {

  @Test
  public void isFirstDraft_whenFirstDraft() {

    var detail = new PwaApplicationDetail();
    detail.setVersionNo(1);
    detail.setStatus(PwaApplicationStatus.DRAFT);

    assertThat(detail.isFirstDraft()).isTrue();

  }

  @Test
  public void isFirstDraft_whenFirstVersion_andNotDraft() {

    var detail = new PwaApplicationDetail();
    detail.setVersionNo(1);
    detail.setStatus(PwaApplicationStatus.UPDATE_REQUESTED);

    assertThat(detail.isFirstDraft()).isFalse();

  }

  @Test
  public void isFirstDraft_whenNotFirstVersion_andDraft() {

    var detail = new PwaApplicationDetail();
    detail.setVersionNo(2);
    detail.setStatus(PwaApplicationStatus.DRAFT);

    assertThat(detail.isFirstDraft()).isFalse();

  }
}