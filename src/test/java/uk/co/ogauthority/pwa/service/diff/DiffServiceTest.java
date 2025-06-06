package uk.co.ogauthority.pwa.service.diff;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.exception.DifferenceProcessingException;
import uk.co.ogauthority.pwa.model.diff.DiffType;
import uk.co.ogauthority.pwa.model.diff.DiffedField;
import uk.co.ogauthority.pwa.model.view.StringWithTag;

@ExtendWith(MockitoExtension.class)
class DiffServiceTest {

  private DiffService diffService;

  private SimpleDiffTestClass simpleObjectCurrent;
  private SimpleDiffTestClass simpleObjectPrevious;

  private DiffTestWithSimpleListField diffWithListsCurrent;
  private DiffTestWithSimpleListField diffWithListsPrevious;

  private List<SimpleDiffTestClass> listOfSimpleDiffsCurrent;
  private List<SimpleDiffTestClass> listOfSimpleDiffsPrevious;

  private final List<String> defaultStringList = Arrays.asList("item1", "item2", "item3");
  private final List<Integer> defaultIntegerList = Arrays.asList(100, 200, 300);
  private final List<SimpleDiffTestClass> defaultSimpleDiffTestClassList = Arrays.asList(
      new SimpleDiffTestClass(
          true,
          "item 1",
          1,
          new StringWithTag("No tag"),
          new OtherDiffableAsStringClass("other 1")
      ),
      new SimpleDiffTestClass(
          true,
          "item 2",
          2,
          new StringWithTag("No tag"),
          new OtherDiffableAsStringClass("other 2")
      ),
      new SimpleDiffTestClass(true,
          "item 3",
          3,
          new StringWithTag("No tag"),
          new OtherDiffableAsStringClass("other 3")
      )
  );

  private final String defaultStringValue = "string";
  private final Integer defaultIntegerValue = 100;

  @BeforeEach
  void setup() {
    diffService = new DiffService();

    simpleObjectCurrent = new SimpleDiffTestClass(true,
        defaultStringValue,
        defaultIntegerValue,
        new StringWithTag("No tag"),
        new OtherDiffableAsStringClass(defaultStringValue));
    simpleObjectPrevious = new SimpleDiffTestClass(true,
        defaultStringValue,
        defaultIntegerValue,
        new StringWithTag("No tag"),
        new OtherDiffableAsStringClass(defaultStringValue));

    diffWithListsCurrent = new DiffTestWithSimpleListField(defaultStringList, defaultIntegerList);
    diffWithListsPrevious = new DiffTestWithSimpleListField(defaultStringList, defaultIntegerList);

    listOfSimpleDiffsCurrent = new ArrayList<>();
    listOfSimpleDiffsCurrent.addAll(defaultSimpleDiffTestClassList);
    listOfSimpleDiffsPrevious = new ArrayList<>();
    // we need two lists containing objects different objects which have matching values
    defaultSimpleDiffTestClassList.forEach(simpleDiffTestClass -> listOfSimpleDiffsPrevious.add(
        new SimpleDiffTestClass(true,
            simpleDiffTestClass.getStringField(),
            simpleDiffTestClass.getIntegerField(),
            simpleDiffTestClass.getStringWithTagField(),
            simpleDiffTestClass.getDiffableAsString())));

  }

  @Test
  void diff_whenUsingSimpleObjects_thenResultContainsAllFields() {
    Map<String, Object> diffResult = diffService.diff(simpleObjectCurrent, simpleObjectPrevious);

    Set<String> resultKeySet = diffResult.keySet();

    List<String> expectedDiffResultKeySet = FieldUtils.getAllFieldsList(SimpleDiffTestClass.class).stream().filter(
        f -> !f.isSynthetic()).map(f -> SimpleDiffTestClass.class.getSimpleName() + "_" + f.getName()).collect(
        toList());

    assertThat(resultKeySet).containsExactlyInAnyOrderElementsOf(expectedDiffResultKeySet);

  }

  @Test
  void diff_whenUsingSimpleObjects_andObjectsAreEquivalent_thenAllDiffedResultObjectsAreTypeUNCHANGED() {
    Map<String, Object> diffResult = diffService.diff(simpleObjectCurrent, simpleObjectPrevious);

    Set<DiffedField> resultDiffedFields = diffResult.values().stream().map(o -> ((DiffedField) o)).collect(
        Collectors.toSet());

    // types of all entries is UNCHANGED
    assertThat(resultDiffedFields.stream().allMatch(
        diffedField -> diffedField.getDiffType().equals(DiffType.UNCHANGED))).isTrue();
    // currentValue and previousValue match for each field
    assertThat(resultDiffedFields.stream().allMatch(
        dangerField -> dangerField.getCurrentValue().equals(dangerField.getPreviousValue()))).isTrue();


  }

