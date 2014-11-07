/*Knapsack

  This model finds the integer optimal packing of a knapsack

  Nigel_Galloway
  January 9th., 2012
*/

/* Constraints */
param MIN_SEC;
param MIN_PERF;
param MIN_STO;

/* Weights */
param WEIGHT_SEC;
param WEIGHT_PERF;
param WEIGHT_STO;
param WEIGHT_STOCOST;
param WEIGHT_BWCOST;
param WEIGHT_AVAIL;
param WEIGHT_DUR;

/*Items for Provider */
set Provider;
param PROV_SEC{t in Provider};
param PROV_PERF{t in Provider};
param PROV_AVAIL{t in Provider};
param PROV_DUR{t in Provider};
param PROV_STORAGECOST{t in Provider};
param PROV_BWCOST{t in Provider};

var PROV_take{t in Provider}, binary;

param PROV_TOTAL;
param PROV_REQ;


/*Items for IDA */
set Ida;
param IDA_SEC{t in Ida};
param IDA_PERF{t in Ida};
param IDA_STO{t in Ida};
var IDA_take{t in Ida}, binary;

/*Items for ENC */
set Enc;
param ENC_SEC{t in Enc};
param ENC_PERF{t in Enc};
param ENC_STO{t in Enc};
var ENC_take{t in Enc}, binary;

/*Items for COMP */
set Comp;
param COMP_SEC{t in Comp};
param COMP_PERF{t in Comp};
param COMP_STO{t in Comp};
var COMP_take{t in Comp}, binary;

/*Constraint Restrictions*/
min_sec:  sum{t in Ida} (IDA_take[t] * IDA_SEC[t]) +
		  sum{t in Enc} (ENC_take[t] * ENC_SEC[t]) +
          sum{t in Comp} (COMP_take[t] * COMP_SEC[t]) +
          sum{t in Provider} ((PROV_take[t] * PROV_SEC[t])/PROV_REQ) >= MIN_SEC;
          
min_perf: sum{t in Ida} (IDA_take[t] * IDA_PERF[t]) +
		  sum{t in Enc} (ENC_take[t] * ENC_PERF[t]) +
          sum{t in Comp} (COMP_take[t] * COMP_PERF[t]) +
          sum{t in Provider} ((PROV_take[t] * PROV_PERF[t])/PROV_REQ) >= MIN_PERF;
          
min_sto: sum{t in Ida} (IDA_take[t] * IDA_STO[t]) +
		  sum{t in Enc} (ENC_take[t] * ENC_STO[t]) +
          sum{t in Comp} (COMP_take[t] * COMP_STO[t]) >= MIN_STO;

/* Select only one of each categorie */
IDA_only : sum{t in Ida} IDA_take[t] == 1;
ENC_only : sum{t in Enc} ENC_take[t] <= 1;
COMP_only : sum{t in Comp} COMP_take[t] <= 1;

/*Select Provider*/
PROV_sel : sum{t in Provider} PROV_take[t] = PROV_REQ;

/* Check if weights add up to 1 */
check WEIGHT_SEC + WEIGHT_PERF + WEIGHT_STO + WEIGHT_STOCOST + WEIGHT_BWCOST + WEIGHT_AVAIL + WEIGHT_DUR = 1;

/* Objective */
maximize knap_value:
/*Security - MAXIMIZE*/
((sum{t in Ida} (IDA_take[t] * IDA_SEC[t]) - min{t in Ida} IDA_SEC[t]) / (1 + max{t in Ida} IDA_SEC[t] - min{t in Ida} IDA_SEC[t])) * WEIGHT_SEC +

((sum{t in Enc} (ENC_take[t] * ENC_SEC[t]) - min{t in Enc} ENC_SEC[t]) / (1 + max{t in Enc} ENC_SEC[t] - min{t in Enc} ENC_SEC[t])) * WEIGHT_SEC +

((sum{t in Comp} (COMP_take[t] * COMP_SEC[t]) - min{t in Comp} COMP_SEC[t] ) / (1 + max{t in Comp} COMP_SEC[t] - min{t in Comp} COMP_SEC[t])) * WEIGHT_SEC +

