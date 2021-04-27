package uk.co.ogauthority.pwa.service.markdown;

import java.util.Map;
import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;

public class MailMergeExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension,
    TextContentRenderer.TextContentRendererExtension {

  private final Map<String, String> mailMergeFields;

  private MailMergeExtension(Map<String, String> mailMergeFields) {
    this.mailMergeFields = mailMergeFields;
  }

  public static Extension create(Map<String, String> mailMergeFields) {
    return new MailMergeExtension(mailMergeFields);
  }

  public Map<String, String> getMailMergeFields() {
    return mailMergeFields;
  }

  @Override
  public void extend(Parser.Builder parserBuilder) {
    parserBuilder.customDelimiterProcessor(new MailMergeDelimiterProcessor());
  }

  @Override
  public void extend(HtmlRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(context -> new MailMergeHtmlNodeRenderer(context, mailMergeFields));
  }

  @Override
  public void extend(TextContentRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(MailMergeTextContentNodeRenderer::new);
  }
}
