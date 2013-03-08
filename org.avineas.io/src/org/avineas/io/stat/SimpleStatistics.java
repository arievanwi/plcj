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

import java.util.HashMap;
import java.util.Map;

/**
 * Simple implementation of statistics.
 * 
 * @author Arie van Wijngaarden
 */
public class SimpleStatistics implements Statistics {
	private Integer reads;
	private Integer writes;
	private int errors;
	private int timeouts;
	private Map<String, Object> extras = new HashMap<String, Object>();
	
	public SimpleStatistics(Integer reads, Integer writes) {
		this.reads = reads;
		this.writes = writes;
	}
	
	public void reset() {
		errors = 0;
		if (this.reads != null) {
			this.reads = 0;
		}
		if (this.writes != null) {
			this.writes = 0;
		}
		timeouts = 0;
	}
	
	@Override
	public int getErrors() {
		return errors;
	}

	@Override
	public Integer getReads() {
		return reads;
	}

	@Override
	public Integer getWrites() {
		return writes;
	}

	@Override
	public int getTimeouts() {
		return timeouts;
	}
	
	@Override
	public Map<String, Object> getExtraStatistics() {
		return extras;
	}

	public void setExtra(String key, Object value) {
		extras.put(key, value);
	}
	
	public void error() {
		errors++;
	}
	
	public void written() {
		writes++;
	}
	
	public void read() {
		reads++;
	}
	
	public void timeout() {
		timeouts++;
	}
}