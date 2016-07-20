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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.LexicalPhrase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.frequency.phrasedetection.PhraseAnnotator;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

/**
 * Based on a previously created token count file, annotate phrases within a text collection.
 * <p>
 * The {@link PhraseAnnotator} adds {@link LexicalPhrase} annotations that span across one or
 * multiple tokens, depending on the co-occurrence frequencies.
 * <p>
 * The counts file has been created using a {@link de.tudarmstadt.ukp.dkpro.core.frequency.phrasedetection.FrequencyCounter}.
 *
 * @see FrequencyCountPipeline
 */
public class PhraseAnnotationPipeline
{
    private static final String COUNTS_PATH = "src/main/resources/counts.xz";

    public static void main(String[] args)
            throws UIMAException, IOException
    {
        /* a lower threshold yields more multi-token phrases */
        float threshold = (float) 100.0;

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, FrequencyCountPipeline.SOURCE_LOCATION,
                TextReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(OpenNlpSegmenter.class);
        AnalysisEngineDescription phraseAnnotator = createEngineDescription(PhraseAnnotator.class,
                PhraseAnnotator.PARAM_MODEL_LOCATION, COUNTS_PATH,
                PhraseAnnotator.PARAM_FEATURE_PATH, Token.class.getCanonicalName(),
                PhraseAnnotator.PARAM_THRESHOLD, threshold,
                PhraseAnnotator.PARAM_DISCOUNT, FrequencyCountPipeline.MIN_COUNT,
                PhraseAnnotator.PARAM_LOWERCASE, FrequencyCountPipeline.LOWERCASE);

        /* iterate over the documents and print multi-word phrases. Note that all other tokens are
        marked as phrases too, but are not displayed here.
         */
        for (JCas jCas : SimplePipeline.iteratePipeline(reader, segmenter, phraseAnnotator)) {
            select(jCas, LexicalPhrase.class).stream()
                    .map(Annotation::getCoveredText)    // extract text
                    .filter(phrase -> phrase.contains(" ")) // filter for multi-word phrases
                    .forEach(System.out::println);
        }
    }
}
