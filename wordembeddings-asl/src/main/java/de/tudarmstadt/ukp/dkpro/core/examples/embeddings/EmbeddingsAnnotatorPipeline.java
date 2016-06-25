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
package de.tudarmstadt.ukp.dkpro.core.examples.embeddings;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.mallet.wordembeddings.WordEmbeddingsAnnotator;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stopwordremover.StopWordRemover;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

public class EmbeddingsAnnotatorPipeline
{
    protected static final File OUTPUT_FILE = new File("target/annotator_output.txt");
    private static final URL EMBEDDINGS_FILE = EmbeddingsAnnotatorPipeline.class.getClassLoader()
            .getResource("embeddings");
    private static final String LANGUAGE = "en";
    private static final URL STOPWORD_FILE = EmbeddingsPipeline.class.getClassLoader()
            .getResource("stopwords_en.txt");
    private static final String DEFAULT_SOURCE = "src/main/resources/texts/*";

    public static void main(String[] args)
            throws IOException, UIMAException
    {
        String inputDir = args.length > 0 ? args[0] : DEFAULT_SOURCE;

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, inputDir,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(OpenNlpSegmenter.class);
        AnalysisEngineDescription stopwordRemover = createEngineDescription(StopWordRemover.class,
                StopWordRemover.PARAM_MODEL_LOCATION, STOPWORD_FILE);
        AnalysisEngineDescription embeddingsAnnotator = createEngineDescription(
                WordEmbeddingsAnnotator.class,
                WordEmbeddingsAnnotator.PARAM_MODEL_LOCATION, EMBEDDINGS_FILE);
        AnalysisEngineDescription writer = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, OUTPUT_FILE);

        SimplePipeline.runPipeline(reader, segmenter, stopwordRemover, embeddingsAnnotator, writer);
    }
}
