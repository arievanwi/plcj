/*
 * Copyright 2005, the original author or authors.
 * Copyright 2010-2011, aVineas IT Consulting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.avineas.fins.payload;

/**
 * Payload that represents a response, meaning the reaction to a command. A response
 * contains next to the standard payload/command request code a response code
 * which should default to 0 to indicate success.
 * 
 * @author Arie van Wijngaarden
 * @since 6-11-2005
 */
public class Response extends Payload {
    /**
     * OK response, meaning that the command was processes successfully.
     */
    public static final int RESPONSEOK = 0;
    private static int RESPONSECODEOFFSET = 2;
    private static int RHEADERSIZE = Payload.HEADERSIZE + 2;
    
    public Response(byte[] payload) {
        super(payload);
    }
    
    /**
     * Initialize a response packet.
     * 
     * @param mrc The main request code
     * @param src The source request code
     * @param responseCode The response code
     * @param contents The contents
     */
    protected void init(int mrc, int src, int responseCode, byte[] contents) {
        setRequestCode(mrc, src);
        setResponseCode(responseCode);
        System.arraycopy(contents, 0, data, RHEADERSIZE, contents.length);
        dataSize = RHEADERSIZE + contents.length;
    }
    
    /**
     * Constructor that creates a response from a command with a specific
     * response code and data.
     * 
     * @param command The command to create the response for
     * @param responseCode The response code
     * @param contents The contents to pass
     */
    public Response(Command command, int responseCode, byte[] contents) {
        setRequestCode(command.getMrc(), command.getSrc());
        setResponseCode(responseCode);
        setContents(contents);
    }
    
    /**
     * Convience constructor that creates a simple response
     * 
     * @param command The command to which this response is a reaction
     * @param responseCode The response code, 0 is success
     */
    public Response(Command command, int responseCode) {
        this(command, responseCode, new byte[0]);
    }
 
    /**
     * Get the response code from this response.
     * 
     * @return The response code, 0 means success
     */
    public int getResponseCode() {
        return decode(data, RESPONSECODEOFFSET, 2);
    }
    
    public void setResponseCode(int code) {
        encode(code, data, RESPONSECODEOFFSET, 2);
    }

    /**
     * Get the main response code. A response code consists of two
     * parts, this method returns the main code.
     */
    public int getMainCode() {
        return data[RESPONSECODEOFFSET] & 0xff;
    }

    /**
     * Get the sub response code. Sub-identification of the response
     * code as received.
     */
    public int getSubCode() {
        return data[RESPONSECODEOFFSET + 1] & 0xff;
    }
    
    /**
     * Get the string representation of this response code.
     * 
     * @return A string representing the response code
     */
    public String getResponseCodeAsString() {
        return "0x" + Integer.toHexString(getMainCode()) + " 0x" +
            Integer.toHexString(getSubCode());
    }

    /**
     * Set the contents of the response, meaning the additional data
     * without response code.
     * 
     * @param contents The contents as byte array
     */
    public void setContents(byte[] contents) {
        System.arraycopy(contents, 0, data, RHEADERSIZE, contents.length);
        dataSize = RHEADERSIZE + contents.length;
    }
 
    /**
     * Get the contents of this response (meaning: the data after the
     * response code).
     * 
     * @return A byte array with the contents. May be a 0 size array
     */
    public byte[] getContents() {
        byte[] contents = new byte[dataSize - RHEADERSIZE];
        System.arraycopy(data, RHEADERSIZE, contents, 0, contents.length);
        return contents;
    }
}