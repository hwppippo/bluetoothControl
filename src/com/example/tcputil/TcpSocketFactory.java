package com.example.tcputil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class TcpSocketFactory {

    private Socket mSocket; // socket连接对象
    private DataOutputStream out;
    private DataInputStream in; // 输入流
    private byte[] buffer = new byte[1024]; // 缓冲区字节数组
    private byte[] tmpBuffer; // 临时缓冲区
    private TcpSocketCallback callback; // 信息回调函数
    private int timeOut = 100 * 50;

    /*
     * 构造方法传入信息回调接口
     * 
     * @param 回调接口
     */
    public TcpSocketFactory(TcpSocketCallback callback) {

        this.callback = callback;
    }

    /*
     * 连接网络服务器
     * 
     * @throws UnknownHostException
     * 
     * @throws IOException
     */
    public void connect(String ip, int port) throws Exception {
        mSocket = new Socket();
        SocketAddress address = new InetSocketAddress(ip, port);
        mSocket.connect(address, timeOut); // 指定ip和端口
        if (isConnected()) {
            out = new DataOutputStream(mSocket.getOutputStream()); // 获取网路输出流
            in = new DataInputStream(mSocket.getInputStream()); // 获取网路输入流
        }
        this.callback.tcp_connected();
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    /*
     * 
     * 返回连接服务器是否成功
     */
    public boolean isConnected() {
        if (mSocket == null || mSocket.isClosed()) {
            return false;
        }

        return mSocket.isConnected();
    }

    /*
     * 
     * 发送数据
     * 
     * @param buffer 信息字节数据
     * 
     * @throws IOException
     */
    public void write(byte[] buffer) throws IOException {
        if (out != null) {
            out.write(buffer);
            out.flush();
        }
    }

    /*
     * 断开连接
     * 
     * @throws IOException
     */
    public void disconnect() {

        try {
            if (mSocket != null) {
                if (!mSocket.isInputShutdown()) {
                    mSocket.shutdownInput();
                }
                if (!mSocket.isOutputShutdown()) {
                    mSocket.shutdownOutput();
                }
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (mSocket != null && !mSocket.isClosed()) { // 判断socket不为空并且是连接状态
                mSocket.close();// 关闭
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            callback.tcp_disconnect();
            out = null;
            in = null;
            mSocket = null;
        }
    }

    /*
     * 读取网络数据
     * 
     * @throws IOException
     */
    public void read() throws IOException {
        if (in != null) {
            int len = 0; //读取长度
            while((len = in.read(buffer)) > 0) {
                tmpBuffer = new byte[len];
                //Log.e("inStream Read", Bytes2HexString(buffer,len));
                System.arraycopy(buffer, 0, tmpBuffer, 0, len);
                callback.tcp_receive(tmpBuffer);
                tmpBuffer = null;
            }
        }
    }
    
    // 从字节数组到十六进制字符串转换
    private final static byte[] hex = "0123456789ABCDEF".getBytes();

    public static String Bytes2HexString(byte[] b, int len) {

        byte[] buff = new byte[2 * len];

        for (int i = 0; i < len; i++) {

            buff[2 * i] = hex[(b[len - i - 1] >> 4) & 0x0f];
            buff[2 * i + 1] = hex[b[len - i - 1] & 0x0f];
        }
        return new String(buff);
    }
}
