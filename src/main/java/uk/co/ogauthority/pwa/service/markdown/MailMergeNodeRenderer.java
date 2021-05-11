package uk.co.ogauthority.pwa.service.markdown;

import java.util.Set;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import uk.co.ogauthority.pwa.service.markdown.automatic.AutomaticMergeField;
import uk.co.ogauthority.pwa.service.markdown.manual.ManualMergeField;

public abstract class MailMergeNodeRenderer implements NodeRenderer {

  @Override
  public Set<Class<? extends Node>> getNodeTypes() {
    return Set.of(AutomaticMergeField.class, ManualMergeField.class);
  }

}
