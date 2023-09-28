<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->

<#include '../layout.ftl'>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle pageHeadingClass="govuk-heading-l" fullWidthColumn=false wrapperWidth=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList/>
    </#if>

    <@fdsForm.htmlForm>
        <#if !existingRecord>
            <@fdsSearchSelector.searchSelectorEnhanced
            path="form.pwaId"
            labelText="PWA reference"
            options=pwaSelectorOptions
            inputWidth="25"
            />
        </#if>

        <@fdsTextInput.textInput path="form.variationTerm" labelText="Variation term" inputClass="govuk-input--width-2" />

        <@threeNumberInput.threeNumberInputs labelText="HUOO terms" formId="huoo" pathOne="form.huooTermOne" pathTwo="form.huooTermTwo" pathThree="form.huooTermThree">
            <@fdsNumberInput.numberInputItem path="form.huooTermOne" labelText="Term one"/>
            <@fdsNumberInput.numberInputItem path="form.huooTermTwo" labelText="Term two"/>
            <@fdsNumberInput.numberInputItem path="form.huooTermThree" labelText="Term three"/>
        </@threeNumberInput.threeNumberInputs>

        <@fdsNumberInput.twoNumberInputs labelText="Depcon" formId="depcon" pathOne="form.depconParagraph" pathTwo="form.depconSchedule">
            <@fdsNumberInput.numberInputItem path="form.depconParagraph" labelText="Paragraph"/>
            <@fdsNumberInput.numberInputItem path="form.depconSchedule" labelText="Schedule"/>
        </@fdsNumberInput.twoNumberInputs>

        <@fdsAction.submitButtons primaryButtonText="Submit" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>
