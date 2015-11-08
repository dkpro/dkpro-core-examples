package de.tudarmstadt.ukp.dkpro.core.examples.tokenizedwriter;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.text.TokenizedTextWriter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.File;
import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

/**
 * This pipeline demonstrates the usage of {@link TokenizedTextWriter}. It reads the given text
 * files, segments it (tokens and sentences, and writes all documents to a target file, one
 * sentence per line, tokens separated by whitespaces.
 * <p>
 * The output format can immediately be fed to e.g. Word2Vec.
 * </p>
 */
public class TokenizedWriterPipeline
{
    private static final File TARGET_FILE = new File("target/tokenized.txt");
    private static final String LANGUAGE = "en";
    private static final String SOURCE_DIR = "src/main/resources/texts/*";

    public static void main(String[] args)
            throws IOException, UIMAException
    {
        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, SOURCE_DIR,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(OpenNlpSegmenter.class);
        AnalysisEngineDescription writer = createEngineDescription(TokenizedTextWriter.class,
                TokenizedTextWriter.PARAM_TARGET_LOCATION, TARGET_FILE);

        SimplePipeline.runPipeline(reader, segmenter, writer);
    }
}
