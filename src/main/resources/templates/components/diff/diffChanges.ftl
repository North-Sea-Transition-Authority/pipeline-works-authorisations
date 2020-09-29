<#include '../../layout.ftl'>

<#function springUrl url>
  <#local springUrl>
    <@spring.url url/>
  </#local>
  <#return springUrl>
</#function>

<#--Show changes past and present-->
<#macro diffChanges>
<#-- Always hide diff-changes elements by default ready to be show -->
  <div class="diff-changes">
    <#nested>
  </div>
</#macro>

<#macro diffChangesInsert>
  <span class="govuk-visually-hidden">start insertion,</span>
  <ins class="diff-changes__insert">
    <#nested>
  </ins>
  <span class="govuk-visually-hidden">end insertion,</span>
</#macro>

<#macro diffChangesDelete>
  <span class="govuk-visually-hidden">start deletion,</span>
  <del class="diff-changes__delete">
    <#nested>
  </del>
  <span class="govuk-visually-hidden">end deletion,</span>
</#macro>


<#macro renderDiffLink diffType diffedLinkText diffedLinkUrl>
  <#if diffType == "ADDED">
    <@diffChanges>

      <@diffChangesInsert>
        <@fdsAction.link linkText=diffedLinkText.currentValue linkClass="govuk-link govuk-link--button" linkUrl=springUrl(diffedLinkUrl.currentValue)/>
      </@diffChangesInsert>

    </@diffChanges>

    <span class="diff-raw-value"><@fdsAction.link linkText=diffedLinkText.currentValue linkClass="govuk-link govuk-link--button" linkUrl=springUrl(diffedLinkUrl.currentValue)/></span>
  </#if>

  <#if diffType == "UPDATED">
    <@diffChanges>

      <@diffChangesDelete>
        <@fdsAction.link linkText=diffedLinkText.previousValue linkClass="govuk-link govuk-link--button" linkUrl=springUrl(diffedLinkUrl.previousValue)/>
      </@diffChangesDelete>

      <@diffChangesInsert>
        <@fdsAction.link linkText=diffedLinkText.currentValue linkClass="govuk-link govuk-link--button" linkUrl=springUrl(diffedLinkUrl.currentValue)/>
      </@diffChangesInsert>

    </@diffChanges>

    <span class="diff-raw-value"><@fdsAction.link linkText=diffedLinkText.currentValue linkClass="govuk-link govuk-link--button" linkUrl=springUrl(diffedLinkUrl.currentValue)/></span>
  </#if>

  <#if diffType == "DELETED">
    <@diffChanges>

      <@diffChangesDelete>
        <@fdsAction.link linkText=diffedLinkText.previousValue linkClass="govuk-link govuk-link--button" linkUrl=springUrl(diffedLinkUrl.previousValue)/>
      </@diffChangesDelete>

    </@diffChanges>
  </#if>

  <#if diffType == "UNCHANGED">
    <@fdsAction.link linkText=diffedLinkText.currentValue linkClass="govuk-link govuk-link--button" linkUrl=springUrl(diffedLinkUrl.currentValue)/>
  </#if>

</#macro>

<#macro renderDiff diffedField noAutoEscapeFlag="false" multiLineTextBlockClass="">

  <#if diffedField.diffType == "ADDED">
    <@diffChanges>

      <@diffChangesInsert>
        <@diffValue noAutoEscapeFlagValue=noAutoEscapeFlag value=diffedField.currentValue tag=diffedField.currentValueTag multiLineTextBlockClass=multiLineTextBlockClass/>
      </@diffChangesInsert>

    </@diffChanges>

    <span class="diff-raw-value"><@diffValue noAutoEscapeFlagValue=noAutoEscapeFlag value=diffedField.currentValue tag=diffedField.currentValueTag multiLineTextBlockClass=multiLineTextBlockClass/></span>
  </#if>

  <#if diffedField.diffType == "UPDATED">
    <@diffChanges>

      <@diffChangesDelete>
        <@diffValue noAutoEscapeFlagValue=noAutoEscapeFlag value=diffedField.previousValue tag=diffedField.previousValueTag multiLineTextBlockClass=multiLineTextBlockClass/>
      </@diffChangesDelete>

      <@diffChangesInsert>
        <@diffValue noAutoEscapeFlagValue=noAutoEscapeFlag value=diffedField.currentValue tag=diffedField.currentValueTag multiLineTextBlockClass=multiLineTextBlockClass/>
      </@diffChangesInsert>

    </@diffChanges>

    <span class="diff-raw-value"><@diffValue noAutoEscapeFlagValue=noAutoEscapeFlag value=diffedField.currentValue tag=diffedField.currentValueTag multiLineTextBlockClass=multiLineTextBlockClass/></span>
  </#if>

  <#if diffedField.diffType == "DELETED">
    <@diffChanges>

      <@diffChangesDelete>
        <@diffValue noAutoEscapeFlagValue=noAutoEscapeFlag value=diffedField.previousValue tag=diffedField.previousValueTag multiLineTextBlockClass=multiLineTextBlockClass/>
      </@diffChangesDelete>

    </@diffChanges>
  </#if>

  <#if diffedField.diffType == "UNCHANGED">
    <@diffValue noAutoEscapeFlagValue=noAutoEscapeFlag value=diffedField.currentValue tag=diffedField.currentValueTag multiLineTextBlockClass=multiLineTextBlockClass/>
  </#if>

</#macro>

<#macro diffValue noAutoEscapeFlagValue="false" value="" tag="" multiLineTextBlockClass="">
  <#if noAutoEscapeFlagValue=="true">
    <span class="diff-changes__value">
      <#noautoesc>${value}</#noautoesc>
    </span>
    <#else>
      <span class="diff-changes__value">
        <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${value}</@multiLineText.multiLineText>
      </span>
      <#if tag.displayName?has_content>
        <strong class="govuk-tag">
          ${tag.displayName}
        </strong>
      </#if>
  </#if>
</#macro>

<#macro toggler showDiffOnLoad=false togglerLabel="Show difference from previous version">
  <div class="govuk-form-group">
    <div class="govuk-checkboxes">
      <div class="govuk-checkboxes__item">
        <input class="govuk-checkboxes__input" id="toggle-diff" type="checkbox" ${showDiffOnLoad?then('checked','')}>
        <label class="govuk-label govuk-checkboxes__label" for="toggle-diff">
          ${togglerLabel}
        </label>
      </div>
    </div>
  </div>

  <script>
    $(document).ready(function() {

      function toggleDiff(diffTogglerIsChecked) {
        if (diffTogglerIsChecked) {
          $('.hide-when-diff-disabled').show();
          $('.diff-raw-value').hide();
        }
        else {
          $('.hide-when-diff-disabled').hide();
          $('.diff-raw-value').show();
        }

      }

      $('#toggle-diff').click(function() {
        toggleDiff(this.checked);
        $('.diff-changes').toggleClass('diff-changes--flex');
      });

      toggleDiff($('#toggle-diff').prop("checked"));

    });
  </script>
</#macro>