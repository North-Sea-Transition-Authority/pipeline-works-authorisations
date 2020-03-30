package uk.co.ogauthority.pwa.config.fileupload;


public class FileDeleteResult {
  private final String fileId;
  private final DeleteOutcomeType deleteOutcomeType;

  public FileDeleteResult(String fileId, DeleteOutcomeType deleteOutcomeType) {
    this.fileId = fileId;
    this.deleteOutcomeType = deleteOutcomeType;
  }

  public static FileDeleteResult generateSuccessfulFileDeleteResult(String fileId) {
    return new FileDeleteResult(fileId, DeleteOutcomeType.SUCCESS);
  }

  public static FileDeleteResult generateFailedFileDeleteResult(String fileId) {
    return new FileDeleteResult(fileId, DeleteOutcomeType.INTERNAL_SERVER_ERROR);
  }

  public boolean isValid() {
    return this.deleteOutcomeType.equals(DeleteOutcomeType.SUCCESS) && this.fileId != null;
  }
}
