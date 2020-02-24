<#include '../../layout.ftl'>
<#import '../../dummyFileUpload.ftl' as dummyFileUpload/>

<#-- @ftlvariable name="pipelineView" type="uk.co.ogauthority.pwa.temp.model.view.PipelineView" -->
<#-- @ftlvariable name="addIdentUrl" type="String" -->
<#-- @ftlvariable name="backToPipelinesUrl" type="String" -->

<#assign heading = "${pipelineView.pipelineType.displayName} - ${pipelineView.pipelineNumber}" />

<@defaultPage htmlTitle=heading pageHeading=heading twoThirdsColumn=false>

    <div>
        <@fdsAction.link linkText="Add ident" linkClass="govuk-button govuk-button--secondary" linkUrl=springUrl(addIdentUrl) />
    </div>

    <h2 class="govuk-heading-l">Details</h2>
    <@fdsCard.card>
        <@fdsDataItems.dataItem>
            <@fdsDataItems.dataValuesNumber key="Length" value="${pipelineView.length}m" valueId="${pipelineView.pipelineNumber}-length" />
            <@fdsDataItems.dataValues key="From" value="${pipelineView.from} ${pipelineView.getFromLatString()?no_esc} ${pipelineView.getFromLongString()?no_esc}" />
            <@fdsDataItems.dataValues key="To" value="${pipelineView.to} ${pipelineView.getToLatString()?no_esc} ${pipelineView.getToLongString()?no_esc}" />
            <@fdsDataItems.dataValues key="Component parts" value=pipelineView.componentParts />
            <@fdsDataItems.dataValues key="Products to be conveyed" value=pipelineView.productsToBeConveyed />
        </@fdsDataItems.dataItem>
    </@fdsCard.card>

    <hr class="govuk-section-break govuk-section-break--l"/>

    <h2 class="govuk-heading-l">Technical Drawing</h2>
    <@dummyFileUpload.fileUpload id="pipelineDrawing" uploadUrl="/" deleteUrl="/" downloadUrl="/" maxAllowedSize=500 allowedExtensions="txt"/>

    <@fdsAction.link linkText="Back to pipelines" linkClass="govuk-link govuk-link--button" linkUrl=springUrl(backToPipelinesUrl) />

</@defaultPage>