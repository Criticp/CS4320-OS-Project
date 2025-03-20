//Importing the necessary libraries
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;

//This defines the main class for simulating OS process scheduling and memory allocation
public class OperatingSystemSchedulesProcesses {
    //This class represents an individual process
    static class Process {
        //These variables store process ID, arrival time, burst time, priority, waiting time, turnaround time, completion time, remaining burst time and CPU initialization time
        int pid, arrival, burst, priority, waiting, turnaround, completion, remaining, cpuInit;
        //This variable represents the memory requirement for the process
        int memoryRequirement;

        //This constructor initializes a process with all given parameters
        Process(int pid, int arrival, int burst, int priority, int memoryRequirement) {
            this.pid = pid; //Assign the process ID
            this.arrival = arrival; //Assign the arrival time
            this.burst = burst; //Assign the CPU burst time
            this.priority = priority; //Assign the process priority
            this.memoryRequirement = memoryRequirement; //Assign the memory requirement
            this.waiting = 0; //Initialize waiting time to 0
            this.turnaround = 0; //Initialize turnaround time to 0
            this.completion = 0; //Initialize completion time to 0
            this.remaining = burst; //Set remaining burst time equal to burst time
            this.cpuInit = -1; //Initialize CPU initialization time to -1 (unset)
        }
        //This constructor initializes a process with a default memory requirement
        Process(int pid, int arrival, int burst, int priority) {
            //Call the main constructor with a default memory requirement of 100
            this(pid, arrival, burst, priority, 100);
        }

        //This copy constructor creates a new process from an existing process
        Process(Process p) {
            this.pid = p.pid; //Copy the process ID
            this.arrival = p.arrival; //Copy the arrival time
            this.burst = p.burst; //Copy the burst time
            this.priority = p.priority; //Copy the process priority
            this.waiting = p.waiting; //Copy the waiting time
            this.turnaround = p.turnaround; //Copy the turnaround time
            this.completion = p.completion; //Copy the completion time
            this.remaining = p.remaining; //Copy the remaining burst time
            this.cpuInit = p.cpuInit; //Copy the CPU initialization time
            this.memoryRequirement = p.memoryRequirement; //Copy the memory requirement
        }
    }

    //This class represents a Gantt chart
    static class GanttSegment {
        //This variable stores the label for the segment (e.g., "P1" or "Idle")
        String label;
        //These variables store the start and finish times of the segment
        int start, finish;

        //This constructor initializes a Gantt segment with the given label, start time and finish time
        GanttSegment(String label, int start, int finish) {
            this.label = label; //Assign the segment label
            this.start = start; //Assign the start time
            this.finish = finish; //Assign the finish time
        }
    }

    //This class represents a free memory block (a memory hole)
    static class MemoryHole {
        int start, size; //These variables represent the starting address and size of the memory hole

        //This constructor initializes a memory hole
        MemoryHole(int start, int size) {
            this.start = start; //Assign the starting address
            this.size = size; //Assign the size of the memory hole
        }
    }

