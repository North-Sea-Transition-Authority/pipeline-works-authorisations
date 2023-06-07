<#include '../../../layout.ftl'>
<#import 'blockCrossingsManagement.ftl' as blockCrossingManagement>
<#import 'medianLineCrossingManagement.ftl' as medianLineCrossingManagement>
<#import 'cableCrossingManagement.ftl' as cableCrossingManagement>
<#import 'pipeline/pipelineCrossingManagement.ftl' as pipelineCrossingManagement>

<#-- @ftlvariable name="errorMessage" type="String" -->

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingUrlFactory" type="uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.BlockCrossingUrlFactory" -->
<#-- @ftlvariable name="blockCrossingFiles" type="java.util.List<uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView>" -->
<#-- @ftlvariable name="isDocumentsRequired" type="java.lang.boolean" -->

<#-- @ftlvariable name="cableCrossings" type="java.util.List<uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.CableCrossingView>" -->
<#-- @ftlvariable name="cableCrossingUrlFactory" type="uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.CableCrossingUrlFactory" -->
<#-- @ftlvariable name="cableCrossingFiles" type="java.util.List<uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView>" -->

<#-- @ftlvariable name="pipelineCrossings" type="java.util.List<uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PipelineCrossingView>" -->
<#-- @ftlvariable name="pipelineCrossingUrlFactory" type="uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PipelineCrossingUrlFactory" -->
<#-- @ftlvariable name="pipelineCrossingFiles" type="java.util.List<uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView>" -->

<#-- @ftlvariable name="medianLineCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.MedianLineCrossingView>" -->
<#-- @ftlvariable name="medianLineCrossingUrlFactory" type="uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.MedianLineCrossingUrlFactory" -->
<#-- @ftlvariable name="medianLineCrossingFiles" type="java.util.List<uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView>" -->

<#-- @ftlvariable name="crossingAgreementValidationResult" type="uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsValidationResult" -->
<#-- @ftlvariable name="overview" type="uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingOverview" -->

<#-- @ftlvariable name="form" type="uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.HasBlockCrossingForm" -->


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
        <@fdsForm.htmlForm>
            <@fdsRadio.radio
            radioItems=radioItems
            path="form.hasMoreBlocks"
            labelText="Do you want to add another associated licence block?"/>
            <@fdsAction.button buttonText="Save and continue"/>
        </@fdsForm.htmlForm>
    <#elseif overview == "PIPELINE_CROSSINGS">
        <@pipelineCrossingManagement.pipelineCrossingManagement
        urlFactory=pipelineCrossingUrlFactory
        pipelineCrossingFileViews=pipelineCrossingFiles
        pipelineCrossings=pipelineCrossings/>
        <@fdsForm.htmlForm>
            <@fdsAction.submitButtons linkSecondaryAction=true linkSecondaryActionUrl=springUrl(backUrl) primaryButtonText="Complete" secondaryLinkText="Back to blocks and crossing agreements"/>
        </@fdsForm.htmlForm>
    <#elseif overview == "CABLE_CROSSINGS">
        <@cableCrossingManagement.cableCrossingManagement
        cableCrossingViews=cableCrossings
        cableCrossingFileViews=cableCrossingFiles
        urlFactory=cableCrossingUrlFactory/>
        <@fdsForm.htmlForm>
            <@fdsAction.submitButtons linkSecondaryAction=true linkSecondaryActionUrl=springUrl(backUrl) primaryButtonText="Complete" secondaryLinkText="Back to blocks and crossing agreements"/>
        </@fdsForm.htmlForm>
    <#elseif overview == "MEDIAN_LINE_CROSSING">
        <@medianLineCrossingManagement.medianLineCrossingManagement
        urlFactory=medianLineUrlFactory
        medianLineAgreementView=medianLineAgreementView!""
        medianLineFileViews=medianLineFiles />
        <@fdsForm.htmlForm>
            <@fdsAction.submitButtons linkSecondaryAction=true linkSecondaryActionUrl=springUrl(backUrl) primaryButtonText="Complete" secondaryLinkText="Back to blocks and crossing agreements"/>
        </@fdsForm.htmlForm>
    </#if>
</@defaultPage>
