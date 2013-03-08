/*
 * Copyright 2012 aVineas IT Consulting
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
package org.avineas.io.stat;

import java.util.Map;

/**
 * Interface that provides IO statistics.
 * 
 * @author Arie van Wijngaarden
 */
public interface Statistics {
	/**
	 * Get the number of reads performed by this channel.
	 * 
	 * @return The number of read actions. Null if this channel is not readable
	 */
	public Integer getReads();
	/**
	 * Get the number of writes performed by this channel.
	 * 
	 * @return The number of writes performed by this channel. Null if this channel
	 * is not writable
	 */
	public Integer getWrites();
	/**
	 * Get the number of errors that occurred to this channel.
	 * 
	 * @return The number of errors
	 */
	public int getErrors();
	/**
	 * Get the number of time-outs somewhere during read or write.
	 * 
	 * @return The timeouts count
	 */
	public int getTimeouts();
	/**
	 * Get extra attributes provided by the statistics.
	 * 
	 * @return Extra attributes, if any
	 */
	public Map<String, ?> getExtraStatistics();
}