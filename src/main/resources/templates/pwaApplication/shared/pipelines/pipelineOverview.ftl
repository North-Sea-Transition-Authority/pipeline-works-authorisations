<#-- Pipeline overview/summary display -->

<#include '../../../layout.ftl'>

<#-- @ftlvariable name="pipeline" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview" -->

<#macro header pipeline>

    <#assign from>
      ${pipeline.fromLocation}
      <br/>
      <@pwaCoordinate.display coordinatePair=pipeline.fromCoordinates />
    </#assign>

    <#assign to>
      ${pipeline.toLocation}
      <br/>
      <@pwaCoordinate.display coordinatePair=pipeline.toCoordinates />
    </#assign>

    <@fdsDataItems.dataItem>
        <@fdsDataItems.dataValues key="Length" value="${pipeline.length}m" />
        <@fdsDataItems.dataValues key="From" value=from />
        <@fdsDataItems.dataValues key="To" value=to />
        <@fdsDataItems.dataValues key="Component parts" value="${pipeline.componentParts}" />
        <@fdsDataItems.dataValues key="Products to be conveyed" value=pipeline.productsToBeConveyed />
    </@fdsDataItems.dataItem>

</#macro>