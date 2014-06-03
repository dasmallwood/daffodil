package edu.illinois.ncsa.daffodil.io

import java.nio.ByteBuffer
import junit.framework.Assert._
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

class TestFastAsciiConvert {

  val cvt = FastAsciiToUnicodeConverter

  @Test def testConvertByte1() {
    assertEquals(cvt.UnicodeReplacementCharacter, cvt.convertByte(-1.toByte))
    assertEquals(0.toChar, cvt.convertByte(0.toByte))
    assertEquals(127.toChar, cvt.convertByte(127.toByte))
    assertEquals(cvt.UnicodeReplacementCharacter, cvt.convertByte(128.toByte))
  }

  @Test def testConvertInt1() {
    assertEquals(cvt.UnicodeReplacementCharacter, cvt.convertInt(-1.toByte))
    assertEquals(0.toChar, cvt.convertInt(0.toByte))
    assertEquals(127.toChar, cvt.convertInt(127.toByte))
    assertEquals(cvt.UnicodeReplacementCharacter, cvt.convertInt(128.toByte))
  }

  @Test def testConvertLong1() {
    assertEquals(0xFFFDFFFDFFFDFFFDL, cvt.convertLong(-1))
    assertEquals(0x0L, cvt.convertLong(0))
    assertEquals(0xFFFDL, cvt.convertLong(128))
    assertEquals(0xFFFD0000L, cvt.convertLong(0x00008000))
    assertEquals(0xFFFD00000000L, cvt.convertLong(0x00800000))
    assertEquals(0xFFFD000000000000L, cvt.convertLong(0x80000000))
    assertEquals(0x002c002c002c002cL, cvt.convertLong(0x2c2c2c2c))
  }

  @Test def testConvert1() {
    val data = "abcdefg".toList.map { _.toByte }.toArray
    val bb = ByteBuffer.wrap(data)
    val cb = cvt.convert(bb)
    val str = cb.toString()
    assertEquals("abcdefg", str)
  }

  @Test def testConvert2() {
    val data = "12345678abcdefg".toList.map { _.toByte }.toArray
    val bb = ByteBuffer.wrap(data)
    val cb = cvt.convert(bb)
    val str = cb.toString()
    assertEquals("12345678abcdefg", str)
  }

  @Test def testConvert2a() {
    val data = "12345678a".toList.map { _.toByte }.toArray
    val bb = ByteBuffer.wrap(data)
    val cb = cvt.convert(bb)
    val str = cb.toString()
    assertEquals("12345678a", str)
  }

  @Test def testConvert3() {
    val data = "\u00802345\u007F78abcdefg".toList.map { _.toByte }.toArray
    val bb = ByteBuffer.wrap(data)
    val cb = cvt.convert(bb)
    val str = cb.toString()
    assertEquals("\uFFFD2345\u007F78abcdefg", str)
  }

  @Test def testConvert4() {
    val data = "\u00802345\u007F78\u00802345\u007F78abcdefg".toList.map { _.toByte }.toArray
    val bb = ByteBuffer.wrap(data)
    val cb = cvt.convert(bb)
    val str = cb.toString()
    assertEquals("\uFFFD2345\u007F78\uFFFD2345\u007F78abcdefg", str)
  }

}