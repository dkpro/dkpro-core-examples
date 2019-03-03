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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.iteratePipeline;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.LexicalPhrase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.frequency.phrasedetection.FrequencyWriter;
import de.tudarmstadt.ukp.dkpro.core.frequency.phrasedetection.PhraseAnnotator;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

/**
 * Run two iterations of counting n-grams and annotation phrases. In the first iteration, unigrams
 * and bigrams are counted on the level of {@link Token}s. Subsequently, unigram and bigram phrases are annotated.
 * <p>
 * In the second iteration, phrases comprising one or multiple n-grams (with n=1 or 2, depending
 * on the results of the first iteration) are counted. This allows the annotator to detect longer phrases.
 * <p>
 * This could be repeated multiple times in order to catch even longer phrases.
 * <p>
 * Note that due to the limited amount of data in this dummy example, the detected phrases are
 * mostly short.
 *
 * @see FrequencyCountPipeline
 * @see PhraseAnnotationPipeline
 */
public class MultiWordPhrasesPipeline
{
    /* the source of the text data */
    private static final String TEXT_PATH = "src/main/resources/texts/*";

    /* minimum token/n-gram frequency */
    private static final int MIN_COUNT = 2;

    /* count files for the 1st and 2nd iteration respectively */
    static final String COUNTS1_PATH = "target/counts1.txt";
    static final String COUNTS2_PATH = "target/counts2.txt";

    /* path for storing binary cas files in order to pass annotations made in the first iteration
    to the second iteration */
    private static final String BINCAS_PATH = "target/bincas";

    /* during counting and annotating, ignore casing */
    private static final boolean LOWERCASE = true;

    public static void main(String[] args)
            throws UIMAException, IOException
    {
        /* initially, operate on token annotations */
        String featurePath = Token.class.getCanonicalName();

        /* count unigrams and bigrams */
        {
            CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                    TextReader.PARAM_SOURCE_LOCATION, TEXT_PATH,
                    TextReader.PARAM_LANGUAGE, "en",
                    TextReader.PARAM_LOG_FREQ, 10);
            AnalysisEngineDescription segmenter = createEngineDescription(OpenNlpSegmenter.class);
            AnalysisEngineDescription freqCounter = createEngineDescription(FrequencyWriter.class,
                    FrequencyWriter.PARAM_TARGET_LOCATION, COUNTS1_PATH,
                    FrequencyWriter.PARAM_SORT_BY_COUNT, true,
                    FrequencyWriter.PARAM_MIN_COUNT, MIN_COUNT,
                    FrequencyWriter.PARAM_FEATURE_PATH, featurePath,
                    FrequencyWriter.PARAM_LOWERCASE, LOWERCASE);
            AnalysisEngineDescription writer = createEngineDescription(BinaryCasWriter.class,
                    BinaryCasWriter.PARAM_TARGET_LOCATION, BINCAS_PATH,
                    BinaryCasWriter.PARAM_OVERWRITE, true);

            runPipeline(reader, segmenter, freqCounter, writer);
        }

        float threshold = (float) 100;
        /* annotate unigram and bigram phrases */
        {
            /* read previously processed documents */
            CollectionReaderDescription reader = createReaderDescription(BinaryCasReader.class,
                    BinaryCasReader.PARAM_SOURCE_LOCATION, BINCAS_PATH + "/*bcas",
                    BinaryCasReader.PARAM_LANGUAGE, "en",
                    BinaryCasReader.PARAM_LOG_FREQ, 10);
            AnalysisEngineDescription phraseAnnotator = createEngineDescription(
                    PhraseAnnotator.class,
                    PhraseAnnotator.PARAM_MODEL_LOCATION, COUNTS1_PATH,
                    PhraseAnnotator.PARAM_FEATURE_PATH, featurePath,
                    PhraseAnnotator.PARAM_THRESHOLD, threshold,
                    PhraseAnnotator.PARAM_DISCOUNT, FrequencyCountPipeline.MIN_COUNT,
                    PhraseAnnotator.PARAM_LOWERCASE, LOWERCASE);
            /* save binary files with annotations */
            AnalysisEngineDescription writer = createEngineDescription(BinaryCasWriter.class,
                    BinaryCasWriter.PARAM_TARGET_LOCATION, BINCAS_PATH,
                    BinaryCasWriter.PARAM_OVERWRITE, true);
            runPipeline(reader, phraseAnnotator, writer);
        }

        /* operate on previously annotated phrases instead of tokens */
        featurePath = LexicalPhrase.class.getCanonicalName();

        /* re-count n-gram phrases, allowing for longer phrases */
        {
            /* load binary files with previously create phrase (and all other) annotations */
            CollectionReaderDescription reader = createReaderDescription(BinaryCasReader.class,
                    BinaryCasReader.PARAM_SOURCE_LOCATION, BINCAS_PATH + "/*bcas",
                    BinaryCasReader.PARAM_LANGUAGE, "en",
                    BinaryCasReader.PARAM_LOG_FREQ, 10);
            AnalysisEngineDescription freqCounter = createEngineDescription(FrequencyWriter.class,
                    FrequencyWriter.PARAM_TARGET_LOCATION, COUNTS2_PATH,
                    FrequencyWriter.PARAM_SORT_BY_COUNT, true,
                    FrequencyWriter.PARAM_MIN_COUNT, MIN_COUNT,
                    FrequencyWriter.PARAM_FEATURE_PATH, featurePath,
                    FrequencyWriter.PARAM_LOWERCASE, LOWERCASE);

            runPipeline(reader, freqCounter);
        }

        /* reduce the threshold for the second iteration */
        threshold = (float) 70;

        /* annotate phrases where each phrase may span across one or multiple tokens */
        {
            CollectionReaderDescription reader = createReaderDescription(BinaryCasReader.class,
                    BinaryCasReader.PARAM_SOURCE_LOCATION, BINCAS_PATH + "/*bcas",
                    BinaryCasReader.PARAM_LANGUAGE, "en",
                    BinaryCasReader.PARAM_LOG_FREQ, 10);
            AnalysisEngineDescription phraseAnnotator = createEngineDescription(
                    PhraseAnnotator.class,
                    PhraseAnnotator.PARAM_MODEL_LOCATION, COUNTS2_PATH,
                    PhraseAnnotator.PARAM_FEATURE_PATH, featurePath,
                    PhraseAnnotator.PARAM_THRESHOLD, threshold,
                    PhraseAnnotator.PARAM_DISCOUNT, MIN_COUNT,
                    PhraseAnnotator.PARAM_LOWERCASE, LOWERCASE);

            /* print multiword-phrases */
            for (JCas jCas : iteratePipeline(reader, phraseAnnotator)) {
                select(jCas, LexicalPhrase.class).stream()
                        .map(Annotation::getCoveredText)
                        .filter(phrase -> phrase.contains(" ")) // identify multi-word phrases
                        .forEach(System.out::println);
            }
        }
    }
}
