<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="ogaServiceDeskEmail" type="String" -->

<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="${screenActionType.actionText} a holder, user, operator, or owner" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsRadio.radioGroup path="form.huooType" labelText="What type of holder, user, operator or owner is being added?" fieldsetHeadingSize="h1" fieldsetHeadingClass="govuk-fieldset__legend--xl" hiddenContent=true>
            <@fdsInsetText.insetText>
                Add a treaty agreement as the user if the application is regarding a transboundary pipeline. You cannot define both a treaty agreement and legal entities as users.
            </@fdsInsetText.insetText>

            <#assign firstItem=false/>
            <#list huooTypes as name, displayText>
                <@fdsRadio.radioItem path="form.huooType" itemMap={name:displayText} isFirstItem=false>
                    <#if name == "PORTAL_ORG">
                        <@fdsSearchSelector.searchSelectorEnhanced path="form.organisationUnitId" options=portalOrgs labelText="Select the legal entity" nestingPath="form.huooType"/>
                        <@fdsDetails.details detailsTitle="I can’t find a legal entity"
                            detailsText="If you are unable to find the legal entity you are looking for then provide the OGA with the holder company name, address, postcode and companies house registration number to add to the PWA service: ${ogaServiceDeskEmail}"/>
                        <@fdsCheckbox.checkboxes path="form.huooRoles" checkboxes=huooRoles fieldsetHeadingText="Which roles will the legal entity have?" nestingPath="form.huooType"/>
                    </#if>
                </@fdsRadio.radioItem>
                <#assign firstItem=true/>
            </#list>
        </@fdsRadio.radioGroup>


        <@fdsAction.submitButtons primaryButtonText="${screenActionType.submitButtonText} holder, user, operator, or owner" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>