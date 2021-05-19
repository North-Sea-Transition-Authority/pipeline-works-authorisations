package uk.co.ogauthority.pwa.service.markdown.manual;

import java.util.Map;
import java.util.Set;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;
import uk.co.ogauthority.pwa.service.markdown.MailMergeContainer;

public class ManualMailMergeHtmlNodeRenderer implements NodeRenderer {

  private final HtmlNodeRendererContext context;
  private final HtmlWriter html;
  private final MailMergeContainer mailMergeContainer;

  public ManualMailMergeHtmlNodeRenderer(HtmlNodeRendererContext context,
                                         MailMergeContainer mailMergeContainer) {
    this.context = context;
    this.html = context.getWriter();
    this.mailMergeContainer = mailMergeContainer;
  }

  @Override
  public Set<Class<? extends Node>> getNodeTypes() {
    return Set.of(ManualMergeField.class);
  }

  @Override
  public void render(Node node) {

    Map<String, String> attributes = context.extendAttributes(node, "span", mailMergeContainer.getManualMailMergeDataHtmlAttributeMap());

    html.tag("span", attributes);

    html.tag("span", Map.of("class", "govuk-visually-hidden"));
    html.text("Manual merge field");
    html.tag("/span");

    html.text(((Text) node.getFirstChild()).getLiteral());

    html.tag("/span");

  }

}
