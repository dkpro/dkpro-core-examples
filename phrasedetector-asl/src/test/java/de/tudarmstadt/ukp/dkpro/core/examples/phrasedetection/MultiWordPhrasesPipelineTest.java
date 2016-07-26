/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.examples.phrasedetection;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MultiWordPhrasesPipelineTest
{

    @Test
    public void testMain()
            throws Exception
    {
        File countsFile1 = new File(MultiWordPhrasesPipeline.COUNTS1_PATH);
        int expectedLines1 = 14612;
        String expectedFirstLine1 = ",\t3306";

        File countsFile2 = new File(MultiWordPhrasesPipeline.COUNTS2_PATH);
        int expectedLines2 = 14524;
        String expectedFirstLine2 = ",\t3306";

        MultiWordPhrasesPipeline.main(new String[0]);

        assertTrue(countsFile1.exists());
        assertTrue(countsFile2.exists());

        List<String> lines1 = Files.readAllLines(countsFile1.toPath());
        assertEquals(expectedFirstLine1, lines1.get(0));
        assertEquals(expectedLines1, lines1.size());

        List<String> lines2 = Files.readAllLines(countsFile2.toPath());
        assertEquals(expectedFirstLine2, lines2.get(0));
        assertEquals(expectedLines2, lines2.size());
    }
}