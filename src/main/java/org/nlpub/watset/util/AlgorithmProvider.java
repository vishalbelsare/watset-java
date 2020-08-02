/*
 * Copyright 2019 Dmitry Ustalov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.nlpub.watset.util;

import org.jgrapht.Graph;
import org.nlpub.watset.graph.*;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

/**
 * A utility class that creates instances of the graph clustering algorithms.
 *
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 */
public class AlgorithmProvider<V, E> implements Function<Graph<V, E>, Clustering<V>> {
    private static final Logger logger = Logger.getLogger(AlgorithmProvider.class.getSimpleName());

    private final String algorithm;
    private final Map<String, String> params;
    private final NodeWeighting<V, E> weighting;

    /**
     * Create an instance of this utility class with empty parameter map.
     *
     * @param algorithm the algorithm identifier
     */
    public AlgorithmProvider(String algorithm) {
        this(algorithm, null);
    }

    /**
     * Create an instance of this utility class.
     *
     * @param algorithm the algorithm identifier
     * @param params    the parameter map for the algorithm
     */
    public AlgorithmProvider(String algorithm, Map<String, String> params) {
        this.algorithm = requireNonNull(algorithm, "algorithm is not specified");
        this.params = requireNonNullElse(params, Collections.emptyMap());
        this.weighting = parseChineseWhispersNodeWeighting();
    }

    @Override
    public Clustering<V> apply(Graph<V, E> graph) {
        switch (algorithm.toLowerCase(Locale.ROOT)) {
            case "empty":
                return new EmptyClustering.Builder<V, E>().build(graph);
            case "together":
                return new TogetherClustering.Builder<V, E>().build(graph);
            case "singleton":
                return new SingletonClustering.Builder<V, E>().build(graph);
            case "components":
                return new ComponentsClustering.Builder<V, E>().build(graph);
            case "cw":
                return new ChineseWhispers.Builder<V, E>().setWeighting(weighting).build(graph);
            case "mcl":
                final var mcl = new MarkovClustering.Builder<V, E>();

                if (params.containsKey("e")) mcl.setE(Integer.parseInt(params.get("e")));
                if (params.containsKey("r")) mcl.setR(Double.parseDouble(params.get("r")));

                return mcl.build(graph);
            case "mcl-bin":
                final var mclOfficial = new MarkovClusteringOfficial.Builder<V, E>().
                        setPath(Path.of(params.get("bin"))).
                        setThreads(Runtime.getRuntime().availableProcessors());

                if (params.containsKey("r")) mclOfficial.setR(Double.parseDouble(params.get("r")));

                return mclOfficial.build(graph);
            case "maxmax":
                return new MaxMax.Builder<V, E>().build(graph);
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }
    }

    private NodeWeighting<V, E> parseChineseWhispersNodeWeighting() {
        switch (params.getOrDefault("mode", "top").toLowerCase(Locale.ROOT)) {
            case "label":
                return NodeWeighting.label();
            case "top":
                return NodeWeighting.top();
            case "log":
                return NodeWeighting.log();
            case "nolog": // We used this notation in many papers; kept for compatibility
                logger.warning("Please update your code: 'nolog' weighting is renamed to 'lin'.");
                return NodeWeighting.linear();
            case "lin":
                return NodeWeighting.linear();
            default:
                throw new IllegalArgumentException("Unknown mode: " + params.get("mode"));
        }
    }
}
