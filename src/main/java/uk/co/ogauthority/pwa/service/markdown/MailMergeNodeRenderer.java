package uk.co.ogauthority.pwa.service.markdown;

import java.util.Collections;
import java.util.Set;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;

abstract class MailMergeNodeRenderer implements NodeRenderer {

  @Override
  public Set<Class<? extends Node>> getNodeTypes() {
    return Collections.singleton(ManualMergeField.class);
  }

}
