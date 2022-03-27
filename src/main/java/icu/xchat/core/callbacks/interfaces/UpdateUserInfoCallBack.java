package icu.xchat.core.callbacks.interfaces;

import icu.xchat.core.entities.UserInfo;

/**
 * 用户信息更新回调
 *
 * @author shouchen
 */
public interface UpdateUserInfoCallBack {
    /**
     * 更新用户信息
     *
     * @param userInfo 用户信息实体
     */
    void updateUserInfo(UserInfo userInfo);
}
