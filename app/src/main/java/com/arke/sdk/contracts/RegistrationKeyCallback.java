package com.arke.sdk.contracts;

import com.arke.sdk.models.RegistrationKey;

public interface RegistrationKeyCallback {
    void done(RegistrationKey registrationKey, Exception e);

}
