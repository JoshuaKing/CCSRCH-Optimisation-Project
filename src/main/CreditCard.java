package main;

import java.util.ArrayList;

public class CreditCard implements Runnable {
	private String card, filename;
	private Settings settings;
	private long offset;
	
	public static int getMinLength() {
		return 14;
	}
	
	public static Boolean validLength(int len) {
		return (len >= 14 && len <= 16) ? true : false; 
	}
	
	public static ArrayList<Integer> validLengths() {
		ArrayList<Integer> a = new ArrayList<Integer>();
		a.add(14);
		a.add(15);
		a.add(16);
		return a;
	}
	
	public CreditCard(Settings settings, String card, String path, long offset) {
		this.settings = settings;
		this.card = card;
		this.filename = path;
		this.offset = offset;
	}
	
	public void check() {
		if (!isValid()) return;
		String msg = "Found Valid Credit Card: " + card + " - Character #" + offset + " - " + filename; 
		settings.log().success(msg);
		settings.addSummary("Credit Card: " + card + " - Character #" + offset + " - " + filename);
	}
	
	public Boolean isValid() {
		// Check Luhn Test //
		if (!luhnTest()) return false;
		
		// Check required cards //
		if (checkVisa()) return true;
		if (checkVisaElectron()) return true;
		if (checkMastercard()) return true;
		if (checkDinersInternational()) return true;
		if (checkAmericanExpress()) return true;
		
		// Not a valid card //
		return false;
	}
	
	private Boolean luhnTest() {
		// Calculates the Luhn Test on a given string of numbers //
		int sum = 0;
		for (int i = 0; i < card.length(); i++) {
			if (i % 2 == 0) {
				sum += card.charAt(i) - '0';
			} else {
				int val = 2 * (card.charAt(i) - '0');
				if (val > 9) sum += val - 9;
				else sum += val;
			}
		}

		if (sum % 10 == 0) return true;
		return false;
	}
	
	private Boolean checkVisa() {
		if (card.length() != 16) return false;
		if (card.charAt(0) != '4') return false;
		return true;
	}
	
	private Boolean checkVisaElectron() {
		if (card.length() != 16) return false;
		if (card.startsWith("4026")) return true;
		if (card.startsWith("417500")) return true;
		if (card.startsWith("4508")) return true;
		if (card.startsWith("4508")) return true;
		if (card.startsWith("4913")) return true;
		if (card.startsWith("4917")) return true;
		return false;
	}
	
	private Boolean checkMastercard() {
		if (card.length() != 16) return false;
		if (card.charAt(0) != '5') return false;
		if (card.charAt(1) < '1' || card.charAt(1) > '5') return false;
		return true;
	}
	
	private Boolean checkDinersInternational() {
		if (card.length() != 14) return false;
		if (!card.startsWith("36")) return false;
		return true;
	}
	
	private Boolean checkAmericanExpress() {
		if (card.length() != 15) return false;
		if (card.startsWith("34") || card.startsWith("37")) return true;
		return false;
	}


	public void run() {
		check();
	}
}
