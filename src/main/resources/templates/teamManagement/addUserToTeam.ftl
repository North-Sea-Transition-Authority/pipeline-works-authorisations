<#-- @ftlvariable name="groupName" type="String" -->
<#-- @ftlvariable name="showTopNav" type="java.lang.Boolean" -->
<#-- @ftlvariable name="ogaRegistrationLink" type="String" -->

<#include '../layout.ftl'>

<@defaultPage htmlTitle="Add user to ${groupName}" backLink=true topNavigation=showTopNav twoThirdsColumn=true>
    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.userIdentifier" labelText="Add user to ${groupName}" hintText="Enter person's email address or login ID" pageHeading=true/>

        <#assign ogaLinkTag = '<a href="${(ogaRegistrationLink)!}">${(ogaRegistrationLink)!}</a>' /> 
        <@fdsDetails.details detailsTitle="The user I want to add does not have an account" 
        detailsText="A user must have an account on the Energy Portal before you can add them to your organisation. A user can register for an account on the Energy Portal at ${(ogaLinkTag)!}."/>

        <@fdsAction.submitButtons primaryButtonText="Next" secondaryLinkText="Cancel" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>