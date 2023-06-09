import java.time.LocalDate;
import java.time.LocalTime;

public class ProcessLogModel {

    String date;
    String time;
    String actionCode;
    String requestType;
    String barCode;
    String everything;


    ProcessLogModel()
    {
        date = LocalDate.now().toString();
        time = LocalTime.now().toString();
        actionCode = "ABCD";
        requestType = "Some ting";
        barCode = "12345678";
        everything = date + " - " + time + " - " + actionCode+ " - " + requestType+ " - " + barCode ;
    }

    ProcessLogModel(String s1, String s2, String s3)
    {
        date = LocalDate.now().toString();
        time = LocalTime.now().toString();
        actionCode = s1;
        requestType = s2;
        barCode = s3;

        everything = date + " - " + time + " - " + actionCode+ " - " + requestType+ " - " + barCode ;
    }
}
