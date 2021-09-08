package uk.co.ogauthority.pwa.util;

import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldType;

public class MailMergeUtils {

  private MailMergeUtils() {
    throw new AssertionError();
  }

  public static boolean textContainsManualMergeDelimiters(String text) {
    return text.contains(MailMergeFieldType.MANUAL.getOpeningDelimiter()) || text.contains(MailMergeFieldType.MANUAL.getClosingDelimiter());
  }

}
