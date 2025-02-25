/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.resourcemanager.utils;

import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.heartbeat.HeartbeatServices;
import org.apache.flink.runtime.heartbeat.TestingHeartbeatServices;
import org.apache.flink.runtime.highavailability.TestingHighAvailabilityServices;
import org.apache.flink.runtime.leaderelection.TestingLeaderElectionService;
import org.apache.flink.runtime.resourcemanager.DefaultJobLeaderIdService;
import org.apache.flink.runtime.resourcemanager.JobLeaderIdService;
import org.apache.flink.runtime.resourcemanager.slotmanager.DeclarativeSlotManagerBuilder;
import org.apache.flink.runtime.resourcemanager.slotmanager.SlotManager;
import org.apache.flink.runtime.rpc.RpcService;
import org.apache.flink.runtime.security.token.DelegationTokenManager;
import org.apache.flink.runtime.security.token.NoOpDelegationTokenManager;
import org.apache.flink.runtime.testutils.DirectScheduledExecutorService;
import org.apache.flink.util.concurrent.ScheduledExecutorServiceAdapter;

import static org.apache.flink.util.Preconditions.checkNotNull;

/** Mock services needed by the resource manager. */
public class MockResourceManagerRuntimeServices {

    public final RpcService rpcService;
    public final Time timeout;
    public final TestingHighAvailabilityServices highAvailabilityServices;
    public final HeartbeatServices heartbeatServices;
    public final DelegationTokenManager delegationTokenManager;
    public final TestingLeaderElectionService rmLeaderElectionService;
    public final JobLeaderIdService jobLeaderIdService;
    public final SlotManager slotManager;

    public MockResourceManagerRuntimeServices(RpcService rpcService, Time timeout) {
        this(
                rpcService,
                timeout,
                DeclarativeSlotManagerBuilder.newBuilder()
                        .setScheduledExecutor(
                                new ScheduledExecutorServiceAdapter(
                                        new DirectScheduledExecutorService()))
                        .setTaskManagerRequestTimeout(Time.seconds(10))
                        .setSlotRequestTimeout(Time.seconds(10))
                        .setTaskManagerTimeout(Time.minutes(1))
                        .build());
    }

    public MockResourceManagerRuntimeServices(
            RpcService rpcService, Time timeout, SlotManager slotManager) {
        this.rpcService = checkNotNull(rpcService);
        this.timeout = checkNotNull(timeout);
        this.slotManager = slotManager;
        highAvailabilityServices = new TestingHighAvailabilityServices();
        rmLeaderElectionService = new TestingLeaderElectionService();
        highAvailabilityServices.setResourceManagerLeaderElectionService(rmLeaderElectionService);
        heartbeatServices = new TestingHeartbeatServices();
        delegationTokenManager = new NoOpDelegationTokenManager();
        jobLeaderIdService =
                new DefaultJobLeaderIdService(
                        highAvailabilityServices,
                        rpcService.getScheduledExecutor(),
                        Time.minutes(5L));
    }
}
