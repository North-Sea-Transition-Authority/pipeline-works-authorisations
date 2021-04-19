<#include '../../../layout.ftl'>
<#include 'minMaxInput.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>"-->

<@defaultPage htmlTitle="Design and operating conditions" pageHeading="Design and operating conditions" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>

        <@minMaxInput minFormPath="form.temperatureOpMinMax.minValue" maxFormPath="form.temperatureOpMinMax.maxValue"
            labelText="What are the temperature operating conditions?" nestedPath="" unitMeasurement=unitMeasurements.DEGREES_CELSIUS />

        <@minMaxInput minFormPath="form.temperatureDesignMinMax.minValue" maxFormPath="form.temperatureDesignMinMax.maxValue"
            labelText="What are the temperature design conditions?" nestedPath="" unitMeasurement=unitMeasurements.DEGREES_CELSIUS />

        <@minMaxInput minFormPath="form.pressureOpMinMax.minValue" maxFormPath="form.pressureOpMinMax.maxValue"
            labelText="What are the pressure operating conditions?" nestedPath="" unitMeasurement=unitMeasurements.BAR_G/>

        <@fdsFieldset.fieldset legendHeading="What is the maximum design pressure condition?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend govuk-fieldset__legend--s">
            <@fdsTextInput.textInput path="form.pressureDesignMax" labelText="max" suffix=stringUtils.superscriptConverter(unitMeasurements.BAR_G.suffixDisplay)
             suffixScreenReaderPrompt=unitMeasurements.BAR_G.suffixScreenReaderDisplay inputClass="govuk-input--width-5"/>
        </@fdsFieldset.fieldset>

        <@minMaxInput minFormPath="form.flowrateOpMinMax.minValue" maxFormPath="form.flowrateOpMinMax.maxValue"
            labelText="What are the flowrate operating conditions?" nestedPath="" unitMeasurement=unitMeasurements.KSCM_D />

        <@minMaxInput minFormPath="form.flowrateDesignMinMax.minValue" maxFormPath="form.flowrateDesignMinMax.maxValue"
            labelText="What are the flowrate design conditions?" nestedPath="" unitMeasurement=unitMeasurements.KSCM_D />

        <@fdsFieldset.fieldset legendHeading="What is the U-value design condition?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend govuk-fieldset__legend--s">
            <@fdsTextInput.textInput path="form.uvalueDesign" labelText="" suffix=stringUtils.superscriptConverter(unitMeasurements.WM2K.suffixDisplay) suffixScreenReaderPrompt=unitMeasurements.WM2K.suffixScreenReaderDisplay inputClass="govuk-input--width-5"/>
        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>