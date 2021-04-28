package uk.co.ogauthority.pwa.service.markdown;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;

public class ManualMergeField extends CustomNode implements Delimited {

  protected static final String OPENING_DELIMITER = "??";
  protected static final String CLOSING_DELIMITER = "??";

  @Override
  public String getOpeningDelimiter() {
    return OPENING_DELIMITER;
  }

  @Override
  public String getClosingDelimiter() {
    return CLOSING_DELIMITER;
  }
}
