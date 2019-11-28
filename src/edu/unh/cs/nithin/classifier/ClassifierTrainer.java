/**
 * @Author: Nithin Sivakumar <Nithin>
 * @Date:   2019-11-27T19:37:33-05:00
 * @Last modified by:   Nithin
 * @Last modified time: 2019-11-27T21:12:06-05:00
 */
package edu.unh.cs.nithin.classifier;

public class ClassifierTrainer {

	private String outputFilePath;
	private String arrfFilePath;

	/**
	 *
	 * @param outputPath
	 */
	public ClassifierTrainer(String outputPath) {
		setArrfFilePath(outputPath);
		setOutputFilePath(outputPath);
	}


	public void buildClassifierModel() {

	}

	/**
	 * @return the outputFilePath
	 */
	public String getOutputFilePath() {
		return outputFilePath;
	}

	/**
	 * @param outputFilePath the outputFilePath to set
	 */
	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath+"/models";
	}

	/**
	 * @return the arrfFilePath
	 */
	public String getArrfFilePath() {
		return arrfFilePath;
	}

	/**
	 * @param arrfFilePath the arrfFilePath to set
	 */
	public void setArrfFilePath(String outputPath) {
		this.arrfFilePath = outputPath+"/enviromentTrainset";
	}

}
