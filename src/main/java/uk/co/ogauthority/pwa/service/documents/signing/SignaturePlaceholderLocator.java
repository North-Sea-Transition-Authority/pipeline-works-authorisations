package uk.co.ogauthority.pwa.service.documents.signing;

import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import uk.co.fivium.ftss.client.FtssVisualSignatureProperties;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;

/*
  Finds the coordinates of the digital signature placeholder text and expands to a rectangular set of
  FTSS SignatureCoordinates with given width and height.
  The placeholder text must be on its own line with no other text before or after.
 */
public class SignaturePlaceholderLocator extends PDFTextStripper {
  public static final String SIGNATURE_PLACEHOLDER_TEXT = String.format("((%s))", MailMergeFieldMnem.DIGITAL_SIGNATURE.name());
  private static final int SIGNATURE_WIDTH = 218;
  private static final int SIGNATURE_HEIGHT = 30;
  private static final int SIGNATURE_PADDING_LEFT = 5;

  private boolean placeholderFound;
  private int placeholderTextPageNumber;
  private int placeholderTextX;
  private int placeholderTextY;

  public SignaturePlaceholderLocator() throws IOException {
    super();
  }

  @Override
  protected void writeString(String text, List<TextPosition> textPositions) {
    if (text.trim().equals(SIGNATURE_PLACEHOLDER_TEXT)) {
      if (placeholderFound) {
        throw new IllegalStateException("Multiple digital signature placeholders found in document");
      }

      var textPosition = textPositions.getFirst(); // use first character as location reference

      placeholderFound = true;
      // PDFTextStripper page number is 1 indexed, PDF (and therefore FTSS) is 0 index so adjust accordingly.
      placeholderTextPageNumber = getCurrentPageNo() - 1;
      placeholderTextX = Math.round(textPosition.getX());
      // PDF coordinates are referenced from bottom left, PDFTextStripper coordinates are referenced top left,
      // so convert to bottom left reference
      placeholderTextY = Math.round(getCurrentPage().getCropBox().getHeight() - textPosition.getY());
    }
  }

  public FtssVisualSignatureProperties.SignatureCoordinates getSignaturePlaceholderLocation(PDDocument document) {
    try {
      this.getText(document);

      if (!placeholderFound) {
        throw new IllegalStateException("No signature placeholder found in document");
      }

      return new FtssVisualSignatureProperties.SignatureCoordinates(
          new FtssVisualSignatureProperties.Coordinate(// Top left
            placeholderTextPageNumber,
            placeholderTextX - SIGNATURE_PADDING_LEFT, // Add slight padding to ensure placeholder text is covered
            placeholderTextY + (SIGNATURE_HEIGHT / 2)
          ),
          new FtssVisualSignatureProperties.Coordinate(// Top right
            placeholderTextPageNumber,
            placeholderTextX + SIGNATURE_WIDTH,
            placeholderTextY + (SIGNATURE_HEIGHT / 2)
          ),
          new FtssVisualSignatureProperties.Coordinate(// Bottom left
            placeholderTextPageNumber,
            placeholderTextX - SIGNATURE_PADDING_LEFT,
            placeholderTextY - (SIGNATURE_HEIGHT / 2)  // minus height as coords referenced bottom left
          ),
          new FtssVisualSignatureProperties.Coordinate(// Bottom right
            placeholderTextPageNumber,
            placeholderTextX + SIGNATURE_WIDTH,
            placeholderTextY - (SIGNATURE_HEIGHT / 2)
          )
      );
    } catch (Exception e) {
      throw new IllegalStateException("Failed to parse signature placeholder", e);
    }
  }
}
