import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.techventus.server.voice.Voice;

public class GVStacker {
	
//---------------------------------------------	
//---------------------Main--------------------
//---------------------------------------------
	public static void main(String[] args) throws InterruptedException, IOException {
		//----------Variables----------
		Object rawSMSData[];
		SMS threadData[];
		Vector<SMS> allMessages = new Vector<SMS>();
		Vector<SMS> messageBuffer = new Vector<SMS>();
		Vector<SMS> tempVector;
		Collection c;
		String textLine = "";
		int displayedMessages = 0;
		int smsThreads = 0;
		int bufferSize = 0;
		boolean match = false;
		
		//----------Initialization----------
		Voice voice = new Voice("eriemakerspace", "wrE$ur$7");
		
		//----------Loop----------
		while (true) {
			c = voice.getSMSThreads();
			rawSMSData = c.toArray();
			smsThreads = rawSMSData.length;
			tempVector = new Vector<SMS>();
			
			//----------Extract Messages from Threads----------
			for (int i=0;i<smsThreads;i++) {
				textLine = rawSMSData[i].toString();
				threadData = getSMS(textLine);
				for(int j=0;j<threadData.length;j++){
					tempVector.add(threadData[j]);
				}
				//System.out.println(textLine);
			}//end Get Threads
			
			//----------Initialize allMessages----------
			if (allMessages.size() == 0){allMessages.equals(tempVector.clone());}
			
			//----------Compare and Add Differences----------
			for(int i=0;i<tempVector.size();i++){
				for(int j=0;j<allMessages.size();j++){
					if (allMessages.elementAt(j).id == tempVector.elementAt(i).id){
						match = true;
						//System.out.println("Match! " + allMessages.elementAt(j).message + " == " + tempVector.elementAt(i).message);
						j = allMessages.size();
					}
				}
				if (match == false){
					allMessages.add(tempVector.elementAt(i));
					//System.out.println("New Message: " + tempVector.elementAt(i).message + "Time: " + tempVector.elementAt(i).timestamp);
				}
				match = false;
			}//end Compare and Add Differences
			
			//----------Sort allMessages----------
			sortVector(allMessages);
			
			//----------Initialize displayedMessages----------
			if (displayedMessages == 0){
				displayedMessages = allMessages.size();
			}
			
			//----------Create and Display Buffer----------
			bufferSize = allMessages.size() - displayedMessages;
			System.out.println("previous disp: " + displayedMessages + " curr vect size: " + allMessages.size() );
			if (bufferSize>0){
				
				for(int i=0;i<bufferSize;i++){
					messageBuffer.add(allMessages.elementAt(i));
				}
				sortVector(messageBuffer);
				writeTexts(messageBuffer);
				
				/*
				for(int i=0;i<allMessages.size();i++){
					System.out.println(i + ": "+ allMessages.elementAt(i).timestamp + " " + allMessages.elementAt(i).message + " Num: " + allMessages.elementAt(i).number);
				}
				System.out.println("==========End Of Messages==========");
				*/
			}//end Create and Display Buffer
			
			displayedMessages = allMessages.size();
			messageBuffer.clear();
			Thread.sleep(1000);
			
		}// end Loop
	}//end Main

	
	
//----------------------------------------------
//--------------------getSMS--------------------
//----------------------------------------------
	public static SMS[] getSMS(String text)
	{
		SMS messageArray[];
		String messageText = "";
		String cellNumber = "";
		String dateString = "";
		char ch = ' ';
		int numOfTexts = 0;
		int startPos = 0;
		int k = 0;
		long timeStamp = 0;
		
		//----------Count Number of Messages----------
		while (startPos != 4) {
			startPos = text.indexOf("text=", startPos) + 5;
			if (startPos != 4){numOfTexts++;}
		}
		messageArray = new SMS[numOfTexts];
		
		//----------Fill messageArray----------
		//startPos = text.indexOf("listsms=") + 8;
		for(int i=0;i<messageArray.length;i++){
			//----------Extract timeStamp----------
				startPos = text.indexOf("dateTime=", startPos) + 9;
				while(ch != ','){
					ch = text.charAt(startPos + k);
					dateString = dateString + ch;
					k++;
				}
				timeStamp = parseDate(dateString) - i;
			k = 0;
			ch = ' ';
			//----------Extract cellNumber----------
			startPos = text.indexOf("number=+", startPos) + 8;
			for(int j=0;j<11;j++){
				ch = text.charAt(startPos + j);
				cellNumber = cellNumber + ch;
			}
			ch = ' ';
			//----------Extract messageText----------
			startPos = text.indexOf("text=", startPos) + 5;
			do {
				ch = text.charAt(startPos + k);
				messageText = messageText + ch;
				k++;
			} while(ch != ']');
			messageText = removeTail(messageText);
			
			//----------Create and Insert SMS----------
			messageArray[i] = new SMS(messageText, cellNumber, timeStamp);

			//----------Clear Vars for Next Loop----------
			messageText = "";
			cellNumber = "";
			dateString = "";
			k = 0;
			ch = ' ';
		}//end fill messageArray while loop
		
		//----------Debugging Output----------
		/*
		for(int i=0;i<messageArray.length;i++){
			System.out.println(i + ": "+ messageArray[i].timestamp + " " + messageArray[i].message + " Num: " + messageArray[i].number);
		}
		*/
		
		return messageArray;
		
	}//end getSMS
	
	
	
//----------------------------------------------------	
//--------------------displayTexts--------------------
//----------------------------------------------------
	public static void displayTexts(Vector<SMS> buffer) throws InterruptedException, IOException{
		for(int i=buffer.size();i<0;i--){
			System.out.println("Display: " + buffer.elementAt(i).message + " Time: " + buffer.elementAt(i).timestamp);
			Thread.sleep(1000);
		}
	}//end displayTexts

	
	
//-------------------------------------------------
//--------------------writeTexts-------------------
//-------------------------------------------------
	public static void writeTexts(Vector<SMS> buffer) throws InterruptedException, IOException
	{
		try
		{
			FileWriter fstream = new FileWriter("texts.txt", true);
			BufferedWriter out = new BufferedWriter(fstream);
			for(int i=0;i<buffer.size();i++)
			{
				out.write(buffer.elementAt(i).message);
				out.newLine();
				System.out.println("Write: " + buffer.elementAt(i).message);
			}
			out.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
			writeTexts(buffer);
		}
		
	}
	
	
	
//---------------------------------------------------
//---------------------sortVector--------------------
//---------------------------------------------------
	public static void sortVector(Vector<SMS> vector){
		SMS tempSMS;
		for(int i=0;i<vector.size();i++){
			for(int j=1;j<(vector.size()-i);j++){
				if(vector.elementAt(j-1).timestamp < vector.elementAt(j).timestamp){
					tempSMS = vector.elementAt(j-1);
			        vector.set(j-1, vector.elementAt(j));
				    vector.set(j, tempSMS);
				}
			}
		}
		/*if (vector.size() > 1 ){
			tempSMS = vector.elementAt(0);
			vector.removeElementAt(0);
			vector.add(0, vector.elementAt(1));
			vector.removeElementAt(1);
			vector.add(1, tempSMS);
		}*/
	}//end sortVector
	
	
	
//--------------------------------------------------
//---------------------parseDate--------------------
//--------------------------------------------------
	public static long parseDate(String dateString){
		long numDate;
		DateFormat sdf = new SimpleDateFormat("E MMM DD hh:mm:ss z yyyy");
		Date smsDate = new Date();
		
		try {
			smsDate = sdf.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		numDate = smsDate.getTime();
	
		return numDate;
	}
	
	
	
//--------------------------------------------------	
//--------------------removeTail--------------------
//--------------------------------------------------
	public static String removeTail(String str) {

		 if (str.charAt(str.length()-1)==']')
		 {
		  str = str.substring(0, str.length()-1);
		  return str;
		 }
		 else
		 {
		  return str;
		 }
		}
}//end class