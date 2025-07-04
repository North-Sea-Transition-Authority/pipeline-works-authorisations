package uk.co.ogauthority.pwa.util.documents;

import java.sql.SQLException;
import javax.sql.rowset.serial.SerialBlob;
import org.springframework.core.io.ByteArrayResource;

public class BlobUtils {
  BlobUtils() {
    throw new AssertionError();
  }

  public static SerialBlob toSerialBlob(ByteArrayResource byteArrayResource) {
    try {
      return new SerialBlob(byteArrayResource.getByteArray());
    } catch (SQLException e) {
      throw new IllegalStateException("Error serialising ByteArrayResource to Blob", e);
    }
  }
}