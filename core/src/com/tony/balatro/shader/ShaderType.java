package com.tony.balatro.shader;

public enum ShaderType {
    whirlpool("shader/loading.vert",
            "shader/loading.frag"),
    post("shader/loading.vert","shader/post.frag");
    private String vertFilePath;
    private String fragFilePath;
    ShaderType(String vert,String frag){
        this.vertFilePath = vert;
        this.fragFilePath = frag;
    }

    public String getFragFilePath() {
        return fragFilePath;
    }

    public String getVertFilePath() {
        return vertFilePath;
    }
}
