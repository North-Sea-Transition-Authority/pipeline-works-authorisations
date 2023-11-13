package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical;

import static uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType.CCUS;
import static uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType.HYDROGEN;
import static uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType.PETROLEUM;
import static uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType.MOLE_PERCENTAGE;
import static uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType.NONE;
import static uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType.PPMV_100;
import static uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType.PPMV_10K;
import static uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType.PPMV_20K;
import static uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType.PPMV_40K;
import static uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType.PPMV_5;
import static uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType.PPMV_50;
import static uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType.PPMV_50K;
import static uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.chemical.ChemicalMeasurementType.TRACE;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;

public enum Chemical {
  CO2(
      "CO₂",
      10,
      Map.of(
          PETROLEUM, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          CCUS, List.of(MOLE_PERCENTAGE, NONE)
      )),
  H2(
      "H₂",
      20,
      Map.of(
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          CCUS, List.of(PPMV_10K, NONE)
      )),
  O2(
      "O₂",
      30,
      Map.of(
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          CCUS, List.of(PPMV_100, NONE)
      )),
  H2S(
      "H₂S",
      40,
      Map.of(
          PETROLEUM, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          CCUS, List.of(PPMV_50, NONE)
      )),

  H2O(
      "H₂O",
      50,
      Map.of(
          PETROLEUM, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          CCUS, List.of(PPMV_100, NONE)
      )),
  N2(
      "N₂",
      60,
      Map.of(
          PETROLEUM, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          CCUS, List.of(PPMV_40K, NONE)
      )),
  C1(
      "C1",
      70,
      Map.of(
          PETROLEUM, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE)
      )),
  C2(
      "C2",
      80,
      Map.of(
          PETROLEUM, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE)
      )),
  C3(
      "C3",
      90,
      Map.of(
          PETROLEUM, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE)
      )),
  C4(
      "C4",
      100,
      Map.of(
          PETROLEUM, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE)
      )),
  C5(
      "C5",
      110,
      Map.of(
          PETROLEUM, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE)
      )),
  C6(
      "C6",
      120,
      Map.of(
      PETROLEUM, List.of(MOLE_PERCENTAGE, TRACE, NONE),
      HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE)
      )),
  C7(
      "C7",
      130,
      Map.of(
          PETROLEUM, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE)
      )),
  C8(
      "C8",
      140,
      Map.of(
          PETROLEUM, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE)
      )),
  C9(
      "C9",
      150,
      Map.of(
          PETROLEUM, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE)
      )),
  C10(
      "C10",
      160,
      Map.of(
          PETROLEUM, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE)
      )),
  C11(
      "C11",
      170,
      Map.of(
          PETROLEUM, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE)
      )),
  C12_PLUS(
      "C12+",
      180,
      Map.of(
          PETROLEUM, List.of(MOLE_PERCENTAGE, TRACE, NONE),
          HYDROGEN, List.of(MOLE_PERCENTAGE, TRACE, NONE)
      )),
  AR(
      "Ar",
      190,
      Map.of(
          CCUS, List.of(PPMV_40K, NONE)
      )),
  CO(
      "CO",
      200,
      Map.of(
          CCUS, List.of(PPMV_20K, NONE)
      )),
  SOX(
      "SOx",
      210,
      Map.of(
          CCUS, List.of(PPMV_100, NONE)
      )),
  NOX(
      "NOx",
      220,
      Map.of(
          CCUS, List.of(PPMV_100, NONE)
      )),
  HG(
      "Hg",
      230,
      Map.of(
          CCUS, List.of(PPMV_5, NONE)
      )),
  HYDROCARBONS(
      "Hydrocarbons",
      240,
      Map.of(
          CCUS, List.of(PPMV_50K, NONE)
      )
  );

  private final String displayText;
  private final int displayOrder;

  private final Map<PwaResourceType, List<ChemicalMeasurementType>> applicableResourceTypes;

  Chemical(String displayText, int displayOrder, Map<PwaResourceType, List<ChemicalMeasurementType>> applicableResourceTypes) {
    this.displayText = displayText;
    this.displayOrder = displayOrder;
    this.applicableResourceTypes = applicableResourceTypes;
  }

  public String getDisplayText() {
    return displayText;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public List<PwaResourceType> getApplicableResourceTypes() {
    return List.copyOf(applicableResourceTypes.keySet());
  }

  public List<ChemicalMeasurementType> getApplicableMeasurementTypes(PwaResourceType resourceType) {
    return applicableResourceTypes.get(resourceType);
  }

  public static List<Chemical> getAll() {
    return Arrays.asList(Chemical.values());
  }

  public static List<Chemical> getAllByResourceType(PwaResourceType resourceType) {
    return Arrays.stream(Chemical.values())
        .filter(chemical -> chemical.getApplicableResourceTypes().contains(resourceType))
        .collect(Collectors.toList());
  }
}
