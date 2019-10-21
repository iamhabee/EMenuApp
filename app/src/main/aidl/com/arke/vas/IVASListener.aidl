package com.arke.vas;
import com.arke.vas.data.VASPayload;
/**
 * Value added service listener
 * <p>
 * 增值服务监听器
 */
interface IVASListener {
    /**
     * The transaction begins
     * <p>
     * 交易开始
     */
    void onStart();

    /**
     * Information feedback during the transaction
     *
     * @param message Feedback information
     * --------------------------------------
     * 交易过程中,信息反馈
     *
     * @param responseData 反馈的信息
     */
    void onNext(in VASPayload responseData);

    /**
     *
     * The end of the transaction
     *
     * @param responseData Response information
     *                     <br/>
     *                     Examples of consumption response：
     *                     <br/>
     *                     <img src="../../../image/sale_on_complete_en.png">
     * @see VASPayload
     * ---------------------------
     * 交易结束
     *
     * @param responseData 响应信息
     *                     <br/>
     *                     消费返回例子：
     *                     <br/>
     *                     <img src="../../../image/sale_on_complete_ch.png">
     * @see VASPayload
     */
    void onComplete(in VASPayload responseData);
}
