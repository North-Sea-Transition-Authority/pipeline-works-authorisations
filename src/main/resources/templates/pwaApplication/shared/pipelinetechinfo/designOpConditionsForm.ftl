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

        <@minMaxInput minFormPath="form.pressureOpInternalExternal.minValue" maxFormPath="form.pressureOpInternalExternal.maxValue"
            labelText="What are the pressure operating conditions?" nestedPath="" unitMeasurement=unitMeasurements.BAR_G altMinLabel="internal" altMaxLabel="external"/>

        <@minMaxInput minFormPath="form.pressureDesignInternalExternal.minValue" maxFormPath="form.pressureDesignInternalExternal.maxValue"
            labelText="What are the pressure design conditions?" nestedPath="" unitMeasurement=unitMeasurements.BAR_G altMinLabel="internal" altMaxLabel="external"/>

        <@minMaxInput minFormPath="form.flowrateOpMinMax.minValue" maxFormPath="form.flowrateOpMinMax.maxValue"
            labelText="What are the flowrate operating conditions?" nestedPath="" unitMeasurement=unitMeasurements.KSCM_D />

        <@minMaxInput minFormPath="form.flowrateDesignMinMax.minValue" maxFormPath="form.flowrateDesignMinMax.maxValue"
            labelText="What are the flowrate design conditions?" nestedPath="" unitMeasurement=unitMeasurements.KSCM_D />

        <@fdsFieldset.fieldset legendHeading="What are the U-value operating conditions?" legendHeadingSize="h5" legendHeadingClass="govuk-fieldset__legend--m">
            <@fdsTextInput.textInput path="form.uvalueOp" labelText="" suffix=unitMeasurements.KSCM_D.suffixDisplay suffixScreenReaderPrompt=unitMeasurements.KSCM_D.suffixScreenReaderDisplay inputClass="govuk-input--width-5"/>
        </@fdsFieldset.fieldset>
        <@fdsFieldset.fieldset legendHeading="What are the U-value design conditions?" legendHeadingSize="h5" legendHeadingClass="govuk-fieldset__legend--m">
            <@fdsTextInput.textInput path="form.uvalueDesign" labelText="" suffix=unitMeasurements.KSCM_D.suffixDisplay suffixScreenReaderPrompt=unitMeasurements.KSCM_D.suffixScreenReaderDisplay inputClass="govuk-input--width-5"/>
        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>