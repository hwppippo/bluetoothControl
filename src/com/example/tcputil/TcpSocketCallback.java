package com.example.tcputil;

public abstract class TcpSocketCallback {

    /*
     *  断开连接
     */
    public static final int TCP_DISCONNECTED = 0;
    
    /*
     *  已连接
     */
    public static final int TCP_CONNECTED = 1;
    
    /*
     *  连接获得数据
     */
    public static final int TCP_DATA = 2;
    
    /*
     *  当建立连接时回调
     */
    public abstract void tcp_connected();
    
    /*
     *  当获取网络数据时回调
     */
    public abstract void tcp_receive(byte[] buffer);
    
    /*
     * 当断开连接时回调
     */
    public abstract void tcp_disconnect();
}
