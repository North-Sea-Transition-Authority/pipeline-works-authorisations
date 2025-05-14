package uk.co.ogauthority.pwa.features.consentdocumentmigration;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.co.fivium.fileuploadlibrary.s3.S3Exception;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@ActiveProfiles("devtools")
@WebMvcTest(ConsentDocumentMigrationController.class)
@Import(PwaMvcTestConfiguration.class)
class ConsentDocumentMigrationControllerTest extends AbstractControllerTest {

  @MockBean
  private ConsentDocumentMigrationService consentDocumentMigrationService;

  @Test
  void verify() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentDocumentMigrationController.class).verify())))
        .andExpect(status().isOk());

    Mockito.verify(consentDocumentMigrationService).verify();
  }

  @Test
  void verify_failed() throws Exception {
    doThrow(new S3Exception("Bucket pwa-migration does not exist")).when(consentDocumentMigrationService).verify();

    mockMvc.perform(get(ReverseRouter.route(on(ConsentDocumentMigrationController.class).verify())))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void migrate() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ConsentDocumentMigrationController.class).migrate())))
        .andExpect(status().isOk());

    Mockito.verify(consentDocumentMigrationService).migrate();
  }

  @Test
  void migrate_failed() throws Exception {
    doThrow(new S3Exception("")).when(consentDocumentMigrationService).migrate();

    mockMvc.perform(get(ReverseRouter.route(on(ConsentDocumentMigrationController.class).migrate())))
        .andExpect(status().is5xxServerError());
  }
}