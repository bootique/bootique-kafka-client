/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.bootique.kafka.streams;

import io.bootique.kafka.BootstrapServers;
import io.bootique.kafka.BootstrapServersCollection;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.util.Objects;
import java.util.Properties;

/**
 * @since 1.0.RC1
 */
public class DefaultKafkaStreamsBuilder implements KafkaStreamsBuilder {

    private KafkaStreamsManager streamsManager;
    private BootstrapServersCollection clusters;

    private Topology topology;
    private Properties properties;
    private String clusterName;

    public DefaultKafkaStreamsBuilder(
            KafkaStreamsManager streamsManager,
            BootstrapServersCollection clusters,
            Properties properties) {

        this.streamsManager = streamsManager;
        this.clusters = clusters;

        // cloning passed properties as we may be changing them in the builder downstream
        this.properties = new Properties(properties);
    }

    @Override
    public KafkaStreamsBuilder topology(Topology topology) {
        this.topology = topology;
        return this;
    }

    @Override
    public KafkaStreamsBuilder properties(Properties properties) {
        // custom properties override everything in the default props
        this.properties.putAll(properties);
        return this;
    }

    @Override
    public KafkaStreamsBuilder cluster(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }

    @Override
    public KafkaStreamsRunner create() {
        return new KafkaStreamsRunner(streamsManager, createStreams());
    }

    protected KafkaStreams createStreams() {

        this.properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, resolveBootstrapServers());

        Objects.requireNonNull(topology, "KafkaStreams 'topology' is not set");
        return new KafkaStreams(topology, properties);
    }

    private String resolveBootstrapServers() {
        BootstrapServers cluster = clusterName != null
                ? clusters.getCluster(clusterName)
                : clusters.getDefaultCluster();

        return cluster.asString();
    }
}