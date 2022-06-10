package main;

import java.util.ArrayList;

import modele.Status;
import modele.TestCase;
import squashConnect.SquashAPI;
import squashConnect.SquashAPIException;

public class Main {

	public static void main(String[] args) {
		SquashAPI squash = new SquashAPI("127.0.0.1", "8080", "admin", "admin");
		ArrayList<TestCase> testCases;
		try {
			testCases = squash.getAllTestsInIteration("TestProject-1", "Campaign Test 1", "Iteration - 1");
			System.out.println("/////////////////////////////////");
			System.out.println(testCases.toString());
			squash.setTestStatus(testCases.get(1), Status.SUCCESS);
		} catch (SquashAPIException e) {
			e.printStackTrace();
		}
	}

}
