package uk.co.ogauthority.pwa.repository.pwaconsents;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.docgen.DocgenRunStatus;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.testutil.PwaViewTabTestUtil;

@ExtendWith(MockitoExtension.class)
class PwaConsentApplicationDtoTest {

  private DocgenRun getRunWithInfo(long id, DocgenRunStatus complete) {
    var run = new DocgenRun();
    run.setId(id);
    run.setStatus(complete);
    return run;
  }

  @Test
  void consentDocumentDownloadable_complete() {

    var dto = PwaViewTabTestUtil.createConsentApplicationDto(Instant.now(), getRunWithInfo(1L, DocgenRunStatus.COMPLETE));

    assertThat(dto.consentDocumentDownloadable()).isTrue();

  }

  @Test
  void consentDocumentDownloadable_notComplete() {

    var dto = PwaViewTabTestUtil.createConsentApplicationDto(Instant.now(), getRunWithInfo(1L, DocgenRunStatus.PENDING));

    assertThat(dto.consentDocumentDownloadable()).isFalse();

  }

  @Test
  void consentDocumentDownloadable_notPresent() {

    var dto = PwaViewTabTestUtil.createMigratedConsentApplicationDto(Instant.now());

    assertThat(dto.consentDocumentDownloadable()).isFalse();

  }

  @Test
  void getDocStatusDisplay_pending() {

    var dto = PwaViewTabTestUtil.createConsentApplicationDto(Instant.now(), getRunWithInfo(1L, DocgenRunStatus.PENDING));

    assertThat(dto.getDocStatusDisplay()).isEqualTo("Document is generating");

  }

  @Test
  void getDocStatusDisplay_failed() {

    var dto = PwaViewTabTestUtil.createConsentApplicationDto(Instant.now(), getRunWithInfo(1L, DocgenRunStatus.FAILED));

    assertThat(dto.getDocStatusDisplay()).isEqualTo("Document failed to generate");

  }

  @Test
  void getDocStatusDisplay_complete() {

    var dto = PwaViewTabTestUtil.createConsentApplicationDto(Instant.now(), getRunWithInfo(1L, DocgenRunStatus.COMPLETE));

    assertThat(dto.getDocStatusDisplay()).isEqualTo("");

  }

}