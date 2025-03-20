import java.io.BufferedReader;//This import is for efficient reading of text from an input stream.
import java.io.FileReader;//This import is for reading files.
import java.io.IOException;//This import handles input/output exceptions.
import java.nio.file.Paths;//This import provides methods to construct file paths.
import java.nio.file.Path;//This import represents file system paths.
import java.util.*;//This import includes utility classes like ArrayList, Comparator, etc.

public class OperatingSystemSchedulesProcesses {//This defines the main class for simulating OS process scheduling and memory allocation.

    static class Process {//This class represents an individual process.
        int pid, arrival, burst, priority, waiting, turnaround, completion, remaining, cpuInit;//These variables store process ID, arrival time, burst time, priority, waiting time, turnaround time, completion time, remaining burst time, and CPU initialization time.
        int memoryRequirement;//This variable represents the memory requirement for the process.

        Process(int pid, int arrival, int burst, int priority, int memoryRequirement) {//This constructor initializes a process with all given parameters.
            this.pid = pid;//Assign the process ID.
            this.arrival = arrival;//Assign the arrival time.
            this.burst = burst;//Assign the CPU burst time.
            this.priority = priority;//Assign the process priority.
            this.memoryRequirement = memoryRequirement;//Assign the memory requirement.
            this.waiting = 0;//Initialize waiting time to 0.
            this.turnaround = 0;//Initialize turnaround time to 0.
            this.completion = 0;//Initialize completion time to 0.
            this.remaining = burst;//Set remaining burst time equal to burst time.
            this.cpuInit = -1;//Initialize CPU initialization time to -1 (unset).
        }

        Process(int pid, int arrival, int burst, int priority) {//This constructor initializes a process with a default memory requirement.
            this(pid, arrival, burst, priority, 100);//Call the main constructor with a default memory requirement of 100.
        }

        Process(Process p) {//This copy constructor creates a new process from an existing process.
            this.pid = p.pid;//Copy the process ID.
            this.arrival = p.arrival;//Copy the arrival time.
            this.burst = p.burst;//Copy the burst time.
            this.priority = p.priority;//Copy the process priority.
            this.waiting = p.waiting;//Copy the waiting time.
            this.turnaround = p.turnaround;//Copy the turnaround time.
            this.completion = p.completion;//Copy the completion time.
            this.remaining = p.remaining;//Copy the remaining burst time.
            this.cpuInit = p.cpuInit;//Copy the CPU initialization time.
            this.memoryRequirement = p.memoryRequirement;//Copy the memory requirement.
        }
    }

    static class GanttSegment {//This class represents a segment of the Gantt chart.
        String label;//This variable stores the label for the segment (e.g., "P1" or "Idle").
        int start, finish;//These variables store the start and finish times of the segment.

        GanttSegment(String label, int start, int finish) {//This constructor initializes a Gantt segment.
            this.label = label;//Assign the segment label.
            this.start = start;//Assign the start time.
            this.finish = finish;//Assign the finish time.
        }
    }

    static class MemoryHole {//This class represents a free memory block (a memory hole).
        int start, size;//These variables represent the starting address and size of the memory hole.

        MemoryHole(int start, int size) {//This constructor initializes a memory hole.
            this.start = start;//Assign the starting address.
            this.size = size;//Assign the size of the memory hole.
        }
    }

    static List<Process> readProcesses(String filename) {//This method reads process data from a file and returns a list of Process objects.
        List<Process> processes = new ArrayList<>();//Create a list to store processes.
        Path filePath = Paths.get(filename);//Construct a Path object for the given filename.
        System.out.println("Reading file from: " + filePath.toAbsolutePath());//Print the absolute file path.
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {//Open the file using BufferedReader.
            String line = br.readLine();//Read the header line and ignore it.
            while ((line = br.readLine()) != null) {//Loop through each subsequent line.
                if (line.trim().isEmpty())//If the line is empty after trimming whitespace,
                    continue;//skip this line.
                String[] parts = line.trim().split("\\s+");//Split the line by one or more whitespace characters.
                if (parts.length >= 4) {//If there are at least 4 parts (PID, Arrival, Burst, Priority),
                    int pid = Integer.parseInt(parts[0]);//Parse the process ID.
                    int arrival = Integer.parseInt(parts[1]);//Parse the arrival time.
                    int burst = Integer.parseInt(parts[2]);//Parse the burst time.
                    int priority = Integer.parseInt(parts[3]);//Parse the process priority.
                    int memoryReq = 100;//Set a default memory requirement.
                    if (parts.length >= 5) {//If a fifth column is present,
                        memoryReq = Integer.parseInt(parts[4]);//Parse the memory requirement.
                    }
                    processes.add(new Process(pid, arrival, burst, priority, memoryReq));//Create a new Process and add it to the list.
                }
            }
        } catch (IOException e) {//Catch any I/O exceptions.
            System.out.println("Error: File " + filename + " not found.");//Print an error message if the file cannot be read.
        }
        return processes;//Return the list of processes.
    }

