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
  KILOGRAM("kg", "kilograms"),
  METRE("m", "metre"),
  MILLIMETRE("mm", "millimetre"),
  MICROGRAM_METRE_CUBED("μg/m³", "in micrograms per metre cubed"),
  PARTS_PER_MILLION("ppm", "in parts per million"),
  KSCM_D("kscm/d", "in kilo standard cubic meters per hour"),
  WM2K("W/m²K", "in watts per metre square Kelvin"),
  BAR_G("barg", "in gauge pressure"),
  MULTIPLICATION_SYMBOL("×", "times"),
  ROCK_GRADE("grade", "grade"),
  CENTIPOISE("cP", "in centipoise"),

  MTONNE_YEAR("Mtpa", "Million tonnes per annum");

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
