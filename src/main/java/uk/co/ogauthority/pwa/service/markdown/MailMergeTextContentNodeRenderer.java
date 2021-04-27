package uk.co.ogauthority.pwa.service.markdown;

import org.commonmark.node.Node;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentWriter;

public class MailMergeTextContentNodeRenderer extends MailMergeNodeRenderer {

  private final TextContentNodeRendererContext context;
  private final TextContentWriter textContent;

  public MailMergeTextContentNodeRenderer(TextContentNodeRendererContext context) {
    this.context = context;
    this.textContent = context.getWriter();
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
