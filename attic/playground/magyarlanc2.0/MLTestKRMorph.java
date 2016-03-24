import org.apache.ibatis.io.Resources;
import hu.u_szeged.magyarlanc.MorAna;
import rfsa.RFSA;


class MLTestKRMorph {

	public static void main(String args[]) {

		String RFS = "rfsa.txt";
		String ENCONDING = "UTF-8";
		RFSA rfsa = null;
		try {
			rfsa = RFSA.read(Resources.getResourceAsStream(RFS), ENCONDING);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }	
		String[] sent = {"A", "nagy", "kutya", "futott", "."};

		for (int i=0; i<sent.length; i++) {
			System.out.format("%s:\n", sent[i]);
			for (String ana: rfsa.analyse(sent[i]))
				System.out.format("  %s\n", ana);
		}
		
	}
}