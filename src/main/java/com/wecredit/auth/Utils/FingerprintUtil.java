package com.wecredit.auth.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FingerprintUtil {

    public static boolean isSameDevice(String storedFingerprint, String newFingerprint) {
        try {
            if (storedFingerprint == null || newFingerprint == null) {
                log.warn("One or both fingerprints are null. storedFingerprint={}, newFingerprint={}", storedFingerprint, newFingerprint);
                return false;
            }

            boolean isSame = storedFingerprint.equals(newFingerprint);
            log.info("Comparing fingerprints: isSame={}", isSame);
            return isSame;
        } catch (Exception e) {
            log.error("Error while comparing fingerprints", e);
            return false;
        }
    }
}
