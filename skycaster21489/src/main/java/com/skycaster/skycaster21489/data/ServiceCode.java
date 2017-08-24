package com.skycaster.skycaster21489.data;

/**
 * Created by 廖华凯 on 2017/5/22.
 * 业务代码
 */

public enum ServiceCode {
    RAW_DATA,ALL,ERROR;

    /**
     * 根据协议中的数字代码生成对应的业务代码对象
     * @param code 业务代码的字符串形式
     * @return 对应的业务代码实例
     */
    public static ServiceCode initServiceCodeByDigit(String code){
        switch (code){
            case "1":
                return RAW_DATA;
            case "ALL":
                return ALL;
        }
        return ERROR;
    }

    private String getDescription(){
        String description;
        switch (this){
            case RAW_DATA:
                description="裸数据传输业务";
                break;
            case ALL:
                description="启动全部业务";
                break;
            case ERROR:
            default:
                description="Error";
                break;
        }

        return description;
    }

    @Override
    /**
     * 改写此方法是为了更好的封装SDK。
     */
    public String toString() {
        switch (super.toString()){
            case "RAW_DATA":
                return "1";
            case "ALL":
                return "ALL";
        }
        return super.toString();
    }

    /**
     * 获得所有item的描述，以数组的形式返回。
     * @return 一个数组，含有除了最后一项“ERROR”以外所有item的文字描述。
     */
    public static String[] getValueDescriptions(){
        ServiceCode[] values = values();
        int len = values.length - 1;
        String[] codeDescription=new String[len];
        for(int i=0;i<len;i++){
            codeDescription[i]=values[i].getDescription();
        }
        return codeDescription;
    }
}
