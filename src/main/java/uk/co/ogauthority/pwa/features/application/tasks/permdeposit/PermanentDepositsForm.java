package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;


public class PermanentDepositsForm {

  private Integer entityID;
  private Boolean depositIsForConsentedPipeline;
  private String depositReference;
  private Set<String> selectedPipelines;
  private Boolean depositIsForPipelinesOnOtherApp;
  private String appRefAndPipelineNum;
  private TwoFieldDateInput fromDate;
  private TwoFieldDateInput toDate;

  private MaterialType materialType;
  private String otherMaterialType;

  private String rocksSize;
  private DecimalInput groutBagsSize;
  private String otherMaterialSize;
  private DecimalInput concreteMattressLength;
  private DecimalInput concreteMattressWidth;
  private DecimalInput concreteMattressDepth;

  private Boolean groutBagsBioDegradable;
  private String bioGroutBagsNotUsedDescription;

  private DecimalInput quantityConcrete;
  private DecimalInput quantityRocks;
  private DecimalInput quantityGroutBags;
  private DecimalInput quantityOther;

  private String contingencyConcreteAmount;
  private String contingencyRocksAmount;
  private String contingencyGroutBagsAmount;
  private String contingencyOtherAmount;

  private CoordinateForm fromCoordinateForm;
  private CoordinateForm toCoordinateForm;

  private String footnote;


  public Integer getEntityID() {
    return entityID;
  }

  public void setEntityID(Integer entityID) {
    this.entityID = entityID;
  }

  public String getDepositReference() {
    return depositReference;
  }

  public void setDepositReference(String depositReference) {
    this.depositReference = depositReference;
  }

  public Set<String> getSelectedPipelines() {
    return selectedPipelines;
  }

  public void setSelectedPipelines(Set<String> selectedPipelines) {
    this.selectedPipelines = selectedPipelines;
  }

  public TwoFieldDateInput getFromDate() {
    return fromDate;
  }

  public void setFromDate(TwoFieldDateInput fromDate) {
    this.fromDate = fromDate;
  }

  public TwoFieldDateInput getToDate() {
    return toDate;
  }

  public void setToDate(TwoFieldDateInput toDate) {
    this.toDate = toDate;
  }

  public MaterialType getMaterialType() {
    return materialType;
  }

  public void setMaterialType(MaterialType materialType) {
    this.materialType = materialType;
  }

  public String getOtherMaterialType() {
    return otherMaterialType;
  }

  public void setOtherMaterialType(String otherMaterialType) {
    this.otherMaterialType = otherMaterialType;
  }

  public String getRocksSize() {
    return rocksSize;
  }

  public void setRocksSize(String rocksSize) {
    this.rocksSize = rocksSize;
  }

  public DecimalInput getGroutBagsSize() {
    return groutBagsSize;
  }

  public void setGroutBagsSize(DecimalInput groutBagsSize) {
    this.groutBagsSize = groutBagsSize;
  }

  public String getOtherMaterialSize() {
    return otherMaterialSize;
  }

  public void setOtherMaterialSize(String otherMaterialSize) {
    this.otherMaterialSize = otherMaterialSize;
  }

  public DecimalInput getConcreteMattressLength() {
    return concreteMattressLength;
  }

  public void setConcreteMattressLength(DecimalInput concreteMattressLength) {
    this.concreteMattressLength = concreteMattressLength;
  }

  public DecimalInput getConcreteMattressWidth() {
    return concreteMattressWidth;
  }

  public void setConcreteMattressWidth(DecimalInput concreteMattressWidth) {
    this.concreteMattressWidth = concreteMattressWidth;
  }

  public DecimalInput getConcreteMattressDepth() {
    return concreteMattressDepth;
  }

  public void setConcreteMattressDepth(DecimalInput concreteMattressDepth) {
    this.concreteMattressDepth = concreteMattressDepth;
  }

  public Boolean getGroutBagsBioDegradable() {
    return groutBagsBioDegradable;
  }

  public void setGroutBagsBioDegradable(Boolean groutBagsBioDegradable) {
    this.groutBagsBioDegradable = groutBagsBioDegradable;
  }

  public String getBioGroutBagsNotUsedDescription() {
    return bioGroutBagsNotUsedDescription;
  }

  public void setBioGroutBagsNotUsedDescription(String bioGroutBagsNotUsedDescription) {
    this.bioGroutBagsNotUsedDescription = bioGroutBagsNotUsedDescription;
  }

  public DecimalInput getQuantityConcrete() {
    return quantityConcrete;
  }

  public void setQuantityConcrete(DecimalInput quantityConcrete) {
    this.quantityConcrete = quantityConcrete;
  }

  public DecimalInput getQuantityRocks() {
    return quantityRocks;
  }

  public void setQuantityRocks(DecimalInput quantityRocks) {
    this.quantityRocks = quantityRocks;
  }

  public DecimalInput getQuantityGroutBags() {
    return quantityGroutBags;
  }

  public void setQuantityGroutBags(DecimalInput quantityGroutBags) {
    this.quantityGroutBags = quantityGroutBags;
  }

  public DecimalInput getQuantityOther() {
    return quantityOther;
  }

  public void setQuantityOther(DecimalInput quantityOther) {
    this.quantityOther = quantityOther;
  }

  public String getContingencyConcreteAmount() {
    return contingencyConcreteAmount;
  }

