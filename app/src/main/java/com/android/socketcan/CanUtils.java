package com.android.socketcan;

import android.annotation.SuppressLint;
import android.util.Log;

import com.android.socketcan.CanSocket.CanFrame;
import com.android.socketcan.CanSocket.CanId;
import com.android.socketcan.CanSocket.CanInterface;
import com.android.socketcan.CanSocket.Mode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressLint("DefaultLocale")
public class CanUtils {

    private static final String TAG = "CanUtils";
    private static final String cmdStopCanNetwork = "ip link set %1$s down";
    private static final String cmdStartCanNetwork = "ip link set %1$s up type can bitrate %2$d";

    private static CanSocket socket;
    private static CanInterface canif;

    public static void config(String device, int bitTiming) {
        String stopCommand = String.format(cmdStopCanNetwork, device);
        String startCommand = String.format(cmdStartCanNetwork, device, bitTiming);
        List<String> cmdList = new ArrayList<>();
        cmdList.add(stopCommand);
        cmdList.add(startCommand);
        String result = ShellExecute.INSTANCE.run(cmdList, null);
        Log.i(TAG, result);
    }

    public static void init(String device) throws IOException {
        socket = new CanSocket(Mode.RAW);
        canif = new CanInterface(socket, device);
        socket.bind(canif);
    }

    public static CanFrame revData() throws IOException {
        return socket.recv();
    }

    public static void sendData(int id, byte[] data) throws IOException {
        CanId canId = new CanId(id);
        int i = 0;
        byte[] canData;
        for (; i * 8 < data.length - 8; i++) {
            canData = Arrays.copyOfRange(data, i * 8, (i + 1) * 8);
            socket.send(new CanFrame(canif, canId, canData));
        }
        canData = Arrays.copyOfRange(data, i * 8, data.length);
        socket.send(new CanFrame(canif, canId, canData));
    }
}
