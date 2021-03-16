<#include '../../../layout.ftl'>
<#import 'admiraltyChartManagement.ftl' as admiraltyChartManagement>
<#import 'pipelineDrawingManagement.ftl' as pipelineDrawingManagement>
<#import 'umbilicalCrossSectionDiagramManagement.ftl' as umbilicalCrossSectionDiagramManagement>

<#-- @ftlvariable name="errorMessage" type="String" -->
<#-- @ftlvariable name="admiraltyChartUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.AdmiraltyChartUrlFactory" -->
<#-- @ftlvariable name="admiraltyChartFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="admiraltyOptional" type="java.lang.Boolean" -->
<#-- @ftlvariable name="validatorFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PipelineDrawingValidationFactory" -->

<@defaultPage htmlTitle="Pipeline schematics and other diagrams" breadcrumbs=true fullWidthColumn=true>

    <#if errorMessage?has_content>
        <@fdsError.singleErrorSummary errorMessage=errorMessage />
    </#if>

    <h1 class="govuk-heading-xl">Pipeline schematics and other diagrams</h1>

    <#if showAdmiraltyChart>
        <@admiraltyChartManagement.admiraltyChartManagement
        urlFactory=admiraltyChartUrlFactory
        optionalSection=admiraltyOptional
        admiraltyChartFileViews=admiraltyChartFileViews />

        <hr class="govuk-section-break govuk-section-break--m"/>
    </#if>

    <#if showUmbilicalCrossSection>
        <@umbilicalCrossSectionDiagramManagement.umbilicalCrossSectionDiagramManagement
        urlFactory=umbilicalCrossSectionUrlFactory
        optionalSection=true
        fileViews=umbilicalCrossSectionFileViews />

        <hr class="govuk-section-break govuk-section-break--m"/>
    </#if>

    <@pipelineDrawingManagement.pipelineDrawingManagement
    urlFactory=pipelineDrawingUrlFactory
    pipelineDrawingSummaryViews=pipelineDrawingSummaryViews
    validatorFactory=validatorFactory!/>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>