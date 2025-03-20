import java.io.BufferedReader;//Import for efficient reading of text from an input stream.
import java.io.FileReader;//Import for reading files.
import java.io.IOException;//Import for handling input/output exceptions.
import java.nio.file.Paths;//Import for constructing file paths from strings.
import java.nio.file.Path;//Import for representing file system paths.
import java.util.*;//Import all utility classes (e.g., ArrayList, Comparator, HashMap, etc.).

//Define the main class that simulates various operating system scheduling algorithms.
public class OperatingSystemSchedulesProcesses {

    //Static nested class to represent a process.
    static class Process {
        int pid, arrival, burst, priority, waiting, turnaround, completion, remaining, cpuInit;//Declare process attributes plus cpuInit.

        //Constructor: Initializes a process with given PID, arrival time, burst time, and priority.
        Process(int pid, int arrival, int burst, int priority) {
            this.pid = pid;//Set process ID.
            this.arrival = arrival;//Set process arrival time.
            this.burst = burst;//Set CPU burst time.
            this.priority = priority;//Set process priority.
            this.waiting = 0;//Initialize waiting time to 0.
            this.turnaround = 0;//Initialize turnaround time to 0.
            this.completion = 0;//Initialize completion time to 0.
            this.remaining = burst;//Initially, remaining time equals burst time.
            this.cpuInit = -1;//Initialize CPU initialization time to -1 (unset).
        }

        //Copy constructor: Creates a new process by copying attributes from an existing process.
        Process(Process p) {
            this.pid = p.pid;//Copy process ID.
            this.arrival = p.arrival;//Copy arrival time.
            this.burst = p.burst;//Copy burst time.
            this.priority = p.priority;//Copy priority.
            this.waiting = p.waiting;//Copy waiting time.
            this.turnaround = p.turnaround;//Copy turnaround time.
            this.completion = p.completion;//Copy completion time.
            this.remaining = p.remaining;//Copy remaining burst time.
            this.cpuInit = p.cpuInit;//Copy CPU initialization time.
        }
    }

    //Static nested class representing a segment in the Gantt chart.
    static class GanttSegment {
        String label;//Label for the segment (e.g., "P1" or "Idle").
        int start, finish;//Start and finish times for this segment.

        //Constructor: Initializes a GanttSegment with a label, start time, and finish time.
        GanttSegment(String label, int start, int finish) {
            this.label = label;//Set the segment label.
            this.start = start;//Set the segment's start time.
            this.finish = finish;//Set the segment's finish time.
        }
    }

    //Static nested class representing a memory hole (free memory block).
    static class MemoryHole {
        int start, size;//Starting address and size of the memory hole.

        //Constructor: Initializes a MemoryHole with a starting address and size.
        MemoryHole(int start, int size) {
            this.start = start;//Set the starting address.
            this.size = size;//Set the size of the memory hole.
        }
    }

    //Reads processes from a file and returns a list of Process objects.
    static List<Process> readProcesses(String filename) {
        List<Process> processes = new ArrayList<>();//Create an ArrayList to hold processes.
        Path filePath = Paths.get(filename);//Create a Path object for the provided filename.
        System.out.println("Reading file from: " + filePath.toAbsolutePath());//Print the absolute file path for debugging.
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {//Open the file using BufferedReader.
            String line = br.readLine();//Read the header line (first line) and ignore it.
            while ((line = br.readLine()) != null) {//Loop over each subsequent line until end-of-file.
                if (line.trim().isEmpty())//If the line is empty after trimming whitespace,
                    continue;//skip this iteration.
                String[] parts = line.trim().split("\\s+");//Split the line by one or more whitespace characters.
                if (parts.length >= 4) {//If there are at least 4 fields (PID, arrival, burst, priority),
                    int pid = Integer.parseInt(parts[0]);//Parse the process ID.
                    int arrival = Integer.parseInt(parts[1]);//Parse the arrival time.
                    int burst = Integer.parseInt(parts[2]);//Parse the burst time.
                    int priority = Integer.parseInt(parts[3]);//Parse the priority.
                    processes.add(new Process(pid, arrival, burst, priority));//Create and add a new Process.
                }
            }
        } catch (IOException e) {//Catch any I/O exceptions.
            System.out.println("Error: File " + filename + " not found.");//Print an error message if the file is not found.
        }
        return processes;//Return the list of processes.
    }

