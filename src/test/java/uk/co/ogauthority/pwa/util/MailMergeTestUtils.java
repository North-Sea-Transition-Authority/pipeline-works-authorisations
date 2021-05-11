package uk.co.ogauthority.pwa.util;

import java.util.Map;
import uk.co.ogauthority.pwa.service.markdown.MailMergeContainer;

public class MailMergeTestUtils {

  private MailMergeTestUtils() {
    throw new AssertionError();
  }

  public static MailMergeContainer getMergeContainerWithMergeFields(Map<String, String> mergeFields) {
    var container = new MailMergeContainer();
    container.setMailMergeFields(mergeFields);
    return container;
  }

}