    static void printGanttChart(List<GanttSegment> gantt) {//This method prints a text-based Gantt chart.
        if (gantt.isEmpty()) {//If the Gantt chart is empty,
            System.out.println("\nNo Gantt chart to display.");//inform the user.
            return;//Exit the method.
        }
        int blockWidth = 6;//Define the width of each block in the chart.
        StringBuilder topLine = new StringBuilder();//Create a StringBuilder for the top line (labels).
        StringBuilder bottomLine = new StringBuilder();//Create a StringBuilder for the bottom line (time markers).
        for (GanttSegment seg : gantt) {//Iterate over each Gantt segment.
            topLine.append("|").append(String.format("%-" + blockWidth + "s", seg.label));//Append the segment label formatted within the block.
        }
        for (GanttSegment seg : gantt) {//Iterate over each Gantt segment again.
            bottomLine.append("|").append(String.format("%-" + blockWidth + "s", seg.start));//Append the start time formatted within the block.
        }
        bottomLine.append("Finish:").append(gantt.get(gantt.size() - 1).finish);//Append the final finish time.
        System.out.println("\nGantt Chart:");//Print the Gantt chart header.
        System.out.println(topLine.toString());//Print the top line of the chart.
        System.out.println(bottomLine.toString());//Print the bottom line of the chart.
    }

