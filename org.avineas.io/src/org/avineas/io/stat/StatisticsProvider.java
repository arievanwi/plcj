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

/**
 * Interface that provides IO statistics.
 * 
 * @author Arie van Wijngaarden
 */
public interface StatisticsProvider {
	/**
	 * Get the statistics for this provider.
	 * 
	 * @return The statistics
	 */
	public Statistics getStatistics();
	/**
	 * Reset the statistics counters.
	 */
	public void resetCounters();
}