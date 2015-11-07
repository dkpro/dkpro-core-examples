/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.examples.nameannotation;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class NameAnnotationPipelineTest
{
    @Test
    public void test()
            throws Exception
    {
        NameAnnotationPipeline.main(new String[] {});
        assertEquals(
                FileUtils.readFileToString(
                        new File("src/test/resources/nameannotation/NameAnnotationPipeline.txt"),
                        "UTF-8").trim(),
                FileUtils.readFileToString(
                        new File("target/NameAnnotationPipeline.txt"), "UTF-8").trim());
    }
}
