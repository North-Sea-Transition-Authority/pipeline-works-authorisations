package uk.co.ogauthority.pwa.service.markdown;

import java.util.Set;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentWriter;
import uk.co.ogauthority.pwa.service.markdown.manual.ManualMergeField;

public class MailMergeTextContentNodeRenderer implements NodeRenderer {

  private final TextContentNodeRendererContext context;
  private final TextContentWriter textContent;

  public MailMergeTextContentNodeRenderer(TextContentNodeRendererContext context) {
    this.context = context;
    this.textContent = context.getWriter();
  }

  @Override
  public Set<Class<? extends Node>> getNodeTypes() {
    return Set.of(AutomaticMergeField.class, ManualMergeField.class);
  }

  @Override
  public void render(Node node) {
    textContent.write('/');
    renderChildren(node);
    textContent.write('/');
  }

  private void renderChildren(Node parent) {
    Node node = parent.getFirstChild();
    while (node != null) {
      Node next = node.getNext();
      context.render(node);
      node = next;
    }
  }

}
