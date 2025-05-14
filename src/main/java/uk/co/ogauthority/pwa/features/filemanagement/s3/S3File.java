package uk.co.ogauthority.pwa.features.filemanagement.s3;

import com.amazonaws.services.s3.model.S3ObjectSummary;

public record S3File(String key, long size) {
  public static S3File from(S3ObjectSummary s3ObjectSummary) {
    return new S3File(s3ObjectSummary.getKey(), s3ObjectSummary.getSize());
  }
}
