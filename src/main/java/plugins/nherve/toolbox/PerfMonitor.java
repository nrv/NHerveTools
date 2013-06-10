/*
 * Copyright 2010, 2011 Institut Pasteur.
 * Copyright 2012 Institut National de l'Audiovisuel.
 * 
 * This file is part of NHerveTools.
 * 
 * NHerveTools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NHerveTools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with NHerveTools. If not, see <http://www.gnu.org/licenses/>.
 */
package plugins.nherve.toolbox;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 
 * @author Nicolas HERVE - nherve@ina.fr
 */
public class PerfMonitor {
	private class CPUTime {
		private long startCPUTime;
		private long startTime;
		private long startUserTime;
		private long stopCPUTime;
		private long stopTime;
		private long stopUserTime;

		public CPUTime() {
			super();

			startUserTime = 0;
			startCPUTime = 0;
			stopUserTime = 0;
			stopCPUTime = 0;
			startTime = 0;
			stopTime = 0;
		}

		public long getCPUElapsedTimeNano() {
			return stopCPUTime - startCPUTime;
		}

		public long getElapsedTimeMilli() {
			return stopTime - startTime;
		}

		public long getStartUserTime() {
			return startUserTime;
		}

		public long getStopUserTime() {
			return stopUserTime;
		}

		public long getUserElapsedTimeNano() {
			return stopUserTime - startUserTime;
		}

		public void setStartCPUTime(long startCPUTime) {
			this.startCPUTime = startCPUTime;
			setStopCPUTime(startCPUTime);
		}

		public void setStartTime(long startTime) {
			this.startTime = startTime;
			setStopTime(startTime);
		}

		public void setStartUserTime(long startUserTime) {
			this.startUserTime = startUserTime;
			setStopUserTime(startUserTime);
		}

		public void setStopCPUTime(long stopCPUTime) {
			this.stopCPUTime = stopCPUTime;
		}

		public void setStopTime(long stopTime) {
			this.stopTime = stopTime;
		}

		public void setStopUserTime(long stopUserTime) {
			this.stopUserTime = stopUserTime;
		}
	}

	private class MemoryUsage {
		private long startHeap;
		private long startNonHeap;
		private long stopHeap;
		private long stopNonHeap;

		public MemoryUsage() {
			super();
			startHeap = 0;
			startNonHeap = 0;
			stopHeap = 0;
			stopNonHeap = 0;
		}

		public long getFullMemoryDiff() {
			return getHeapMemoryDiff() + getNonHeapMemoryDiff();
		}

		public long getHeapMemoryDiff() {
			return stopHeap - startHeap;
		}

		public long getNonHeapMemoryDiff() {
			return stopNonHeap - startNonHeap;
		}

		public void setStartHeap(long startHeap) {
			this.startHeap = startHeap;
		}
		
		public void setStartNonHeap(long startNonHeap) {
			this.startNonHeap = startNonHeap;
		}
		
		public void setStopHeap(long stopHeap) {
			this.stopHeap = stopHeap;
		}
		
		public void setStopNonHeap(long stopNonHeap) {
			this.stopNonHeap = stopNonHeap;
		}
	}

	private static final double MILLI_TO_SEC = 1d / 1000d;
	public final static int MONITOR_ALL_THREAD_FINELY = 2;
	public final static int MONITOR_ALL_THREAD_ROUGHLY = 1;

	public final static int MONITOR_CURRENT_THREAD = 0;
	private static final double NANO_TO_MILLI = 1d / 1000000d;
	private static final double NANO_TO_SEC = NANO_TO_MILLI * MILLI_TO_SEC;

	public static int getAvailableProcessors() {
		return Runtime.getRuntime().availableProcessors();
	}

	private ThreadMXBean bean;
	private MemoryUsage mem;
	private MemoryMXBean memBean;
	private boolean monitorMemory;
	private int monitorType;
	private OperatingSystemMXBean osBean;

	private Map<Long, CPUTime> threadTimes;
	private CPUTime time;

	public PerfMonitor() {
		this(MONITOR_CURRENT_THREAD);
	}

	public PerfMonitor(int type) {
		super();

		this.monitorType = type;

		time = new CPUTime();

		bean = ManagementFactory.getThreadMXBean();
		osBean = ManagementFactory.getOperatingSystemMXBean();

		setMonitorMemory(false);
	}

	public long getCPUElapsedTimeMilli() {
		return nanoToMilli(time.getCPUElapsedTimeNano());
	}

	public double getCPUElapsedTimeSec() {
		return nanoToSec(time.getCPUElapsedTimeNano());
	}

	public long getElapsedTimeMilli() {
		return time.getElapsedTimeMilli();
	}

