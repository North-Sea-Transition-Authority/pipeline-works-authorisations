package uk.co.ogauthority.pwa.service.mailmerge;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.termsandconditions.service.TermsAndConditionsService;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.service.documents.templates.TemplateDocumentSource;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@ExtendWith(MockitoExtension.class)
class TermsAndConditionsMailMergeResolverTest {
  @Mock
  private TermsAndConditionsService termsAndConditionsService;

  private TermsAndConditionsMailMergeResolver termsAndConditionsMailMergeResolver;

  private List<MailMergeFieldMnem> mailMergeFields;

  @BeforeEach
  void setup() {
    termsAndConditionsMailMergeResolver = new TermsAndConditionsMailMergeResolver(termsAndConditionsService);

    mailMergeFields = Arrays.stream(MailMergeFieldMnem.values())
        .collect(Collectors.toList());
  }

  @Test
  void supportsDocumentSource() {
    assertThat(termsAndConditionsMailMergeResolver
        .supportsDocumentSource(new PwaApplication())).isTrue();
    assertThat(termsAndConditionsMailMergeResolver
        .supportsDocumentSource(new TemplateDocumentSource(DocumentSpec.VARIATION_PETROLEUM_CONSENT_DOCUMENT))).isTrue();

  }
}
