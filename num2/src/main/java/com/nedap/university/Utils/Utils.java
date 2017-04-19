package com.nedap.university.Utils;

import com.nedap.university.ExtraHeader;
import com.nedap.university.TimeOutEventHandler;

import java.io.*;
import java.net.DatagramPacket;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Utils {
    private Utils() {
    }

    public static byte[] joinByteArrays(byte[] array1, byte[] array2) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(array1);
            outputStream.write(array2);
        } catch (IOException e){
            System.out.println("Could not write this!");
        }
        return outputStream.toByteArray( );

    }

    /**
     * Returns the data of the complete packet (header and data).
     * @param dataAndHeader a byte-array of the header and data
     * @return the data of the complete packet
     */
    public static byte[] getDataOfPacket(byte[] dataAndHeader) {
        ExtraHeader header = ExtraHeader.returnHeader(dataAndHeader);
        int from = ExtraHeader.headerLength();
        int to = from + header.getLengthData();
        return Arrays.copyOfRange(dataAndHeader, from, to);//dataAndHeader.length);
    }

    /**
     * Helper class for setting timeouts. Supplied for convenience.
     *
     * @author Jaco ter Braak & Frans van Dijk, Twente University
     * @version 09-02-2016
     */
    public static class Timeout implements Runnable {
        private static Map<Date, Map<TimeOutEventHandler, List<DatagramPacket>>> eventHandlers = new HashMap<>();
        private static Map<Object, Date> packets = new HashMap<>();
        private static Thread eventTriggerThread;
        private static boolean started = false;
        private static ReentrantLock lock = new ReentrantLock();

        /**
         * Starts the helper thread
         */
        public static void Start() {
            if (started)
                throw new IllegalStateException("Already started");
            started = true;
            eventTriggerThread = new Thread(new Timeout());
            eventTriggerThread.start();
        }

        /**
         * Stops the helper thread
         */
        public static void Stop() {
            if (!started)
                throw new IllegalStateException(
                        "Not started or already stopped");
            eventTriggerThread.interrupt();
            try {
                eventTriggerThread.join();
            } catch (InterruptedException e) {
            }
        }

        public static void stopTimeOut(Object tag) {
            if (tag != null) {
                eventHandlers.remove(packets.get(tag));
                packets.remove(tag);
                System.out.println("Remove timeout");
            }
        }

        /**
         * Set a timeout
         *
         * @param millisecondsTimeout
         *            the timeout interval, starting now
         * @param handler
         *            the event handler that is called once the timeout elapses
         */
        public static void SetTimeout(long millisecondsTimeout,
                                      TimeOutEventHandler handler, DatagramPacket tag) {
            Date elapsedMoment = new Date();
            elapsedMoment
                    .setTime(elapsedMoment.getTime() + millisecondsTimeout);
            if (packets.containsKey(tag)) {
                stopTimeOut(tag);
            }
            packets.put(tag, elapsedMoment);

            lock.lock();
            if (!eventHandlers.containsKey(elapsedMoment)) {
                eventHandlers.put(elapsedMoment,
                        new HashMap<>());
            }
            if (!eventHandlers.get(elapsedMoment).containsKey(handler)) {
                eventHandlers.get(elapsedMoment).put(handler,
                        new ArrayList<>());
            }
            eventHandlers.get(elapsedMoment).get(handler).add(tag);
            lock.unlock();
        }

        /**
         * Do not call this
         */
        @Override
        public void run() {
            boolean runThread = true;
            ArrayList<Date> datesToRemove = new ArrayList<>();
            HashMap<TimeOutEventHandler, List<DatagramPacket>> handlersToInvoke = new HashMap<>();
            Date now;

            while (runThread) {
                try {
                    now = new Date();

                    // If any timeouts have elapsed, trigger their handlers
                    lock.lock();

                    for (Date date : eventHandlers.keySet()) {
                        if (date.before(now)) {
                            datesToRemove.add(date);
                            for (TimeOutEventHandler handler : eventHandlers.get(date).keySet()) {
                                if (!handlersToInvoke.containsKey(handler)) {
                                    handlersToInvoke.put(handler,
                                            new ArrayList<>());
                                }
                                for (DatagramPacket tag : eventHandlers.get(date).get(
                                        handler)) {
                                    handlersToInvoke.get(handler).add(tag);
                                }
                            }
                        }
                    }

                    // Remove elapsed events
                    for (Date date : datesToRemove) {
                        eventHandlers.remove(date);
                    }
                    datesToRemove.clear();

                    lock.unlock();

                    // Invoke the event handlers outside of the lock, to prevent
                    // deadlocks
                    for (TimeOutEventHandler handler : handlersToInvoke
                            .keySet()) {
                        handlersToInvoke.get(handler).forEach(handler::TimeoutElapsed);
                    }
                    handlersToInvoke.clear();

                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    runThread = false;
                }
            }

        }
    }
}
