package uk.co.ogauthority.pwa.model.form.pwaapplications.shared;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;


public class PermanentDepositsForm extends UploadMultipleFilesWithDescriptionForm {


  private String selectedPipelines;
  private Integer fromMonth;
  private Integer fromYear;
  private Integer toMonth;
  private Integer toYear;

  private MaterialType materialType;

  private Integer rocksSize;
  private Integer groutBagsSize;
  private String otherMaterialSize;
  private Integer concreteMattressLength;
  private Integer concreteMattressWidth;
  private Integer concreteMattressDepth;

  private boolean groutBagsBioDegradable;
  private String bioGroutBagsNotUsedDescription;

  private String quantityConcrete;
  private String quantityRocks;
  private String quantityGroutBags;
  private String quantityOther;

  private String contingencyConcreteAmount;
  private String contingencyRocksAmount;
  private String contingencyGroutBagsAmount;
  private String contingencyOtherAmount;


  @NotNull
  private String fromLatitudeDegrees;
  @NotNull
  private String fromLatitudeMinutes;
  @NotNull
  private String fromLatitudeSeconds;

  @NotNull
  private String fromLongitudeDegrees;
  @NotNull
  private String fromLongitudeMinutes;
  @NotNull
  private String fromLongitudeSeconds;
  @NotNull
  private String fromLongitudeDirection;

  @NotNull
  private String toLatitudeDegrees;
  @NotNull
  private String toLatitudeMinutes;
  @NotNull
  private String toLatitudeSeconds;

  @NotNull
  private String toLongitudeDegrees;
  @NotNull
  private String toLongitudeMinutes;
  @NotNull
  private String toLongitudeSeconds;
  @NotNull
  private String toLongitudeDirection;


  public String getSelectedPipelines() {
    return selectedPipelines;
  }

