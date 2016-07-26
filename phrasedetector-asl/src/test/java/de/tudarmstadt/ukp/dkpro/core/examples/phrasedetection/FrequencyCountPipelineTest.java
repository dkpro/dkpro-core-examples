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

public class FrequencyCountPipelineTest
{

    @Test
    public void testMain()
            throws Exception
    {
        String expectedFirstLine = ",\t3306";
        int expectedLines = 8159;
        final File targetFile = new File(FrequencyCountPipeline.COUNTS_FILE);

        FrequencyCountPipeline.main(new String[0]);

        assertTrue(targetFile.exists());
        List<String> lines = Files.readAllLines(targetFile.toPath());
        assertEquals(expectedFirstLine, lines.get(0));
        assertEquals(expectedLines, lines.size());
    }
}