	public double getElapsedTimeSec() {
		return milliToSec(time.getElapsedTimeMilli());
	}

	public long getFullMemoryDiff() {
		return mem.getFullMemoryDiff();
	}

	public long getHeapMemoryDiff() {
		return mem.getHeapMemoryDiff();
	}

	public long getNonHeapMemoryDiff() {
		return mem.getNonHeapMemoryDiff();
	}

	public int getThreadCount() {
		return bean.getThreadCount();
	}

	public long getUserElapsedTimeMilli() {
		return nanoToMilli(time.getUserElapsedTimeNano());
	}

	public double getUserElapsedTimeSec() {
		return nanoToSec(time.getUserElapsedTimeNano());
	}

	public boolean isMonitorMemory() {
		return monitorMemory;
	}

	private double milliToSec(long milli) {
		return milli * MILLI_TO_SEC;
	}

	private long nanoToMilli(long nano) {
		return Math.round(nano * NANO_TO_MILLI);
	}

	private double nanoToSec(long nano) {
		return nano * NANO_TO_SEC;
	}

	public void setMonitorMemory(boolean monitorMemory) {
		this.monitorMemory = monitorMemory;

		if (monitorMemory) {
			memBean = ManagementFactory.getMemoryMXBean();
			mem = new MemoryUsage();
		}
	}

	public void start() throws IllegalAccessError {
		if (!bean.isCurrentThreadCpuTimeSupported()) {
			throw new IllegalAccessError("This JVM does not support time benchmarking");
		}

		if (monitorMemory) {
			mem.setStartHeap(memBean.getHeapMemoryUsage().getUsed());
			mem.setStartNonHeap(memBean.getNonHeapMemoryUsage().getUsed());
		}
		
		switch (monitorType) {
		case MONITOR_CURRENT_THREAD:
			time.setStartUserTime(bean.getCurrentThreadUserTime());
			time.setStartCPUTime(bean.getCurrentThreadCpuTime());
			break;
		case MONITOR_ALL_THREAD_ROUGHLY:
			if (!(osBean instanceof com.sun.management.OperatingSystemMXBean)) {
				throw new IllegalAccessError("This JVM does not support this version of multiple threads time benchmarking");
			}

			time.setStartUserTime(((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuTime());
			time.setStartCPUTime(time.getStartUserTime());
			break;
		case MONITOR_ALL_THREAD_FINELY:
			threadTimes = new HashMap<Long, CPUTime>();
			time.setStartUserTime(0);
			time.setStartCPUTime(0);
			long[] tids = bean.getAllThreadIds();
			for (long id : tids) {
				CPUTime cput = new CPUTime();
				cput.setStartCPUTime(bean.getThreadCpuTime(id));
				cput.setStartUserTime(bean.getThreadUserTime(id));
				threadTimes.put(id, cput);
			}

			break;
		}

		time.setStartTime(System.currentTimeMillis());
	}

	
	
	public void stop() {
		switch (monitorType) {
		case MONITOR_CURRENT_THREAD:
			time.setStopUserTime(bean.getCurrentThreadUserTime());
			time.setStopCPUTime(bean.getCurrentThreadCpuTime());
			break;
		case MONITOR_ALL_THREAD_ROUGHLY:
			time.setStopUserTime(((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuTime());
			time.setStopCPUTime(time.getStopUserTime());
			break;
		case MONITOR_ALL_THREAD_FINELY:
			// Ignores threads that died during the monitoring
			long[] tids = bean.getAllThreadIds();
			long c = 0;
			long u = 0;
			for (long id : tids) {
				CPUTime cput = threadTimes.get(id);
				if (cput == null) {
					cput = new CPUTime();
				}
				cput.setStopCPUTime(bean.getThreadCpuTime(id));
				cput.setStopUserTime(bean.getThreadUserTime(id));

				c += cput.getCPUElapsedTimeNano();
				u += cput.getUserElapsedTimeNano();
			}
			time.setStopCPUTime(c);
			time.setStopUserTime(u);
			break;
		}
		time.setStopTime(System.currentTimeMillis());
		
		if (monitorMemory) {
			mem.setStopHeap(memBean.getHeapMemoryUsage().getUsed());
			mem.setStopNonHeap(memBean.getNonHeapMemoryUsage().getUsed());
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		DecimalFormat df = new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.FRANCE));
		sb.append("CPU " + df.format(getCPUElapsedTimeSec()) + " s / " + df.format(getElapsedTimeSec()) + " s");
		if (monitorMemory) {
			sb.append(" // MEM " + df.format(getFullMemoryDiff() / (1024d * 1024d)) + " Mo");
		}
		return sb.toString();
	}
}
