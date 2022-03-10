package icu.xchat.core.database.interfaces;

import icu.xchat.core.entities.ChatRoomInfo;

/**
 * 聊天室数据访问对象
 *
 * @author shouchen
 */
public interface RoomDao {
    /**
     * 获取一个房间信息
     *
     * @param rid 房间id
     * @return 房间信息实体
     */
    ChatRoomInfo getRoomInfoByRid(int rid);

    /**
     * 添加一个房间
     *
     * @param roomInfo 房间信息
     * @return 结果
     */
    boolean addRoomInfo(ChatRoomInfo roomInfo);

    /**
     * 更新一个房间
     *
     * @param roomInfo 房间信息
     * @return 结果
     */
    boolean updateRoomInfo(ChatRoomInfo roomInfo);
}
