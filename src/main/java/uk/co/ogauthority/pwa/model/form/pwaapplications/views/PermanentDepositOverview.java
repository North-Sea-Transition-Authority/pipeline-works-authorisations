package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.view.StringWithTag;


public class PermanentDepositOverview {

  private final Integer entityID;
  private final Boolean depositIsForConsentedPipeline;
  private final MaterialType materialTypeLookup;
  private final String depositReference;
  private final List<String> pipelineRefs;
  private final Boolean depositIsForPipelinesOnOtherApp;
  private final String appRefAndPipelineNum;
  private final String fromDateEstimate;
  private final String toDateEstimate;

  private final StringWithTag materialType;
  private final String materialSize;

  private final Boolean groutBagsBioDegradable;
  private final String bioGroutBagsNotUsedDescription;

  private final String quantity;

  private final String contingencyAmount;
  private final CoordinatePair fromCoordinates;
  private final CoordinatePair toCoordinates;

  public PermanentDepositOverview(Integer entityID,
                                  // this stores the enum name so decisions can be made easily when processing templates.
                                  Boolean depositIsForConsentedPipeline, MaterialType materialTypeLookup,
                                  String depositReference,
                                  List<String> pipelineRefs,
                                  Boolean depositIsForPipelinesOnOtherApp, String appRefAndPipelineNum,
                                  String fromDateEstimate,
                                  String toDateEstimate,
                                  StringWithTag materialType,
                                  String materialSize,
                                  Boolean groutBagsBioDegradable,
                                  String bioGroutBagsNotUsedDescription,
                                  String quantity,
                                  String contingencyAmount,
                                  CoordinatePair fromCoordinates,
                                  CoordinatePair toCoordinates) {
    this.entityID = entityID;
    this.depositIsForConsentedPipeline = depositIsForConsentedPipeline;
    this.materialTypeLookup = materialTypeLookup;
    this.depositReference = depositReference;
    this.pipelineRefs = pipelineRefs;
    this.depositIsForPipelinesOnOtherApp = depositIsForPipelinesOnOtherApp;
    this.appRefAndPipelineNum = appRefAndPipelineNum;
    this.fromDateEstimate = fromDateEstimate;
    this.toDateEstimate = toDateEstimate;
    this.materialType = materialType;
    this.materialSize = materialSize;
    this.groutBagsBioDegradable = groutBagsBioDegradable;
    this.bioGroutBagsNotUsedDescription = bioGroutBagsNotUsedDescription;
    this.quantity = quantity;
    this.contingencyAmount = contingencyAmount;
    this.fromCoordinates = fromCoordinates;
    this.toCoordinates = toCoordinates;
  }

  public Integer getEntityID() {
    return entityID;
  }

  public Boolean getDepositIsForConsentedPipeline() {
    return depositIsForConsentedPipeline;
  }

  public MaterialType getMaterialTypeLookup() {
    return materialTypeLookup;
  }

  public String getDepositReference() {
    return depositReference;
  }

  public Boolean getDepositIsForPipelinesOnOtherApp() {
    return depositIsForPipelinesOnOtherApp;
  }

  public String getAppRefAndPipelineNum() {
    return appRefAndPipelineNum;
  }

  public List<String> getPipelineRefs() {
    return pipelineRefs;
  }

  public String getFromDateEstimate() {
    return fromDateEstimate;
  }

  public String getToDateEstimate() {
    return toDateEstimate;
  }

  public StringWithTag getMaterialType() {
    return materialType;
  }

  public String getMaterialSize() {
    return materialSize;
  }

  public Boolean getGroutBagsBioDegradable() {
    return groutBagsBioDegradable;
  }

  public String getBioGroutBagsNotUsedDescription() {
    return bioGroutBagsNotUsedDescription;
  }

  public String getQuantity() {
    return quantity;
  }

  public String getContingencyAmount() {
    return contingencyAmount;
  }

  public CoordinatePair getFromCoordinates() {
    return fromCoordinates;
  }

  public CoordinatePair getToCoordinates() {
    return toCoordinates;
  }


}
