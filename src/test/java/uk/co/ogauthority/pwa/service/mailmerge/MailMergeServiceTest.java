package uk.co.ogauthority.pwa.service.mailmerge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.documents.view.SectionClauseVersionView;
import uk.co.ogauthority.pwa.model.documents.view.SectionView;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.model.entity.mailmerge.MailMergeField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.repository.mailmerge.MailMergeFieldRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.markdown.MailMergeContainer;
import uk.co.ogauthority.pwa.service.markdown.MarkdownService;
import uk.co.ogauthority.pwa.testutils.DocumentDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class MailMergeServiceTest {

  @Mock
  private MarkdownService markdownService;

  @Mock
  private MailMergeFieldRepository mailMergeFieldRepository;

  @Mock
  private PwaApplicationMailMergeResolver pwaApplicationMailMergeResolver;

  private MailMergeService mailMergeService;

  private PwaApplicationDetail detail;

  private static final String APPENDED_BY_MERGE = " merged";

  private static final String AUTOMATIC_MAIL_MERGE_CLASSES = "pwa-mail-merge__preview pwa-mail-merge__preview--automatic";
  private static final String MANUAL_MAIL_MERGE_CLASSES = "pwa-mail-merge__preview pwa-mail-merge__preview--manual";

  private final Map<String, String> resolvedMergeFields = Map.of(
      MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE.name(), DateUtils.formatDate(Instant.now()),
      MailMergeFieldMnem.PROJECT_NAME.name(), "project name"
  );

  @Before
  public void setUp() throws Exception {

    when(pwaApplicationMailMergeResolver.supportsDocumentSource(any())).thenCallRealMethod();

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    mailMergeService = new MailMergeService(List.of(pwaApplicationMailMergeResolver), markdownService, mailMergeFieldRepository);

    doAnswer(invocation -> {
      var passedArg = (String) invocation.getArgument(0);
      return passedArg + APPENDED_BY_MERGE;
    }).when(markdownService).convertMarkdownToHtml(any(), any());

    when(pwaApplicationMailMergeResolver.resolveMergeFields(detail.getPwaApplication())).thenReturn(resolvedMergeFields);

  }

  @Test
  public void getMailMergeFieldsForDocumentSource() {

    var mnems = List.of(MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE, MailMergeFieldMnem.PROJECT_NAME);

    when(pwaApplicationMailMergeResolver.getAvailableMailMergeFields(detail.getPwaApplication()))
        .thenReturn(mnems);

    mailMergeService.getMailMergeFieldsForDocumentSource(detail.getPwaApplication());

    verify(mailMergeFieldRepository, times(1)).findAllByMnemIn(mnems);

  }

  @Test
  public void resolveMergeFields_preview() {

    var resolvedMergeFields = Map.of(
        MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE.name(), DateUtils.formatDate(Instant.now()),
        MailMergeFieldMnem.PROJECT_NAME.name(), "project name"
    );

    when(pwaApplicationMailMergeResolver.resolveMergeFields(detail.getPwaApplication())).thenReturn(resolvedMergeFields);

    var container = mailMergeService.resolveMergeFields(detail.getPwaApplication(), DocGenType.PREVIEW);

    assertThat(container.getMailMergeFields()).containsExactlyInAnyOrderEntriesOf(resolvedMergeFields);

    assertThat(container.getAutomaticMailMergeDataHtmlAttributeMap()).containsOnly(
        entry("class", AUTOMATIC_MAIL_MERGE_CLASSES));

    assertThat(container.getManualMailMergeDataHtmlAttributeMap()).containsOnly(
        entry("class", MANUAL_MAIL_MERGE_CLASSES));

  }

  @Test
  public void resolveMergeFields_full() {

    var container = mailMergeService.resolveMergeFields(detail.getPwaApplication(), DocGenType.FULL);

    assertThat(container.getMailMergeFields()).containsExactlyInAnyOrderEntriesOf(resolvedMergeFields);

  }

  @Test
  public void mailMerge_preview() {

    var docView = setupDocView();

    var resolvedMergeFields = Map.of(
        MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE.name(), DateUtils.formatDate(Instant.now()),
        MailMergeFieldMnem.PROJECT_NAME.name(), "project name"
    );

    when(pwaApplicationMailMergeResolver.resolveMergeFields(detail.getPwaApplication())).thenReturn(resolvedMergeFields);

    var container = new MailMergeContainer();
    container.setMailMergeFields(resolvedMergeFields);
    container.setAutomaticMailMergeDataHtmlAttributeMap(Map.of("class", AUTOMATIC_MAIL_MERGE_CLASSES));
    container.setManualMailMergeDataHtmlAttributeMap(Map.of("class", MANUAL_MAIL_MERGE_CLASSES));

    mailMergeService.mailMerge(docView, DocGenType.PREVIEW);

    var originalFlattenedClauseList = getFlattenedClauseList(setupDocView());
    var mergedFlattenedClauseList = getFlattenedClauseList(docView);

    verify(markdownService, times(originalFlattenedClauseList.size())).convertMarkdownToHtml(any(), eq(container));

    for (int i = 0; i < originalFlattenedClauseList.size(); i++) {

      var originalClause = originalFlattenedClauseList.get(i);
      var mergedClause = mergedFlattenedClauseList.get(i);

      assertThat(mergedClause.getText()).isEqualTo(originalClause.getText() + APPENDED_BY_MERGE);

    }

  }

  @Test
  public void mailMerge_full() {

    var docView = setupDocView();

    var resolvedMergeFields = Map.of(
        MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE.name(), DateUtils.formatDate(Instant.now()),
        MailMergeFieldMnem.PROJECT_NAME.name(), "project name"
    );

    when(pwaApplicationMailMergeResolver.resolveMergeFields(detail.getPwaApplication())).thenReturn(resolvedMergeFields);

    var container = new MailMergeContainer();
    container.setMailMergeFields(resolvedMergeFields);

    mailMergeService.mailMerge(docView, DocGenType.FULL);

    var originalFlattenedClauseList = getFlattenedClauseList(setupDocView());
    var mergedFlattenedClauseList = getFlattenedClauseList(docView);

    verify(markdownService, times(originalFlattenedClauseList.size())).convertMarkdownToHtml(any(), eq(container));

    for (int i = 0; i < originalFlattenedClauseList.size(); i++) {

      var originalClause = originalFlattenedClauseList.get(i);
      var mergedClause = mergedFlattenedClauseList.get(i);

      assertThat(mergedClause.getText()).isEqualTo(originalClause.getText() + APPENDED_BY_MERGE);

    }

  }

  @Test
  public void validateMailMergeFields_noInvalid() {

    var text = "((PROPOSED_START_OF_WORKS_DATE)) here";

    var mnems = List.of(MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE, MailMergeFieldMnem.PROJECT_NAME);

    when(pwaApplicationMailMergeResolver.getAvailableMailMergeFields(detail.getPwaApplication()))
        .thenReturn(mnems);

    var merge1 = new MailMergeField();
    merge1.setMnem(mnems.get(0));
    merge1.setText("start date");

    var merge2 = new MailMergeField();
    merge2.setMnem(mnems.get(1));
    merge2.setText("project name");

    when(mailMergeFieldRepository.findAllByMnemIn(mnems)).thenReturn(List.of(merge1, merge2));

    var invalidFields = mailMergeService.validateMailMergeFields(detail.getPwaApplication(), text);

    assertThat(invalidFields).isEmpty();

  }

  @Test
  public void validateMailMergeFields_invalid() {

    var text = "((PROPOSED_START_OF_WORKS_DATE)) here ((arrr)) ((badfieldshere))";

    var mnems = List.of(MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE, MailMergeFieldMnem.PROJECT_NAME);

    when(pwaApplicationMailMergeResolver.getAvailableMailMergeFields(detail.getPwaApplication()))
        .thenReturn(mnems);

    var merge1 = new MailMergeField();
    merge1.setMnem(mnems.get(0));
    merge1.setText("start date");

    var merge2 = new MailMergeField();
    merge2.setMnem(mnems.get(1));
    merge2.setText("project name");

    when(mailMergeFieldRepository.findAllByMnemIn(mnems)).thenReturn(List.of(merge1, merge2));

    var invalidFields = mailMergeService.validateMailMergeFields(detail.getPwaApplication(), text);

    assertThat(invalidFields).containsExactlyInAnyOrder("arrr", "badfieldshere");

  }

  private DocumentView setupDocView() {

    var docView = new DocumentView(PwaDocumentType.INSTANCE, detail.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);

    var dto1 = DocumentDtoTestUtils
        .getDocumentInstanceSectionClauseVersionDto(DocumentSection.INITIAL_TERMS_AND_CONDITIONS.name(), "init1", 1, 1);
    var dto2 = DocumentDtoTestUtils
        .getDocumentInstanceSectionClauseVersionDto(DocumentSection.INITIAL_TERMS_AND_CONDITIONS.name(), "init2", 1, 2);

    var dto1Child1 = DocumentDtoTestUtils
        .getDocumentInstanceSectionClauseVersionDto(DocumentSection.INITIAL_TERMS_AND_CONDITIONS.name(), "init1Child1", 2, 5);
    dto1Child1.setParentClauseId(1);

    var dto1Child1Child = DocumentDtoTestUtils
        .getDocumentInstanceSectionClauseVersionDto(DocumentSection.INITIAL_TERMS_AND_CONDITIONS.name(), "init1Child1Child", 3, 6);
    dto1Child1Child.setParentClauseId(5);

    var dto3 = DocumentDtoTestUtils
        .getDocumentInstanceSectionClauseVersionDto(DocumentSection.SCHEDULE_2.name(), "sched21", 1, 3);
    var dto4 = DocumentDtoTestUtils
        .getDocumentInstanceSectionClauseVersionDto(DocumentSection.SCHEDULE_2.name(), "sched22", 1, 4);

    var initSectionView = new SectionView();
    var initClauseList = new ArrayList<SectionClauseVersionView>();
    var view1 = SectionClauseVersionView.from(dto1);
    var view1Child = SectionClauseVersionView.from(dto1Child1);
    view1Child.setChildClauses(List.of(SectionClauseVersionView.from(dto1Child1Child)));
    view1.setChildClauses(List.of(view1Child));

    initClauseList.add(view1);
    initClauseList.add(SectionClauseVersionView.from(dto2));
    initSectionView.setClauses(initClauseList);

    var schedule2SectionView = new SectionView();
    var schedule2SectionViewClauseList = new ArrayList<SectionClauseVersionView>();
    schedule2SectionViewClauseList.add(SectionClauseVersionView.from(dto3));
    schedule2SectionViewClauseList.add(SectionClauseVersionView.from(dto4));
    schedule2SectionView.setClauses(schedule2SectionViewClauseList);

    docView = new DocumentView(PwaDocumentType.INSTANCE, detail.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);
    docView.setSections(List.of(initSectionView, schedule2SectionView));

    return docView;

  }

  private List<SectionClauseVersionView> getFlattenedClauseList(DocumentView documentView) {

    var list = new ArrayList<SectionClauseVersionView>();

    documentView.getSections().forEach(s -> {

      s.getClauses().forEach(level1 -> {

        list.add(level1);

        level1.getChildClauses().forEach(level2 -> {

          list.add(level2);

          list.addAll(level2.getChildClauses());

        });

      });

    });

    return list;

  }

}