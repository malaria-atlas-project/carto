<#ftl strip_text=true strip_whitespace=true>
<@compress single_line=true>
<#if (nSurveys > 0)>The ${nSurveys} <i>${parasiteAbbr}</i> parasite rate surveys available for predicting prevalance within the stable limits were collected between ${yearStart} and ${yearEnd}.</#if>
<b>Biological masks:</b> Biological masks were used to modify malaria risk defined by the 
medical intelligence layers. If temperature did not exceed the limit for successful 
sporogony of <i>${parasiteAbbr}</i> in the local dominant <i>Anopheles</i> vector species, 
risk was downgraded to malaria free (Gething <i>et al</i>. (2011). in prep). If the 
area was hyper-arid (as defined by the bare area definition of the GLOBCOVER product 
(http://ionia1.esrin.esa.int/)) risk was downgraded from stable to unstable or 
from unstable to malaria free.
<#if duffyRequired><br><b>Duffy:</b> The masked area shows the area of the country where the prevalence of the 
Duffy negativity blood group exceeds 90% (Howes <i>et al</i>. (2011). Nature Comm., submitted). 
This population is refractory to <i>Plasmodium vivax</i> infection but transmission is possible 
in the remaining minority.</#if>
<br><#include "copyright.ftl">
</@compress>