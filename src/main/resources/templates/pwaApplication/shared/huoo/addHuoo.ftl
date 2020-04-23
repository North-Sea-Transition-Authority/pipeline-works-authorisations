<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->

<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="${screenActionType.actionText} a holder, user, operator, or owner" pageHeading="${screenActionType.actionText} a holder, user, operator, or owner" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsInsetText.insetText>
        If you are unable to find the legal entity or treaty agreement you are looking for, please contact the OGA.
    </@fdsInsetText.insetText>

    <@fdsForm.htmlForm>
        <@fdsRadio.radioGroup path="form.huooType" labelText="What type of holder, user, operator or owner is being added?" hiddenContent=true>
            <#assign firstItem=false/>
            <#list huooTypes as name, displayText>
                <@fdsRadio.radioItem path="form.huooType" itemMap={name:displayText} isFirstItem=false>
                    <#if name == "PORTAL_ORG">
                        <@fdsSearchSelector.searchSelectorEnhanced path="form.organisationUnit" options=portalOrgs labelText="Select the legal entity" nestingPath="form.huooType"/>
                        <@fdsCheckbox.checkboxes path="form.huooRoles" checkboxes=huooRoles fieldsetHeadingText="Which roles will the legal entity have?" fieldsetHeadingSize="h3" fieldsetHeadingClass="govuk-fieldset__legend--s" nestingPath="form.huooType"/>
                    <#else>
                        <@fdsRadio.radio path="form.treatyAgreement" labelText="Which country is the treaty agreement with?" radioItems=treatyAgreements nestingPath="form.huooType" fieldsetHeadingSize="h3" fieldsetHeadingClass="govuk-fieldset__legend--s"/>
                    </#if>
                </@fdsRadio.radioItem>
                <#assign firstItem=true/>
            </#list>
        </@fdsRadio.radioGroup>
        <@fdsAction.submitButtons primaryButtonText="${screenActionType.submitButtonText} holder, user, operator, or owner" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>