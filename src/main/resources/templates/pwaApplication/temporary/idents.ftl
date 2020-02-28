<#include '../../layout.ftl'>
<#import 'identsSummary.ftl' as identsSummary/>

<!-- @ftlvariable name="projectInformationUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="${pipelineView.pipelineNumber} idents" pageHeading="${pipelineView.pipelineNumber} idents" breadcrumbs=true>

    <@fdsDataItems.dataItem>
        <@fdsDataItems.dataValuesNumber key="Length" value="${pipelineView.length}m" valueId="${pipelineView.pipelineNumber}-length" />
        <@fdsDataItems.dataValues key="From" value="${pipelineView.from} ${pipelineView.getFromLatString()?no_esc} ${pipelineView.getFromLongString()?no_esc}" />
        <@fdsDataItems.dataValues key="To" value="${pipelineView.to} ${pipelineView.getToLatString()?no_esc} ${pipelineView.getToLongString()?no_esc}" />
        <@fdsDataItems.dataValues key="Component parts" value=pipelineView.componentParts!"" />
        <@fdsDataItems.dataValues key="Products to be conveyed" value=pipelineView.productsToBeConveyed!"" />
    </@fdsDataItems.dataItem>

    <@identsSummary.identsSummary pipelineView=pipelineView canEdit=true/>

</@defaultPage>