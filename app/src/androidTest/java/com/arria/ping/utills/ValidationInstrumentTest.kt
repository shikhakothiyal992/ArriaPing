package com.arria.ping.utills

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.arria.ping.util.EncryptionAndDecryptionUtil
import com.arria.ping.util.Validation
import com.chibatching.kotpref.Kotpref
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ValidationInstrumentTest {
    private lateinit var validation : Validation
    private lateinit var instrumentationContext: Context

    @Before
    fun init(){
        instrumentationContext = InstrumentationRegistry.getInstrumentation().targetContext
        validation = Validation()
        Kotpref.init(instrumentationContext)
    }

    // Test cases for checkAmountPercentageValue() by passing the Amount (with -ive amount), Value and Percentage values.
    @Test
    fun checkAmountValuePercentage_ValueAndPercentageNull_FormattedAmount(){
        val amount = validation.checkAmountPercentageValue(instrumentationContext,4179.0,null,null)
        Assert.assertEquals("$4,179", amount)
    }

    @Test
    fun checkAmountValuePercentage_ValueAndPercentageNull_FormattedTwoDigitsAmount(){
        val amount = validation.checkAmountPercentageValue(instrumentationContext,97.0,null,null)
        Assert.assertEquals("$97", amount)
    }

    @Test
    fun checkAmountValuePercentage_ValueAndPercentageNull_FormattedNegativeAmount(){
        val amount = validation.checkAmountPercentageValue(instrumentationContext,-4179.0,null,null)
        Assert.assertEquals("-$4,179", amount)
    }

    @Test
    fun checkAmountValuePercentage_AmountAndPercentageNull_FormattedValue(){
        val value = validation.checkAmountPercentageValue(instrumentationContext,null,null,4179.0)
        Assert.assertEquals("4,179", value)
    }

    @Test
    fun checkAmountValuePercentage_AmountAndPercentageNull_FormattedTwoDigitsValue(){
        val value = validation.checkAmountPercentageValue(instrumentationContext,null,null,97.0)
        Assert.assertEquals("97", value)
    }


    @Test
    fun checkAmountValuePercentage_AmountAndValueNull_FormattedPercentage(){
        val percentage = validation.checkAmountPercentageValue(instrumentationContext,null,97.8,null)
        Assert.assertEquals("97.8%", percentage)
    }

    @Test
    fun checkAmountValuePercentage_AmountAndValueNull_FormattedDecimalZeroPercentage(){
        val percentage = validation.checkAmountPercentageValue(instrumentationContext,null,97.0,null)
        Assert.assertEquals("97%", percentage)
    }

    @Test
    fun checkAmountPercentageValue_AllNullValues(){
        val allValues = validation.checkAmountPercentageValue(instrumentationContext,null,null,null)
        Assert.assertEquals("", allValues)
    }

    @Test
    fun encrypt_Encrypt_Data_In_ByteToString_Expected_Original_Data() {
        val encryptedResult = EncryptionAndDecryptionUtil.encrypt("Test")
        val decryptedResult = encryptedResult?.let {EncryptionAndDecryptionUtil.decrypt(it)}
        Assert.assertEquals("Test", decryptedResult)
    }

}