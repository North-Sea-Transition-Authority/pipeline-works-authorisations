<#-- @ftlvariable name="teamName" type="String" -->
<#-- @ftlvariable name="roles" type="java.util.Map<String,String>" -->
<#-- @ftlvariable name="userName" type="String" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="error" type="String" -->
<#-- @ftlvariable name="showTopNav" type="java.lang.Boolean" -->

<#include '../layout.ftl'>

<@defaultPage htmlTitle=teamName twoThirdsColumn=false backLink=true topNavigation=showTopNav>

    <#if error?has_content>
        <@fdsError.singleErrorSummary errorMessage=error/>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsCheckbox.checkboxes path="form.userRoles" checkboxes=roles fieldsetHeadingText="What actions does " + userName + " perform?" fieldsetHeadingSize="h1" fieldsetHeadingClass="govuk-fieldset__legend--l"/>
        <@fdsAction.submitButtons primaryButtonText="Save" secondaryLinkText="Cancel" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>