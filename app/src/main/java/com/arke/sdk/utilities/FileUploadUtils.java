package com.arke.sdk.utilities;

import android.os.AsyncTask;

import com.arke.sdk.companions.Credentials;
import com.arke.sdk.contracts.FileUploadDoneCallBack;
import com.filestack.Client;
import com.filestack.Config;
import com.filestack.FileLink;

import java.io.IOException;

public class FileUploadUtils {

    public static void uploadFile(String localFilePath,
                                  FileUploadDoneCallBack fileUploadDoneCallBack) {
        new AsyncTask<Void, Void, FileLinkOrException>() {

            private Config config;
            private Client client;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                config = new Config(Credentials.FILE_STACK_API_KEY);
                client = new Client(config);
            }

            @Override
            protected FileLinkOrException doInBackground(Void... voids) {
                try {
                    FileLink uploadedFileLink = client.upload(localFilePath, true);
                    if (uploadedFileLink != null) {
                        String fileHandle = "https://cdn.filestackcontent.com/"
                                + uploadedFileLink.getHandle();
                        EMenuLogger.d("FileUploader", "Uploaded FileUrl=" + fileHandle);
                        return new FileLinkOrException(fileHandle, null);
                    } else {
                        return new FileLinkOrException(null, new Exception("Sorry, failed to upload file. Please try again."));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return new FileLinkOrException(null, e);
                }
            }

            @Override
            protected void onPostExecute(FileLinkOrException fileLinkOrException) {
                super.onPostExecute(fileLinkOrException);
                String uploadedFilePath = fileLinkOrException.getFileLink();
                Exception exception = fileLinkOrException.getException();
                if (uploadedFilePath != null) {
                    fileUploadDoneCallBack.done(uploadedFilePath, null);
                } else {
                    fileUploadDoneCallBack.done(null, exception);
                }
            }

        }.execute();

    }

    static class FileLinkOrException {

        private String fileLink;
        private Exception exception;

        FileLinkOrException(String fileLink, Exception exception) {
            this.fileLink = fileLink;
            this.exception = exception;
        }

        String getFileLink() {
            return fileLink;
        }

        public Exception getException() {
            return exception;
        }

    }

}