(((sum{t in Provider} (PROV_take[t] * PROV_SEC[t])/PROV_REQ) - min{t in Provider} PROV_SEC[t] ) / (1 + max{t in Provider} PROV_SEC[t] - min{t in Provider} PROV_SEC[t])) * WEIGHT_SEC +

/*Performance - MAXIMIZE*/
((sum{t in Ida} (IDA_take[t] * IDA_PERF[t]) - min{t in Ida} IDA_PERF[t]) / (1 + max{t in Ida} IDA_PERF[t] - min{t in Ida} IDA_PERF[t])) * WEIGHT_PERF +

((sum{t in Enc} (ENC_take[t] * ENC_PERF[t]) - min{t in Enc} ENC_PERF[t]) / (1 + max{t in Enc} ENC_PERF[t] - min{t in Enc} ENC_PERF[t])) * WEIGHT_PERF +

((sum{t in Comp} (COMP_take[t] * COMP_PERF[t]) - min{t in Comp} COMP_PERF[t] ) / (1 + max{t in Comp} COMP_PERF[t] - min{t in Comp} COMP_PERF[t])) * WEIGHT_PERF +

(((sum{t in Provider} (PROV_take[t] * PROV_PERF[t])/PROV_REQ) - min{t in Provider} PROV_PERF[t] ) / (1 + max{t in Provider} PROV_PERF[t] - min{t in Provider} PROV_PERF[t])) * WEIGHT_PERF +

/*Storage Overhead - MINIMIZE*/
((max{t in Ida} IDA_STO[t] - sum{t in Ida} (IDA_take[t] * IDA_STO[t])) / (1 + max{t in Ida} IDA_STO[t] - min{t in Ida} IDA_STO[t])) * WEIGHT_STO +

((max{t in Enc} ENC_STO[t] - sum{t in Enc} (ENC_take[t] * ENC_STO[t])) / (1 + max{t in Enc} ENC_STO[t] - min{t in Enc} ENC_STO[t])) * WEIGHT_STO +

((max{t in Comp} COMP_STO[t] - sum{t in Comp} (COMP_take[t] * COMP_STO[t])) / (1 + max{t in Comp} COMP_STO[t] - min{t in Comp} COMP_STO[t])) * WEIGHT_STO +

/*Provider Storage Cost - MINIMIZE*/
((max{t in Provider} PROV_STORAGECOST[t] - sum{t in Provider} ((PROV_take[t] * PROV_STORAGECOST[t]))/PROV_REQ) / (1 + max{t in Provider} PROV_STORAGECOST[t] - min{t in Provider} PROV_STORAGECOST[t])) * WEIGHT_STOCOST +

/*Provider Bandwidth Cost - MINIMIZE*/
((max{t in Provider} PROV_BWCOST[t] - sum{t in Provider} (PROV_take[t] * PROV_BWCOST[t])) / (1 + max{t in Provider} PROV_BWCOST[t] - min{t in Provider} PROV_BWCOST[t])) * WEIGHT_BWCOST +

/*Provider Availability - MAXIMIZE*/
((sum{t in Provider} (PROV_take[t] * PROV_AVAIL[t]) - min{t in Provider} PROV_AVAIL[t]) / (1 + max{t in Provider} PROV_AVAIL[t] - min{t in Provider} PROV_AVAIL[t])) * WEIGHT_AVAIL +

/*Provider DURability - MAXIMIZE*/
((sum{t in Provider} (PROV_take[t] * PROV_DUR[t]) - min{t in Provider} PROV_DUR[t]) / (1 + max{t in Provider} PROV_DUR[t] - min{t in Provider} PROV_DUR[t])) * WEIGHT_DUR
;

solve;

/*Print the results*/

printf{i in Provider: PROV_take[i] == 1} 'provider;%d\n',i;
printf{i in Ida: IDA_take[i] == 1} 'ida;%d\n',i;
printf{i in Enc: ENC_take[i] == 1} 'enc;%d\n',i;
printf{i in Enc: COMP_take[i] == 1} 'comp;%d\n',i;

end;
