<#include '../../layout.ftl'>
<#import 'consentSearchTopLevelView.ftl' as consentSearchTopLevelView>

<#-- @ftlvariable name="resultView" type="uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView" -->
<#-- @ftlvariable name="consentSearchUrlFactory" type="uk.co.ogauthority.pwa.controller.search.consents.ConsentSearchUrlFactory" -->


<#macro view resultView consentSearchUrlFactory>

  <div class="filter-result">
    <h3 class="filter-result__heading">
        <@fdsAction.link
          linkText=resultView.pwaReference
          linkUrl=springUrl(consentSearchUrlFactory.getPwaViewRoute(resultView.pwaId)) 
          linkClass="govuk-link govuk-!-font-size-24 govuk-link--no-visited-state" />
    </h3>

    <@consentSearchTopLevelView.topLevelData resultView/>

  </div>

</#macro>