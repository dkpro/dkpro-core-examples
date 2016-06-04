/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.examples.tokenizedwriter;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TokenizedWriterPipelineTest
{
    @Test
    public void testMain()
            throws IOException, UIMAException
    {
        File expectedOutput = new File("src/test/resources/tokenized.txt");
        TokenizedWriterPipeline.TARGET_FILE.delete();
        TokenizedWriterPipeline.main(new String[] {});
        assertTrue("Output file not generated.", TokenizedWriterPipeline.TARGET_FILE.exists());
        assertEquals("Output file content does not match.",
                FileUtils.readFileToString(expectedOutput),
                FileUtils.readFileToString(TokenizedWriterPipeline.TARGET_FILE));
    }
}