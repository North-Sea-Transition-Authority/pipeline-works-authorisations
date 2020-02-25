package uk.co.ogauthority.pwa.temp.model.view;

import java.io.Serializable;
import java.math.BigDecimal;

public class IdentView implements Serializable {

  private Integer identNo;

  private String from;

  private String fromLatitudeDegrees;
  private String fromLatitudeMinutes;
  private String fromLatitudeSeconds;

  private String fromLongitudeDegrees;
  private String fromLongitudeMinutes;
  private String fromLongitudeSeconds;

  private String to;

  private String toLatitudeDegrees;
  private String toLatitudeMinutes;
  private String toLatitudeSeconds;

  private String toLongitudeDegrees;
  private String toLongitudeMinutes;
  private String toLongitudeSeconds;

  private String componentParts;

  private BigDecimal length;

  private BigDecimal externalDiameter;
  private BigDecimal internalDiameter;
  private BigDecimal wallThickness;

  private String typeOfInsulationOrCoating;

  private BigDecimal maop;

  private String productsToBeConveyed;

  public IdentView() {
  }

  public Integer getIdentNo() {
    return identNo;
  }

  public void setIdentNo(Integer identNo) {
    this.identNo = identNo;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
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

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
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

  public String getComponentParts() {
    return componentParts;
  }

  public void setComponentParts(String componentParts) {
    this.componentParts = componentParts;
  }

  public BigDecimal getLength() {
    return length;
  }

  public void setLength(BigDecimal length) {
    this.length = length;
  }

  public BigDecimal getExternalDiameter() {
    return externalDiameter;
  }

  public void setExternalDiameter(BigDecimal externalDiameter) {
    this.externalDiameter = externalDiameter;
  }

  public BigDecimal getInternalDiameter() {
    return internalDiameter;
  }

  public void setInternalDiameter(BigDecimal internalDiameter) {
    this.internalDiameter = internalDiameter;
  }

  public BigDecimal getWallThickness() {
    return wallThickness;
  }

  public void setWallThickness(BigDecimal wallThickness) {
    this.wallThickness = wallThickness;
  }

  public String getTypeOfInsulationOrCoating() {
    return typeOfInsulationOrCoating;
  }

  public void setTypeOfInsulationOrCoating(String typeOfInsulationOrCoating) {
    this.typeOfInsulationOrCoating = typeOfInsulationOrCoating;
  }

  public BigDecimal getMaop() {
    return maop;
  }

  public void setMaop(BigDecimal maop) {
    this.maop = maop;
  }

  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }

  public void setProductsToBeConveyed(String productsToBeConveyed) {
    this.productsToBeConveyed = productsToBeConveyed;
  }
}
