package com.example.NFCtest;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class NFCtest extends Activity
{
	private TextView NfcTest;

	private NfcAdapter nfcAdapter;

	private MifareClassic mfc;

	private Button wBt;

	private EditText editText;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		init();
	}

	private void init()
	{
		NfcTest = (TextView) findViewById(R.id.test);
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		wBt = (Button) findViewById(R.id.write_bt);
		editText = (EditText) findViewById(R.id.num);

		TestNFC();

		wBt.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				writeNFC(IntegerToHex(Integer.parseInt(editText.getText().toString())));
				//System.out.println("aha-->" + IntegerToHex(Integer.parseInt(editText.getText().toString())));
			}
		});
	}

	private void TestNFC()
	{
		if (nfcAdapter == null)
		{
			Toast.makeText(this, "no nfc", Toast.LENGTH_LONG).show();
			finish();
		}
		else if (! nfcAdapter.isEnabled())
		{
			Toast.makeText(this, "open nfc", Toast.LENGTH_LONG).show();
			finish();
		}
	}

//    public void onNewIntent(Intent intent)
//    {
//        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//        System.out.println("tag" + tag.toString());
//    }

	public void onResume()
	{
		super.onResume();

		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction()))
		{
			readFromTag(getIntent());
		}

	}

	private boolean readFromTag(Intent intent)
	{
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

		byte[] nq_a = {(byte) 0x01, (byte) 0x2E, (byte) 0x92, (byte) 0x73, (byte) 0x59, (byte) 0x2B};
		byte[] nq_b = {(byte) 0x95, (byte) 0x66, (byte) 0x05, (byte) 0x14, (byte) 0x69, (byte) 0x02}; // true

		byte[] a1 = {(byte) 0xAC, (byte) 0x20, (byte) 0x10, (byte) 0x13, (byte) 0x79, (byte) 0xDB};
//        for (String tech : tagFromIntent.getTechList())
//        {
//            System.out.println(tech);
//        }

		mfc = MifareClassic.get(tagFromIntent);

		boolean auth = false;

		try
		{
			String metaInfo = "";
			mfc.connect();
			int type = mfc.getType();
			int sectorCount = mfc.getSectorCount();

			String typeS = "";

			switch (type)
			{
				case MifareClassic.TYPE_CLASSIC:
					typeS = "TYPE_CLASSIC";
					break;
				case MifareClassic.TYPE_PLUS:
					typeS = "TYPE_PLUS";
					break;
				case MifareClassic.TYPE_PRO:
					typeS = "TYPE_PRO";
					break;
				case MifareClassic.TYPE_UNKNOWN:
					typeS = "TYPE_UNKNOWN";
					break;
			}

			metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
					+ mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize() + "B\n";

			System.out.println("metaInfo:" + metaInfo);
//
//			for (int i = 9; i <= 12; i++)
//			{
//				for (int j = 0; j < 4; j++)
//				{
//					auth = mfc.authenticateSectorWithKeyA(i, nq[j]);
//
//					int bCount;
//					int bIndex;
//					if (auth)
//					{
//						metaInfo += "Sector" + i + "验证成功A : " + j + "\n";
//						bCount = mfc.getBlockCountInSector(i);      //获取块数
//						bIndex = mfc.sectorToBlock(i);              //获取i块扇区第一块
//						for (int k = 0; k < bCount; k++)
//						{
//							byte[] data = mfc.readBlock(bIndex);
//							metaInfo += "Block " + bIndex + " : "
//									+ bytesToHexString(data) + "\n";
//							bIndex++;
//						}
//					}
//					else
//					{
//						metaInfo += "Sector" + i + "验证失败A\n";
//					}
//				}
//			}

			for (int i = 0; i < sectorCount; i++)
			{

				if (i == 10)
					auth = mfc.authenticateSectorWithKeyB(i, nq_b);
				else
					auth = mfc.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT);

				int bCount;
				int bIndex;
				if (auth)
				{
					metaInfo += "Sector" + i + "验证成功" + "\n";
					bCount = mfc.getBlockCountInSector(i);      //获取块数
					bIndex = mfc.sectorToBlock(i);              //获取i块扇区第一块
					for (int k = 0; k < bCount; k++)
					{
						byte[] data = mfc.readBlock(bIndex);
						metaInfo += "Block " + bIndex + " : "
								+ bytesToHexString(data) + "\n";
						bIndex++;
					}
				}
				else
				{
					metaInfo += "Sector" + i + "验证失败\n";
				}
			}

//            for (int i = 0; i < sectorCount; i++)
//            {
//                if (i == 10 || i == 9 || i == 11)
//                    auth = mfc.authenticateSectorWithKeyA(i, nq_b);
//                else
//                    auth = mfc.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT);
//
//                int bCount;
//                int bIndex;
//                if (auth)
//                {
//                    metaInfo += "Sector" + i + "验证成功\n";
//                    bCount = mfc.getBlockCountInSector(i);      //获取块数
//                    bIndex = mfc.sectorToBlock(i);              //获取i块扇区第一块
//                    for (int j = 0; j < bCount; j++)
//                    {
//                        byte[] data = mfc.readBlock(bIndex);
//                        metaInfo += "Block " + bIndex + " : "
//                                + bytesToHexString(data) + "\n";
//                        bIndex++;
//                    }
//                }
//                else
//                {
//                    metaInfo += "Sector" + i + "验证失败";
//                }
//            }

			System.out.println("metaInfo:" + metaInfo);
			NfcTest.setText(metaInfo);
		} catch (IOException e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				mfc.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}


		return false;
	}

	private void writeNFC(List<String> list)
	{
		byte[] bytes = {0, 0, 17, - 81, 24, 84, 16, 4, 1, 21, 0, 0, 0, 4, 0, - 112};
		byte[] nq_by = {(byte) 0x26,
				(byte) 0x5a,
				(byte) 0x3b,
				(byte) 0x1f,
				(byte) 0x00,
				(byte) 0xa5,
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0x03,
				(byte) 0x03,
				(byte) 0x00,
				(byte) 0x00,
				(byte) 0xfc,
				(byte) 0x01,
				(byte) 0xb3};
		byte[] nq_by_2 = {(byte) 0x02,
				(byte) 0x01,
				(byte) 0xd0,
				(byte) 0x06,
				(byte) 0xff,
				(byte) 0xbb,
				(byte) 0x6e,
				(byte) 0x00,
				(byte) 0x04,
				(byte) 0xa7,
				(byte) 0x54,
				(byte) 0x66,
				(byte) 0x13,
				(byte) 0x14,
				(byte) 0x69,
				(byte) 0x0b};
		byte[] bytes1 = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 24, 84, 16, 4, 1, 21, 0, 0, 0, 4, 0, - 112};
		byte[] key = {(byte) 0xAC, (byte) 0x20, (byte) 0x10, (byte) 0x13, (byte) 0x79, (byte) 0xDB};
		byte[] nq_b = {(byte) 0x95, (byte) 0x66, (byte) 0x05, (byte) 0x14, (byte) 0x69, (byte) 0x02};