    static void fcfsScheduling(List<Process> processes) {//This method simulates First-Come, First-Served scheduling.
        List<Process> procs = new ArrayList<>();//Create a new list to hold copies of the processes.
        for (Process p : processes) {//For each process in the input list,
            procs.add(new Process(p));//add a copy to avoid modifying the original list.
        }
        if (procs.isEmpty()) {//If there are no processes to schedule,
            System.out.println("\n--- FCFS Scheduling ---");//print the FCFS scheduling header.
            System.out.println("No processes to schedule.");//inform the user.
            return;//Exit the method.
        }
        int time = 0;//Initialize simulation time to 0.
        List<GanttSegment> gantt = new ArrayList<>();//Create a list to store Gantt chart segments.
        System.out.println("\n--- FCFS Scheduling ---");//Print the scheduling header.
        for (Process p : procs) {//For each process in FCFS order,
            if (time < p.arrival) {//if the current time is less than the process arrival time,
                gantt.add(new GanttSegment("Idle", time, p.arrival));//add an "Idle" segment.
                time = p.arrival;//update the current time to the process arrival time.
            }
            int start = time;//Record the start time for the process.
            if (p.cpuInit == -1) {//If the CPU initialization time is not set,
                p.cpuInit = start;//set it to the current start time.
            }
            p.waiting = time - p.arrival;//Calculate the waiting time.
            time += p.burst;//Increment time by the burst time.
            p.completion = time;//Set the completion time.
            p.turnaround = p.completion - p.arrival;//Calculate the turnaround time.
            gantt.add(new GanttSegment("P" + p.pid, start, time));//Add a segment for this process.
        }
        double avgWait = procs.stream().mapToInt(p -> p.waiting).average().orElse(0);//Calculate the average waiting time.
        double avgTurnaround = procs.stream().mapToInt(p -> p.turnaround).average().orElse(0);//Calculate the average turnaround time.
        printGanttChart(gantt);//Print the Gantt chart.
        for (Process p : procs) {//For each process,
            System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n",
                              p.pid, p.cpuInit, p.waiting, p.turnaround);//print the process details.
        }
        System.out.printf("Average Waiting Time: %.2f\n", avgWait);//Print the average waiting time.
        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);//Print the average turnaround time.
    }

    static void sjfScheduling(List<Process> processes) {//This method simulates Shortest Job First scheduling (non-preemptive).
        List<Process> procs = new ArrayList<>();//Create a list to hold copies of the processes.
        for (Process p : processes) {//For each process,
            procs.add(new Process(p));//add a copy to the list.
        }
        if (procs.isEmpty()) {//If there are no processes,
            System.out.println("\n--- SJF Scheduling (Non-Preemptive) ---");//print the SJF scheduling header.
            System.out.println("No processes to schedule.");//inform the user.
            return;//Exit the method.
        }
        int time = 0;//Initialize simulation time.
        List<GanttSegment> gantt = new ArrayList<>();//Create a list for Gantt chart segments.
        List<Process> finished = new ArrayList<>();//Create a list to store finished processes.
        System.out.println("\n--- SJF Scheduling (Non-Preemptive) ---");//Print the scheduling header.
        while (!procs.isEmpty()) {//Continue until all processes are scheduled.
            List<Process> available = new ArrayList<>();//Create a list for processes that have arrived.
            for (Process p : procs) {//For each process,
                if (p.arrival <= time)//if it has arrived,
                    available.add(p);//add it to the available list.
            }
            if (available.isEmpty()) {//If no process is available,
                int nextArrival = procs.stream().mapToInt(p -> p.arrival).min().orElse(time);//Determine the next arrival time.
                gantt.add(new GanttSegment("Idle", time, nextArrival));//Add an "Idle" segment.
                time = nextArrival;//Update simulation time.
                continue;//Continue to the next iteration.
            }
            Process current = available.get(0);//Assume the first available process is the current one.
            for (Process p : available) {//For each available process,
                if (p.burst < current.burst)//if it has a shorter burst time,
                    current = p;//select it.
            }
            procs.remove(current);//Remove the selected process from the list.
            int start = time;//Record the start time.
            if (current.cpuInit == -1) {//If CPU initialization time is not set,
                current.cpuInit = start;//set it to the current time.
            }
            current.waiting = time - current.arrival;//Calculate the waiting time.
            time += current.burst;//Increment time by the burst time.
            current.completion = time;//Set the completion time.
            current.turnaround = current.completion - current.arrival;//Calculate turnaround time.
            gantt.add(new GanttSegment("P" + current.pid, start, time));//Add a segment for this process.
            finished.add(current);//Add the process to the finished list.
        }
        double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);//Calculate average waiting time.
        double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);//Calculate average turnaround time.
        printGanttChart(gantt);//Print the Gantt chart.
        for (Process p : finished) {//For each finished process,
            System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n",
                              p.pid, p.cpuInit, p.waiting, p.turnaround);//Print the process details.
        }
        System.out.printf("Average Waiting Time: %.2f\n", avgWait);//Print average waiting time.
        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);//Print average turnaround time.
    }

    static void roundRobinScheduling(List<Process> processes, int timeQuantum) {//This method simulates Round Robin scheduling.
        List<Process> procs = new ArrayList<>();//Create a list to hold copies of the processes.
        for (Process p : processes) {//For each process,
            procs.add(new Process(p));//add a copy to avoid modifying the original list.
        }
        if (procs.isEmpty()) {//If there are no processes,
            System.out.println("\n--- Round Robin Scheduling (Time Quantum = " + timeQuantum + ") ---");//Print the scheduling header.
            System.out.println("No processes to schedule.");//Inform the user.
            return;//Exit the method.
        }
        for (Process p : procs) {//For each process,
            p.remaining = p.burst;//Set the remaining time to the burst time.
        }
        int time = 0;//Initialize simulation time.
        List<GanttSegment> gantt = new ArrayList<>();//Create a list for Gantt chart segments.
        List<Process> finished = new ArrayList<>();//Create a list for finished processes.
        List<Process> notAdded = new ArrayList<>(procs);//Create a list of processes not yet added to the ready queue.
        notAdded.sort(Comparator.comparingInt(p -> p.arrival));//Sort the not-added processes by arrival time.
        List<Process> queue = new ArrayList<>();//Create the ready queue.
        System.out.println("\n--- Round Robin Scheduling (Time Quantum = " + timeQuantum + ") ---");//Print the scheduling header.
        while (!queue.isEmpty() || !notAdded.isEmpty()) {//Continue until both the queue and notAdded list are empty.
            if (queue.isEmpty()) {//If the ready queue is empty,
                Process nextProc = notAdded.get(0);//Get the next process by arrival.
                if (time < nextProc.arrival) {//If the current time is less than the process arrival time,
                    gantt.add(new GanttSegment("Idle", time, nextProc.arrival));//Add an "Idle" segment.
                    time = nextProc.arrival;//Update simulation time.
                }
                while (!notAdded.isEmpty() && notAdded.get(0).arrival <= time) {//Move all processes that have arrived into the ready queue.
                    queue.add(notAdded.remove(0));//Add the process to the queue.
                }
            }
            Process current = queue.remove(0);//Dequeue the first process from the ready queue.
            int start = time;//Record the start time.
            if (current.cpuInit == -1) {//If CPU initialization time is not set,
                current.cpuInit = start;//Set it to the current time.
            }
            int execTime = Math.min(timeQuantum, current.remaining);//Determine execution time as the minimum of time quantum and remaining burst time.
            time += execTime;//Increment simulation time by the execution time.
            current.remaining -= execTime;//Subtract the execution time from the remaining burst time.
            gantt.add(new GanttSegment("P" + current.pid, start, time));//Add a segment for this execution.
            while (!notAdded.isEmpty() && notAdded.get(0).arrival <= time) {//Add any new processes that have arrived into the ready queue.
                queue.add(notAdded.remove(0));//Add the process to the queue.
            }
            if (current.remaining > 0) {//If the process still has burst time remaining,
                queue.add(current);//Re-add it to the ready queue.
            } else {//If the process has finished,
                current.completion = time;//Set its completion time.
                current.turnaround = current.completion - current.arrival;//Calculate turnaround time.
                current.waiting = current.turnaround - current.burst;//Calculate waiting time.
                finished.add(current);//Add the process to the finished list.
            }
        }
        finished.sort(Comparator.comparingInt(p -> p.pid));//Sort finished processes by PID.
        double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);//Calculate average waiting time.
        double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);//Calculate average turnaround time.
        printGanttChart(gantt);//Print the Gantt chart.
        for (Process p : finished) {//For each finished process,
            System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n",
                              p.pid, p.cpuInit, p.waiting, p.turnaround);//Print the process details.
        }
        System.out.printf("Average Waiting Time: %.2f\n", avgWait);//Print average waiting time.
        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);//Print average turnaround time.
    }

    static void priorityScheduling(List<Process> processes) {//This method simulates Priority scheduling (non-preemptive) with reversed priority order.
        List<Process> procs = new ArrayList<>();//Create a list to hold copies of the processes.
        for (Process p : processes) {//For each process,
            procs.add(new Process(p));//Add a copy to avoid modifying the original list.
        }
        if (procs.isEmpty()) {//If there are no processes,
            System.out.println("\n--- Priority Scheduling (Non-Preemptive) ---");//Print the scheduling header.
            System.out.println("No processes to schedule.");//Inform the user.
            return;//Exit the method.
        }
        int time = 0;//Initialize simulation time.
        List<GanttSegment> gantt = new ArrayList<>();//Create a list for Gantt chart segments.
        List<Process> finished = new ArrayList<>();//Create a list for finished processes.
        System.out.println("\n--- Priority Scheduling (Non-Preemptive) ---");//Print the scheduling header.
        while (!procs.isEmpty()) {//Continue until all processes are scheduled.
            List<Process> available = new ArrayList<>();//Create a list for processes that have arrived.
            for (Process p : procs) {//For each process,
                if (p.arrival <= time)//If the process has arrived,
                    available.add(p);//add it to the available list.
            }
            if (available.isEmpty()) {//If no process is available,
                int nextArrival = procs.stream().mapToInt(p -> p.arrival).min().orElse(time);//Determine the next arrival time.
                gantt.add(new GanttSegment("Idle", time, nextArrival));//Add an "Idle" segment.
                time = nextArrival;//Update simulation time.
                continue;//Continue to the next iteration.
            }
            Process current = available.get(0);//Assume the first available process is the current one.
            for (Process p : available) {//For each available process,
                if (p.priority > current.priority)//If it has a higher numerical priority,
                    current = p;//select it.
            }
            procs.remove(current);//Remove the selected process from the list.
            int start = time;//Record the start time.
            if (current.cpuInit == -1) {//If CPU initialization time is not set,
                current.cpuInit = start;//set it to the current time.
            }
            current.waiting = time - current.arrival;//Calculate waiting time.
            time += current.burst;//Increment simulation time by the burst time.
            current.completion = time;//Set the completion time.
            current.turnaround = current.completion - current.arrival;//Calculate turnaround time.
            gantt.add(new GanttSegment("P" + current.pid, start, time));//Add a segment for this process.
            finished.add(current);//Add the process to the finished list.
        }
        double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);//Calculate average waiting time.
        double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);//Calculate average turnaround time.
        printGanttChart(gantt);//Print the Gantt chart.
        for (Process p : finished) {//For each finished process,
            System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d | Priority: %d\n",
                              p.pid, p.cpuInit, p.waiting, p.turnaround, p.priority);//Print process details including priority.
        }
        System.out.printf("Average Waiting Time: %.2f\n", avgWait);//Print average waiting time.
        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);//Print average turnaround time.
    }

    static MemoryHole firstFitAllocation(List<MemoryHole> holes, int request) {//This method implements the first-fit memory allocation strategy.
        for (int i = 0; i < holes.size(); i++) {//Iterate over the list of memory holes.
            MemoryHole hole = holes.get(i);//Get the current memory hole.
            if (hole.size >= request) {//If the hole is large enough for the request,
                MemoryHole allocated = new MemoryHole(hole.start, request);//Allocate memory from this hole.
                int newStart = hole.start + request;//Calculate the new start address after allocation.
                int newSize = hole.size - request;//Calculate the remaining size of the hole.
                if (newSize > 0) {//If there is remaining space,
                    holes.set(i, new MemoryHole(newStart, newSize));//Update the hole with the new start and size.
                } else {//If the hole is completely used,
                    holes.remove(i);//Remove the hole.
                }
                return allocated;//Return the allocated memory block.
            }
        }
        return null;//Return null if no suitable hole is found.
    }

    static MemoryHole bestFitAllocation(List<MemoryHole> holes, int request) {//This method implements the best-fit memory allocation strategy.
        int bestIndex = -1;//Initialize the best index to -1.
        int bestSize = Integer.MAX_VALUE;//Initialize the best size to a very large value.
        for (int i = 0; i < holes.size(); i++) {//Iterate over the memory holes.
            MemoryHole hole = holes.get(i);//Get the current memory hole.
            if (hole.size >= request && hole.size < bestSize) {//If the hole is large enough and smaller than the current best,
                bestSize = hole.size;//Update the best size.
                bestIndex = i;//Update the best index.
            }
        }
        if (bestIndex != -1) {//If a suitable hole was found,
            MemoryHole hole = holes.get(bestIndex);//Retrieve the best-fit hole.
            MemoryHole allocated = new MemoryHole(hole.start, request);//Allocate memory from the hole.
            int newStart = hole.start + request;//Calculate the new start address.
            int newSize = hole.size - request;//Calculate the remaining size.
            if (newSize > 0) {//If there is remaining memory,
                holes.set(bestIndex, new MemoryHole(newStart, newSize));//Update the hole.
            } else {//If the hole is completely used,
                holes.remove(bestIndex);//Remove the hole.
            }
            return allocated;//Return the allocated memory.
        }
        return null;//Return null if allocation fails.
    }

    static MemoryHole worstFitAllocation(List<MemoryHole> holes, int request) {//This method implements the worst-fit memory allocation strategy.
        int worstIndex = -1;//Initialize the worst index to -1.
        int worstSize = -1;//Initialize the worst size to -1.
        for (int i = 0; i < holes.size(); i++) {//Iterate over the memory holes.
            MemoryHole hole = holes.get(i);//Get the current memory hole.
            if (hole.size >= request && hole.size > worstSize) {//If the hole is large enough and larger than the current worst,
                worstSize = hole.size;//Update the worst size.
                worstIndex = i;//Update the worst index.
            }
        }
        if (worstIndex != -1) {//If a suitable hole was found,
            MemoryHole hole = holes.get(worstIndex);//Retrieve the worst-fit hole.
            MemoryHole allocated = new MemoryHole(hole.start, request);//Allocate memory from the hole.
            int newStart = hole.start + request;//Calculate the new start address.
            int newSize = hole.size - request;//Calculate the remaining size.
            if (newSize > 0) {//If there is remaining memory,
                holes.set(worstIndex, new MemoryHole(newStart, newSize));//Update the hole.
            } else {//If the hole is completely used,
                holes.remove(worstIndex);//Remove the hole.
            }
            return allocated;//Return the allocated memory block.
        }
        return null;//Return null if no suitable hole is found.
    }

    static void simulateMemoryAllocation(String strategy, List<Process> processes) {//This method simulates memory allocation for processes using a given strategy.
        //Create a fragmented memory scenario with multiple free holes.
        List<MemoryHole> holes = new ArrayList<>();//Create a list to hold memory holes.
        holes.add(new MemoryHole(0, 250));//First hole: starts at 0 with size 250.
        holes.add(new MemoryHole(300, 150));//Second hole: starts at 300 with size 150.
        holes.add(new MemoryHole(500, 300));//Third hole: starts at 500 with size 300.
        holes.add(new MemoryHole(850, 150));//Fourth hole: starts at 850 with size 150.
        System.out.println("\nMemory Allocation Simulation using " + strategy.toUpperCase() + " (Fragmented Memory):");//Print the simulation header.
        for (Process p : processes) {//For each process,
            int request = p.memoryRequirement;//Get its memory requirement.
            MemoryHole allocated = null;//Initialize the allocated memory block to null.
            if (strategy.equals("first_fit")) {//If the strategy is first-fit,
                allocated = firstFitAllocation(holes, request);//Allocate memory using first-fit.
            } else if (strategy.equals("best_fit")) {//If the strategy is best-fit,
                allocated = bestFitAllocation(holes, request);//Allocate memory using best-fit.
            } else if (strategy.equals("worst_fit")) {//If the strategy is worst-fit,
                allocated = worstFitAllocation(holes, request);//Allocate memory using worst-fit.
            } else {//If an unknown strategy is specified,
                System.out.println("Unknown strategy");//Print an error message.
                return;//Exit the method.
            }
            if (allocated != null) {//If allocation was successful,
                System.out.println("Process " + p.pid + " (memory request: " + request +
                                   ") allocated at address " + allocated.start + " with size " + allocated.size);//Print the allocation details.
            } else {//If allocation failed,
                System.out.println("Process " + p.pid + " allocation of size " + request + " failed.");//Print a failure message.
            }
        }
        System.out.println("Remaining free holes:");//Print header for remaining free memory holes.
        for (MemoryHole hole : holes) {//For each remaining memory hole,
            System.out.println("Start: " + hole.start + ", Size: " + hole.size);//Print the hole's details.
        }
    }

    static int simulatePagingFIFO(int[] pageReferences, int numFrames) {//This method simulates paging using the FIFO algorithm.
        List<Integer> frames = new ArrayList<>();//Create a list to represent memory frames.
        int pageFaults = 0;//Initialize the page fault counter to 0.
        for (int page : pageReferences) {//For each page reference,
            if (!frames.contains(page)) {//If the page is not in memory,
                pageFaults++;//Increment the page fault counter.
                if (frames.size() < numFrames) {//If there is space in memory,
                    frames.add(page);//Add the page to the frames.
                } else {//If memory is full,
                    frames.remove(0);//Remove the oldest page.
                    frames.add(page);//Add the new page.
                }
            }
        }
        return pageFaults;//Return the total number of page faults.
    }

    static int simulatePagingLRU(int[] pageReferences, int numFrames) {//This method simulates paging using the LRU algorithm.
        List<Integer> frames = new ArrayList<>();//Create a list to represent memory frames.
        int pageFaults = 0;//Initialize the page fault counter to 0.
        for (int page : pageReferences) {//For each page reference,
            if (!frames.contains(page)) {//If the page is not in memory,
                pageFaults++;//Increment the page fault counter.
                if (frames.size() < numFrames) {//If there is space in memory,
                    frames.add(page);//Add the page.
                } else {//If memory is full,
                    frames.remove(0);//Remove the least recently used page (simplified approach).
                    frames.add(page);//Add the new page.
                }
            } else {//If the page is already in memory,
                frames.remove(Integer.valueOf(page));//Remove it to update its recency.
                frames.add(page);//Re-add it to mark it as most recently used.
            }
        }
        return pageFaults;//Return the total number of page faults.
    }

    static boolean getYesNo(String prompt, Scanner sc) {//This helper method prompts the user for a yes/no input.
        while (true) {//Loop until valid input is received.
            System.out.print(prompt);//Print the prompt.
            String input = sc.nextLine().trim().toLowerCase();//Read and normalize the input.
            if (input.equals("y")) {//If the input is "y",
                return true;//Return true.
            } else if (input.equals("n")) {//If the input is "n",
                return false;//Return false.
            } else {//If the input is invalid,
                System.out.println("Invalid option. Please enter 'y' or 'n'.");//Print an error message.
            }
        }
    }

    public static void main(String[] args) {//This is the main method, the entry point of the program.
        Scanner sc = new Scanner(System.in);//Create a Scanner object to read user input.
        String filename = "processes.txt";//Define the filename for the processes file.
        Path filePath = Paths.get(filename);//Construct a Path object for the filename.
        System.out.println("The processes file is located at: " + filePath.toAbsolutePath());//Print the absolute file path.
        boolean useFile = getYesNo("Do you want to run the program using this file? (y/n): ", sc);//Prompt the user to confirm using the file.
        if (!useFile) {//If the user chooses not to use the file,
            System.out.println("Exiting program.");//Print an exit message.
            sc.close();//Close the Scanner.
            return;//Exit the program.
        }
        List<Process> processes = readProcesses(filename);//Read the processes from the file.
        if (processes.isEmpty()) {//If no processes were read,
            System.out.println("No processes to schedule. Please check your processes.txt file.");//Inform the user.
        } else {//If processes are available,
            processes.sort(Comparator.comparingInt(p -> p.arrival));//Sort the processes by arrival time.
            if (getYesNo("Run FCFS Scheduling? (y/n): ", sc)) {//Prompt to run FCFS scheduling.
                fcfsScheduling(processes);//Execute FCFS scheduling.
                System.out.println("\n--------------------\n");//Print a separator.
            }
            if (getYesNo("Run SJF Scheduling? (y/n): ", sc)) {//Prompt to run SJF scheduling.
                sjfScheduling(processes);//Execute SJF scheduling.
                System.out.println("\n--------------------\n");//Print a separator.
            }
            if (getYesNo("Run Round Robin Scheduling? (y/n): ", sc)) {//Prompt to run Round Robin scheduling.
                roundRobinScheduling(processes, 4);//Execute Round Robin scheduling with a time quantum of 4.
                System.out.println("\n--------------------\n");//Print a separator.
            }
            if (getYesNo("Run Priority Scheduling? (y/n): ", sc)) {//Prompt to run Priority scheduling.
                priorityScheduling(processes);//Execute Priority scheduling.
                System.out.println("\n--------------------\n");//Print a separator.
            }
        }
        System.out.println("\nMemory Allocation Simulation:");//Print header for memory allocation simulation.
        simulateMemoryAllocation("first_fit", processes);//Simulate memory allocation using the first-fit strategy.
        simulateMemoryAllocation("best_fit", processes);//Simulate memory allocation using the best-fit strategy.
        simulateMemoryAllocation("worst_fit", processes);//Simulate memory allocation using the worst-fit strategy.
        
        int[] pageRefs = {7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2};//Define an array of page references.
        int numFrames = 3;//Define the number of memory frames.
        int fifoFaults = simulatePagingFIFO(pageRefs, numFrames);//Simulate FIFO paging and capture the number of page faults.
        int lruFaults = simulatePagingLRU(pageRefs, numFrames);//Simulate LRU paging and capture the number of page faults.
        System.out.println("\nPaging Simulation:");//Print header for paging simulation.
        System.out.println("FIFO Page Faults: " + fifoFaults);//Print the number of FIFO page faults.
        System.out.println("LRU Page Faults: " + lruFaults);//Print the number of LRU page faults.
        sc.close();//Close the Scanner.
    }
}
