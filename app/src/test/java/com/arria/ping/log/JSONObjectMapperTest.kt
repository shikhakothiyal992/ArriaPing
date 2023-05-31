package com.arria.ping.log

import com.arria.ping.log.data.QueryData
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert


class JSONObjectMapperKtTest {

    @Test
    fun mapQueryFilters_With_AreaCode_StateCode_StoreNumber_Query_JSONFormatted() {
        val expected = "{\"areaCode\":[\"16025\"],\"Query\":\"Yesterday KPI\",\"supervisor\": [\"Test1\"],\"storeNumbers\": " +
                "[\"1234\"],\"stateCode\": [\"IND\"]}"

        val areaCode = mutableListOf<String>()
        areaCode.add("16025")
        val stateCode = mutableListOf<String>()
        stateCode.add("IND")
        val supervisor = mutableListOf<String>()
        supervisor.add("Test1")
        val storeNumber = mutableListOf<String>()
        storeNumber.add("1234")
        val actual = mapQueryFilters(areaCode,stateCode,supervisor,storeNumber,"Yesterday KPI")
        JSONAssert.assertEquals(expected,actual.toString(),true)
    }

    @Test
    fun mapQueryFilters_With_AreaCode_StateCode_StoreNumber_FilterType_Query_JSONFormatted() {
        val expected =
            "{\"areaCode\":[\"16024\",\"16025\"],\"Query\":\"Yesterday KPI\",\"supervisor\": [\"Test1\",\"Test2\"]," +
                    "\"storeNumbers\": [\"1234\",\"5678\"],\"stateCode\": [\"IND\",\"USA\"],\"periodRangeTo\": \"\",\"periodRangeFrom\": \"\",\"filterType\": \"LAST_YEAR\"}"
        val areaCode = mutableListOf<String>()
        areaCode.add("16024")
        areaCode.add("16025")
        val stateCode = mutableListOf<String>()
        stateCode.add("IND")
        stateCode.add("USA")
        val supervisor = mutableListOf<String>()
        supervisor.add("Test1")
        supervisor.add("Test2")
        val storeNumber = mutableListOf<String>()
        storeNumber.add("1234")
        storeNumber.add("5678")

        val actual = mapQueryFilters(
            QueryData(areaCode, stateCode, supervisor,storeNumber, "", "", "LAST_YEAR", "Yesterday KPI")
        )
        JSONAssert.assertEquals(expected,actual.toString(),true)
    }

    @Test
    fun mapQueryFilters_With_AreaCode_StateCode_StoreNumber_RangeTo_RangeFrom_FilterType_Query_JSONFormatted() {
        val expected =
            "{\"areaCode\":[\"16024\",\"16025\"],\"Query\":\"Yesterday KPI\",\"supervisor\": [\"Test1\",\"Test2\"]," +
                    "\"storeNumbers\": [\"1234\",\"5678\"],\"stateCode\": [\"IND\",\"USA\"],\"periodRangeTo\": \"2022-05-15\",\"periodRangeFrom\": \"2022-05-01\",\"filterType\": \"LAST_YEAR\"}"

        val areaCode = mutableListOf<String>()
        areaCode.add("16024")
        areaCode.add("16025")
        val stateCode = mutableListOf<String>()
        stateCode.add("IND")
        stateCode.add("USA")
        val supervisor = mutableListOf<String>()
        supervisor.add("Test1")
        supervisor.add("Test2")
        val storeNumber = mutableListOf<String>()
        storeNumber.add("1234")
        storeNumber.add("5678")

        val actual = mapQueryFilters(
            QueryData(areaCode, stateCode, supervisor,storeNumber, "2022-05-15", "2022-05-01", "LAST_YEAR", "Yesterday KPI")
        )

        JSONAssert.assertEquals(expected,actual.toString(),true)
    }

    @Test
    fun mapQueryFilters_With_AreaCode_StateCode_StoreNumber_RangeTo_RangeFrom_Query_JSONFormatted() {
        val expected =
            "{\"areaCode\":[\"16024\",\"16025\"],\"Query\":\"Yesterday KPI\",\"supervisor\": [\"Test1\",\"Test2\"]," +
                    "\"storeNumbers\": [\"1234\",\"5678\"],\"stateCode\": [\"IND\",\"USA\"],\"periodRangeTo\": \"2022-05-15\",\"periodRangeFrom\": \"2022-05-01\",\"filterType\": \"\"}"

        val areaCode = mutableListOf<String>()
        areaCode.add("16024")
        areaCode.add("16025")
        val stateCode = mutableListOf<String>()
        stateCode.add("IND")
        stateCode.add("USA")
        val supervisor = mutableListOf<String>()
        supervisor.add("Test1")
        supervisor.add("Test2")
        val storeNumber = mutableListOf<String>()
        storeNumber.add("1234")
        storeNumber.add("5678")

        val actual = mapQueryFilters(
            QueryData(areaCode, stateCode, supervisor,storeNumber, "2022-05-15", "2022-05-01", "", "Yesterday KPI")
        )

        JSONAssert.assertEquals(expected,actual.toString(),true)
    }

    @Test
    fun mapQueryFilters_With_Query_JSONFormatted() {
        val expected =
            "{\"areaCode\":[\"\"],\"Query\":\"Yesterday KPI\",\"supervisor\": [\"\"],\"storeNumbers\": [\"\"]," +
                    "\"stateCode\": " +
                    "[\"\"],\"periodRangeTo\": \"\",\"periodRangeFrom\": \"\",\"filterType\": \"\"}"

        val areaCode = mutableListOf<String>()
        areaCode.add("")
        val stateCode = mutableListOf<String>()
        stateCode.add("")
        val supervisor = mutableListOf<String>()
        supervisor.add("")
        val storeNumber = mutableListOf<String>()
        storeNumber.add("")

        val actual = mapQueryFilters(
            QueryData(areaCode, storeNumber,supervisor, stateCode, "", "", "", "Yesterday KPI")
        )

        JSONAssert.assertEquals(expected,actual.toString(),true)
    }
}