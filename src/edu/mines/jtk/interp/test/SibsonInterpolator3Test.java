/****************************************************************************
Copyright (c) 2009, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package edu.mines.jtk.interp.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import static java.lang.Math.*;
import java.awt.Color;
import javax.swing.SwingUtilities;

import edu.mines.jtk.awt.*;
import edu.mines.jtk.dsp.*;
import edu.mines.jtk.interp.*;
import edu.mines.jtk.mosaic.*;
import edu.mines.jtk.util.*;

/**
 * Tests {@link edu.mines.jtk.mesh.SibsonInterpolator3}.
 * @author Dave Hale, Colorado School of Mines
 * @version 2009.06.14
 */
public class SibsonInterpolator3Test extends TestCase {
  public static void main(String[] args) {
    if (args.length>=1 && args[0].equals("bench")) {
      benchSine();
    } else {
      TestSuite suite = new TestSuite(SibsonInterpolator3Test.class);
      junit.textui.TestRunner.run(suite);
    }
  }

  public void testSimpleTet() {
    float[] f  = {-1.0f,-1.0f,-1.0f, 3.0f};
    float[] x1 = { 1.0f,-1.0f,-1.0f, 1.0f};
    float[] x2 = {-1.0f, 1.0f,-1.0f, 1.0f};
    float[] x3 = {-1.0f,-1.0f, 1.0f, 1.0f};
    SibsonInterpolator3.Method[] methods = {HL,BS};
    SibsonInterpolator3 si = new SibsonInterpolator3(methods[0],f,x1,x2,x3);
    assertValue(si, 0.0f, 0.0f, 0.0f, 0.0f);
    assertValue(si, 0.5f, 0.0f, 0.0f, 0.5f);
    assertValue(si, 0.0f, 0.5f, 0.0f, 0.5f);
    assertValue(si, 0.0f, 0.0f, 0.5f, 0.5f);
    assertValue(si, 0.5f, 0.5f, 0.5f, 1.5f);
  }

