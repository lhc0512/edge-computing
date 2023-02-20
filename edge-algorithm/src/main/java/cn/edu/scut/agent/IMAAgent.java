package cn.edu.scut.agent;

import java.io.InputStream;

public interface IMAAgent {

    int selectAction(float[] state, int[] availAction, boolean training);

    void train();

    void saveModel(String flag);

    void loadModel(String flag);

    void saveHdfsModel(String flag);

    void loadHdfsModel(String flag);

    void loadSteamModel(InputStream inputStream, String fileName);
}
