package udo.util.parser;

import udo.util.shared.Command;
import udo.util.shared.InputData;
import udo.util.shared.ParsingStatus;

public class ParserList implements ParserCommand {

	@Override
	public InputData run(Command type, String details) {
		InputData data = new InputData(type);
		String lowerCaseDetails = details.toLowerCase();
		ParserListCommand list;
		
		if (details.contains("#")) {
			list = new ParserListHashtag();
			list.fill(type, details, data);
		} else if (details.contains("/") || lowerCaseDetails.contains("day")) {
			list = new ParserListDate();
			list.fill(type, details, data);
		} else if (lowerCaseDetails.contains("all")){
			list = new ParserListAll();
			list.fill(type, details, data);
		} else {
			data.setParsingStatus(ParsingStatus.FAIL);
		}
		return data;
	}

	@Override
	public InputData run(Command type) {
		return null;
	}

}
