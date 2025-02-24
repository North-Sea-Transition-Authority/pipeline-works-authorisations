package uk.co.ogauthority.pwa.service.documents.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.sql.rowset.serial.SerialException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import uk.co.fivium.ftss.client.FtssClient;
import uk.co.fivium.ftss.client.FtssSignerProperties;
import uk.co.fivium.ftss.client.FtssVisualSignatureProperties;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.util.documents.BlobUtils;

@RunWith(MockitoJUnitRunner.class)
public class DocumentSigningServiceTest {

  @Mock
  private FtssClient ftssClient;

  @Captor
  private ArgumentCaptor<ByteArrayInputStream> inputStreamCaptor;

  private final DigitalSignatureProperties digitalSignatureProperties = new DigitalSignatureProperties(
    "PWA",
      "Fivium Ltd",
      "tech@fivium.co.uk",
      "London",
      "GB",
      "London",
      "test signing",
      "unit tests",
      "test line 1",
      "test line 3"
  );

  private final FtssSignerProperties expectedSignerProperties = new FtssSignerProperties(
      "PWA",
      "Fivium Ltd",
      "tech@fivium.co.uk",
      "London",
      "GB",
      "London",
      "test signing",
      "unit tests");

  private final AuthenticatedUserAccount signingUser = AuthenticatedUserAccountTestUtil.defaultAllPrivUserAccount();

  private final ByteArrayResource document;

  private final ByteArrayResource signedPdf = new ByteArrayResource(new byte[] {1, 2, 3});

  private DocumentSigningService documentSigningService;

  public DocumentSigningServiceTest() throws IOException {
    document =
        new ByteArrayResource(new ClassPathResource("signing/signature-placeholder-page1-top.pdf").getContentAsByteArray());
  }

  @Before
  public void setUp() {
    documentSigningService = new DocumentSigningService(ftssClient, digitalSignatureProperties);
  }

  @Test
  public void previewPdfSignature() throws IOException, SerialException {

    when(ftssClient.previewPdf(inputStreamCaptor.capture(), eq(expectedSignerProperties), any(FtssVisualSignatureProperties.class)))
        .thenReturn(signedPdf.getInputStream());

    var result = documentSigningService.previewPdfSignature(document);

    assertThat(result.getBinaryStream().readAllBytes()).isEqualTo(BlobUtils.toSerialBlob(signedPdf).getBinaryStream().readAllBytes());
    assertThat(inputStreamCaptor.getValue().readAllBytes()).isEqualTo(document.getContentAsByteArray());
  }

  @Test
  public void signPdf() throws IOException, SerialException {

    when(ftssClient.signPdf(inputStreamCaptor.capture(), eq(expectedSignerProperties), any(FtssVisualSignatureProperties.class)))
        .thenReturn(signedPdf.getInputStream());

    var result = documentSigningService.signPdf(document, signingUser.getLinkedPerson());

    assertThat(result.getBinaryStream().readAllBytes()).isEqualTo(BlobUtils.toSerialBlob(signedPdf).getBinaryStream().readAllBytes());
    assertThat(inputStreamCaptor.getValue().readAllBytes()).isEqualTo(document.getContentAsByteArray());
  }
}
