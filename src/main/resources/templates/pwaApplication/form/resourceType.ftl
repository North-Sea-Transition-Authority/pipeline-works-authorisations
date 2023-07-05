<#include '../../layout.ftl'>

<#-- @ftlvariable name="resourceOptions" type="java.util.List<uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType>" -->


<@defaultPage htmlTitle="Resource type" errorItems=errorList backLink=true>

    <@fdsForm.htmlForm>
        <@fdsRadio.radioGroup
          path="form.resourceType"
          labelText="Select the resource type"
          fieldsetHeadingClass="govuk-fieldset__legend--l"
          fieldsetHeadingSize="h1">
            <#list resourceOptions as resourceOption>
                <#assign resourceTypeName = resourceOption.name()/>
                <#assign displayName = resourceOption.displayName/>
                <@fdsRadio.radioItem path="form.resourceType" itemMap={resourceTypeName: "PWA - " + displayName}/>
            </#list>
        </@fdsRadio.radioGroup>
        <@fdsAction.submitButtons primaryButtonText="Continue" linkSecondaryAction=true secondaryLinkText="Back to work area" linkSecondaryActionUrl=springUrl(workareaUrl) />
    </@fdsForm.htmlForm>

</@defaultPage>