    //Prints a text-based Gantt chart with process labels and time markers.
    static void printGanttChart(List<GanttSegment> gantt) {
        if (gantt.isEmpty()) {//If there are no Gantt segments,
            System.out.println("\nNo Gantt chart to display.");//Inform the user.
            return;//Exit the method.
        }
        int blockWidth = 6;//Define a fixed block width for each segment.
        StringBuilder topLine = new StringBuilder();//StringBuilder to construct the top line (process labels).
        StringBuilder bottomLine = new StringBuilder();//StringBuilder to construct the bottom line (time markers).
        for (GanttSegment seg : gantt) {//Loop through each Gantt segment.
            topLine.append("|").append(String.format("%-" + blockWidth + "s", seg.label));//Append a bar and formatted label.
        }
        for (GanttSegment seg : gantt) {//Loop through each Gantt segment.
            bottomLine.append("|").append(String.format("%-" + blockWidth + "s", seg.start));//Append a bar and formatted start time.
        }
        bottomLine.append("Finish:").append(gantt.get(gantt.size() - 1).finish);//Append the final finish time.
        System.out.println("\nGantt Chart:");//Print a header for the Gantt chart.
        System.out.println(topLine.toString());//Print the top line (labels).
        System.out.println(bottomLine.toString());//Print the bottom line (time markers).
    }

