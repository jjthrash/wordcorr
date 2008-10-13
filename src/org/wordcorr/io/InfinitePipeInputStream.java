/*
 * InfinitePipeInputStream.java
 *
 * Created on November 17, 2005, 11:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wordcorr.io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
/**
 *
 * @author Nathan
 *
 * an object of this class must NOT be accessed from two separate threads.  Data corruption will result.
 * Use the JDK standard PipedInputStream for communication between threads.
 * This class is designed as a replacement for the standard PipedInputStream for use within a single thread.
 */
public class InfinitePipeInputStream extends java.io.InputStream {
    private LinkedList<byte[]> data = new LinkedList<byte[]>();
    private int BUFFER_SIZE = 400;
    private byte[] read_buffer = null;
    private byte[] write_buffer = new byte[BUFFER_SIZE];
    private int read = BUFFER_SIZE; //represents the currently readable byte;
    private int write = 0;//represents the currently writable byte
    private boolean endReceived = false;

    /*
     * will never block.  Instead will throw an IOException if input is unavailable.
     */
    public int read() throws IOException {
        if (data == null) {
            throw (new IOException("Stream closed"));
        }
        
        if (read == BUFFER_SIZE) {
            if (data.isEmpty()) {
                if (endReceived) {
                    return -1;
                }
                throw (new IOException("No data in buffer, and End Of Stream not reached"));
            }
            read_buffer = data.removeFirst();
            read = 0;
        }
        
        if (read_buffer == write_buffer && read == write) {
            throw (new IOException("No data in buffer, and End Of Stream not reached"));
        }
        
        if (data.isEmpty() && write_buffer == null && read == write) {
            return -1;
        }

        return read_buffer[read++];
    }
    
    public void close() {
        data = null;
    }
    
    protected void receive(int b) throws IOException {
        if (data == null) {
            throw (new IOException("Stream closed"));
        }
        if (write == BUFFER_SIZE) {
            data.add(write_buffer);
            write_buffer = new byte[BUFFER_SIZE];
            write = 0;
        }
        write_buffer[write++] = (byte) b;
    }
    
    //no more bytes will be received.
    void outStreamClosed() {
        endReceived = true;
        data.add(write_buffer);
        write_buffer = null;
    }
}
