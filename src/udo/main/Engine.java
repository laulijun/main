package udo.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import udo.util.engine.Cache;
import udo.util.engine.FileManager;
import udo.util.engine.Runner;
import udo.util.engine.RunnerAdd;
import udo.util.engine.UndoBin;
import udo.util.exceptions.WritingToStorageFileException;
import udo.util.shared.Command;
import udo.util.shared.Constants.Keys;
import udo.util.shared.EditField;
import udo.util.shared.ExecutionStatus;
import udo.util.shared.InputData;
import udo.util.shared.ItemData;
import udo.util.shared.ItemType;
import udo.util.shared.ListQuery;
import udo.util.shared.OutputData;
import udo.util.shared.ParsingStatus;

public class Engine {
	
	private static Engine ENGINE_INSTANCE = null;
	
	private FileManager mFileManager;
	private Cache mCache;
	private UndoBin mUndoBin;

	/**
	 * private constructor. to instantiate this class, use the static getInstance() method.
	 */
	private Engine() {
		mFileManager = new FileManager();
		mCache = new Cache();
		mUndoBin = new UndoBin();
	}
	
	/**
	 * This method returns the Engine. It restricts the caller to only one instance
	 * of the Engine. This follows the Singleton Pattern.
	 * @return The one and only instance of the Engine.
	 */
	public static Engine getInstance() {
		if (ENGINE_INSTANCE == null) {
			ENGINE_INSTANCE = new Engine();
		}
		ENGINE_INSTANCE.loadFile();
		return ENGINE_INSTANCE;
	}

	// ****** public methods ****** //
	
	/**
	 * This method returns an arraylist of events that occur on the specified day frame.
	 * @param todayCal the calendar object containing the specified day
	 * @return an arraylist of events that occur on the specified day
	 */
	public ArrayList<ItemData> getTodayScreenItems(Calendar todayCal) {
		return mCache.getAllEventsOn(todayCal);
	}
	
	/**
	 * This method returns an arraylist of tasks that occur between two dates.
	 * @param fromCal one of the dates
	 * @param toCal another one of the dates.
	 * @return an arraylist of tasks that occur between the dates.
	 */
	public ArrayList<ItemData> getTodoScreenItems(Calendar fromCal, Calendar toCal) {
		return mCache.getAllTasksBetween(fromCal, toCal);
	}

	/**
	 * This method returns an outputdata object that contains the execution result
	 * and additional information.
	 * @param input The inputdata object to execute 
	 * @return the outputdata object containing execution result.
	 */
	public OutputData execute(InputData input) {
		// precondition, input cannot be null
		assert (input != null);
		
		Command cmd = input.getCommand();
		ParsingStatus parsingStatus = input.getStatus();
		//preconditions, input must have these components
		assert (cmd != null);
		assert (parsingStatus != null);
		
		OutputData output = null;
		// check if parsing success or not
		if (parsingStatus.equals(ParsingStatus.FAIL)) {
			output = new OutputData(cmd, 
					ParsingStatus.FAIL,
					ExecutionStatus.NULL);
			return output;
		}
		
		Runner commandRunner = null;
		
		// decide what function to run.
		switch (cmd) {
			case ADD_EVENT :
			case ADD_TASK :
			case ADD_PLAN :
				commandRunner = new RunnerAdd(input, mUndoBin, mCache);
				break;
			case EDIT :
				output = runEdit(input);
				break;
			case LIST :
				output = runList(input);
				break;
			case DELETE :
				output = runDelete(input);
				break;
			case UNDO :
				output = runUndo(input);
				break;
			case SAVE :
				output = runSave(input);
				break;
			case EXIT :
				output = runExit(input);
				break;
			default:
				output = new OutputData(cmd, 
						ParsingStatus.SUCCESS,
						ExecutionStatus.NULL);
		}
		
		if (commandRunner != null) {
			output = commandRunner.run();
		}
		
		// postcondition
		assert (output != null);
		output.setParsingStatus(ParsingStatus.SUCCESS);
		return output;
	}
	
	// ********* methods that execute the commands ******* //
	
	private OutputData runList(InputData input) {
		/*
		 * 1. build a list of all items 2. select only the items we want. 3.
		 * sort the items according to date/time 4. put the items in the
		 * outputdata. 5. return output
		 */
		ArrayList<ItemData> listOfAllItems = mCache.getAllItems();
		ArrayList<ItemData> result;

		ListQuery query = (ListQuery) input.get(Keys.QUERY_TYPE);
		String queryString = "";
		switch (query) {
		case ALL:
			result = listOfAllItems;
			break;
		case SINGLE_HASHTAG:
			queryString = (String) input.get(Keys.HASHTAG);
			result = trimList(listOfAllItems, queryString);
			break;
		default:
			return null;
		}

		OutputData output = new OutputData(Command.LIST,
				ParsingStatus.SUCCESS, 
				ExecutionStatus.SUCCESS);
		output.put(Keys.QUERY_TYPE, query);
		output.put(Keys.QUERY, queryString);
		output.put(Keys.ITEMS, result);

		return output;
	}

