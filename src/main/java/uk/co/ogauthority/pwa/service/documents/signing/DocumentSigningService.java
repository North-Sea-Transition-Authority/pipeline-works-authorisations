package uk.co.ogauthority.pwa.service.documents.signing;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import uk.co.fivium.ftss.client.FtssClient;
import uk.co.fivium.ftss.client.FtssVisualSignatureProperties;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

@Service
public class DocumentSigningService {

  private final FtssClient ftssClient;
  private final DigitalSignatureProperties digitalSignatureProperties;

  DocumentSigningService(
      FtssClient ftssClient,
      DigitalSignatureProperties digitalSignatureProperties
  ) {
    this.ftssClient = ftssClient;
    this.digitalSignatureProperties = digitalSignatureProperties;
  }

  public ByteArrayResource previewPdfSignature(ByteArrayResource pdfResource) {
    var visualSignatureProperties = getVisualSignatureProperties(
        pdfResource,
        digitalSignatureProperties.line1(),
        "((CASE_OFFICER))",
        digitalSignatureProperties.line3()
    );
    var ftssSignerProperties = digitalSignatureProperties.asFtssSignerProperties();

    try {
      return new ByteArrayResource(
          ftssClient.previewPdf(pdfResource.getInputStream(), ftssSignerProperties, visualSignatureProperties).readAllBytes()
      );
    } catch (Exception e) {
      throw new IllegalStateException("Failed to preview signature", e);
    }
  }

  public ByteArrayResource signPdf(ByteArrayResource pdfResource, Person signingUser) {
    var visualSignatureProperties = getVisualSignatureProperties(
        pdfResource,
        digitalSignatureProperties.line1(),
        signingUser.getFullName(),
        digitalSignatureProperties.line3()
    );
    var ftssSignerProperties = digitalSignatureProperties.asFtssSignerProperties();

    try {
      return new ByteArrayResource(
          ftssClient.signPdf(pdfResource.getInputStream(), ftssSignerProperties, visualSignatureProperties).readAllBytes()
      );
    } catch (Exception e) {
      throw new IllegalStateException("Failed to sign PDF", e);
    }

  }

  private FtssVisualSignatureProperties getVisualSignatureProperties(
      ByteArrayResource pdfResource,
      String line1,
      String line2,
      String line3
  ) {
    try (var pdf = PDDocument.load(pdfResource.getByteArray())) {
      var signatureCoordinates = new SignaturePlaceholderLocator().getSignaturePlaceholderLocation(pdf);

      return new FtssVisualSignatureProperties(
          // Workaround for current FTSS signature image requirements TODO FTSS-90
          new ClassPathResource("document-assets/blank-1px.png"),
          signatureCoordinates,
          line1,
          line2,
          line3
      );
    } catch (Exception e) {
      throw new IllegalStateException("Failed to calculate FTSS visual signature properties", e);
    }
  }
}
