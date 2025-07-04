package uk.co.ogauthority.pwa.service.mailmerge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.service.documents.DocumentSource;
import uk.co.ogauthority.pwa.service.documents.templates.TemplateDocumentSource;

public class PageBreakMailMergeResolverTest {

  private PageBreakMailMergeResolver underTest;

  @Before
  public void setUp() {
    underTest = new PageBreakMailMergeResolver();
  }

  @Test
  public void supportsDocumentSource_givenPwaApplication_returnsTrue() {
    // Arrange
    PwaApplication pwaApplication = new PwaApplication();

    // Act
    boolean result = underTest.supportsDocumentSource(pwaApplication);

    // Assert
    assertThat(result).isTrue();
  }

  @Test
  public void supportsDocumentSource_givenTemplateDocumentSource_returnsTrue() {
    // Arrange
    TemplateDocumentSource templateSource = mock(TemplateDocumentSource.class);

    // Act
    boolean result = underTest.supportsDocumentSource(templateSource);

    // Assert
    assertThat(result).isTrue();
  }

  @Test
  public void supportsDocumentSource_givenUnsupportedType_returnsFalse() {
    // Arrange
    DocumentSource documentSource = mock(DocumentSource.class);

    // Act
    boolean result = underTest.supportsDocumentSource(documentSource);

    // Assert
    assertThat(result).isFalse();
  }

  @Test
  public void getAvailableMailMergeFields_returnsDigitalSignature() {
    // Arrange
    DocumentSource pwaApplication = new PwaApplication();

    // Act
    List<MailMergeFieldMnem> fields = underTest.getAvailableMailMergeFields(pwaApplication);

    // Assert
    assertThat(fields)
        .containsExactly(MailMergeFieldMnem.PAGE_BREAK)
        .hasSize(1);
  }

  @Test
  public void resolveMergeFields_returnsExpectedPlaceholders() {
    // Arrange
    TemplateDocumentSource templateSource = mock(TemplateDocumentSource.class);

    // Act
    Map<String, String> result = underTest.resolveMergeFields(templateSource);

    // Assert
    assertThat(result)
        .hasSize(1)
        .containsEntry("PAGE_BREAK", "((PAGE_BREAK))");
  }
}
