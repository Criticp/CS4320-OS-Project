import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;

public class OperatingSystemSchedulesProcesses {

	// Class representing a process.
	static class Process {
		int pid, arrival, burst, priority, waiting, turnaround, completion, remaining;
		
		Process(int pid, int arrival, int burst, int priority) {
			this.pid = pid;
			this.arrival = arrival;
			this.burst = burst;
			this.priority = priority;
			this.waiting = 0;
			this.turnaround = 0;
			this.completion = 0;
			this.remaining = burst;
		}
		
		// Copy constructor
		Process(Process p) {
			this.pid = p.pid;
			this.arrival = p.arrival;
			this.burst = p.burst;
			this.priority = p.priority;
			this.waiting = p.waiting;
			this.turnaround = p.turnaround;
			this.completion = p.completion;
			this.remaining = p.remaining;
		}
	}
	
	// Class representing a segment in the Gantt chart.
	static class GanttSegment {
		String label;
		int start, finish;
		
		GanttSegment(String label, int start, int finish) {
			this.label = label;
			this.start = start;
			this.finish = finish;
		}
	}
	
	// Class representing a memory hole.
	static class MemoryHole {
		int start, size;
		
		MemoryHole(int start, int size) {
			this.start = start;
			this.size = size;
		}
	}
	
