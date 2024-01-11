package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

import java.util.List;

public class CarbonStorageCrossingView {

  private int id;
  private String storageAreaReference;
  private boolean ownedCompletelyByHolder;
  private List<String> operatorList;

  public CarbonStorageCrossingView(int id,
                                   String storageAreaReference,
                                   List<String> operatorList,
                                   boolean ownedCompletelyByHolder) {
    this.id = id;
    this.storageAreaReference = storageAreaReference;
    this.operatorList = operatorList;
    this.ownedCompletelyByHolder = ownedCompletelyByHolder;
  }

  public String getStorageAreaReference() {
    return storageAreaReference;
  }

  public void setStorageAreaReference(String storageAreaReference) {
    this.storageAreaReference = storageAreaReference;
  }

  public List<String> getOperatorList() {
    return operatorList;
  }

  public void setOperatorList(List<String> operatorList) {
    this.operatorList = operatorList;
  }

  public boolean getOwnedCompletelyByHolder() {
    return ownedCompletelyByHolder;
  }

  public void setOwnedCompletelyByHolder(boolean ownedCompletelyByHolder) {
    this.ownedCompletelyByHolder = ownedCompletelyByHolder;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}
