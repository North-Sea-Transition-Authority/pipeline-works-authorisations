package uk.co.ogauthority.pwa.service.markdown.automatic;

import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

public class AutomaticMailMergeDelimiterProcessor implements DelimiterProcessor {

  @Override
  public char getOpeningCharacter() {
    return AutomaticMergeField.OPENING_DELIMITER.charAt(0);
  }

  @Override
  public char getClosingCharacter() {
    return AutomaticMergeField.CLOSING_DELIMITER.charAt(0);
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
    Node mailMerge = new AutomaticMergeField();

    Node tmp = opener.getNext();
    while (tmp != null && tmp != closer) {
      Node next = tmp.getNext();
      mailMerge.appendChild(tmp);
      tmp = next;
    }

    opener.insertAfter(mailMerge);
  }
}
