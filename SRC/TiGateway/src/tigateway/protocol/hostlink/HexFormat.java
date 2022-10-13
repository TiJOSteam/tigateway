package tigateway.protocol.hostlink;

/**
 * @author bingo
 * @Description          格式化十六进制
 * @Date 2018/8/18
 */
public class HexFormat {

    /**
     *   将十六进制数字进行格式化 2个字节
     *    @param value  十六进制字符串
     */
    public static String formatHex (String value){
        if(value.length()==1){
            value = "000"+value;
        }else if(value.length()==2){
            value = "00"+value;
        }else if(value.length()==3){
            value = "0"+value;
        }
        return  value.toUpperCase();
    }
    
    /**
     *   将十六进制数字进行格式化 2个字节
     *    @param value  十六进制字符串
     */
    public static  String formatSimple(String value){
        if(value.length()==1){
            value = "0"+value;
        }
        return value.toUpperCase();
    }
}
