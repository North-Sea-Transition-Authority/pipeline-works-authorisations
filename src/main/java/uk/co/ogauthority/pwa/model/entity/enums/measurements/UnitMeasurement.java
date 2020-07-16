package uk.co.ogauthority.pwa.model.entity.enums.measurements;

import java.util.Arrays;
import java.util.Map;
import uk.co.ogauthority.pwa.util.StreamUtils;

public enum UnitMeasurement {

  PERCENTAGE_WEIGHT("weight %", "in percentage weight"),
  DEGREES_CELSIUS("°C", "in degrees Celsius"),
  ACID_NUMBER("< mg KOH/g", "of acid number"),
  BAR_A("bar(a)", "in absolute pressure"),
  KG_METRE_CUBED("kg/m³", "in kilograms per metre cubed"),
  MICROGRAM_METRE_CUBED("μg/m³", "in micrograms per metre cubed"),
  PARTS_PER_MILLION("ppm", "in parts per million"),
  KSCM_D("kscm/d", "in kilo standard cubic meters per hour"),
  WM2K("W/m2K", "in watts per metre square Kelvin"),
  BAR_G("barg", "in gauge pressure");
  private final String suffixDisplay;
  private final String suffixScreenReaderDisplay;

  UnitMeasurement(String suffixDisplay, String suffixScreenReaderDisplay) {
    this.suffixDisplay = suffixDisplay;
    this.suffixScreenReaderDisplay = suffixScreenReaderDisplay;
  }

  public String getSuffixDisplay() {
    return suffixDisplay;
  }

  public String getSuffixScreenReaderDisplay() {
    return suffixScreenReaderDisplay;
  }

  public static Map<String, UnitMeasurement> toMap() {
    return Arrays.stream(UnitMeasurement.values())
        .collect(StreamUtils.toLinkedHashMap(Enum::name, unitMeasurement -> unitMeasurement));
  }
}
