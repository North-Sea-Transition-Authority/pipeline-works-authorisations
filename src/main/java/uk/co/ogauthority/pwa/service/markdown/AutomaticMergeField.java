package uk.co.ogauthority.pwa.service.markdown;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldType;

public class AutomaticMergeField extends CustomNode implements Delimited {

  protected static final String OPENING_DELIMITER = MailMergeFieldType.AUTOMATIC.getOpeningDelimiter();
  protected static final String CLOSING_DELIMITER = MailMergeFieldType.AUTOMATIC.getClosingDelimiter();

  @Override
  public String getOpeningDelimiter() {
    return OPENING_DELIMITER;
  }

  @Override
  public String getClosingDelimiter() {
    return CLOSING_DELIMITER;
  }
}
