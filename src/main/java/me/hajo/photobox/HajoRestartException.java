package me.hajo.photobox;

import java.io.IOException;

/**
 * Created by fxtentacle on 29.04.16.
 */
public class HajoRestartException extends RuntimeException {
    public HajoRestartException(Exception e) {
        super(e);
        e.printStackTrace();
    }
}
