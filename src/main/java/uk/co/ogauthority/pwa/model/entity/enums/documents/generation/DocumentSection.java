package uk.co.ogauthority.pwa.model.entity.enums.documents.generation;

import uk.co.ogauthority.pwa.service.documents.generation.AdmiraltyChartGeneratorService;
import uk.co.ogauthority.pwa.service.documents.generation.DepositDrawingsGeneratorService;
import uk.co.ogauthority.pwa.service.documents.generation.DepositsGeneratorService;
import uk.co.ogauthority.pwa.service.documents.generation.DocumentSectionGenerator;
import uk.co.ogauthority.pwa.service.documents.generation.HuooGeneratorService;
import uk.co.ogauthority.pwa.service.documents.generation.Schedule1GeneratorService;
import uk.co.ogauthority.pwa.service.documents.generation.Schedule2GeneratorService;
import uk.co.ogauthority.pwa.service.documents.generation.TableAGeneratorService;

public enum DocumentSection {

  SCHEDULE_1("Schedule 1", SectionType.CLAUSE_LIST, Schedule1GeneratorService.class),

  SCHEDULE_2("Schedule 2", SectionType.CLAUSE_LIST, Schedule2GeneratorService.class),

  HUOO("HUOOs", HuooGeneratorService.class),

  TABLE_A("Table As", TableAGeneratorService.class),

  DEPOSITS("Deposits", DepositsGeneratorService.class),

  DEPOSIT_DRAWINGS("Deposit drawings", DepositDrawingsGeneratorService.class),

  ADMIRALTY_CHART("Admiralty chart", AdmiraltyChartGeneratorService.class);

  private final String displayName;
  private final SectionType sectionType;
  private final Class<? extends DocumentSectionGenerator> sectionGenerator;

  DocumentSection(String displayName,
                  Class<? extends DocumentSectionGenerator> sectionGenerator) {
    this.displayName = displayName;
    this.sectionGenerator = sectionGenerator;
    this.sectionType = SectionType.CUSTOM;
  }

  DocumentSection(String displayName,
                  SectionType sectionType,
                  Class<? extends DocumentSectionGenerator> sectionGenerator) {
    this.displayName = displayName;
    this.sectionType = sectionType;
    this.sectionGenerator = sectionGenerator;
  }

  public String getDisplayName() {
    return displayName;
  }

  public SectionType getSectionType() {
    return sectionType;
  }

  public Class<? extends DocumentSectionGenerator> getSectionGenerator() {
    return sectionGenerator;
  }
}
