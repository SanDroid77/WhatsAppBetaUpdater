package com.sandroid.updaterforwhatsappbeta;

/**
 * Created by sandro on 31/05/2015.
 */
public class CompareVersion {
    public static boolean comparar (String versionInstalada, String ultimaVersion) {
        String[] arrVI = versionInstalada.split("\\.");
        String[] arrUV = ultimaVersion.split("\\.");

        Integer[] arrIntVI = new Integer[3];
        Integer[] arrIntUV = new Integer[3];

        for (int i = 0; i < arrVI.length; i++) {
            arrIntVI[i] = Integer.parseInt(arrVI[i]);
            arrIntUV[i] = Integer.parseInt(arrUV[i]);
        }

        if (arrIntVI[0] < arrIntUV[0]){
            return true;
        }
        else if (arrIntVI[1] < arrIntUV[1]) return true;
        else if (arrIntVI[2] < arrIntUV[2]) return true;
        else return false;
    }
}