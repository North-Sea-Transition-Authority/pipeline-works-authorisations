package uk.co.ogauthority.pwa.service.mailmerge;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.termsandconditions.service.TermsAndConditionsService;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.service.documents.templates.TemplateDocumentSource;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@RunWith(MockitoJUnitRunner.class)
public class TermsAndConditionsMailMergeResolverTest {

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private TermsAndConditionsService termsAndConditionsService;
  private TermsAndConditionsMailMergeResolver termsAndConditionsMailMergeResolver;

  private List<MailMergeFieldMnem> mailMergeFields;

  @Before
  public void setup() {
    termsAndConditionsMailMergeResolver = new TermsAndConditionsMailMergeResolver(
        pwaApplicationDetailService,
        termsAndConditionsService);

    mailMergeFields = Arrays.stream(MailMergeFieldMnem.values())
        .collect(Collectors.toList());
  }

  @Test
  public void supportsDocumentSource() {
    assertThat(termsAndConditionsMailMergeResolver
        .supportsDocumentSource(new PwaApplication())).isTrue();
    assertThat(termsAndConditionsMailMergeResolver
        .supportsDocumentSource(new TemplateDocumentSource(DocumentSpec.VARIATION_PETROLEUM_CONSENT_DOCUMENT))).isTrue();

  }
}
