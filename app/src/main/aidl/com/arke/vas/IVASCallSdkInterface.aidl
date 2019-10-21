package com.arke.vas;
import com.arke.vas.IVASListener;
import com.arke.vas.data.VASPayload;
// Declare any non-default types here with import statements
interface IVASCallSdkInterface {

    // Do sale.
    void sale(in VASPayload requestData, IVASListener listener);
}
