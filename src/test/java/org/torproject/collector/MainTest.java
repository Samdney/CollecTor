/* Copyright 2016 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.collector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.torproject.collector.conf.ConfigurationException;
import org.torproject.collector.conf.Key;
import org.torproject.collector.cron.Scheduler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class MainTest {

  @Rule
  public TemporaryFolder tmpf = new TemporaryFolder();

  @Test(expected = ConfigurationException.class)
  public void testInitializationConfigException() throws Exception {
    File conf = new File(Main.CONF_FILE);
    checkCleanEnv(conf);
    Main.main(new String[] {"/tmp/"});
    assertTrue(conf.exists());
    assertTrue(conf.delete());
  }

  private void checkCleanEnv(File conf) {
    assertFalse("Please remove " + Main.CONF_FILE + " before running tests!",
        conf.exists());
  }

  @Test()
  public void testInitializationNullArgs() throws Exception {
    File conf = new File(Main.CONF_FILE);
    checkCleanEnv(conf);
    Main.main(null);
    assertTrue(conf.exists());
    assertTrue(conf.delete());
  }

  @Test(expected = RuntimeException.class)
  public void testInitializationUnwritable() throws Exception {
    File conf = tmpf.newFolder("folder");

    Main.main(new String[] {
        Paths.get(conf.toString(), "x", "y", "z").toString()});
  }

  @Test()
  public void testInitializationEmptyArgs() throws Exception {
    File conf = new File(Main.CONF_FILE);
    checkCleanEnv(conf);
    Main.main(new String[] { });
    assertTrue(conf.exists());
    assertTrue(conf.delete());
  }

  @Test()
  public void testInitializationTooManyArgs() throws Exception {
    File conf = new File(Main.CONF_FILE);
    checkCleanEnv(conf);
    Main.main(new String[] { "x", "y" });
    assertFalse(conf.exists());
  }

  @Test()
  public void testSmoke() throws Exception {
    File conf = tmpf.newFile("test.conf");
    assertEquals(0L, conf.length());
    Main.main(new String[]{conf.toString()});
    assertTrue(4_000L <= conf.length());
    changeFilePathsAndSetActivation(conf,
        Key.TorperfActivated.name());
    Main.main(new String[]{conf.toString()});
    waitSec(2);
  }

  public static void waitSec(int sec) {
    long now = System.currentTimeMillis();
    while (System.currentTimeMillis() - now < 1_000L * sec) {
      try {
        Thread.sleep(sec * 1_000L);
      } catch (Exception e) {/* ignored */}
    }
  }

  private void changeFilePathsAndSetActivation(File f, String a)
      throws Exception {
    List<String> lines = Files.readAllLines(f.toPath());
    BufferedWriter bw = Files.newBufferedWriter(f.toPath());
    File in = tmpf.newFolder();
    File out = tmpf.newFolder();
    String inStr = "in/";
    String outStr = "out/";
    for(String line : lines) {
      if (line.contains(inStr)) {
        line = line.replace(inStr, in.toString() + inStr);
      } else if (line.contains(outStr)) {
        line = line.replace(outStr, out.toString() + outStr);
      } else if (line.contains(a)) {
        line = line.replace("false", "true");
      }
      bw.write(line);
      bw.newLine();
    }
    bw.flush();
    bw.close();
  }

  /* Verifies the contents of the default collector.properties file.
   * All properties specified have to be present but nothing else. */
  @Test()
  public void testPropertiesFile() throws Exception {
    Properties props = new Properties();
    props.load(getClass().getClassLoader().getResourceAsStream(
        Main.CONF_FILE));
    for (Key key : Key.values()) {
      assertNotNull("Property '" + key.name() + "' not specified in "
          + Main.CONF_FILE + ".",
          props.getProperty(key.name()));
    }
    for (String propName : props.stringPropertyNames()) {
      try {
        Key.valueOf(propName);
      } catch (IllegalArgumentException ex) {
        fail("Invalid property name '" + propName + "' found in "
            + Main.CONF_FILE + ".");
      }
    }
  }

  /* Verifies that every collecTorMain class is configured in the
   * default collector.properties file and the other way around. */
  @Test()
  public void testRunConfiguration() throws Exception {
    Properties props = new Properties();
    props.load(getClass().getClassLoader().getResourceAsStream(
        Main.CONF_FILE));
    String[] runConfigSettings = new String[] {Scheduler.ACTIVATED,
        Scheduler.PERIODMIN, Scheduler.OFFSETMIN};
    for (Key key : Main.collecTorMains.keySet()) {
      for (String part : runConfigSettings) {
        String key2 = key.name().replace("Activated", part);
        assertNotNull("Property '" + key2 + "' not specified in "
            + Main.CONF_FILE + ".",
            props.getProperty(key2));
      }
    }
    for (String propName : props.stringPropertyNames()) {
      for (String part : runConfigSettings) {
        if (propName.contains(part)) {
          String key2 = propName.replace(part, "");
          assertTrue("CollecTorMain '" + key2
              + "' not specified in Main.class.",
              Main.collecTorMains.containsKey(Key.valueOf(key2 + "Activated")));
        }
      }
    }
  }

  @Test(expected = ConfigurationException.class)
  public void testNoModuleActivated() throws Exception {
    File conf = tmpf.newFile("test.conf");
    try {
      assertEquals(0L, conf.length());
      // create default configuration
      Main.main(new String[]{conf.toString()});
    } catch (ConfigurationException ce) {
      fail("This should not cause a ConfigurationException: " + ce);
    }
    // no module activated; the following should throw a ConfigurationException
    Main.main(new String[]{conf.toString()});
  }
}

