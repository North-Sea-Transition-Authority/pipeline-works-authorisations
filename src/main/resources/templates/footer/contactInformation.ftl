<#include '../layout.ftl'>
<#import 'serviceContact.ftl' as serviceContact/>

<@defaultPage htmlTitle="Contact" pageHeading="Contact" fullWidthColumn=true topNavigation=false backLink=true phaseBanner=false>
  <#list contacts as contact>
    <@serviceContact.serviceContact serviceContact=contact includeHeader=true />
  </#list>
</@defaultPage>
