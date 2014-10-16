package udo.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;

import org.junit.Test;

import com.sun.corba.se.impl.oa.poa.ActiveObjectMap.Key;

import udo.main.Parser;
import udo.util.parser.ParserAdd;
import udo.util.parser.ParserEdit;
import udo.util.shared.Command;
import udo.util.shared.InputData;
import udo.util.shared.ParsingStatus;
import udo.util.shared.Constants.Keys;

public class uDoParserUnitTest {

	Parser p = new Parser();
	ParserAdd addActivity = new ParserAdd();
	ParserEdit editActivity = new ParserEdit();
	
	String testEvent1 = "add #date with #jiawei from 30/9/14 11:09pm to 3/8/25 6:45pm";
	String testEvent2 = "add #cs2010 #homework finish it! from 12/12/13 11:30am to 11/3/14 5:45am";
	String testEvent3 = "add #test only from 10am to 1pm"; // returns current date and time for both start and end time
	
	String testTask1 = "add make #friendship #bands by 12/12/13 9:31am";
	String testTask2 = "add finish homework by 11:30am";
	String testTask3 = "add do #critical reflections by 18/10";
	String testTask4 = "add reflections by Saturday"; // return today's date. Unable to take in day yet
	
	String testPlan1 = "add vending #machines no money";
	String testPlan2 = "add call meow mi later";
	
	String testEditTitle1 = "edit title go to school";
	String testEditTitle2 = "edit 12345 title #school is #fun!";
	
	String testEditStartTime1 = "edit 12345 start time 3:15am";
	String testEditStartTime2 = "edit 12345 start time 3:00am";
	
	String testEditEndTime1 = "edit 12345 end time 4:14pm";
	String testEditEndTime2 = "edit 12345 end time 4:00pm";
	
	String testEditStartDate1 = "edit 12345 start date 12/10/14";
	String testEditStartDate2 = "edit 12345 start date 1/1/11";
	
	String testEditEndDate1 = "edit 12345 end date 12/10/14";
	String testEditEndDate2 = "edit 12345 end date 12/10/14";
	
	String testEditDueTime1 = "edit 12345 due date 7:12pm";
	String tsetEditDueTime2 = "edit 12345 due date 7pm";
	
	String testEditDueDate1 = "edit 12345 due date 12/10/14";
	String testEditDueDate2 = "edit 12345 due date 1/1/11";
	
	@Test
	public void testEdit() {
		InputData data = p.getInputData(testEditTitle2);
	}
	
	@Test
	public void testStartDate() {
		InputData data = editActivity.edit(Command.EDIT, testEditStartDate2);
		ParsingStatus status = data.getStatus();
		Calendar date = (Calendar) data.get(Keys.VALUE);
		
		int day = date.get(Calendar.DAY_OF_MONTH);
		int month = date.get(Calendar.MONTH);
		int year = date.get(Calendar.YEAR);
		
		assertEquals(ParsingStatus.SUCCESS, status);
		assertEquals(1, day);
		assertEquals(0, month);
		assertEquals(2011, year);
	}
	
	@Test
	public void testEndTime() {
		InputData data = editActivity.edit(Command.EDIT, testEditEndTime2);
		ParsingStatus status = data.getStatus();
		Calendar time = (Calendar) data.get(Keys.VALUE);
		int hour = time.get(Calendar.HOUR);
		int mins = time.get(Calendar.MINUTE);
		
		assertEquals(ParsingStatus.SUCCESS, status);
		assertEquals(4, hour);
		assertEquals(0, mins);
	}
	
	@Test
	public void testStartTime() {
		InputData data = editActivity.edit(Command.EDIT, testEditStartTime2);
		ParsingStatus status = data.getStatus();
		Calendar time = (Calendar) data.get(Keys.VALUE);	
		int hour = time.get(Calendar.HOUR);
		int mins = time.get(Calendar.MINUTE);
		
		assertEquals(ParsingStatus.SUCCESS, status);
		assertEquals(3, hour);
		assertEquals(0, mins);
	}
	
	@Test
	public void testEditTitle() {
		InputData data = editActivity.edit(Command.EDIT, testEditTitle1);
		ParsingStatus status = data.getStatus();
		assertEquals(ParsingStatus.FAIL, status);
		
		data = editActivity.edit(Command.EDIT, testEditTitle2);
		status = data.getStatus();
		Object title = data.get(Keys.VALUE);
		Object tags = data.get(Keys.HASHTAGS);
		
		assertEquals(ParsingStatus.SUCCESS, status);
		assertEquals("#school is #fun!", title);
		assertEquals("[school, fun!]", tags.toString());
	}
	
	@Test
	public void testAddingEvent() {
		InputData event = p.add(Command.ADD, testEvent3);
		ParsingStatus status = event.getStatus();
		Command type = event.getCommand();
		
		assertEquals(ParsingStatus.SUCCESS, status);
		assertEquals(Command.ADD_EVENT, type);
		assertFalse(type == Command.ADD_TASK);
	}
	
