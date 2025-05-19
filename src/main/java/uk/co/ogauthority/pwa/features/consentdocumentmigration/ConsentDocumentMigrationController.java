package uk.co.ogauthority.pwa.features.consentdocumentmigration;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.co.fivium.fileuploadlibrary.s3.S3Exception;

@Controller
@Profile("devtools")
@RequestMapping("devtool/migration/consent-documents")
public class ConsentDocumentMigrationController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsentDocumentMigrationController.class);

  private final ConsentDocumentMigrationService consentDocumentMigrationService;

  public ConsentDocumentMigrationController(ConsentDocumentMigrationService consentDocumentMigrationService) {
    this.consentDocumentMigrationService = consentDocumentMigrationService;
  }

  @GetMapping("/verify")
  ResponseEntity<String> verify() {
    try {
      consentDocumentMigrationService.verify();
    } catch (S3Exception | IOException e) {
      var message = "Failed to verify consent documents";
      LOGGER.error(message, e);
      return ResponseEntity.internalServerError().body(message);
    }
    return ResponseEntity.ok().body("Verification Successful");
  }

  @GetMapping("/migrate")
  ResponseEntity<String> migrate() {
    try {
      consentDocumentMigrationService.migrate();
    } catch (S3Exception | IOException e) {
      var message = "Failed to migrate consent documents";
      LOGGER.error(message, e);
      return ResponseEntity.internalServerError().body(message);
    }
    return ResponseEntity.ok().body("Migration Successful");
  }

}

