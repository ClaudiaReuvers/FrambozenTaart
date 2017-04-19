package com.nedap.university;

import java.net.DatagramPacket;

/**
 * Created by claudia.reuvers on 19/04/2017.
 */
public interface TimeOutEventHandler {

    void TimeoutElapsed(long tag);

}
