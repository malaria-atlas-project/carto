<#ftl strip_text=true strip_whitespace=true>
<@compress single_line=true>
<#if (nSurveys > 0)>The ${nSurveys} <i>${parasiteAbbr}</i> parasite rate surveys available for predicting prevalance within the stable limits were collected between ${yearStart?string("####")} and ${yearEnd?string("####")}.</#if>
<br><b>Citations:</b> Hay, S.I. <i>et al.</i> (2009). A world malaria map:  <i>Plasmodium falciparum</i> endemicity in 2007.  <i>PLoS Medicine</i> <b>6</b>(3): e1000048 and Hay, S.I.  <i>et al.</i> (2011). A revised world malaria map:  <i>Plasmodium falciparum</i> endemicity in 2010.  <i>PLoS Medicine</i>, to submit.
<br><#include "copyright.ftl">
</@compress>