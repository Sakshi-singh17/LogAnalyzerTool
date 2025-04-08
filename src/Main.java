import java.util.List;
import java.util.Map;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        String filePath="logs/sample.log";
        Scanner scanner = new Scanner(System.in);
        //creating the LogAnalyzer object
        LogAnalyzer analyzer = new LogAnalyzer(filePath);
        //loading the log entries from file and parsing into java objects
        analyzer.loadlogs();

        //creating user menu
        while(true){
            System.out.println("\n===== Log Analyzer Menu =====");
            System.out.println("1. View All Logs");
            System.out.println("2. Filter Logs by Level");
            System.out.println("3. Sort Logs by Timestamp");
            System.out.println("4. Count Logs by Level");
            System.out.println("5. Frequent Error Messages");
            System.out.println("6. Search Logs by Keyword");
            System.out.println("7. Export Logs to file");
            System.out.println("8. Group Logs by Date");
            System.out.println("9. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); //consuming newline

            switch(choice){
                case 1://view all logs
                    for(LogEntry entry : analyzer.getFilteredLogs("ALL")){
                        System.out.println(entry);
                    }
                    break;
                case 2://filter logs by level
                    System.out.println("Enter log level to filter(INFO / WARNING / ERROR )");
                    String level = scanner.nextLine();
                    List<LogEntry> filteredLogs = analyzer.getFilteredLogs(level);
                    System.out.println("\nFiltered Log Entries :\n");
                    for(LogEntry entry : filteredLogs){
                        System.out.println(entry);
                    }
                    break;
                case 3://sort logs by timestamp
                    System.out.println("Type 'asc' for ascending and 'desc' for descending: ");
                    String order = scanner.nextLine().trim();
                    boolean ascending = order.equalsIgnoreCase("asc");

                    List<LogEntry>sortedLogs = analyzer.getSortedLogs(ascending);
                    System.out.println("\n Logs sorted by timestamp (" + (ascending ? "Oldest First" : "Latest First")+"):\n");
                    for(LogEntry entry : sortedLogs){
                        System.out.println(entry);
                    }
                case 4://count logs by level
                    System.out.println("Log Level Summary : ");
                    Map<String, Integer> levelCount = analyzer.countByLevel();
                    for(String lvl : levelCount.keySet()){
                        System.out.println(lvl + ": " + levelCount.get(lvl));
                    }
                    break;
                case 5://frequent error messages
                    System.out.println("\n Frequent Error Messages: ");
                    Map<String, Integer> errorSummary = analyzer.countErrorMessages();
                    if(errorSummary.isEmpty()){
                        System.out.println("No ERROR logs found.");
                    }
                    else{
                        for(String msg : errorSummary.keySet()){
                            System.out.println("\"" + msg +"\" - " + errorSummary.get(msg) +" time(s)");
                        }
                    }
                    break;
                case 6://search logs by keyword
                    System.out.println("Enter keyword to search: ");
                    String keyword = scanner.nextLine().trim();
                    List<LogEntry>matchedLogs = analyzer.searchLogs(keyword);

                    //printing the search results
                    if(matchedLogs.isEmpty()){
                        System.out.println("No logs found with the keyword \"" + keyword + "\"");
                    }
                    else{
                        for(LogEntry entry : matchedLogs){
                            System.out.println(entry);
                        }
                    }
                    break;
                case 7:
                    System.out.println("Enter log level to export (INFO/WARN/ERROR/ALL):");
                    String exportLevel = scanner.nextLine().toUpperCase();
                    List<LogEntry>logsToExport = analyzer.getFilteredLogs(exportLevel);
                    System.out.println("Enter output file path (e.g., exported_logs.txt): ");
                    String outputFile = scanner.nextLine();

                    boolean success = analyzer.exportLogsToFile(logsToExport,outputFile);
                    if(success){
                        System.out.println("Logs exported to: "+outputFile);
                    }
                    break;
                case 8://group logs by date
                    Map<String, List<LogEntry>>groupedLogs = analyzer.groupLogsByDate();
                    for(String date : groupedLogs.keySet()){
                        System.out.println(date);
                        for(LogEntry entry : groupedLogs.get(date)){
                            System.out.println("  - "+entry);
                        }
                        System.out.println();
                    }
                    break;
                case 9://exit
                    System.out.println("Exiting Log Analyzer. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("❗ Invalid choice. Please try again.");
            }
        }
    }
}