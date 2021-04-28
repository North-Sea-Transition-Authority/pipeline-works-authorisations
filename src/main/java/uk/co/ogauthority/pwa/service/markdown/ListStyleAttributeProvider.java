package uk.co.ogauthority.pwa.service.markdown;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.commonmark.node.BulletList;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.renderer.html.AttributeProvider;

public class ListStyleAttributeProvider implements AttributeProvider {

  private final List<String> classNames;

  public ListStyleAttributeProvider(List<String> classNames) {
    this.classNames = classNames;
  }

  public ListStyleAttributeProvider() {
    this.classNames = Collections.emptyList();
  }

  @Override
  public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
    if (node instanceof OrderedList || node instanceof BulletList) {
      var classes = String.join(" ", classNames);
      attributes.put("class", classes);
    }
  }
}
