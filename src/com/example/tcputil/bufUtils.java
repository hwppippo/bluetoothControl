package com.example.tcputil;

import java.util.regex.Pattern;

import android.util.Log;

public class bufUtils {

	private final static Pattern phone = Pattern
			.compile("^((13[0-9])|170|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");

	public static byte uniteBytes(byte src0, byte src1) {
		
		byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
				.byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
				.byteValue();
		byte ret = (byte) (_b0 | _b1);
		return ret;
	}

	/**
	 * 判断是不是一个合法的手机号码
	 */
	public static boolean isPhone(String phoneNum) {
		if (phoneNum == null || phoneNum.trim().length() == 0)
			return false;
		return phone.matcher(phoneNum).matches();
	}
}