    //FCFS (First-Come, First-Served) Scheduling.
    static void fcfsScheduling(List<Process> processes) {
        List<Process> procs = new ArrayList<>();//Create a list to hold copies of the processes.
        for (Process p : processes) {//For each process in the input list,
            procs.add(new Process(p));//add a copy to avoid modifying the original.
        }
        if (procs.isEmpty()) {//If no processes exist,
            System.out.println("\n--- FCFS Scheduling ---");//Print the scheduling header.
            System.out.println("No processes to schedule.");//Inform the user.
            return;//Exit the method.
        }
        int time = 0;//Initialize simulation time to 0.
        List<GanttSegment> gantt = new ArrayList<>();//Create a list for storing Gantt chart segments.
        System.out.println("\n--- FCFS Scheduling ---");//Print scheduling header.
        for (Process p : procs) {//Process each process in FCFS order.
            if (time < p.arrival) {//If current time is before the process arrival time,
                gantt.add(new GanttSegment("Idle", time, p.arrival));//Add an "Idle" segment.
                time = p.arrival;//Update simulation time to the process's arrival.
            }
            int start = time;//Record the start time for the process.
            if(p.cpuInit == -1) {//If CPU initialization time is not yet set,
                p.cpuInit = start;//Set it to the current start time.
            }
            p.waiting = time - p.arrival;//Calculate waiting time.
            time += p.burst;//Increment simulation time by the burst time.
            p.completion = time;//Set completion time to current time.
            p.turnaround = p.completion - p.arrival;//Calculate turnaround time.
            gantt.add(new GanttSegment("P" + p.pid, start, time));//Add a segment for this process.
        }
        double avgWait = procs.stream().mapToInt(p -> p.waiting).average().orElse(0);//Calculate average waiting time.
        double avgTurnaround = procs.stream().mapToInt(p -> p.turnaround).average().orElse(0);//Calculate average turnaround time.
        printGanttChart(gantt);//First, print the Gantt chart.
        for (Process p : procs) {//Then, print the results for each process.
            System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n",
                              p.pid, p.cpuInit, p.waiting, p.turnaround);//Print process details.
        }
        System.out.printf("Average Waiting Time: %.2f\n", avgWait);//Print average waiting time.
        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);//Print average turnaround time.
    }

    //SJF (Shortest Job First) Scheduling (Non-Preemptive).
    static void sjfScheduling(List<Process> processes) {
        List<Process> procs = new ArrayList<>();//Create a list for process copies.
        for (Process p : processes) {//For each process,
            procs.add(new Process(p));//Add a copy.
        }
        if (procs.isEmpty()) {//If there are no processes,
            System.out.println("\n--- SJF Scheduling (Non-Preemptive) ---");
            System.out.println("No processes to schedule.");//Inform the user.
            return;//Exit the method.
        }
        int time = 0;//Initialize simulation time.
        List<GanttSegment> gantt = new ArrayList<>();//Create a list for Gantt segments.
        List<Process> finished = new ArrayList<>();//Create a list to hold finished processes.
        System.out.println("\n--- SJF Scheduling (Non-Preemptive) ---");//Print scheduling header.
        while (!procs.isEmpty()) {//Continue until all processes are scheduled.
            List<Process> available = new ArrayList<>();//List for processes that have arrived.
            for (Process p : procs) {//For each remaining process,
                if (p.arrival <= time)
                    available.add(p);//Add process if it has arrived.
            }
            if (available.isEmpty()) {//If no process is available,
                int nextArrival = procs.stream().mapToInt(p -> p.arrival).min().orElse(time);//Find next arrival time.
                gantt.add(new GanttSegment("Idle", time, nextArrival));//Add an "Idle" segment.
                time = nextArrival;//Update simulation time.
                continue;//Continue to next iteration.
            }
            Process current = available.get(0);//Choose the process with the smallest burst time.
            for (Process p : available) {//Loop through available processes.
                if (p.burst < current.burst)
                    current = p;//Select process with shorter burst.
            }
            procs.remove(current);//Remove the selected process from the list.
            int start = time;//Record start time.
            if(current.cpuInit == -1) {//If CPU initialization time is not yet set,
                current.cpuInit = start;//Set it to the start time.
            }
            current.waiting = time - current.arrival;//Calculate waiting time.
            time += current.burst;//Update simulation time by adding burst time.
            current.completion = time;//Set completion time.
            current.turnaround = current.completion - current.arrival;//Calculate turnaround time.
            gantt.add(new GanttSegment("P" + current.pid, start, time));//Add a segment for the process.
            finished.add(current);//Add process to finished list.
        }
        double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);//Calculate average waiting time.
        double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);//Calculate average turnaround time.
        printGanttChart(gantt);//First, print the Gantt chart.
        for (Process p : finished) {//Then, print the results for each finished process.
            System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n",
                              p.pid, p.cpuInit, p.waiting, p.turnaround);//Print process details.
        }
        System.out.printf("Average Waiting Time: %.2f\n", avgWait);//Print average waiting time.
        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);//Print average turnaround time.
    }

    //Round Robin Scheduling.
    static void roundRobinScheduling(List<Process> processes, int timeQuantum) {
        List<Process> procs = new ArrayList<>();//Create a list to hold copies of the processes.
        for (Process p : processes) {//For each process,
            procs.add(new Process(p));//Add a copy.
        }
        if (procs.isEmpty()) {//If no processes exist,
            System.out.println("\n--- Round Robin Scheduling (Time Quantum = " + timeQuantum + ") ---");
            System.out.println("No processes to schedule.");//Inform the user.
            return;//Exit the method.
        }
        for (Process p : procs) {//Reset remaining time for each process.
            p.remaining = p.burst;//Set remaining time to the burst time.
        }
        int time = 0;//Initialize simulation time.
        List<GanttSegment> gantt = new ArrayList<>();//Create a list for Gantt chart segments.
        List<Process> finished = new ArrayList<>();//Create a list for finished processes.
        List<Process> notAdded = new ArrayList<>(procs);//List for processes not yet added to the queue.
        notAdded.sort(Comparator.comparingInt(p -> p.arrival));//Sort the not-added list by arrival time.
        List<Process> queue = new ArrayList<>();//Create the scheduling queue.
        System.out.println("\n--- Round Robin Scheduling (Time Quantum = " + timeQuantum + ") ---");//Print scheduling header.
        while (!queue.isEmpty() || !notAdded.isEmpty()) {//Continue scheduling until no process remains in the queue or waiting list.
            if (queue.isEmpty()) {//If the queue is empty,
                Process nextProc = notAdded.get(0);//Get the next process based on arrival.
                if (time < nextProc.arrival) {//If current time is before its arrival,
                    gantt.add(new GanttSegment("Idle", time, nextProc.arrival));//Add an "Idle" segment.
                    time = nextProc.arrival;//Update simulation time.
                }
                while (!notAdded.isEmpty() && notAdded.get(0).arrival <= time) {//Move all processes that have arrived by current time into the queue.
                    queue.add(notAdded.remove(0));
                }
            }
            Process current = queue.remove(0);//Dequeue the first process.
            int start = time;//Record the start time for this execution.
            if(current.cpuInit == -1) {//If CPU initialization time is not yet set,
                current.cpuInit = start;//Set it to the current start time.
            }
            int execTime = Math.min(timeQuantum, current.remaining);//Determine execution time (min of time quantum or remaining time).
            time += execTime;//Update simulation time.
            current.remaining -= execTime;//Decrease the process's remaining burst time.
            gantt.add(new GanttSegment("P" + current.pid, start, time));//Add a Gantt segment for this execution.
            while (!notAdded.isEmpty() && notAdded.get(0).arrival <= time) {//Add any new processes that have arrived during execution to the queue.
                queue.add(notAdded.remove(0));
            }
            if (current.remaining > 0) {//If the process still requires CPU time,
                queue.add(current);//Re-add it to the queue.
            } else {//If the process has finished execution,
                current.completion = time;//Set its completion time.
                current.turnaround = current.completion - current.arrival;//Calculate turnaround time.
                current.waiting = current.turnaround - current.burst;//Calculate waiting time.
                finished.add(current);//Add the process to the finished list.
            }
        }
        if (!finished.isEmpty()) {//If finished processes exist,
            finished.sort(Comparator.comparingInt(p -> p.pid));//Sort them by PID.
        }
        double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);//Calculate average waiting time.
        double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);//Calculate average turnaround time.
        printGanttChart(gantt);//First, print the Gantt chart.
        for (Process p : finished) {//Then, print the results for each finished process.
            System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n",
                              p.pid, p.cpuInit, p.waiting, p.turnaround);//Print process details.
        }
        System.out.printf("Average Waiting Time: %.2f\n", avgWait);//Print average waiting time.
        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);//Print average turnaround time.
    }

    //Priority Scheduling (Non-Preemptive) with reversed priority order.
    //In this version, a higher numerical priority value indicates a higher priority.
    static void priorityScheduling(List<Process> processes) {
        List<Process> procs = new ArrayList<>();//Create a list to hold process copies.
        for (Process p : processes) {//For each process,
            procs.add(new Process(p));//Add a copy.
        }
        if (procs.isEmpty()) {//If no processes exist,
            System.out.println("\n--- Priority Scheduling (Non-Preemptive) ---");
            System.out.println("No processes to schedule.");//Inform the user.
            return;//Exit the method.
        }
        int time = 0;//Initialize simulation time.
        List<GanttSegment> gantt = new ArrayList<>();//Create a list for Gantt segments.
        List<Process> finished = new ArrayList<>();//Create a list for finished processes.
        System.out.println("\n--- Priority Scheduling (Non-Preemptive) ---");//Print scheduling header.
        while (!procs.isEmpty()) {//Continue until all processes are scheduled.
            List<Process> available = new ArrayList<>();//List for processes that have arrived.
            for (Process p : procs) {//For each remaining process,
                if (p.arrival <= time)
                    available.add(p);//Add it if it has arrived.
            }
            if (available.isEmpty()) {//If no process is available,
                int nextArrival = procs.stream().mapToInt(p -> p.arrival).min().orElse(time);//Find next arrival time.
                gantt.add(new GanttSegment("Idle", time, nextArrival));//Add an "Idle" segment.
                time = nextArrival;//Update simulation time.
                continue;//Continue to next iteration.
            }
            Process current = available.get(0);//Select the process with the highest numerical priority (reversed order).
            for (Process p : available) {//Loop through available processes.
                if (p.priority > current.priority)//If a process has a higher numerical priority,
                    current = p;//Select it.
            }
            procs.remove(current);//Remove the selected process from the list.
            int start = time;//Record start time.
            if(current.cpuInit == -1) {//If CPU initialization time is not yet set,
                current.cpuInit = start;//Set it to the start time.
            }
            current.waiting = time - current.arrival;//Calculate waiting time.
            time += current.burst;//Update simulation time by adding burst time.
            current.completion = time;//Set completion time.
            current.turnaround = current.completion - current.arrival;//Calculate turnaround time.
            gantt.add(new GanttSegment("P" + current.pid, start, time));//Add a segment for this process.
            finished.add(current);//Add the process to the finished list.
        }
        double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);//Calculate average waiting time.
        double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);//Calculate average turnaround time.
        printGanttChart(gantt);//First, print the Gantt chart.
        for (Process p : finished) {//Then, print the results for each finished process.
            System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d | Priority: %d\n",
                              p.pid, p.cpuInit, p.waiting, p.turnaround, p.priority);//Print process details.
        }
        System.out.printf("Average Waiting Time: %.2f\n", avgWait);//Print average waiting time.
        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);//Print average turnaround time.
    }

    //First-Fit Allocation: Allocates memory from the first hole large enough to satisfy the request.
    static MemoryHole firstFitAllocation(List<MemoryHole> holes, int request) {
        for (int i = 0; i < holes.size(); i++) {//Loop over each memory hole.
            MemoryHole hole = holes.get(i);//Get the current memory hole.
            if (hole.size >= request) {//Check if the hole can satisfy the request.
                MemoryHole allocated = new MemoryHole(hole.start, request);//Allocate memory from this hole.
                int newStart = hole.start + request;//Calculate new start address after allocation.
                int newSize = hole.size - request;//Calculate the remaining size.
                if (newSize > 0) {//If space remains in the hole,
                    holes.set(i, new MemoryHole(newStart, newSize));//Update the hole with new start and size.
                } else {//If the hole is completely used,
                    holes.remove(i);//Remove the hole.
                }
                return allocated;//Return the allocated memory block.
            }
        }
        return null;//Return null if no suitable hole is found.
    }

    //Best-Fit Allocation: Allocates memory from the smallest hole that is large enough.
    static MemoryHole bestFitAllocation(List<MemoryHole> holes, int request) {
        int bestIndex = -1;//Initialize best index to -1.
        int bestSize = Integer.MAX_VALUE;//Initialize best size to a very large value.
        for (int i = 0; i < holes.size(); i++) {//Loop over each memory hole.
            MemoryHole hole = holes.get(i);//Get the current hole.
            if (hole.size >= request && hole.size < bestSize) {//If the hole fits and is smaller than the current best,
                bestSize = hole.size;//Update best size.
                bestIndex = i;//Update best index.
            }
        }
        if (bestIndex != -1) {//If a suitable hole is found,
            MemoryHole hole = holes.get(bestIndex);//Get that hole.
            MemoryHole allocated = new MemoryHole(hole.start, request);//Allocate memory.
            int newStart = hole.start + request;//Calculate new start address.
            int newSize = hole.size - request;//Calculate remaining size.
            if (newSize > 0) {//If there is remaining space,
                holes.set(bestIndex, new MemoryHole(newStart, newSize));//Update the hole.
            } else {//Otherwise,
                holes.remove(bestIndex);//Remove the hole.
            }
            return allocated;//Return the allocated memory.
        }
        return null;//Return null if allocation fails.
    }

    //Worst-Fit Allocation: Allocates memory from the largest available hole.
    static MemoryHole worstFitAllocation(List<MemoryHole> holes, int request) {
        int worstIndex = -1;//Initialize worst index to -1.
        int worstSize = -1;//Initialize worst size to -1.
        for (int i = 0; i < holes.size(); i++) {//Loop over each memory hole.
            MemoryHole hole = holes.get(i);//Get the current hole.
            if (hole.size >= request && hole.size > worstSize) {//If the hole fits and is larger than current worst,
                worstSize = hole.size;//Update worst size.
                worstIndex = i;//Update worst index.
            }
        }
        if (worstIndex != -1) {//If a suitable hole is found,
            MemoryHole hole = holes.get(worstIndex);//Get that hole.
            MemoryHole allocated = new MemoryHole(hole.start, request);//Allocate memory from this hole.
            int newStart = hole.start + request;//Calculate new start address.
            int newSize = hole.size - request;//Calculate remaining size.
            if (newSize > 0) {//If space remains,
                holes.set(worstIndex, new MemoryHole(newStart, newSize));//Update the hole.
            } else {//Otherwise,
                holes.remove(worstIndex);//Remove the hole.
            }
            return allocated;//Return the allocated memory.
        }
        return null;//Return null if no suitable hole is found.
    }

    //Simulates memory allocation using a specified strategy (first_fit, best_fit, worst_fit).
    static void simulateMemoryAllocation(String strategy) {
        List<MemoryHole> holes = new ArrayList<>();//Create a list to hold memory holes.
        holes.add(new MemoryHole(0, 1000));//Initialize with one large free memory hole (start at 0, size 1000).
        int[][] requests = {//Define sample allocation requests as {processId, memorySize}.
            {1, 100},
            {2, 300},
            {3, 50},
            {4, 200},
            {5, 150},
            {6, 100},
            {7, 80}
        };
        Map<Integer, MemoryHole> allocations = new HashMap<>();//Create a map to record allocations per process.
        System.out.println("\nMemory Allocation Simulation using " + strategy.toUpperCase() + ":");//Print header.
        for (int[] req : requests) {//Loop over each allocation request.
            int pid = req[0];//Extract process ID.
            int size = req[1];//Extract requested memory size.
            MemoryHole allocated = null;//Initialize allocated memory block as null.
            if (strategy.equals("first_fit")) {//If strategy is first-fit,
                allocated = firstFitAllocation(holes, size);//Attempt first-fit allocation.
            } else if (strategy.equals("best_fit")) {//If strategy is best-fit,
                allocated = bestFitAllocation(holes, size);//Attempt best-fit allocation.
            } else if (strategy.equals("worst_fit")) {//If strategy is worst-fit,
                allocated = worstFitAllocation(holes, size);//Attempt worst-fit allocation.
            } else {//If an unknown strategy is specified,
                System.out.println("Unknown strategy");//Print error message.
                return;//Exit the method.
            }
            if (allocated != null) {//If allocation was successful,
                allocations.put(pid, allocated);//Record the allocation in the map.
                System.out.println("Process " + pid + " allocated at address " + allocated.start + " with size " + allocated.size);//Print allocation details.
            } else {//If allocation failed,
                System.out.println("Process " + pid + " allocation of size " + size + " failed.");//Print failure message.
            }
        }
        System.out.println("Remaining free holes:");//Print header for remaining memory holes.
        for (MemoryHole hole : holes) {//Loop over each remaining memory hole.
            System.out.println("Start: " + hole.start + ", Size: " + hole.size);//Print hole details.
        }
    }

    //Simulates paging using the FIFO (First-In, First-Out) algorithm.
    static int simulatePagingFIFO(int[] pageReferences, int numFrames) {
        List<Integer> frames = new ArrayList<>();//Create a list to represent memory frames.
        int pageFaults = 0;//Initialize page fault counter.
        for (int page : pageReferences) {//Loop through each page reference.
            if (!frames.contains(page)) {//If the page is not in memory,
                pageFaults++;//Increment the page fault count.
                if (frames.size() < numFrames) {//If there is an empty frame,
                    frames.add(page);//Add the page.
                } else {//If no frame is free,
                    frames.remove(0);//Remove the oldest page (FIFO).
                    frames.add(page);//Add the new page.
                }
            }
        }
        return pageFaults;//Return the total number of page faults.
    }

    //Simulates paging using the LRU (Least Recently Used) algorithm.
    static int simulatePagingLRU(int[] pageReferences, int numFrames) {
        List<Integer> frames = new ArrayList<>();//Create a list to represent memory frames.
        int pageFaults = 0;//Initialize page fault counter.
        for (int page : pageReferences) {//Loop through each page reference.
            if (!frames.contains(page)) {//If the page is not already in memory,
                pageFaults++;//Increment page fault counter.
                if (frames.size() < numFrames) {//If there is space in memory,
                    frames.add(page);//Add the page.
                } else {//If memory is full,
                    frames.remove(0);//Remove the least recently used page (simplified approach).
                    frames.add(page);//Add the new page.
                }
            } else {//If the page is already in memory,
                frames.remove(Integer.valueOf(page));//Remove it from its current position.
                frames.add(page);//Re-add it to mark it as most recently used.
            }
        }
        return pageFaults;//Return the total number of page faults.
    }

    //Helper method: Prompts the user for a yes/no input and returns true for "y" and false for "n".
    static boolean getYesNo(String prompt, Scanner sc) {
        while (true) {//Loop indefinitely until valid input is received.
            System.out.print(prompt);//Print the prompt without newline.
            String input = sc.nextLine().trim().toLowerCase();//Read user input, trim whitespace, and convert to lowercase.
            if (input.equals("y")) {//If input is "y",
                return true;//return true.
            } else if (input.equals("n")) {//If input is "n",
                return false;//return false.
            } else {//If input is invalid,
                System.out.println("Invalid option. Please enter 'y' or 'n'.");//Print an error message.
            }
        }
    }

    //Main method: Entry point of the program.
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);//Create a Scanner object to read user input.
        String filename = "processes.txt";//Define the filename for the processes file.
        Path filePath = Paths.get(filename);//Create a Path object for the filename.
        System.out.println("The processes file is located at: " + filePath.toAbsolutePath());//Display the absolute file path.
        //Prompt the user whether to run the program using this file.
        boolean useFile = getYesNo("Do you want to run the program using this file? (y/n): ", sc);//Prompt for yes/no.
        if (!useFile) {//If the user does not want to use this file,
            System.out.println("Exiting program.");//Inform the user.
            sc.close();//Close the Scanner.
            return;//Exit the program.
        }
        List<Process> processes = readProcesses(filename);//Read the processes from the file.
        if (processes.isEmpty()) {//If no processes were read,
            System.out.println("No processes to schedule. Please check your processes.txt file.");//Print an error message.
        } else {//Otherwise, if processes are available,
            processes.sort(Comparator.comparingInt(p -> p.arrival));//Sort the processes by arrival time.
            //Prompt the user for each scheduling algorithm.
            if (getYesNo("Run FCFS Scheduling? (y/n): ", sc)) {//Prompt for FCFS scheduling.
                fcfsScheduling(processes);//Run FCFS scheduling if user chooses yes.
                System.out.println("\n--------------------\n");//Print separator.
            }
            if (getYesNo("Run SJF Scheduling? (y/n): ", sc)) {//Prompt for SJF scheduling.
                sjfScheduling(processes);//Run SJF scheduling if user chooses yes.
                System.out.println("\n--------------------\n");//Print separator.
            }
            if (getYesNo("Run Round Robin Scheduling? (y/n): ", sc)) {//Prompt for Round Robin scheduling.
                roundRobinScheduling(processes, 4);//Run Round Robin scheduling with a time quantum of 4.
                System.out.println("\n--------------------\n");//Print separator.
            }
            if (getYesNo("Run Priority Scheduling? (y/n): ", sc)) {//Prompt for Priority scheduling.
                priorityScheduling(processes);//Run Priority scheduling if user chooses yes.
                System.out.println("\n--------------------\n");//Print separator.
            }
        }
        //Run Memory Allocation Simulation for different strategies (this section runs automatically).
        System.out.println("\nMemory Allocation Simulation:");//Print header.
        simulateMemoryAllocation("first_fit");//Simulate memory allocation using first-fit strategy.
        simulateMemoryAllocation("best_fit");//Simulate memory allocation using best-fit strategy.
        simulateMemoryAllocation("worst_fit");//Simulate memory allocation using worst-fit strategy.
        //Run Paging Simulation (this section runs automatically).
        int[] pageRefs = {7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2};//Define an array of page references.
        int numFrames = 3;//Define the number of memory frames.
        int fifoFaults = simulatePagingFIFO(pageRefs, numFrames);//Simulate FIFO paging.
        int lruFaults = simulatePagingLRU(pageRefs, numFrames);//Simulate LRU paging.
        System.out.println("\nPaging Simulation:");//Print paging simulation header.
        System.out.println("FIFO Page Faults: " + fifoFaults);//Output the FIFO page fault count.
        System.out.println("LRU Page Faults: " + lruFaults);//Output the LRU page fault count.
        sc.close();//Close the Scanner.
    }
}