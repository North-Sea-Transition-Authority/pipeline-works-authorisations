package uk.co.ogauthority.pwa.service.markdown;

import java.util.Collections;
import java.util.Map;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;

public class MailMergeHtmlNodeRenderer extends MailMergeNodeRenderer {

  private final HtmlNodeRendererContext context;
  private final HtmlWriter html;
  private final Map<String, String> mailMergeFields;

  public MailMergeHtmlNodeRenderer(HtmlNodeRendererContext context, Map<String, String> mailMergeFields) {
    this.context = context;
    this.html = context.getWriter();
    this.mailMergeFields = mailMergeFields;
  }

  @Override
  public void render(Node node) {
    Map<String, String> attributes = context.extendAttributes(node, "span", Collections.emptyMap());
    html.tag("span", attributes);
    var mailMergeLiteral = ((Text) node.getFirstChild()).getLiteral();
    html.text(mailMergeFields.get(mailMergeLiteral));
    html.tag("/span");
  }
}
