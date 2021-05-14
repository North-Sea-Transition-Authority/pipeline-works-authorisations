package uk.co.ogauthority.pwa.service.markdown.manual;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldType;

public class ManualMergeField extends CustomNode implements Delimited {

  public static final String OPENING_DELIMITER = MailMergeFieldType.MANUAL.getOpeningDelimiter();
  public static final String CLOSING_DELIMITER = MailMergeFieldType.MANUAL.getClosingDelimiter();

  @Override
  public String getOpeningDelimiter() {
    return OPENING_DELIMITER;
  }

  @Override
  public String getClosingDelimiter() {
    return CLOSING_DELIMITER;
  }
}
