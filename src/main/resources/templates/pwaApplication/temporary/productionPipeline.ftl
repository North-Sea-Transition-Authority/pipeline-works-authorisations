<#include '../../layout.ftl'>

<#-- @ftlvariable name="pipelineView" type="uk.co.ogauthority.pwa.temp.model.view.PipelineView" -->
<#-- @ftlvariable name="addIdentUrl" type="String" -->
<#-- @ftlvariable name="backToPipelinesUrl" type="String" -->

<#assign heading = "${pipelineView.pipelineType.displayName} - ${pipelineView.pipelineNumber}" />

<@defaultPage htmlTitle=heading pageHeading=heading twoThirdsColumn=false>

    <@fdsDataItems.dataItem>
        <@fdsDataItems.dataValuesNumber key="Length" value="${pipelineView.length}m" valueId="${pipelineView.pipelineNumber}-length" />
        <@fdsDataItems.dataValues key="From" value="${pipelineView.from} ${pipelineView.getFromLatString()?no_esc} ${pipelineView.getFromLongString()?no_esc}" />
        <@fdsDataItems.dataValues key="To" value="${pipelineView.to} ${pipelineView.getToLatString()?no_esc} ${pipelineView.getToLongString()?no_esc}" />
        <@fdsDataItems.dataValues key="Component parts" value=pipelineView.componentParts />
        <@fdsDataItems.dataValues key="Products to be conveyed" value=pipelineView.productsToBeConveyed />
    </@fdsDataItems.dataItem>

    <@fdsAction.link linkText="Add ident" linkClass="govuk-button govuk-button--secondary" linkUrl=springUrl(addIdentUrl) />
    <@fdsAction.link linkText="Back to pipelines" linkClass="govuk-link govuk-link--button" linkUrl=springUrl(backToPipelinesUrl) />

</@defaultPage>