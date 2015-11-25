/*
 * Copyright 2015 (c) Secure System Group (https://se-sy.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uab.cis.spies.audiorecorderdemo;

/**
 * @author Swapnil Udar
 */
public abstract class TwoFactorThread extends Thread {

    private boolean sTimeToDie = false;

    public TwoFactorThread(ThreadGroup tGroup, String name) {
        super(tGroup, name);
        setDaemon(true);
    }

    /**
     * In some cases, you can use application specific tricks. For example, if a
     * thread is waiting on a known socket, you can close the socket to cause
     * the thread to return immediately. Unfortunately, there really isn't any
     * technique that works in general. It should be noted that in all
     * situations where a waiting thread doesn't respond to Thread.interrupt, it
     * wouldn't respond to Thread.stop either. Such cases include deliberate
     * denial-of-service attacks, and I/O operations for which thread.stop and
     * thread.interrupt do not work properly.
     */
    @Override
    public void interrupt() {
        super.interrupt();
        this.sTimeToDie = true;
    }

    @Override
    public boolean isInterrupted() {
        return this.sTimeToDie || super.isInterrupted();
    }

    @Override
    public void run() {
        try {
            mainloop();
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
    }

    protected abstract void mainloop() throws InterruptedException;

    protected void takeRest(long time) throws InterruptedException {
        Thread.sleep(time);
    }
}