update reportsummarydefn set report_internalreportname=match.internalreportname
from
(select brd.internalreportname, rsd.id
from basereportdefn brd inner join reportsummarydefn rsd on brd.reportsummary_id = rsd.id) as match
where match.id = reportsummarydefn.id;