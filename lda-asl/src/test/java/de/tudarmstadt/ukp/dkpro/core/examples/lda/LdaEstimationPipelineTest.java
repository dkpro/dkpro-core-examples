/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.examples.lda;

import cc.mallet.topics.ParallelTopicModel;
import org.apache.uima.UIMAException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class LdaEstimationPipelineTest
{

    @Test
    public void testMain()
            throws IOException, UIMAException
    {
        LdaEstimationPipeline.TARGET_FILE.delete();
        LdaEstimationPipeline.main(new String[] {});
        assertTrue("Model not generated.", LdaEstimationPipeline.TARGET_FILE.exists());
        try {
            ParallelTopicModel.read(LdaEstimationPipeline.TARGET_FILE);
        }
        catch (Exception e) {
            System.err.println("Could not read model file:\n" + e.getLocalizedMessage());
            throw new AssertionError(e);
        }
    }
}