  public void testSimpleCube() {
    float[] f  = {-3.0f,-1.0f,-1.0f, 1.0f,-1.0f, 1.0f, 1.0f, 3.0f};
    float[] x1 = {-1.0f, 1.0f,-1.0f, 1.0f,-1.0f, 1.0f,-1.0f, 1.0f};
    float[] x2 = {-1.0f,-1.0f, 1.0f, 1.0f,-1.0f,-1.0f, 1.0f, 1.0f};
    float[] x3 = {-1.0f,-1.0f,-1.0f,-1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
    SibsonInterpolator3.Method[] methods = {HL,BS};
    SibsonInterpolator3 si = new SibsonInterpolator3(methods[0],f,x1,x2,x3);
    assertValue(si,-0.5f,-0.5f,-0.5f,-1.5f);
    assertValue(si, 0.5f,-0.5f,-0.5f,-0.5f);
    assertValue(si,-0.5f, 0.5f,-0.5f,-0.5f);
    assertValue(si, 0.5f, 0.5f,-0.5f, 0.5f);
    assertValue(si,-0.5f,-0.5f, 0.5f,-0.5f);
    assertValue(si, 0.5f,-0.5f, 0.5f, 0.5f);
    assertValue(si,-0.5f, 0.5f, 0.5f, 0.5f);
    assertValue(si, 0.5f, 0.5f, 0.5f, 1.5f);
  }

  public void testLinear() {
    TestFunction tf = TestFunction.makeLinear();
    float[][] fx = tf.sampleUniform3(NS,XMIN,XMAX,XMIN,XMAX,XMIN,XMAX);
    float[] f = fx[0], x1 = fx[1], x2 = fx[2], x3 = fx[3];
    SibsonInterpolator3.Method[] methods = {HL};
    for (SibsonInterpolator3.Method m:methods) {
      SibsonInterpolator3 si = new SibsonInterpolator3(m,f,x1,x2,x3);
      int n1 = NX, n2 = NX, n3 = NX;
      Sampling s1 = SX, s2 = SX, s3 = SX;
      float[][][] g = si.interpolate(s1,s2,s3);
      for (int i3=0; i3<n3; ++i3) {
        float x3i = (float)s3.getValue(i3);
        for (int i2=0; i2<n2; ++i2) {
          float x2i = (float)s2.getValue(i2);
          for (int i1=0; i1<n1; ++i1) {
            float x1i = (float)s1.getValue(i1);
            float fe = x1i+x2i+x3i;
            assertEquals(fe,g[i3][i2][i1]);
          }
        }
      }
    }
  }

  public static void benchSine() {
    TestFunction tf = TestFunction.makeSine();
    testScattered(tf);
    testUniform(tf);
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  // Bounds on sample coordinates x1 and x2.
  private static final float XMIN = 0.0f;
  private static final float XMAX = 1.0f;

  // Number of scattered samples to be interpolated. The cube of some 
  // integer is a good choice for both uniform and random sampling.
  //private static final int NS = 27;
  private static final int NS = 125;
  //private static final int NS = 1000;

  // Uniform sampling used in interpolation.
  //private static final int NX = 41; // some samples on the convex hull
  //private static final double DX = (XMAX-XMIN)/(NX-1);
  //private static final double FX = XMIN;
  private static final int NX = 39; // all samples inside the convex hull
  private static final double DX = (XMAX-XMIN)/(NX+1);
  private static final double FX = XMIN+DX;
  private static final Sampling SX = new Sampling(NX,DX,FX);

  // Implementation methods.
  private static SibsonInterpolator3.Method HL = 
    SibsonInterpolator3.Method.HALE_LIANG;
  private static SibsonInterpolator3.Method BS = 
    SibsonInterpolator3.Method.BRAUN_SAMBRIDGE;
  private static SibsonInterpolator3.Method WS = 
    SibsonInterpolator3.Method.WATSON_SAMBRIDGE;

  private static final double TOLERANCE = 1.0e-5;
  private void assertEquals(float e, float a) {
    assertEquals(e,a,TOLERANCE);
  }
  private void assertValue(
    SibsonInterpolator3 si, float x1, float x2, float x3, float f) 
  {
    float g = si.interpolate(x1,x2,x3);
    assertEquals(f,g);
  }

  private static void testScattered(TestFunction tf) {
    float[][] fx = tf.sampleScattered3(NS,XMIN,XMAX,XMIN,XMAX,XMIN,XMAX);
    testMethods(tf,fx);
  }

  private static void testUniform(TestFunction tf) {
    float[][] fx = tf.sampleUniform3(NS,XMIN,XMAX,XMIN,XMAX,XMIN,XMAX);
    testMethods(tf,fx);
  }

  private static void testMethods(TestFunction tf, float[][] fx) {
    float[] f = fx[0], x1 = fx[1], x2 = fx[2], x3 = fx[3];
    System.out.println();
    SibsonInterpolator3.Method[] methods = {HL,BS};
    for (int i=0; i<methods.length; ++i) {
      SibsonInterpolator3.Method method = methods[i];
      SibsonInterpolator3 si = new SibsonInterpolator3(method,f,x1,x2,x3);
      si.setNullValue(1.0f);
      double tmin = Double.MAX_VALUE;
      float[][][] g = null;
      for (int iter=0; iter<1; ++iter) {
        Stopwatch sw = new Stopwatch();
        sw.start();
        g = si.interpolate(SX,SX,SX);
        sw.stop();
        tmin = min(tmin,sw.time());
      }
      System.out.println("method="+method+" time="+tmin);
      System.out.println("min="+Array.min(g)+" max="+Array.max(g));
      plot(method.toString(),g);
    }
  }
  private static void dump(float[] f, float[] x1, float[] x2, float[] x3) {
    for (int i=0; i<f.length; ++i)
      System.out.println(
        "i="+i+" f="+f[i]+" x1="+x1[i]+" x2="+x2[i]+" x3="+x3[i]);
  }
  private static void dump(float[][][] g) {
    int n1 = g[0][0].length;
    int n2 = g[0].length;
    int n3 = g.length;
    for (int i3=0; i3<n3; ++i3)
      for (int i2=0; i2<n2; ++i2)
        for (int i1=0; i1<n1; ++i1)
          System.out.println("i1="+i1+" i2="+i2+" i3="+i3+" g="+g[i3][i2][i1]);
  }
 
  private static void plot(
    final String method, final float[][][] g)
  {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        PlotPanelPixels3 plot = new PlotPanelPixels3(
          PlotPanelPixels3.Orientation.X1RIGHT,
          PlotPanelPixels3.AxesPlacement.LEFT_BOTTOM,
          SX,SX,SX,g);
        plot.setTitle(method);
        plot.setLabel1("x");
        plot.setLabel2("y");
        plot.setLabel3("z");
        plot.addColorBar();
        plot.setLineColor(null);
        plot.setColorModel(ColorMap.JET);
        plot.setLineColor(Color.BLACK);
        plot.setInterpolation(PixelsView.Interpolation.NEAREST);
        PlotFrame frame = new PlotFrame(plot);
        frame.setSize(800,760);
        frame.setDefaultCloseOperation(PlotFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
      }
    });
  }
}