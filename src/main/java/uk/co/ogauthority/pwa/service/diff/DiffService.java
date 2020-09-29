package uk.co.ogauthority.pwa.service.diff;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.DifferenceProcessingException;
import uk.co.ogauthority.pwa.model.diff.DiffType;
import uk.co.ogauthority.pwa.model.diff.DiffedField;


/**
 * Service to calculate the difference between two objects of the same class by comparing their fields.
 */
@Service
public class DiffService {


  /**
   * Overload diff to be used when there are no fields on the object to be ignored.
   *
   * @param current  The current instance of the object.
   * @param previous A previous instance of the same type to compare against.
   * @param <T>      The type of both objects.
   * @return A map of field name to DiffedField or List of DiffedFields
   */
  public <T> Map<String, Object> diff(T current, T previous) {
    return diff(current, previous, Collections.emptySet());
  }

  /**
   * Calculate the difference between 2 objects, by comparing all their fields.
   * All accessible fields must be representable in String form.
   * The result of the diff is returned as a DiffField.
   *
   * @param current     The current instance of the object.
   * @param previous    A previous instance of the same type to compare against.
   * @param ignoreFieldNames if provided, do not diff field where name is within set
   * @param <T>         The type of both objects.
   * @return A map of field name to DiffedField or List of DiffedFields
   */
  public <T> Map<String, Object> diff(T current, T previous, Set<String> ignoreFieldNames) {

    Map<String, Object> diffResult = new HashMap<>();


    doWithField(current.getClass(), field -> {
      var processField = !ignoreFieldNames.contains(field.getName());

      if (processField) {
        Object currentValue = getFieldValue(field, current);
        Object previousValue = getFieldValue(field, previous);

        // To avoid clashing member variable names between objects, use the simple class name for a slightly better chance
        // at uniqueness in the map
        String attributeName = createAttributeName(current.getClass(), field);
        compare(diffResult, attributeName, currentValue, previousValue, field);
      }
    });

    return diffResult;
  }

  private String createAttributeName(Class parentClass, Field field) {
    return parentClass.getSimpleName() + "_" + field.getName();
  }

  /**
   * Return a list of diffed object maps by comparing two lists.
   * The final list contains diffed items common between each list, items which do not exist in the current but do in
   * the previous list and items which do not exist in the previous list but do in the current.
   *
   * @param currentList                  current list of items to compare
   * @param previousList                 previous list of items to compare
   * @param getCurrentItemLinkToPrevious when looping the current list, how can the item object be mapped to an item in the "previous" list
   * @param getPreviousItemLinkToCurrent when looping the previous list, how can the item object be mapped to an item in the "current" list
   * @param <T>                          each list for comparison contains objects of the this type
   * @param <V>                          the return type of the getCurrentItemLinkToPrevious and getPreviousItemLinkToCurrent
   *                                     function to map between lists
   */
  public <T, V> List<Map<String, ?>> diffComplexLists(List<T> currentList,
                                                      List<T> previousList,
                                                      Function<T, V> getCurrentItemLinkToPrevious,
                                                      Function<T, V> getPreviousItemLinkToCurrent) {


    List<Map<String, ?>> listOfItemDiffs = new ArrayList<>();

    for (T currentListItem : currentList) {
      // Get this item in the previous list
      Optional<T> previousListItemOptional = previousList
          .stream()
          .filter(prev ->
              // the item requires diffing when we can map to the previous item list.
              getPreviousItemLinkToCurrent.apply(prev).equals(getCurrentItemLinkToPrevious.apply(currentListItem))
                  || (getPreviousItemLinkToCurrent.apply(prev).equals(
                  getPreviousItemLinkToCurrent.apply(currentListItem)))
          )
          .findFirst();

      if (previousListItemOptional.isPresent()) {
        // Compare all the item fields
        Map<String, ?> itemDiffMap = diff(currentListItem, previousListItemOptional.get());
        listOfItemDiffs.add(itemDiffMap);
      } else {
        // This item didn't exist in the previous version so it must be newly added
        Map<String, ?> itemDiffMap = createdAddedObject(currentListItem);
        listOfItemDiffs.add(itemDiffMap);
      }

    }

    for (T previousItem : previousList) {
      // Get this item if it exists in the current list
      Optional<T> currentItemOptional = currentList
          .stream()
          .filter(cur ->
              // the item has been deleted when we cannot map from the previous list to the current.
              getPreviousItemLinkToCurrent.apply(previousItem).equals(getCurrentItemLinkToPrevious.apply(cur))
                  || (getPreviousItemLinkToCurrent.apply(previousItem).equals(getPreviousItemLinkToCurrent.apply(cur)))
          )
          .findFirst();

      if (!currentItemOptional.isPresent()) {
        // This item exists in the previous list but not the current list. It must have been deleted.
        Map<String, ?> itemDiffMap = createdDeletedObject(previousItem);
        listOfItemDiffs.add(itemDiffMap);
      }
    }

    return listOfItemDiffs;
  }

