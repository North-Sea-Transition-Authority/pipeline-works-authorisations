package uk.co.ogauthority.pwa.features.datatypes.geojson;

public interface DecimalDegreesPoint {

  Double getLongitudeDecimalDegrees();

  Double getLatitudeDecimalDegrees();

  boolean longAndLatHaveValue();
}
