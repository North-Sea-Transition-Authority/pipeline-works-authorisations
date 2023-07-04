<#import '/spring.ftl' as spring>

<#import '../../fds/components/error/error.ftl' as error>
<#import '../../fds/components/fieldset/fieldset.ftl' as numberFieldset>
<#import '../../fds/utilities/utilities.ftl' as fdsUtil>


<#macro threeNumberInputs
pathOne
pathTwo
pathThree
formId
labelText
hintText=""
optionalLabel=false
nestingPath=""
fieldsetHeadingSize="h2"
fieldsetHeadingClass="govuk-fieldset__legend--s"
formGroupClass=""
caption=""
captionClass="govuk-caption-s"
showLabelOnly=false
noFieldsetHeadingSize="--s"
moreNestedContent="">

    <@spring.bind pathOne/>
    <#local hasErrorOne=fdsUtil.hasSpringStatusErrors()>
    <@spring.bind pathTwo/>
    <#local hasErrorTwo=fdsUtil.hasSpringStatusErrors()>
    <@spring.bind pathThree/>
    <#local hasErrorThree=fdsUtil.hasSpringStatusErrors()>
    <#local hasError=hasErrorOne || hasErrorTwo || hasErrorThree>

    <@numberFieldset.fieldset
    legendHeading=labelText
    legendHeadingSize=fieldsetHeadingSize
    legendHeadingClass=fieldsetHeadingClass
    caption=caption
    captionClass=captionClass
    optionalLabel=optionalLabel
    hintText=hintText
    formErrorId=formId
    formHasError=hasError
    showHeadingOnly=showLabelOnly
    noFieldsetHeadingSize=noFieldsetHeadingSize
    formGroupClass=formGroupClass>

        <#if hasErrorOne>
            <@spring.bind pathOne/>
            <@error.inputError inputId="${formId}"/>
        </#if>
        <#if hasErrorTwo>
            <@spring.bind pathTwo/>
            <@error.inputError inputId="${formId}"/>
        </#if>
        <#if hasErrorThree>
            <@spring.bind pathThree/>
            <@error.inputError inputId="${formId}"/>
        </#if>

        ${moreNestedContent}

      <div class="govuk-date-input" id="${formId}-number-input">
          <#--Add more numberInputItem macros as needed-->
          <#nested>
      </div>
    </@numberFieldset.fieldset>

<#--Rebind your form when a component is used inside show/hide radio groups-->
    <#if nestingPath?has_content>
        <@spring.bind nestingPath/>
    </#if>
</#macro>