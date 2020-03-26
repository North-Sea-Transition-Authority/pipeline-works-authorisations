package uk.co.ogauthority.pwa.config.fileupload;


public class FileDeleteResult {
  private final String fileId;
  private final DeleteOutcomeType deleteOutcomeType;

  public FileDeleteResult(String fileId, DeleteOutcomeType deleteOutcomeType) {
    this.fileId = fileId;
    this.deleteOutcomeType = deleteOutcomeType;
  }

  public static FileDeleteResult generateSuccessfulFileDeleteResult(String fileId,
                                                                    DeleteOutcomeType deleteOutcomeType) {
    return new FileDeleteResult(fileId, deleteOutcomeType);
  }

  public static FileDeleteResult generateFailedFileDeleteResult(String fileId,
                                                                DeleteOutcomeType deleteOutcomeType) {
    return new FileDeleteResult(fileId, deleteOutcomeType);
  }

  public boolean isValid() {
    return this.deleteOutcomeType.equals(DeleteOutcomeType.SUCCESS) && this.fileId != null;
  }
}
