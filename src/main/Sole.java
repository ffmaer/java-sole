package main;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;

public class Sole {
	public final static int reg = 0, head_mask = 1, end_mask = 2;
	public final static int b = 6;// number of bits in a block
	public static int mode = 1;// 1 means 3 blocks overhead, 2 means 1 block
	// overhead
	public static boolean test = false;
	public static boolean enableHex = false, enableFileInput = false;
	public final static BigInteger n = (BigInteger.valueOf(2).pow(b / 2))
			.add(BigInteger.valueOf(1 / 2));
	public final static BigInteger blockSize = BigInteger.valueOf(2).pow(b);
	public static BigInteger local[] = new BigInteger[4];
	public static BigInteger A, B, x, y, Api, Bpi, nextx, nexty, z, xypi[];
	public static FileInputStream in = null;
	public static BigInteger index = BigInteger.ONE;
	public static String bin, buffer;

	public static void bufferComp() {
		String buffer1 = buffer.substring(0, b);
		String buffer2 = buffer.substring(b);
		if (local[0] == null)// compute the first output
		{
			local[0] = new BigInteger(buffer1, 2);
			local[1] = new BigInteger(buffer2, 2);

			xypi = compOut(head_mask);

			printxypi(true, head_mask);
		} else if (local[2] == null)// compute the 2nd and 3rd output
		{
			local[2] = new BigInteger(buffer1, 2);
			local[3] = new BigInteger(buffer2, 2);

			xypi = compOut(reg);

			printxypi(true, reg);
		} else {
			forward();
			local[2] = new BigInteger(buffer1, 2);
			local[3] = new BigInteger(buffer2, 2);

			xypi = compOut(reg);

			printxypi(true, reg);

		}
	}

	public static BigInteger[] compOut(int control) {

		// 1 pass
		A = blockSize.add(BigInteger.valueOf(mode));
		B = blockSize.add(BigInteger.valueOf(mode));
		// 1 pass front
		x = local[0];
		y = local[1];
		Api = blockSize.subtract(index.subtract(BigInteger.ONE).multiply(
				BigInteger.valueOf(4 * mode)));
		Bpi = blockSize.add(index.multiply(BigInteger.valueOf(4 * mode)));
		// handle the 1st block
		if ((head_mask & control) > 0) {
			return swap();
		} else {
			nextx = swap()[1];
		}

		// 1 pass end
		x = local[2];
		y = local[3];

		Api = blockSize.subtract(index.multiply(BigInteger.valueOf(4 * mode)));
		Bpi = blockSize.add(index.add(BigInteger.ONE).multiply(
				BigInteger.valueOf(4 * mode)));
		nexty = swap()[0];
		// 2 pass
		x = nextx;
		y = nexty;
		A = blockSize.add(index.multiply(BigInteger.valueOf(4 * mode)));
		B = blockSize.subtract(index.multiply(BigInteger.valueOf(4 * mode)));
		Api = blockSize;
		Bpi = blockSize;

		overflowAlert();
		index = index.add(BigInteger.ONE);
		return swap();
	}

	public static void core() throws IOException, InterruptedException {
		if (enableFileInput) {
			readFromFile();
		} else {
			readInput();
		}

	}

	public static void forward() {
		local[0] = local[2];
		local[1] = local[3];
	}

	public static String get3Plus(String more) throws IOException {
		int c = in.read();
		while (c != -1) {
			more = more + Integer.toBinaryString(c);
			if (more.length() <= 3 * b) {
				c = in.read();
			} else {
				break;
			}
		}
		return more;
	}

