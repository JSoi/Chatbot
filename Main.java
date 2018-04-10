package ai.api.examples.Chatbot;

import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int N = 0;
		String inputs = null;
		Scanner scan = new Scanner(System.in);
		char[] inputChar;

		int j = 0;
		inputs = scan.nextLine();
		inputChar = new char[inputs.length()];
		while (j < inputs.length()) {
			inputChar[j] = inputs.charAt(j);
			j++;
		}
		//RevertUpDown(inputChar);
		System.out.println(findIndex(inputChar,1));
		
	}

	

	private static int findIndex(char[] inputChar, int index) {
		int countX = 0;
		int ResultIndex=0;
		if (inputChar[index] == 'x') {
			int i = index;
			while (i++ < 4) {
				if (inputChar[i] == 'x')
					countX++;
			}	
			ResultIndex = index+countX*4+1;
		}
		else ResultIndex = index+1;
		return ResultIndex;
	}
	
	// 실제인덱스 구하는 방법을 모르겠다.
		private static void RevertUpDown(char[] inputChar) {
			char[] reverseChar = new char[inputChar.length];
			int i = 0;

		}

		private void change(char[] inputChar) {
			int index = 0;
			findIndex(inputChar, index);

			// 전체에서 세번째거를 찾는다
			// recursive에서 그 index에서부터 세번째거를 찾는다.
			// 만약에 세번째 것이 x가아니면 그 index를 배열에 저장해준다.

			// findThird

		}

}
