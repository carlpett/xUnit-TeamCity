/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package se.capeit.dev.xunittestrunner;

import java.util.concurrent.*;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.log.Loggers;
import org.jetbrains.annotations.NotNull;

abstract class FutureBasedBuildProcess implements BuildProcess, Callable<BuildFinishedStatus>
{
    @NotNull protected final BuildProgressLogger logger;
    private Future<BuildFinishedStatus> myFuture;

    public FutureBasedBuildProcess(@NotNull final BuildRunnerContext context) {
        this.logger = context.getBuild().getBuildLogger();
    }

    public void start() throws RunBuildException
    {
        try {
            myFuture = Executors.newSingleThreadExecutor().submit(this);
        } catch (final RejectedExecutionException e) {
            logger.error("Failed to start build!");
            logger.exception(e);
            throw new RunBuildException(e);
        }
    }

    public boolean isInterrupted()
    {
        return myFuture.isCancelled() && isFinished();
    }

    public boolean isFinished()
    {
        return myFuture.isDone();
    }

    public void interrupt()
    {
        logger.message("Attempt to interrupt build process");
        myFuture.cancel(true);
    }

    @NotNull
    public BuildFinishedStatus waitFor() throws RunBuildException
    {
        try {
            final BuildFinishedStatus status = myFuture.get();
            return status;
        } catch (final InterruptedException e) {
            throw new RunBuildException(e);
        } catch (final ExecutionException e) {
            throw new RunBuildException(e);
        } catch (final CancellationException e) {
            logger.exception(e);
            return BuildFinishedStatus.INTERRUPTED;
        }
    }
}