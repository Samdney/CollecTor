/* Copyright 2016 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.collector.persist;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class CleanUtils {
    /**
     * Basic clean-up method.
     * All files in the given directory and below are inspected and
     * erased, if they are older than the given cut-off-time.
     */
    public static void cleanDir(Path directory, final long cutOffTime) throws IOException{
        SimpleFileVisitor visitor = new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes att) throws IOException{
                if(att.lastModifiedTime().toMillis() < cutOffTime){
                    Files.delete(path);
                }
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(directory, visitor);
    }

    /**
     * Remove files with a certain ending.
     * All files in the given directory and below are inspected and
     * erased, if they are older than the given cut-off-time, and their name
     * ends with one of the given patterns.
     * @param endingStrings is case sensitive
     */
    public static void cleanDirPattern(Path directory, final long cutOffTime, final String ... endingStrings) throws IOException{
        SimpleFileVisitor visitor = new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes att) throws IOException{
                for(String s : endingStrings){
                    if(path.toString().endsWith(s) && att.lastModifiedTime().toMillis() < cutOffTime){
                        Files.delete(path);
                        break;
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(directory, visitor);
    }

    // Some renaming methods:
    /**
     * All files in the given directory and below are inspected and renamed
     * from <filename><ending> to <fileneame>, if their name ends with the given 'ending'.
     * @param ending is case sensitive
     */
    public static void renameFiles(Path directory, final String ending) throws IOException{
        SimpleFileVisitor visitor = new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes att) throws IOException{
                if(path.toString().endsWith(ending)){
                    String newPathName = path.toString().replace(ending, "");
                    try{
                        Files.move(path, Paths.get(newPathName), ATOMIC_MOVE, REPLACE_EXISTING);
                    }catch(AtomicMoveNotSupportedException ex){
                        Files.move(path, Paths.get(newPathName), REPLACE_EXISTING);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(directory, visitor);
    }

    /**
     * Implements renameFiles(). All files in the given directory and below are inspected and renamed
     * from <filename>.tmp to <fileneame>, if their name ends with '.tmp'.
     */
    public static void renameTmpFiles(Path directory) throws IOException{
        renameFiles(directory, ".tmp");
    }
}