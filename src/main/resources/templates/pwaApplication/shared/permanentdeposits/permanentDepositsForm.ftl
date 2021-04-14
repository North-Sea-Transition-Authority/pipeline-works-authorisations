<#include '../../../layout.ftl'>
<#import '../../../components/coordinates/coordinateInput.ftl' as coordinateInput/>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="pipelines" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="materialTypes" type="java.util.List<MaterialType>" -->
<#-- @ftlvariable name="longDirections" type="java.util.List<LongitudeDirection>" -->
<#-- @ftlvariable name="proposedStartDate" type="java.lang.String" -->

<@defaultPage htmlTitle="${screenAction.actionText} permanent deposit" pageHeading="${screenAction.actionText} permanent deposit" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.depositReference" labelText="Deposit reference" hintText="Uniquely identifies this deposit on this application, e.g. PL1234 grout bags"/>

        <@fdsRadio.radioGroup path="form.depositIsForConsentedPipeline" labelText="Is the deposit for a consented pipeline or a pipeline that is on this application?" hiddenContent=true>
            <@fdsRadio.radioYes path="form.depositIsForConsentedPipeline">
                <@fdsSearchSelector.searchSelectorEnhanced path="form.selectedPipelines" options=pipelines labelText="Pipelines" multiSelect=true
                    hintText="Only add more than one pipeline on the same deposit if they’re within the same trench or piggy-backed." nestingPath="form.depositIsForConsentedPipeline"/>
            </@fdsRadio.radioYes>
            <@fdsRadio.radioNo path="form.depositIsForConsentedPipeline"/>
        </@fdsRadio.radioGroup>

        <@fdsRadio.radioGroup path="form.depositIsForPipelinesOnOtherApp" labelText="Is the deposit for proposed pipelines on other applications that haven’t yet been consented?"
                hintText="You can only add pipelines submitted on applications for the same master PWA" hiddenContent=true>
            <@fdsRadio.radioYes path="form.depositIsForPipelinesOnOtherApp">
                <@fdsTextarea.textarea path="form.appRefAndPipelineNum" labelText="Enter the application reference and proposed pipeline number for each pipeline" characterCount=true maxCharacterLength="4000" nestingPath="form.depositIsForPipelinesOnOtherApp"/>
            </@fdsRadio.radioYes>
            <@fdsRadio.radioNo path="form.depositIsForPipelinesOnOtherApp"/>
        </@fdsRadio.radioGroup>

        <@fdsNumberInput.twoNumberInputs pathOne="form.fromDate.month" pathTwo="form.fromDate.year" labelText="Month and year of deposit start" formId="from-month-year">
            <@fdsNumberInput.numberInputItem path="form.fromDate.month" labelText="Month" inputClass="govuk-input--width-2"/>
            <@fdsNumberInput.numberInputItem path="form.fromDate.year" labelText="Year" inputClass="govuk-input--width-4"/>
        </@fdsNumberInput.twoNumberInputs>

        <@fdsNumberInput.twoNumberInputs pathOne="form.toDate.month" pathTwo="form.toDate.year" labelText="Month and year of deposit end" formId="to-month-year">
            <@fdsNumberInput.numberInputItem path="form.toDate.month" labelText="Month" inputClass="govuk-input--width-2"/>
            <@fdsNumberInput.numberInputItem path="form.toDate.year" labelText="Year" inputClass="govuk-input--width-4"/>
        </@fdsNumberInput.twoNumberInputs>


        <@fdsRadio.radioGroup path="form.materialType" labelText="Select the material type to be used" hiddenContent=true>

            <#assign firstItem=true/>
            <#assign contingencyGuidance = "Contingency should be included if appropriate as if more is required you will have to request a new consent"/>
            <#assign contingencyLabelText = "How much contingency was included? (optional)"/>

            <#list materialTypes as  materialTypeOption>
                <@fdsRadio.radioItem path="form.materialType" itemMap={materialTypeOption : materialTypeOption.getDisplayText()} isFirstItem=firstItem>
                    <#assign hintText = "e.g. \"the above deposits include a contingency of 10 mattresses\" or \"includes 5% rock as a contingency\""/>
                    <#if materialTypeOption == "CONCRETE_MATTRESSES">
                        <@fdsTextInput.textInput path="form.concreteMattressLength" nestingPath="form.materialType" labelText="Length" suffix="m" inputClass="govuk-input--width-10"/>
                        <@fdsTextInput.textInput path="form.concreteMattressWidth" nestingPath="form.materialType" labelText="Width" suffix="m" inputClass="govuk-input--width-10"/>
                        <@fdsTextInput.textInput path="form.concreteMattressDepth" nestingPath="form.materialType" labelText="Depth" suffix="m" inputClass="govuk-input--width-10"/>
                        <@fdsTextInput.textInput path="form.quantityConcrete" nestingPath="form.materialType" labelText="Enter quantity of material to be used"/>
                        <@fdsInsetText.insetText> ${contingencyGuidance} </@fdsInsetText.insetText>
                        <@fdsTextInput.textInput path="form.contingencyConcreteAmount" nestingPath="form.materialType" labelText=contingencyLabelText hintText=hintText/>

                    <#elseif materialTypeOption == "ROCK">
                        <@fdsTextInput.textInput path="form.rocksSize" nestingPath="form.materialType" labelText="Size" hintText="For example 1-5" suffix="Grade" inputClass="govuk-input--width-10"/>
                        <@fdsTextInput.textInput path="form.quantityRocks" nestingPath="form.materialType" labelText="Quantity of material to be used (decimal tonnes)" inputClass="govuk-input--width-10"/>
                        <@fdsInsetText.insetText> ${contingencyGuidance} </@fdsInsetText.insetText>
                        <@fdsTextInput.textInput path="form.contingencyRocksAmount" nestingPath="form.materialType" labelText=contingencyLabelText hintText=hintText/>

                    <#elseif materialTypeOption == "GROUT_BAGS">
                        <@fdsTextInput.textInput path="form.groutBagsSize" nestingPath="form.materialType" labelText="Size" suffix="kg" inputClass="govuk-input--width-20"/>
                        <@fdsRadio.radioGroup path="form.groutBagsBioDegradable" nestingPath="form.materialType" labelText="Are the grout bags bio-degradable?" hiddenContent=true>
                            <@fdsRadio.radioYes path="form.groutBagsBioDegradable"/>
                            <@fdsRadio.radioNo path="form.groutBagsBioDegradable">
                                <@fdsTextInput.textInput path="form.bioGroutBagsNotUsedDescription" nestingPath="form.materialType" labelText="Why are bio-degradable grout bags not being used?" maxCharacterLength="4000"/>
                            </@fdsRadio.radioNo>
                        </@fdsRadio.radioGroup>
                        <@fdsTextInput.textInput path="form.quantityGroutBags" nestingPath="form.materialType" labelText="Quantity of material to be used" inputClass="govuk-input--width-20"/>
                        <@fdsInsetText.insetText> ${contingencyGuidance} </@fdsInsetText.insetText>
                        <@fdsTextInput.textInput path="form.contingencyGroutBagsAmount" nestingPath="form.materialType" labelText=contingencyLabelText hintText=hintText/>

                    <#elseif materialTypeOption == "OTHER">
                        <@fdsTextInput.textInput path="form.otherMaterialType" nestingPath="form.materialType" labelText="Deposit material" inputClass="govuk-input--width-20"/>
                        <@fdsTextInput.textInput path="form.otherMaterialSize" nestingPath="form.materialType" labelText="Size" inputClass="govuk-input--width-20"/>
                        <@fdsTextInput.textInput path="form.quantityOther" nestingPath="form.materialType" labelText="Quantity of material to be used" inputClass="govuk-input--width-20"/>
                        <@fdsInsetText.insetText> ${contingencyGuidance} </@fdsInsetText.insetText>
                        <@fdsTextInput.textInput path="form.contingencyOtherAmount" nestingPath="form.materialType" labelText=contingencyLabelText hintText=hintText/>
                    </#if>
                </@fdsRadio.radioItem>
            <#assign firstItem=false/>
            </#list>
        </@fdsRadio.radioGroup>


        <@fdsFieldset.fieldset legendHeading="Where is the start location?">
            <@coordinateInput.latitudeInput degreesLocationPath="form.fromCoordinateForm.latitudeDegrees"
                                          minutesLocationPath="form.fromCoordinateForm.latitudeMinutes"
                                          secondsLocationPath="form.fromCoordinateForm.latitudeSeconds"
                                          formId="fromLatitude"
                                          labelText="Start point latitude"/>

            <@coordinateInput.longitudeInput degreesLocationPath="form.fromCoordinateForm.longitudeDegrees"
                                          minutesLocationPath="form.fromCoordinateForm.longitudeMinutes"
                                          secondsLocationPath="form.fromCoordinateForm.longitudeSeconds"
                                          direction="EW"
                                          directionPath="form.fromCoordinateForm.longitudeDirection"
                                          directionList=longDirections
                                          formId="fromLongitude"
                                          labelText="Start point longitude"/>
        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Where is the end location?">
            <@coordinateInput.latitudeInput degreesLocationPath="form.toCoordinateForm.latitudeDegrees"
                                          minutesLocationPath="form.toCoordinateForm.latitudeMinutes"
                                          secondsLocationPath="form.toCoordinateForm.latitudeSeconds"
                                          formId="toLatitude"
                                          labelText="Finish point latitude"/>

            <@coordinateInput.longitudeInput degreesLocationPath="form.toCoordinateForm.longitudeDegrees"
                                          minutesLocationPath="form.toCoordinateForm.longitudeMinutes"
                                          secondsLocationPath="form.toCoordinateForm.longitudeSeconds"
                                          direction="EW"
                                          directionPath="form.toCoordinateForm.longitudeDirection"
                                          directionList=longDirections
                                          formId="toLongitude"
                                          labelText="Finish point longitude"/>
        </@fdsFieldset.fieldset>

        <@fdsTextarea.textarea path="form.footnote" labelText="Is there any other important information relevant to this deposit?" maxCharacterLength="4000" characterCount=true optionalLabel=true hintText="This will be included on the consent if granted"/>
        <@fdsDetails.summaryDetails summaryTitle="Show some examples of relevant information">
            <ol class="govuk-list govuk-list--number">
                <li> The above is for a change of date only and was previously consented under XX/D/YY which has now expired. </li>
                <li> The above is for additional deposits, to those consented under XX/D/YY. </li>
                <li> The coordinates are a central point, the deposits will be place in a X meter radius of this central point </li>
                <li> Col 1 PLX (PWA XX/W/YY and PLY (ZZ/W/YY) which are covered under separate PWAS are PL that are piggy-backed.  Therefore the consent is requested un PLX (PWAXX/W/YY) but the deposits will cover both pipelines. </li>
            </ul>
        </@fdsDetails.summaryDetails>

        <@fdsAction.submitButtons primaryButtonText="${screenAction.submitButtonText} deposit" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(backUrl)/>

    </@fdsForm.htmlForm>

</@defaultPage>