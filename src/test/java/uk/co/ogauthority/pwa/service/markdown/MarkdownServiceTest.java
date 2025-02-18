package uk.co.ogauthority.pwa.service.markdown;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.util.MailMergeTestUtils;

class MarkdownServiceTest {

  private MarkdownService markdownService;

  @BeforeEach
  void setUp() throws Exception {

    markdownService = new MarkdownService();

  }

  @Test
  void convertMarkdownToHtml_heading() {

    String html = markdownService.convertMarkdownToHtml("### heading level 3");

    assertThat(html).contains("<h3>heading level 3</h3>");

  }

  @Test
  void convertMarkdownToHtml_emptyMergeContainer_heading() {

    var container = new MailMergeContainer();
    String html = markdownService.convertMarkdownToHtml("### heading level 3", container);

    assertThat(html).contains("<h3>heading level 3</h3>");

  }

  @Test
  void convertMarkdownToHtml_mailMergeFields() {

    var container = MailMergeTestUtils.getMergeContainerWithMergeFields(Map.of("PROJECT_NAME", "my proj"));
    String html = markdownService.convertMarkdownToHtml("((PROJECT_NAME))", container);

    assertThat(html).contains("<span><span class=\"govuk-visually-hidden\">Automatic merge field</span>my proj</span>");

  }

  @Test
  void convertMarkdownToHtml_null_noError() {

    var container = new MailMergeContainer();
    String html = markdownService.convertMarkdownToHtml(null, container);

    assertThat(html).isNull();

  }

}