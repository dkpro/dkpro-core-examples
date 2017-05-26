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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.dl4j.internal.BinaryWordVectorSerializer.BinaryVectorizer;
import de.tudarmstadt.ukp.dkpro.core.dl4j.internal.Vectorize;

public class Dl4jPosTagger
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = true)
    private File targetModelLocation;

    public static final String PARAM_EMBEDDINGS_LOCATION = "embeddingsLocation";
    @ConfigurationParameter(name = PARAM_EMBEDDINGS_LOCATION, mandatory = false)
    private File embeddingsLocation;

    // Model information
    private MultiLayerNetwork net;
    private BinaryVectorizer wordVectors;
    private int truncateLength = 150;
    private int maxTagsetSize = 70;

    private Vectorize vectorize;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        vectorize = new Vectorize();
        
        try (ZipFile archive = new ZipFile(targetModelLocation)) {
            // Read tagset
            try (InputStream content = archive.getInputStream(archive.getEntry("tagset.txt"))) {
                List<String> tagset = IOUtils.readLines(content, StandardCharsets.UTF_8);
                vectorize = new Vectorize(tagset.toArray(new String[tagset.size()]));
            }

            // Read model
            try (InputStream content = archive.getInputStream(archive.getEntry("model.zip"))) {
                net = ModelSerializer.restoreMultiLayerNetwork(content);
                net.init();
            }
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        
        // Embeddings
        try {
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

        try {
            // Process sentence-by-sentence
            for (List<Token> sentence : sentences) {
                // Vectorize data
                DataSet data = new Vectorize().vectorize(asList(sentence), wordVectors,
                        truncateLength, maxTagsetSize, false);

                // Predict labels
                INDArray predicted = net.output(data.getFeatureMatrix(), false,
                        data.getFeaturesMaskArray(), data.getLabelsMaskArray());
                
                int i = 0;
                String[] tagset = vectorize.getTagset();
                for (Token t : sentence) {
                    int tagIdx = Nd4j.argMax(predicted, 1).getInt(i);
                    
                    // Create UIMA annotation
                    POS pos = new POS(aJCas, t.getBegin(), t.getEnd());
                    pos.setPosValue(tagset[tagIdx]);
                    pos.addToIndexes();
                    t.setPos(pos);
                    
                    i++;
                }
            }
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
