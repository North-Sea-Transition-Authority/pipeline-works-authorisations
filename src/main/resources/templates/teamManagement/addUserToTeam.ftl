<#-- @ftlvariable name="groupName" type="String" -->
<#-- @ftlvariable name="showTopNav" type="java.lang.Boolean" -->

<#include '../layout.ftl'>

<@defaultPage htmlTitle="Add user to ${groupName}" backLink=true topNavigation=showTopNav twoThirdsColumn=false>
    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.userIdentifier" labelText="Add user to ${groupName}" hintText="Enter person's email address or login ID" pageHeading=true/>
        <@fdsAction.submitButtons primaryButtonText="Next" secondaryLinkText="Cancel" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>