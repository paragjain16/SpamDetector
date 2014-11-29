package reader;

import java.io.IOException;

import org.mozilla.universalchardet.UniversalDetector;

public class DetectEncoding {

	public static String detect(String fileName) {
		byte[] buf = new byte[4096];

		java.io.FileInputStream fis;
		try {
			fis = new java.io.FileInputStream(fileName);

			// (1)
			UniversalDetector detector = new UniversalDetector(null);

			// (2)
			int nread;
			while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
				detector.handleData(buf, 0, nread);
			}
			// (3)
			detector.dataEnd();

			// (4)
			String encoding = detector.getDetectedCharset();
//			if (encoding != null) {
//				System.out.println("Detected encoding = " + encoding);
//			} else {
//				System.out.println("No encoding detected.");
//			}
			fis.close();
			// (5)
			detector.reset();
			return encoding;
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		return null;
	}
}
