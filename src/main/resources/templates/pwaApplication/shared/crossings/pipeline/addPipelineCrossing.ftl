<#include '../../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="${screenActionType.actionText} pipeline crossing" pageHeading="${screenActionType.actionText} pipeline crossing" breadcrumbs=true errorItems=errorList>
    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.pipelineCrossed" labelText="Which pipeline will be crossed?" hintText="This will be the PL number or other reference if not part of the PWA regime"/>
        <@fdsRadio.radioGroup path="form.pipelineFullyOwnedByOrganisation" labelText="Is the pipeline being crossed fully owned by your organisation?" hiddenContent=true>
          <@fdsRadio.radioYes path="form.pipelineFullyOwnedByOrganisation"/>
          <@fdsRadio.radioNo path="form.pipelineFullyOwnedByOrganisation">
            <@fdsSearchSelector.searchSelectorRest path="form.pipelineOwners" labelText="Who are the owners of the pipeline being crossed?" restUrl=springUrl(orgsRestUrl) multiSelect=true preselectedItems=preselectedOwners nestingPath="form.pipelineFullyOwnedByOrganisation"/>
          </@fdsRadio.radioNo>
        </@fdsRadio.radioGroup>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="${screenActionType.submitButtonText} pipeline crossing" secondaryLinkText="Back to pipeline crossings" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>