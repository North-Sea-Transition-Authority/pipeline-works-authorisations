package uk.co.ogauthority.pwa.service.markdown.automatic;

import java.util.Map;
import java.util.Set;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;
import uk.co.ogauthority.pwa.service.markdown.MailMergeContainer;

public class AutomaticMailMergeHtmlNodeRenderer implements NodeRenderer {

  private final HtmlNodeRendererContext context;
  private final HtmlWriter html;
  private final MailMergeContainer mailMergeContainer;

  public AutomaticMailMergeHtmlNodeRenderer(HtmlNodeRendererContext context,
                                            MailMergeContainer mailMergeContainer) {
    this.context = context;
    this.html = context.getWriter();
    this.mailMergeContainer = mailMergeContainer;
  }

  @Override
  public Set<Class<? extends Node>> getNodeTypes() {
    return Set.of(AutomaticMergeField.class);
  }

  @Override
  public void render(Node node) {

    var mergeFieldContents = mailMergeContainer.getMailMergeFields().get(((Text) node.getFirstChild()).getLiteral());
    if (mergeFieldContents.matches("(\\?\\?).*(\\?\\?)")) {
      renderManual(mergeFieldContents, node);
    } else {
      renderWithValue(mergeFieldContents, node);
    }
  }

  public void renderWithValue(String mergeFieldContents, Node node) {
    var attributes = context.extendAttributes(node, "span", mailMergeContainer.getAutomaticMailMergeDataHtmlAttributeMap());
    html.tag("span", attributes);

    html.tag("span", Map.of("class", "govuk-visually-hidden"));
    html.text("Automatic merge field");
    html.tag("/span");

    html.text(mergeFieldContents);

    html.tag("/span");
  }

  /**
   * Renders the merge field as a manual input field for when the field value can not be resolved.
   * Using this method, the field will be rendered as a manual field requiring intervention.
   * This cannot be done in a different method except by changing the enire merge resolver framework.
   * This is because the field type is determined by the delimter processor, which marks it as automatic.
   * @param mergeFieldContents the contents to merge in.
   * @param node the node to merge into.
   */
  public void renderManual(String mergeFieldContents, Node node) {
    var attributes = context.extendAttributes(node, "span", mailMergeContainer.getManualMailMergeDataHtmlAttributeMap());
    html.tag("span", attributes);

    html.tag("span", Map.of("class", "govuk-visually-hidden"));
    html.text("Automatic merge field");
    html.tag("/span");

    html.text(mergeFieldContents.split("\\?\\?")[1]);

    html.tag("/span");
  }

}
