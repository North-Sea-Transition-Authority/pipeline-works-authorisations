<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Median line agreements" breadcrumbs=false>
    <@fdsForm.htmlForm>
        <@fdsRadio.radioGroup path="form.agreementStatus" labelText="Will the proposed works cross the median line?" fieldsetHeadingSize="h1" fieldsetHeadingClass="govuk-fieldset__legend--xl" hiddenContent=true>
            <#assign firstItem=true/>
            <#list crossingOptions as name, displayText>
                <@fdsRadio.radioItem path="form.agreementStatus" itemMap={name:displayText} isFirstItem=firstItem>
                    <#if name == "NEGOTIATIONS_ONGOING">
                        <@fdsTextInput.textInput path="form.negotiatorName" labelText="Name of negotiator" nestingPath="form.negotiatorName"/>
                        <@fdsTextInput.textInput path="form.negotiatorEmail" labelText="Email address of negotiator" nestingPath="form.negotiatorEmail"/>
                    <#elseif name == "NEGOTIATIONS_COMPLETED">
                        <@fdsTextInput.textInput path="form.negotiatorName" labelText="Name of negotiator" nestingPath="form.negotiatorName"/>
                        <@fdsTextInput.textInput path="form.negotiatorEmail" labelText="Email address of negotiator" nestingPath="form.negotiatorEmail"/>
                    </#if>
                </@fdsRadio.radioItem>
                <#assign firstItem=false/>
            </#list>
        </@fdsRadio.radioGroup>
    </@fdsForm.htmlForm>
</@defaultPage>