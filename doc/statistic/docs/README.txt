为了正确使用com.landicorp.epay.SystemStat.jar包，需要在工程的AndroidManifest.xml中的application字段中添加如下代码：
    <uses-library
        android:name="android.epay.dataacq"
        android:required="false" />
否则SystemStat会抛出NoSupportException，功能不可用。
下面是例子:

<application
    android:allowBackup="true"
    android:icon="@drawable/ic_launcher"
    android:label="@string/app_name"
    android:theme="@style/AppTheme" >
    
    <uses-library
        android:name="android.epay.dataacq"
        android:required="false" /><!-- 添加在此处 -->
    
</application>