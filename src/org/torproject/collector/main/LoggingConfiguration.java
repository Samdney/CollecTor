/* Copyright 2010--2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.collector.main;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Initialize logging configuration.
 *
 * Log levels used by ERNIE:
 *
 * - SEVERE: An event made it impossible to continue program execution.
 * - WARNING: A potential problem occurred that requires the operator to
 *   look after the otherwise unattended setup
 * - INFO: Messages on INFO level are meant to help the operator in making
 *   sure that operation works as expected.
 * - FINE: Debug messages that are used to identify problems and which are
 *   turned on by default.
 * - FINER: More detailed debug messages to investigate problems in more
 *   detail. Not turned on by default. Increase log file limit when using
 *   FINER.
 * - FINEST: Most detailed debug messages. Not used.
 */
public class LoggingConfiguration {
  public LoggingConfiguration(String moduleName) {

    /* Remove default console handler. */
    for (Handler h : Logger.getLogger("").getHandlers()) {
      Logger.getLogger("").removeHandler(h);
    }

    /* Disable logging of internal Sun classes. */
    Logger.getLogger("sun").setLevel(Level.OFF);

    /* Set minimum log level we care about from INFO to FINER. */
    Logger.getLogger("").setLevel(Level.FINER);

    /* Create log handler that writes messages on WARNING or higher to the
     * console. */
    final SimpleDateFormat dateTimeFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    Formatter cf = new Formatter() {
      public String format(LogRecord record) {
        return dateTimeFormat.format(new Date(record.getMillis())) + " "
            + record.getMessage() + "\n";
      }
    };
    Handler ch = new ConsoleHandler();
    ch.setFormatter(cf);
    ch.setLevel(Level.WARNING);
    Logger.getLogger("").addHandler(ch);

    /* Initialize own logger for this class. */
    Logger logger = Logger.getLogger(
        LoggingConfiguration.class.getName());

    /* Create log handler that writes all messages on FINE or higher to a
     * local file. */
    Formatter ff = new Formatter() {
      public String format(LogRecord record) {
        return dateTimeFormat.format(new Date(record.getMillis())) + " "
            + record.getLevel() + " " + record.getSourceClassName() + " "
            + record.getSourceMethodName() + " " + record.getMessage()
            + (record.getThrown() != null ? " " + record.getThrown() : "")
            + "\n";
      }
    };
    try {
      new File("log").mkdirs();
      FileHandler fh = new FileHandler("log/" + moduleName, 5000000, 5,
          true);
      fh.setFormatter(ff);
      fh.setLevel(Level.FINE);
      Logger.getLogger("").addHandler(fh);
    } catch (SecurityException e) {
      logger.log(Level.WARNING, "No permission to create log file. "
          + "Logging to file is disabled.", e);
    } catch (IOException e) {
      logger.log(Level.WARNING, "Could not write to log file. Logging to "
          + "file is disabled.", e);
    }
  }
}
