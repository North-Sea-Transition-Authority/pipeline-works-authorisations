<#-- @ftlvariable name="groupName" type="String" -->
<#-- @ftlvariable name="showTopNav" type="java.lang.Boolean" -->
<#-- @ftlvariable name="ogaRegistrationLink" type="String" -->

<#include '../layout.ftl'>

<@defaultPage htmlTitle="Add user to ${groupName}" backLink=true topNavigation=showTopNav>
    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.userIdentifier" labelText="Add user to ${groupName}" hintText="Enter person's email address or login ID" pageHeading=true/>
    
        <@fdsDetails.summaryDetails summaryTitle="The user I want to add does not have an account">
            A user must have an account on the Energy Portal before you can add them to your organisation. A user can register for an account on the Energy Portal at 
            <@fdsAction.link linkText=ogaRegistrationLink! linkUrl=ogaRegistrationLink!/>
        </@fdsDetails.summaryDetails>

        <@fdsAction.submitButtons primaryButtonText="Next" secondaryLinkText="Cancel" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>