package de.tudarmstadt.ukp.dkpro.core.examples.lda;

import cc.mallet.topics.ParallelTopicModel;
import org.apache.uima.UIMAException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created by schnober on 09.11.15.
 */
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