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

        TestNFC();

        wBt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                trySector((short)11);
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

            //System.out.println("metaInfo:" + metaInfo);
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

    private void writeNFC()
    {
        byte[] bytes = {-54, -70, -26, 18, -124, 8, 4, 0, 98, 99, 100, 101, 102, 103, 104, 105};
        byte[] bytes1 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] bytes2 = {0, 0, 0, 0, 0, 0, -1, 7, -128, 105, -1, -1, -1, -1, -1, -1};
        try
        {
            mfc.connect();
            boolean auth = false;
            short sectorAddress = 11;
            auth = mfc.authenticateSectorWithKeyA(sectorAddress, MifareClassic.KEY_DEFAULT);
            if (auth)
            {
                mfc.writeBlock(1, bytes2);
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
        return stringBuilder1.toString();
    }

    private void trySector(short sect)
    {
        List<byte[]> lists = new ArrayList<byte[]>();

        byte[] a1 = {(byte)0xD3, (byte)0xF7, (byte)0xD3, (byte)0xF7, (byte)0xD3, (byte)0xF7};
        byte[] a2 = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
        byte[] a3 = {(byte)0xA0, (byte)0xA1, (byte)0xA2, (byte)0xA3, (byte)0xA4, (byte)0xA5};
        byte[] a4 = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        byte[] a5 = {(byte)0xA1, (byte)0xB1, (byte)0xC1, (byte)0xD1, (byte)0xE1, (byte)0xF1};
        byte[] a6 = {(byte)0xB1, (byte)0xB2, (byte)0xB3, (byte)0xB4, (byte)0xB5, (byte)0xB6};

        lists.add(a1);
        lists.add(a2);
        lists.add(a3);
        lists.add(a4);
        lists.add(a5);
        lists.add(a6);

        try
        {
            mfc.connect();
            short sector = sect;
            boolean auth = false;
            int i;
            int a = -128, b = -128, c = -128, d = -128, e = -128, f = -128;

            for (i = 0; i < lists.size(); i++)
            {
                auth = mfc.authenticateSectorWithKeyA(sector, lists.get(i));
                if (auth)
                    break;
            }
            if (auth)
                System.out.println("true-->" + i);
            else
                System.out.println("false");

//            for (a = -128; a < 128; a++)
//            {
//                for (b = -128; b < 128; b++)
//                {
//                    for (c = -128; c < 128; c++)
//                    {
//                        for (d = -128; d < 128; d++)
//                        {
//                            for (e = -128; e < 128; e++)
//                            {
//                                for (f = -128; f < 128; f++)
//                                {
//                                    byte[] test = {(byte)a, (byte)b, (byte)c, (byte)d, (byte)e, (byte)f};
//                                    auth = mfc.authenticateSectorWithKeyA(sector, test);
//                                    System.out.println("test:" + a + " " + b + " " + c + " " + d + " " + e + " " + f);
//                                    if (auth)
//                                        break;
//                                }
//                                if (auth)
//                                    break;
//                            }
//                            if (auth)
//                                break;
//                        }
//                        if (auth)
//                            break;
//                    }
//                    if (auth)
//                        break;
//                }
//                if (auth)
//                    break;
//            }
//
//            if (auth)
//            {
//                System.out.println("i:" + a + " " + b + " " + c + " " + d + " " + e + " " + f);
//                NfcTest.setText("i:" + a + " " + b + " " + c + " " + d + " " + e + " " + f);
//            }
//            else
//                System.out.println("false");
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
    }
}
