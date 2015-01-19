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
        wBt = (Button)findViewById(R.id.write_bt);
        editText = (EditText)findViewById(R.id.num);

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
        else if (!nfcAdapter.isEnabled())
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
        byte[] a1 = {(byte)0xAC, (byte)0x20, (byte)0x10, (byte)0x13, (byte)0x79, (byte)0xDB};
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

            for (int i = 0; i < sectorCount; i++)
            {
                if (i == 11 || i == 12)
                    auth = mfc.authenticateSectorWithKeyA(i, a1);
                else
                    auth = mfc.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT);

//                String test = "";
//                for (int p = 0; p < MifareClassic.KEY_DEFAULT.length; p++)
//                    test += MifareClassic.KEY_DEFAULT[p] + " ";
//
//                System.out.println("Key:" + test);

                int bCount;
                int bIndex;
                if (auth)
                {
                    metaInfo += "Sector" + i + "验证成功\n";
                    bCount = mfc.getBlockCountInSector(i);      //获取块数
                    bIndex = mfc.sectorToBlock(i);              //获取i块扇区第一块
                    for (int j = 0; j < bCount; j++)
                    {
                        byte[] data = mfc.readBlock(bIndex);
                        metaInfo += "Block " + bIndex + " : "
                                + bytesToHexString(data) + "\n";
                        bIndex++;
                    }
                }
                else
                {
                    metaInfo += "Sector" + i + "验证失败";
                }
            }

            System.out.println("metaInfo:" + metaInfo);
            NfcTest.setText(metaInfo);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                mfc.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }


        return false;
    }

    private void writeNFC(List<String> list)
    {
        byte[] bytes = {0, 0, 17, -81, 24, 84, 16, 4, 1, 21, 0, 0, 0, 4, 0, -112};
        byte[] bytes1 = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 24, 84, 16, 4, 1, 21, 0, 0, 0, 4, 0, -112};
        byte[] key = {(byte)0xAC, (byte)0x20, (byte)0x10, (byte)0x13, (byte)0x79, (byte)0xDB};

        for (int i = 0; i < list.size(); i++)
        {
            bytes1[3 - i] = (byte)Integer.parseInt(list.get(i), 16);
        }

        try
        {
            mfc.connect();
            boolean auth = false;
            short sectorAddress = 12;
            auth = mfc.authenticateSectorWithKeyA(sectorAddress, key);
            if (auth)
            {
                mfc.writeBlock(48, bytes1);
                mfc.writeBlock(49, bytes1);
                Toast.makeText(this, "写入成功", Toast.LENGTH_LONG).show();
            }
        }
        catch (IOException e)
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
