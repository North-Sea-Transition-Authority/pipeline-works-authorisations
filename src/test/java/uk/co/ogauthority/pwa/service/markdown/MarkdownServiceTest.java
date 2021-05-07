package uk.co.ogauthority.pwa.service.markdown;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class MarkdownServiceTest {

  private MarkdownService markdownService;

  @Before
  public void setUp() throws Exception {

    markdownService = new MarkdownService();

  }

  @Test
  public void convertMarkdownToHtml_heading() {

    String html = markdownService.convertMarkdownToHtml("### heading level 3", Map.of());

    assertThat(html).contains("<h3>heading level 3</h3>");

  }

  @Test
  public void convertMarkdownToHtml_mailMergeFields() {

    String html = markdownService.convertMarkdownToHtml("((PROJECT_NAME))", Map.of("PROJECT_NAME", "my proj"));

    assertThat(html).contains("<span>my proj</span>");

  }

}