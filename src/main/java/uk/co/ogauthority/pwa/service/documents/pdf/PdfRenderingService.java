package uk.co.ogauthority.pwa.service.documents.pdf;

import com.google.common.base.Stopwatch;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import javax.sql.rowset.serial.SerialBlob;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
public class PdfRenderingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PdfRenderingService.class);

  private ByteArrayResource render(String html) {

    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

      Stopwatch renderStopwatch = Stopwatch.createStarted();

      // PDF renderer parses content as XML, so convert HTML to XHTML to avoid issues
      Document document = Jsoup.parse(html);
      document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

      PdfRendererBuilder builder = new PdfRendererBuilder();
      builder.withHtmlContent(document.html(), "classpath://");
      builder.toStream(os);
      builder.run();

      var elapsedMs = renderStopwatch.elapsed(TimeUnit.MILLISECONDS);

      LOGGER.info("PDF generated. Took [{}ms]", elapsedMs);

      return new ByteArrayResource(os.toByteArray());

    } catch (Exception e) {
      throw new RuntimeException("Error rendering template to PDF", e);
    }

  }

  public Blob renderToBlob(String html) {

    var pdf = render(html);

    Blob blob;
    try {
      blob = new SerialBlob(pdf.getByteArray());
    } catch (SQLException e) {
      throw new RuntimeException("Error serialising PDF blob", e);
    }

    return blob;

  }

}
