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
package de.tudarmstadt.ukp.dkpro.examples.nameannotation;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class NameAnnotationPipelineTest
{

    public static final File EXPECTED_OUTPUT = new File(
            "src/test/resources/output.txt");
    public static final String ENCODING = "UTF-8";
    public static final File OUTPUT_FILE = new File("target/NameAnnotationPipeline.txt");

    @Test
    public void test()
            throws Exception
    {
        NameAnnotationPipeline.main(new String[] {});
        assertEquals(
                FileUtils.readFileToString(EXPECTED_OUTPUT, ENCODING).trim(),
                FileUtils.readFileToString(OUTPUT_FILE, ENCODING).trim());
    }
}
