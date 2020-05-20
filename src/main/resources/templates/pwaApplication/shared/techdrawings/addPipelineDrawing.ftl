<#include '../../../layout.ftl'>
<#import '../../../components/widgets/pipelineTableSelection.ftl' as pipelineTableSelection/>

<#-- @ftlvariable name="admiraltyChartUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.AdmiraltyChartUrlFactory" -->
<#-- @ftlvariable name="admiraltyChartFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="admiraltyOptional" type="java.lang.Boolean" -->

<@defaultPage htmlTitle="Technical drawings" pageHeading="Technical drawings" breadcrumbs=true fullWidthColumn=true>

    <@fdsForm.htmlForm>

        <@fdsFileUpload.fileUpload id="drawing-file-upload" path="form.uploadedFileWithDescriptionForms" uploadUrl=springUrl("#") deleteUrl=springUrl("#") downloadUrl=springUrl("#") maxAllowedSize="400" allowedExtensions=".txt"/>

        <@pipelineTableSelection.pipelineTableSelection path="form.pipelineIds" pipelineOverviews=pipelineViews/>

        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>