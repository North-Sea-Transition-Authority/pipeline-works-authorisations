package uk.co.ogauthority.pwa.service.markdown.manual;

import java.util.Map;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;
import uk.co.ogauthority.pwa.service.markdown.MailMergeContainer;
import uk.co.ogauthority.pwa.service.markdown.MailMergeNodeRenderer;

public class ManualMailMergeHtmlNodeRenderer extends MailMergeNodeRenderer {

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
  public void render(Node node) {

    Map<String, String> attributes = context.extendAttributes(node, "span", mailMergeContainer.getManualMailMergeDataHtmlAttributeMap());

    html.tag("span", attributes);

    html.text(((Text) node.getFirstChild()).getLiteral());

    html.tag("/span");

  }

}
