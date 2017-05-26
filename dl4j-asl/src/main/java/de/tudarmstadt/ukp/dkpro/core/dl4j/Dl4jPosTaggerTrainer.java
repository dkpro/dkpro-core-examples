/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.dl4j;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.dl4j.internal.BinaryWordVectorSerializer.BinaryVectorizer;
import de.tudarmstadt.ukp.dkpro.core.dl4j.internal.Vectorize;

public class Dl4jPosTaggerTrainer
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private File targetLocation;

    public static final String PARAM_NETWORK = "network";
    @ConfigurationParameter(name = PARAM_NETWORK, mandatory = true)
    private String network;

    public static final String PARAM_EMBEDDINGS_LOCATION = "embeddingsLocation";
    @ConfigurationParameter(name = PARAM_EMBEDDINGS_LOCATION, mandatory = false)
    private File embeddingsLocation;

    public static final String PARAM_MAX_TAGSET_SIZE = "maxTagsetSize";
    @ConfigurationParameter(name = PARAM_MAX_TAGSET_SIZE, mandatory = true)
    private int maxTagsetSize;

    public static final String PARAM_TRUNCATE_LENGTH = "truncateLength";
    @ConfigurationParameter(name = PARAM_MAX_TAGSET_SIZE, mandatory = false, defaultValue="150")
    private int truncateLength;

    public static final String PARAM_EPOCHS = "nEpochs";
    @ConfigurationParameter(name = PARAM_EPOCHS, mandatory = false, defaultValue="1")
    private int nEpochs;

    public static final String PARAM_BATCH_SIZE = "batchSize";
    @ConfigurationParameter(name = PARAM_BATCH_SIZE, mandatory = false, defaultValue="25")
    private int batchSize;

    public static final String PARAM_EPOCH_SHUFFLE = "epochShuffle";
    @ConfigurationParameter(name = PARAM_EPOCH_SHUFFLE, mandatory = true, defaultValue="true")
    private boolean epochShuffle;

    private BinaryVectorizer wordVectors;
    private MultiLayerConfiguration conf;
    private Vectorize vectorize;
    
    private List<DataSet> trainingData;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        trainingData = new ArrayList<>();

        vectorize = new Vectorize();

        getLogger().info("Loading network configuration...");
        conf = MultiLayerConfiguration.fromJson(network);
    
        try {
            getLogger().info("Loading embeddings...");
            wordVectors = BinaryVectorizer.load(embeddingsLocation.toPath());
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        List<List<Token>> sentences = new ArrayList<>();
        for (Sentence s : select(aJCas, Sentence.class)) {
            sentences.add(selectCovered(Token.class, s));
        }

        // Add sentence-by-sentence
        for (List<Token> sentence : sentences) {
            try {
                trainingData.add(vectorize.vectorize(asList(sentence), wordVectors, truncateLength,
                        maxTagsetSize, true));
            }
            catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        // Instantiate NN from configuration
        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        
        // Nice web-based interface to watch NN parameters as they train
        //net.setListeners(new ScoreIterationListener(1), new HistogramIterationListener(1, true));

        // Perform the actual training
        for (int i = 0; i < nEpochs; i++) {
            DataSetIterator train = new ListDataSetIterator(trainingData, batchSize);
            net.fit(train);
        }

        try (ArchiveOutputStream archive = new ZipArchiveOutputStream(targetLocation)) {
            // Write tagset
            {
                ZipArchiveEntry entry = new ZipArchiveEntry("tagset.txt");
                archive.putArchiveEntry(entry);
                for (String tag : vectorize.getTagset()) {
                    archive.write(tag.getBytes(StandardCharsets.UTF_8));
                    archive.write("\n".getBytes(StandardCharsets.UTF_8));
                }
                archive.closeArchiveEntry();
            }
            
            // Write model
            {
                ZipArchiveEntry entry = new ZipArchiveEntry("model.zip");
                archive.putArchiveEntry(entry);
                ModelSerializer.writeModel(net, new CloseShieldOutputStream(archive), true);
                archive.closeArchiveEntry();
            }
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
