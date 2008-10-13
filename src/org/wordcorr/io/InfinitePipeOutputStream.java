/*
 * InfinitePipeOutputStream.java
 *
 * Created on November 17, 2005, 11:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wordcorr.io;

import java.io.IOException;

/**
 *
 * @author Nathan
 * an object of this class must NOT be accessed from two separate threads.  Data corruption will result.
 * Use the JDK standard PipedOutputStream for communication between threads.
 * This class is designed as a replacement for the standard PipedOutputStream for use within a single thread.
 */
public class InfinitePipeOutputStream extends java.io.OutputStream {
    InfinitePipeInputStream instream = null;
    
    /** Creates a new instance of InfinitePipeOutputStream */
    public InfinitePipeOutputStream(InfinitePipeInputStream instream) {
        this.instream = instream;
    }

    public void write(int b) throws IOException {
        if (instream == null) {
            throw(new IOException("Stream closed"));
        }
        instream.receive(b);
    }
    
    public void close() throws IOException {
        instream.outStreamClosed();
        instream = null;
    }
}
