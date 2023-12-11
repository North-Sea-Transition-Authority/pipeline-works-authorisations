package uk.co.ogauthority.pwa.service.documents;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.documents.SectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.documents.view.SectionClauseVersionView;
import uk.co.ogauthority.pwa.model.documents.view.SectionView;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryViewService;
import uk.co.ogauthority.pwa.service.documents.templates.DocumentTemplateService;
import uk.co.ogauthority.pwa.service.documents.templates.TemplateDocumentSource;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.SectionClauseVersionDtoTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class DocumentViewServiceTest {

  @Mock
  private Clock clock;

  private DocumentViewService documentViewService;

  private final Person person = PersonTestUtil.createDefaultPerson();

  @Before
  public void setUp() throws Exception {

    documentViewService = new DocumentViewService();

  }

  @Test
  public void getDocumentView_multipleSections_withMaxNestingLevels_FromInstance() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var docSpec = DocumentSpec.getSpecForApplication(detail.getPwaApplicationType(), detail.getResourceType());

    var clauseDtos = SectionClauseVersionDtoTestUtils
        .getInstanceSectionClauseVersionDtoList(1, docSpec, clock, person, 2, 3, 3)
        .stream()
        .map(SectionClauseVersionDto.class::cast)
        .collect(Collectors.toList());

    var docView = documentViewService.createDocumentView(PwaDocumentType.INSTANCE, detail.getPwaApplication(), clauseDtos);

    verifyDocViewAndClauses(docView, clauseDtos);

  }

  @Test
  public void getDocumentView_multipleSections_withMaxNestingLevels_FromTemplate() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var docSpec = DocumentSpec.getSpecForApplication(detail.getPwaApplicationType(), detail.getResourceType());

    var clauseDtos = SectionClauseVersionDtoTestUtils
        .getTemplateSectionClauseVersionDtoList(1, docSpec, clock, person, 2, 3, 3)
        .stream()
        .map(SectionClauseVersionDto.class::cast)
        .collect(Collectors.toList());

    var docSource = new TemplateDocumentSource(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT);

    var docView = documentViewService.createDocumentView(PwaDocumentType.TEMPLATE, docSource, clauseDtos);

    verifyDocViewAndClauses(docView, clauseDtos);

  }

  @Test
  public void getDocumentView_buildSidebarTopLink_fromPwaApplication() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var clauseDtos = SectionClauseVersionDtoTestUtils
        .getDefaultTemplateSectionClauseVersionDto(DocumentSection.INITIAL_INTRO, 1)
        .stream()
        .map(SectionClauseVersionDto.class::cast)
        .collect(Collectors.toList());

    var docSource = detail.getPwaApplication();

    var docView = documentViewService.createDocumentView(PwaDocumentType.INSTANCE, docSource, clauseDtos);

    assertThat(docView.getSections()).isNotEmpty();
    assertThat(docView.getSections().get(0).getSidebarSectionLinks()).isNotEmpty();
    var firstSideBarLink = docView.getSections().get(0).getSidebarSectionLinks().get(0);

    assertThat(firstSideBarLink.getDisplayText()).isEqualTo(docSource.getAppReference());
    assertThat(firstSideBarLink.getIsAnchorLink()).isTrue();
    assertThat(firstSideBarLink.getLink()).isEqualTo("#" + CaseSummaryViewService.CASE_SUMMARY_HEADER_ID);

  }

  @Test
  public void getDocumentView_buildSidebarTopLink_fromTemplateDocument() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var clauseDtos = SectionClauseVersionDtoTestUtils
        .getDefaultTemplateSectionClauseVersionDto(DocumentSection.INITIAL_INTRO, 1)
        .stream()
        .map(SectionClauseVersionDto.class::cast)
        .collect(Collectors.toList());

    var docSource = new TemplateDocumentSource(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT);

    var docView = documentViewService.createDocumentView(PwaDocumentType.TEMPLATE, docSource, clauseDtos);

    assertThat(docView.getSections()).isNotEmpty();
    assertThat(docView.getSections().get(0).getSidebarSectionLinks()).isNotEmpty();
    var firstSideBarLink = docView.getSections().get(0).getSidebarSectionLinks().get(0);

    assertThat(firstSideBarLink.getDisplayText()).isEqualTo(docSource.getDocumentSpec().getDisplayName());
    assertThat(firstSideBarLink.getIsAnchorLink()).isTrue();
    assertThat(firstSideBarLink.getLink()).isEqualTo("#" + DocumentTemplateService.DOC_TEMPLATE_EDITOR_HEADER_ID);

  }

  @Test
  public void getDocumentView_buildSidebarTopLink_topLinkOnlyIncludedInFirstSection() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var docSource = detail.getPwaApplication();
    var clauseDtos = new ArrayList<SectionClauseVersionDto>();

    var documentSections = List.of(DocumentSection.INITIAL_INTRO,
        DocumentSection.INITIAL_TERMS_AND_CONDITIONS,
        DocumentSection.HUOO,
        DocumentSection.DEPOSITS);

    documentSections.forEach(documentSection -> clauseDtos.addAll(
        SectionClauseVersionDtoTestUtils
            .getDefaultTemplateSectionClauseVersionDto(documentSection, 1)
            .stream()
            .map(SectionClauseVersionDto.class::cast)
            .collect(Collectors.toList())));

    var docView = documentViewService.createDocumentView(PwaDocumentType.INSTANCE, docSource, clauseDtos);


    //assert first section top link is the app ref link
    assertThat(docView.getSections()).hasSizeGreaterThan(1);
    assertThat(docView.getSections().get(0).getSidebarSectionLinks()).isNotEmpty();
    var firstSectionTopSideBarLink = docView.getSections().get(0).getSidebarSectionLinks().get(0);

    assertThat(firstSectionTopSideBarLink.getDisplayText()).isEqualTo(docSource.getAppReference());
    assertThat(firstSectionTopSideBarLink.getIsAnchorLink()).isTrue();
    assertThat(firstSectionTopSideBarLink.getLink()).isEqualTo("#" + CaseSummaryViewService.CASE_SUMMARY_HEADER_ID);


    //assert that from the second section onwards there are no appRef links as this should be only in the first section
    for (var x = 1; x < docView.getSections().size(); x++) {

      var section = docView.getSections().get(x);

      assertThat(section.getSidebarSectionLinks())
          .isNotEmpty()
          .allSatisfy(link -> assertThat(link.getDisplayText()).isNotEqualTo(docSource.getAppReference()));

    }
  }


  private void verifyDocViewAndClauses(DocumentView docView, Collection<SectionClauseVersionDto> clauseDtos) {

    clauseDtos.forEach(version -> {

      // check that each clause returned from the query is present in the docview in the expected place (right level etc)
      var docViewClause = findClause(docView, version);
      assertThat(docViewClause).isNotNull();

      // check that the doc view version of the clause has all of the right data and that data is equal to the query clause
      var nullFieldsList = new ArrayList<String>();
      if (version.getParentClauseId() == null) {
        nullFieldsList.add("parentClauseId");
      }
      ObjectTestUtils.assertAllExpectedFieldsHaveValue(docViewClause, nullFieldsList);

      assertThat(docViewClause.getId()).isEqualTo(version.getVersionId());
      assertThat(docViewClause.getClauseId()).isEqualTo(version.getClauseId());
      assertThat(docViewClause.getLevelNumber()).isEqualTo(version.getLevelNumber());
      assertThat(docViewClause.getLevelOrder()).isEqualTo(version.getLevelOrder());
      assertThat(docViewClause.getName()).isEqualTo(version.getName());
      assertThat(docViewClause.getText()).isEqualTo(version.getText());
      assertThat(docViewClause.getParentClauseId()).isEqualTo(version.getParentClauseId());

      // check that there is a sidebar link for the clause (as long as it is not level 3)
      var sidebarLinkOptional = findSidebarLink(docView, version);

      if (docViewClause.getLevelNumber() == 3) {
        assertThat(sidebarLinkOptional).isEmpty();
      } else {
        assertThat(sidebarLinkOptional).isPresent();

        var sidebarLink = sidebarLinkOptional.get();

        assertThat(sidebarLink).isNotNull();

        assertThat(sidebarLink.getDisplayText()).isEqualTo(docViewClause.getName());
        assertThat(sidebarLink.getIsAnchorLink()).isTrue();
        assertThat(sidebarLink.getLink()).isEqualTo("#clauseId-" + docViewClause.getClauseId());

      }

    });

    assertThat(docView.getSections().size()).isEqualTo(2);

  }

  private SectionClauseVersionView findClause(DocumentView documentView, SectionClauseVersionDto versionDto) {

    return documentView.getSections().stream()
        .filter(section -> section.getName().equals(DocumentSection.valueOf(versionDto.getSectionName()).getDisplayName()))
        .map(section -> {

          switch (versionDto.getLevelNumber()) {
            case 1:
              return section.getClauses().stream()
                  .filter(clause -> Objects.equals(clause.getId(), versionDto.getVersionId()))
                  .findFirst()
                  .orElseThrow();
            case 2:
              return section.getClauses().stream()
                  .flatMap(clause -> clause.getChildClauses().stream())
                  .filter(childClause -> Objects.equals(childClause.getId(), versionDto.getVersionId()))
                  .findFirst()
                  .orElseThrow();
            case 3:
              return section.getClauses().stream()
                  .flatMap(clause -> clause.getChildClauses().stream())
                  .flatMap(childClause -> childClause.getChildClauses().stream())
                  .filter(childChildClause -> Objects.equals(childChildClause.getId(), versionDto.getVersionId()))
                  .findFirst()
                  .orElseThrow();
          }

          return null;

        })
        .findFirst()
        .orElseThrow();

  }

  private Optional<SidebarSectionLink> findSidebarLink(DocumentView documentView, SectionClauseVersionDto versionDto) {

    return documentView.getSections().stream()
        .filter(section -> section.getName().equals(DocumentSection.valueOf(versionDto.getSectionName()).getDisplayName()))
        .flatMap(section -> section.getSidebarSectionLinks().stream())
        .filter(link -> link.getLink().contains(versionDto.getVersionId().toString()))
        .findFirst();

  }

  @Test
  public void documentViewHasClauses_noDocumentClauses() {

    var emptyDocView = new DocumentView(PwaDocumentType.INSTANCE, new PwaApplication(), DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT);
    emptyDocView.setSections(List.of(new SectionView()));

    boolean hasClauses = documentViewService.documentViewHasClauses(emptyDocView);

    assertThat(hasClauses).isFalse();

  }

  @Test
  public void documentViewHasClauses_hasDocumentClauses() {

    var docView = new DocumentView(PwaDocumentType.INSTANCE, new PwaApplication(), DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT);
    var sectionView = new SectionView();
    sectionView.setClauses(List.of(new SectionClauseVersionView()));
    docView.setSections(List.of(sectionView));

    boolean hasClauses = documentViewService.documentViewHasClauses(docView);

    assertThat(hasClauses).isTrue();

  }

  @Test
  public void documentViewContainsManualMergeData_hasManualMergeData() {

    var docView = new DocumentView(PwaDocumentType.INSTANCE, new PwaApplication(), DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT);
    var sectionView = new SectionView();

    var clauseView1 = new SectionClauseVersionView();
    clauseView1.setText("??some manual merge data here??");

    var clauseView2 = new SectionClauseVersionView();
    clauseView2.setText("no manual merge data here gov");

    sectionView.setClauses(List.of(clauseView1, clauseView2));
    docView.setSections(List.of(sectionView));

    boolean containsManualMergeData = documentViewService.documentViewContainsManualMergeData(docView);

    assertThat(containsManualMergeData).isTrue();

  }

  @Test
  public void documentViewContainsManualMergeData_noManualMergeData() {

    var docView = new DocumentView(PwaDocumentType.INSTANCE, new PwaApplication(), DocumentTemplateMnem.PETROLEUM_CONSENT_DOCUMENT);
    var sectionView = new SectionView();

    var clauseView1 = new SectionClauseVersionView();
    clauseView1.setText("nothing to see here");

    var clauseView2 = new SectionClauseVersionView();
    clauseView2.setText("no manual merge data here gov");

    sectionView.setClauses(List.of(clauseView1, clauseView2));
    docView.setSections(List.of(sectionView));

    boolean containsManualMergeData = documentViewService.documentViewContainsManualMergeData(docView);

    assertThat(containsManualMergeData).isFalse();

  }

}
