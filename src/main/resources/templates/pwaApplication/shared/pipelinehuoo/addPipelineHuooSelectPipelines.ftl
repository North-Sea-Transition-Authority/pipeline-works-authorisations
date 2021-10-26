<#include '../../../layout.ftl'>
<#import 'pipelineHuooTableSelection.ftl' as pwaPipelineHuooTableSelection/>

<#-- @ftlvariable name="pageHeading" type="java.lang.String" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="backLinkText" type="java.lang.String" -->
<#-- @ftlvariable name="submitButtonText" type="java.lang.String" -->
<#-- @ftlvariable name="pickableHuooPipelineOptions" type="java.util.List<uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickableHuooPipelineOption>" -->

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading breadcrumbs=false fullWidthColumn=true>

    <@fdsForm.htmlForm>

        <@pwaPipelineHuooTableSelection.pickablePipelineTableSelection path="form.pickedPipelineStrings"
        pickableHuooPipelineOptions=pickableHuooPipelineOptions/>

        <@fdsAction.submitButtons
          primaryButtonText="Continue"
          linkSecondaryAction=true
          secondaryLinkText=backLinkText
          linkSecondaryActionUrl=springUrl(backUrl)
        />
    </@fdsForm.htmlForm>

</@defaultPage>