	@Test
	public void testAddingTask() {
		InputData task = p.add(Command.ADD, testTask4);
		ParsingStatus status = task.getStatus();
		Command type = task.getCommand();
		Calendar c = (Calendar) task.get(Keys.DUE);
		
		assertEquals(ParsingStatus.SUCCESS, status);
		assertEquals(Command.ADD_TASK, type);
		//assertEquals("", c.toString());
	}

	@Test
	public void testAddingPlan() {
		InputData plan = p.add(Command.ADD, testPlan2);
		ParsingStatus status = plan.getStatus();
		Command type = plan.getCommand();
		
		assertEquals(ParsingStatus.SUCCESS, status);
		assertEquals(Command.ADD_PLAN, type);
	}
	
	@Test
	public void testSetFirstTimeAndDate() {
		Calendar start = addActivity.setFirstTimeAndDate(testTask1);
		int day = start.get(Calendar.DAY_OF_MONTH);
		int month = start.get(Calendar.MONTH);
		int year = start.get(Calendar.YEAR);
		int hour = start.get(Calendar.HOUR);
		int mins = start.get(Calendar.MINUTE);
		
		assertEquals(12, day);
		assertEquals(11, month);
		assertEquals(2013, year);
		assertEquals(9, hour); // noon and mid-night are represented by 0
		assertEquals(31, mins);
	}
	
	@Test
	public void testSetSecondTimeAndDate() {
		Calendar start = addActivity.setSecondTimeAndDate(testEvent1);
		int day = start.get(Calendar.DAY_OF_MONTH);
		int month = start.get(Calendar.MONTH);
		int year = start.get(Calendar.YEAR);
		int hour = start.get(Calendar.HOUR);
		int mins = start.get(Calendar.MINUTE);
		
		assertEquals(3, day);
		assertEquals(7, month);
		assertEquals(2025, year);
		assertEquals(6, hour); // noon and mid-night are represented by 0
		assertEquals(45, mins);
	}
	
	@Test
	public void testAdd() {
		InputData activity1 = addActivity.add(Command.ADD, testEvent1);
		ParsingStatus status1 = activity1.getStatus();
		Object title = activity1.get(Keys.TITLE);
		Object hashtags = activity1.get(Keys.HASHTAGS);
		Calendar startEvent = (Calendar) activity1.get(Keys.START);
		Calendar endEvent = (Calendar) activity1.get(Keys.END);
		
		int day = startEvent.get(Calendar.DAY_OF_MONTH);
		int month = startEvent.get(Calendar.MONTH);
		int year = startEvent.get(Calendar.YEAR);
		int hour = startEvent.get(Calendar.HOUR);
		int mins = startEvent.get(Calendar.MINUTE);
		
		assertEquals(ParsingStatus.SUCCESS, status1);
		assertEquals(Command.ADD_EVENT, activity1.getCommand());
		assertEquals("date with jiawei", title);
		assertEquals("[date, jiawei]", hashtags.toString());
		
		assertEquals(30, day);
		assertEquals(8, month);
		assertEquals(2014, year);
		assertEquals(11, hour);
		assertEquals(9, mins);
		
		day = endEvent.get(Calendar.DAY_OF_MONTH);
		month = endEvent.get(Calendar.MONTH);
		year = endEvent.get(Calendar.YEAR);
		hour = endEvent.get(Calendar.HOUR);
		mins = endEvent.get(Calendar.MINUTE);
		
		assertEquals(3, day);
		assertEquals(7, month);
		assertEquals(2025, year);
		assertEquals(6, hour);
		assertEquals(45, mins);
		
		InputData activity2 = addActivity.add(Command.ADD, testTask2);
		ParsingStatus status2 = activity2.getStatus();
		title = activity2.get(Keys.TITLE);
		hashtags = activity2.get(Keys.HASHTAGS);
		Calendar due = (Calendar) activity2.get(Keys.DUE);
		
		assertEquals(ParsingStatus.SUCCESS, status2);
		assertEquals(Command.ADD_TASK, activity2.getCommand());
		assertEquals("finish homework", title);
		assertEquals("[]", hashtags.toString());
		
		day = due.get(Calendar.DAY_OF_MONTH);
		month = due.get(Calendar.MONTH);
		year = due.get(Calendar.YEAR);
		hour = due.get(Calendar.HOUR);
		mins = due.get(Calendar.MINUTE);
		
		assertEquals(16, day);
		assertEquals(9, month);
		assertEquals(2014, year);
		assertEquals(11, hour);
		assertEquals(30, mins);
		
		InputData activity3 = addActivity.add(Command.ADD, testPlan1);
		ParsingStatus status3 = activity3.getStatus();
		title = activity3.get(Keys.TITLE);
		hashtags = activity3.get(Keys.HASHTAGS);
		
		assertEquals(ParsingStatus.SUCCESS, status3);
		assertEquals(Command.ADD_PLAN, activity3.getCommand());
		assertEquals("vending machines no money", title);
		assertEquals("[machines]", hashtags.toString());
	}
	
}
