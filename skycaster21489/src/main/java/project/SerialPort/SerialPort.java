package project.SerialPort;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by LeoLiao on 2017/3/13.
 */

/**
 * A Serial Port Object,representing a serial port to be open.
 */
public class SerialPort {
    private static final String TAG = "SerialPort";
    static {
        System.loadLibrary("SerialPort");
    }
    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;
    private File device;

    /**
     * Open serial port with params.
     * @param device A File created with the path of the selected serial port.
     * @param baudrate The selected baud rate.
     * @param flags  The default value is 0. Use the default value unless you know what you are doing.
     * @throws SecurityException This is thrown when access to the serial port is denied.
     * @throws IOException This is thrown when serial port configuration is not correctly set or due to other unknown reasons.
     */
    public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {
        this.device=device;
		/* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
				/* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec("/system/xbin/su");
                String cmd = "chmod 777 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    Log.e(TAG,"(su.waitFor() != 0) || !device.canRead()|| !device.canWrite()");
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if(e.getMessage()!=null){
                    Log.e(TAG,e.getMessage());
                }

                throw new SecurityException();
            }
        }

        mFd =open(device.getAbsolutePath(), baudrate, flags);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }else {
            Log.e(TAG, "open device success!  "+mFd.toString());
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    /**
     * Return the path of the current serial port.
     * @return
     */
    public String getPath(){
        return device.getAbsolutePath();
    }

    // Getters and setters

    /**
     * gain access to the input stream of the serial port.
     * @return
     */
    public InputStream getInputStream() {
        return mFileInputStream;
    }


    /**
     * gain access to the output stream of the serial port.
     * @return
     */
    public OutputStream getOutputStream() {return mFileOutputStream;}


    private native FileDescriptor open(String path,int baudrate,int flag);

    /**
     * Release the serial port. Strongly recommend to close the serial port when
     * it's no longer needed.
     */
    public native void close();
}
