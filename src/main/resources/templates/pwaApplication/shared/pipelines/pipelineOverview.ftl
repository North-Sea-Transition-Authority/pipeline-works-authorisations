<#-- Pipeline overview/summary display -->

<#include '../../../layout.ftl'>

<#-- @ftlvariable name="pipeline" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview" -->

<#macro header pipeline>

    <#assign from>
      ${pipeline.getFromLocation()}
      <br/>
      <@pwaCoordinate.display coordinatePair=pipeline.getFromCoordinates() />
    </#assign>

    <#assign to>
      ${pipeline.getToLocation()}
      <br/>
      <@pwaCoordinate.display coordinatePair=pipeline.getToCoordinates() />
    </#assign>

    <@fdsDataItems.dataItem>
        <@fdsDataItems.dataValues key="Length" value="${pipeline.getLength()}m" />
        <@fdsDataItems.dataValues key="From" value=from />
        <@fdsDataItems.dataValues key="To" value=to />
        <@fdsDataItems.dataValues key="Component parts" value="${pipeline.getComponentParts()}" />
        <@fdsDataItems.dataValues key="Products to be conveyed" value=pipeline.getProductsToBeConveyed() />
    </@fdsDataItems.dataItem>

</#macro>