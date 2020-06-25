<#include '../../../layout.ftl'>

<#-- @ftlvariable name="pageHeading" type="java.lang.String" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="backLinkText" type="java.lang.String" -->
<#-- @ftlvariable name="submitButtonText" type="java.lang.String" -->
<#-- @ftlvariable name="pickablePipelineOptions" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickablePipelineOption>" -->

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading breadcrumbs=false fullWidthColumn=true>

    <@fdsForm.htmlForm>

        <@pwaPipelineTableSelection.pickablePipelineTableSelection path="form.pickedPipelineStrings"
        pickablePipelineOptions=pickablePipelineOptions/>

        <@fdsAction.submitButtons
          primaryButtonText="Continue"
          linkSecondaryAction=true
          secondaryLinkText=backLinkText
          linkSecondaryActionUrl=springUrl(backUrl)
        />
    </@fdsForm.htmlForm>

</@defaultPage>