	private OutputData runDelete(InputData inputData) {
		int uid = (int) inputData.get(Keys.UID);
		ItemData deletedItem = mCache.getItem(uid);
		boolean deleteOK = mCache.deleteItem(uid);
		OutputData output;
		if (deleteOK) {
			output = new OutputData(Command.DELETE, 
					ParsingStatus.SUCCESS, 
					ExecutionStatus.SUCCESS);
			output.put(Keys.ITEM, deletedItem);
			storeAddDeleteUndo(Command.DELETE, deletedItem);
		} else {
			output = new OutputData(Command.DELETE, 
					ParsingStatus.SUCCESS, 
					ExecutionStatus.FAIL);
		}

		return output;
	}

	private OutputData runEdit(InputData inputData) {
		int uid = (int) inputData.get(Keys.UID);
		ItemData itemToEdit = mCache.getItem(uid);
		EditField field = (EditField) inputData.get(Keys.FIELD);
		Object value = inputData.get(Keys.VALUE);
		ExecutionStatus eStatus = ExecutionStatus.FAIL;
		switch (field) {
			case DUE_DATE :
				eStatus = runEditDueDate(itemToEdit, (Calendar) value);
				break;
			case DUE_TIME :
				eStatus = runEditDueTime(itemToEdit, (Calendar) value);
				break;
			case END_DATE :
				eStatus = runEditEndDate(itemToEdit, (Calendar) value);
				break;
			case END_TIME :
				eStatus = runEditEndTime(itemToEdit, (Calendar) value);
				break;
			case START_DATE :
				eStatus = runEditStartDate(itemToEdit, (Calendar) value);
				break;
			case START_TIME :
				eStatus = runEditStartTime(itemToEdit, (Calendar) value);
				break;
			case TITLE :
				eStatus = runEditTitle(itemToEdit, (String) value);
				break;
			default:
				eStatus = ExecutionStatus.FAIL;
		}
		OutputData output = new OutputData(Command.EDIT, 
				ParsingStatus.SUCCESS,
				eStatus);
		output.put(Keys.ITEM, itemToEdit);
		return output;
	}

	private ExecutionStatus runEditTitle(ItemData item, String title) {
		String oldTitle = (String) item.get(Keys.TITLE);
		item.put(Keys.TITLE, title);
		storeEditUndo(EditField.TITLE, oldTitle);
		return ExecutionStatus.SUCCESS;
	}
	
	private ExecutionStatus runEditDueTime(ItemData item, Calendar timeCal) {
		if (item.getItemType() != ItemType.TASK) {
			return ExecutionStatus.FAIL;
		}
		Calendar dueCal = (Calendar) item.get(Keys.DUE);
		Calendar calToStore = (Calendar) dueCal.clone();
		setTime(dueCal, timeCal);
		storeEditUndo(EditField.DUE_TIME, calToStore);
		return ExecutionStatus.SUCCESS;
	}
	
	private ExecutionStatus runEditStartTime(ItemData item, Calendar timeCal) {
		if (item.getItemType() != ItemType.EVENT) {
			return ExecutionStatus.FAIL;
		}
		Calendar startCal = (Calendar) item.get(Keys.START);
		Calendar calToStore = (Calendar) startCal.clone();
		setTime(startCal, timeCal);
		storeEditUndo(EditField.START_TIME, calToStore);
		return ExecutionStatus.SUCCESS;
	}
	
	private ExecutionStatus runEditEndTime(ItemData item, Calendar timeCal) {
		if (item.getItemType() != ItemType.EVENT) {
			return ExecutionStatus.FAIL;
		}
		Calendar endCal = (Calendar) item.get(Keys.END);
		Calendar calToStore = (Calendar) endCal.clone();
		setTime(endCal, timeCal);
		storeEditUndo(EditField.END_TIME, calToStore);
		return ExecutionStatus.SUCCESS;
	}

	private ExecutionStatus setTime(Calendar itemCal, Calendar timeCal) {
		itemCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
		itemCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
		return ExecutionStatus.SUCCESS;
	}

	private ExecutionStatus runEditDueDate(ItemData item, Calendar dateCal) {
		if (item.getItemType() != ItemType.TASK) {
			return ExecutionStatus.FAIL;
		}
		Calendar dueCal = (Calendar) item.get(Keys.DUE);
		Calendar calToStore = (Calendar) dueCal.clone();
		setDate(dueCal, dateCal);
		storeEditUndo(EditField.DUE_DATE, calToStore);
		return ExecutionStatus.SUCCESS;
	}

