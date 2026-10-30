package com.waypoint.app

import org.junit.Assert.*
import org.junit.Test

class JsonSafetyTest {
    @Test fun acceptsBoundaryAndRejectsExcessiveDepth() {
        JsonSafety.checkDepth("[".repeat(64)+"0"+"]".repeat(64))
        assertTrue(runCatching { JsonSafety.checkDepth("[".repeat(65)+"0"+"]".repeat(65)) }.isFailure)
    }
    @Test fun ignoresQuotedBracketsAndEscapedQuotes() {
        JsonSafety.checkDepth("{\"name\":\""+"[".repeat(200)+"\\\"still quoted\"}")
    }
    @Test fun rejectsLenientSyntaxAndUnbalancedDocuments() {
        for (value in listOf("{'name':'value'}", "{name:1}", "/* comment */{}", "[1,]", "{}{}", "[}", "{\"a\":01}", "{\"a\":NaN}")) {
            assertTrue(value,runCatching { JsonSafety.checkDepth(value) }.isFailure)
        }
    }
    @Test fun supportsStandardLiteralsNumbersAndEscapes() {
        JsonSafety.checkDepth("{\"value\":[true,false,null,-1.2e+3,\"\\u0041\\n\\\\\"]}")
    }
    @Test fun rejectsShallowWideIgnoredFieldsBeforeTreeAllocation() {
        val text="{\"ignored\":["+List(30) { "[]" }.joinToString(",")+"]}"
        assertTrue(runCatching { JsonSafety.checkDepth(text,maxValues=20) }.isFailure)
        JsonSafety.checkDepth(text,maxValues=32)
    }
}
