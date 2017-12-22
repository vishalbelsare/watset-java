/*
 * Copyright 2017 Dmitry Ustalov
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

package org.nlpub.cw.weighting;

import org.jgrapht.Graph;

public class TopWeighting<V, E> extends NeighborhoodWeighting<V, E> {
    protected double getScore(Graph<V, E> graph, V node, V neighbor) {
        return graph.getEdgeWeight(graph.getEdge(node, neighbor));
    }
}