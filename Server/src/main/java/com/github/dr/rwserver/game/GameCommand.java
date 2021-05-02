package com.github.dr.rwserver.game;

/**
 * @author Dr
 */
public class GameCommand {
    public final int sendBy;
    public final byte[] arr;

    public GameCommand(int sendBy,byte[] arr) {
        this.sendBy = sendBy;
        this.arr = arr;
    }
}
