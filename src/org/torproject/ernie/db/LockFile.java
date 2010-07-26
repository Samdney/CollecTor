package org.torproject.ernie.db;

import java.io.*;
import java.util.logging.*;

public class LockFile {

  private File lockFile;
  private Logger logger;

  public LockFile() {
    this.lockFile = new File("lock");
    this.logger = Logger.getLogger(LockFile.class.getName());
  }

  public boolean acquireLock() {
    this.logger.fine("Trying to acquire lock...");
    try {
      if (this.lockFile.exists()) {
        BufferedReader br = new BufferedReader(new FileReader("lock"));
        long runStarted = Long.parseLong(br.readLine());
        br.close();
        if (System.currentTimeMillis() - runStarted < 55L * 60L * 1000L) {
          return false;
        }
      }
      BufferedWriter bw = new BufferedWriter(new FileWriter("lock"));
      bw.append("" + System.currentTimeMillis() + "\n");
      bw.close();
      this.logger.fine("Acquired lock.");
      return true;
    } catch (IOException e) {
      this.logger.warning("Caught exception while trying to acquire "
          + "lock!");
      return false;
    }
  }

  public void releaseLock() {
    this.logger.fine("Releasing lock...");
    this.lockFile.delete();
    this.logger.fine("Released lock.");
  }
}

