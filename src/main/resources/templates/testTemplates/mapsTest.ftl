<#include '../layout.ftl'>

<@defaultPage htmlTitle="Interactive maps" pageHeading="Interactive maps" fullWidthColumn=true wrapperWidth=true>

  <@maps.map/>
  <div class="input-wrapper">
    <@fdsFieldset.fieldset legendHeading="Pipeline start">
      <div class="govuk-form-group">
        <label class="govuk-label" for="start-lat-input">
          Lat
        </label>
        <input class="govuk-input govuk-!-width-two-thirds" id="start-lat-input" type="text">
      </div>
      <div class="govuk-form-group">
        <label class="govuk-label" for="start-long-input">
          Long
        </label>
        <input class="govuk-input govuk-!-width-two-thirds" id="start-long-input" type="text">
      </div>
    </@fdsFieldset.fieldset>
    <@fdsFieldset.fieldset legendHeading="Pipeline end">
      <div class="govuk-form-group">
        <label class="govuk-label" for="end-lat-input">
          Lat
        </label>
        <input class="govuk-input govuk-!-width-two-thirds" id="end-lat-input" type="text">
      </div>
      <div class="govuk-form-group">
        <label class="govuk-label" for="end-long-input">
          Long
        </label>
        <input class="govuk-input govuk-!-width-two-thirds" id="end-long-input" type="text">
      </div>
    </@fdsFieldset.fieldset>
  </div>
  <style>
    .input-wrapper {
      display: flex;
      margin-top: 10px;
    }
    .input-wrapper:last-child {
      margin-left: 20px;
    }
  </style>
  <script>
    $(document).ready(function() {
      var map = new MapComponent('map');
      map.registerLayerEventHandlers();

      var startPointLat, startPointLong, endPointLat, endPointLong;

      var updateStartPoint = function() {
        if (startPointLat != null && startPointLong != null) {
          map.setPipelineStartPoint(startPointLat, startPointLong)
        }
      }

      var updateEndPoint = function() {
        if (endPointLat != null && endPointLong != null) {
          map.setPipelineEndPoint(endPointLat, endPointLong)
        }
      }

      $('#start-lat-input').on('input', function() {
        startPointLat = this.value;
        updateStartPoint();
      });
      $('#start-long-input').on('input', function() {
        startPointLong = this.value;
        updateStartPoint();
      });
      $('#end-lat-input').on('input', function() {
        endPointLat = this.value;
        updateEndPoint();
      });
      $('#end-long-input').on('input', function() {
        endPointLong = this.value;
        updateEndPoint();
      });

    });
  </script>
</@defaultPage>