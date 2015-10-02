package com.example.kev94.ttsbrowserjs;

/**
 * Created by Kev94 on 26.08.2015.
 */

//Interface that describes the basic behaviour of a gesture class; there are only a few methods,
// because the listener does all the work
public interface iGestures {

    void registerListener();
    void unregisterListener();

    int gestureCompleted();
}
