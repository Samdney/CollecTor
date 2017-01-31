/* Copyright 2016 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.collector.persist;

import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

public class CleanUtilsTest{
    private File subtempfol;
    private HashMap<String, File> testFiles = new HashMap<>();

    private final String suffixTest = ".zip";
    private final long setCutOffTime = 10000;
    private final long cutOffTime = 0;
    private final long testCutOffTime = 1000000;

    @Rule
    public TemporaryFolder tempfol = new TemporaryFolder();

    /**
     * Initialize files for testing
     */
    @Before
    public void setUp() throws IOException{
        subtempfol = tempfol.newFolder("subfolder");
        subtempfol.mkdir();

        testFiles.put("Iamsocreative.png", tempfol.newFile("Iamsocreative.png"));
        testFiles.put("waffles.zip", tempfol.newFile("waffles.zip"));
        testFiles.put("bloo.txt", tempfol.newFile("bloo.txt"));
        testFiles.put("blah.tmp", tempfol.newFile("blah.tmp"));
        //newFile() method also creates file, hence it can't be used below
        testFiles.put("non-existent-file", new File(tempfol.getRoot(), "non-existent-file"));

        testFiles.put("Iamsocreative2.png", new File(subtempfol,"Iamsocreative2.png"));
        testFiles.put("waffles2.zip", new File(subtempfol,"waffles2.zip"));
        testFiles.put("bloo2.txt", new File(subtempfol,"bloo2.txt"));
        testFiles.put("blah2.tmp", new File(subtempfol,"blah2.tmp"));
        testFiles.put("non-existent-subfile", new File(subtempfol,"non-existent-subfile"));

        for(Map.Entry<String, File> val : testFiles.entrySet()){
            if(!val.getValue().exists() && !val.getKey().contains("existent")){
                val.getValue().createNewFile();
            }
            if(val.getKey().contains("Iamsocreative")){
                val.getValue().setLastModified(testCutOffTime);
            }else{
                val.getValue().setLastModified(cutOffTime);
            }
        }
    }

    /**
     * Tests that all files and sub-files, which are older than a specified cutOffTime,
     * are deleted in the directory.
     */
    @Test
    public void cleanDirTest() throws IOException{
        for(Map.Entry<String, File> val : testFiles.entrySet()){
            if(val.getKey().contains("existent")){
                assumeFalse(val.getValue().exists());
            } else {
                assumeTrue(val.getValue().exists());
            }
        }

        CleanUtils.cleanDir(tempfol.getRoot().toPath(), setCutOffTime);

        for(Map.Entry<String, File> val : testFiles.entrySet()){
            if(val.getKey().contains("Iamsocreative")) {
                assertTrue(val.getValue().exists());
            } else{
                assertFalse(val.getValue().exists());
            }
        }
    }

    /**
     * Tests that only files and sub-files, which are older than a specified cutOffTime,
     * are deleted when a file-ending is specified.
     */
    @Test
    public void cleanDirWithPatternTest() throws IOException{
        for(Map.Entry<String, File> val : testFiles.entrySet()){
            if(val.getKey().contains("existent")){
                assumeFalse(val.getValue().exists());
            } else {
                assumeTrue(val.getValue().exists());
            }
        }

        CleanUtils.cleanDirPattern(tempfol.getRoot().toPath(), setCutOffTime, suffixTest);

        for(Map.Entry<String, File> val : testFiles.entrySet()){
            if(val.getValue().toString().endsWith(suffixTest) && val.getValue().lastModified() < setCutOffTime || val.getKey().contains("existent")){
                assertFalse(val.getValue().exists());
            }else{
                assertTrue(val.getValue().exists());
            }
        }
    }

    /**
     * Tests that all files in the given directory and below are inspected and renamed
     * from <filename><ending> to <fileneame>, if their name ends with the given 'suffixTest'.
     */
    @Test
    public void renameFilesTest() throws IOException{
        for(Map.Entry<String, File> val : testFiles.entrySet()){
            if(val.getKey().contains("existent")){
                assumeFalse(val.getValue().exists());
            } else {
                assumeTrue(val.getValue().exists());
            }
        }

        CleanUtils.renameFiles(tempfol.getRoot().toPath(), suffixTest);

        assertEquals(new File(tempfol.getRoot(), "Iamsocreative.png"), testFiles.get("Iamsocreative.png"));
        assertEquals(new File(tempfol.getRoot(),"blah.tmp"), testFiles.get("blah.tmp"));
        assertEquals(new File(tempfol.getRoot(),"bloo.txt"), testFiles.get("bloo.txt"));
        assertEquals(new File(subtempfol, "Iamsocreative2.png"), testFiles.get("Iamsocreative2.png"));
        assertEquals(new File(subtempfol,"blah2.tmp"), testFiles.get("blah2.tmp"));
        assertEquals(new File(subtempfol,"bloo2.txt"), testFiles.get("bloo2.txt"));

        assertTrue(new File(tempfol.getRoot(),"waffles").exists());
        assertTrue(new File(subtempfol,"waffles2").exists());

        assertFalse(testFiles.get("waffles.zip").exists());
        assertFalse(testFiles.get("waffles2.zip").exists());
        assertFalse(testFiles.get("non-existent-file").exists());
        assertFalse(testFiles.get("non-existent-subfile").exists());
    }

    /**
     * Tests that all files in the given directory and below are inspected.
     * If their name ends with '.tmp'. they are renamed from <filename>.tmp to <filename>.
     */
    @Test
    public void renameTmpFilesTest() throws IOException{
        for(Map.Entry<String, File> val : testFiles.entrySet()){
            if(val.getKey().contains("existent")){
                assumeFalse(val.getValue().exists());
            } else {
                assumeTrue(val.getValue().exists());
            }
        }

        CleanUtils.renameTmpFiles(tempfol.getRoot().toPath());

        assertEquals(new File(tempfol.getRoot(), "Iamsocreative.png"), testFiles.get("Iamsocreative.png"));
        assertEquals(new File(tempfol.getRoot(),"bloo.txt"), testFiles.get("bloo.txt"));
        assertEquals(new File(tempfol.getRoot(),"waffles.zip"), testFiles.get("waffles.zip"));
        assertEquals(new File(subtempfol, "Iamsocreative2.png"), testFiles.get("Iamsocreative2.png"));
        assertEquals(new File(subtempfol,"bloo2.txt"), testFiles.get("bloo2.txt"));
        assertEquals(new File(subtempfol,"waffles2.zip"), testFiles.get("waffles2.zip"));

        assertTrue(new File(tempfol.getRoot(),"blah").exists());
        assertTrue(new File(subtempfol,"blah2").exists());

        assertFalse(testFiles.get("blah.tmp").exists());
        assertFalse(testFiles.get("blah2.tmp").exists());
        assertFalse(testFiles.get("non-existent-file").exists());
        assertFalse(testFiles.get("non-existent-subfile").exists());
    }

    /**
     * Tests cleanDir() for null input.
     *
     * Sub-folders are not tested.
     * Expects a {@code NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void cleanDirNullTest() throws IOException{
        CleanUtils.cleanDir(null, setCutOffTime);
    }

    /**
     * Tests cleanDirPattern() for null input.
     *
     * Sub-folders are not tested.
     * Expects a {@code NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void cleanDirPatNullTest() throws IOException{
        CleanUtils.cleanDirPattern(null, setCutOffTime, suffixTest);
    }

    /**
     * Tests renameFiles() for null input.
     *
     * Sub-folders are not tested.
     * Expects a {@code NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void renameFilesNullTest() throws IOException{
        CleanUtils.renameFiles(null, suffixTest);
    }

    /**
     * Tests renameTmpFiles() for null input.
     *
     * Sub-folders are not tested.
     * Expects a {@code NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void renameTmpNullTest() throws IOException{
        CleanUtils.renameTmpFiles(null);
    }

    /**
     * Tests cleanDir() for empty string input.
     *
     * Sub-folders are not tested.
     * Expects an {@code IOException}.
     */
    @Test(expected = IOException.class)
    public void cleanDirEmptyFileNameTest() throws IOException{
        CleanUtils.cleanDir(tempfol.newFile("").toPath(), setCutOffTime);
    }

    /**
     * Tests cleanDirPattern() for empty string input.
     *
     * Sub-folders are not tested.
     * Expects an {@code IOException}.
     */
    @Test(expected = IOException.class)
    public void cleanDirPatEmptyFileNameTest() throws IOException{
        CleanUtils.cleanDirPattern(tempfol.newFile("").toPath(), setCutOffTime, suffixTest);
    }

    /**
     * Tests renameFiles() for empty string input.
     *
     * Sub-folders are not tested.
     * Expects an {@code IOException}.
     */
    @Test(expected = IOException.class)
    public void renameFilesEmptyFileNameTest() throws IOException{
        CleanUtils.renameFiles(tempfol.newFile("").toPath(), suffixTest);
    }

    /**
     * Tests renameTmpFiles() for empty string input.
     *
     * Sub-folders are not tested.
     * Expects an {@code IOException}.
     */
    @Test(expected = IOException.class)
    public void renameTmpEmptyFileNameTest() throws IOException{
        CleanUtils.renameTmpFiles(tempfol.newFile("").toPath());
    }
}
