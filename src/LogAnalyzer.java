import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogAnalyzer {
    private final String filePath;
    private final Pattern logPattern = Pattern.compile("\\[(.*?)\\]\\s(\\w+):\\s(.+)");
    private final List<LogEntry> logEntries = new ArrayList<>();//stores parsed logs in memory

    //constructor
    public LogAnalyzer(String filePath){
        this.filePath=filePath;
    }

    //reading and parsing logs into objects
    public void loadlogs() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            System.out.println("Parsed Log Entries:\n");
            while ((line = reader.readLine()) != null) {
                Matcher matcher = logPattern.matcher(line);

                if (matcher.matches()) {
                    String timestamp = matcher.group(1);
                    String level = matcher.group(2);
                    String message = matcher.group(3);

                    LogEntry entry = new LogEntry(timestamp, level, message);
                    logEntries.add(entry);
                } else {
                    System.out.println("Invalid Format : " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file : " + e.getMessage());
        }
    }

    //filtering logs based on level
    public List<LogEntry> getFilteredLogs(String level){
        if(level.equalsIgnoreCase("ALL")){
            return logEntries;
        }
        List<LogEntry> filtered = new ArrayList<>();
        for(LogEntry entry : logEntries){
            if(entry.getLevel().equalsIgnoreCase(level)){
                filtered.add(entry);
            }
        }
        return filtered;
    }

    //storing log level counts
    public Map<String,Integer> countByLevel(){
        Map<String, Integer> levelCount = new HashMap<>();
        for(LogEntry entry : logEntries){
            String level = entry.getLevel();
            levelCount.put(level,levelCount.getOrDefault(level,0)+1);
        }
        return levelCount;
    }

    //maintaining count of error messages
    public Map<String,Integer>countErrorMessages(){
        Map<String,Integer>errorCount = new HashMap<>();
        for(LogEntry entry : logEntries){
            if(entry.getLevel().equalsIgnoreCase("ERROR")){
                String message = entry.getMessage();
                errorCount.put(message,errorCount.getOrDefault(message,0)+1);
            }
        }
        return errorCount;
    }

    //search by keyword feature
    public List<LogEntry>searchLogs(String keyword){
        List<LogEntry>result = new ArrayList<>();
        for(LogEntry entry : logEntries){
            if(entry.getMessage().toLowerCase().contains(keyword.toLowerCase())){
                result.add(entry);
            }
        }
        return result;
    }

    //sorting method to sort by timestamp
    public List<LogEntry>getSortedLogs(boolean ascending){
        List<LogEntry>sorted = new ArrayList<>(logEntries);

        sorted.sort((e1,e2)->{
            Date d1 = e1.getParsedTimeStamp();
            Date d2 = e2.getParsedTimeStamp();
            return ascending? d1.compareTo(d2):d2.compareTo(d1);
        });
        return sorted;
    }

    //export method to write the logs in a file
    public boolean exportLogsToFile(List<LogEntry>logs,String outputPath){
        try(PrintWriter writer = new PrintWriter(outputPath)){
            for(LogEntry entry : logs){
                writer.println(entry.toString());
            }
            return true;
        }
        catch(IOException e){
            System.out.println("Error writing to file: "+e.getMessage());
            return false;
        }
    }

    //grouping logs by date(yyyy-MM-dd)
    public Map<String,List<LogEntry>>groupLogsByDate(){
        Map<String,List<LogEntry>> grouped = new TreeMap<>();

        for(LogEntry entry : logEntries){
            //extracting date
            String dateOnly = entry.getTimestamp().split(" ")[0];
            grouped.putIfAbsent(dateOnly,new ArrayList<>());
            grouped.get(dateOnly).add(entry);
        }
        return grouped;
    }
}
