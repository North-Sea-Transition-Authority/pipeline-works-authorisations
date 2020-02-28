<#include "../../layout.ftl">
<#import "pipelineTechnicalDetailsSummary.ftl" as techDetailsSummary>
<#import "identsSummary.ftl" as identsSummary>

<#-- @ftlvariable name="pipelineView" type="uk.co.ogauthority.pwa.temp.model.view.PipelineView" -->
<#-- @ftlvariable name="addIdentUrl" type="String" -->
<#-- @ftlvariable name="backToPipelinesUrl" type="String" -->

<@defaultPage htmlTitle="${pipelineView.pipelineNumber} summary" pageHeading="${pipelineView.pipelineNumber} summary" twoThirdsColumn=false breadcrumbs=true>

    <@fdsDataItems.dataItem>
        <@fdsDataItems.dataValuesNumber key="Length" value="${pipelineView.length}m" valueId="${pipelineView.pipelineNumber}-length" />
        <@fdsDataItems.dataValues key="From" value="${pipelineView.from} ${pipelineView.getFromLatString()?no_esc} ${pipelineView.getFromLongString()?no_esc}" />
        <@fdsDataItems.dataValues key="To" value="${pipelineView.to} ${pipelineView.getToLatString()?no_esc} ${pipelineView.getToLongString()?no_esc}" />
        <@fdsDataItems.dataValues key="Component parts" value=pipelineView.componentParts!"" />
        <@fdsDataItems.dataValues key="Products to be conveyed" value=pipelineView.productsToBeConveyed!"" />
    </@fdsDataItems.dataItem>

    <@identsSummary.identsSummary pipelineView=pipelineView/>

    <@techDetailsSummary.techDetailsSummary technicalDetailsView=pipelineView.technicalDetailsView/>

    <@fdsAction.link linkText="Back to pipelines" linkClass="govuk-link" linkUrl=springUrl(backToPipelinesUrl) />

</@defaultPage>