	public static void handleEOF(String bin) {
		if (bin.length() <= 2 * b) {
			// step 1
			if (local[2] != null) {
				forward();
			}
			if (mode == 1) {
				local[2] = new BigInteger(padBinaryEnd(bin.substring(0, b)), 2);
				local[3] = new BigInteger(padBinaryEnd(bin.substring(b)), 2);
			} else if (mode == 2) {
				local[2] = blockSize;
				local[3] = new BigInteger(padBinaryEnd(bin.substring(0, b)), 2);
			}
			xypi = compOut(reg);
			printxypi(true, reg);
			// step 2
			forward();
			if (mode == 1) {
				local[2] = blockSize;
				local[3] = blockSize;
			} else if (mode == 2) {
				local[2] = new BigInteger(padBinaryEnd(bin.substring(b)), 2);
				local[3] = BigInteger.ZERO;
			}
			xypi = compOut(reg);
			printxypi(true, reg);

		} else// 2b < bin's length <= 3b
		{
			// store last two blocks
			BigInteger l2 = local[2];
			BigInteger l3 = local[3];

			// step 1
			if (local[2] != null) {
				forward();
			}
			if (mode == 1) {
				local[2] = new BigInteger(padBinaryEnd(bin.substring(0, b)), 2);
				local[3] = new BigInteger(
						padBinaryEnd(bin.substring(b, 2 * b)), 2);
				xypi = compOut(reg);
				printxypi(true, reg);
			} else if (mode == 2) {
				local[2] = new BigInteger(padBinaryEnd(bin.substring(0, b)), 2);
				local[3] = blockSize;
			}

			// step 2
			forward();
			if (mode == 1) {
				local[2] = new BigInteger(padBinaryEnd(bin.substring(2 * b)), 2);
				local[3] = blockSize;
				xypi = compOut(reg);
				printxypi(true, reg);
			} else if (mode == 2) {
				local[2] = new BigInteger(
						padBinaryEnd(bin.substring(b, 2 * b)), 2);
				local[3] = new BigInteger(padBinaryEnd(bin.substring(2 * b)), 2);
			}

			// step 3
			forward();
			local[2] = BigInteger.ZERO;
			local[3] = BigInteger.ZERO;

			if (mode == 1) {
				xypi = compOut(reg);
				printxypi(true, reg);
				return;// the end for mode 1
			} else if (mode == 2) {
				// extra step 4
				if (xypi[1].compareTo(BigInteger.ZERO) > 0) {
					local[0] = l2;
					local[1] = l3;
					local[2] = new BigInteger(
							padBinaryEnd(bin.substring(0, b)), 2);
					local[3] = blockSize.add(BigInteger.ONE);
					xypi = compOut(reg);
					printxypi(true, reg);

					forward();
					local[2] = new BigInteger(padBinaryEnd(bin.substring(b,
							2 * b)), 2);
					local[3] = new BigInteger(
							padBinaryEnd(bin.substring(2 * b)), 2);
					xypi = compOut(reg);
					printxypi(true, reg);

					forward();
					local[2] = BigInteger.ZERO;
					local[3] = BigInteger.ZERO;
					xypi = compOut(reg);
					printxypi(true, head_mask);
				}
			}
		}
	}

	public static String handleInput(String nums, int radix) {

		String[] numsArr = nums.split(",");
		String binStream = "";
		int i;
		for (i = 0; i < numsArr.length; i++) {
			binStream = binStream
					+ padBinaryFront(new BigInteger(numsArr[i], radix)
							.toString(2));
		}
		return binStream;
	}

	public static void main(String args[]) throws IOException,
			InterruptedException, InterruptedException {
		if(test){
			test();
		}
		else{
			core();
		}
	}

	public static void overflowAlert() {
		if (n.compareTo(index.subtract(BigInteger.ONE).multiply(
				BigInteger.valueOf(2).add(BigInteger.ONE))) < 0) {
			System.out.println("");
			System.out.println(">>> >>> overflow <<< <<<");
			System.out.println(index.subtract(BigInteger.ONE).multiply(
					BigInteger.valueOf(2).add(BigInteger.ONE))
					+ ": " + n);
			System.out.println(">>> >>> overflow <<< <<<");
		}
	}

	public static String padBinaryEnd(String bits) {
		while (bits.length() < b) {
			bits = bits + "0";
		}
		return bits;
	}

	public static String padBinaryFront(String bits) {
		while (bits.length() < b) {
			bits = "0" + bits;
		}
		return bits;
	}

	public static String padZeros(String bits) {
		while (bits.length() * 4 < b) {
			bits = "0" + bits;
		}
		return bits;

	}

	public static void printxypi(boolean print, int control) {
		if (print) {
			if (enableHex) {
				System.out.print(padZeros(xypi[0].toString(16)));// hex
			} else {
				System.out.print(xypi[0].toString(10));// decimal
			}
			System.out.println(" ");
			if ((head_mask & control) == 0) {
				if (enableHex) {
					System.out.print(padZeros(xypi[1].toString(16)));// hex
				} else {
					System.out.print(xypi[1].toString(10));// decimal
				}
				System.out.println(" ");
			}
		}
	}

	public static void readFromFile() throws IOException, InterruptedException {
		try {
			in = new FileInputStream("nyu.png");
			bin = get3Plus("");
			while (bin.length() > 3 * b) {
				buffer = bin.substring(0, 2 * b);
				bufferComp();
				bin = get3Plus(bin.substring(2 * b));
			}
			// will handle the EOF here
			handleEOF(bin);

			// System.out.println(bin);
			// Thread.sleep(10);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public static void readInput() {
		String bin = handleInput("8,57,17,33,4", 10);
		while (bin.length() > 3 * b) {
			buffer = bin.substring(0, 2 * b);
			bufferComp();
			bin = bin.substring(2 * b);
		}
		// will handle the EOF here
		handleEOF(bin);
	}

	public static BigInteger[] swap() {
		z = y.multiply(A).add(x);
		return new BigInteger[] { z.mod(Api), z.divide(Api) };
	}

	public static void test() {
		local[0] = new BigInteger("8");
		local[1] = new BigInteger("57");

		xypi = compOut(head_mask);
		printxypi(true, head_mask);

		local[2] = new BigInteger("17");
		local[3] = new BigInteger("33");

		xypi = compOut(reg);
		printxypi(true, reg);

		forward();
		local[2] = new BigInteger("4");
		local[3] = new BigInteger("64");

		xypi = compOut(reg);
		printxypi(true, reg);

		forward();
		local[2] = new BigInteger("0");
		local[3] = new BigInteger("0");

		xypi = compOut(reg);
		printxypi(true, reg);

	}
}
