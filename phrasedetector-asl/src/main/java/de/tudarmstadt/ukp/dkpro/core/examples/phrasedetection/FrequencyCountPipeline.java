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

import de.tudarmstadt.ukp.dkpro.core.frequency.phrasedetection.FrequencyCounter;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

/**
 * Count unigram and bigram frequencies in a text collection and store in a file.
 * <p>
 * The file can subsequently be used by a {@link de.tudarmstadt.ukp.dkpro.core.frequency.phrasedetection.PhraseAnnotator}
 * to annotate phrases that may span across one or multiple tokens.
 *
 * @see PhraseAnnotationPipeline
 */
public class FrequencyCountPipeline
{
    static final String SOURCE_LOCATION = "src/main/resources/texts/*";

    /* ignore all n-grams that occur less frequently */
    static final int MIN_COUNT = 3;

    /* lowercase all tokens. Important: set this parameter in the phrase annotator to the same value! */
    static boolean LOWERCASE = true;

    /* target file, compression is determined by the file name suffix */
    static String COUNTS_FILE = "target/counts.txt";

    public static void main(String[] args)
            throws UIMAException, IOException
    {
        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, SOURCE_LOCATION,
                TextReader.PARAM_LANGUAGE, "en",
                TextReader.PARAM_LOG_FREQ, 10
        );
        AnalysisEngineDescription segmenter = createEngineDescription(OpenNlpSegmenter.class);
        AnalysisEngineDescription freqCounter = createEngineDescription(FrequencyCounter.class,
                FrequencyCounter.PARAM_TARGET_LOCATION, COUNTS_FILE,
                FrequencyCounter.PARAM_SORT_BY_COUNT, true,
                FrequencyCounter.PARAM_MIN_COUNT, MIN_COUNT,
                FrequencyCounter.PARAM_LOWERCASE, LOWERCASE);

        SimplePipeline.runPipeline(reader, segmenter, freqCounter);
    }
}
