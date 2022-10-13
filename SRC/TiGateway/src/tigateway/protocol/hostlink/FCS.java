package tigateway.protocol.hostlink;

/**
 * @author bingo
 * @Description      OmRon  HostLink数据校验算法
 * @Date 2018/8/22
 */
public class FCS {

    public static  String getFCS(String value){
        int ret=value.charAt(0);
        for(int i=1;i<value.length();i++){
            ret = ret ^ value.charAt(i);
        }

        return HexFormat.formatSimple(Integer.toHexString(ret));
    }

    public static void main(String[] args) {
        System.out.println(FCS.getFCS("@00RD00AB3101CE"));
    }
}
