package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import java.util.List;

public class BlockCrossingView {

  private int id;
  private String blockReference;
  private String licenceReference;
  private Boolean blockOwnedCompletelyByHolder;
  private List<String> blockOperatorList;

  public BlockCrossingView(int id,
                           String blockReference,
                           String licenceReference,
                           List<String> blockOperatorList,
                           Boolean blockOwnedCompletelyByHolder) {
    this.id = id;
    this.blockReference = blockReference;
    this.licenceReference = licenceReference;
    this.blockOperatorList = blockOperatorList;
    this.blockOwnedCompletelyByHolder = blockOwnedCompletelyByHolder;
  }

  public String getBlockReference() {
    return blockReference;
  }

  public void setBlockReference(String blockReference) {
    this.blockReference = blockReference;
  }

  public String getLicenceReference() {
    return licenceReference;
  }

  public void setLicenceReference(String licenceReference) {
    this.licenceReference = licenceReference;
  }

  public List<String> getBlockOperatorList() {
    return blockOperatorList;
  }

  public void setBlockOperatorList(List<String> blockOperatorList) {
    this.blockOperatorList = blockOperatorList;
  }

  public Boolean getBlockOwnedCompletelyByHolder() {
    return blockOwnedCompletelyByHolder;
  }

  public void setBlockOwnedCompletelyByHolder(Boolean blockOwnedCompletelyByHolder) {
    this.blockOwnedCompletelyByHolder = blockOwnedCompletelyByHolder;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}
