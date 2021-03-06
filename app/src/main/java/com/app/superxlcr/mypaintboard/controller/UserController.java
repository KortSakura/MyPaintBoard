package com.app.superxlcr.mypaintboard.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.app.superxlcr.mypaintboard.model.Protocol;
import com.app.superxlcr.mypaintboard.model.User;
import com.app.superxlcr.mypaintboard.utils.MyLog;
import com.app.superxlcr.mypaintboard.utils.ProtocolListener;

import org.json.JSONArray;

/**
 * Created by superxlcr on 2017/1/8.
 *
 * 用户模块控制器
 */

public class UserController {

    private static final String TAG = UserController.class.getSimpleName();

    public static UserController instance = null;

    public static UserController getInstance() {
        if (instance == null) {
            synchronized (UserController.class) {
                if (instance == null) {
                    instance = new UserController();
                }
            }
        }
        return instance;
    }

    private User user = null;
    private String tempPassword = null;

    private ProtocolListener loginListener; // 登录用监听器
    private ProtocolListener registerListener; // 注册用监听器
    private ProtocolListener editInfoListener; // 编辑信息用监听器
    private ProtocolListener loginTimeOutPushListener; // 登录过期推送监听器

    private UserController() {
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    /**
     * 设置
     * @param context 上下文
     * @param handler 用于回调消息
     */
    public void setLoginTimeOutPushHandler(Context context, final Handler handler) {
        // 清除旧监听器
        if (loginTimeOutPushListener != null) {
            CommunicationController.getInstance(context).removeListener(loginTimeOutPushListener);
        }
        // 连接服务器
        CommunicationController.getInstance(context).connectServer();
        // 注册监听器
        loginTimeOutPushListener = new ProtocolListener() {
            @Override
            public boolean onReceive(Protocol protocol) {
                int order = protocol.getOrder();
                if (order == Protocol.LOGIN_TIME_OUT_PUSH) {
                    // 通过handler返回协议信息
                    Message message = handler.obtainMessage();
                    message.obj = protocol;
                    handler.sendMessage(message);
                    return true;
                }
                return false;
            }
        };
        CommunicationController.getInstance(context).registerListener(loginTimeOutPushListener);
    }

    /**
     * 登录
     * @param context 上下文
     * @param handler 回调结果用
     * @param time 发送时间
     * @param username 用户名
     * @param password 密码
     * @return 是否成功发送登录信息
     */
    public boolean login(final Context context, final Handler handler, long time, String username, String password) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(username);
        jsonArray.put(password);
        // 用于自动重新登录
        tempPassword = password;
        Protocol sendProtocol = new Protocol(Protocol.LOGIN, time, jsonArray);
        // 注册监听器
        loginListener = new ProtocolListener() {
            @Override
            public boolean onReceive(Protocol protocol) {
                int order = protocol.getOrder();
                if (order == Protocol.LOGIN) {
                    // 通过handler返回协议信息
                    Message message = handler.obtainMessage();
                    message.obj = protocol;
                    handler.sendMessage(message);
                    // 移除监听器
                    CommunicationController.getInstance(context).removeListener(loginListener);
                    return true;
                }
                return false;
            }
        };
        CommunicationController.getInstance(context).registerListener(loginListener);
        // 发送信息
        return CommunicationController.getInstance(context).sendProtocol(sendProtocol);
    }

    /**
     * 注册
     * @param context 上下文
     * @param handler 回调结果用
     * @param time 发送时间
     * @param username 用户名
     * @param password 密码
     * @param nickname 昵称
     * @return 是否成功发送注册信息
     */
    public boolean register(final Context context, final Handler handler, long time, String username, String password, String nickname) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(username);
        jsonArray.put(password);
        // 用于自动重新登录
        tempPassword = password;
        jsonArray.put(nickname);
        Protocol sendProtocol = new Protocol(Protocol.REGISTER, time, jsonArray);
        // 注册监听器
        registerListener = new ProtocolListener() {
            @Override
            public boolean onReceive(Protocol protocol) {
                int order = protocol.getOrder();
                if (order == Protocol.REGISTER) {
                    // 通过handler返回协议信息
                    Message message = handler.obtainMessage();
                    message.obj = protocol;
                    handler.sendMessage(message);
                    // 移除监听器
                    CommunicationController.getInstance(context).removeListener(registerListener);
                    return true;
                }
                return false;
            }
        };
        CommunicationController.getInstance(context).registerListener(registerListener);
        // 发送信息
        return CommunicationController.getInstance(context).sendProtocol(sendProtocol);
    }

    /**
     * 编辑用户信息
     * @param context 上下文
     * @param handler 回调结果用
     * @param time 发送时间
     * @param username 用户名
     * @param password 密码
     * @param nickname 昵称
     * @return 是否成功发送编辑用户信息
     */
    public boolean editInfo(final Context context, final Handler handler, long time, String username, String password, String nickname) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(user.getId());
        jsonArray.put(username);
        jsonArray.put(password);
        jsonArray.put(nickname);
        Protocol sendProtocol = new Protocol(Protocol.EDIT_INFO, time, jsonArray);
        // 注册监听器，监听结果
        editInfoListener = new ProtocolListener() {
            @Override
            public boolean onReceive(Protocol protocol) {
                int order = protocol.getOrder();
                if (order == Protocol.EDIT_INFO) {
                    // 通过handler返回协议信息
                    Message message = handler.obtainMessage();
                    message.obj = protocol;
                    handler.sendMessage(message);
                    // 移除监听器
                    CommunicationController.getInstance(context).removeListener(editInfoListener);
                    return true;
                }
                return false;
            }
        };
        CommunicationController.getInstance(context).registerListener(editInfoListener);
        // 发送信息
        return CommunicationController.getInstance(context).sendProtocol(sendProtocol);
    }
}
