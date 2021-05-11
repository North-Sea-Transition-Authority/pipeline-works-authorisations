package uk.co.ogauthority.pwa.service.markdown.automatic;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import uk.co.ogauthority.pwa.service.markdown.MailMergeContainer;
import uk.co.ogauthority.pwa.service.markdown.MailMergeTextContentNodeRenderer;

public class AutomaticMailMergeExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension,
    TextContentRenderer.TextContentRendererExtension {

  private final MailMergeContainer mailMergeContainer;

  private AutomaticMailMergeExtension(MailMergeContainer mailMergeContainer) {
    this.mailMergeContainer = mailMergeContainer;
  }

  public static Extension create(MailMergeContainer mailMergeContainer) {
    return new AutomaticMailMergeExtension(mailMergeContainer);
  }

  @Override
  public void extend(Parser.Builder parserBuilder) {
    parserBuilder.customDelimiterProcessor(new AutomaticMailMergeDelimiterProcessor());
  }

  @Override
  public void extend(HtmlRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(context -> new AutomaticMailMergeHtmlNodeRenderer(context, mailMergeContainer));
  }

  @Override
  public void extend(TextContentRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(MailMergeTextContentNodeRenderer::new);
  }
}
