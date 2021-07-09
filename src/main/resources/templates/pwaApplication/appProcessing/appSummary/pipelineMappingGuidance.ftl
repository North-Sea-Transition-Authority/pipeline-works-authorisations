<#include '../../../layout.ftl'>

<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="pipelineDataDownloadOptionItems" type="java.util.List<uk.co.ogauthority.pwa.controller.appsummary.PipelineDataDownloadOptionItem>" -->
<#-- @ftlvariable name="serviceName" type="java.lang.String" -->
<#-- @ftlvariable name="regulatorMapsAndToolsUrl" type="java.lang.String" -->
<#-- @ftlvariable name="regulatorMapsAndToolsLabel" type="java.lang.String" -->
<#-- @ftlvariable name="offshoreMapLabel" type="java.lang.String" -->

<#assign pageHeading="Download application pipeline map data"/>

<@defaultPage htmlTitle=pageHeading topNavigation=true fullWidthColumn=true>

    <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

    <h2 class="govuk-heading-l">${pageHeading}</h2>

    <p class="govuk-body">In-service pipelines from the application can be downloaded in <a class="govuk-link" href=https://datatracker.ietf.org/doc/html/rfc7946>GeoJson</a> format and plotted on a variety of mapping tools.</p>

    <ol class="govuk-list">
        <#list pipelineDataDownloadOptionItems as downloadOption>
            <li><@fdsAction.link linkText=downloadOption.displayString linkClass="govuk-link govuk-link--button" linkUrl=springUrl(downloadOption.downloadUrl) openInNewTab=true /></li>
        </#list>
    </ol>

    <p class="govuk-body">The OGA provides a variety of UKCS offshore data sets and interactive maps that you may find useful when using the ${serviceName}.</p>

    <h3 class="govuk-heading-m">How do I plot application pipelines on a map?</h3>
    <ol class="govuk-list govuk-list--number">
        <li>Use this page to download application pipeline data in GeoJson format and save the file locally.</li>
        <li>Navigate in your browser to the <a class="govuk-link" href=${regulatorMapsAndToolsUrl}>${regulatorMapsAndToolsLabel}</a> and then click the ${offshoreMapLabel} link.</li>
        <li>Once the map has loaded locate the "Add data" menu. This is located in the top right of the map with the following icon:<br>
            <img class="" src="<@spring.url '/assets/static/images/oga-offshore-map-add-data-icon-highlight.png'/>" alt="Add data menu icon highlighted among OGA offshore map navigation items"/>
        </li>
        <li>Select the "File" data source, and follow the on screen instructions.</li>
    </ol>

    <h3 class="govuk-heading-m">FAQs</h3>

    <h4 class="govuk-heading-s">How can I change the information shown on the map?</h4>
    <p class="govuk-body">Once the map has loaded locate the "Layers" menu. This is located in the top right of the map with the following icon:<br>
        <img class="" src="<@spring.url '/assets/static/images/oga-offshore-map-layers-icon-highlight.png'/>" alt="Layers menu icon highlighted among OGA offshore map navigation items"/>
    </p>
    <p class="govuk-body">If application pipelines have been added to the map, they will appear as their own 'layer' within the Layers menu.</p>

    <h4 class="govuk-heading-s">How can I easily identify specific pipelines on an application with a large number of pipelines?</h4>
    <p class="govuk-body">All data about elements within a layer can be viewed in a table by clicking the three dots ("...") icon on the layer and then clicking the "View attributes in table" option.</p>
    <p class="govuk-body">Pipelines selected in the attribute table can be zoomed to on the map.</p>

    <h4 class="govuk-heading-s">How can I see information associated with feature shown on the map?</h4>
    <p class="govuk-body">Click on a map feature to show a popup showing more information. Its possible there are multiple features available at the point clicked. Use the arrows in the popup heading to change which feature information is visible.</p>

</@defaultPage>