    //This method reads process data from a file and returns a list of Process objects
    static List<Process> readProcesses(String filename){
        //Create a list to store processes
        List<Process> processes = new ArrayList<>();
        //Construct a Path object for the given filename
        Path filePath = Paths.get(filename);
        //Print the absolute file path
        System.out.println("Reading file from: " + filePath.toAbsolutePath());

        //Open the file using BufferedReader
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
            //Read the header line and ignore it
            String line = br.readLine();

            //Loop through each subsequent line
            while ((line = br.readLine()) != null) {
                //If the line is empty after trimming whitespace
                if (line.trim().isEmpty())
                    continue;
                //Split the line by one or more whitespace characters
                String[] parts = line.trim().split("\\s+");
                //If there are at least 4 parts (PID, Arrival, Burst, Priority)
                if (parts.length >= 4) {
                    //Parse the process ID, arrival time, burst time and priority
                    int pid = Integer.parseInt(parts[0]);
                    int arrival = Integer.parseInt(parts[1]);
                    int burst = Integer.parseInt(parts[2]);
                    int priority = Integer.parseInt(parts[3]);
                    //Set a default memory requirement
                    int memoryReq = 100;
                    //If a fifth column is present
                    if (parts.length >= 5) {
                        //Parse the memory requirement
                        memoryReq = Integer.parseInt(parts[4]);
                    }
                    //Create a new Process and add it to the list
                    processes.add(new Process(pid, arrival, burst, priority, memoryReq));
                }
            }
        } 
        //Catch any I/O exceptions
        catch (IOException e) {
            //Print an error message if the file cannot be read
            System.out.println("Error: File " + filename + " not found.");
        }
        return processes;
    }

    //This method prints a text-based Gantt chart
    static void printGanttChart(List<GanttSegment> gantt){
        //If the Gantt chart is empty
        if (gantt.isEmpty()) {
            System.out.println("\nNo Gantt chart to display.");
            return;
        }
        int blockWidth = 6; //Define the width of each block in the chart
        StringBuilder topLine = new StringBuilder(); //Create a StringBuilder for the top line (labels)
        StringBuilder bottomLine = new StringBuilder(); //Create a StringBuilder for the bottom line (time markers)
        //Iterate over each Gantt segment
        for (GanttSegment seg : gantt) {
            //Append the segment label formatted within the block
            topLine.append("|").append(String.format("%-" + blockWidth + "s", seg.label));
        }
        //Iterate over each Gantt segment again
        for (GanttSegment seg : gantt) {
            //Append the start time formatted within the block
            bottomLine.append("|").append(String.format("%-" + blockWidth + "s", seg.start));
        }
        //Append the final finish time
        bottomLine.append("Finish:").append(gantt.get(gantt.size() - 1).finish);
        System.out.println("\nGantt Chart:"); //Print the Gantt chart header
        System.out.println(topLine.toString()); //Print the top line of the chart
        System.out.println(bottomLine.toString()); //Print the bottom line of the chart
    }

    //This method simulates First-Come, First-Served scheduling
    static void fcfsScheduling(List<Process> processes) {
        List<Process> procs = new ArrayList<>(); //Create a new list to hold copies of the processes
        //For each process in the input list
        for (Process p : processes) {
            //add a copy to avoid modifying the original list
            procs.add(new Process(p));
        }
        //If there are no processes to schedule
        if (procs.isEmpty()) {
            System.out.println("\n--- FCFS Scheduling ---");
            System.out.println("No processes to schedule.");
            return;
        }
        int time = 0; //Initialize simulation time to 0
        List<GanttSegment> gantt = new ArrayList<>(); //Create a list to store Gantt chart segments
        System.out.println("\n--- FCFS Scheduling ---"); //Print the scheduling header
        //For each process in FCFS order
        for (Process p : procs) {
            //if the current time is less than the process arrival time
            if (time < p.arrival) {
                //add an "Idle" segment and update the current time to the process arrival time
                gantt.add(new GanttSegment("Idle", time, p.arrival));
                time = p.arrival;
            }
            int start = time; //Record the start time for the process
            //If the CPU initialization time is not set
            if (p.cpuInit == -1) {
                //set it to the current start time
                p.cpuInit = start;
            }
            p.waiting = time - p.arrival; //Calculate the waiting time
            time += p.burst; //Increment time by the burst time
            p.completion = time; //Set the completion time
            p.turnaround = p.completion - p.arrival; //Calculate the turnaround time
            gantt.add(new GanttSegment("P" + p.pid, start, time)); //Add a segment for this process
        }
        double avgWait = procs.stream().mapToInt(p -> p.waiting).average().orElse(0); //Calculate the average waiting time
        double avgTurnaround = procs.stream().mapToInt(p -> p.turnaround).average().orElse(0); //Calculate the average turnaround time
        printGanttChart(gantt); //Print the Gantt chart
        //For each process in the list
        for (Process p : procs) {
            //print the process details
            System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n", p.pid, p.cpuInit, p.waiting, p.turnaround);
        }
        System.out.printf("Average Waiting Time: %.2f\n", avgWait); //Print the average waiting time
        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround); //Print the average turnaround time
    }

    //This method simulates Shortest Job First scheduling (non-preemptive)
    static void sjfScheduling(List<Process> processes) {
        List<Process> procs = new ArrayList<>(); //Create a list to hold copies of the processes
        //For each process
        for (Process p : processes) {
            //add a copy to the list
            procs.add(new Process(p));
        }
        //If there are no processes
        if (procs.isEmpty()) {
            System.out.println("\n--- SJF Scheduling (Non-Preemptive) ---");
            System.out.println("No processes to schedule.");
            return;
        }
        int time = 0; //Initialize simulation time
        List<GanttSegment> gantt = new ArrayList<>(); //Create a list for Gantt chart segments
        List<Process> finished = new ArrayList<>(); //Create a list to store finished processes
        System.out.println("\n--- SJF Scheduling (Non-Preemptive) ---"); //Print the scheduling header
        //Continue until all processes are scheduled
        while (!procs.isEmpty()) {
            //Create a list for processes that have arrived
            List<Process> available = new ArrayList<>();
            //For each process
            for (Process p : procs) {
                if (p.arrival <= time)
                    available.add(p);
            }
            //If no process is available
            if (available.isEmpty()) {
                int nextArrival = procs.stream().mapToInt(p -> p.arrival).min().orElse(time);
                gantt.add(new GanttSegment("Idle", time, nextArrival));
                time = nextArrival;
                continue;
            }
            Process current = available.get(0); //Assume the first available process is the current one
            //For each available process
            for (Process p : available) {
                if (p.burst < current.burst)
                    current = p;
            }
            procs.remove(current); //Remove the selected process from the list
            int start = time; //Record the start time
            if (current.cpuInit == -1) {
                current.cpuInit = start;
            }
            current.waiting = time - current.arrival;
            time += current.burst;
            current.completion = time;
            current.turnaround = current.completion - current.arrival;
            gantt.add(new GanttSegment("P" + current.pid, start, time));
            finished.add(current);
        }
        double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);
        double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);
        printGanttChart(gantt);
        for (Process p : finished) {
            System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n", p.pid, p.cpuInit, p.waiting, p.turnaround);
        }
        System.out.printf("Average Waiting Time: %.2f\n", avgWait);
        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);
    }

    //This method simulates Round Robin scheduling
    static void roundRobinScheduling(List<Process> processes, int timeQuantum) {
        List<Process> procs = new ArrayList<>(); //Create a list to hold copies of the processes
        for (Process p : processes) {
            procs.add(new Process(p));
        }
        if (procs.isEmpty()) {
            System.out.println("\n--- Round Robin Scheduling (Time Quantum = " + timeQuantum + ") ---");
            System.out.println("No processes to schedule.");
            return;
        }
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
            if (current.cpuInit == -1) {
                current.cpuInit = start;
            }
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
        finished.sort(Comparator.comparingInt(p -> p.pid));
        double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);
        double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);
        printGanttChart(gantt);
        for (Process p : finished) {
            System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d\n", p.pid, p.cpuInit, p.waiting, p.turnaround);
        }
        System.out.printf("Average Waiting Time: %.2f\n", avgWait);
        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);
    }

    //This method simulates Priority scheduling (non-preemptive) with reversed priority order
    static void priorityScheduling(List<Process> processes) {
        //Create a list to hold copies of the processes
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
                continue;
            }
            Process current = available.get(0);
            for (Process p : available) {
                if (p.priority > current.priority)
                    current = p;
            }
            procs.remove(current);
            int start = time;
            if (current.cpuInit == -1) {
                current.cpuInit = start;
            }
            current.waiting = time - current.arrival;
            time += current.burst;
            current.completion = time;
            current.turnaround = current.completion - current.arrival;
            gantt.add(new GanttSegment("P" + current.pid, start, time));
            finished.add(current);
        }
        double avgWait = finished.stream().mapToInt(p -> p.waiting).average().orElse(0);
        double avgTurnaround = finished.stream().mapToInt(p -> p.turnaround).average().orElse(0);
        printGanttChart(gantt);
        for (Process p : finished) {
            System.out.printf("PID: %-3d | CPU Init: %-3d | Waiting Time: %-3d | Turnaround Time: %-3d | Priority: %d\n", 
                              p.pid, p.cpuInit, p.waiting, p.turnaround, p.priority);
        }
        System.out.printf("Average Waiting Time: %.2f\n", avgWait);
        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaround);
    }

    //This method implements the first-fit memory allocation strategy
    static MemoryHole firstFitAllocation(List<MemoryHole> holes, int request) {
        //Iterate over the list of memory holes
        for (int i = 0; i < holes.size(); i++) {
            //Get the current memory hole
            MemoryHole hole = holes.get(i);
            //If the hole is large enough for the request
            if (hole.size >= request) {
                //Allocate memory from this hole
                MemoryHole allocated = new MemoryHole(hole.start, request);
                //Calculate the new start address after allocation
                int newStart = hole.start + request;
                //Calculate the remaining size of the hole
                int newSize = hole.size - request;
                //If there is remaining space
                if (newSize > 0) {
                    holes.set(i, new MemoryHole(newStart, newSize));
                }
                //If the hole is completely used
                else {
                    holes.remove(i);
                }
                //Return the allocated memory block
                return allocated;
            }
        }
        //Return null if no suitable hole is found
        return null;
    }

    //This method simulates memory allocation for processes using the first-fit strategy
    //It automatically determines the number of memory holes based on the number of processes
    static void simulateMemoryAllocationFirstFit(List<Process> processes) {
        //Determine the number of processes (and thus, memory holes)
        int numHoles = processes.size();
        //Create a list to hold memory holes (one per process)
        List<MemoryHole> holes = new ArrayList<>();
        Random rand = new Random();
        int start = 0;
        //For each process, create a memory hole automatically
        for (int i = 0; i < numHoles; i++) {
            //Generate a random size for each memory hole between 100 and 200 units
            int size = 100 + rand.nextInt(101);
            holes.add(new MemoryHole(start, size));
            //Set the next hole's start address with a gap of 10 units
            start += size + 10;
        }
        //Print the simulation header
        System.out.println("\nMemory Allocation Simulation using FIRST_FIT (Automatically Generated Memory Holes):");
        //Iterate over each process
        for (Process p : processes) {
            //Get its memory requirement
            int request = p.memoryRequirement;
            //Allocate memory using first-fit
            MemoryHole allocated = firstFitAllocation(holes, request);
            //If allocation was successful, print the allocation details
            if (allocated != null) {
                System.out.println("Process " + p.pid + " (memory request: " + request +
                                ") allocated at address " + allocated.start + " with size " + allocated.size);
            }
            //If allocation failed, print a failure message
            else {
                System.out.println("Process " + p.pid + " allocation of size " + request + " failed.");
            }
        }
        //Print header for remaining free memory holes
        System.out.println("Remaining free holes:");
        //For each remaining memory hole, print the hole's details
        for (MemoryHole hole : holes) {
            System.out.println("Start: " + hole.start + ", Size: " + hole.size);
        }
    }

    static int simulatePagingFIFO(int[] pageReferences, int numFrames) {
        //This method simulates paging using the FIFO algorithm
        List<Integer> frames = new ArrayList<>(); //Create a list to represent memory frames
        int pageFaults = 0; //Initialize the page fault counter to 0
        for (int page : pageReferences) { //For each page reference
            if (!frames.contains(page)) { //If the page is not in memory
                pageFaults++; //Increment the page fault counter
                if (frames.size() < numFrames) { //If there is space in memory
                    frames.add(page); //Add the page to the frames
                } else { //If memory is full
                    frames.remove(0); //Remove the oldest page
                    frames.add(page); //Add the new page
                }
            }
        }
        return pageFaults; //Return the total number of page faults
    }

    static int simulatePagingLRU(int[] pageReferences, int numFrames) {
        //This method simulates paging using the LRU algorithm
        List<Integer> frames = new ArrayList<>(); //Create a list to represent memory frames
        int pageFaults = 0; //Initialize the page fault counter to 0
        for (int page : pageReferences) { //For each page reference
            if (!frames.contains(page)) { //If the page is not in memory
                pageFaults++; //Increment the page fault counter
                if (frames.size() < numFrames) { //If there is space in memory
                    frames.add(page); //Add the page
                } else { //If memory is full
                    frames.remove(0); //Remove the least recently used page (simplified approach)
                    frames.add(page); //Add the new page
                }
            } else { //If the page is already in memory
                frames.remove(Integer.valueOf(page)); //Remove it to update its recency
                frames.add(page); //Re-add it to mark it as most recently used
            }
        }
        return pageFaults; //Return the total number of page faults
    }

    static boolean getYesNo(String prompt, Scanner sc) {
        //This helper method prompts the user for a yes/no input
        while (true) { //Loop until valid input is received
            System.out.print(prompt); //Print the prompt
            String input = sc.nextLine().trim().toLowerCase(); //Read and normalize the input
            if (input.equals("y")) { //If the input is "y"
                return true; //Return true
            } else if (input.equals("n")) { //If the input is "n"
                return false; //Return false
            } else { //If the input is invalid
                System.out.println("Invalid option. Please enter 'y' or 'n'."); //Print an error message
            }
        }
    }

    public static void main(String[] args) {
        //This is the main method, the entry point of the program
        Scanner sc = new Scanner(System.in); //Create a Scanner object to read user input
        String filename = "processes.txt"; //Define the filename for the processes file
        Path filePath = Paths.get(filename); //Construct a Path object for the filename
        System.out.println("The processes file is located at: " + filePath.toAbsolutePath()); //Print the absolute file path
        boolean useFile = getYesNo("Do you want to run the program using this file? (y/n): ", sc); //Prompt the user to confirm using the file
        if (!useFile) { //If the user chooses not to use the file
            System.out.println("Exiting program."); //Print an exit message
            sc.close(); //Close the Scanner
            return;
        }
        List<Process> processes = readProcesses(filename); //Read the processes from the file
        if (processes.isEmpty()) { //If no processes were read
            System.out.println("No processes to schedule. Please check your processes.txt file."); //Inform the user
        } else { //If processes are available
            processes.sort(Comparator.comparingInt(p -> p.arrival)); //Sort the processes by arrival time
            if (getYesNo("Run FCFS Scheduling? (y/n): ", sc)) { //Prompt to run FCFS scheduling
                fcfsScheduling(processes); //Execute FCFS scheduling
                System.out.println("\n--------------------\n"); //Print a separator
            }
            if (getYesNo("Run SJF Scheduling? (y/n): ", sc)) { //Prompt to run SJF scheduling
                sjfScheduling(processes); //Execute SJF scheduling
                System.out.println("\n--------------------\n"); //Print a separator
            }
            if (getYesNo("Run Round Robin Scheduling? (y/n): ", sc)) { //Prompt to run Round Robin scheduling
                roundRobinScheduling(processes, 4); //Execute Round Robin scheduling with a time quantum of 4
                System.out.println("\n--------------------\n"); //Print a separator
            }
            if (getYesNo("Run Priority Scheduling? (y/n): ", sc)) { //Prompt to run Priority scheduling
                priorityScheduling(processes); //Execute Priority scheduling
                System.out.println("\n--------------------\n"); //Print a separator
            }
        }
        System.out.println("\nMemory Allocation Simulation:"); //Print header for memory allocation simulation
        simulateMemoryAllocationFirstFit(processes); //Simulate memory allocation using the first-fit strategy
        
        int[] pageRefs = {7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2}; //Define an array of page references
        int numFrames = 3; //Define the number of memory frames
        int fifoFaults = simulatePagingFIFO(pageRefs, numFrames); //Simulate FIFO paging and capture the number of page faults
        int lruFaults = simulatePagingLRU(pageRefs, numFrames); //Simulate LRU paging and capture the number of page faults
        System.out.println("\nPaging Simulation:"); //Print header for paging simulation
        System.out.println("FIFO Page Faults: " + fifoFaults); //Print the number of FIFO page faults
        System.out.println("LRU Page Faults: " + lruFaults); //Print the number of LRU page faults
        sc.close(); //Close the Scanner
    }
}