  @Test
  void diff_whenUsingSimpleObjects_andAllFieldsHaveChanged_thenAllDiffedResultObjectsAreTypeUPDATED() {
    simpleObjectCurrent.setBooleanField(false);
    simpleObjectCurrent.setIntegerField(999);
    simpleObjectCurrent.setStringField("Updated String");
    simpleObjectCurrent.setStringWithTagField(new StringWithTag("Updated String"));
    simpleObjectCurrent.setDiffableAsString(new OtherDiffableAsStringClass("Updated String"));

    Map<String, Object> diffResult = diffService.diff(simpleObjectCurrent, simpleObjectPrevious);

    Set<DiffedField> resultDiffedFields = diffResult.values().stream().map(o -> ((DiffedField) o)).collect(
        Collectors.toSet());

    // type of all entries is UPDATED
    assertThat(resultDiffedFields.stream().allMatch(
        diffedfield -> diffedfield.getDiffType().equals(DiffType.UPDATED))).isTrue();

    // currentValue and previousValue do not match for each field
    assertThat(resultDiffedFields.stream().noneMatch(
        diffedfield -> diffedfield.getCurrentValue().equals(diffedfield.getPreviousValue()))).isTrue();

  }

  @Test
  void diff_whenUsingSimpleObjects_andAllFieldsHaveChanged_thenAllDiffedResultObjectsAreTypeDELETED() {
    simpleObjectCurrent.setBooleanField(null);
    simpleObjectCurrent.setIntegerField(null);
    simpleObjectCurrent.setStringField(null);
    simpleObjectCurrent.setStringWithTagField(new StringWithTag());
    simpleObjectCurrent.setDiffableAsString(null);

    Map<String, Object> diffResult = diffService.diff(simpleObjectCurrent, simpleObjectPrevious);

    Set<DiffedField> resultDiffedFields = diffResult.values().stream().map(o -> ((DiffedField) o)).collect(
        Collectors.toSet());

    // type of all entries is DELETED
    assertThat(resultDiffedFields.stream().allMatch(
        diffedField -> diffedField.getDiffType().equals(DiffType.DELETED))).isTrue();

    // currentValue is blank and previous value has value each field
    assertThat(resultDiffedFields)
        .isNotEmpty()
        .allSatisfy(diffedField -> StringUtils.isBlank(diffedField.getCurrentValue()))
        .allSatisfy(diffedField -> StringUtils.isNotBlank(diffedField.getPreviousValue()));

  }


  @Test
  void diff_whenObjectWithUnsupportedFieldType_thenThrowsException() {
    UndiffableObject undiffableObject = new UndiffableObject(Instant.now());
    assertThrows(DifferenceProcessingException.class, () ->
      diffService.diff(undiffableObject, undiffableObject));
  }

  @Test
  void diff_whenObjectHasUnsupportedLists_thenThrowsException() {
    DiffTestWithUnsupportedListField undiffableObject = new DiffTestWithUnsupportedListField();
    assertThrows(DifferenceProcessingException.class, () -> {
      Map<String, Object> diffResult = diffService.diff(undiffableObject, undiffableObject);
    });
  }

  @Test
  void diff_whenObjectHasSupportedLists_thenResultContainsListOfDiffs() {

    Map<String, Object> diffResult = diffService.diff(diffWithListsCurrent, diffWithListsPrevious);

    for (Object diffResultObject : diffResult.values()) {

      // test that a list fields produce a mapped list
      assertThat(diffResultObject instanceof List).isTrue();

      // all objects in list are of the expected type
      var diffResultObjectList = (List) diffResultObject;
      diffResultObjectList
          .forEach(o -> assertThat(o).isInstanceOf(DiffedField.class));

    }

  }

  @Test
  void diff_whenObjectHasSupportedListsA_andNoListItemsAreDifferent_thenResultListForFieldsAreAllUNCHANGED() {

    Map<String, Object> diffResult = diffService.diff(diffWithListsCurrent, diffWithListsPrevious);

    for (Object diffResultObject : diffResult.values()) {

      List<Object> resultObjectList = ((List) diffResultObject);
      List<DiffedField> diffedFieldList = resultObjectList.stream().filter(o -> o instanceof DiffedField).map(
          o -> ((DiffedField) o)).collect(toList());

      assertThat(diffedFieldList.size()).isGreaterThan(0);
      // all objects in list are of the expected type
      assertThat(diffedFieldList).allMatch(diffedField -> diffedField.getDiffType().equals(DiffType.UNCHANGED));
      // all objects in list have equal previous and current values
      assertThat(diffedFieldList).allMatch(
          diffedField -> diffedField.getCurrentValue().equals(diffedField.getPreviousValue()));

    }

  }

