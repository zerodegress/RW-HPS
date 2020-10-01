package com.github.dr.rwserver.game;

/**
 * @author Dr
 */
public class GameCommand {
    public int sendBy;
    public byte[] arr;

    public GameCommand(int sendBy,byte[] arr) {
        this.sendBy = sendBy;
        this.arr = arr;
    }
}
