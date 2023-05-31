package com.arria.ping.log

import com.arria.ping.log.data.QueryData
import org.json.JSONObject

fun mapQueryFilters(areaCodes: List<String>, stateCodes: List<String>, supervisors: List<String>,storeNumbers: List<String>,
                    queryName: String): JSONObject{
    val jsonObject = JSONObject()
    jsonObject.put("areaCode",areaCodes)
    jsonObject.put("stateCode",stateCodes)
    jsonObject.put("supervisor",supervisors)
    jsonObject.put("storeNumbers",storeNumbers)
    jsonObject.put("Query", queryName)
    return jsonObject
}


fun mapQueryFilters(queryData: QueryData): JSONObject{
    val jsonObject = JSONObject()
    jsonObject.put("areaCode",queryData.areaCodes)
    jsonObject.put("stateCode",queryData.stateCodes)
    jsonObject.put("supervisor",queryData.supervisors)
    jsonObject.put("storeNumbers",queryData.storeNumbers)
    jsonObject.put("periodRangeTo",queryData.rangeTo)
    jsonObject.put("periodRangeFrom",queryData.rangeFrom)
    jsonObject.put("filterType",queryData.filterType)
    jsonObject.put("Query", queryData.queryName)
    return jsonObject
}