  @Test
  void diff_whenObjectHasSupportedListsA_andPreviousListWasEmpty_thenAllResultsAreADDED() {

    diffWithListsPrevious.setIntegerList(Collections.emptyList());
    diffWithListsPrevious.setStringList(Collections.emptyList());

    Map<String, Object> diffResult = diffService.diff(diffWithListsCurrent, diffWithListsPrevious);

    for (Object diffResultObject : diffResult.values()) {

      List<Object> resultObjectList = ((List) diffResultObject);
      List<DiffedField> diffedFieldList = resultObjectList.stream().filter(o -> o instanceof DiffedField).map(
          o -> ((DiffedField) o)).collect(toList());

      assertThat(diffedFieldList.size()).isEqualTo(defaultStringList.size());
      // all objects in list are of the expected type
      assertThat(diffedFieldList).allMatch(diffedField -> diffedField.getDiffType().equals(DiffType.ADDED));
      // all objects in list have a current value but no previous value
      assertThat(diffedFieldList).allMatch(diffedField -> StringUtils.isNotBlank(diffedField.getCurrentValue()));
      assertThat(diffedFieldList).allMatch(diffedField -> StringUtils.isBlank(diffedField.getPreviousValue()));

    }

  }

  @Test
  void diff_whenObjectHasSupportedListsA_andCurrentListIsEmpty_andPreviousListHasContent_thenAllResultsAreDELETED() {

    diffWithListsCurrent.setIntegerList(Collections.emptyList());
    diffWithListsCurrent.setStringList(Collections.emptyList());

    Map<String, Object> diffResult = diffService.diff(diffWithListsCurrent, diffWithListsPrevious);

    for (Object diffResultObject : diffResult.values()) {

      List<Object> resultObjectList = ((List) diffResultObject);
      List<DiffedField> diffedFieldList = resultObjectList.stream().filter(o -> o instanceof DiffedField).map(
          o -> ((DiffedField) o)).collect(toList());

      assertThat(diffedFieldList.size()).isEqualTo(defaultStringList.size());
      // all objects in list are of the expected type
      assertThat(diffedFieldList).allMatch(diffedField -> diffedField.getDiffType().equals(DiffType.DELETED));
      // all objects in list have blank current value but non-blank previous value
      assertThat(diffedFieldList).allMatch(diffedField -> StringUtils.isBlank(diffedField.getCurrentValue()));
      assertThat(diffedFieldList).allMatch(diffedField -> StringUtils.isNotBlank(diffedField.getPreviousValue()));

    }

  }

  @Test
  void diffComplexLists_whenListsObjectsCompletelyMap_andThereAreNoUpdatedObjects_thenResultListContainsOnlyUNCHANGEDDiffs() {
    List<Map<String, ?>> diffResultList = diffService.diffComplexLists(listOfSimpleDiffsCurrent,
        listOfSimpleDiffsPrevious, this::simpleDiffTestClassMappingFunction, this::simpleDiffTestClassMappingFunction);

    assertThat(diffResultList.size()).isEqualTo(defaultSimpleDiffTestClassList.size());
    for (Map<String, ?> diffResultMap : diffResultList) {
      // this test works for the simple class where we know no List is contained within the object
      assertThat(diffResultMap.values()).allMatch(o -> o instanceof DiffedField);
      List<DiffedField> diffedFieldList = diffResultMap.values().stream().filter(o -> o instanceof DiffedField).map(
          o -> (DiffedField) o).collect(toList());
      assertThat(diffedFieldList)
          .isNotEmpty()
          .allMatch(diffedField -> diffedField.getDiffType().equals(DiffType.UNCHANGED));
    }
  }

