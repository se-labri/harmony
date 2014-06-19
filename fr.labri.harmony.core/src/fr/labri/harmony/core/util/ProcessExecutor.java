package fr.labri.harmony.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ProcessExecutor {

	private ArrayList<String> processOutput;
	private ArrayList<String> processError;
	private ProcessBuilder processBuilder;

	public ProcessExecutor(String... command) {
		ArrayList<String> checkedCommand = new ArrayList<>();
		for (String arg : command) {
			if (arg != null) checkedCommand.add(arg);
		}
		processBuilder = new ProcessBuilder(checkedCommand);

		processOutput = new ArrayList<>();
		processError = new ArrayList<>();
	}

	public ProcessExecutor setDirectory(String path) {
		processBuilder.directory(new File(path));
		return this;
	}

	public ProcessExecutor run() throws IOException, InterruptedException {
		Process process = processBuilder.start();

		Thread t1 = new Thread(new ProcessStreamReader(process.getInputStream(), processOutput));
		Thread t2 = new Thread(new ProcessStreamReader(process.getErrorStream(), processError));
		t1.start();
		t2.start();
		process.waitFor();
		t1.join();
		t2.join();
		return this;
	}

	public ArrayList<String> getOutput() {
		return processOutput;
	}

	public ArrayList<String> getError() {
		return processError;
	}

	private class ProcessStreamReader implements Runnable {

		private InputStream inputStream;
		private ArrayList<String> lines;

		public ProcessStreamReader(InputStream inputStream, ArrayList<String> lines) {
			this.inputStream = inputStream;
			this.lines = lines;
		}

		private BufferedReader getBufferedReader() {
			return new BufferedReader(new InputStreamReader(inputStream));
		}

		@Override
		public void run() {
			BufferedReader br = getBufferedReader();
			String line;
			try {
				while ((line = br.readLine()) != null)
					lines.add(line);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
