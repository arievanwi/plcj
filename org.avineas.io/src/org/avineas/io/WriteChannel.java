/*
 * Copyright 2010 aVineas IT Consulting
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
package org.avineas.io;

import java.io.Closeable;

/**
 * Write channel, channel over which data can be written. Is the base interface for
 * writing data via a specific medium.
 * 
 * @author Arie van Wijngaarden
 */
public interface WriteChannel extends Closeable {
	/**
	 * Write a packet or number of bytes via the channel.
	 *  
	 * @param data The data to write
	 * @param length The length to write
	 * @return The number of bytes written, normally the value of the parameter 'length'. -1
	 * is returned if the channel is somehow closed or another error occurred
	 */
	public int write(byte[] data, int length);
}
