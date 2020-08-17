/*
 * Copyright 2020 Dmitry Ustalov
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

package org.nlpub.watset.graph;

import org.apache.commons.math3.ml.clustering.Clusterer;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.util.VertexToIntegerMapping;
import org.nlpub.watset.util.Matrices;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.jgrapht.GraphTests.requireUndirected;

/**
 * @param <V> the type of nodes in the graph
 * @param <E> the type of edges in the graph
 * @see <a href="https://doi.org/10.1109/34.868688">Shi &amp; Malik (IEEE PAMI 22:8)</a>
 * @see <a href="https://doi.org/10.1007/s11222-007-9033-z">von Luxburg (Statistics and Computing 17:4)</a>
 */
public class SpectralClustering<V, E> implements ClusteringAlgorithm<V> {
    /**
     * Builder for {@link SpectralClustering}.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     */
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static class Builder<V, E> implements ClusteringAlgorithmBuilder<V, E, SpectralClustering<V, E>> {
        private Clusterer<NodeEmbedding<V>> clusterer;
        private Integer k;

        public Builder<V, E> setClusterer(Clusterer<NodeEmbedding<V>> clusterer) {
            this.clusterer = clusterer;
            return this;
        }

        public Builder<V, E> setK(int k) {
            this.k = k;
            return this;
        }

        @Override
        public SpectralClustering<V, E> apply(Graph<V, E> graph) {
            return new SpectralClustering<>(graph, clusterer, requireNonNull(k, "k must be specified"));
        }
    }

    /**
     * Create a builder.
     *
     * @param <V> the type of nodes in the graph
     * @param <E> the type of edges in the graph
     * @return a builder
     */
    public static <V, E> Builder<V, E> builder() {
        return new Builder<>();
    }

    private final Graph<V, E> graph;
    private final Clusterer<NodeEmbedding<V>> clusterer;
    private final int k;
    private Clustering<V> clustering;

    public SpectralClustering(Graph<V, E> graph, Clusterer<NodeEmbedding<V>> clusterer, int k) {
        this.graph = requireUndirected(graph);
        this.clusterer = clusterer;
        this.k = k;
    }

    @Override
    public Clustering<V> getClustering() {
        if (isNull(clustering)) {
            clustering = new Implementation<>(graph, clusterer, k).compute();
        }

        return clustering;
    }

    public static class Implementation<V, E> {
        protected final Clusterer<NodeEmbedding<V>> clusterer;
        protected final VertexToIntegerMapping<V> mapping;
        protected final List<NodeEmbedding<V>> embeddings;

        public Implementation(Graph<V, E> graph, Clusterer<NodeEmbedding<V>> clusterer, int k) {
            this.clusterer = clusterer;
            this.mapping = Graphs.getVertexToIntegerMapping(graph);
            this.embeddings = Matrices.computeSpectralEmbedding(graph, mapping, k);
        }

        public Clustering<V> compute() {
            final var clusters = clusterer.cluster(embeddings);

            return new ClusteringImpl<>(clusters.stream().
                    map(cluster -> cluster.getPoints().stream().
                            map(NodeEmbedding::getNode).
                            collect(Collectors.toSet())).
                    collect(Collectors.toList()));
        }
    }
}
