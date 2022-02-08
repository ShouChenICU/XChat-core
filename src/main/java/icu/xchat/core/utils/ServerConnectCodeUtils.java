package icu.xchat.core.utils;

import icu.xchat.core.entities.ServerInfo;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;

import java.util.Base64;
import java.util.zip.DataFormatException;

/**
 * 服务器连接码工具类
 *
 * @author shouchen
 */
public final class ServerConnectCodeUtils {

    /**
     * 根据服务器连接码生成一个服务器信息实体
     *
     * @param serverConnectCode 连接码
     * @return 服务器信息
     */
    public static ServerInfo fromCode(String serverConnectCode) throws DataFormatException {
        byte[] dat = Base64.getDecoder().decode(serverConnectCode);
        dat = CompressionUtils.deCompress(dat);
        BSONObject object = new BasicBSONDecoder().readObject(dat);
        return new ServerInfo()
                .setServerCode((String) object.get("SERVER_CODE"))
                .setAkeAlgorithm((String) object.get("AKE_ALGORITHM"))
                .setHost((String) object.get("HOST"))
                .setPort((Integer) object.get("PORT"));
    }
}