	    // Reads processes from a file and returns a list of Process objects.
		static List<Process> readProcesses(String filename) {
			List<Process> processes = new ArrayList<>();
			Path filePath = Paths.get(filename);
			System.out.println("Reading file from: " + filePath.toAbsolutePath()); // Print the absolute path for debugging
			try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
				// Skip header line
				String line = br.readLine();
				while ((line = br.readLine()) != null) {
					if (line.trim().isEmpty())
						continue;
					String[] parts = line.trim().split("\\s+");
					if (parts.length >= 4) {
						int pid = Integer.parseInt(parts[0]);
						int arrival = Integer.parseInt(parts[1]);
						int burst = Integer.parseInt(parts[2]);
						int priority = Integer.parseInt(parts[3]);
						processes.add(new Process(pid, arrival, burst, priority));
					}
				}
			} catch (IOException e) {
				System.out.println("Error: File " + filename + " not found.");
			}
			return processes;
		}
	


	// Prints a Gantt chart with each segment's start time aligned under its vertical bar.
	static void printGanttChart(List<GanttSegment> gantt) {
		if (gantt.isEmpty()) {
			System.out.println("\nNo Gantt chart to display.");
			return;
		}
		
		int blockWidth = 6; // Adjust width as needed
		StringBuilder topLine = new StringBuilder();
		StringBuilder bottomLine = new StringBuilder();
		
		// Build the top line: each segment printed with a leading "|" and label left-aligned.
		for (GanttSegment seg : gantt) {
			topLine.append("|").append(String.format("%-" + blockWidth + "s", seg.label));
		}
		// Build the bottom line: each segment's start time below its bar.
		for (GanttSegment seg : gantt) {
			bottomLine.append("|").append(String.format("%-" + blockWidth + "s", seg.start));
		}
		// Append the final finish time with label "Finish:" (without a preceding bar)
		bottomLine.append("Finish:").append(gantt.get(gantt.size() - 1).finish);
		
		System.out.println("\nGantt Chart:");
		System.out.println(topLine.toString());
		System.out.println(bottomLine.toString());
	}
	


	// FCFS Scheduling
	static void fcfsScheduling(List<Process> processes) {
		List<Process> procs = new ArrayList<>();
		for (Process p : processes) {
			procs.add(new Process(p));
		}
		if (procs.isEmpty()) {
			System.out.println("\n--- FCFS Scheduling ---");
			System.out.println("No processes to schedule.");
			return;
		}
		int time = 0;
		List<GanttSegment> gantt = new ArrayList<>();
		System.out.println("\n--- FCFS Scheduling ---");
		for (Process p : procs) {
			if (time < p.arrival) {
				gantt.add(new GanttSegment("Idle", time, p.arrival));
				time = p.arrival;
			}
			int start = time;
			p.waiting = time - p.arrival;
			time += p.burst;
			p.completion = time;
			p.turnaround = p.completion - p.arrival;
			gantt.add(new GanttSegment("P" + p.pid, start, time));
			System.out.printf("PID: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n", p.pid, p.waiting, p.turnaround);
		}
		double avgWait = procs.stream().mapToInt(p -> p.waiting).average().orElse(0);
		double avgTurnaround = procs.stream().mapToInt(p -> p.turnaround).average().orElse(0);
		System.out.printf("Average Waiting Time: %.2f\n", avgWait);
		System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);
		printGanttChart(gantt);
	}
	


	// SJF Scheduling (Non-Preemptive)
	static void sjfScheduling(List<Process> processes) {
		List<Process> procs = new ArrayList<>();
		for (Process p : processes) {
			procs.add(new Process(p));
		}
		if (procs.isEmpty()) {
			System.out.println("\n--- SJF Scheduling (Non-Preemptive) ---");
			System.out.println("No processes to schedule.");
			return;
		}
		int time = 0;
		List<GanttSegment> gantt = new ArrayList<>();
		List<Process> finished = new ArrayList<>();
		System.out.println("\n--- SJF Scheduling (Non-Preemptive) ---");
		while (!procs.isEmpty()) {
			List<Process> available = new ArrayList<>();
			for (Process p : procs) {
				if (p.arrival <= time)
					available.add(p);
			}
			if (available.isEmpty()) {
				int nextArrival = procs.stream().mapToInt(p -> p.arrival).min().orElse(time);
				gantt.add(new GanttSegment("Idle", time, nextArrival));
				time = nextArrival;
				continue;
			}
			// Choose process with smallest burst
			Process current = available.get(0);
			for (Process p : available) {
				if (p.burst < current.burst)
					current = p;
			}
			procs.remove(current);
			if (time < current.arrival) {
				gantt.add(new GanttSegment("Idle", time, current.arrival));
				time = current.arrival;
			}
			int start = time;
			current.waiting = time - current.arrival;
			time += current.burst;
			current.completion = time;
			current.turnaround = current.completion - current.arrival;
			gantt.add(new GanttSegment("P" + current.pid, start, time));
			finished.add(current);
			System.out.printf("PID: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n", current.pid, current.waiting, current.turnaround);
		}
		double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);
		double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);
		System.out.printf("Average Waiting Time: %.2f\n", avgWait);
		System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);
		printGanttChart(gantt);
	}
	


	// Round Robin Scheduling
	static void roundRobinScheduling(List<Process> processes, int timeQuantum) {
		List<Process> procs = new ArrayList<>();
		for (Process p : processes) {
			procs.add(new Process(p));
		}
		if (procs.isEmpty()) {
			System.out.println("\n--- Round Robin Scheduling (Time Quantum = " + timeQuantum + ") ---");
			System.out.println("No processes to schedule.");
			return;
		}
		// Set remaining equal to burst (already set in constructor, but reset if needed)
		for (Process p : procs) {
			p.remaining = p.burst;
		}
		int time = 0;
		List<GanttSegment> gantt = new ArrayList<>();
		List<Process> finished = new ArrayList<>();
		List<Process> notAdded = new ArrayList<>(procs);
		notAdded.sort(Comparator.comparingInt(p -> p.arrival));
		List<Process> queue = new ArrayList<>();
		System.out.println("\n--- Round Robin Scheduling (Time Quantum = " + timeQuantum + ") ---");
		while (!queue.isEmpty() || !notAdded.isEmpty()) {
			if (queue.isEmpty()) {
				Process nextProc = notAdded.get(0);
				if (time < nextProc.arrival) {
					gantt.add(new GanttSegment("Idle", time, nextProc.arrival));
					time = nextProc.arrival;
				}
				while (!notAdded.isEmpty() && notAdded.get(0).arrival <= time) {
					queue.add(notAdded.remove(0));
				}
			}
			Process current = queue.remove(0);
			int start = time;
			int execTime = Math.min(timeQuantum, current.remaining);
			time += execTime;
			current.remaining -= execTime;
			gantt.add(new GanttSegment("P" + current.pid, start, time));
			while (!notAdded.isEmpty() && notAdded.get(0).arrival <= time) {
				queue.add(notAdded.remove(0));
			}
			if (current.remaining > 0) {
				queue.add(current);
			} else {
				current.completion = time;
				current.turnaround = current.completion - current.arrival;
				current.waiting = current.turnaround - current.burst;
				finished.add(current);
			}
		}
		if (!finished.isEmpty()) {
			finished.sort(Comparator.comparingInt(p -> p.pid));
			for (Process p : finished) {
				System.out.printf("PID: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n", p.pid, p.waiting, p.turnaround);
			}
			double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);
			double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);
			System.out.printf("Average Waiting Time: %.2f\n", avgWait);
			System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);
		}
		printGanttChart(gantt);
	}
	


	// Priority Scheduling (Non-Preemptive)
	static void priorityScheduling(List<Process> processes) {
		List<Process> procs = new ArrayList<>();
		for (Process p : processes) {
			procs.add(new Process(p));
		}
		if (procs.isEmpty()) {
			System.out.println("\n--- Priority Scheduling (Non-Preemptive) ---");
			System.out.println("No processes to schedule.");
			return;
		}
		int time = 0;
		List<GanttSegment> gantt = new ArrayList<>();
		List<Process> finished = new ArrayList<>();
		System.out.println("\n--- Priority Scheduling (Non-Preemptive) ---");
		while (!procs.isEmpty()) {
			List<Process> available = new ArrayList<>();
			for (Process p : procs) {
				if (p.arrival <= time)
					available.add(p);
			}
			if (available.isEmpty()) {
				int nextArrival = procs.stream().mapToInt(p -> p.arrival).min().orElse(time);
				gantt.add(new GanttSegment("Idle", time, nextArrival));
				time = nextArrival;
				available.clear();
				for (Process p : procs) {
					if (p.arrival <= time)
						available.add(p);
				}
			}
			// Choose process with highest priority (lowest number)
			Process current = available.get(0);
			for (Process p : available) {
				if (p.priority < current.priority)
					current = p;
			}
			procs.remove(current);
			if (time < current.arrival) {
				gantt.add(new GanttSegment("Idle", time, current.arrival));
				time = current.arrival;
			}
			int start = time;
			current.waiting = time - current.arrival;
			time += current.burst;
			current.completion = time;
			current.turnaround = current.completion - current.arrival;
			gantt.add(new GanttSegment("P" + current.pid, start, time));
			finished.add(current);
			System.out.printf("PID: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d | Priority: %d\n", current.pid, current.waiting, current.turnaround, current.priority);
		}
		if (!finished.isEmpty()) {
			double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);
			double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);
			System.out.printf("Average Waiting Time: %.2f\n", avgWait);
			System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);
		}
		printGanttChart(gantt);
	}
	


	// ---------------- Memory Management Simulation ----------------
	
	// First-Fit allocation
	static MemoryHole firstFitAllocation(List<MemoryHole> holes, int request) {
		for (int i = 0; i < holes.size(); i++) {
			MemoryHole hole = holes.get(i);
			if (hole.size >= request) {
				MemoryHole allocated = new MemoryHole(hole.start, request);
				int newStart = hole.start + request;
				int newSize = hole.size - request;
				if (newSize > 0) {
					holes.set(i, new MemoryHole(newStart, newSize));
				} else {
					holes.remove(i);
				}
				return allocated;
			}
		}
		return null;
	}
	


	// Best-Fit allocation
	static MemoryHole bestFitAllocation(List<MemoryHole> holes, int request) {
		int bestIndex = -1;
		int bestSize = Integer.MAX_VALUE;
		for (int i = 0; i < holes.size(); i++) {
			MemoryHole hole = holes.get(i);
			if (hole.size >= request && hole.size < bestSize) {
				bestSize = hole.size;
				bestIndex = i;
			}
		}
		if (bestIndex != -1) {
			MemoryHole hole = holes.get(bestIndex);
			MemoryHole allocated = new MemoryHole(hole.start, request);
			int newStart = hole.start + request;
			int newSize = hole.size - request;
			if (newSize > 0) {
				holes.set(bestIndex, new MemoryHole(newStart, newSize));
			} else {
				holes.remove(bestIndex);
			}
			return allocated;
		}
		return null;
	}
	


	// Worst-Fit allocation
	static MemoryHole worstFitAllocation(List<MemoryHole> holes, int request) {
		int worstIndex = -1;
		int worstSize = -1;
		for (int i = 0; i < holes.size(); i++) {
			MemoryHole hole = holes.get(i);
			if (hole.size >= request && hole.size > worstSize) {
				worstSize = hole.size;
				worstIndex = i;
			}
		}
		if (worstIndex != -1) {
			MemoryHole hole = holes.get(worstIndex);
			MemoryHole allocated = new MemoryHole(hole.start, request);
			int newStart = hole.start + request;
			int newSize = hole.size - request;
			if (newSize > 0) {
				holes.set(worstIndex, new MemoryHole(newStart, newSize));
			} else {
				holes.remove(worstIndex);
			}
			return allocated;
		}
		return null;
	}
	


	static void simulateMemoryAllocation(String strategy) {
		List<MemoryHole> holes = new ArrayList<>();
		holes.add(new MemoryHole(0, 1000)); // Initial memory: one big hole
		// Sample allocation requests: {processId, memorySize}
		int[][] requests = {
			{1, 100},
			{2, 300},
			{3, 50},
			{4, 200},
			{5, 150},
			{6, 100},
			{7, 80}
		};
		Map<Integer, MemoryHole> allocations = new HashMap<>();
		System.out.println("\nMemory Allocation Simulation using " + strategy.toUpperCase() + ":");
		for (int[] req : requests) {
			int pid = req[0];
			int size = req[1];
			MemoryHole allocated = null;
			if (strategy.equals("first_fit")) {
				allocated = firstFitAllocation(holes, size);
			} else if (strategy.equals("best_fit")) {
				allocated = bestFitAllocation(holes, size);
			} else if (strategy.equals("worst_fit")) {
				allocated = worstFitAllocation(holes, size);
			} else {
				System.out.println("Unknown strategy");
				return;
			}
			if (allocated != null) {
				allocations.put(pid, allocated);
				System.out.println("Process " + pid + " allocated at address " + allocated.start + " with size " + allocated.size);
			} else {
				System.out.println("Process " + pid + " allocation of size " + size + " failed.");
			}
		}
		System.out.println("Remaining free holes:");
		for (MemoryHole hole : holes) {
			System.out.println("Start: " + hole.start + ", Size: " + hole.size);
		}
	}
	


	// Paging Simulation: FIFO
	static int simulatePagingFIFO(int[] pageReferences, int numFrames) {
		List<Integer> frames = new ArrayList<>();
		int pageFaults = 0;
		for (int page : pageReferences) {
			if (!frames.contains(page)) {
				pageFaults++;
				if (frames.size() < numFrames) {
					frames.add(page);
				} else {
					frames.remove(0); // Remove oldest page
					frames.add(page);
				}
			}
		}
		return pageFaults;
	}
	


	// Paging Simulation: LRU
	static int simulatePagingLRU(int[] pageReferences, int numFrames) {
		List<Integer> frames = new ArrayList<>();
		int pageFaults = 0;
		for (int page : pageReferences) {
			if (!frames.contains(page)) {
				pageFaults++;
				if (frames.size() < numFrames) {
					frames.add(page);
				} else {
					frames.remove(0); // Remove least recently used (oldest in our simple list)
					frames.add(page);
				}
			} else {
				// Update recency: remove page and add it to the end
				frames.remove(Integer.valueOf(page));
				frames.add(page);
			}
		}
		return pageFaults;
	}
	


    public static void main(String[] args) {
        // Scheduling Simulation
        Path currentRelativePath = Paths.get("");
        String filename = currentRelativePath.toAbsolutePath().toString() + "\\processes.txt"; // Use relative path
        List<Process> processes = readProcesses(filename);
        if (processes.isEmpty()) {
            System.out.println("No processes to schedule. Please check your processes.txt file.");
        } else {
            // Sort by arrival time
            processes.sort(Comparator.comparingInt(p -> p.arrival));
            fcfsScheduling(processes);
            System.out.println("\n\n");
            sjfScheduling(processes);
            System.out.println("\n\n");
            roundRobinScheduling(processes, 4);
            System.out.println("\n\n");
            priorityScheduling(processes);
        }

        // Memory Allocation Simulation
        System.out.println("\n\n");
        simulateMemoryAllocation("first_fit");
        simulateMemoryAllocation("best_fit");
        simulateMemoryAllocation("worst_fit");

        // Paging Simulation
        int[] pageRefs = {7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2};
        int numFrames = 3;
        int fifoFaults = simulatePagingFIFO(pageRefs, numFrames);
        int lruFaults = simulatePagingLRU(pageRefs, numFrames);
        System.out.println("\nPaging Simulation:");
        System.out.println("FIFO Page Faults: " + fifoFaults);
        System.out.println("LRU Page Faults: " + lruFaults);
    }
}