  /**
   * Return a object with all fields marked as added.
   *
   * @param addedObject The object to show as added.
   * @return A map of all fields on the object to an ADDED DiffType.
   */
  private Map<String, Object> createdAddedObject(Object addedObject) {

    Map<String, Object> diffResult = new HashMap<>();

    doWithField(addedObject.getClass(), field -> {
      String attributeName = addedObject.getClass().getSimpleName() + "_" + field.getName();
      Object currentField = getFieldValue(field, addedObject);

      DiffComparisonTypes diffType = DiffComparisonTypes.findDiffComparisonType(field.getType());
      // Workaround for Diffable Lists. There is no specific diff strategy for diffed objects containing lists
      // As a workaround for the "added object" case where we dont bother doing a real diff, we need to recreate the compare() special case
      // contained in {@link this::compare} in order to get our output model correctly populated
      if (diffType.equals(DiffComparisonTypes.LIST)) {
        compare(diffResult, attributeName, currentField, List.of(), field);
      } else {
        DiffComparisonStrategy diffComparisonStrategy = diffType.getDiffComparisonStrategy();
        diffResult.put(attributeName, diffComparisonStrategy.createAddedDiffedField(currentField));
      }

    });

    return diffResult;
  }

  /**
   * Return a object with all fields marked as deleted.
   *
   * @param deletedObject The object to show as deleted.
   * @return A map of all fields on the object to an DELETED DiffType.
   */
  private Map<String, Object> createdDeletedObject(Object deletedObject) {

    Map<String, Object> diffResult = new HashMap<>();

    doWithField(deletedObject.getClass(), field -> {
      String attributeName = createAttributeName(deletedObject.getClass(), field);
      Object deletedField = getFieldValue(field, deletedObject);
      DiffComparisonTypes diffType = DiffComparisonTypes.findDiffComparisonType(field.getType());
      // Workaround for Diffable Lists. There is no specific diff strategy for diffed objects containing lists
      // As a workaround for the "deleted object" case where we dont bother doing a real diff, we need to recreate the diff special case
      // contained in {@link this::compare} in order to get our output model correctly populated
      if (diffType.equals(DiffComparisonTypes.LIST)) {
        compare(diffResult, attributeName, List.of(), deletedField, field);
      } else {
        DiffComparisonStrategy diffComparisonStrategy = diffType.getDiffComparisonStrategy();
        diffResult.put(attributeName, diffComparisonStrategy.createDeletedDiffedField(deletedField));
      }
    });

    return diffResult;
  }

  private Object getFieldValue(Field field, Object object) {
    try {
      return FieldUtils.readField(field, object, true);
    } catch (IllegalAccessException e) {
      throw new DifferenceProcessingException(
          String.format("Failed to access field '%s' on class '%s'", field.getName(), object.getClass()));
    }
  }

  private void doWithField(Class clazz, Consumer<Field> fieldConsumer) {
    for (Field field : FieldUtils.getAllFields(clazz)) {
      // This is required so that synthetic fields added by e.g Jacoco are ignored and don't make tests fail.
      if (!field.isSynthetic()) {
        fieldConsumer.accept(field);
      }
    }
  }

  /**
   * Helper which determines how to compare the two values and then adds them to the provided diffResult map.
   *
   * @param diffResult    the map of diff results to update
   * @param attributeName what to give map as the key for diffedField object
   * @param currentValue  the value now
   * @param previousValue the value previously
   */
  private void compare(Map<String, Object> diffResult, String attributeName, Object currentValue, Object previousValue,
                       Field valueField) {

    DiffComparisonTypes diffComparisonType = DiffComparisonTypes.findDiffComparisonType(valueField.getType());

    if (diffComparisonType.equals(DiffComparisonTypes.NOT_SUPPORTED)) {
      throw new DifferenceProcessingException(
          "Unsupported Object given to diff service. AttributeName: " + attributeName +
              ". Object class: " + currentValue.getClass().getSimpleName());
    }

    if (diffComparisonType.equals(DiffComparisonTypes.LIST)) {
      diffResult.put(attributeName, compareUsingListComparison(((List<?>) currentValue), ((List<?>) previousValue)));
    } else {
      diffResult.put(attributeName,
          diffComparisonType.getDiffComparisonStrategy().compare(currentValue, previousValue));
    }

  }


  /**
   * From a list of simple strings or integers, get a list of diffed fields. Assumption is the list two lists have no repeating elements.
   *
   * @return list of DiffedField objects based on lists provided.
   */
  List<DiffedField> compareUsingListComparison(List<?> currentList, List<?> previousList) {
    List<DiffedField> listOfDiffedStrings = new ArrayList<>();
    StringComparisonStrategy stringComparisonStrategy = new StringComparisonStrategy();

    for (Object current : currentList) {
      String currentString = stringComparisonStrategy.objectAsType(current);
      Optional<String> previousMatchingString = previousList.stream().filter(
          s -> stringComparisonStrategy.objectAsType(s).equals(currentString)).findFirst().map(
          stringComparisonStrategy::objectAsType);
      if (previousMatchingString.isPresent()) {
        listOfDiffedStrings.add(new DiffedField(DiffType.UNCHANGED, currentString, previousMatchingString.get()));
      } else {
        listOfDiffedStrings.add(new DiffedField(DiffType.ADDED, currentString, null));
      }
    }

    for (Object previous : previousList) {
      String previousString = stringComparisonStrategy.objectAsType(previous);
      Optional<String> currentMatchingString = currentList.stream().filter(
          s -> stringComparisonStrategy.objectAsType(s).equals(previousString)).findFirst().map(
          stringComparisonStrategy::objectAsType);
      if (currentMatchingString.isEmpty()) {
        listOfDiffedStrings.add(new DiffedField(DiffType.DELETED, null, previousString));
      }
    }

    return listOfDiffedStrings;
  }

}
