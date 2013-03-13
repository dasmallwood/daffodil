package edu.illinois.ncsa.daffodil.profiling

/* Copyright (c) 2013 Tresys Technology, LLC. All rights reserved.
 *
 * Developed by: Tresys Technology, LLC
 *               http://www.tresys.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal with
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimers.
 * 
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimers in the
 *     documentation and/or other materials provided with the distribution.
 * 
 *  3. Neither the names of Tresys Technology, nor the names of its contributors
 *     may be used to endorse or promote products derived from this Software
 *     without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE
 * SOFTWARE.
 */

import junit.framework.Assert._
import org.scalatest.junit.JUnitSuite
import org.junit.Test
import scala.xml._
import edu.illinois.ncsa.daffodil.xml.XMLUtils
import edu.illinois.ncsa.daffodil.xml.XMLUtils._
import edu.illinois.ncsa.daffodil.compiler.Compiler
import edu.illinois.ncsa.daffodil.util._
import edu.illinois.ncsa.daffodil.tdml.DFDLTestSuite
import java.io.File
import edu.illinois.ncsa.daffodil.debugger.Debugger

import org.junit.contrib.java.lang.system.ExpectedSystemExit
import org.junit.contrib.java.lang.system.internal.CheckExitCalled
import org.junit.Rule
import edu.illinois.ncsa.daffodil.util.Misc
import java.io.ByteArrayInputStream
import edu.illinois.ncsa.daffodil.Main

class TestProfiling extends JUnitSuite {
  val testDir = "/edu/illinois/ncsa/daffodil/profiling/"
  val aa = testDir + "Profiling.tdml"
  lazy val runner = new DFDLTestSuite(Misc.getRequiredResource(aa))

  @Test def testTakCalibration { Tak.testTak }

  var bytesProcessed: Long = 0
  var charsProcessed: Long = 0

  /**
   * Runs for about a minute.
   */
  @Test def testLongRunningForProfiling {
    Tak.calibrate
    val ns = Tak.time {
      val (by, ch) = runner.runOneTestWithDataVolumes("AB007")
      bytesProcessed = by
      charsProcessed = ch
    }
    val takeonsThisRun = ns / Tak.takeons
    val bpns = ((bytesProcessed * 1.0) / ns)
    val kbps = bpns * 1000000
    val callsPerByte = 1 / (Tak.takeons * bpns)
    println("\nKB/sec = " + kbps)
    println("tak call equivalents per byte (takeons/byte) =  " + callsPerByte)
  }

}

/**
 * Tak is about establishing a sort-of platform independent self-calibrating
 * unit of comparison, which is how fast the JVM implements a 3 argument
 * function call.
 *
 * A unit, called a takeon, or scala takeon because these are language
 * specific, is the number of nanoseconds per 3-argument java function call.
 *
 * The function tak(a, b, c), does not build a deep stack,
 * uses no heap allocation, and never creates a large number.
 * It can be setup to run for billions of calls using only 3 small integer values.
 * All the calls are 3-arguments, passing Scala Int type.
 */
object Tak {

  def calibrate = {
    if (takeons == 0.0) {
      testTak
    }
  }

  var takeons = 0.0 // value on Mike Beckerle's laptop

  var callCount: Long = 0

  // Original Tak function
  def tak(x: Int, y: Int, z: Int): Int = {
    callCount += 1
    if (y < x)
      tak(
        tak(x - 1, y, z),
        tak(y - 1, z, x),
        tak(z - 1, x, y))
    else
      z
  }

  def time[R](block: => R): Long = {
    val t0 = System.nanoTime()
    val result = block // call-by-name
    val t1 = System.nanoTime()
    val nanos = t1 - t0
    println("Elapsed time: " + (t1 - t0) + "ns")
    nanos
  }

  def testTak {
    println("Calibrating takeon units")
    callCount = 0
    val nanos = time { tak(21, 3, 21) }
    println("tak call count = " + callCount + " in " + nanos + "ns")
    takeons = (1.0 * nanos) / callCount
    println("Under current load, 1 CPU of this system executes " + takeons + " nanoseconds per tak call.")
    println("So on this system, currently, 1 takeon = " + takeons + "ns")
    println("Done calibrating")
  }

}