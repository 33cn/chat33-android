package com.fzm.chat33.ait;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/08/19
 * Description:
 */
public class AitBlock {

    /**
     * text = "@" + name
     */
    public String text;

    /**
     * 在文本中的位置
     */
    private List<AitSegment> segments = new ArrayList<>();

    AitBlock(String name) {
        this.text = "@" + name;
    }

    /**
     * 新增 segment
     *
     * @param start
     * @return
     */
    AitSegment addSegment(int start) {
        int end = start + text.length() - 1;
        AitSegment segment = new AitSegment(start, end);
        segments.add(segment);
        return segment;
    }

    /**
     * 当进行插入操作时，移动@块的位置
     *
     * @param start      起始光标位置
     * @param changeText 插入文本
     */
    void moveRight(int start, String changeText) {
        if (changeText == null) {
            return;
        }
        int length = changeText.length();
        for (AitSegment segment : segments) {
            // 从已有的一个@块中插入
            if (start > segment.start && start <= segment.end) {
                segment.end += length;
                segment.broken = true;
            } else if (start <= segment.start) {
                segment.start += length;
                segment.end += length;
            }
        }
    }

    /**
     * 当进行删除操作时，移动@块的位置
     *
     * @param start  删除前光标位置
     * @param length 删除块的长度
     */
    void moveLeft(int start, int length) {
        int after = start - length;
        Iterator<AitSegment> iterator = segments.iterator();

        while (iterator.hasNext()) {
            AitSegment segment = iterator.next();
            // 从已有@块中删除
            if (start > segment.start) {
                // @被删除掉
                if (after <= segment.start) {
                    iterator.remove();
                } else if (after <= segment.end) {
                    segment.broken = true;
                    segment.end -= length;
                }
            } else {
                segment.start -= length;
                segment.end -= length;
            }
        }
    }

    /**
     * 获取该账号所有有效的@块最靠前的start
     *
     * @return
     */
    public int getFirstSegmentStart() {
        int start = -1;
        for (AitSegment segment : segments) {
            if (segment.broken) {
                continue;
            }
            if (start == -1 || segment.start < start) {
                start = segment.start;
            }
        }
        return start;
    }

    AitSegment findLastSegmentByEnd(int end) {
        int pos = end - 1;
        for (AitSegment segment : segments) {
            if (!segment.broken && segment.end == pos) {
                return segment;
            }
        }
        return null;
    }

    boolean valid() {
        if (segments.size() == 0) {
            return false;
        }
        for (AitSegment segment : segments) {
            if (!segment.broken) {
                return true;
            }
        }
        return false;
    }

    public static class AitSegment {
        /**
         * 位于文本起始位置(include)
         */
        public int start;

        /**
         * 位于文本结束位置(include)
         */
        public int end;

        /**
         * 是否坏掉
         */
        boolean broken = false;

        AitSegment(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
