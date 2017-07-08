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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.iteratePipeline;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.ConfigurationParameterFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesBidirectionalLSTM;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import de.tudarmstadt.ukp.dkpro.core.api.datasets.Dataset;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.DatasetFactory;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.Split;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.dl4j.internal.BinaryWordVectorSerializer;
import de.tudarmstadt.ukp.dkpro.core.dl4j.internal.BinaryWordVectorSerializer.BinaryVectorizer;
import de.tudarmstadt.ukp.dkpro.core.eval.EvalUtil;
import de.tudarmstadt.ukp.dkpro.core.eval.model.Span;
import de.tudarmstadt.ukp.dkpro.core.eval.report.Result;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2006Reader;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2006Writer;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class Dl4jPosTaggerTrainerTest
{
    private static final String DIM = "50";
    
    private static Dataset ds;

    @Test
    public void testLSTM()
            throws Exception
    {
        String embeddings = "target/glove.6B." + DIM + "d.dl4jw2v";
        
        int embeddingSize = getEmbeddingsSize(embeddings);
        int maxTagsetSize = 70;
        int batchSize = 25;
        int epochs = 2;
        boolean shuffle = true;
        int iterations = 2;
        double learningRate = 0.1;
        
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(iterations)
                .seed(12345l)
                .updater(Updater.RMSPROP).regularization(true).l2(1e-5)
                .weightInit(WeightInit.RELU)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .gradientNormalizationThreshold(1.0)
                .learningRate(learningRate)
                .list()
                .layer(0, new GravesLSTM.Builder()
                            .activation(Activation.SOFTSIGN)
                            .nIn(embeddingSize)
                            .nOut(200)
                            .build())
                .layer(1, new RnnOutputLayer.Builder()
                            .activation(Activation.SOFTMAX)
                            .lossFunction(LossFunctions.LossFunction.MCXENT)
                            .nIn(200)
                            .nOut(maxTagsetSize)
                            .build())
                .pretrain(false).backprop(true).build();
        
        Result results = test(conf.toJson(), embeddings, maxTagsetSize, epochs, batchSize, shuffle);
        
        // DIM = 50
        assertEquals(0.413291, results.getFscore(), 0.0001);
        assertEquals(0.408929, results.getPrecision(), 0.0001);
        assertEquals(0.417746, results.getRecall(), 0.0001);

        // DIM = 100
        // assertEquals(0.711824, results.getFscore(), 0.0001);
        // assertEquals(0.704313, results.getPrecision(), 0.0001);
        // assertEquals(0.719498, results.getRecall(), 0.0001);
    }
    
    @Test
    public void testBidirectonalLSTM()
            throws Exception
    {
        String embeddings = "target/glove.6B." + DIM + "d.dl4jw2v";
        
        int featuresSize = getEmbeddingsSize(embeddings);
        int maxTagsetSize = 70;
        int batchSize = 25;
        int epochs = 2;
        boolean shuffle = true;
        int iterations = 2;
        double learningRate = 0.1;
        
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(iterations)
                .seed(12345l)
                .updater(Updater.RMSPROP).regularization(true).l2(1e-5)
                .weightInit(WeightInit.RELU)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .gradientNormalizationThreshold(1.0)
                .learningRate(learningRate)
                .list()
                .layer(0, new GravesBidirectionalLSTM.Builder()
                                .activation(Activation.SOFTSIGN)
                                .nIn(featuresSize)
                                .nOut(200)
                                .build())
                .layer(1, new RnnOutputLayer.Builder()
                                .activation(Activation.SOFTMAX)
                                .lossFunction(LossFunctions.LossFunction.MCXENT)
                                .nIn(200)
                                .nOut(maxTagsetSize)
                                .build())
                .pretrain(false).backprop(true).build();
        
        Result results = test(conf.toJson(), embeddings, maxTagsetSize, epochs, batchSize, shuffle);

        // DIM = 50
        assertEquals(0.742591, results.getFscore(), 0.0001);
        assertEquals(0.734754, results.getPrecision(), 0.0001);
        assertEquals(0.750596, results.getRecall(), 0.0001);

        // DIM = 100
        // assertEquals(0.742591, results.getFscore(), 0.0001);
        // assertEquals(0.734754, results.getPrecision(), 0.0001);
        // assertEquals(0.750596, results.getRecall(), 0.0001);
    }
    
    public Result test(String network, String embeddings, int maxTagsetSize, int aEpochs,
            int aBatchSize, boolean aEpochShuffle)
        throws Exception
    {
        File targetFolder = testContext.getTestOutputFolder();
        
        Split split = ds.getSplit(0.8);
        
        // Train model
        File model = new File(targetFolder, "model.bin");
        CollectionReaderDescription trainReader = createReaderDescription(
                Conll2006Reader.class,
                Conll2006Reader.PARAM_PATTERNS, split.getTrainingFiles(),
                Conll2006Reader.PARAM_LANGUAGE, ds.getLanguage(),
                Conll2006Reader.PARAM_USE_CPOS_AS_POS, true);
        
        AnalysisEngineDescription trainer = createEngineDescription(
                Dl4jPosTaggerTrainer.class,
                Dl4jPosTaggerTrainer.PARAM_NETWORK, network,
                Dl4jPosTaggerTrainer.PARAM_EPOCHS, aEpochs,
                Dl4jPosTaggerTrainer.PARAM_EPOCH_SHUFFLE, aEpochShuffle,
                Dl4jPosTaggerTrainer.PARAM_BATCH_SIZE, aBatchSize,
                Dl4jPosTaggerTrainer.PARAM_TARGET_LOCATION, model,
                Dl4jPosTaggerTrainer.PARAM_EMBEDDINGS_LOCATION, embeddings,
                Dl4jPosTaggerTrainer.PARAM_MAX_TAGSET_SIZE, maxTagsetSize);
        
        SimplePipeline.runPipeline(trainReader, trainer);

        // Apply model and collect labels
        System.out.println("Applying model to test data");
        CollectionReaderDescription testReader = createReaderDescription(
                Conll2006Reader.class,
                Conll2006Reader.PARAM_PATTERNS, split.getTestFiles(),
                Conll2006Reader.PARAM_READ_POS, false,
                Conll2006Reader.PARAM_LANGUAGE, ds.getLanguage());
        
        AnalysisEngineDescription ner = createEngineDescription(
                Dl4jPosTagger.class,
                //Dl4jPosTagger.PARAM_PRINT_TAGSET, true,
                Dl4jPosTagger.PARAM_MODEL_LOCATION, new File(targetFolder, "model.bin"),
                Dl4jPosTagger.PARAM_EMBEDDINGS_LOCATION, embeddings);

        AnalysisEngineDescription out = createEngineDescription(
                Conll2006Writer.class,
                Conll2006Writer.PARAM_SINGULAR_TARGET, true,
                Conll2006Writer.PARAM_TARGET_LOCATION, new File(targetFolder, "data.conll"));
        
        List<Span<String>> actual = EvalUtil.loadSamples(iteratePipeline(testReader, ner, out),
                POS.class, pos -> {
                    return pos.getPosValue();
                });
        System.out.printf("Actual samples: %d%n", actual.size());
        
        // Read reference data collect labels
        ConfigurationParameterFactory.setParameter(testReader, 
                Conll2006Reader.PARAM_READ_POS, true);
        List<Span<String>> expected = EvalUtil.loadSamples(testReader, POS.class, pos -> {
            return pos.getPosValue();
        });
        System.out.printf("Expected samples: %d%n", expected.size());

        return EvalUtil.dumpResults(targetFolder, expected, actual);
    }

    private int getEmbeddingsSize(String embeddings)
        throws IOException
    {
        BinaryVectorizer wordVectors = BinaryVectorizer.load(Paths.get(embeddings));
        return wordVectors.getVectorSize();
    }
    
    @BeforeClass
    public static void setup()
        throws Exception
    {
        DatasetFactory loader = new DatasetFactory(DkproTestContext.getCacheFolder());
        ds = loader.load("gum-en-conll-2.2.0");

        convertGloveVectors();
    }

    public static void convertGloveVectors()
        throws Exception
    {
        DatasetFactory loader = new DatasetFactory(DkproTestContext.getCacheFolder());
        Dataset dsGlove = loader.load("glove.6B-en-20151025");
        
        File input = dsGlove.getFile("glove/glove.6B." + DIM + "d.txt");
        String output = "target/glove.6B." + DIM + "d.dl4jw2v";
        
        System.out.println("Loading vectors...");
        WordVectors wv = WordVectorSerializer.loadTxtVectors(new FileInputStream(input), false);
        System.out.println("Loading vectors... complete");

        System.out.println("Converting vectors...");
        BinaryWordVectorSerializer.convertWordVectorsToBinary(wv, true, Locale.US, Paths.get(output));
        System.out.println("Converting vectors... complete");
        
        System.out.println("Verifying vectors...");
        BinaryWordVectorSerializer.verify(wv, Paths.get(output));
        System.out.println("Verifying vectors... complete");
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}