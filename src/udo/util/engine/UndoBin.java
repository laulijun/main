//@author A0108358B
package udo.util.engine;

import udo.util.shared.InputData;

public class UndoBin {
	private InputData mInputData;

	public UndoBin() {
		mInputData = null;
	}

	/**
	 * put a inputdata here
	 * 
	 * @param input the data
	 */
	public void storeUndo(InputData input) {
		mInputData = input;
	}

	/**
	 * get the input data from here
	 * 
	 * @return the data
	 */
	public InputData getUndo() {
		return mInputData;
	}

	/**
	 * clear the contents of the cache.
	 */
	public void clear() {
		mInputData = null;
	}
}
