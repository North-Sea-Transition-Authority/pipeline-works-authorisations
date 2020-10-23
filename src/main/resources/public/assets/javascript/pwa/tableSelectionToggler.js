'use strict';


class TableSelectionToggler {
  constructor($togglerContainer) {
    this.$tableId = $togglerContainer.attr('data-table-id');

    this.$togglerSelectAllLink = $togglerContainer.find("a.table-selection-toggler__select-all-link");
    this.$togglerSelectNoneLink = $togglerContainer.find("a.table-selection-toggler__select-none-link");

    this._setupTogglerInputs();
  }

  _setupTogglerInputs() {
    var tableId = this.$tableId;

    var that = this;

    if(!$.isEmptyObject(this.$togglerSelectAllLink)){
      this.$togglerSelectAllLink.click(function(e){
        e.preventDefault();
        that._toggleCheckBoxes(tableId, true);
      });
    }

    if(!$.isEmptyObject(this.$togglerSelectNoneLink)){
      this.$togglerSelectNoneLink.click(function(e){
        e.preventDefault();
        that._toggleCheckBoxes(tableId, false);
      });
    }

  }

  _toggleCheckBoxes(tableId, isSelected){
    $(`#${tableId} td input:checkbox`).prop('checked', isSelected).change();

  }
}


$(document).ready(() => {
  $('.table-selection-toggler').each((index, element) => new TableSelectionToggler($(element)));
});
