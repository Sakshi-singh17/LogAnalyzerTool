import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogEntry {
    private String timestamp;
    private String level;
    private String message;

    //constructor
    public LogEntry(String timestamp, String level, String message){
        this.timestamp=timestamp;
        this.level=level;
        this.message=message;
    }

    //getter methods
    public String getTimestamp(){
        return timestamp;
    }
    public String getLevel(){
        return level;
    }
    public String getMessage(){
        return message;
    }

    //converting timestamp string to a Date object to sort it
    public Date getParsedTimeStamp(){
        try{
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return formatter.parse(timestamp);
        }
        catch(ParseException e){
            return new Date(0);
        }
    }

    @Override
    public String toString(){
        return "[" + timestamp +"] " + level + ": " + message;
    }
}
