package com.example.tcputil;

import java.util.Vector;

import org.apache.http.message.BufferedHeader;

import android.R.integer;
import android.util.Log;

public class TcpSocketConnect implements Runnable {

    private static final int MAX_LINK_NUM = 5;
    private boolean isConnect = false; // 是否连接服务器
    private boolean isWrite = false; // 是否发送数据
    private static Vector<byte[]> datas = new Vector<byte[]>(); // 待发送数据队列
    private Object lock = new Object(); // 连接锁对象
    private TcpSocketFactory mSocket; // socket连接
    private WriteRunnable writeRunnable; // 发送数据线程
    private String ip = null;
    private int port = -1;

    /*
     * 创建连接
     * 
     * @param callback 回调接口
     * 
     * @param executor 线程池对象
     */
    public TcpSocketConnect(TcpSocketCallback callback, String ip, int port) {
        mSocket = new TcpSocketFactory(callback);
        writeRunnable = new WriteRunnable();
        setAddress(ip, port);
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        Log.e("!!!!!!!!!!!!!", "run");
        if (ip == null || port == -1) {
            return;
        }
        isConnect = true;
        int num = 0;
        while (isConnect) {
            synchronized (lock) {
                try {
                    Log.i("TCP连接服务器 ", ip+"   " + port);
                    mSocket.connect(ip, port);
                  
                } catch (Exception e) {
                    // TODO: handle exception
                    try {
                        Log.e("", "TCP连接服务器失败，5s后重试");
                        resetConnect();
                        lock.wait(3000);
                        num++;
                        if(num == MAX_LINK_NUM){
                            Log.e("", "连接失败退出");
                            disconnect();
                        }
                        continue;
                    } catch (InterruptedException e2) {
                        // TODO: handle exception
                        continue;
                    }
                }
            }
            Log.e("", "TCP连接服务器成功");
            
            isWrite = true;
            new Thread(writeRunnable).start();
            try {
                mSocket.read();
            } catch (Exception e) {
                // TODO: handle exception
                Log.e("", "TCP读取数据错误",e);
                disconnect();
                return;
            }
        }
    }

    /*
     * 关闭服务器
     */
    public void disconnect() {
        synchronized (lock) {
            isConnect = false;
            lock.notify();
            resetConnect();
        }
    }

    /*
     * 重置连接
     */
    public void resetConnect() {
        writeRunnable.stop(); // 停止发送消息
        mSocket.disconnect();
    }

    /*
     * 向发送线程写入发送数据
     */
    private void write(byte[] buffer) {
        writeRunnable.write(buffer);
    }

    /*
     *  发送的命令从字符串转换成字节方式，16进制
     */
    public void sendButCmd(String code) {
        byte[] ret = new byte[code.length()/2];
        for(int i = 0,j = 0; i < code.length(); i+=2,j++){
            byte temp = bufUtils.uniteBytes(code.getBytes()[i], code.getBytes()[i+1]);
            ret[j] = temp;
        }
        
        write(ret);
    }

    public void sendButCmd(byte[] cmd){
        write(cmd);
    }
    /*
     * 设置IP和端口
     * 
     * @param IP
     * 
     * @param port
     */
    public void setAddress(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /*
     * 发送数据
     */
    private boolean writes(byte[] buffer) {
        try {
            mSocket.write(buffer);
            Thread.sleep(1);
            return true;
        } catch (Exception e) {
            // TODO: handle exception
            resetConnect();
            return false;
        }
    }

    /*
     * 发送线程
     */
    private class WriteRunnable implements Runnable {
        private Object wlock = new Object(); // 发送线程锁对象

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Log.e("aaaaa", "TCP发送线程开启");
            while (isWrite) {
                synchronized (wlock) {
                    if (datas.size() <= 0) {
                        try {
                            wlock.wait(); // 等待发送数据
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            continue;
                        }
                    }
                    while (datas.size() > 0) {
                        Log.e("", "有数据发送了");
                        byte[] buffer = datas.remove(0);
                        if (isWrite) {
                            writes(buffer);
                            datas.clear();
                        } else {
                            wlock.notify();
                        }
                    }
                }
            }
        }

        /*
         * 添加数据到发送队列
         * 
         * @param buffer 数据字节
         */
        public void write(byte[] buffer) {
            synchronized (wlock) {
                datas.add(buffer);
                wlock.notify(); // 取消等待
            }
        }

        public void stop() {
            synchronized (wlock) {
                isWrite = false;
                wlock.notify();
            }
        }
    }

}
