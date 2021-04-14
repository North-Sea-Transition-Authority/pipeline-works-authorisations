<#include '../../../layout.ftl'>
<#import 'blockCrossingsManagement.ftl' as blockCrossingManagement>
<#import 'medianLineCrossingManagement.ftl' as medianLineCrossingManagement>
<#import 'cableCrossingManagement.ftl' as cableCrossingManagement>
<#import 'pipeline/pipelineCrossingManagement.ftl' as pipelineCrossingManagement>

<#-- @ftlvariable name="errorMessage" type="String" -->

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory" -->
<#-- @ftlvariable name="blockCrossingFiles" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="isDocumentsRequired" type="java.lang.boolean" -->

<#-- @ftlvariable name="cableCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CableCrossingView>" -->
<#-- @ftlvariable name="cableCrossingUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CableCrossingUrlFactory" -->
<#-- @ftlvariable name="cableCrossingFiles" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->

<#-- @ftlvariable name="pipelineCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PipelineCrossingView>" -->
<#-- @ftlvariable name="pipelineCrossingUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PipelineCrossingUrlFactory" -->
<#-- @ftlvariable name="pipelineCrossingFiles" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->

<#-- @ftlvariable name="medianLineCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.MedianLineCrossingView>" -->
<#-- @ftlvariable name="medianLineCrossingUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.MedianLineCrossingUrlFactory" -->
<#-- @ftlvariable name="medianLineCrossingFiles" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->

<#-- @ftlvariable name="crossingAgreementValidationResult" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsValidationResult" -->
<#-- @ftlvariable name="overview" type="uk.co.ogauthority.pwa.model.form.enums.CrossingOverview" -->

<@defaultPage htmlTitle=overview.sectionTitle breadcrumbs=true fullWidthColumn=true>

    <#if errorMessage?has_content>
        <@fdsError.singleErrorSummary errorMessage=errorMessage />
    </#if>

    <h1 class="govuk-heading-xl">${overview.sectionTitle}</h1>

    <#if overview == "LICENCE_AND_BLOCKS">
        <@blockCrossingManagement.blockCrossingManagement
        blockCrossings=blockCrossings
        blockCrossingFileViews=blockCrossingFiles
        urlFactory=blockCrossingUrlFactory
        isDocumentsRequired=isDocumentsRequired/>
    <#elseif overview == "PIPELINE_CROSSINGS">
        <@pipelineCrossingManagement.pipelineCrossingManagement
        urlFactory=pipelineCrossingUrlFactory
        pipelineCrossingFileViews=pipelineCrossingFiles
        pipelineCrossings=pipelineCrossings/>
    <#elseif overview == "CABLE_CROSSINGS">
        <@cableCrossingManagement.cableCrossingManagement
        cableCrossingViews=cableCrossings
        cableCrossingFileViews=cableCrossingFiles
        urlFactory=cableCrossingUrlFactory/>
    <#elseif overview == "MEDIAN_LINE_CROSSING">
        <@medianLineCrossingManagement.medianLineCrossingManagement
        urlFactory=medianLineUrlFactory
        medianLineAgreementView=medianLineAgreementView!""
        medianLineFileViews=medianLineFiles />
    </#if>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true linkSecondaryActionUrl=springUrl(backUrl) primaryButtonText="Complete" secondaryLinkText="Back to blocks and crossing agreements"/>
    </@fdsForm.htmlForm>

</@defaultPage>