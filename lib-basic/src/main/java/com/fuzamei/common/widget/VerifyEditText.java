package com.fuzamei.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.fuzamei.commonlib.R;

public class VerifyEditText extends AppCompatEditText {
    /**
     * 本控件的宽高
     */
    private int width;
    private int height;
    private OnVerifyInputCompleteListener listener;
    //TODO 保存输入的验证码集合
    private StringBuffer sb = new StringBuffer();
    //底部的线 画笔
    private Paint mLinePaint;
    //文字的画笔
    private Paint mTextPaint;
    //计算出的每一个输入框的宽度
    private int textLineLength;

    //验证码的数量
    private int totalCount;
    //每一个验证码之间的距离
    private int intervalLength;
    //底部线的颜色
    private int lineColor;
    //输入文字的颜色
    private int textColor;

    public VerifyEditText(Context context) {
        this(context, null);
    }

    public VerifyEditText(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    public VerifyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerifyEditText, defStyleAttr, 0);
        totalCount = typedArray.getInt(R.styleable.VerifyEditText_totalCount, 6);
        intervalLength = typedArray.getInt(R.styleable.VerifyEditText_intervalLength, 30);
        lineColor = typedArray.getColor(R.styleable.VerifyEditText_lineColor, getColor(R.color.basic_white));
        textColor = typedArray.getColor(R.styleable.VerifyEditText_textColor, getColor(R.color.basic_white));
        typedArray.recycle();
        initView();
    }

    private void initView() {
        setBackground(null);
        setMaxLines(1);
        setLines(1);
        /**
         * 必须重写setOnTouchListener 自己处理触摸事件，否则光标可以移动
         * 必须重写setOnLongClickListener 否则长按可以复制验证码
         * 不然光标就可以移动啦~~
         */
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                requestFocus();
                if (imm != null) {
                    imm.showSoftInput(VerifyEditText.this, 0);
                }
                return true;
            }
        });
        //TODO 设置输入的字体透明 不可见
        setTextColor(getColor(android.R.color.transparent));

        mLinePaint = new Paint();
        mLinePaint.setColor(lineColor);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(3);

        mTextPaint = new Paint();
        mTextPaint.setTextSize(sp2px(18));
        mTextPaint.setColor(textColor);
        mTextPaint.setAntiAlias(true);
        //TODO 增加输入字符的监听
        addTextChangedListener(textWatcher);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        //每一个输入文字的宽度
        textLineLength = (width - (totalCount - 1) * intervalLength) / totalCount;
        //定位光标的初始显示位置
        setPadding(textLineLength / 2, getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (totalCount <= 0) {
            return;
        }
        int currentLength = 0;
        //TODO 绘制文字
        for (int i = 0; i < totalCount; i++) {
            String textString = String.valueOf((sb != null && sb.length() > i) ? sb.charAt(i) : "");
            canvas.drawText(textString, currentLength + textLineLength / 2 - mTextPaint.measureText(textString) / 2, height / 2 + mTextPaint.getTextSize() / 2, mTextPaint);
            currentLength += textLineLength + intervalLength;
        }
        //TODO 绘制文字底部的线
        currentLength = 0;
        for (int i = 0; i < totalCount; i++) {
            canvas.drawLine(currentLength, height - 3, textLineLength * (i + 1) + intervalLength * i, height - 3, mLinePaint);
            currentLength += textLineLength + intervalLength;
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            sb.delete(0, sb.length());
            if (!TextUtils.isEmpty(s.toString())) {
                //TODO 只能输入指定totalCount个数的字多出的需要删除
                if (s.toString().length() > totalCount) {
                    s.delete(totalCount, s.length());
                    return;
                }
                sb.append(s);
                if (s.toString().length() == totalCount && listener != null) {
                    listener.onCompleteInput(sb.toString());
                }

            }
            int paddingLeft;
            /**
             * 计算验证码输入之后光标显示的位置
             * 如果已经输完验证码则光标需要显示在最后一个字符的后面而不是下一个文字输入框
             * */
            if (sb.length() < totalCount) {
                paddingLeft = (int) ((textLineLength + intervalLength) * sb.length()
                        + textLineLength / 2 - getPaint().measureText(!TextUtils.isEmpty(sb.toString()) ? sb.toString() : ""));

            } else {
                paddingLeft = (int) ((textLineLength + intervalLength) * (sb.length() - 1)
                        + (mTextPaint.measureText(sb.substring(sb.length() - 2, sb.length() - 1)) / 2)
                        + textLineLength / 2 - getPaint().measureText(!TextUtils.isEmpty(sb.toString()) ? sb.toString() : ""));
            }
            setPadding(paddingLeft, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        }
    };

    /**
     * 设置输入完成监听
     */
    public void setOnVerifyInputCompleteListener(OnVerifyInputCompleteListener listener) {
        this.listener = listener;
    }

    public interface OnVerifyInputCompleteListener {
        void onCompleteInput(String string);
    }

    /**
     * 获取输入的验证码
     */
    public String getVerifyCode() {
        return sb.toString();
    }

    public int getColor(int id) {
        return ContextCompat.getColor(getContext(), id);
    }

    public int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
