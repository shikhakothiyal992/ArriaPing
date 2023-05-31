package com.arria.ping.util

import com.google.common.truth.Truth.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ValidationTest {
    private lateinit var validation : Validation
    private val strValidEmail = "test@gmail.com"
    private val strInvalidEmail = "testgmail.com"


    @Before
    fun init(){
        validation = Validation()
    }

    @Test
    fun checkIsValidEmail() {
        val result = validation.isEmailValid(strValidEmail)
        assertThat(result).isTrue()
    }

    @Test
    fun checkIsInvalidEmail() {
        val result = validation.isEmailValid(strInvalidEmail)
        assertThat(result).isFalse()
    }

    @Test
    fun dollarFormatting_For_Thousands_Value_Input_Double_value_Expected_String_In_US_Format(){
        val result = validation.dollarFormatting(1000.00)
        Assert.assertEquals(result, "1,000")
    }

    @Test
    fun dollarFormatting_For_Thousands_With_Fractional_Value_Input_Double_value_Expected_String_In_US_Format(){
        val result = validation.dollarFormatting(1000.04)
        Assert.assertEquals(result, "1,000.04")
    }


    @Test
    fun dollarFormatting_For_TenThousands_Value_Input_Double_value_Expected_String_In_US_Format(){
        val result = validation.dollarFormatting(10000.00)
        Assert.assertEquals(result, "10,000")
    }


    @Test
    fun dollarFormatting_For_OneLac_Value_Input_Double_value_Expected_String_In_US_Format(){
        val result = validation.dollarFormatting(100000.00)
        Assert.assertEquals(result, "100,000")
    }

    @Test
    fun dollarFormatting_For_OneMillion_Value_Input_Double_value_Expected_String_In_US_Format(){
        val result = validation.dollarFormatting(1000000.00)
        Assert.assertEquals(result, "1,000,000")
    }

    @Test
    fun getNumber_For_TenMillion_Value_Input_Double_value_Expected_String_In_US_Format(){
        val result = validation.dollarFormatting(10000000.00)
        Assert.assertEquals(result, "10,000,000")
    }

    @Test
    fun getNumber_For_HundredMillion_Value_Input_Double_value_Expected_String_In_US_Format(){
        val result = validation.dollarFormatting(100000000.00)
        Assert.assertEquals(result, "100,000,000")
    }

}