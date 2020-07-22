package com.fuzamei.componentservice.widget.dialog;

public interface DialogInterface{

    void dismiss();

    void cancel();


    interface OnClickListener {
        /**
         * This method will be invoked when the dialog is dismissed.
         *
         * @param dialog the dialog that was dismissed will be passed into the
         *               method
         */
        void onClick(DialogInterface dialog);
    }
}