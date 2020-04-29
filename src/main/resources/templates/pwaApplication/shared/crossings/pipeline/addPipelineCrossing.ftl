<#include '../../../../layout.ftl'>

<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<@defaultPage htmlTitle="${screenActionType.actionText} pipeline crossing" pageHeading="${screenActionType.actionText} pipeline crossing" breadcrumbs=true>
    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsTextarea.textarea path="form.pipelineCrossed" labelText="Which pipeline will be crossed?" hintText="This may be the pipeline number, or other reference"/>
        <@fdsRadio.radioGroup path="form.pipelineFullyOwnedByOrganisation" labelText="Is the pipeline being crossed fully owned by your organisation?" hiddenContent=true>
          <@fdsRadio.radioYes path="form.pipelineFullyOwnedByOrganisation"/>
          <@fdsRadio.radioNo path="form.pipelineFullyOwnedByOrganisation">
            <@fdsSearchSelector.searchSelectorRest path="form.pipelineOwners" labelText="Who are the owners of the pipeline being crossed?" restUrl=springUrl(orgsRestUrl) multiSelect=true preselectedItems=preselectedOwners nestingPath="form.pipelineFullyOwnedByOrganisation"/>
          </@fdsRadio.radioNo>
        </@fdsRadio.radioGroup>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="${screenActionType.submitButtonText} pipeline crossing" secondaryLinkText="Back to crossings" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>