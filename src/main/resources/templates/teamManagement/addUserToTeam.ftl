<#include '../layout.ftl'>

<@defaultPage htmlTitle="Add user to team" backLink=true topNavigation=true>
    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.userIdentifier" labelText="Add user to team" hintText="Enter person's email address or login ID" pageHeading=true/>
        <@fdsAction.submitButtons primaryButtonText="Next" secondaryLinkText="Cancel" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>