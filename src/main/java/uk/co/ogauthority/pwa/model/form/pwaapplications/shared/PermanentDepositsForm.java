package uk.co.ogauthority.pwa.model.form.pwaapplications.shared;

import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotNull;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;


public class PermanentDepositsForm extends UploadMultipleFilesWithDescriptionForm {

  private Integer entityID;
  private String depositReference;
  private Set<String> selectedPipelines;
  private Integer fromMonth;
  private Integer fromYear;
  private Integer toMonth;
  private Integer toYear;

  private MaterialType materialType;

  private String rocksSize;
  private Integer groutBagsSize;
  private String otherMaterialSize;
  private Integer concreteMattressLength;
  private Integer concreteMattressWidth;
  private Integer concreteMattressDepth;

  private Boolean groutBagsBioDegradable;
  private String bioGroutBagsNotUsedDescription;

  private String quantityConcrete;
  private String quantityRocks;
  private String quantityGroutBags;
  private String quantityOther;

  private String contingencyConcreteAmount;
  private String contingencyRocksAmount;
  private String contingencyGroutBagsAmount;
  private String contingencyOtherAmount;

  private CoordinateForm fromCoordinateForm;
  private CoordinateForm toCoordinateForm;


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

  public Integer getFromMonth() {
    return fromMonth;
  }

  public void setFromMonth(Integer fromMonth) {
    this.fromMonth = fromMonth;
  }

  public Integer getFromYear() {
    return fromYear;
  }

  public void setFromYear(Integer fromYear) {
    this.fromYear = fromYear;
  }

  public Integer getToMonth() {
    return toMonth;
  }

  public void setToMonth(Integer toMonth) {
    this.toMonth = toMonth;
  }

  public Integer getToYear() {
    return toYear;
  }

  public void setToYear(Integer toYear) {
    this.toYear = toYear;
  }

  public MaterialType getMaterialType() {
    return materialType;
  }

  public void setMaterialType(MaterialType materialType) {
    this.materialType = materialType;
  }

  public String getRocksSize() {
    return rocksSize;
  }

  public void setRocksSize(String rocksSize) {
    this.rocksSize = rocksSize;
  }

  public Integer getGroutBagsSize() {
    return groutBagsSize;
  }

  public void setGroutBagsSize(Integer groutBagsSize) {
    this.groutBagsSize = groutBagsSize;
  }

  public String getOtherMaterialSize() {
    return otherMaterialSize;
  }

  public void setOtherMaterialSize(String otherMaterialSize) {
    this.otherMaterialSize = otherMaterialSize;
  }

  public Integer getConcreteMattressLength() {
    return concreteMattressLength;
  }

  public void setConcreteMattressLength(Integer concreteMattressLength) {
    this.concreteMattressLength = concreteMattressLength;
  }

  public Integer getConcreteMattressWidth() {
    return concreteMattressWidth;
  }

  public void setConcreteMattressWidth(Integer concreteMattressWidth) {
    this.concreteMattressWidth = concreteMattressWidth;
  }

  public Integer getConcreteMattressDepth() {
    return concreteMattressDepth;
  }

  public void setConcreteMattressDepth(Integer concreteMattressDepth) {
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

  public String getQuantityConcrete() {
    return quantityConcrete;
  }

  public void setQuantityConcrete(String quantityConcrete) {
    this.quantityConcrete = quantityConcrete;
  }

  public String getQuantityRocks() {
    return quantityRocks;
  }

  public void setQuantityRocks(String quantityRocks) {
    this.quantityRocks = quantityRocks;
  }

  public String getQuantityGroutBags() {
    return quantityGroutBags;
  }

  public void setQuantityGroutBags(String quantityGroutBags) {
    this.quantityGroutBags = quantityGroutBags;
  }

  public String getQuantityOther() {
    return quantityOther;
  }

  public void setQuantityOther(String quantityOther) {
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
        && Objects.equals(depositReference, that.depositReference)
        && Objects.equals(selectedPipelines, that.selectedPipelines)
        && Objects.equals(fromMonth, that.fromMonth)
        && Objects.equals(fromYear, that.fromYear)
        && Objects.equals(toMonth, that.toMonth)
        && Objects.equals(toYear, that.toYear)
        && materialType == that.materialType
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
        && Objects.equals(toCoordinateForm, that.toCoordinateForm);
  }

  @Override
  public int hashCode() {
    return Objects.hash(entityID, depositReference, selectedPipelines, fromMonth, fromYear,
        toMonth, toYear, materialType, rocksSize, groutBagsSize,
        otherMaterialSize, concreteMattressLength, concreteMattressWidth, concreteMattressDepth, groutBagsBioDegradable,
        bioGroutBagsNotUsedDescription, quantityConcrete, quantityRocks, quantityGroutBags, quantityOther,
        contingencyConcreteAmount, contingencyRocksAmount, contingencyGroutBagsAmount,
        contingencyOtherAmount, fromCoordinateForm, toCoordinateForm);
  }
}