//		for (int i = 0; i < list.size(); i++)
//		{
//			nq_by[3 - i] = (byte) Integer.parseInt(list.get(list.size() - i - 1), 16);
//		}

		try
		{
			mfc.connect();
			boolean auth = false;
			short sectorAddress = 10;
			auth = mfc.authenticateSectorWithKeyB(sectorAddress, nq_b);
			if (auth)
			{
				mfc.writeBlock(40, nq_by);
				//mfc.writeBlock(41, nq_by_2);
				mfc.writeBlock(42, nq_by);
				Toast.makeText(this, "写入成功", Toast.LENGTH_LONG).show();
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private String bytesToHexString(byte[] src)
	{
		StringBuilder stringBuilder = new StringBuilder("0x");

		StringBuilder stringBuilder1 = new StringBuilder();

		if (src == null || src.length <= 0)
		{
			return null;
		}
		char[] buffer = new char[2];
		for (int i = 0; i < src.length; i++)
		{
			buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
			buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
			//System.out.println(buffer);
			stringBuilder.append(buffer);

			stringBuilder1.append(src[i] + " ");
		}
		return stringBuilder.toString();
	}

	private List<String> IntegerToHex(int x)
	{
		if (x > 60000)
			x = 60000;

		String hex = Integer.toHexString(x);
		//System.out.println("hex -->" + hex);
		List<String> list = new ArrayList<String>();

		byte[] bytes = new byte[4];
		char[] hexs = hex.toCharArray();

//        for (int i = 0; i < hexs.length; i++)
//        {
//            System.out.println("hexs" + i + ":" + hexs[i]);
//        }

		for (int i = hexs.length - 1; i >= 0; i = i - 2)
		{
			String hello = "";
			if (i == 0)
			{
				hello += (hexs[i] + "");
			}
			else
			{
				hello += ("" + hexs[i - 1]) + ("" + hexs[i]);
			}

			//System.out.println("hello -->" + hello);
			list.add(hello);
		}
		return list;
	}
}