  public void setContingencyConcreteAmount(String contingencyConcreteAmount) {
    this.contingencyConcreteAmount = contingencyConcreteAmount;
  }

  public String getContingencyRocksAmount() {
    return contingencyRocksAmount;
  }

  public void setContingencyRocksAmount(String contingencyRocksAmount) {
    this.contingencyRocksAmount = contingencyRocksAmount;
  }

  public String getContingencyGroutBagsAmount() {
    return contingencyGroutBagsAmount;
  }

  public void setContingencyGroutBagsAmount(String contingencyGroutBagsAmount) {
    this.contingencyGroutBagsAmount = contingencyGroutBagsAmount;
  }

  public String getContingencyOtherAmount() {
    return contingencyOtherAmount;
  }

  public void setContingencyOtherAmount(String contingencyOtherAmount) {
    this.contingencyOtherAmount = contingencyOtherAmount;
  }

  public CoordinateForm getFromCoordinateForm() {
    return fromCoordinateForm;
  }

  public void setFromCoordinateForm(CoordinateForm fromCoordinateForm) {
    this.fromCoordinateForm = fromCoordinateForm;
  }

  public CoordinateForm getToCoordinateForm() {
    return toCoordinateForm;
  }

  public void setToCoordinateForm(CoordinateForm toCoordinateForm) {
    this.toCoordinateForm = toCoordinateForm;
  }

  public Boolean getDepositIsForConsentedPipeline() {
    return depositIsForConsentedPipeline;
  }

  public void setDepositIsForConsentedPipeline(Boolean depositIsForConsentedPipeline) {
    this.depositIsForConsentedPipeline = depositIsForConsentedPipeline;
  }

  public Boolean getDepositIsForPipelinesOnOtherApp() {
    return depositIsForPipelinesOnOtherApp;
  }

  public void setDepositIsForPipelinesOnOtherApp(Boolean depositIsForPipelinesOnOtherApp) {
    this.depositIsForPipelinesOnOtherApp = depositIsForPipelinesOnOtherApp;
  }

  public String getAppRefAndPipelineNum() {
    return appRefAndPipelineNum;
  }

  public void setAppRefAndPipelineNum(String appRefAndPipelineNum) {
    this.appRefAndPipelineNum = appRefAndPipelineNum;
  }

  public String getFootnote() {
    return footnote;
  }

  public void setFootnote(String footnote) {
    this.footnote = footnote;
  }




  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PermanentDepositsForm that = (PermanentDepositsForm) o;
    return Objects.equals(entityID, that.entityID)
        && Objects.equals(depositIsForConsentedPipeline, that.depositIsForConsentedPipeline)
        && Objects.equals(depositReference, that.depositReference)
        && Objects.equals(selectedPipelines, that.selectedPipelines)
        && Objects.equals(depositIsForPipelinesOnOtherApp, that.depositIsForPipelinesOnOtherApp)
        && Objects.equals(appRefAndPipelineNum, that.appRefAndPipelineNum)
        && Objects.equals(fromDate, that.fromDate)
        && Objects.equals(toDate, that.toDate)
        && materialType == that.materialType
        && Objects.equals(otherMaterialType, that.otherMaterialType)
        && Objects.equals(rocksSize, that.rocksSize)
        && Objects.equals(groutBagsSize, that.groutBagsSize)
        && Objects.equals(otherMaterialSize, that.otherMaterialSize)
        && Objects.equals(concreteMattressLength, that.concreteMattressLength)
        && Objects.equals(concreteMattressWidth, that.concreteMattressWidth)
        && Objects.equals(concreteMattressDepth, that.concreteMattressDepth)
        && Objects.equals(groutBagsBioDegradable, that.groutBagsBioDegradable)
        && Objects.equals(bioGroutBagsNotUsedDescription, that.bioGroutBagsNotUsedDescription)
        && Objects.equals(quantityConcrete, that.quantityConcrete)
        && Objects.equals(quantityRocks, that.quantityRocks)
        && Objects.equals(quantityGroutBags, that.quantityGroutBags)
        && Objects.equals(quantityOther, that.quantityOther)
        && Objects.equals(contingencyConcreteAmount, that.contingencyConcreteAmount)
        && Objects.equals(contingencyRocksAmount, that.contingencyRocksAmount)
        && Objects.equals(contingencyGroutBagsAmount, that.contingencyGroutBagsAmount)
        && Objects.equals(contingencyOtherAmount, that.contingencyOtherAmount)
        && Objects.equals(fromCoordinateForm, that.fromCoordinateForm)
        && Objects.equals(toCoordinateForm, that.toCoordinateForm)
        && Objects.equals(footnote, that.footnote);
  }

  @Override
  public int hashCode() {
    return Objects.hash(entityID, depositIsForConsentedPipeline, depositReference, selectedPipelines,
        depositIsForPipelinesOnOtherApp, appRefAndPipelineNum, fromDate,
        toDate, materialType, otherMaterialType, rocksSize, groutBagsSize,
        otherMaterialSize, concreteMattressLength, concreteMattressWidth, concreteMattressDepth, groutBagsBioDegradable,
        bioGroutBagsNotUsedDescription, quantityConcrete, quantityRocks, quantityGroutBags, quantityOther,
        contingencyConcreteAmount, contingencyRocksAmount, contingencyGroutBagsAmount,
        contingencyOtherAmount, fromCoordinateForm, toCoordinateForm, footnote);
  }
}
