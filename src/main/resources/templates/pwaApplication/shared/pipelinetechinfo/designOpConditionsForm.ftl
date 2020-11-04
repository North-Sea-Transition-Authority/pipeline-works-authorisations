<#include '../../../layout.ftl'>
<#include 'minMaxInput.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>"-->

<@defaultPage htmlTitle="Design and operating conditions" pageHeading="Design and operating conditions" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList />
    </#if>

    <@fdsForm.htmlForm>

        <@minMaxInput minFormPath="form.temperatureOpMinMax.minValue" maxFormPath="form.temperatureOpMinMax.maxValue"
            labelText="What are the temperature operating conditions?" nestedPath="" unitMeasurement=unitMeasurements.DEGREES_CELSIUS />

        <@minMaxInput minFormPath="form.temperatureDesignMinMax.minValue" maxFormPath="form.temperatureDesignMinMax.maxValue"
            labelText="What are the temperature design conditions?" nestedPath="" unitMeasurement=unitMeasurements.DEGREES_CELSIUS />

        <@minMaxInput minFormPath="form.pressureOpMinMax.minValue" maxFormPath="form.pressureOpMinMax.maxValue"
            labelText="What are the pressure operating conditions?" nestedPath="" unitMeasurement=unitMeasurements.BAR_G/>

        <@fdsFieldset.fieldset legendHeading="What are the pressure design conditions?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend govuk-fieldset__legend--s"
         hintText="If a single value, provide as both the min and max">
            <@fdsTextInput.textInput path="form.pressureDesignMax" labelText="max" suffix=stringUtils.superscriptConverter(unitMeasurements.BAR_G.suffixDisplay) 
             suffixScreenReaderPrompt=unitMeasurements.BAR_G.suffixScreenReaderDisplay inputClass="govuk-input--width-5"/>
        </@fdsFieldset.fieldset>

        <@minMaxInput minFormPath="form.flowrateOpMinMax.minValue" maxFormPath="form.flowrateOpMinMax.maxValue"
            labelText="What are the flowrate operating conditions?" nestedPath="" unitMeasurement=unitMeasurements.KSCM_D />

        <@minMaxInput minFormPath="form.flowrateDesignMinMax.minValue" maxFormPath="form.flowrateDesignMinMax.maxValue"
            labelText="What are the flowrate design conditions?" nestedPath="" unitMeasurement=unitMeasurements.KSCM_D />

        <@fdsFieldset.fieldset legendHeading="What are the U-value design conditions?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend govuk-fieldset__legend--s">
            <@fdsTextInput.textInput path="form.uvalueDesign" labelText="" suffix=unitMeasurements.WM2K.suffixDisplay suffixScreenReaderPrompt=unitMeasurements.WM2K.suffixScreenReaderDisplay inputClass="govuk-input--width-5"/>
        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>