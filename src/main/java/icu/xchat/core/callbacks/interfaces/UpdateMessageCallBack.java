package icu.xchat.core.callbacks.interfaces;

import icu.xchat.core.entities.MessageInfo;

/**
 * 消息更新回调
 *
 * @author shouchen
 */
public interface UpdateMessageCallBack {

    /**
     * 更新消息
     *
     * @param messageInfo 消息实体
     */
    void updateMessage(MessageInfo messageInfo);
}