  public void setSelectedPipelines(String selectedPipelines) {
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

  public Integer getRocksSize() {
    return rocksSize;
  }

  public void setRocksSize(Integer rocksSize) {
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

  public boolean isGroutBagsBioDegradable() {
    return groutBagsBioDegradable;
  }

  public void setGroutBagsBioDegradable(boolean groutBagsBioDegradable) {
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

  public String getFromLatitudeDegrees() {
    return fromLatitudeDegrees;
  }

  public void setFromLatitudeDegrees(String fromLatitudeDegrees) {
    this.fromLatitudeDegrees = fromLatitudeDegrees;
  }

  public String getFromLatitudeMinutes() {
    return fromLatitudeMinutes;
  }

  public void setFromLatitudeMinutes(String fromLatitudeMinutes) {
    this.fromLatitudeMinutes = fromLatitudeMinutes;
  }

  public String getFromLatitudeSeconds() {
    return fromLatitudeSeconds;
  }

  public void setFromLatitudeSeconds(String fromLatitudeSeconds) {
    this.fromLatitudeSeconds = fromLatitudeSeconds;
  }

  public String getFromLongitudeDegrees() {
    return fromLongitudeDegrees;
  }

  public void setFromLongitudeDegrees(String fromLongitudeDegrees) {
    this.fromLongitudeDegrees = fromLongitudeDegrees;
  }

  public String getFromLongitudeMinutes() {
    return fromLongitudeMinutes;
  }

  public void setFromLongitudeMinutes(String fromLongitudeMinutes) {
    this.fromLongitudeMinutes = fromLongitudeMinutes;
  }

  public String getFromLongitudeSeconds() {
    return fromLongitudeSeconds;
  }

  public void setFromLongitudeSeconds(String fromLongitudeSeconds) {
    this.fromLongitudeSeconds = fromLongitudeSeconds;
  }

  public String getFromLongitudeDirection() {
    return fromLongitudeDirection;
  }

  public void setFromLongitudeDirection(String fromLongitudeDirection) {
    this.fromLongitudeDirection = fromLongitudeDirection;
  }

  public String getToLatitudeDegrees() {
    return toLatitudeDegrees;
  }

  public void setToLatitudeDegrees(String toLatitudeDegrees) {
    this.toLatitudeDegrees = toLatitudeDegrees;
  }

  public String getToLatitudeMinutes() {
    return toLatitudeMinutes;
  }

  public void setToLatitudeMinutes(String toLatitudeMinutes) {
    this.toLatitudeMinutes = toLatitudeMinutes;
  }

  public String getToLatitudeSeconds() {
    return toLatitudeSeconds;
  }

  public void setToLatitudeSeconds(String toLatitudeSeconds) {
    this.toLatitudeSeconds = toLatitudeSeconds;
  }

  public String getToLongitudeDegrees() {
    return toLongitudeDegrees;
  }

  public void setToLongitudeDegrees(String toLongitudeDegrees) {
    this.toLongitudeDegrees = toLongitudeDegrees;
  }

  public String getToLongitudeMinutes() {
    return toLongitudeMinutes;
  }

  public void setToLongitudeMinutes(String toLongitudeMinutes) {
    this.toLongitudeMinutes = toLongitudeMinutes;
  }

  public String getToLongitudeSeconds() {
    return toLongitudeSeconds;
  }

  public void setToLongitudeSeconds(String toLongitudeSeconds) {
    this.toLongitudeSeconds = toLongitudeSeconds;
  }

  public String getToLongitudeDirection() {
    return toLongitudeDirection;
  }

  public void setToLongitudeDirection(String toLongitudeDirection) {
    this.toLongitudeDirection = toLongitudeDirection;
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
    return groutBagsBioDegradable == that.groutBagsBioDegradable
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
        && Objects.equals(bioGroutBagsNotUsedDescription, that.bioGroutBagsNotUsedDescription)
        && Objects.equals(quantityConcrete, that.quantityConcrete)
        && Objects.equals(quantityRocks, that.quantityRocks)
        && Objects.equals(quantityGroutBags, that.quantityGroutBags)
        && Objects.equals(quantityOther, that.quantityOther)
        && Objects.equals(contingencyConcreteAmount, that.contingencyConcreteAmount)
        && Objects.equals(contingencyRocksAmount, that.contingencyRocksAmount)
        && Objects.equals(contingencyGroutBagsAmount, that.contingencyGroutBagsAmount)
        && Objects.equals(contingencyOtherAmount, that.contingencyOtherAmount)
        && Objects.equals(fromLatitudeDegrees, that.fromLatitudeDegrees)
        && Objects.equals(fromLatitudeMinutes, that.fromLatitudeMinutes)
        && Objects.equals(fromLatitudeSeconds, that.fromLatitudeSeconds)
        && Objects.equals(fromLongitudeDegrees, that.fromLongitudeDegrees)
        && Objects.equals(fromLongitudeMinutes, that.fromLongitudeMinutes)
        && Objects.equals(fromLongitudeSeconds, that.fromLongitudeSeconds)
        && Objects.equals(fromLongitudeDirection, that.fromLongitudeDirection)
        && Objects.equals(toLatitudeDegrees, that.toLatitudeDegrees)
        && Objects.equals(toLatitudeMinutes, that.toLatitudeMinutes)
        && Objects.equals(toLatitudeSeconds, that.toLatitudeSeconds)
        && Objects.equals(toLongitudeDegrees, that.toLongitudeDegrees)
        && Objects.equals(toLongitudeMinutes, that.toLongitudeMinutes)
        && Objects.equals(toLongitudeSeconds, that.toLongitudeSeconds)
        && Objects.equals(toLongitudeDirection, that.toLongitudeDirection);
  }

  @Override
  public int hashCode() {
    return Objects.hash(selectedPipelines, fromMonth, fromYear, toMonth, toYear, materialType, rocksSize, groutBagsSize, otherMaterialSize,
        concreteMattressLength, concreteMattressWidth, concreteMattressDepth, groutBagsBioDegradable, bioGroutBagsNotUsedDescription,
        quantityConcrete, quantityRocks, quantityGroutBags, quantityOther, contingencyConcreteAmount, contingencyRocksAmount,
        contingencyGroutBagsAmount, contingencyOtherAmount, fromLatitudeDegrees, fromLatitudeMinutes, fromLatitudeSeconds,
        fromLongitudeDegrees, fromLongitudeMinutes, fromLongitudeSeconds, fromLongitudeDirection, toLatitudeDegrees, toLatitudeMinutes,
        toLatitudeSeconds, toLongitudeDegrees, toLongitudeMinutes, toLongitudeSeconds, toLongitudeDirection);
  }
}
