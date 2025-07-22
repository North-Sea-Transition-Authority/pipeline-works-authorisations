<#include '../../layout.ftl'>

<#macro map>
  <div class="map-wrapper">
    <div id="map" class="map"></div>
    <div class="map-layers">
      <h3 class="govuk-heading-m">Layers</h3>
      <div class="govuk-checkboxes">
        <div class="govuk-checkboxes__item">
          <input class="govuk-checkboxes__input" id="map-layer-quadrants" type="checkbox" checked>
          <label class="govuk-label govuk-checkboxes__label" for="map-layer-quadrants">
            Quadrants
          </label>
        </div>
        <div class="govuk-checkboxes__item">
          <input class="govuk-checkboxes__input" id="map-layer-blocks" type="checkbox" checked>
          <label class="govuk-label govuk-checkboxes__label" for="map-layer-blocks">
            Licenced Blocks
          </label>
        </div>
        <div class="govuk-checkboxes__item">
          <input class="govuk-checkboxes__input" id="map-layer-fields" type="checkbox" checked>
          <label class="govuk-label govuk-checkboxes__label" for="map-layer-fields">
            Fields
          </label>
        </div>
        <div class="govuk-checkboxes__item">
          <input class="govuk-checkboxes__input" id="map-layer-pipelines" type="checkbox" checked>
          <label class="govuk-label govuk-checkboxes__label" for="map-layer-pipelines">
            Pipelines
          </label>
        </div>
      </div>
    </div>
  </div>
  <div id="map-overlay"></div>

  <link rel="stylesheet" href="<@spring.url '/assets/css/vendor/openlayers/ol.6.3.1.css'/>" type="text/css">
  <script src="<@spring.url '/assets/javascript/vendor/openlayers/ol.6.3.1.min.js'/>"></script>
  <script src="<@spring.url '/assets/static/js/pwa/mapComponent.js'/>"></script>
</#macro>