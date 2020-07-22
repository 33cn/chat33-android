package com.fuzamei.componentservice.event;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaptureEvent {
    private int mType;
    private String mText;

    public CaptureEvent(int type, String text) {
        this.mType = type;
        this.mText = matcherImtokenAddress(text);
    }

    public int getType() {
        return mType;
    }

    public String getText() {
        return mText;
    }


    private String matcherImtokenAddress(String text) {
        Pattern compile = Pattern.compile("(?<=:).*(?=\\?)");
        Matcher matcher = compile.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }

        return text;
    }
}
