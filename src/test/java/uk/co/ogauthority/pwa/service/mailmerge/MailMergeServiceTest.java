package uk.co.ogauthority.pwa.service.mailmerge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.mailmerge.MailMergeField;
import uk.co.ogauthority.pwa.model.entity.mailmerge.MailMergeFieldDocSpec;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.repository.mailmerge.MailMergeFieldDocSpecRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.markdown.MarkdownService;

@RunWith(MockitoJUnitRunner.class)
public class MailMergeServiceTest {

  @Mock
  private MailMergeFieldDocSpecRepository mailMergeFieldDocSpecRepository;

  @Mock
  private MarkdownService markdownService;

  private MailMergeService mailMergeService;

  @Before
  public void setUp() throws Exception {

    mailMergeService = new MailMergeService(mailMergeFieldDocSpecRepository, List.of(), markdownService);

  }

  @Test
  public void getMailMergeFieldsForDocumentSource() {

    var app = new PwaApplication();
    app.setApplicationType(PwaApplicationType.INITIAL);

    var mailMergeField1 = new MailMergeField();
    var mailMergeDocSpec1 = new MailMergeFieldDocSpec();
    mailMergeDocSpec1.setMailMergeField(mailMergeField1);

    var mailMergeField2 = new MailMergeField();
    var mailMergeDocSpec2 = new MailMergeFieldDocSpec();
    mailMergeDocSpec2.setMailMergeField(mailMergeField2);

    when(mailMergeFieldDocSpecRepository.getAllByDocumentSpec(app.getDocumentSpec()))
        .thenReturn(List.of(mailMergeDocSpec1, mailMergeDocSpec2));

    var fields = mailMergeService.getMailMergeFieldsForDocumentSource(app);

    assertThat(fields).containsExactlyInAnyOrder(mailMergeField1, mailMergeField2);

  }

}