package com.fzm.chat33.ait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author zhengjy
 * @since 2019/08/19
 * Description:
 */
public class AitContactsModel {

    /**
     * 保存已@的成员和对应的@块
     */
    private Map<String, AitBlock> aitBlocks = new HashMap<>();

    /**
     * 清除所有的@块
     */
    public void reset() {
        aitBlocks.clear();
    }

    void addAitMember(String account, String name, int start) {
        AitBlock aitBlock = aitBlocks.get(account);
        if (aitBlock == null) {
            aitBlock = new AitBlock(name);
            aitBlocks.put(account, aitBlock);
        }
        aitBlock.addSegment(start);
    }

    /**
     * 查所有被@的群成员
     *
     * @return  被@人的id列表
     */
    List<String> getAitMembers() {
        if (aitBlocks.size() == 0) {
            return null;
        }
        List<String> teamMembers = new ArrayList<>();
        for (String account : aitBlocks.keySet()) {
            AitBlock block = aitBlocks.get(account);
            if (block.valid()) {
                teamMembers.add(account);
            }
        }
        return teamMembers;
    }

    /**
     * 找到 curPos 恰好命中 end 的segment
     *
     * @param start
     * @return
     */
    AitBlock.AitSegment findAitSegmentByEndPos(int start) {
        for (String account : aitBlocks.keySet()) {
            AitBlock block = aitBlocks.get(account);
            AitBlock.AitSegment segment = block.findLastSegmentByEnd(start);
            if (segment != null) {
                return segment;
            }
        }
        return null;
    }

    /**
     * 文本插入后更新@块的起止位置
     *
     * @param start         @块起始位置
     * @param changeText    插入文本的长度
     */
    void onInsertText(int start, String changeText) {
        Iterator<String> iterator = aitBlocks.keySet().iterator();
        while (iterator.hasNext()) {
            String account = iterator.next();
            AitBlock block = aitBlocks.get(account);
            block.moveRight(start, changeText);
            if (!block.valid()) {
                iterator.remove();
            }
        }
    }

    /**
     * 文本删除后更新@块的起止位置
     *
     * @param start     @块起始位置
     * @param length    @块的长度
     */
    void onDeleteText(int start, int length) {
        Iterator<String> iterator = aitBlocks.keySet().iterator();
        while (iterator.hasNext()) {
            String account = iterator.next();
            AitBlock block = aitBlocks.get(account);
            block.moveLeft(start, length);
            if (!block.valid()) {
                iterator.remove();
            }
        }
    }
}

