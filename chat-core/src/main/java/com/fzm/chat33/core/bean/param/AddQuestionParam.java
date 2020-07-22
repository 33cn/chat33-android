package com.fzm.chat33.core.bean.param;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2019/01/04
 * Description:
 */
public class AddQuestionParam implements Serializable {

    private int tp;
    private String question;
    private String answer;

    public AddQuestionParam() {
        this(2, null, null);
    }

    public AddQuestionParam(String question, String answer) {
        this(1, question, answer);
    }

    public AddQuestionParam(int tp, String question, String answer) {
        this.tp = tp;
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }
}
