package com.fzm.chat33.core.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/07/11
 * Description:
 */
public class ModuleState implements Serializable {

    /**
     * 模块
     * 1：实名认证模块
     * 2: 考勤打卡模块
     */
    private int type;
    private String name;
    private boolean enable;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public static class Wrapper implements Serializable {
        public List<ModuleState> modules;
    }
}
