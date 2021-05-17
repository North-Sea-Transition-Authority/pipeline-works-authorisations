package uk.co.ogauthority.pwa.model.entity.enums.documents.generation;

import uk.co.ogauthority.pwa.service.documents.generation.AdmiraltyChartGeneratorService;
import uk.co.ogauthority.pwa.service.documents.generation.DepconIntroductionGeneratorService;
import uk.co.ogauthority.pwa.service.documents.generation.DepositDrawingsGeneratorService;
import uk.co.ogauthority.pwa.service.documents.generation.DepositsGeneratorService;
import uk.co.ogauthority.pwa.service.documents.generation.DocumentSectionGenerator;
import uk.co.ogauthority.pwa.service.documents.generation.HuooGeneratorService;
import uk.co.ogauthority.pwa.service.documents.generation.InitialIntroductionGeneratorService;
import uk.co.ogauthority.pwa.service.documents.generation.TableAGeneratorService;
import uk.co.ogauthority.pwa.service.documents.generation.VariationIntroductionGeneratorService;

public enum DocumentSection {

  INITIAL_INTRO("Introduction", InitialIntroductionGeneratorService.class, SectionType.OPENING_PARAGRAPH),

  INITIAL_TERMS_AND_CONDITIONS("Terms and conditions", SectionType.CLAUSE_LIST),

  DEPCON_INTRO("Introduction", DepconIntroductionGeneratorService.class, SectionType.OPENING_PARAGRAPH),

  VARIATION_INTRO("Introduction", VariationIntroductionGeneratorService.class, SectionType.OPENING_PARAGRAPH),

  HUOO("Schedule 1", HuooGeneratorService.class),

  SCHEDULE_2("Schedule 2", SectionType.CLAUSE_LIST, ClauseDisplay.SHOW_HEADING),

  TABLE_A("Table As", TableAGeneratorService.class),

  DEPOSITS("Deposits", DepositsGeneratorService.class),

  DEPOSIT_DRAWINGS("Deposit drawings", DepositDrawingsGeneratorService.class),

  ADMIRALTY_CHART("Admiralty chart", AdmiraltyChartGeneratorService.class);

  private final String displayName;
  private final SectionType sectionType;
  private final ClauseDisplay clauseDisplay;
  private final Class<? extends DocumentSectionGenerator> sectionGenerator;

  DocumentSection(String displayName,
                  Class<? extends DocumentSectionGenerator> sectionGenerator) {
    this.displayName = displayName;
    this.sectionType = SectionType.CUSTOM;
    this.clauseDisplay = ClauseDisplay.HIDE_HEADING;
    this.sectionGenerator = sectionGenerator;
  }

  DocumentSection(String displayName,
                  Class<? extends DocumentSectionGenerator> sectionGenerator,
                  SectionType sectionType) {
    this.displayName = displayName;
    this.sectionType = sectionType;
    this.clauseDisplay = ClauseDisplay.HIDE_HEADING;
    this.sectionGenerator = sectionGenerator;
  }

  DocumentSection(String displayName,
                  SectionType sectionType) {
    this.displayName = displayName;
    this.sectionType = sectionType;
    this.clauseDisplay = ClauseDisplay.HIDE_HEADING;
    this.sectionGenerator = null;
  }

  DocumentSection(String displayName,
                  SectionType sectionType,
                  ClauseDisplay clauseDisplay) {
    this.displayName = displayName;
    this.sectionType = sectionType;
    this.clauseDisplay = clauseDisplay;
    this.sectionGenerator = null;
  }

  public String getDisplayName() {
    return displayName;
  }

  public SectionType getSectionType() {
    return sectionType;
  }

  public ClauseDisplay getClauseDisplay() {
    return clauseDisplay;
  }

  public Class<? extends DocumentSectionGenerator> getSectionGenerator() {
    return sectionGenerator;
  }
}
