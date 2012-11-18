public class SMS {
	public long number;
	public String message;
	public long timestamp;
	public long id = 7;
	
//----------Default Constructor----------
	public SMS()
	{
		number = 0;
		timestamp = 0;
		message = "";
		id = 0;
	}//end default constructor
	
//----------Standard Constructor----------
	public SMS(String messageText, String cellNumber, long time)
	{
		message = messageText;
		number = Long.parseLong(cellNumber);
		timestamp = time;
		for (int i=0;i<message.length();i++) {
		    id = id*31+message.charAt(i);
		}
		id = id + number;
	}//end standard constructor

}//end SMS class