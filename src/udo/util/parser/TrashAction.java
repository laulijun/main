//@author A0114847B
package udo.util.parser;

import udo.util.shared.Command;
import udo.util.shared.InputData;
import udo.util.shared.ParsingStatus;

public class TrashAction implements Action {

	/**
	 * This class helps handle all input without command
	 */
	
	public TrashAction() {

	}

	@Override
	public InputData run(Command type, String input) {
		return null;
	}

	@Override
	public InputData run(Command type) {
		InputData trashInputData = new InputData(type);
		trashInputData.setParsingStatus(ParsingStatus.FAIL);
		return trashInputData;
	}

}