<#include '../../../layout.ftl'>

<#-- @ftlvariable name="pipelineOverview" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview" -->
<#-- @ftlvariable name="addIdentUrl" type="String" -->

<@defaultPage htmlTitle="${pipelineOverview.pipelineNumber} idents" pageHeading="${pipelineOverview.pipelineNumber} idents" breadcrumbs=true>

    <@fdsDataItems.dataItem>
        <@fdsDataItems.dataValues key="Length" value="${pipelineOverview.length}m" />
        <@fdsDataItems.dataValues key="From" value="${pipelineOverview.fromLocation}" />
        <@fdsDataItems.dataValues key="To" value="${pipelineOverview.toLocation}" />
        <@fdsDataItems.dataValues key="Component parts" value="${pipelineOverview.componentParts}" />
        <@fdsDataItems.dataValues key="Products to be conveyed" value=pipelineOverview.productsToBeConveyed />
    </@fdsDataItems.dataItem>

    <@fdsAction.link linkText="Add ident" linkUrl=springUrl(addIdentUrl) linkClass="govuk-button govuk-button--blue" />

</@defaultPage>