package uk.co.ogauthority.pwa;

import com.google.common.net.HttpHeaders;
import java.io.IOException;
import javax.sql.rowset.serial.SerialException;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.service.documents.signing.DocumentSigningService;

@Profile("test-digital-signing")
@Controller
@RequestMapping("digital-signing")
public class DigitalSigningTestController {

  private final DocumentSigningService documentSigningService;

  public DigitalSigningTestController(DocumentSigningService documentSigningService) {
    this.documentSigningService = documentSigningService;
  }

  @GetMapping("/test-preview")
  public ResponseEntity<?> testPreview() throws IOException, SerialException {

    var res = new ClassPathResource("test-digital-signature-document.pdf");

    var previewPdf = documentSigningService.previewPdfSignature(new ByteArrayResource(res.getContentAsByteArray()));

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .contentLength(previewPdf.length())
        .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"%s\"".formatted("preview-test.pdf"))
        .body(new InputStreamResource((previewPdf.getBinaryStream())));
  }

  @GetMapping("/test-sign")
  public ResponseEntity<?> testSign() throws IOException, SerialException {

    var res = new ClassPathResource("test-digital-signature-document.pdf");

    var person = new Person();
    person.setId(1);
    person.setForename("Signer");
    person.setSurname("Test");
    person.setEmailAddress("test@example.com");

    var previewPdf = documentSigningService.signPdf(new ByteArrayResource(res.getContentAsByteArray()), person);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .contentLength(previewPdf.length())
        .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"%s\"".formatted("sign-test.pdf"))
        .body(new InputStreamResource((previewPdf.getBinaryStream())));
  }

}