  @Test
  void diffComplexLists_whenListsObjectsCompletelyMap_andEachObjectHasHadANonMappingFieldUpdated_thenResultListContainsUPDATEDDiffs() {
    for (int i = 0; i < listOfSimpleDiffsCurrent.size(); i++) {
      listOfSimpleDiffsCurrent.get(i).setStringField("Updated Item");
    }

    List<Map<String, ?>> diffResultList = diffService.diffComplexLists(listOfSimpleDiffsCurrent,
        listOfSimpleDiffsPrevious, this::simpleDiffTestClassMappingFunction, this::simpleDiffTestClassMappingFunction);

    assertThat(diffResultList.size()).isEqualTo(defaultSimpleDiffTestClassList.size());
    for (Map<String, ?> diffResultMap : diffResultList) {
      // this test works for the simple class where we know no List is contained within the object
      assertThat(diffResultMap.values()).allMatch(o -> o instanceof DiffedField);
      List<DiffedField> diffedFieldList = diffResultMap.values().stream().filter(o -> o instanceof DiffedField).map(
          o -> (DiffedField) o).collect(toList());
      assertThat(diffedFieldList).anyMatch(diffedField -> diffedField.getDiffType().equals(DiffType.UPDATED));
    }
  }

  @Test
  void diffComplexLists_whenPreviousListIsEmpty_thenResultListContainsADDEDDiffsOnly() {
    listOfSimpleDiffsPrevious = Collections.emptyList();

    List<Map<String, ?>> diffResultList = diffService.diffComplexLists(listOfSimpleDiffsCurrent,
        listOfSimpleDiffsPrevious, this::simpleDiffTestClassMappingFunction, this::simpleDiffTestClassMappingFunction);

    assertThat(diffResultList.size()).isEqualTo(defaultSimpleDiffTestClassList.size());
    for (Map<String, ?> diffResultMap : diffResultList) {
      // this test works for the simple class where we know no List is contained within the object
      assertThat(diffResultMap.values()).allMatch(o -> o instanceof DiffedField);
      List<DiffedField> diffedFieldList = diffResultMap.values().stream().filter(o -> o instanceof DiffedField).map(
          o -> (DiffedField) o).collect(toList());
      assertThat(diffedFieldList).isNotEmpty()
          .allMatch(diffedField -> diffedField.getDiffType().equals(DiffType.ADDED));
    }
  }

  @Test
  void diffComplexLists_whenCurrentListIsEmpty_thenResultListContainsDeletedDiffsOnly() {
    listOfSimpleDiffsCurrent = Collections.emptyList();

    List<Map<String, ?>> diffResultList = diffService.diffComplexLists(listOfSimpleDiffsCurrent,
        listOfSimpleDiffsPrevious, this::simpleDiffTestClassMappingFunction, this::simpleDiffTestClassMappingFunction);

    assertThat(diffResultList.size()).isEqualTo(defaultSimpleDiffTestClassList.size());
    for (Map<String, ?> diffResultMap : diffResultList) {
      // this test works for the simple class where we know no List is contained within the object
      assertThat(diffResultMap.values()).allMatch(o -> o instanceof DiffedField);
      List<DiffedField> diffedFieldList = diffResultMap.values().stream().filter(o -> o instanceof DiffedField).map(
          o -> (DiffedField) o).collect(toList());
      assertThat(diffedFieldList).isNotEmpty()
          .allMatch(diffedField -> diffedField.getDiffType().equals(DiffType.DELETED));
    }
  }

  private Integer simpleDiffTestClassMappingFunction(SimpleDiffTestClass simpleDiffTestClass) {
    return simpleDiffTestClass.getIntegerField();
  }

  @Test
  void allSupportedDiffClassesAreIncludedInTestedObject() {
    Set<Class> supportedClassSet = new HashSet<>();
    Set<Class> testedClassMemberClassSet = new HashSet<Field>(
        Arrays.asList(FieldUtils.getAllFields(SimpleDiffTestClass.class)))
        .stream()
        .map(Field::getType)
        .collect(Collectors.toSet());

    for (DiffComparisonTypes diffComparisonType : DiffComparisonTypes.values()) {
      if (!diffComparisonType.equals(DiffComparisonTypes.LIST)) {
        supportedClassSet.addAll(diffComparisonType.getSupportedClasses());
      }
    }

    // Assert that every supported class is the class of a field in SimpleDiffTestClass. This ensures we dont add new comparison types and forget to test them
    try {
      assertThat(supportedClassSet).allMatch(testedClassMemberClassSet::contains);
    } catch (AssertionError e) {
      throw new AssertionError("All supported diff comparison classes  need to added to the SimpleDiffTestClass!", e);
    }

  }

  @Test
  void diff_ignoresFieldsWithinIgnoreSet(){

    var allFieldNames = Arrays.stream(FieldUtils.getAllFields(SimpleDiffTestClass.class))
        .map(Field::getName)
        .collect(toSet());

    Map<String, Object> diffResult = diffService.diff(simpleObjectCurrent, simpleObjectPrevious, allFieldNames);

    assertThat(diffResult).isEmpty();

  }

}
