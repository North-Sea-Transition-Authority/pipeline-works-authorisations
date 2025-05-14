package uk.co.ogauthority.pwa.features.filemanagement.s3;

import java.util.List;

public record ListS3FilesResult(boolean truncated, String nextContinuationToken, List<S3File> s3Files) {
}
