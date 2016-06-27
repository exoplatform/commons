package org.exoplatform.commons.api.ui;

public class Response {
    private byte[] data;
    
    private String mimeType;

    public Response(byte[] data, String mimeType) {
        this.data = data;
        this.mimeType = mimeType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
}
