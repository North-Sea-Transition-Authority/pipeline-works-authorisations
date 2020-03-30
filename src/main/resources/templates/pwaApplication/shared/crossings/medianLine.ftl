<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Median line agreements" breadcrumbs=false>
    <@fdsForm.htmlForm>
        <@fdsRadio.radioGroup path="form.agreementStatus" labelText="Will the proposed works cross the median line?" fieldsetHeadingSize="h1" fieldsetHeadingClass="govuk-fieldset__legend--xl" hiddenContent=true>
            <#assign firstItem=true/>
            <#list crossingOptions as name, displayText>
                <@fdsRadio.radioItem path="form.agreementStatus" itemMap={name:displayText} isFirstItem=firstItem/>
                <#assign firstItem=false/>
            </#list>
        </@fdsRadio.radioGroup>
    </@fdsForm.htmlForm>
</@defaultPage>