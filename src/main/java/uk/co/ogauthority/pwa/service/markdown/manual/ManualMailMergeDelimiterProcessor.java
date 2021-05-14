package uk.co.ogauthority.pwa.service.markdown.manual;

import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

public class ManualMailMergeDelimiterProcessor implements DelimiterProcessor {

  @Override
  public char getOpeningCharacter() {
    return ManualMergeField.OPENING_DELIMITER.charAt(0);
  }

  @Override
  public char getClosingCharacter() {
    return ManualMergeField.CLOSING_DELIMITER.charAt(0);
  }

  @Override
  public int getMinLength() {
    return 2;
  }

  @Override
  public int getDelimiterUse(DelimiterRun opener, DelimiterRun closer) {
    if (opener.length() >= 2 && closer.length() >= 2) {
      // Use exactly two delimiters even if we have more, and don't care about internal openers/closers.
      return 2;
    } else {
      return 0;
    }
  }

  @Override
  public void process(Text opener, Text closer, int delimiterUse) {
    // Wrap nodes between delimiters in strikethrough.
    Node mailMerge = new ManualMergeField();

    Node tmp = opener.getNext();
    while (tmp != closer) {
      Node next = tmp.getNext();
      mailMerge.appendChild(tmp);
      tmp = next;
    }

    opener.insertAfter(mailMerge);
  }
}