	private ExecutionStatus runEditEndDate(ItemData item, Calendar dateCal) {
		if (item.getItemType() != ItemType.EVENT) {
			return ExecutionStatus.FAIL;
		}
		Calendar dueCal = (Calendar) item.get(Keys.END);
		Calendar calToStore = (Calendar) dueCal.clone();
		setDate(dueCal, dateCal);
		storeEditUndo(EditField.END_DATE, calToStore);
		return ExecutionStatus.SUCCESS;
	}
	
	private ExecutionStatus runEditStartDate(ItemData item, Calendar dateCal) {
		if (item.getItemType() != ItemType.EVENT) {
			return ExecutionStatus.FAIL;
		}
		Calendar dueCal = (Calendar) item.get(Keys.START);
		Calendar calToStore = (Calendar) dueCal.clone();
		setDate(dueCal, dateCal);
		storeEditUndo(EditField.START_DATE, calToStore);
		return ExecutionStatus.SUCCESS;
	}

	private void setDate(Calendar itemCal, Calendar dateCal) {
		itemCal.set(Calendar.YEAR, dateCal.get(Calendar.YEAR));
		itemCal.set(Calendar.MONTH, dateCal.get(Calendar.MONTH));
		itemCal.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH));
	}

	private OutputData runUndo(InputData inputData) {
		/*
		 * store the inputdata to be executed. so here just execute that
		 * inputdata.
		 */
		InputData undoInput = mUndoBin.getInputData();
		return execute(undoInput);
	}

	private OutputData runSave(InputData inputData) {
		OutputData output = new OutputData(Command.SAVE,
				ParsingStatus.SUCCESS, 
				ExecutionStatus.SUCCESS);
		try {
			writeCacheToFile();
		} catch (WritingToStorageFileException e) {
			output.setExecutionStatus(ExecutionStatus.FAIL);
		}
		
		return output;
	}

	private OutputData runExit(InputData inputData) {
		runSave(null);
		OutputData output = new OutputData(Command.EXIT,
				ParsingStatus.SUCCESS, 
				ExecutionStatus.SUCCESS);
		return output;
	}

	// ********* private abstracted helper methods ******* //

	private void storeAddDeleteUndo(Command previousCommand, ItemData previousItem) {
		InputData undoInput;
		switch (previousCommand) {
			case ADD_EVENT :
				undoInput = new InputData(Command.DELETE);
				undoInput.put(Keys.UID, previousItem.get(Keys.UID));
				break;
			case ADD_TASK :
				undoInput = new InputData(Command.DELETE);
				undoInput.put(Keys.UID, previousItem.get(Keys.UID));
				break;
			case ADD_PLAN :
				undoInput = new InputData(Command.DELETE);
				undoInput.put(Keys.UID, previousItem.get(Keys.UID));
				break;
			case DELETE :
				ItemType previousItemType = previousItem.getItemType();
				// decide which kind of command based on the kind of item deleted.
				switch (previousItemType) {
					case EVENT :
						undoInput = new InputData(Command.ADD_EVENT);
						break;
					case TASK :
						// TODO
						undoInput = new InputData(Command.ADD_TASK);
						break;
					case PLAN :
						// TODO
						undoInput = new InputData(Command.ADD_PLAN);
						break;
					default:
						// should not
						return;
				}
				// copy the data in the item to the inputdata.
				// so that the add command can add like it came from the parser
				// it will also copy the uid field
				// but it will be ignored in the add execution.
				for (String key : previousItem.getKeys()) {
					undoInput.put(key, previousItem.get(key));
				}
				break;
			default:
				// do nothing. this shouldnt happen
				return;

		}
		undoInput.setParsingStatus(ParsingStatus.SUCCESS);
		mUndoBin.putInputData(undoInput);
	}

	private void storeEditUndo(EditField field, Object oldValue) {
		InputData undoInput = new InputData(Command.EDIT, ParsingStatus.SUCCESS);
		undoInput.put(Keys.FIELD, field);
		undoInput.put(Keys.VALUE, oldValue);
		mUndoBin.putInputData(undoInput);
	}

	private boolean loadFile() {
		mCache.clear();
		try {
			ArrayList<ItemData> itemsFromFile = mFileManager.getFromFile();
			mCache.addAll(itemsFromFile);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private boolean writeCacheToFile() throws WritingToStorageFileException {
		ArrayList<ItemData> itemsToWrite = mCache.getAllItems(); 
		mFileManager.writeToFile(itemsToWrite);
		return true;
	}

	private ArrayList<ItemData> trimList(ArrayList<ItemData> list, String tag) {
		ArrayList<ItemData> result = new ArrayList<ItemData>();
		for (ItemData item : list) {
			@SuppressWarnings("unchecked")
			ArrayList<String> tags = (ArrayList<String>) item.get(Keys.HASHTAGS);
			if (tags.contains(tag)) {
				result.add(item);
			}
		}
		Collections.sort(result);
		return result;
	}
}
